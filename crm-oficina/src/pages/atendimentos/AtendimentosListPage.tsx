import { useState } from 'react'
import { useNavigate } from 'react-router-dom'
import type { ColumnDef } from '@tanstack/react-table'
import { Plus, Pencil, Eye, Trash2 } from 'lucide-react'
import { Button } from '../../components/ui/button'
import { Badge } from '../../components/ui/badge'
import { DataTable } from '../../components/ui/data-table'
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from '../../components/ui/select'
import {
  Dialog,
  DialogContent,
  DialogHeader,
  DialogTitle,
  DialogFooter,
  DialogDescription,
} from '../../components/ui/dialog'
import { useAtendimentos, useExcluirAtendimento } from '../../hooks/useAtendimentos'
import { useClientes } from '../../hooks/useClientes'
import { useFuncionarios } from '../../hooks/useFuncionarios'
import type { AtendimentoResponse } from '../../types/atendimento'
import { StatusAtendimento, STATUS_LABELS } from '../../types/atendimento'
import { formatDate } from '../../utils/formatadores'

const statusVariant: Record<StatusAtendimento, 'default' | 'secondary' | 'destructive' | 'outline'> = {
  [StatusAtendimento.AGUARDANDO]: 'secondary',
  [StatusAtendimento.ANDAMENTO]: 'default',
  [StatusAtendimento.CONCLUIDO]: 'outline',
  [StatusAtendimento.CANCELADO]: 'destructive',
}

const statusBadgeClass: Record<StatusAtendimento, string> = {
  [StatusAtendimento.AGUARDANDO]: 'bg-yellow-100 text-yellow-800 border-yellow-300 hover:bg-yellow-200',
  [StatusAtendimento.ANDAMENTO]: 'bg-blue-100 text-blue-800 border-blue-300 hover:bg-blue-200',
  [StatusAtendimento.CONCLUIDO]: 'bg-emerald-100 text-emerald-800 border-emerald-300 hover:bg-emerald-200',
  [StatusAtendimento.CANCELADO]: '',
}

