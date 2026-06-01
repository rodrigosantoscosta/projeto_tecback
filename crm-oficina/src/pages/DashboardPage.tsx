import { LogOut, Wrench } from 'lucide-react'
import { useAuthStore } from '../store/authStore'
import { useNavigate } from 'react-router-dom'

export function DashboardPage() {
  const { funcionario, logout } = useAuthStore()
  const navigate = useNavigate()

  const handleLogout = () => {
    logout()
    navigate('/login', { replace: true })
  }

  return (
    <div className="min-h-screen bg-zinc-950 text-white">
      <header className="border-b border-zinc-800 px-6 py-4 flex items-center justify-between">
        <div className="flex items-center gap-2">
          <Wrench size={20} className="text-blue-500" />
          <span className="font-semibold">CRM Oficina</span>
        </div>
        <div className="flex items-center gap-4">
          <span className="text-zinc-400 text-sm">
            {funcionario?.nome ?? '—'} · {funcionario?.cargo ?? '—'}
          </span>
          <button
            onClick={handleLogout}
            className="flex items-center gap-1.5 text-zinc-400 hover:text-white text-sm transition"
          >
            <LogOut size={15} />
            Sair
          </button>
        </div>
      </header>
      <main className="flex flex-col items-center justify-center h-[calc(100vh-65px)] gap-3">
        <p className="text-zinc-500 text-sm">Dashboard em construção</p>
      </main>
    </div>
  )
}
