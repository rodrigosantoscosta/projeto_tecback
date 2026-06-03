import { http, HttpResponse } from 'msw'
import { setupServer } from 'msw/node'
import { afterAll, afterEach, beforeAll, describe, expect, it } from 'vitest'
import { renderHook, waitFor } from '@testing-library/react'
import { QueryClient, QueryClientProvider } from '@tanstack/react-query'
import type { ReactNode } from 'react'
import {
  useAtendimentos,
  useAtendimento,
  useCadastrarAtendimento,
  useExcluirAtendimento,
} from '../../hooks/useAtendimentos'
import type { AtendimentoResponse, AtendimentoRequest } from '../../types/atendimento'

const BASE_URL = 'http://localhost:8080'

const mockAtendimentos: AtendimentoResponse[] = [
  {
    id: 'a1', descricaoServico: 'Troca de óleo',
    dataEntrada: '2026-06-03T10:00:00', dataConclusao: null,
    status: 'AGUARDANDO', dataCadastro: '2026-06-03T09:00:00',
    cliente: 'c1', veiculo: 'ABC1D23', funcionario: 'f1',
  },
  {
    id: 'a2', descricaoServico: 'Revisão geral',
    dataEntrada: '2026-06-02T10:00:00', dataConclusao: '2026-06-02T16:00:00',
    status: 'CONCLUIDO', dataCadastro: '2026-06-02T09:00:00',
    cliente: 'c2', veiculo: 'XYZ9K99', funcionario: 'f2',
  },
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

describe('useAtendimentos', () => {
  it('retorna lista de atendimentos', async () => {
    server.use(
      http.get(`${BASE_URL}/atendimentos/listar-ordem-decrescente`, () =>
        HttpResponse.json(mockAtendimentos),
      ),
    )

    const { result } = renderHook(() => useAtendimentos(), { wrapper: createWrapper() })

    await waitFor(() => expect(result.current.isSuccess).toBe(true))
    expect(result.current.data).toEqual(mockAtendimentos)
    expect(result.current.data).toHaveLength(2)
  })

  it('retorna array vazio quando não há atendimentos', async () => {
    server.use(
      http.get(`${BASE_URL}/atendimentos/listar-ordem-decrescente`, () =>
        HttpResponse.json([]),
      ),
    )

    const { result } = renderHook(() => useAtendimentos(), { wrapper: createWrapper() })

    await waitFor(() => expect(result.current.isSuccess).toBe(true))
    expect(result.current.data).toEqual([])
  })
})

describe('useAtendimento', () => {
  it('retorna atendimento por id', async () => {
    server.use(
      http.get(`${BASE_URL}/atendimentos/id/:id`, ({ params }) => {
        const item = mockAtendimentos.find(a => a.id === params.id)
        return item ? HttpResponse.json(item) : new HttpResponse(null, { status: 404 })
      }),
    )

    const { result } = renderHook(() => useAtendimento('a1'), { wrapper: createWrapper() })

    await waitFor(() => expect(result.current.isSuccess).toBe(true))
    expect(result.current.data?.descricaoServico).toBe('Troca de óleo')
  })

  it('não faz fetch quando id é vazio', () => {
    const { result } = renderHook(() => useAtendimento(''), { wrapper: createWrapper() })
    expect(result.current.fetchStatus).toBe('idle')
  })
})

describe('useCadastrarAtendimento', () => {
  it('chama POST e invalida cache', async () => {
    let posted = false
    server.use(
      http.post(`${BASE_URL}/atendimentos/cadastrar`, async ({ request }) => {
        posted = true
        const body = await request.json() as AtendimentoRequest
        expect(body.descricaoServico).toBe('Novo serviço')
        return HttpResponse.json(mockAtendimentos[0], { status: 201 })
      }),
      http.get(`${BASE_URL}/atendimentos/listar-ordem-decrescente`, () =>
        HttpResponse.json(mockAtendimentos),
      ),
    )

    const { result } = renderHook(() => useCadastrarAtendimento(), { wrapper: createWrapper() })

    const payload: AtendimentoRequest = {
      descricaoServico: 'Novo serviço',
      statusAtendimento: 'AGUARDANDO',
      clienteId: 'c1',
      veiculoPlaca: 'ABC1D23',
      funcionarioId: 'f1',
    }

    await result.current.mutateAsync(payload)
    expect(posted).toBe(true)
  })
})

describe('useExcluirAtendimento', () => {
  it('chama DELETE e invalida cache', async () => {
    let deleted = false
    server.use(
      http.delete(`${BASE_URL}/atendimentos/delete/:id`, () => {
        deleted = true
        return HttpResponse.json('Atendimento deletado com sucesso!')
      }),
      http.get(`${BASE_URL}/atendimentos/listar-ordem-decrescente`, () =>
        HttpResponse.json(mockAtendimentos),
      ),
    )

    const { result } = renderHook(() => useExcluirAtendimento(), { wrapper: createWrapper() })

    await result.current.mutateAsync('a1')
    expect(deleted).toBe(true)
  })
})
