import { useEffect, useRef } from 'react'
import { useNavigate, useParams } from 'react-router-dom'
import { useForm } from 'react-hook-form'
import { zodResolver } from '@hookform/resolvers/zod'
import { z } from 'zod'
import { ArrowLeft } from 'lucide-react'
import { Button } from '../../components/ui/button'
import { Input } from '../../components/ui/input'
import { Label } from '../../components/ui/label'
import { Card, CardContent, CardHeader, CardTitle } from '../../components/ui/card'
import { useCliente, useCadastrarCliente, useAtualizarCliente } from '../../hooks/useClientes'
import { clienteService } from '../../services/clienteService'
import { formatCpfCnpj, formatTelefone, formatCep, stripMask } from '../../utils/formatadores'
import { validarCpfOuCnpj } from '../../utils/validadores'

const schema = z.object({
  nomeCompleto: z.string().min(2, 'Nome obrigatório'),
  cpfCNPJ: z.string().refine(v => validarCpfOuCnpj(v), 'CPF ou CNPJ inválido'),
  telefone: z.string().min(10, 'Telefone obrigatório'),
  email: z.string().email('E-mail inválido'),
  cep: z.string().min(8, 'CEP obrigatório'),
  numero: z.string().min(1, 'Número obrigatório'),
  complemento: z.string().optional(),
  logradouro: z.string().optional(),
  bairro: z.string().optional(),
  cidade: z.string().optional(),
  estado: z.string().max(2).optional(),
})

type FormData = z.infer<typeof schema>

