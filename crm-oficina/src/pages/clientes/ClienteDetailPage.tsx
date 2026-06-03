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

  const endereco = cliente.endereco
  const enderecoFormatado = [
    endereco?.logradouro,
    endereco?.numero,
    endereco?.bairro,
    endereco?.cidade,
    endereco?.estado,
  ]
    .filter(Boolean)
    .join(', ')

  return (
    <main className="p-6 max-w-2xl">
      <button
        onClick={() => navigate('/clientes')}
        className="flex items-center gap-1.5 text-sm text-muted-foreground hover:text-teal-600 mb-5 transition-colors"
      >
        <ArrowLeft size={14} />
        Voltar
      </button>

      <div className="flex items-center justify-between mb-6">
        <div>
          <h1 className="text-xl font-semibold text-ocean-900">{cliente.nomeCompleto}</h1>
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

        {enderecoFormatado && (
          <Card>
            <CardHeader className="pb-3">
              <CardTitle className="text-sm font-medium">Endereço</CardTitle>
            </CardHeader>
            <CardContent className="text-sm">
              <p>{enderecoFormatado}</p>
              {endereco?.complemento && <p className="text-muted-foreground">{endereco.complemento}</p>}
              {endereco?.cep && <p className="text-muted-foreground mt-1">CEP {endereco.cep}</p>}
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
              className="h-7 text-xs gap-1 text-muted-foreground hover:text-teal-600"
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
                    className="flex items-center justify-between p-3 rounded-md bg-sand-100 hover:bg-sand-200 transition-colors text-sm"
                  >
                    <div>
                      <span className="font-mono font-medium text-teal-600">{v.placa}</span>
                      <span className="text-muted-foreground ml-2">{v.marca} {v.modelo}</span>
                    </div>
                    <span className="text-muted-foreground">{v.ano}</span>
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
