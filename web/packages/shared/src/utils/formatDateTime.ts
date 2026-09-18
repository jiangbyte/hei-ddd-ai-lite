/**
 * 将接口返回的时间统一格式化为 yyyy-MM-dd HH:mm:ss。
 * 兼容 ISO（含 T）与已格式化字符串。
 */
export function formatDateTime(value?: string | null): string {
  if (!value) {
    return '-'
  }
  const normalized = value.trim().replace('T', ' ').replace(/\.\d+.*$/, '')
  const match = normalized.match(/^(\d{4}-\d{2}-\d{2})[ ](\d{2}:\d{2}:\d{2})/)
  if (match) {
    return `${match[1]} ${match[2]}`
  }
  const date = new Date(value)
  if (Number.isNaN(date.getTime())) {
    return value
  }
  const pad = (n: number) => String(n).padStart(2, '0')
  return `${date.getFullYear()}-${pad(date.getMonth() + 1)}-${pad(date.getDate())} ${pad(date.getHours())}:${pad(date.getMinutes())}:${pad(date.getSeconds())}`
}