export function AtendimentosListPage() {
  const navigate = useNavigate()
  const { data: atendimentos = [], isLoading } = useAtendimentos()
  const { data: clientes = [] } = useClientes()
  const { data: funcionarios = [] } = useFuncionarios()
  const excluir = useExcluirAtendimento()

  const clienteMap = new Map(clientes.map(c => [c.id, c.nomeCompleto]))
  const funcionarioMap = new Map(funcionarios.map(f => [f.id, f.nome]))

  const [deleteTarget, setDeleteTarget] = useState<AtendimentoResponse | null>(null)
  const [deleteError, setDeleteError] = useState<string | null>(null)
  const [statusFilter, setStatusFilter] = useState<string>('')

  const filtered = statusFilter
    ? atendimentos.filter(a => a.status === statusFilter)
    : atendimentos

  const handleDelete = async () => {
    if (!deleteTarget) return
    setDeleteError(null)
    try {
      await excluir.mutateAsync(deleteTarget.id)
      setDeleteTarget(null)
    } catch {
      setDeleteError('Erro ao excluir atendimento. Tente novamente.')
    }
  }

  const columns: ColumnDef<AtendimentoResponse>[] = [
    {
      accessorKey: 'descricaoServico',
      header: 'Serviço',
      cell: ({ row }) => (
        <span className="font-medium">{row.original.descricaoServico}</span>
      ),
    },
    {
      accessorKey: 'status',
      header: 'Status',
      cell: ({ row }) => {
        const status = row.original.status
        return (
          <Badge variant={statusVariant[status]} className={statusBadgeClass[status]}>
            {STATUS_LABELS[status]}
          </Badge>
        )
      },
    },
    {
      id: 'cliente',
      header: 'Cliente',
      cell: ({ row }) => (
        <span className="text-muted-foreground">{clienteMap.get(row.original.cliente) ?? '—'}</span>
      ),
    },
    {
      accessorKey: 'veiculo',
      header: 'Veículo',
      cell: ({ row }) => (
        <span className="font-mono text-xs">{row.original.veiculo}</span>
      ),
    },
    {
      id: 'funcionario',
      header: 'Responsável',
      cell: ({ row }) => (
        <span className="text-muted-foreground">{funcionarioMap.get(row.original.funcionario) ?? '—'}</span>
      ),
    },
    {
      accessorKey: 'dataEntrada',
      header: 'Entrada',
      cell: ({ row }) => (
        <span className="text-muted-foreground text-sm">{formatDate(row.original.dataEntrada)}</span>
      ),
    },
    {
      id: 'acoes',
      header: '',
      cell: ({ row }) => (
        <div className="flex items-center gap-1 justify-end">
          <Button size="icon" variant="ghost" className="h-8 w-8 text-muted-foreground hover:text-ocean-900"
            onClick={() => navigate(`/atendimentos/${row.original.id}`)} title="Visualizar">
            <Eye size={14} />
          </Button>
          <Button size="icon" variant="ghost" className="h-8 w-8 text-muted-foreground hover:text-ocean-900"
            onClick={() => navigate(`/atendimentos/${row.original.id}/editar`)} title="Editar">
            <Pencil size={14} />
          </Button>
          <Button size="icon" variant="ghost" className="h-8 w-8 text-muted-foreground hover:text-destructive"
            onClick={() => { setDeleteTarget(row.original); setDeleteError(null) }} title="Excluir">
            <Trash2 size={14} />
          </Button>
        </div>
      ),
    },
  ]

  return (
    <main className="p-6">
      <div className="flex items-center justify-between mb-6">
        <div>
          <h1 className="text-xl font-semibold">Atendimentos</h1>
          <p className="text-sm text-muted-foreground mt-0.5">Gerencie as ordens de serviço</p>
        </div>
        <Button onClick={() => navigate('/atendimentos/novo')} className="gap-1.5">
          <Plus size={15} />
          Novo atendimento
        </Button>
      </div>

      <div className="flex items-center gap-3 mb-4">
        <Select value={statusFilter} onValueChange={v => setStatusFilter(v === 'all' ? '' : v)}>
          <SelectTrigger className="w-44">
            <SelectValue placeholder="Filtrar por status" />
          </SelectTrigger>
          <SelectContent>
            <SelectItem value="all">Todos os status</SelectItem>
            <SelectItem value={StatusAtendimento.AGUARDANDO}>Aguardando</SelectItem>
            <SelectItem value={StatusAtendimento.ANDAMENTO}>Em Andamento</SelectItem>
            <SelectItem value={StatusAtendimento.CONCLUIDO}>Concluído</SelectItem>
            <SelectItem value={StatusAtendimento.CANCELADO}>Cancelado</SelectItem>
          </SelectContent>
        </Select>
      </div>

      {isLoading ? (
        <div className="flex items-center justify-center py-16 text-muted-foreground text-sm">Carregando...</div>
      ) : (
        <DataTable columns={columns} data={filtered} searchPlaceholder="Buscar por serviço..." />
      )}

      <Dialog open={!!deleteTarget} onOpenChange={open => { if (!open) { setDeleteTarget(null); setDeleteError(null) } }}>
        <DialogContent>
          <DialogHeader>
            <DialogTitle>Excluir atendimento</DialogTitle>
            <DialogDescription>
              Excluir atendimento de <span className="font-medium text-foreground">{deleteTarget?.descricaoServico}</span>? Esta ação não pode ser desfeita.
            </DialogDescription>
          </DialogHeader>
          {deleteError && (
            <p className="text-sm text-destructive bg-destructive/10 border border-destructive/30 rounded-md px-3 py-2">
              {deleteError}
            </p>
          )}
          <DialogFooter>
            <Button variant="ghost" onClick={() => setDeleteTarget(null)}>Cancelar</Button>
            <Button variant="destructive" onClick={handleDelete} disabled={excluir.isPending}>
              {excluir.isPending ? 'Excluindo...' : 'Excluir'}
            </Button>
          </DialogFooter>
        </DialogContent>
      </Dialog>
    </main>
  )
}
