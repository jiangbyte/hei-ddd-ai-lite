import { defineStore } from 'pinia'
import { computed, ref } from 'vue'
import type { UserProfile } from '@hei/shared'
import { authApi, tokenStorage } from '@/api'

export const useAuthStore = defineStore('portal-auth', () => {
  const token = ref<string | null>(tokenStorage.get())
  const profile = ref<UserProfile | null>(null)

  const isLoggedIn = computed(() => Boolean(token.value))

  function setToken(value: string) {
    token.value = value
    tokenStorage.set(value)
  }

  function clearSession() {
    token.value = null
    profile.value = null
    tokenStorage.clear()
  }

  async function login(username: string, password: string) {
    const result = await authApi.login({ username, password, clientType: 'PORTAL' })
    setToken(result.token)
    profile.value = await authApi.me()
    return result
  }

  async function register(username: string, password: string) {
    return authApi.register({ username, password })
  }

  async function fetchMe() {
    profile.value = await authApi.me()
    return profile.value
  }

  async function logout() {
    try {
      if (token.value) {
        await authApi.logout()
      }
    } finally {
      clearSession()
    }
  }

  return {
    token,
    profile,
    isLoggedIn,
    setToken,
    clearSession,
    login,
    register,
    fetchMe,
    logout,
  }
})
