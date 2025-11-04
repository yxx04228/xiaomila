import axios from 'axios'

// 创建axios实例
const request = axios.create({
  baseURL: '/api', // 通过代理转发到 http://192.168.0.142:8081
  timeout: 15000, // 增加超时时间
  headers: {
    'Content-Type': 'application/json',
  },
})

// 请求拦截器
request.interceptors.request.use(
  (config) => {
    // 添加token
    const token = localStorage.getItem('music_token')
    if (token) {
      config.headers['X-Access-Token'] = token
    }
    return config
  },
  (error) => {
    return Promise.reject(error)
  },
)

// 响应拦截器
request.interceptors.response.use(
  (response) => {
    // 直接返回后端数据
    return response.data
  },
  (error) => {
    console.error('请求失败:', error)
    if (error.response) {
      // 服务器返回错误状态码
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
  },
)

export default request
