import { create } from 'zustand'
import { persist } from 'zustand/middleware'
import type { AuthState, FuncionarioLogado } from '../types/auth'
import { isTokenExpired } from '../utils/jwt'
import api from '../services/api'

export const useAuthStore = create<AuthState>()(
  persist(
    (set, get) => ({
      token: null,
      refreshToken: null,
      funcionario: null,

      login: async (usuario: string, senha: string) => {
        const { data: loginData } = await api.post('/auth/login', { usuario, senha })
        set({ token: loginData.accessToken, refreshToken: loginData.refreshToken, funcionario: null })
        try {
          const { data: me } = await api.get<FuncionarioLogado>('/funcionarios/me')
          set({ funcionario: me })
        } catch {
          set({ token: null, refreshToken: null, funcionario: null })
          throw new Error('Não foi possível carregar os dados do usuário.')
        }
      },

      logout: () => {
        const { refreshToken } = get()
        if (refreshToken) {
          api.post('/auth/logout', { refreshToken }).catch(() => {})
        }
        set({ token: null, refreshToken: null, funcionario: null })
      },

      isExpired: () => isTokenExpired(get().token),
    }),
    {
      name: 'crm-oficina-auth',
      partialize: (state) => ({
        token: state.token,
        refreshToken: state.refreshToken,
        funcionario: state.funcionario,
      }),
    }
  )
)
