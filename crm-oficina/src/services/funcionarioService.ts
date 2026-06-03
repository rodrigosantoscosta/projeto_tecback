import { api } from './api'

export interface FuncionarioDTO {
  id: string
  nome: string
  cpfCNPJ: string
  cargo: string
  telefone: string | null
  email: string | null
  usuario: string
  dataCadastro: string
}

export const funcionarioService = {
  listar: () => api.get<FuncionarioDTO[]>('/funcionarios').then(r => r.data),
}
