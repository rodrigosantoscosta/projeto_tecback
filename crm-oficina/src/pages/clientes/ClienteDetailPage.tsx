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

  if (isLoading) return <main className="p-6 text-muted-foreground text-sm">Carregando...</main>
  if (!cliente) return <main className="p-6 text-muted-foreground text-sm">Cliente não encontrado.</main>

  const endereco = [cliente.logradouro, cliente.numero, cliente.bairro, cliente.cidade, cliente.estado]
    .filter(Boolean)
    .join(', ')

  return (
    <main className="p-6 max-w-2xl">
      <button
        onClick={() => navigate('/clientes')}
        className="flex items-center gap-1.5 text-sm text-muted-foreground hover:text-ocean-700 mb-5 transition-colors"
      >
        <ArrowLeft size={14} />
        Voltar
      </button>

      <div className="flex items-center justify-between mb-6">
        <div>
          <h1 className="text-xl font-semibold text-ocean-900">{cliente.nome}</h1>
          <p className="text-sm text-muted-foreground mt-0.5">Cadastrado em {formatDate(cliente.dataCadastro)}</p>
        </div>
        <Button
          variant="outline"
          size="sm"
          className="gap-1.5"
          onClick={() => navigate(`/clientes/${id}/editar`)}
        >
          <Pencil size={13} />
          Editar
        </Button>
      </div>

      <div className="space-y-4">
        <Card>
          <CardHeader className="pb-3">
            <CardTitle className="text-sm font-medium">Dados pessoais</CardTitle>
          </CardHeader>
          <CardContent className="grid grid-cols-2 gap-x-8 gap-y-3 text-sm">
            <div>
              <p className="text-muted-foreground text-xs mb-0.5">CPF / CNPJ</p>
              <p className="font-mono">{formatCpfCnpj(cliente.cpfCNPJ)}</p>
            </div>
            <div>
              <p className="text-muted-foreground text-xs mb-0.5">Telefone</p>
              <p>{formatTelefone(cliente.telefone)}</p>
            </div>
            {cliente.email && (
              <div className="col-span-2">
                <p className="text-muted-foreground text-xs mb-0.5">E-mail</p>
                <p>{cliente.email}</p>
              </div>
            )}
          </CardContent>
        </Card>

        {endereco && (
          <Card>
            <CardHeader className="pb-3">
              <CardTitle className="text-sm font-medium">Endereço</CardTitle>
            </CardHeader>
            <CardContent className="text-sm">
              <p>{endereco}</p>
              {cliente.complemento && <p className="text-muted-foreground">{cliente.complemento}</p>}
              {cliente.cep && <p className="text-muted-foreground mt-1">CEP {cliente.cep}</p>}
            </CardContent>
          </Card>
        )}

        <Card>
          <CardHeader className="flex flex-row items-center justify-between pb-3">
            <CardTitle className="text-sm font-medium">
              Veículos
              <Badge variant="secondary" className="ml-2">
                {veiculos.length}
              </Badge>
            </CardTitle>
            <Button
              size="sm"
              variant="ghost"
              className="h-7 text-xs gap-1 text-muted-foreground hover:text-ocean-700"
              onClick={() => navigate(`/veiculos/novo?clienteId=${id}`)}
            >
              <Car size={12} />
              Adicionar
            </Button>
          </CardHeader>
          <CardContent>
            {veiculos.length === 0 ? (
              <p className="text-sm text-muted-foreground">Nenhum veículo cadastrado.</p>
            ) : (
              <div className="space-y-2">
                {veiculos.map(v => (
                  <Link
                    key={v.id}
                    to={`/veiculos/${v.id}`}
                    className="flex items-center justify-between p-3 rounded-md bg-ocean-50 hover:bg-ocean-100 transition-colors text-sm"
                  >
                    <div>
                      <span className="font-mono font-medium text-ocean-700">{v.placa}</span>
                      <span className="text-muted-foreground ml-2">{v.marca} {v.modelo}</span>
                    </div>
                    <span className="text-muted-foreground">{v.anoFabricacao}</span>
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
