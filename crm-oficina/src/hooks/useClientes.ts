import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query'
import { clienteService } from '../services/clienteService'
import type { ClienteRequest } from '../types/cliente'

export const CLIENTES_KEY = ['clientes'] as const

export function useClientes() {
  return useQuery({ queryKey: CLIENTES_KEY, queryFn: clienteService.listar })
}

export function useCliente(id: string) {
  return useQuery({
    queryKey: [...CLIENTES_KEY, id],
    queryFn: () => clienteService.buscarPorId(id),
    enabled: !!id,
  })
}

export function useCadastrarCliente() {
  const qc = useQueryClient()
  return useMutation({
    mutationFn: (data: ClienteRequest) => clienteService.cadastrar(data),
    onSuccess: () => qc.invalidateQueries({ queryKey: CLIENTES_KEY }),
  })
}

export function useAtualizarCliente(id: string) {
  const qc = useQueryClient()
  return useMutation({
    mutationFn: (data: ClienteRequest) => clienteService.atualizar(id, data),
    onSuccess: () => qc.invalidateQueries({ queryKey: CLIENTES_KEY }),
  })
}

export function useExcluirCliente() {
  const qc = useQueryClient()
  return useMutation({
    mutationFn: (id: string) => clienteService.excluir(id),
    onSuccess: () => qc.invalidateQueries({ queryKey: CLIENTES_KEY }),
  })
}
