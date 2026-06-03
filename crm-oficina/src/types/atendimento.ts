export const StatusAtendimento = {
  AGUARDANDO: 'AGUARDANDO',
  ANDAMENTO: 'ANDAMENTO',
  CONCLUIDO: 'CONCLUIDO',
  CANCELADO: 'CANCELADO',
} as const

export type StatusAtendimento = (typeof StatusAtendimento)[keyof typeof StatusAtendimento]

export const STATUS_LABELS: Record<StatusAtendimento, string> = {
  [StatusAtendimento.AGUARDANDO]: 'Aguardando',
  [StatusAtendimento.ANDAMENTO]: 'Em Andamento',
  [StatusAtendimento.CONCLUIDO]: 'Concluído',
  [StatusAtendimento.CANCELADO]: 'Cancelado',
}

export const STATUS_ORDER: StatusAtendimento[] = [
  StatusAtendimento.AGUARDANDO,
  StatusAtendimento.ANDAMENTO,
  StatusAtendimento.CONCLUIDO,
  StatusAtendimento.CANCELADO,
]

export interface AtendimentoRequest {
  descricaoServico: string
  statusAtendimento: string
  clienteId: string
  veiculoPlaca: string
  funcionarioId: string
}

export interface AtendimentoResponse {
  id: string
  descricaoServico: string
  dataEntrada: string
  dataConclusao: string | null
  status: StatusAtendimento
  dataCadastro: string
  cliente: string
  veiculo: string
  funcionario: string
}
