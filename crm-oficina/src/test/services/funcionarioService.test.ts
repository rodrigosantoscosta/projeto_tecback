import { describe, it, expect, vi, beforeEach } from 'vitest'
import { funcionarioService } from '../../services/funcionarioService'
import { api } from '../../services/api'

vi.mock('../../services/api', () => ({
  api: {
    get: vi.fn(),
  },
}))

const mockFuncionarios = [
  { id: 'f1', nome: 'João', cpfCNPJ: '12345678901', cargo: 'Mecânico', telefone: null, email: null, usuario: 'joao', dataCadastro: '2026-01-01T00:00:00' },
  { id: 'f2', nome: 'Maria', cpfCNPJ: '98765432101', cargo: 'Chefe', telefone: '11999999999', email: 'maria@oficina.com', usuario: 'maria', dataCadastro: '2026-01-01T00:00:00' },
]

describe('funcionarioService', () => {
  beforeEach(() => vi.clearAllMocks())

  describe('listar', () => {
    it('chama GET /funcionarios e retorna lista', async () => {
      vi.mocked(api.get).mockResolvedValue({ data: mockFuncionarios })
      const result = await funcionarioService.listar()
      expect(api.get).toHaveBeenCalledWith('/funcionarios')
      expect(result).toHaveLength(2)
      expect(result[0].nome).toBe('João')
      expect(result[1].nome).toBe('Maria')
    })
  })
})
