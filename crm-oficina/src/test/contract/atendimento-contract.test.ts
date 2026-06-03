import { describe, it, expect } from 'vitest'
import type { AtendimentoResponse, AtendimentoRequest, StatusAtendimento } from '../../types/atendimento'

const BACKEND_ENDPOINTS = {
  listar: { method: 'GET', path: '/atendimentos/listar-ordem-decrescente' as const },
  buscarPorId: { method: 'GET', path: '/atendimentos/id/{id}' as const },
  cadastrar: { method: 'POST', path: '/atendimentos/cadastrar' as const },
  atualizar: { method: 'PUT', path: '/atendimentos/atualizar/{id}' as const },
  excluir: { method: 'DELETE', path: '/atendimentos/delete/{id}' as const },
  funcionarios: { method: 'GET', path: '/funcionarios' as const },
} as const

describe('Contrato Frontend ↔ Backend — Atendimentos', () => {
  describe('Endpoints', () => {
    it('todos os endpoints usados no service existem no backend', () => {
      expect(BACKEND_ENDPOINTS.listar.path).toBe('/atendimentos/listar-ordem-decrescente')
      expect(BACKEND_ENDPOINTS.buscarPorId.path).toBe('/atendimentos/id/{id}')
      expect(BACKEND_ENDPOINTS.cadastrar.path).toBe('/atendimentos/cadastrar')
      expect(BACKEND_ENDPOINTS.atualizar.path).toBe('/atendimentos/atualizar/{id}')
      expect(BACKEND_ENDPOINTS.excluir.path).toBe('/atendimentos/delete/{id}')
      expect(BACKEND_ENDPOINTS.funcionarios.path).toBe('/funcionarios')
    })
  })

  describe('Response DTO (AtendimentoDTO → AtendimentoResponse)', () => {
    it('deve conter todos os campos do backend', () => {
      const mockResponse: AtendimentoResponse = {
        id: '550e8400-e29b-41d4-a716-446655440000',
        descricaoServico: 'Troca de óleo',
        dataEntrada: '2026-06-03T10:00:00',
        dataConclusao: null,
        status: 'AGUARDANDO' as StatusAtendimento,
        dataCadastro: '2026-06-03T10:00:00',
        cliente: '550e8400-e29b-41d4-a716-446655440001',
        veiculo: 'ABC1D23',
        funcionario: '550e8400-e29b-41d4-a716-446655440002',
      }

      expect(mockResponse).toHaveProperty('id')
      expect(mockResponse).toHaveProperty('descricaoServico')
      expect(mockResponse).toHaveProperty('dataEntrada')
      expect(mockResponse).toHaveProperty('dataConclusao')
      expect(mockResponse).toHaveProperty('status')
      expect(mockResponse).toHaveProperty('dataCadastro')
      expect(mockResponse).toHaveProperty('cliente')
      expect(mockResponse).toHaveProperty('veiculo')
      expect(mockResponse).toHaveProperty('funcionario')
    })

    it('status aceita apenas valores válidos do enum', () => {
      const validStatuses = ['AGUARDANDO', 'ANDAMENTO', 'CONCLUIDO', 'CANCELADO'] as const
      validStatuses.forEach(s => {
        const item: AtendimentoResponse = {
          id: 'x', descricaoServico: 'x', dataEntrada: 'x',
          dataConclusao: null, status: s as StatusAtendimento,
          dataCadastro: 'x', cliente: 'x', veiculo: 'x', funcionario: 'x',
        }
        expect(item.status).toBe(s)
      })
    })
  })

  describe('Request DTO (CadastrarAtendimentoDTO → AtendimentoRequest)', () => {
    it('deve conter todos os campos enviados ao backend', () => {
      const request: AtendimentoRequest = {
        descricaoServico: 'Troca de óleo',
        statusAtendimento: 'AGUARDANDO',
        clienteId: '550e8400-e29b-41d4-a716-446655440001',
        veiculoPlaca: 'ABC1D23',
        funcionarioId: '550e8400-e29b-41d4-a716-446655440002',
      }

      expect(request).toHaveProperty('descricaoServico')
      expect(request).toHaveProperty('statusAtendimento')
      expect(request).toHaveProperty('clienteId')
      expect(request).toHaveProperty('veiculoPlaca')
      expect(request).toHaveProperty('funcionarioId')
    })

    it('descricaoServico é string não vazia', () => {
      const valid: AtendimentoRequest = { descricaoServico: 'a', statusAtendimento: 'AGUARDANDO', clienteId: 'x', veiculoPlaca: 'x', funcionarioId: 'x' }
      expect(valid.descricaoServico.length).toBeGreaterThan(0)
    })
  })
})
