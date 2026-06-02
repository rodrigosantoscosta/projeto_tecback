import { useEffect } from 'react'
import { useNavigate, useParams, useSearchParams } from 'react-router-dom'
import { useForm, Controller } from 'react-hook-form'
import { zodResolver } from '@hookform/resolvers/zod'
import { z } from 'zod'
import { ArrowLeft } from 'lucide-react'
import { Button } from '../../components/ui/button'
import { Input } from '../../components/ui/input'
import { Label } from '../../components/ui/label'
import { Card, CardContent, CardHeader, CardTitle } from '../../components/ui/card'
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from '../../components/ui/select'
import { Combobox } from '../../components/ui/combobox'
import { useVeiculo, useCadastrarVeiculo, useAtualizarVeiculo } from '../../hooks/useVeiculos'
import { useClientes } from '../../hooks/useClientes'
import { formatPlaca } from '../../utils/formatadores'
import { PLACA_MERCOSUL_REGEX } from '../../utils/validadores'
import type { CombustívelTipo } from '../../types/veiculo'

const COMBUSTIVEIS: CombustívelTipo[] = ['GASOLINA', 'ETANOL', 'FLEX', 'DIESEL', 'GNV', 'ELETRICO', 'HIBRIDO']

const schema = z.object({
  placa: z.string().regex(PLACA_MERCOSUL_REGEX, 'Placa inválida — formato Mercosul (ex: ABC1D23)'),
  marca: z.string().min(1, 'Marca obrigatória'),
  modelo: z.string().min(1, 'Modelo obrigatório'),
  anoFabricacao: z.string().refine(v => {
    const n = Number(v)
    return Number.isInteger(n) && n >= 1900 && n <= new Date().getFullYear() + 1
  }, 'Ano inválido'),
  cor: z.string().optional(),
  combustivel: z.enum(['GASOLINA', 'ETANOL', 'DIESEL', 'FLEX', 'ELETRICO', 'HIBRIDO', 'GNV']).optional(),
  clienteId: z.string().min(1, 'Selecione um cliente'),
})

type FormData = z.infer<typeof schema>

