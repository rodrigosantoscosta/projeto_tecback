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
import { Combobox } from '../../components/ui/combobox'
import { useVeiculo, useCadastrarVeiculo, useAtualizarVeiculo } from '../../hooks/useVeiculos'
import { useClientes } from '../../hooks/useClientes'
import { formatPlaca } from '../../utils/formatadores'
import { PLACA_MERCOSUL_REGEX } from '../../utils/validadores'

const schema = z.object({
  placa: z.string().regex(PLACA_MERCOSUL_REGEX, 'Placa inválida — formato Mercosul (ex: ABC1D23)'),
  marca: z.string().min(1, 'Marca obrigatória'),
  modelo: z.string().min(1, 'Modelo obrigatório'),
  ano: z.string().refine(v => {
    const n = Number(v)
    return Number.isInteger(n) && n >= 1900 && n <= new Date().getFullYear() + 1
  }, 'Ano inválido'),
  cor: z.string().optional(),
  quilometragem: z.string().optional(),
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
    defaultValues: { ano: String(new Date().getFullYear()) },
  })

  useEffect(() => {
    if (veiculo) {
      setValue('placa', veiculo.placa)
      setValue('marca', veiculo.marca)
      setValue('modelo', veiculo.modelo)
      setValue('ano', String(veiculo.ano))
      setValue('cor', veiculo.cor ?? '')
      setValue('clienteId', veiculo.clienteId)
      if (veiculo.quilometragem != null) setValue('quilometragem', String(veiculo.quilometragem))
    }
  }, [veiculo, setValue])

  useEffect(() => {
    const clienteId = searchParams.get('clienteId')
    if (clienteId && !isEdit) setValue('clienteId', clienteId)
  }, [searchParams, isEdit, setValue])

  const onSubmit = async (data: FormData) => {
    const payload = {
      placa: data.placa,
      marca: data.marca,
      modelo: data.modelo,
      ano: Number(data.ano),
      cor: data.cor || undefined,
      quilometragem: data.quilometragem ? Number(data.quilometragem) : undefined,
      clienteId: data.clienteId,
    }
    if (isEdit) {
      await atualizar.mutateAsync(payload)
    } else {
      await cadastrar.mutateAsync(payload)
    }
    navigate('/veiculos')
  }

  if (isEdit && loadingVeiculo) {
    return <main className="p-6 text-muted-foreground text-sm">Carregando...</main>
  }

  const mutationError = isEdit ? atualizar.error : cadastrar.error
  const errorMsg = mutationError
    ? ((mutationError as { response?: { data?: { message?: string } } })?.response?.data?.message ?? 'Erro ao salvar. Tente novamente.')
    : null

  const clienteOptions = clientes.map(c => ({ value: c.id, label: c.nomeCompleto }))

  return (
    <main className="p-6 max-w-xl">
      <button
        onClick={() => navigate('/veiculos')}
        className="flex items-center gap-1.5 text-sm text-muted-foreground hover:text-ocean-900 mb-5 transition-colors"
      >
        <ArrowLeft size={14} />
        Voltar
      </button>

      <h1 className="text-xl font-semibold mb-6">{isEdit ? 'Editar veículo' : 'Novo veículo'}</h1>

      <form onSubmit={handleSubmit(onSubmit)} className="space-y-5">
        {errorMsg && (
          <p className="text-sm text-destructive bg-destructive/10 border border-destructive/30 rounded-md px-3 py-2">
            {errorMsg}
          </p>
        )}

        <Card>
          <CardHeader className="pb-3">
            <CardTitle className="text-sm font-medium">Dados do veículo</CardTitle>
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
              {errors.clienteId && <p className="text-xs text-destructive">{errors.clienteId.message}</p>}
            </div>

            <div className="grid grid-cols-2 gap-4">
              <div className="space-y-1.5">
                <Label>Placa *</Label>
                <Input
                  placeholder="ABC1D23"
                  value={watch('placa') ?? ''}
                  onChange={e => setValue('placa', formatPlaca(e.target.value))}
                  className="font-mono uppercase"
                  maxLength={7}
                />
                {errors.placa && <p className="text-xs text-destructive">{errors.placa.message}</p>}
              </div>
              <div className="space-y-1.5">
                <Label>Ano *</Label>
                <Input
                  type="number"
                  {...register('ano')}
                  className=""
                />
                {errors.ano && <p className="text-xs text-destructive">{errors.ano.message}</p>}
              </div>
            </div>

            <div className="grid grid-cols-2 gap-4">
              <div className="space-y-1.5">
                <Label>Marca *</Label>
                <Input {...register('marca')} placeholder="Toyota" className="" />
                {errors.marca && <p className="text-xs text-destructive">{errors.marca.message}</p>}
              </div>
              <div className="space-y-1.5">
                <Label>Modelo *</Label>
                <Input {...register('modelo')} placeholder="Corolla" className="" />
                {errors.modelo && <p className="text-xs text-destructive">{errors.modelo.message}</p>}
              </div>
            </div>

            <div className="grid grid-cols-2 gap-4">
              <div className="space-y-1.5">
                <Label>Cor</Label>
                <Input {...register('cor')} placeholder="Prata" className="" />
              </div>
              <div className="space-y-1.5">
                <Label>Quilometragem</Label>
                <Input
                  type="number"
                  {...register('quilometragem')}
                  placeholder="50000"
                  className=""
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
