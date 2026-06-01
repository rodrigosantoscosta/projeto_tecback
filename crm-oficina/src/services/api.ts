import axios from 'axios'
import type { LoginResponse } from '../types/auth'

const BASE_URL = import.meta.env.VITE_API_URL ?? 'http://localhost:8080'

export const api = axios.create({
  baseURL: BASE_URL,
  headers: { 'Content-Type': 'application/json' },
})

api.interceptors.request.use((config) => {
  const skip = ['/auth/login', '/auth/refresh']
  if (skip.some((path) => config.url?.startsWith(path))) return config

  const raw = localStorage.getItem('crm-oficina-auth')
  if (raw) {
    try {
      const { state } = JSON.parse(raw)
      if (state?.token) {
        config.headers.Authorization = `Bearer ${state.token}`
      }
    } catch { /* localStorage corrompido */ }
  }
  return config
})

let isRefreshing = false
let pendingQueue: Array<{ resolve: (token: string) => void; reject: (err: unknown) => void }> = []

function processQueue(error: unknown, token: string | null) {
  pendingQueue.forEach(({ resolve, reject }) => error ? reject(error) : resolve(token!))
  pendingQueue = []
}

api.interceptors.response.use(
  (response) => response,
  async (error) => {
    const original = error.config

    if (original?.url?.startsWith('/auth/refresh')) {
      doLogout()
      return Promise.reject(error)
    }

    if (error.response?.status === 401 && !original._retry) {
      original._retry = true

      const raw = localStorage.getItem('crm-oficina-auth')
      const refreshToken = raw ? JSON.parse(raw)?.state?.refreshToken : null

      if (!refreshToken) { doLogout(); return Promise.reject(error) }

      if (isRefreshing) {
        return new Promise((resolve, reject) => {
          pendingQueue.push({ resolve, reject })
        }).then((token) => {
          original.headers.Authorization = `Bearer ${token}`
          return api(original)
        })
      }

      isRefreshing = true
      try {
        const { data } = await api.post<LoginResponse>('/auth/refresh', { refreshToken })
        const stored = localStorage.getItem('crm-oficina-auth')
        if (stored) {
          const parsed = JSON.parse(stored)
          parsed.state.token = data.accessToken
          parsed.state.refreshToken = data.refreshToken
          localStorage.setItem('crm-oficina-auth', JSON.stringify(parsed))
        }
        processQueue(null, data.accessToken)
        original.headers.Authorization = `Bearer ${data.accessToken}`
        return api(original)
      } catch (refreshError) {
        processQueue(refreshError, null)
        doLogout()
        return Promise.reject(refreshError)
      } finally {
        isRefreshing = false
      }
    }

    return Promise.reject(error)
  }
)

function doLogout() {
  localStorage.removeItem('crm-oficina-auth')
  window.location.href = '/login?reason=expired'
}

export default api
