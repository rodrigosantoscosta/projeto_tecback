import { api } from './api'
import type { AtendimentoRequest, AtendimentoResponse } from '../types/atendimento'

export const atendimentoService = {
  listar: () => api.get<AtendimentoResponse[]>('/atendimentos/listar-ordem-decrescente').then(r => r.data),

  buscarPorId: (id: string) => api.get<AtendimentoResponse>(`/atendimentos/id/${id}`).then(r => r.data),

  cadastrar: (data: AtendimentoRequest) =>
    api.post<AtendimentoResponse>('/atendimentos/cadastrar', data).then(r => r.data),

  atualizar: (id: string, data: AtendimentoRequest) =>
    api.put<AtendimentoResponse>(`/atendimentos/atualizar/${id}`, data).then(r => r.data),

  excluir: (id: string) => api.delete(`/atendimentos/delete/${id}`),
}
