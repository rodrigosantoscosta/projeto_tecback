import { useEffect } from 'react'
import { useNavigate, useParams } from 'react-router-dom'
import { useForm, Controller } from 'react-hook-form'
import { zodResolver } from '@hookform/resolvers/zod'
import { z } from 'zod'
import { ArrowLeft } from 'lucide-react'
import { Button } from '../../components/ui/button'
import { Input } from '../../components/ui/input'
import { Label } from '../../components/ui/label'
import { Card, CardContent, CardHeader, CardTitle } from '../../components/ui/card'
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from '../../components/ui/select'
import { Combobox } from '../../components/ui/combobox'
import { useAtendimento, useCadastrarAtendimento, useAtualizarAtendimento } from '../../hooks/useAtendimentos'
import { useClientes } from '../../hooks/useClientes'
import { useVeiculosPorCliente } from '../../hooks/useVeiculos'
import { useFuncionarios } from '../../hooks/useFuncionarios'
import { StatusAtendimento, STATUS_LABELS, STATUS_ORDER } from '../../types/atendimento'

const schema = z.object({
  descricaoServico: z.string().min(1, 'Descrição do serviço obrigatória'),
  statusAtendimento: z.string().min(1, 'Selecione um status'),
  clienteId: z.string().min(1, 'Selecione um cliente'),
  veiculoPlaca: z.string().min(1, 'Selecione um veículo'),
  funcionarioId: z.string().min(1, 'Selecione um funcionário'),
})

type FormData = z.infer<typeof schema>

