/** 后端统一响应 R */
export interface ApiResult<T = unknown> {
  code: string
  message: string
  data: T
}

export type UserType = 'PORTAL' | 'ADMIN'
export type AppScope = 'portal' | 'admin'

export interface AuthCredentials {
  username: string
  password: string
  clientType?: UserType
}

export interface AuthLoginResult {
  token: string
  tokenType: string
  userId: number
  username: string
  userType: UserType
  expiresIn: number
}

export interface AuthRegisterResult {
  userId: number
  username: string
  userType: UserType
}

export interface UserProfile {
  userId: number
  username: string
  userType: UserType
  enabled: boolean
  createTime: string
  updateTime: string
}

export interface PublicUser {
  userId: number
  username: string
  userType: UserType
}

export interface PageResult<T> {
  total: number
  pageNo: number
  pageSize: number
  records: T[]
}

export interface CreateUserPayload {
  username: string
  password: string
  userType?: UserType
}

export interface ListUsersParams {
  pageNo?: number
  pageSize?: number
  username?: string
  userType?: UserType | ''
}
