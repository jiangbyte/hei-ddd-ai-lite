import axios, { type AxiosInstance } from 'axios'
import { ApiError } from './apiError'
import type { ApiResult } from '../types'

export interface HttpClientOptions {
  baseURL?: string
  getToken?: () => string | null
  onUnauthorized?: () => void
}

export function createHttpClient(options: HttpClientOptions = {}): AxiosInstance {
  const client = axios.create({
    baseURL: options.baseURL ?? '/api',
    timeout: 15000,
  })

  client.interceptors.request.use((config) => {
    const token = options.getToken?.()
    if (token) {
      config.headers.Authorization = `Bearer ${token}`
    }
    return config
  })

  client.interceptors.response.use(
    (response) => {
      const body = response.data as ApiResult
      if (body && typeof body === 'object' && 'code' in body) {
        if (body.code === '0') {
          return body.data
        }
        if (body.code === 'UNAUTHORIZED') {
          options.onUnauthorized?.()
        }
        throw new ApiError(body.code, body.message || '请求失败')
      }
      return response.data
    },
    (error) => {
      if (error.response?.status === 401) {
        options.onUnauthorized?.()
      }
      const message = error.response?.data?.message || error.message || '网络错误'
      throw new ApiError(error.response?.data?.code || 'NETWORK_ERROR', message)
    },
  )

  return client
}
