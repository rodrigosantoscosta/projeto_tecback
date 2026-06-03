export interface EnderecoViaCep {
  logradouro: string
  bairro: string
  localidade: string
  uf: string
  erro?: boolean
}

export interface EnderecoResponse {
  id: number
  cep: string | null
  logradouro: string | null
  numero: string | null
  complemento: string | null
  bairro: string | null
  cidade: string | null
  estado: string | null
}

export interface ClienteRequest {
  nomeCompleto: string
  cpfCNPJ: string
  telefone: string
  email: string
  cep: string
  numero: string
  complemento?: string
}

export interface ClienteResponse {
  id: string
  nomeCompleto: string
  cpfCNPJ: string
  telefone: string
  email: string | null
  endereco: EnderecoResponse | null
  quantidadeVeiculos: number
  dataCadastro: string
}
