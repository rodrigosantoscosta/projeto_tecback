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
  nome: z.string().min(2, 'Nome obrigatório'),
  cpfCNPJ: z.string().refine(v => validarCpfOuCnpj(v), 'CPF ou CNPJ inválido'),
  telefone: z.string().min(10, 'Telefone obrigatório'),
  email: z.string().email('E-mail inválido').optional().or(z.literal('')),
  cep: z.string().optional(),
  logradouro: z.string().optional(),
  numero: z.string().optional(),
  complemento: z.string().optional(),
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

  // Preencher formulário no modo edição
  useEffect(() => {
    if (cliente) {
      setValue('nome', cliente.nome)
      setValue('cpfCNPJ', formatCpfCnpj(cliente.cpfCNPJ))
      setValue('telefone', formatTelefone(cliente.telefone))
      setValue('email', cliente.email ?? '')
      setValue('cep', cliente.cep ?? '')
      setValue('logradouro', cliente.logradouro ?? '')
      setValue('numero', cliente.numero ?? '')
      setValue('complemento', cliente.complemento ?? '')
      setValue('bairro', cliente.bairro ?? '')
      setValue('cidade', cliente.cidade ?? '')
      setValue('estado', cliente.estado ?? '')
    }
  }, [cliente, setValue])

  const onSubmit = async (data: FormData) => {
    const payload = {
      nome: data.nome,
      cpfCNPJ: stripMask(data.cpfCNPJ),
      telefone: stripMask(data.telefone),
      email: data.email || undefined,
      cep: data.cep ? stripMask(data.cep) : undefined,
      logradouro: data.logradouro || undefined,
      numero: data.numero || undefined,
      complemento: data.complemento || undefined,
      bairro: data.bairro || undefined,
      cidade: data.cidade || undefined,
      estado: data.estado || undefined,
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
        } catch { /* ViaCEP indisponível — usuário preenche manualmente */ }
      }, 500)
    }
  }

  if (isEdit && loadingCliente) {
    return <main className="p-6 text-zinc-500 text-sm">Carregando...</main>
  }

  const mutationError = isEdit ? atualizar.error : cadastrar.error
  const errorMsg = mutationError
    ? ((mutationError as { response?: { data?: { message?: string } } })?.response?.data?.message ?? 'Erro ao salvar. Tente novamente.')
    : null

  return (
    <main className="p-6 max-w-2xl">
      <button
        onClick={() => navigate('/clientes')}
        className="flex items-center gap-1.5 text-sm text-zinc-400 hover:text-white mb-5 transition-colors"
      >
        <ArrowLeft size={14} />
        Voltar
      </button>

      <h1 className="text-xl font-semibold mb-6">{isEdit ? 'Editar cliente' : 'Novo cliente'}</h1>

      <form onSubmit={handleSubmit(onSubmit)} className="space-y-5">
        {errorMsg && (
          <p className="text-sm text-red-400 bg-red-950/30 border border-red-800/50 rounded-md px-3 py-2">
            {errorMsg}
          </p>
        )}

        <Card className="bg-zinc-900 border-zinc-800">
          <CardHeader className="pb-3">
            <CardTitle className="text-sm font-medium text-zinc-300">Dados pessoais</CardTitle>
          </CardHeader>
          <CardContent className="space-y-4">
            <div className="space-y-1.5">
              <Label htmlFor="nome">Nome *</Label>
              <Input
                id="nome"
                {...register('nome')}
                placeholder="Nome completo"
                className="bg-zinc-950 border-zinc-700"
              />
              {errors.nome && <p className="text-xs text-red-400">{errors.nome.message}</p>}
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
                  className="bg-zinc-950 border-zinc-700"
                />
                {errors.cpfCNPJ && <p className="text-xs text-red-400">{errors.cpfCNPJ.message}</p>}
              </div>

              <div className="space-y-1.5">
                <Label htmlFor="telefone">Telefone *</Label>
                <Input
                  id="telefone"
                  {...register('telefone')}
                  placeholder="(00) 00000-0000"
                  onChange={e => setValue('telefone', formatTelefone(e.target.value))}
                  value={watch('telefone') ?? ''}
                  className="bg-zinc-950 border-zinc-700"
                />
                {errors.telefone && <p className="text-xs text-red-400">{errors.telefone.message}</p>}
              </div>
            </div>

            <div className="space-y-1.5">
              <Label htmlFor="email">E-mail</Label>
              <Input
                id="email"
                type="email"
                {...register('email')}
                placeholder="email@exemplo.com"
                className="bg-zinc-950 border-zinc-700"
              />
              {errors.email && <p className="text-xs text-red-400">{errors.email.message}</p>}
            </div>
          </CardContent>
        </Card>

        <Card className="bg-zinc-900 border-zinc-800">
          <CardHeader className="pb-3">
            <CardTitle className="text-sm font-medium text-zinc-300">Endereço</CardTitle>
          </CardHeader>
          <CardContent className="space-y-4">
            <div className="grid grid-cols-3 gap-4">
              <div className="space-y-1.5">
                <Label htmlFor="cep">CEP</Label>
                <Input
                  id="cep"
                  placeholder="00000-000"
                  value={watch('cep') ?? ''}
                  onChange={handleCepChange}
                  className="bg-zinc-950 border-zinc-700"
                />
              </div>
              <div className="col-span-2 space-y-1.5">
                <Label htmlFor="logradouro">Logradouro</Label>
                <Input
                  id="logradouro"
                  {...register('logradouro')}
                  placeholder="Rua, Av..."
                  className="bg-zinc-950 border-zinc-700"
                />
              </div>
            </div>

            <div className="grid grid-cols-3 gap-4">
              <div className="space-y-1.5">
                <Label htmlFor="numero">Número</Label>
                <Input id="numero" {...register('numero')} placeholder="123" className="bg-zinc-950 border-zinc-700" />
              </div>
              <div className="col-span-2 space-y-1.5">
                <Label htmlFor="complemento">Complemento</Label>
                <Input id="complemento" {...register('complemento')} placeholder="Apto, sala..." className="bg-zinc-950 border-zinc-700" />
              </div>
            </div>

            <div className="grid grid-cols-3 gap-4">
              <div className="col-span-2 space-y-1.5">
                <Label htmlFor="cidade">Cidade</Label>
                <Input id="cidade" {...register('cidade')} className="bg-zinc-950 border-zinc-700" />
              </div>
              <div className="space-y-1.5">
                <Label htmlFor="estado">UF</Label>
                <Input
                  id="estado"
                  {...register('estado')}
                  maxLength={2}
                  placeholder="PB"
                  className="bg-zinc-950 border-zinc-700 uppercase"
                  onChange={e => setValue('estado', e.target.value.toUpperCase())}
                />
              </div>
            </div>

            <div className="space-y-1.5">
              <Label htmlFor="bairro">Bairro</Label>
              <Input id="bairro" {...register('bairro')} className="bg-zinc-950 border-zinc-700" />
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
