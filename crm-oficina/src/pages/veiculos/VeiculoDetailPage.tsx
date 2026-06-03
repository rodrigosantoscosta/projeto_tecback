import { useNavigate, useParams } from 'react-router-dom'
import { ArrowLeft, Pencil } from 'lucide-react'
import { Button } from '../../components/ui/button'
import { Card, CardContent, CardHeader, CardTitle } from '../../components/ui/card'
import { useVeiculo } from '../../hooks/useVeiculos'
import { useClientes } from '../../hooks/useClientes'
import { formatDate } from '../../utils/formatadores'

export function VeiculoDetailPage() {
  const navigate = useNavigate()
  const { id } = useParams<{ id: string }>()
  const { data: veiculo, isLoading } = useVeiculo(id ?? '')
  const { data: clientes = [] } = useClientes()

  const clienteNome = veiculo
    ? clientes.find(c => c.id === veiculo.clienteId)?.nomeCompleto ?? null
    : null

  if (isLoading) return <main className="p-6 text-muted-foreground text-sm">Carregando...</main>
  if (!veiculo) return <main className="p-6 text-muted-foreground text-sm">Veículo não encontrado.</main>

  return (
    <main className="p-6 max-w-xl">
      <button
        onClick={() => navigate('/veiculos')}
        className="flex items-center gap-1.5 text-sm text-muted-foreground hover:text-ocean-900 mb-5 transition-colors"
      >
        <ArrowLeft size={14} />
        Voltar
      </button>

      <div className="flex items-center justify-between mb-6">
        <div>
          <h1 className="font-mono text-2xl font-bold tracking-widest text-teal-500">{veiculo.placa}</h1>
          <p className="text-muted-foreground mt-0.5">{veiculo.marca} {veiculo.modelo} · {veiculo.ano}</p>
        </div>
        <Button
          variant="outline"
          size="sm"
          className="gap-1.5"
          onClick={() => navigate(`/veiculos/${id}/editar`)}
        >
          <Pencil size={13} />
          Editar
        </Button>
      </div>

      <div className="space-y-4">
        <Card>
          <CardHeader className="pb-3">
            <CardTitle className="text-sm font-medium">Detalhes</CardTitle>
          </CardHeader>
          <CardContent className="grid grid-cols-2 gap-x-8 gap-y-3 text-sm">
            <div>
              <p className="text-muted-foreground text-xs mb-0.5">Proprietário</p>
              <p>{clienteNome ?? '—'}</p>
            </div>
            <div>
              <p className="text-muted-foreground text-xs mb-0.5">Cor</p>
              <p>{veiculo.cor ?? '—'}</p>
            </div>
            <div>
              <p className="text-muted-foreground text-xs mb-0.5">Quilometragem</p>
              <p>{veiculo.quilometragem != null ? `${veiculo.quilometragem.toLocaleString('pt-BR')} km` : '—'}</p>
            </div>
            <div>
              <p className="text-muted-foreground text-xs mb-0.5">Cadastrado em</p>
              <p>{formatDate(veiculo.dataCadastro)}</p>
            </div>
          </CardContent>
        </Card>
      </div>
    </main>
  )
}
