export interface VeiculoRequest {
  placa: string
  marca: string
  modelo: string
  ano: number
  cor?: string
  quilometragem?: number
  clienteId: string
}

export interface VeiculoResponse {
  id: string
  placa: string
  marca: string
  modelo: string
  ano: number
  cor: string | null
  quilometragem: number | null
  dataCadastro: string
  clienteId: string
}
