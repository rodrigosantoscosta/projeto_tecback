import { http, HttpResponse } from 'msw'
import { setupServer } from 'msw/node'
import { afterAll, afterEach, beforeAll, describe, expect, it } from 'vitest'
import { renderHook, waitFor } from '@testing-library/react'
import { QueryClient, QueryClientProvider } from '@tanstack/react-query'
import type { ReactNode } from 'react'
import { useFuncionarios } from '../../hooks/useFuncionarios'

const BASE_URL = 'http://localhost:8080'

const mockFuncionarios = [
  { id: 'f1', nome: 'João', cpfCNPJ: '12345678901', cargo: 'Mecânico', telefone: null, email: null, usuario: 'joao', dataCadastro: '2026-01-01T00:00:00' },
  { id: 'f2', nome: 'Maria', cpfCNPJ: '98765432101', cargo: 'Chefe', telefone: '11999999999', email: 'maria@oficina.com', usuario: 'maria', dataCadastro: '2026-01-01T00:00:00' },
]

const server = setupServer()

function createWrapper() {
  const qc = new QueryClient({ defaultOptions: { queries: { retry: false } } })
  return function Wrapper({ children }: { children: ReactNode }) {
    return <QueryClientProvider client={qc}>{children}</QueryClientProvider>
  }
}

beforeAll(() => server.listen())
afterEach(() => server.resetHandlers())
afterAll(() => server.close())

describe('useFuncionarios', () => {
  it('retorna lista de funcionários', async () => {
    server.use(
      http.get(`${BASE_URL}/funcionarios`, () =>
        HttpResponse.json(mockFuncionarios),
      ),
    )

    const { result } = renderHook(() => useFuncionarios(), { wrapper: createWrapper() })

    await waitFor(() => expect(result.current.isSuccess).toBe(true))
    expect(result.current.data).toHaveLength(2)
    expect(result.current.data![0].nome).toBe('João')
    expect(result.current.data![1].cargo).toBe('Chefe')
  })

  it('retorna array vazio quando não há funcionários', async () => {
    server.use(
      http.get(`${BASE_URL}/funcionarios`, () => HttpResponse.json([])),
    )

    const { result } = renderHook(() => useFuncionarios(), { wrapper: createWrapper() })

    await waitFor(() => expect(result.current.isSuccess).toBe(true))
    expect(result.current.data).toEqual([])
  })
})
