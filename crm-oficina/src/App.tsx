import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom'
import { QueryClient, QueryClientProvider } from '@tanstack/react-query'
import { LoginPage } from './pages/auth/LoginPage'
import { DashboardPage } from './pages/DashboardPage'
import { AppLayout } from './components/layout/AppLayout'
import { ProtectedRoute } from './components/layout/ProtectedRoute'
import { ClientesListPage } from './pages/clientes/ClientesListPage'
import { ClienteFormPage } from './pages/clientes/ClienteFormPage'
import { ClienteDetailPage } from './pages/clientes/ClienteDetailPage'
import { VeiculosListPage } from './pages/veiculos/VeiculosListPage'
import { VeiculoFormPage } from './pages/veiculos/VeiculoFormPage'
import { VeiculoDetailPage } from './pages/veiculos/VeiculoDetailPage'
import { AtendimentosListPage } from './pages/atendimentos/AtendimentosListPage'
import { AtendimentoFormPage } from './pages/atendimentos/AtendimentoFormPage'
import { AtendimentoDetailPage } from './pages/atendimentos/AtendimentoDetailPage'

const queryClient = new QueryClient({
  defaultOptions: {
    queries: { retry: 1, staleTime: 30_000 },
  },
})

export default function App() {
  return (
    <QueryClientProvider client={queryClient}>
      <BrowserRouter>
        <Routes>
          <Route path="/login" element={<LoginPage />} />
          <Route element={<ProtectedRoute />}>
            <Route element={<AppLayout />}>
              <Route path="/dashboard" element={<DashboardPage />} />
              <Route path="/clientes" element={<ClientesListPage />} />
              <Route path="/clientes/novo" element={<ClienteFormPage />} />
              <Route path="/clientes/:id" element={<ClienteDetailPage />} />
              <Route path="/clientes/:id/editar" element={<ClienteFormPage />} />
              <Route path="/veiculos" element={<VeiculosListPage />} />
              <Route path="/veiculos/novo" element={<VeiculoFormPage />} />
              <Route path="/veiculos/:id" element={<VeiculoDetailPage />} />
              <Route path="/veiculos/:id/editar" element={<VeiculoFormPage />} />
              <Route path="/atendimentos" element={<AtendimentosListPage />} />
              <Route path="/atendimentos/novo" element={<AtendimentoFormPage />} />
              <Route path="/atendimentos/:id" element={<AtendimentoDetailPage />} />
              <Route path="/atendimentos/:id/editar" element={<AtendimentoFormPage />} />
            </Route>
          </Route>
          <Route path="*" element={<Navigate to="/dashboard" replace />} />
        </Routes>
      </BrowserRouter>
    </QueryClientProvider>
  )
}
