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
    <div className="flex min-h-screen bg-ocean-50">
      {/* Sidebar */}
      <aside className="w-56 shrink-0 flex flex-col border-r border-ocean-200 bg-white">
        <div className="flex items-center gap-2 px-5 py-5 border-b border-ocean-200">
          <Wrench size={18} className="text-ocean-600" />
          <span className="font-semibold text-sm tracking-tight text-ocean-900">CRM Oficina</span>
        </div>

        <nav className="flex-1 px-3 py-4 flex flex-col gap-1">
          {navItems.map(({ to, icon: Icon, label }) => (
            <NavLink
              key={to}
              to={to}
              className={({ isActive }) =>
                cn(
                  'flex items-center gap-2.5 px-3 py-2 rounded-md text-sm transition-colors',
                  isActive
                    ? 'bg-ocean-600 text-white shadow-sm'
                    : 'text-muted-foreground hover:text-ocean-700 hover:bg-ocean-50'
                )
              }
            >
              <Icon size={15} />
              {label}
            </NavLink>
          ))}
        </nav>

        <div className="px-3 py-4 border-t border-ocean-200">
          <div className="px-3 mb-3">
            <p className="text-xs font-medium text-ocean-900 truncate">{funcionario?.nome ?? '—'}</p>
            <p className="text-xs text-muted-foreground truncate">{funcionario?.cargo ?? '—'}</p>
          </div>
          <button
            onClick={handleLogout}
            className="flex w-full items-center gap-2.5 px-3 py-2 rounded-md text-sm text-muted-foreground hover:text-ocean-700 hover:bg-ocean-50 transition-colors"
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
