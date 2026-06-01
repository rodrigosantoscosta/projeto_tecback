import api from './api'
import type { LoginResponse, FuncionarioLogado } from '../types/auth'

export const authService = {
  login: (usuario: string, senha: string) =>
    api.post<LoginResponse>('/auth/login', { usuario, senha }),
  refresh: (refreshToken: string) =>
    api.post<LoginResponse>('/auth/refresh', { refreshToken }),
  logout: (refreshToken: string) =>
    api.post('/auth/logout', { refreshToken }),
  me: () =>
    api.get<FuncionarioLogado>('/funcionarios/me'),
}
