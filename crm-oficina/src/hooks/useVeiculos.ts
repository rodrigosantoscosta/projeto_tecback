import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query'
import { veiculoService } from '../services/veiculoService'
import type { VeiculoRequest } from '../types/veiculo'

export const VEICULOS_KEY = ['veiculos'] as const

export function useVeiculos() {
  return useQuery({ queryKey: VEICULOS_KEY, queryFn: veiculoService.listar })
}

export function useVeiculo(id: string) {
  return useQuery({
    queryKey: [...VEICULOS_KEY, id],
    queryFn: () => veiculoService.buscarPorId(id),
    enabled: !!id,
  })
}

export function useVeiculosPorCliente(clienteId: string) {
  return useQuery({
    queryKey: [...VEICULOS_KEY, 'cliente', clienteId],
    queryFn: () => veiculoService.listarPorCliente(clienteId),
    enabled: !!clienteId,
  })
}

export function useCadastrarVeiculo() {
  const qc = useQueryClient()
  return useMutation({
    mutationFn: (data: VeiculoRequest) => veiculoService.cadastrar(data),
    onSuccess: () => qc.invalidateQueries({ queryKey: VEICULOS_KEY }),
  })
}

export function useAtualizarVeiculo(id: string) {
  const qc = useQueryClient()
  return useMutation({
    mutationFn: (data: VeiculoRequest) => veiculoService.atualizar(id, data),
    onSuccess: () => qc.invalidateQueries({ queryKey: VEICULOS_KEY }),
  })
}

export function useExcluirVeiculo() {
  const qc = useQueryClient()
  return useMutation({
    mutationFn: (id: string) => veiculoService.excluir(id),
    onSuccess: () => qc.invalidateQueries({ queryKey: VEICULOS_KEY }),
  })
}
