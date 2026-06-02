import { api } from './api'
import type { ClienteRequest, ClienteResponse, EnderecoViaCep } from '../types/cliente'

export const clienteService = {
  listar: () => api.get<ClienteResponse[]>('/clientes').then(r => r.data),

  buscarPorId: (id: string) => api.get<ClienteResponse>(`/clientes/${id}`).then(r => r.data),

  cadastrar: (data: ClienteRequest) => api.post<ClienteResponse>('/clientes', data).then(r => r.data),

  atualizar: (id: string, data: ClienteRequest) =>
    api.put<ClienteResponse>(`/clientes/${id}`, data).then(r => r.data),

  excluir: (id: string) => api.delete(`/clientes/${id}`),

  consultarCep: (cep: string) =>
    fetch(`https://viacep.com.br/ws/${cep.replace(/\D/g, '')}/json/`)
      .then(r => r.json() as Promise<EnderecoViaCep>),
}
