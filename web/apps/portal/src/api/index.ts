import { createAuthApi, createHttpClient, createTokenStorage } from '@hei/shared'

const tokenStorage = createTokenStorage('portal')

let unauthorizedHandler: (() => void) | null = null

export function setUnauthorizedHandler(handler: () => void) {
  unauthorizedHandler = handler
}

export const http = createHttpClient({
  getToken: () => tokenStorage.get(),
  onUnauthorized: () => {
    tokenStorage.clear()
    unauthorizedHandler?.()
  },
})

export const authApi = createAuthApi(http)
export { tokenStorage }
