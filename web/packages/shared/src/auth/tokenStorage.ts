import type { AppScope } from '../types'

const TOKEN_KEYS: Record<AppScope, string> = {
  portal: 'hei_portal_token',
  admin: 'hei_admin_token',
}

export function createTokenStorage(scope: AppScope) {
  const key = TOKEN_KEYS[scope]

  return {
    get(): string | null {
      return localStorage.getItem(key)
    },
    set(token: string) {
      localStorage.setItem(key, token)
    },
    clear() {
      localStorage.removeItem(key)
    },
  }
}
