import { useState } from 'react'
import { useNavigate } from 'react-router-dom'
import type { ColumnDef } from '@tanstack/react-table'
import { Plus, Pencil, Eye, Trash2 } from 'lucide-react'
import { Button } from '../../components/ui/button'
import { Badge } from '../../components/ui/badge'
import { DataTable } from '../../components/ui/data-table'
import {
  Dialog,
  DialogContent,
  DialogHeader,
  DialogTitle,
  DialogFooter,
  DialogDescription,
} from '../../components/ui/dialog'
import { useClientes, useExcluirCliente } from '../../hooks/useClientes'
import type { ClienteResponse } from '../../types/cliente'
import { formatCpfCnpj, formatTelefone } from '../../utils/formatadores'

export function ClientesListPage() {
  const navigate = useNavigate()
  const { data: clientes = [], isLoading } = useClientes()
  const excluir = useExcluirCliente()

  const [deleteTarget, setDeleteTarget] = useState<ClienteResponse | null>(null)
  const [deleteError, setDeleteError] = useState<string | null>(null)

  const handleDelete = async () => {
    if (!deleteTarget) return
    setDeleteError(null)
    try {
      await excluir.mutateAsync(deleteTarget.id)
      setDeleteTarget(null)
    } catch (err: unknown) {
      const status = (err as { response?: { status?: number } })?.response?.status
      if (status === 409) {
        setDeleteError('Este cliente possui veículos associados. Remova os veículos antes de excluí-lo.')
      } else {
        setDeleteError('Erro ao excluir cliente. Tente novamente.')
      }
    }
  }

  const columns: ColumnDef<ClienteResponse>[] = [
    {
      accessorKey: 'nomeCompleto',
      header: 'Nome',
      cell: ({ row }) => <span className="font-medium">{row.original.nomeCompleto}</span>,
    },
    {
      accessorKey: 'cpfCNPJ',
      header: 'CPF / CNPJ',
      cell: ({ row }) => (
        <span className="font-mono text-sm text-muted-foreground">
          {formatCpfCnpj(row.original.cpfCNPJ)}
        </span>
      ),
    },
    {
      accessorKey: 'telefone',
      header: 'Telefone',
      cell: ({ row }) => formatTelefone(row.original.telefone),
    },
    {
      accessorKey: 'quantidadeVeiculos',
      header: 'Veículos',
      cell: ({ row }) => (
        <Badge variant="secondary">
          {row.original.quantidadeVeiculos}
        </Badge>
      ),
    },
    {
      id: 'acoes',
      header: '',
      cell: ({ row }) => (
        <div className="flex items-center gap-1 justify-end">
          <Button
            size="icon"
            variant="ghost"
            className="h-8 w-8 text-muted-foreground hover:text-ocean-700"
            onClick={() => navigate(`/clientes/${row.original.id}`)}
            title="Visualizar"
          >
            <Eye size={14} />
          </Button>
          <Button
            size="icon"
            variant="ghost"
            className="h-8 w-8 text-muted-foreground hover:text-ocean-700"
            onClick={() => navigate(`/clientes/${row.original.id}/editar`)}
            title="Editar"
          >
            <Pencil size={14} />
          </Button>
          <Button
            size="icon"
            variant="ghost"
            className="h-8 w-8 text-muted-foreground hover:text-destructive"
            onClick={() => { setDeleteTarget(row.original); setDeleteError(null) }}
            title="Excluir"
          >
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
          <h1 className="text-xl font-semibold">Clientes</h1>
          <p className="text-sm text-muted-foreground mt-0.5">Gerencie os clientes da oficina</p>
        </div>
        <Button onClick={() => navigate('/clientes/novo')} className="gap-1.5">
          <Plus size={15} />
          Novo cliente
        </Button>
      </div>

      {isLoading ? (
        <div className="flex items-center justify-center py-16 text-muted-foreground text-sm">
          Carregando...
        </div>
      ) : (
        <DataTable
          columns={columns}
          data={clientes}
          searchPlaceholder="Buscar por nome, CPF/CNPJ..."
        />
      )}

      <Dialog open={!!deleteTarget} onOpenChange={open => { if (!open) { setDeleteTarget(null); setDeleteError(null) } }}>
        <DialogContent>
          <DialogHeader>
            <DialogTitle>Excluir cliente</DialogTitle>
            <DialogDescription>
              Tem certeza que deseja excluir <span className="font-medium text-foreground">{deleteTarget?.nomeCompleto}</span>?
              Esta ação não pode ser desfeita.
            </DialogDescription>
          </DialogHeader>
          {deleteError && (
            <p className="text-sm text-destructive bg-red-50 border border-red-200 rounded-md px-3 py-2">
              {deleteError}
            </p>
          )}
          <DialogFooter>
            <Button variant="ghost" onClick={() => { setDeleteTarget(null); setDeleteError(null) }}>
              Cancelar
            </Button>
            <Button
              variant="destructive"
              onClick={handleDelete}
              disabled={excluir.isPending}
            >
              {excluir.isPending ? 'Excluindo...' : 'Excluir'}
            </Button>
          </DialogFooter>
        </DialogContent>
      </Dialog>
    </main>
  )
}
