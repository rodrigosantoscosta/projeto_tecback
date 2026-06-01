export interface LoginRequest {
  usuario: string
  senha: string
}

export interface LoginResponse {
  accessToken: string
  refreshToken: string
  tokenType: string
}

export interface JwtPayload {
  sub: string
  id: string
  cargo: string
  iat: number
  exp: number
}

export interface FuncionarioLogado {
  id: string
  nome: string
  cpfCNPJ: string
  cargo: string
  telefone: string | null
  email: string | null
  usuario: string
  dataCadastro: string
}

export interface AuthState {
  token: string | null
  refreshToken: string | null
  funcionario: FuncionarioLogado | null
  login: (usuario: string, senha: string) => Promise<void>
  logout: () => void
  isExpired: () => boolean
}
