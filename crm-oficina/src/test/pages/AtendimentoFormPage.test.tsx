import { describe, expect, it, vi, beforeEach } from 'vitest'
import { render, screen } from '@testing-library/react'
import { MemoryRouter, Route, Routes } from 'react-router-dom'
import { QueryClient, QueryClientProvider } from '@tanstack/react-query'
import type { ReactNode } from 'react'
import { AtendimentoFormPage } from '../../pages/atendimentos/AtendimentoFormPage'

vi.mock('../../hooks/useAtendimentos', () => ({
  useAtendimento: vi.fn(() => ({ data: undefined, isLoading: false })),
  useCadastrarAtendimento: vi.fn(() => ({ mutateAsync: vi.fn().mockResolvedValue(undefined), isPending: false, error: null })),
  useAtualizarAtendimento: vi.fn(() => ({ mutateAsync: vi.fn().mockResolvedValue(undefined), isPending: false, error: null })),
}))

vi.mock('../../hooks/useClientes', () => ({
  useClientes: vi.fn(() => ({ data: [{ id: 'c1', nomeCompleto: 'João Silva' }] })),
}))

vi.mock('../../hooks/useVeiculos', () => ({
  useVeiculosPorCliente: vi.fn(() => ({ data: [{ placa: 'ABC1D23', marca: 'Toyota', modelo: 'Corolla' }] })),
}))

vi.mock('../../hooks/useFuncionarios', () => ({
  useFuncionarios: vi.fn(() => ({ data: [{ id: 'f1', nome: 'Carlos', cargo: 'Mecânico' }] })),
}))

function createWrapper(initialRoute = '/atendimentos/novo') {
  const qc = new QueryClient({ defaultOptions: { queries: { retry: false } } })
  return function Wrapper({ children }: { children: ReactNode }) {
    return (
      <QueryClientProvider client={qc}>
        <MemoryRouter initialEntries={[initialRoute]}>
          <Routes>
            <Route path="/atendimentos/novo" element={children} />
            <Route path="/atendimentos" element={<div>Lista de atendimentos</div>} />
          </Routes>
        </MemoryRouter>
      </QueryClientProvider>
    )
  }
}

describe('AtendimentoFormPage (criação)', () => {
  beforeEach(() => vi.clearAllMocks())

  it('renderiza título "Novo atendimento"', () => {
    render(<AtendimentoFormPage />, { wrapper: createWrapper() })
    expect(screen.getByText('Novo atendimento')).toBeInTheDocument()
  })

  it('renderiza campo de descrição do serviço', () => {
    render(<AtendimentoFormPage />, { wrapper: createWrapper() })
    expect(screen.getByPlaceholderText('Troca de óleo e filtros')).toBeInTheDocument()
  })

  it('renderiza botão "Cadastrar atendimento"', () => {
    render(<AtendimentoFormPage />, { wrapper: createWrapper() })
    expect(screen.getByText('Cadastrar atendimento')).toBeInTheDocument()
  })

  it('renderiza botão Voltar', () => {
    render(<AtendimentoFormPage />, { wrapper: createWrapper() })
    expect(screen.getByText('Voltar')).toBeInTheDocument()
  })
})
