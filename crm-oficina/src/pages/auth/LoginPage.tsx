import { useState } from 'react'
import { useNavigate, useSearchParams, Navigate } from 'react-router-dom'
import { useForm } from 'react-hook-form'
import { zodResolver } from '@hookform/resolvers/zod'
import { z } from 'zod'
import { Eye, EyeOff, Wrench } from 'lucide-react'
import { useAuthStore } from '../../store/authStore'
import { isAxiosError } from 'axios'

const schema = z.object({
  usuario: z.string().min(1, 'Informe o usuário'),
  senha: z.string().min(1, 'Informe a senha'),
})
type FormData = z.infer<typeof schema>
type PageStatus = 'idle' | 'loading' | 'error_auth' | 'error_server'

export function LoginPage() {
  const navigate = useNavigate()
  const [searchParams] = useSearchParams()
  const { login, token, isExpired } = useAuthStore()
  const [status, setStatus] = useState<PageStatus>('idle')
  const [showPassword, setShowPassword] = useState(false)

  const { register, handleSubmit, setFocus, formState: { errors } } = useForm<FormData>({
    resolver: zodResolver(schema),
  })

  if (token && !isExpired()) return <Navigate to="/dashboard" replace />

  const onSubmit = async (data: FormData) => {
    setStatus('loading')
    try {
      await login(data.usuario, data.senha)
      navigate('/dashboard', { replace: true })
    } catch (err) {
      if (isAxiosError(err)) {
        if (err.response?.status === 401) { setStatus('error_auth'); setFocus('usuario') }
        else setStatus('error_server')
      } else {
        setStatus('error_server')
      }
    }
  }

  const isLoading = status === 'loading'
  const sessionExpired = searchParams.get('reason') === 'expired'

  return (
    <div className="min-h-screen bg-zinc-950 flex items-center justify-center px-4">
      <div className="w-full max-w-sm">
        <div className="flex flex-col items-center gap-3 mb-8">
          <div className="bg-blue-600 p-3 rounded-xl">
            <Wrench className="text-white" size={28} />
          </div>
          <h1 className="text-white text-2xl font-bold tracking-tight">CRM Oficina</h1>
          <p className="text-zinc-400 text-sm">Acesse sua conta para continuar</p>
        </div>

        {sessionExpired && (
          <div className="mb-4 px-4 py-3 rounded-lg bg-blue-950 border border-blue-700 text-blue-300 text-sm">
            Sua sessão expirou. Faça login novamente.
          </div>
        )}
        {status === 'error_auth' && (
          <div className="mb-4 px-4 py-3 rounded-lg bg-red-950 border border-red-700 text-red-300 text-sm">
            Usuário ou senha inválidos.
          </div>
        )}
        {status === 'error_server' && (
          <div className="mb-4 px-4 py-3 rounded-lg bg-zinc-800 border border-zinc-600 text-zinc-300 text-sm">
            Não foi possível conectar ao servidor. Tente novamente.
          </div>
        )}

        <form onSubmit={handleSubmit(onSubmit)} className="flex flex-col gap-5" noValidate>
          <div className="flex flex-col gap-1.5">
            <label htmlFor="usuario" className="text-zinc-300 text-sm font-medium">Usuário</label>
            <input
              id="usuario" type="text" autoComplete="username" autoFocus disabled={isLoading}
              {...register('usuario')}
              className="bg-zinc-900 border border-zinc-700 rounded-lg px-3 py-2.5 text-white text-sm
                         placeholder-zinc-500 focus:outline-none focus:ring-2 focus:ring-blue-600
                         disabled:opacity-50 disabled:cursor-not-allowed transition"
              placeholder="seu.usuario"
            />
            {errors.usuario && <span className="text-red-400 text-xs">{errors.usuario.message}</span>}
          </div>

          <div className="flex flex-col gap-1.5">
            <label htmlFor="senha" className="text-zinc-300 text-sm font-medium">Senha</label>
            <div className="relative">
              <input
                id="senha" type={showPassword ? 'text' : 'password'}
                autoComplete="current-password" disabled={isLoading}
                {...register('senha')}
                className="w-full bg-zinc-900 border border-zinc-700 rounded-lg px-3 py-2.5 pr-10
                           text-white text-sm placeholder-zinc-500 focus:outline-none focus:ring-2
                           focus:ring-blue-600 disabled:opacity-50 disabled:cursor-not-allowed transition"
                placeholder="••••••••"
              />
              <button
                type="button" onClick={() => setShowPassword((v) => !v)} tabIndex={-1}
                className="absolute right-3 top-1/2 -translate-y-1/2 text-zinc-400 hover:text-zinc-200 transition"
                aria-label={showPassword ? 'Ocultar senha' : 'Mostrar senha'}
              >
                {showPassword ? <EyeOff size={16} /> : <Eye size={16} />}
              </button>
            </div>
            {errors.senha && <span className="text-red-400 text-xs">{errors.senha.message}</span>}
          </div>

          <button
            type="submit" disabled={isLoading}
            className="bg-blue-600 hover:bg-blue-500 disabled:bg-blue-800 disabled:cursor-not-allowed
                       text-white font-medium rounded-lg py-2.5 text-sm transition flex items-center
                       justify-center gap-2"
          >
            {isLoading ? (
              <><span className="w-4 h-4 border-2 border-white/30 border-t-white rounded-full animate-spin" />Entrando...</>
            ) : 'Entrar'}
          </button>
        </form>
      </div>
    </div>
  )
}
