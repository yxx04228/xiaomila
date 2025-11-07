import axios from 'axios'

// 创建axios实例
const request = axios.create({
  baseURL: '/api',
  timeout: 60000,
  headers: {
    'Content-Type': 'application/json',
  },
})

// 创建专门用于音乐播放的实例（不设置默认超时）
const musicRequest = axios.create({
  baseURL: '/api',
  // 不设置默认超时，在具体请求中设置
  headers: {
    'Content-Type': 'application/json',
  },
})

// 请求拦截器 - 通用
const requestInterceptor = (config: any) => {
  // 添加token
  const token = localStorage.getItem('music_token')
  if (token) {
    config.headers['X-Access-Token'] = token
  }

  // 对音乐播放和下载接口设置超长超时
  if (
    config.url?.includes('/music/play') ||
    config.url?.includes('/music/download') ||
    config.url?.includes('/upload')
  ) {
    config.timeout = 300000 // 5分钟
  }

  return config
}

// 为两个实例都添加拦截器
request.interceptors.request.use(requestInterceptor)
musicRequest.interceptors.request.use(requestInterceptor)

// 响应拦截器 - 通用
const responseInterceptor = (response: any) => {
  return response.data
}

const errorInterceptor = (error: any) => {
  console.error('请求失败:', error)

  // 超时错误特殊处理
  if (error.code === 'ECONNABORTED' && error.message.includes('timeout')) {
    console.error('请求超时，请检查网络连接')
    // 可以在这里添加重试逻辑
  }

  if (error.response) {
    switch (error.response.status) {
      case 401:
        console.error('未授权，请重新登录')
        break
      case 403:
        console.error('拒绝访问')
        break
      case 404:
        console.error('请求地址不存在')
        break
      case 500:
        console.error('服务器内部错误')
        break
      default:
        console.error('请求失败:', error.response.status)
    }
  } else if (error.request) {
    console.error('网络错误，请检查网络连接')
  } else {
    console.error('请求配置错误:', error.message)
  }

  return Promise.reject(error)
}

request.interceptors.response.use(responseInterceptor, errorInterceptor)
musicRequest.interceptors.response.use(responseInterceptor, errorInterceptor)

export { musicRequest }
export default request
