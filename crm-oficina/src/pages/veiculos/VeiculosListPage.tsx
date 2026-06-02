import { useState } from 'react'
import { useNavigate } from 'react-router-dom'
import type { ColumnDef } from '@tanstack/react-table'
import { Plus, Pencil, Eye, Trash2 } from 'lucide-react'
import { Button } from '../../components/ui/button'
import { DataTable } from '../../components/ui/data-table'
import {
  Dialog,
  DialogContent,
  DialogHeader,
  DialogTitle,
  DialogFooter,
  DialogDescription,
} from '../../components/ui/dialog'
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from '../../components/ui/select'
import { useVeiculos, useExcluirVeiculo } from '../../hooks/useVeiculos'
import { useClientes } from '../../hooks/useClientes'
import type { VeiculoResponse } from '../../types/veiculo'

export function VeiculosListPage() {
  const navigate = useNavigate()
  const { data: veiculos = [], isLoading } = useVeiculos()
  const { data: clientes = [] } = useClientes()
  const excluir = useExcluirVeiculo()

  const [clienteFilter, setClienteFilter] = useState<string>('all')
  const [deleteTarget, setDeleteTarget] = useState<VeiculoResponse | null>(null)
  const [deleteError, setDeleteError] = useState<string | null>(null)

  const filtered = clienteFilter === 'all'
    ? veiculos
    : veiculos.filter(v => v.clienteId === clienteFilter)

  const handleDelete = async () => {
    if (!deleteTarget) return
    setDeleteError(null)
    try {
      await excluir.mutateAsync(deleteTarget.id)
      setDeleteTarget(null)
    } catch {
      setDeleteError('Erro ao excluir veículo. Tente novamente.')
    }
  }

  const columns: ColumnDef<VeiculoResponse>[] = [
    {
      accessorKey: 'placa',
      header: 'Placa',
      cell: ({ row }) => (
        <span className="font-mono font-semibold text-blue-400">{row.original.placa}</span>
      ),
    },
    {
      id: 'veiculo',
      header: 'Veículo',
      cell: ({ row }) => (
        <span>{row.original.marca} {row.original.modelo} · {row.original.anoFabricacao}</span>
      ),
    },
    {
      accessorKey: 'clienteNome',
      header: 'Cliente',
      cell: ({ row }) => (
        <span className="text-zinc-300">{row.original.clienteNome}</span>
      ),
    },
    {
      accessorKey: 'combustivel',
      header: 'Combustível',
      cell: ({ row }) => (
        <span className="text-zinc-400 text-sm">{row.original.combustivel ?? '—'}</span>
      ),
    },
    {
      id: 'acoes',
      header: '',
      cell: ({ row }) => (
        <div className="flex items-center gap-1 justify-end">
          <Button size="icon" variant="ghost" className="h-8 w-8 text-zinc-400 hover:text-white"
            onClick={() => navigate(`/veiculos/${row.original.id}`)} title="Visualizar">
            <Eye size={14} />
          </Button>
          <Button size="icon" variant="ghost" className="h-8 w-8 text-zinc-400 hover:text-white"
            onClick={() => navigate(`/veiculos/${row.original.id}/editar`)} title="Editar">
            <Pencil size={14} />
          </Button>
          <Button size="icon" variant="ghost" className="h-8 w-8 text-zinc-400 hover:text-red-400"
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
          <h1 className="text-xl font-semibold">Veículos</h1>
          <p className="text-sm text-zinc-500 mt-0.5">Gerencie os veículos cadastrados</p>
        </div>
        <Button onClick={() => navigate('/veiculos/novo')} className="gap-1.5">
          <Plus size={15} />
          Novo veículo
        </Button>
      </div>

      <div className="flex items-center gap-3 mb-4">
        <Select value={clienteFilter} onValueChange={setClienteFilter}>
          <SelectTrigger className="w-56 bg-zinc-900 border-zinc-700 text-white">
            <SelectValue placeholder="Filtrar por cliente" />
          </SelectTrigger>
          <SelectContent className="bg-zinc-900 border-zinc-700">
            <SelectItem value="all" className="text-zinc-200">Todos os clientes</SelectItem>
            {clientes.map(c => (
              <SelectItem key={c.id} value={c.id} className="text-zinc-200">{c.nome}</SelectItem>
            ))}
          </SelectContent>
        </Select>
      </div>

      {isLoading ? (
        <div className="flex items-center justify-center py-16 text-zinc-500 text-sm">Carregando...</div>
      ) : (
        <DataTable columns={columns} data={filtered} searchPlaceholder="Buscar por placa, modelo..." />
      )}

      <Dialog open={!!deleteTarget} onOpenChange={open => { if (!open) { setDeleteTarget(null); setDeleteError(null) } }}>
        <DialogContent className="bg-zinc-900 border-zinc-800 text-white">
          <DialogHeader>
            <DialogTitle>Excluir veículo</DialogTitle>
            <DialogDescription className="text-zinc-400">
              Excluir <span className="text-white font-medium">{deleteTarget?.placa}</span> — {deleteTarget?.marca} {deleteTarget?.modelo}? Esta ação não pode ser desfeita.
            </DialogDescription>
          </DialogHeader>
          {deleteError && (
            <p className="text-sm text-red-400 bg-red-950/30 border border-red-800/50 rounded-md px-3 py-2">
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
