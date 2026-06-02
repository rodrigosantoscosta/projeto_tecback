export type CombustívelTipo = 'GASOLINA' | 'ETANOL' | 'DIESEL' | 'FLEX' | 'ELETRICO' | 'HIBRIDO' | 'GNV'

export interface VeiculoRequest {
  placa: string
  marca: string
  modelo: string
  anoFabricacao: number
  cor?: string
  combustivel?: CombustívelTipo
  clienteId: string
}

export interface VeiculoResponse {
  id: string
  placa: string
  marca: string
  modelo: string
  anoFabricacao: number
  cor: string | null
  combustivel: CombustívelTipo | null
  clienteId: string
  clienteNome: string
  dataCadastro: string
}
