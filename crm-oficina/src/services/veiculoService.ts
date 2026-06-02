import { api } from './api'
import type { VeiculoRequest, VeiculoResponse } from '../types/veiculo'

export const veiculoService = {
  listar: () => api.get<VeiculoResponse[]>('/veiculos').then(r => r.data),

  buscarPorId: (id: string) => api.get<VeiculoResponse>(`/veiculos/${id}`).then(r => r.data),

  listarPorCliente: (clienteId: string) =>
    api.get<VeiculoResponse[]>(`/veiculos/cliente/${clienteId}`).then(r => r.data),

  cadastrar: (data: VeiculoRequest) => api.post<VeiculoResponse>('/veiculos', data).then(r => r.data),

  atualizar: (id: string, data: VeiculoRequest) =>
    api.put<VeiculoResponse>(`/veiculos/${id}`, data).then(r => r.data),

  excluir: (id: string) => api.delete(`/veiculos/${id}`),
}
