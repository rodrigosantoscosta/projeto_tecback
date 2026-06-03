import { Users, Car, Wrench } from 'lucide-react'
import { useClientes } from '../hooks/useClientes'
import { useVeiculos } from '../hooks/useVeiculos'
import { useAtendimentos } from '../hooks/useAtendimentos'

function StatCard({ label, value, icon: Icon }: { label: string; value: number | string; icon: React.ElementType }) {
  return (
    <div className="bg-white border border-sand-200 rounded-lg p-5 shadow-sm">
      <div className="flex items-center justify-between mb-3">
        <span className="text-sm text-muted-foreground">{label}</span>
        <div className="bg-teal-50 p-1.5 rounded-lg">
          <Icon size={16} className="text-teal-500" />
        </div>
      </div>
      <p className="text-2xl font-semibold text-ocean-900">{value}</p>
    </div>
  )
}

export function DashboardPage() {
  const { data: clientes = [] } = useClientes()
  const { data: veiculos = [] } = useVeiculos()
  const { data: atendimentos = [] } = useAtendimentos()

  return (
    <main className="p-6">
      <h1 className="text-xl font-semibold text-ocean-900 mb-1">Dashboard</h1>
      <p className="text-sm text-muted-foreground mb-6">Visão geral da oficina</p>
      <div className="grid grid-cols-3 gap-4 mb-8">
        <StatCard label="Clientes" value={clientes.length} icon={Users} />
        <StatCard label="Veículos" value={veiculos.length} icon={Car} />
        <StatCard label="Atendimentos" value={atendimentos.length} icon={Wrench} />
      </div>
    </main>
  )
}
