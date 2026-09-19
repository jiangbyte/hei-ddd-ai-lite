import type { AxiosInstance } from 'axios'
import type {
  AuthCredentials,
  AuthLoginResult,
  AuthRegisterResult,
  CreateUserPayload,
  ListUsersParams,
  PageResult,
  PublicUser,
  UserProfile,
  UserType,
} from '../types'

export function createAuthApi(http: AxiosInstance) {
  return {
    register(payload: AuthCredentials) {
      return http.post<any, AuthRegisterResult>('/auth/register', {
        username: payload.username,
        password: payload.password,
      })
    },
    login(payload: AuthCredentials) {
      return http.post<any, AuthLoginResult>('/auth/login', {
        username: payload.username,
        password: payload.password,
        clientType: payload.clientType ?? 'PORTAL',
      })
    },
    logout() {
      return http.post<any, void>('/auth/logout')
    },
    me() {
      return http.get<any, UserProfile>('/auth/me')
    },
    getPublicUser(userId: number | string) {
      return http.get<any, PublicUser>('/users/public', { params: { userId } })
    },
  }
}

export function createAdminUserApi(http: AxiosInstance) {
  return {
    list(params: ListUsersParams = {}) {
      return http.get<any, PageResult<UserProfile>>('/admin/users', { params })
    },
    create(payload: CreateUserPayload) {
      return http.post<any, { userId: number; username: string; userType: UserType }>('/admin/users', payload)
    },
    changeEnabled(userId: number, enabled: boolean) {
      return http.post<any, UserProfile>('/admin/users/change-enabled', { userId, enabled })
    },
  }
}

export type AuthApi = ReturnType<typeof createAuthApi>
export type AdminUserApi = ReturnType<typeof createAdminUserApi>
