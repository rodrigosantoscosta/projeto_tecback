import { useNavigate, useParams, Link } from 'react-router-dom'
import { ArrowLeft, Pencil, Car } from 'lucide-react'
import { Button } from '../../components/ui/button'
import { Badge } from '../../components/ui/badge'
import { Card, CardContent, CardHeader, CardTitle } from '../../components/ui/card'
import { useCliente } from '../../hooks/useClientes'
import { useVeiculosPorCliente } from '../../hooks/useVeiculos'
import { formatCpfCnpj, formatTelefone, formatDate } from '../../utils/formatadores'

export function ClienteDetailPage() {
  const navigate = useNavigate()
  const { id } = useParams<{ id: string }>()
  const { data: cliente, isLoading } = useCliente(id ?? '')
  const { data: veiculos = [] } = useVeiculosPorCliente(id ?? '')

  if (isLoading) return <main className="p-6 text-zinc-500 text-sm">Carregando...</main>
  if (!cliente) return <main className="p-6 text-zinc-500 text-sm">Cliente não encontrado.</main>

  const endereco = [cliente.logradouro, cliente.numero, cliente.bairro, cliente.cidade, cliente.estado]
    .filter(Boolean)
    .join(', ')

  return (
    <main className="p-6 max-w-2xl">
      <button
        onClick={() => navigate('/clientes')}
        className="flex items-center gap-1.5 text-sm text-zinc-400 hover:text-white mb-5 transition-colors"
      >
        <ArrowLeft size={14} />
        Voltar
      </button>

      <div className="flex items-center justify-between mb-6">
        <div>
          <h1 className="text-xl font-semibold">{cliente.nome}</h1>
          <p className="text-sm text-zinc-500 mt-0.5">Cadastrado em {formatDate(cliente.dataCadastro)}</p>
        </div>
        <Button
          variant="outline"
          size="sm"
          className="gap-1.5 border-zinc-700 bg-transparent hover:bg-zinc-800"
          onClick={() => navigate(`/clientes/${id}/editar`)}
        >
          <Pencil size={13} />
          Editar
        </Button>
      </div>

      <div className="space-y-4">
        <Card className="bg-zinc-900 border-zinc-800">
          <CardHeader className="pb-3">
            <CardTitle className="text-sm font-medium text-zinc-300">Dados pessoais</CardTitle>
          </CardHeader>
          <CardContent className="grid grid-cols-2 gap-x-8 gap-y-3 text-sm">
            <div>
              <p className="text-zinc-500 text-xs mb-0.5">CPF / CNPJ</p>
              <p className="font-mono">{formatCpfCnpj(cliente.cpfCNPJ)}</p>
            </div>
            <div>
              <p className="text-zinc-500 text-xs mb-0.5">Telefone</p>
              <p>{formatTelefone(cliente.telefone)}</p>
            </div>
            {cliente.email && (
              <div className="col-span-2">
                <p className="text-zinc-500 text-xs mb-0.5">E-mail</p>
                <p>{cliente.email}</p>
              </div>
            )}
          </CardContent>
        </Card>

        {endereco && (
          <Card className="bg-zinc-900 border-zinc-800">
            <CardHeader className="pb-3">
              <CardTitle className="text-sm font-medium text-zinc-300">Endereço</CardTitle>
            </CardHeader>
            <CardContent className="text-sm">
              <p>{endereco}</p>
              {cliente.complemento && <p className="text-zinc-400">{cliente.complemento}</p>}
              {cliente.cep && <p className="text-zinc-400 mt-1">CEP {cliente.cep}</p>}
            </CardContent>
          </Card>
        )}

        <Card className="bg-zinc-900 border-zinc-800">
          <CardHeader className="flex flex-row items-center justify-between pb-3">
            <CardTitle className="text-sm font-medium text-zinc-300">
              Veículos
              <Badge variant="secondary" className="ml-2 bg-zinc-800 text-zinc-300">
                {veiculos.length}
              </Badge>
            </CardTitle>
            <Button
              size="sm"
              variant="ghost"
              className="h-7 text-xs gap-1 text-zinc-400 hover:text-white"
              onClick={() => navigate(`/veiculos/novo?clienteId=${id}`)}
            >
              <Car size={12} />
              Adicionar
            </Button>
          </CardHeader>
          <CardContent>
            {veiculos.length === 0 ? (
              <p className="text-sm text-zinc-500">Nenhum veículo cadastrado.</p>
            ) : (
              <div className="space-y-2">
                {veiculos.map(v => (
                  <Link
                    key={v.id}
                    to={`/veiculos/${v.id}`}
                    className="flex items-center justify-between p-3 rounded-md bg-zinc-800/60 hover:bg-zinc-800 transition-colors text-sm"
                  >
                    <div>
                      <span className="font-mono font-medium">{v.placa}</span>
                      <span className="text-zinc-400 ml-2">{v.marca} {v.modelo}</span>
                    </div>
                    <span className="text-zinc-500">{v.anoFabricacao}</span>
                  </Link>
                ))}
              </div>
            )}
          </CardContent>
        </Card>
      </div>
    </main>
  )
}
