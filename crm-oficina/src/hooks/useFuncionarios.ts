import { useQuery } from '@tanstack/react-query'
import { funcionarioService } from '../services/funcionarioService'

export const FUNCIONARIOS_KEY = ['funcionarios'] as const

export function useFuncionarios() {
  return useQuery({ queryKey: FUNCIONARIOS_KEY, queryFn: funcionarioService.listar })
}
