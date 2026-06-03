import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query'
import { atendimentoService } from '../services/atendimentoService'
import type { AtendimentoRequest } from '../types/atendimento'

export const ATENDIMENTOS_KEY = ['atendimentos'] as const

export function useAtendimentos() {
  return useQuery({ queryKey: ATENDIMENTOS_KEY, queryFn: atendimentoService.listar })
}

export function useAtendimento(id: string) {
  return useQuery({
    queryKey: [...ATENDIMENTOS_KEY, id],
    queryFn: () => atendimentoService.buscarPorId(id),
    enabled: !!id,
  })
}

export function useCadastrarAtendimento() {
  const qc = useQueryClient()
  return useMutation({
    mutationFn: (data: AtendimentoRequest) => atendimentoService.cadastrar(data),
    onSuccess: () => qc.invalidateQueries({ queryKey: ATENDIMENTOS_KEY }),
  })
}

export function useAtualizarAtendimento(id: string) {
  const qc = useQueryClient()
  return useMutation({
    mutationFn: (data: AtendimentoRequest) => atendimentoService.atualizar(id, data),
    onSuccess: () => qc.invalidateQueries({ queryKey: ATENDIMENTOS_KEY }),
  })
}

export function useExcluirAtendimento() {
  const qc = useQueryClient()
  return useMutation({
    mutationFn: (id: string) => atendimentoService.excluir(id),
    onSuccess: () => qc.invalidateQueries({ queryKey: ATENDIMENTOS_KEY }),
  })
}