export function VeiculoFormPage() {
  const navigate = useNavigate()
  const { id } = useParams<{ id: string }>()
  const [searchParams] = useSearchParams()
  const isEdit = !!id

  const { data: veiculo, isLoading: loadingVeiculo } = useVeiculo(id ?? '')
  const { data: clientes = [] } = useClientes()
  const cadastrar = useCadastrarVeiculo()
  const atualizar = useAtualizarVeiculo(id ?? '')

  const {
    register,
    handleSubmit,
    setValue,
    control,
    watch,
    formState: { errors, isSubmitting },
  } = useForm<FormData>({
    resolver: zodResolver(schema),
    defaultValues: { anoFabricacao: String(new Date().getFullYear()) },
  })

  useEffect(() => {
    if (veiculo) {
      setValue('placa', veiculo.placa)
      setValue('marca', veiculo.marca)
      setValue('modelo', veiculo.modelo)
      setValue('anoFabricacao', String(veiculo.anoFabricacao))
      setValue('cor', veiculo.cor ?? '')
      if (veiculo.combustivel) setValue('combustivel', veiculo.combustivel)
      setValue('clienteId', veiculo.clienteId)
    }
  }, [veiculo, setValue])

  useEffect(() => {
    const clienteId = searchParams.get('clienteId')
    if (clienteId && !isEdit) setValue('clienteId', clienteId)
  }, [searchParams, isEdit, setValue])

  const onSubmit = async (data: FormData) => {
    const payload = {
      ...data,
      anoFabricacao: Number(data.anoFabricacao),
    }
    if (isEdit) {
      await atualizar.mutateAsync(payload)
    } else {
      await cadastrar.mutateAsync(payload)
    }
    navigate('/veiculos')
  }

  if (isEdit && loadingVeiculo) {
    return <main className="p-6 text-zinc-500 text-sm">Carregando...</main>
  }

  const mutationError = isEdit ? atualizar.error : cadastrar.error
  const errorMsg = mutationError
    ? ((mutationError as { response?: { data?: { message?: string } } })?.response?.data?.message ?? 'Erro ao salvar. Tente novamente.')
    : null

  const clienteOptions = clientes.map(c => ({ value: c.id, label: c.nome }))

  return (
    <main className="p-6 max-w-xl">
      <button
        onClick={() => navigate('/veiculos')}
        className="flex items-center gap-1.5 text-sm text-zinc-400 hover:text-white mb-5 transition-colors"
      >
        <ArrowLeft size={14} />
        Voltar
      </button>

      <h1 className="text-xl font-semibold mb-6">{isEdit ? 'Editar veículo' : 'Novo veículo'}</h1>

      <form onSubmit={handleSubmit(onSubmit)} className="space-y-5">
        {errorMsg && (
          <p className="text-sm text-red-400 bg-red-950/30 border border-red-800/50 rounded-md px-3 py-2">
            {errorMsg}
          </p>
        )}

        <Card className="bg-zinc-900 border-zinc-800">
          <CardHeader className="pb-3">
            <CardTitle className="text-sm font-medium text-zinc-300">Dados do veículo</CardTitle>
          </CardHeader>
          <CardContent className="space-y-4">
            <div className="space-y-1.5">
              <Label>Cliente *</Label>
              <Controller
                control={control}
                name="clienteId"
                render={({ field }) => (
                  <Combobox
                    options={clienteOptions}
                    value={field.value ?? ''}
                    onChange={field.onChange}
                    placeholder="Selecionar cliente..."
                    searchPlaceholder="Buscar cliente..."
                  />
                )}
              />
              {errors.clienteId && <p className="text-xs text-red-400">{errors.clienteId.message}</p>}
            </div>

            <div className="grid grid-cols-2 gap-4">
              <div className="space-y-1.5">
                <Label>Placa *</Label>
                <Input
                  placeholder="ABC1D23"
                  value={watch('placa') ?? ''}
                  onChange={e => setValue('placa', formatPlaca(e.target.value))}
                  className="bg-zinc-950 border-zinc-700 font-mono uppercase"
                  maxLength={7}
                />
                {errors.placa && <p className="text-xs text-red-400">{errors.placa.message}</p>}
              </div>
              <div className="space-y-1.5">
                <Label>Ano *</Label>
                <Input
                  type="number"
                  {...register('anoFabricacao')}
                  className="bg-zinc-950 border-zinc-700"
                />
                {errors.anoFabricacao && <p className="text-xs text-red-400">{errors.anoFabricacao.message}</p>}
              </div>
            </div>

            <div className="grid grid-cols-2 gap-4">
              <div className="space-y-1.5">
                <Label>Marca *</Label>
                <Input {...register('marca')} placeholder="Toyota" className="bg-zinc-950 border-zinc-700" />
                {errors.marca && <p className="text-xs text-red-400">{errors.marca.message}</p>}
              </div>
              <div className="space-y-1.5">
                <Label>Modelo *</Label>
                <Input {...register('modelo')} placeholder="Corolla" className="bg-zinc-950 border-zinc-700" />
                {errors.modelo && <p className="text-xs text-red-400">{errors.modelo.message}</p>}
              </div>
            </div>

            <div className="grid grid-cols-2 gap-4">
              <div className="space-y-1.5">
                <Label>Cor</Label>
                <Input {...register('cor')} placeholder="Prata" className="bg-zinc-950 border-zinc-700" />
              </div>
              <div className="space-y-1.5">
                <Label>Combustível</Label>
                <Controller
                  control={control}
                  name="combustivel"
                  render={({ field }) => (
                    <Select value={field.value} onValueChange={field.onChange}>
                      <SelectTrigger className="bg-zinc-950 border-zinc-700 text-white">
                        <SelectValue placeholder="Selecionar..." />
                      </SelectTrigger>
                      <SelectContent className="bg-zinc-900 border-zinc-700">
                        {COMBUSTIVEIS.map(c => (
                          <SelectItem key={c} value={c} className="text-zinc-200">{c}</SelectItem>
                        ))}
                      </SelectContent>
                    </Select>
                  )}
                />
              </div>
            </div>
          </CardContent>
        </Card>

        <div className="flex gap-3 justify-end">
          <Button type="button" variant="ghost" onClick={() => navigate('/veiculos')}>
            Cancelar
          </Button>
          <Button type="submit" disabled={isSubmitting}>
            {isSubmitting ? 'Salvando...' : isEdit ? 'Salvar alterações' : 'Cadastrar veículo'}
          </Button>
        </div>
      </form>
    </main>
  )
}
