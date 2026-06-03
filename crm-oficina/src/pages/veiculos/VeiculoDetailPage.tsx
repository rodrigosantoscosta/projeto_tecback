import { useNavigate, useParams } from 'react-router-dom'
import { ArrowLeft, Pencil } from 'lucide-react'
import { Button } from '../../components/ui/button'
import { Card, CardContent, CardHeader, CardTitle } from '../../components/ui/card'
import { useVeiculo } from '../../hooks/useVeiculos'
import { formatDate } from '../../utils/formatadores'

export function VeiculoDetailPage() {
  const navigate = useNavigate()
  const { id } = useParams<{ id: string }>()
  const { data: veiculo, isLoading } = useVeiculo(id ?? '')

  if (isLoading) return <main className="p-6 text-zinc-500 text-sm">Carregando...</main>
  if (!veiculo) return <main className="p-6 text-zinc-500 text-sm">Veículo não encontrado.</main>

  return (
    <main className="p-6 max-w-xl">
      <button
        onClick={() => navigate('/veiculos')}
        className="flex items-center gap-1.5 text-sm text-zinc-400 hover:text-white mb-5 transition-colors"
      >
        <ArrowLeft size={14} />
        Voltar
      </button>

      <div className="flex items-center justify-between mb-6">
        <div>
          <h1 className="font-mono text-2xl font-bold tracking-widest text-blue-400">{veiculo.placa}</h1>
          <p className="text-zinc-400 mt-0.5">{veiculo.marca} {veiculo.modelo} · {veiculo.ano}</p>
        </div>
        <Button
          variant="outline"
          size="sm"
          className="gap-1.5 border-zinc-700 bg-transparent hover:bg-zinc-800"
          onClick={() => navigate(`/veiculos/${id}/editar`)}
        >
          <Pencil size={13} />
          Editar
        </Button>
      </div>

      <div className="space-y-4">
        <Card className="bg-zinc-900 border-zinc-800">
          <CardHeader className="pb-3">
            <CardTitle className="text-sm font-medium text-zinc-300">Detalhes</CardTitle>
          </CardHeader>
          <CardContent className="grid grid-cols-2 gap-x-8 gap-y-3 text-sm">
            <div>
              <p className="text-zinc-500 text-xs mb-0.5">Cor</p>
              <p>{veiculo.cor ?? '—'}</p>
            </div>
            <div>
              <p className="text-zinc-500 text-xs mb-0.5">Quilometragem</p>
              <p>{veiculo.quilometragem != null ? `${veiculo.quilometragem.toLocaleString('pt-BR')} km` : '—'}</p>
            </div>
            <div>
              <p className="text-zinc-500 text-xs mb-0.5">Cadastrado em</p>
              <p>{formatDate(veiculo.dataCadastro)}</p>
            </div>
          </CardContent>
        </Card>
      </div>
    </main>
  )
}
