import axios from 'axios'

const request = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL || '',
  timeout: 30000,
  headers: { 'Content-Type': 'application/json' },
})

// 请求拦截器
request.interceptors.request.use(
  config => {
    // 如后续引入登录态，可在此注入 token
    // const token = useAuthStore().token
    // if (token) config.headers.Authorization = `Bearer ${token}`
    return config
  },
  error => Promise.reject(error),
)

// 响应拦截器
request.interceptors.response.use(
  res => {
    const payload = res.data
    if (
      payload &&
      typeof payload === 'object' &&
      'code' in payload &&
      'data' in payload
    ) {
      return payload.data
    }
    return payload
  },
  error => {
    console.error('[Request Error]', error.response?.status, error.message)
    return Promise.reject(error)
  },
)

export default request
