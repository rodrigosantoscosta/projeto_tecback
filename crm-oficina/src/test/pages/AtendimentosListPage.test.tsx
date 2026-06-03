import { describe, expect, it, vi, beforeEach } from 'vitest'
import { render, screen, waitFor } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { BrowserRouter } from 'react-router-dom'
import { QueryClient, QueryClientProvider } from '@tanstack/react-query'
import type { ReactNode } from 'react'
import { AtendimentosListPage } from '../../pages/atendimentos/AtendimentosListPage'
import type { AtendimentoResponse, StatusAtendimento } from '../../types/atendimento'

const mockAtendimentos: AtendimentoResponse[] = [
  {
    id: 'a1',
    descricaoServico: 'Troca de óleo',
    dataEntrada: '2026-06-03T10:00:00',
    dataConclusao: null,
    status: 'AGUARDANDO' as StatusAtendimento,
    dataCadastro: '2026-06-03T09:00:00',
    cliente: 'c1',
    veiculo: 'ABC1D23',
    funcionario: 'f1',
  },
  {
    id: 'a2',
    descricaoServico: 'Revisão completa',
    dataEntrada: '2026-06-02T10:00:00',
    dataConclusao: '2026-06-02T16:00:00',
    status: 'CONCLUIDO' as StatusAtendimento,
    dataCadastro: '2026-06-02T09:00:00',
    cliente: 'c2',
    veiculo: 'XYZ9K99',
    funcionario: 'f2',
  },
]

vi.mock('../../hooks/useAtendimentos', () => ({
  useAtendimentos: vi.fn(),
  useExcluirAtendimento: vi.fn(() => ({
    mutateAsync: vi.fn().mockResolvedValue(undefined),
    isPending: false,
  })),
}))

vi.mock('../../hooks/useClientes', () => ({
  useClientes: vi.fn(() => ({
    data: [
      { id: 'c1', nomeCompleto: 'João Silva' },
      { id: 'c2', nomeCompleto: 'Maria Santos' },
    ],
  })),
}))

vi.mock('../../hooks/useFuncionarios', () => ({
  useFuncionarios: vi.fn(() => ({
    data: [
      { id: 'f1', nome: 'Carlos' },
      { id: 'f2', nome: 'Ana' },
    ],
  })),
}))

import { useAtendimentos, useExcluirAtendimento } from '../../hooks/useAtendimentos'

function createWrapper() {
  const qc = new QueryClient({ defaultOptions: { queries: { retry: false } } })
  return function Wrapper({ children }: { children: ReactNode }) {
    return (
      <QueryClientProvider client={qc}>
        <BrowserRouter>{children}</BrowserRouter>
      </QueryClientProvider>
    )
  }
}

describe('AtendimentosListPage', () => {
  beforeEach(() => vi.clearAllMocks())

  it('renderiza título e botão novo atendimento', () => {
    vi.mocked(useAtendimentos).mockReturnValue({
      data: [],
      isLoading: false,
      isSuccess: true,
    } as ReturnType<typeof useAtendimentos>)

    render(<AtendimentosListPage />, { wrapper: createWrapper() })
    expect(screen.getByText('Atendimentos')).toBeInTheDocument()
    expect(screen.getByText('Novo atendimento')).toBeInTheDocument()
  })

  it('exibe Carregando... enquanto carrega', () => {
    vi.mocked(useAtendimentos).mockReturnValue({
      data: [],
      isLoading: true,
    } as ReturnType<typeof useAtendimentos>)

    render(<AtendimentosListPage />, { wrapper: createWrapper() })
    expect(screen.getByText('Carregando...')).toBeInTheDocument()
  })

  it('renderiza tabela com atendimentos', async () => {
    vi.mocked(useAtendimentos).mockReturnValue({
      data: mockAtendimentos,
      isLoading: false,
      isSuccess: true,
    } as ReturnType<typeof useAtendimentos>)

    render(<AtendimentosListPage />, { wrapper: createWrapper() })

    await waitFor(() => {
      expect(screen.getByText('Troca de óleo')).toBeInTheDocument()
      expect(screen.getByText('Revisão completa')).toBeInTheDocument()
    })
  })

  it('exibe "Nenhum resultado encontrado" quando lista vazia', async () => {
    vi.mocked(useAtendimentos).mockReturnValue({
      data: [],
      isLoading: false,
      isSuccess: true,
    } as ReturnType<typeof useAtendimentos>)

    render(<AtendimentosListPage />, { wrapper: createWrapper() })

    await waitFor(() => {
      expect(screen.getByText('Nenhum resultado encontrado.')).toBeInTheDocument()
    })
  })

  it('abre modal de exclusão ao clicar em Trash2', async () => {
    const user = userEvent.setup()

    vi.mocked(useAtendimentos).mockReturnValue({
      data: mockAtendimentos,
      isLoading: false,
      isSuccess: true,
    } as ReturnType<typeof useAtendimentos>)

    render(<AtendimentosListPage />, { wrapper: createWrapper() })

    await waitFor(() => {
      expect(screen.getByText('Troca de óleo')).toBeInTheDocument()
    })

    const deleteButtons = screen.getAllByTitle('Excluir')
    await user.click(deleteButtons[0])

    await waitFor(() => {
      expect(screen.getByText('Excluir atendimento')).toBeInTheDocument()
    })
  })

  it('renderiza o filtro por status', async () => {
    vi.mocked(useAtendimentos).mockReturnValue({
      data: mockAtendimentos,
      isLoading: false,
      isSuccess: true,
    } as ReturnType<typeof useAtendimentos>)

    render(<AtendimentosListPage />, { wrapper: createWrapper() })

    await waitFor(() => {
      expect(screen.getByText('Filtrar por status')).toBeInTheDocument()
    })
  })
})
