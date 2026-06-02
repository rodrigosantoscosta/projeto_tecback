export interface EnderecoViaCep {
  logradouro: string
  bairro: string
  localidade: string
  uf: string
  erro?: boolean
}

export interface ClienteRequest {
  nome: string
  cpfCNPJ: string
  telefone: string
  email?: string
  logradouro?: string
  numero?: string
  complemento?: string
  bairro?: string
  cidade?: string
  estado?: string
  cep?: string
}

export interface ClienteResponse {
  id: string
  nome: string
  cpfCNPJ: string
  telefone: string
  email: string | null
  logradouro: string | null
  numero: string | null
  complemento: string | null
  bairro: string | null
  cidade: string | null
  estado: string | null
  cep: string | null
  quantidadeVeiculos: number
  dataCadastro: string
}
