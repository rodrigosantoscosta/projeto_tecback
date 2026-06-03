import { describe, expect, it, vi, beforeEach } from 'vitest'
import { render, screen, waitFor } from '@testing-library/react'
import { MemoryRouter, Route, Routes } from 'react-router-dom'
import { QueryClient, QueryClientProvider } from '@tanstack/react-query'
import type { ReactNode } from 'react'
import { AtendimentoDetailPage } from '../../pages/atendimentos/AtendimentoDetailPage'
import type { AtendimentoResponse, StatusAtendimento } from '../../types/atendimento'

const mockAtendimento: AtendimentoResponse = {
  id: 'a1',
  descricaoServico: 'Troca de óleo',
  dataEntrada: '2026-06-03T10:00:00',
  dataConclusao: null,
  status: 'AGUARDANDO' as StatusAtendimento,
  dataCadastro: '2026-06-03T09:00:00',
  cliente: 'c1',
  veiculo: 'ABC1D23',
  funcionario: 'f1',
}

vi.mock('../../hooks/useAtendimentos', () => ({
  useAtendimento: vi.fn(),
}))

vi.mock('../../hooks/useClientes', () => ({
  useClientes: vi.fn(() => ({
    data: [
      { id: 'c1', nomeCompleto: 'João Silva' },
    ],
  })),
}))

vi.mock('../../hooks/useFuncionarios', () => ({
  useFuncionarios: vi.fn(() => ({
    data: [
      { id: 'f1', nome: 'Carlos Mecânico' },
    ],
  })),
}))

import { useAtendimento } from '../../hooks/useAtendimentos'

function createWrapper(initialRoute = '/atendimentos/a1') {
  const qc = new QueryClient({ defaultOptions: { queries: { retry: false } } })
  return function Wrapper({ children }: { children: ReactNode }) {
    return (
      <QueryClientProvider client={qc}>
        <MemoryRouter initialEntries={[initialRoute]}>
          <Routes>
            <Route path="/atendimentos/:id" element={children} />
          </Routes>
        </MemoryRouter>
      </QueryClientProvider>
    )
  }
}

describe('AtendimentoDetailPage', () => {
  beforeEach(() => vi.clearAllMocks())

  it('exibe Carregando... enquanto carrega', () => {
    vi.mocked(useAtendimento).mockReturnValue({
      data: undefined,
      isLoading: true,
    } as ReturnType<typeof useAtendimento>)

    render(<AtendimentoDetailPage />, { wrapper: createWrapper() })
    expect(screen.getByText('Carregando...')).toBeInTheDocument()
  })

  it('exibe dados do atendimento', async () => {
    vi.mocked(useAtendimento).mockReturnValue({
      data: mockAtendimento,
      isLoading: false,
      isSuccess: true,
    } as ReturnType<typeof useAtendimento>)

    render(<AtendimentoDetailPage />, { wrapper: createWrapper() })

    await waitFor(() => {
      expect(screen.getByText('Troca de óleo')).toBeInTheDocument()
      expect(screen.getByText('ABC1D23')).toBeInTheDocument()
      expect(screen.getByText('Carlos Mecânico')).toBeInTheDocument()
    })
    expect(screen.getAllByText('Aguardando').length).toBeGreaterThanOrEqual(1)
  })

  it('exibe link para o cliente', async () => {
    vi.mocked(useAtendimento).mockReturnValue({
      data: mockAtendimento,
      isLoading: false,
      isSuccess: true,
    } as ReturnType<typeof useAtendimento>)

    render(<AtendimentoDetailPage />, { wrapper: createWrapper() })

    await waitFor(() => {
      const clienteBtn = screen.getByText('João Silva')
      expect(clienteBtn).toBeInTheDocument()
      expect(clienteBtn.tagName).toBe('BUTTON')
    })
  })

  it('exibe "não encontrado" quando não há atendimento', () => {
    vi.mocked(useAtendimento).mockReturnValue({
      data: undefined,
      isLoading: false,
      isSuccess: false,
    } as ReturnType<typeof useAtendimento>)

    render(<AtendimentoDetailPage />, { wrapper: createWrapper() })
    expect(screen.getByText('Atendimento não encontrado.')).toBeInTheDocument()
  })

  it('exibe data de conclusão quando disponível', async () => {
    const concluido = {
      ...mockAtendimento,
      status: 'CONCLUIDO' as StatusAtendimento,
      dataConclusao: '2026-06-03T16:00:00',
    }

    vi.mocked(useAtendimento).mockReturnValue({
      data: concluido,
      isLoading: false,
      isSuccess: true,
    } as ReturnType<typeof useAtendimento>)

    render(<AtendimentoDetailPage />, { wrapper: createWrapper() })

    await waitFor(() => {
      expect(screen.getAllByText('Concluído').length).toBeGreaterThanOrEqual(1)
    })
  })
})
