import { useNavigate, useParams } from 'react-router-dom'
import { ArrowLeft, Pencil } from 'lucide-react'
import { Button } from '../../components/ui/button'
import { Badge } from '../../components/ui/badge'
import { Card, CardContent, CardHeader, CardTitle } from '../../components/ui/card'
import { useAtendimento } from '../../hooks/useAtendimentos'
import { useClientes } from '../../hooks/useClientes'
import { useFuncionarios } from '../../hooks/useFuncionarios'
import { STATUS_LABELS } from '../../types/atendimento'
import type { StatusAtendimento } from '../../types/atendimento'
import { formatDate } from '../../utils/formatadores'

const statusBadgeClass: Record<StatusAtendimento, string> = {
  AGUARDANDO: 'bg-yellow-100 text-yellow-800 border-yellow-300',
  ANDAMENTO: 'bg-blue-100 text-blue-800 border-blue-300',
  CONCLUIDO: 'bg-emerald-100 text-emerald-800 border-emerald-300',
  CANCELADO: 'bg-red-100 text-red-800 border-red-300',
}

export function AtendimentoDetailPage() {
  const navigate = useNavigate()
  const { id } = useParams<{ id: string }>()
  const { data: atendimento, isLoading } = useAtendimento(id ?? '')
  const { data: clientes = [] } = useClientes()
  const { data: funcionarios = [] } = useFuncionarios()

  const clienteNome = atendimento
    ? clientes.find(c => c.id === atendimento.cliente)?.nomeCompleto ?? null
    : null

  const funcionarioNome = atendimento
    ? funcionarios.find(f => f.id === atendimento.funcionario)?.nome ?? null
    : null

  if (isLoading) return <main className="p-6 text-muted-foreground text-sm">Carregando...</main>
  if (!atendimento) return <main className="p-6 text-muted-foreground text-sm">Atendimento não encontrado.</main>

  return (
    <main className="p-6 max-w-xl">
      <button
        onClick={() => navigate('/atendimentos')}
        className="flex items-center gap-1.5 text-sm text-muted-foreground hover:text-ocean-900 mb-5 transition-colors"
      >
        <ArrowLeft size={14} />
        Voltar
      </button>

      <div className="flex items-start justify-between mb-6">
        <div className="min-w-0">
          <div className="flex items-center gap-3 mb-1">
            <h1 className="text-xl font-semibold truncate">{atendimento.descricaoServico}</h1>
            <Badge className={statusBadgeClass[atendimento.status]}>
              {STATUS_LABELS[atendimento.status]}
            </Badge>
          </div>
          <p className="text-sm text-muted-foreground">Atendimento #{atendimento.id.slice(0, 8)}</p>
        </div>
        <Button
          variant="outline"
          size="sm"
          className="gap-1.5 shrink-0"
          onClick={() => navigate(`/atendimentos/${id}/editar`)}
        >
          <Pencil size={13} />
          Editar
        </Button>
      </div>

      <div className="space-y-4">
        <Card>
          <CardHeader className="pb-3">
            <CardTitle className="text-sm font-medium">Detalhes do serviço</CardTitle>
          </CardHeader>
          <CardContent className="grid grid-cols-2 gap-x-8 gap-y-3 text-sm">
            <div>
              <p className="text-muted-foreground text-xs mb-0.5">Cliente</p>
              <button
                onClick={() => navigate(`/clientes/${atendimento.cliente}`)}
                className="text-teal-600 hover:text-teal-700 hover:underline font-medium text-left cursor-pointer"
              >
                {clienteNome ?? '—'}
              </button>
            </div>
            <div>
              <p className="text-muted-foreground text-xs mb-0.5">Veículo</p>
              <p className="font-mono">{atendimento.veiculo}</p>
            </div>
            <div>
              <p className="text-muted-foreground text-xs mb-0.5">Responsável</p>
              <p>{funcionarioNome ?? '—'}</p>
            </div>
            <div>
              <p className="text-muted-foreground text-xs mb-0.5">Status</p>
              <Badge className={statusBadgeClass[atendimento.status]}>
                {STATUS_LABELS[atendimento.status]}
              </Badge>
            </div>
          </CardContent>
        </Card>

        <Card>
          <CardHeader className="pb-3">
            <CardTitle className="text-sm font-medium">Datas</CardTitle>
          </CardHeader>
          <CardContent className="grid grid-cols-2 gap-x-8 gap-y-3 text-sm">
            <div>
              <p className="text-muted-foreground text-xs mb-0.5">Cadastro</p>
              <p>{formatDate(atendimento.dataCadastro)}</p>
            </div>
            <div>
              <p className="text-muted-foreground text-xs mb-0.5">Entrada</p>
              <p>{formatDate(atendimento.dataEntrada)}</p>
            </div>
            <div>
              <p className="text-muted-foreground text-xs mb-0.5">Conclusão</p>
              <p>{atendimento.dataConclusao ? formatDate(atendimento.dataConclusao) : '—'}</p>
            </div>
          </CardContent>
        </Card>
      </div>
    </main>
  )
}