export function ClienteFormPage() {
  const navigate = useNavigate()
  const { id } = useParams<{ id: string }>()
  const isEdit = !!id

  const { data: cliente, isLoading: loadingCliente } = useCliente(id ?? '')
  const cadastrar = useCadastrarCliente()
  const atualizar = useAtualizarCliente(id ?? '')

  const cepDebounce = useRef<ReturnType<typeof setTimeout> | null>(null)

  const { register, handleSubmit, setValue, watch, formState: { errors, isSubmitting } } = useForm<FormData>({
    resolver: zodResolver(schema),
  })

  useEffect(() => {
    if (cliente) {
      setValue('nomeCompleto', cliente.nomeCompleto)
      setValue('cpfCNPJ', formatCpfCnpj(cliente.cpfCNPJ))
      setValue('telefone', formatTelefone(cliente.telefone))
      setValue('email', cliente.email ?? '')
      setValue('cep', cliente.endereco?.cep ?? '')
      setValue('logradouro', cliente.endereco?.logradouro ?? '')
      setValue('numero', cliente.endereco?.numero ?? '')
      setValue('complemento', cliente.endereco?.complemento ?? '')
      setValue('bairro', cliente.endereco?.bairro ?? '')
      setValue('cidade', cliente.endereco?.cidade ?? '')
      setValue('estado', cliente.endereco?.estado ?? '')
    }
  }, [cliente, setValue])

  const onSubmit = async (data: FormData) => {
    const payload = {
      nomeCompleto: data.nomeCompleto,
      cpfCNPJ: stripMask(data.cpfCNPJ),
      telefone: stripMask(data.telefone),
      email: data.email,
      cep: stripMask(data.cep),
      numero: data.numero,
      complemento: data.complemento || undefined,
    }
    if (isEdit) {
      await atualizar.mutateAsync(payload)
    } else {
      await cadastrar.mutateAsync(payload)
    }
    navigate('/clientes')
  }

  const handleCepChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const masked = formatCep(e.target.value)
    setValue('cep', masked)
    const digits = stripMask(masked)
    if (digits.length === 8) {
      if (cepDebounce.current) clearTimeout(cepDebounce.current)
      cepDebounce.current = setTimeout(async () => {
        try {
          const data = await clienteService.consultarCep(digits)
          if (!data.erro) {
            setValue('logradouro', data.logradouro)
            setValue('bairro', data.bairro)
            setValue('cidade', data.localidade)
            setValue('estado', data.uf)
          }
        } catch { /* ViaCEP indisponível */ }
      }, 500)
    }
  }

  if (isEdit && loadingCliente) {
    return <main className="p-6 text-muted-foreground text-sm">Carregando...</main>
  }

  const mutationError = isEdit ? atualizar.error : cadastrar.error
  const errorMsg = mutationError
    ? ((mutationError as { response?: { data?: { message?: string } } })?.response?.data?.message ?? 'Erro ao salvar. Tente novamente.')
    : null

  return (
    <main className="p-6 max-w-2xl">
      <button
        onClick={() => navigate('/clientes')}
        className="flex items-center gap-1.5 text-sm text-muted-foreground hover:text-ocean-700 mb-5 transition-colors"
      >
        <ArrowLeft size={14} />
        Voltar
      </button>

      <h1 className="text-xl font-semibold text-ocean-900 mb-6">{isEdit ? 'Editar cliente' : 'Novo cliente'}</h1>

      <form onSubmit={handleSubmit(onSubmit)} className="space-y-5">
        {errorMsg && (
          <p className="text-sm text-destructive bg-red-50 border border-red-200 rounded-md px-3 py-2">
            {errorMsg}
          </p>
        )}

        <Card>
          <CardHeader className="pb-3">
            <CardTitle className="text-sm font-medium">Dados pessoais</CardTitle>
          </CardHeader>
          <CardContent className="space-y-4">
            <div className="space-y-1.5">
              <Label htmlFor="nomeCompleto">Nome *</Label>
              <Input
                id="nomeCompleto"
                {...register('nomeCompleto')}
                placeholder="Nome completo"
              />
              {errors.nomeCompleto && <p className="text-xs text-destructive">{errors.nomeCompleto.message}</p>}
            </div>

            <div className="grid grid-cols-2 gap-4">
              <div className="space-y-1.5">
                <Label htmlFor="cpfCNPJ">CPF / CNPJ *</Label>
                <Input
                  id="cpfCNPJ"
                  {...register('cpfCNPJ')}
                  placeholder="000.000.000-00"
                  onChange={e => setValue('cpfCNPJ', formatCpfCnpj(e.target.value))}
                  value={watch('cpfCNPJ') ?? ''}
                />
                {errors.cpfCNPJ && <p className="text-xs text-destructive">{errors.cpfCNPJ.message}</p>}
              </div>

              <div className="space-y-1.5">
                <Label htmlFor="telefone">Telefone *</Label>
                <Input
                  id="telefone"
                  {...register('telefone')}
                  placeholder="(00) 00000-0000"
                  onChange={e => setValue('telefone', formatTelefone(e.target.value))}
                  value={watch('telefone') ?? ''}
                />
                {errors.telefone && <p className="text-xs text-destructive">{errors.telefone.message}</p>}
              </div>
            </div>

            <div className="space-y-1.5">
              <Label htmlFor="email">E-mail *</Label>
              <Input
                id="email"
                type="email"
                {...register('email')}
                placeholder="email@exemplo.com"
              />
              {errors.email && <p className="text-xs text-destructive">{errors.email.message}</p>}
            </div>
          </CardContent>
        </Card>

        <Card>
          <CardHeader className="pb-3">
            <CardTitle className="text-sm font-medium">Endereço</CardTitle>
          </CardHeader>
          <CardContent className="space-y-4">
            <div className="grid grid-cols-3 gap-4">
              <div className="space-y-1.5">
                <Label htmlFor="cep">CEP *</Label>
                <Input
                  id="cep"
                  placeholder="00000-000"
                  value={watch('cep') ?? ''}
                  onChange={handleCepChange}
                />
                {errors.cep && <p className="text-xs text-destructive">{errors.cep.message}</p>}
              </div>
              <div className="col-span-2 space-y-1.5">
                <Label htmlFor="logradouro">Logradouro</Label>
                <Input
                  id="logradouro"
                  {...register('logradouro')}
                  placeholder="Rua, Av..."
                />
              </div>
            </div>

            <div className="grid grid-cols-3 gap-4">
              <div className="space-y-1.5">
                <Label htmlFor="numero">Número *</Label>
                <Input id="numero" {...register('numero')} placeholder="123" />
                {errors.numero && <p className="text-xs text-destructive">{errors.numero.message}</p>}
              </div>
              <div className="col-span-2 space-y-1.5">
                <Label htmlFor="complemento">Complemento</Label>
                <Input id="complemento" {...register('complemento')} placeholder="Apto, sala..." />
              </div>
            </div>

            <div className="grid grid-cols-3 gap-4">
              <div className="col-span-2 space-y-1.5">
                <Label htmlFor="cidade">Cidade</Label>
                <Input id="cidade" {...register('cidade')} />
              </div>
              <div className="space-y-1.5">
                <Label htmlFor="estado">UF</Label>
                <Input
                  id="estado"
                  {...register('estado')}
                  maxLength={2}
                  placeholder="PB"
                  className="uppercase"
                  onChange={e => setValue('estado', e.target.value.toUpperCase())}
                />
              </div>
            </div>

            <div className="space-y-1.5">
              <Label htmlFor="bairro">Bairro</Label>
              <Input id="bairro" {...register('bairro')} />
            </div>
          </CardContent>
        </Card>

        <div className="flex gap-3 justify-end">
          <Button type="button" variant="ghost" onClick={() => navigate('/clientes')}>
            Cancelar
          </Button>
          <Button type="submit" disabled={isSubmitting}>
            {isSubmitting ? 'Salvando...' : isEdit ? 'Salvar alterações' : 'Cadastrar cliente'}
          </Button>
        </div>
      </form>
    </main>
  )
}
