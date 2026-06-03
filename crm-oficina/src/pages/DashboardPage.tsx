import { Users, Car, Wrench } from 'lucide-react'
import { useClientes } from '../hooks/useClientes'
import { useVeiculos } from '../hooks/useVeiculos'

function StatCard({ label, value, icon: Icon }: { label: string; value: number | string; icon: React.ElementType }) {
  return (
    <div className="bg-white border border-ocean-200 rounded-lg p-5 shadow-sm">
      <div className="flex items-center justify-between mb-3">
        <span className="text-sm text-muted-foreground">{label}</span>
        <Icon size={16} className="text-ocean-400" />
      </div>
      <p className="text-2xl font-semibold text-ocean-900">{value}</p>
    </div>
  )
}

export function DashboardPage() {
  const { data: clientes = [] } = useClientes()
  const { data: veiculos = [] } = useVeiculos()

  return (
    <main className="p-6">
      <h1 className="text-xl font-semibold text-ocean-900 mb-1">Dashboard</h1>
      <p className="text-sm text-muted-foreground mb-6">Visão geral da oficina</p>

      <div className="grid grid-cols-3 gap-4 mb-8">
        <StatCard label="Clientes" value={clientes.length} icon={Users} />
        <StatCard label="Veículos" value={veiculos.length} icon={Car} />
        <StatCard label="Atendimentos" value="—" icon={Wrench} />
      </div>

      <p className="text-muted-foreground text-sm">Módulo de atendimentos em breve.</p>
    </main>
  )
}