export function AtendimentoFormPage() {
  const navigate = useNavigate()
  const { id } = useParams<{ id: string }>()
  const isEdit = !!id

  const { data: atendimento, isLoading: loadingAtendimento } = useAtendimento(id ?? '')
  const { data: clientes = [] } = useClientes()
  const { data: funcionarios = [] } = useFuncionarios()

  const {
    register,
    handleSubmit,
    setValue,
    control,
    watch,
    formState: { errors, isSubmitting },
  } = useForm<FormData>({
    resolver: zodResolver(schema),
    defaultValues: { statusAtendimento: StatusAtendimento.AGUARDANDO },
  })

  const watchedClienteId = watch('clienteId')

  const { data: veiculos = [] } = useVeiculosPorCliente(watchedClienteId ?? '')

  const cadastrar = useCadastrarAtendimento()
  const atualizar = useAtualizarAtendimento(id ?? '')

  useEffect(() => {
    if (atendimento) {
      setValue('descricaoServico', atendimento.descricaoServico)
      setValue('statusAtendimento', atendimento.status)
      setValue('clienteId', atendimento.cliente)
      setValue('veiculoPlaca', atendimento.veiculo)
      setValue('funcionarioId', atendimento.funcionario)
    }
  }, [atendimento, setValue])

  useEffect(() => {
    if (watchedClienteId && !isEdit) {
      setValue('veiculoPlaca', '')
    }
  }, [watchedClienteId, isEdit, setValue])

  const onSubmit = async (data: FormData) => {
    const payload = {
      descricaoServico: data.descricaoServico,
      statusAtendimento: data.statusAtendimento,
      clienteId: data.clienteId,
      veiculoPlaca: data.veiculoPlaca,
      funcionarioId: data.funcionarioId,
    }
    if (isEdit) {
      await atualizar.mutateAsync(payload)
    } else {
      await cadastrar.mutateAsync(payload)
    }
    navigate('/atendimentos')
  }

  if (isEdit && loadingAtendimento) {
    return <main className="p-6 text-muted-foreground text-sm">Carregando...</main>
  }

  const mutationError = isEdit ? atualizar.error : cadastrar.error
  const errorMsg = mutationError
    ? ((mutationError as { response?: { data?: { message?: string } } })?.response?.data?.message ?? 'Erro ao salvar. Tente novamente.')
    : null

  const clienteOptions = clientes.map(c => ({ value: c.id, label: c.nomeCompleto }))
  const veiculoOptions = veiculos.map(v => ({ value: v.placa, label: `${v.placa} — ${v.marca} ${v.modelo}` }))
  const funcionarioOptions = funcionarios.map(f => ({ value: f.id, label: `${f.nome} (${f.cargo})` }))

  return (
    <main className="p-6 max-w-xl">
      <button
        onClick={() => navigate('/atendimentos')}
        className="flex items-center gap-1.5 text-sm text-muted-foreground hover:text-ocean-900 mb-5 transition-colors"
      >
        <ArrowLeft size={14} />
        Voltar
      </button>

      <h1 className="text-xl font-semibold mb-6">{isEdit ? 'Editar atendimento' : 'Novo atendimento'}</h1>

      <form onSubmit={handleSubmit(onSubmit)} className="space-y-5">
        {errorMsg && (
          <p className="text-sm text-destructive bg-destructive/10 border border-destructive/30 rounded-md px-3 py-2">
            {errorMsg}
          </p>
        )}

        <Card>
          <CardHeader className="pb-3">
            <CardTitle className="text-sm font-medium">Dados do atendimento</CardTitle>
          </CardHeader>
          <CardContent className="space-y-4">
            <div className="space-y-1.5">
              <Label htmlFor="descricaoServico">Descrição do serviço *</Label>
              <Input id="descricaoServico" {...register('descricaoServico')} placeholder="Troca de óleo e filtros" />
              {errors.descricaoServico && <p className="text-xs text-destructive">{errors.descricaoServico.message}</p>}
            </div>

            <div className="space-y-1.5">
              <Label>Status *</Label>
              <Controller
                control={control}
                name="statusAtendimento"
                render={({ field }) => (
                  <Select value={field.value} onValueChange={field.onChange}>
                    <SelectTrigger className="w-full">
                      <SelectValue placeholder="Selecionar status..." />
                    </SelectTrigger>
                    <SelectContent>
                      {STATUS_ORDER.map(s => (
                        <SelectItem key={s} value={s}>{STATUS_LABELS[s]}</SelectItem>
                      ))}
                    </SelectContent>
                  </Select>
                )}
              />
              {errors.statusAtendimento && <p className="text-xs text-destructive">{errors.statusAtendimento.message}</p>}
            </div>

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

            <div className="space-y-1.5">
              <Label>Veículo *</Label>
              <Controller
                control={control}
                name="veiculoPlaca"
                render={({ field }) => (
                  <Combobox
                    options={veiculoOptions}
                    value={field.value ?? ''}
                    onChange={field.onChange}
                    placeholder={watchedClienteId ? 'Selecionar veículo...' : 'Selecione um cliente primeiro'}
                    searchPlaceholder="Buscar veículo..."
                    disabled={!watchedClienteId}
                  />
                )}
              />
              {errors.veiculoPlaca && <p className="text-xs text-destructive">{errors.veiculoPlaca.message}</p>}
            </div>

            <div className="space-y-1.5">
              <Label>Funcionário *</Label>
              <Controller
                control={control}
                name="funcionarioId"
                render={({ field }) => (
                  <Combobox
                    options={funcionarioOptions}
                    value={field.value ?? ''}
                    onChange={field.onChange}
                    placeholder="Selecionar funcionário..."
                    searchPlaceholder="Buscar funcionário..."
                  />
                )}
              />
              {errors.funcionarioId && <p className="text-xs text-destructive">{errors.funcionarioId.message}</p>}
            </div>
          </CardContent>
        </Card>

        <div className="flex gap-3 justify-end">
          <Button type="button" variant="ghost" onClick={() => navigate('/atendimentos')}>
            Cancelar
          </Button>
          <Button type="submit" disabled={isSubmitting}>
            {isSubmitting ? 'Salvando...' : isEdit ? 'Salvar alterações' : 'Cadastrar atendimento'}
          </Button>
        </div>
      </form>
    </main>
  )
}
