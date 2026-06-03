import { describe, it, expect, vi, beforeEach } from 'vitest'
import { atendimentoService } from '../../services/atendimentoService'
import { api } from '../../services/api'
import type { AtendimentoRequest, AtendimentoResponse } from '../../types/atendimento'

vi.mock('../../services/api', () => ({
  api: {
    get: vi.fn(),
    post: vi.fn(),
    put: vi.fn(),
    delete: vi.fn(),
  },
}))

const mockAtendimento: AtendimentoResponse = {
  id: '550e8400-e29b-41d4-a716-446655440000',
  descricaoServico: 'Troca de óleo',
  dataEntrada: '2026-06-03T10:00:00',
  dataConclusao: null,
  status: 'AGUARDANDO',
  dataCadastro: '2026-06-03T09:00:00',
  cliente: '550e8400-e29b-41d4-a716-446655440001',
  veiculo: 'ABC1D23',
  funcionario: '550e8400-e29b-41d4-a716-446655440002',
}

describe('atendimentoService', () => {
  beforeEach(() => {
    vi.clearAllMocks()
  })

  describe('listar', () => {
    it('chama GET /atendimentos/listar-ordem-decrescente', async () => {
      vi.mocked(api.get).mockResolvedValue({ data: [mockAtendimento] })
      const result = await atendimentoService.listar()
      expect(api.get).toHaveBeenCalledWith('/atendimentos/listar-ordem-decrescente')
      expect(result).toEqual([mockAtendimento])
    })
  })

  describe('buscarPorId', () => {
    it('chama GET /atendimentos/id/{id}', async () => {
      vi.mocked(api.get).mockResolvedValue({ data: mockAtendimento })
      const result = await atendimentoService.buscarPorId(mockAtendimento.id)
      expect(api.get).toHaveBeenCalledWith(`/atendimentos/id/${mockAtendimento.id}`)
      expect(result).toEqual(mockAtendimento)
    })
  })

  describe('cadastrar', () => {
    it('chama POST /atendimentos/cadastrar com o payload', async () => {
      const payload: AtendimentoRequest = {
        descricaoServico: 'Troca de óleo',
        statusAtendimento: 'AGUARDANDO',
        clienteId: '550e8400-e29b-41d4-a716-446655440001',
        veiculoPlaca: 'ABC1D23',
        funcionarioId: '550e8400-e29b-41d4-a716-446655440002',
      }
      vi.mocked(api.post).mockResolvedValue({ data: mockAtendimento })
      const result = await atendimentoService.cadastrar(payload)
      expect(api.post).toHaveBeenCalledWith('/atendimentos/cadastrar', payload)
      expect(result).toEqual(mockAtendimento)
    })
  })

  describe('atualizar', () => {
    it('chama PUT /atendimentos/atualizar/{id} com o payload', async () => {
      const payload: AtendimentoRequest = {
        descricaoServico: 'Troca de óleo e filtro',
        statusAtendimento: 'CONCLUIDO',
        clienteId: '550e8400-e29b-41d4-a716-446655440001',
        veiculoPlaca: 'ABC1D23',
        funcionarioId: '550e8400-e29b-41d4-a716-446655440002',
      }
      vi.mocked(api.put).mockResolvedValue({ data: mockAtendimento })
      const result = await atendimentoService.atualizar(mockAtendimento.id, payload)
      expect(api.put).toHaveBeenCalledWith(`/atendimentos/atualizar/${mockAtendimento.id}`, payload)
      expect(result).toEqual(mockAtendimento)
    })
  })

  describe('excluir', () => {
    it('chama DELETE /atendimentos/delete/{id}', async () => {
      vi.mocked(api.delete).mockResolvedValue({})
      await atendimentoService.excluir(mockAtendimento.id)
      expect(api.delete).toHaveBeenCalledWith(`/atendimentos/delete/${mockAtendimento.id}`)
    })
  })
})
