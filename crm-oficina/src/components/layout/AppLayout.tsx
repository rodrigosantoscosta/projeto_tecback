import { NavLink, Outlet, useNavigate } from 'react-router-dom'
import { Wrench, LayoutDashboard, Users, Car, LogOut, ClipboardList } from 'lucide-react'
import { useAuthStore } from '../../store/authStore'
import { cn } from '../../lib/utils'

const navItems = [
  { to: '/dashboard', icon: LayoutDashboard, label: 'Dashboard' },
  { to: '/clientes', icon: Users, label: 'Clientes' },
  { to: '/veiculos', icon: Car, label: 'Veículos' },
  { to: '/atendimentos', icon: ClipboardList, label: 'Atendimentos' },
]

export function AppLayout() {
  const { funcionario, logout } = useAuthStore()
  const navigate = useNavigate()

  const handleLogout = () => {
    logout()
    navigate('/login', { replace: true })
  }

  return (
    <div className="flex min-h-screen bg-sand-50">
      {/* Sidebar */}
      <aside className="w-56 shrink-0 flex flex-col border-r border-ocean-800 bg-ocean-900">
        <div className="flex items-center gap-2.5 px-5 py-5 border-b border-ocean-800">
          <div className="bg-teal-500 p-1.5 rounded-lg">
            <Wrench size={16} className="text-white" />
          </div>
          <span className="font-semibold text-sm tracking-tight text-white">CRM Oficina</span>
        </div>

        <nav className="flex-1 px-3 py-4 flex flex-col gap-0.5">
          {navItems.map(({ to, icon: Icon, label }) => (
            <NavLink
              key={to}
              to={to}
              className={({ isActive }) =>
                cn(
                  'flex items-center gap-2.5 px-3 py-2 rounded-md text-sm transition-colors',
                  isActive
                    ? 'bg-teal-500 text-white shadow-sm'
                    : 'text-ocean-300 hover:text-white hover:bg-ocean-800'
                )
              }
            >
              <Icon size={15} />
              {label}
            </NavLink>
          ))}
        </nav>

        <div className="px-3 py-4 border-t border-ocean-800">
          <div className="px-3 mb-3">
            <p className="text-xs font-medium text-white truncate">{funcionario?.nome ?? '—'}</p>
            <p className="text-xs text-ocean-300 truncate">{funcionario?.cargo ?? '—'}</p>
          </div>
          <button
            onClick={handleLogout}
            className="flex w-full items-center gap-2.5 px-3 py-2 rounded-md text-sm text-ocean-300 hover:text-white hover:bg-ocean-800 transition-colors"
          >
            <LogOut size={15} />
            Sair
          </button>
        </div>
      </aside>

      {/* Main content */}
      <div className="flex-1 flex flex-col min-w-0">
        <Outlet />
      </div>
    </div>
  )
}
