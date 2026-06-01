import type { JwtPayload } from '../types/auth'

export function decodeJwt(token: string | null): JwtPayload | null {
  if (!token) return null
  try {
    const parts = token.split('.')
    if (parts.length !== 3) return null
    const payload = parts[1].replace(/-/g, '+').replace(/_/g, '/')
    return JSON.parse(atob(payload)) as JwtPayload
  } catch {
    return null
  }
}

export function isTokenExpired(token: string | null): boolean {
  const payload = decodeJwt(token)
  if (!payload) return true
  return payload.exp * 1000 < Date.now()
}
