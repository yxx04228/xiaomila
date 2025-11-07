// utils/request.js
import axios from 'axios'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useUserStore } from '@/stores/user'
import router from '@/router'

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
const requestInterceptor = (config) => {
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

// 响应拦截器 - 成功处理
const responseInterceptor = (response) => {
  return response.data
}

// 响应拦截器 - 错误处理
const errorInterceptor = (error) => {
  console.error('请求失败:', error)

  const userStore = useUserStore()

  // 处理响应错误
  if (error.response) {
    const { status, data } = error.response

    // Token 过期或无效处理
    if (
      status === 401 ||
      (data &&
        data.message &&
        (data.message.includes('Token已过期') ||
          data.message.includes('Token过期') ||
          data.message.includes('Token格式错误') ||
          data.message.includes('Token签名验证失败') ||
          data.message.includes('Token验证失败')))
    ) {
      // 避免重复弹窗
      if (!error.config._retry) {
        error.config._retry = true

        // 清除用户信息
        userStore.clearUserInfo()

        // 显示过期提示
        ElMessageBox.confirm('登录状态已过期，请重新登录', '系统提示', {
          confirmButtonText: '重新登录',
          cancelButtonText: '取消',
          type: 'warning',
        })
          .then(() => {
            // 跳转到登录页
            router.push('/login')
          })
          .catch(() => {
            // 用户取消，不做任何操作
          })
      }
      return Promise.reject(error)
    }

    // 其他 HTTP 状态码处理
    switch (status) {
      case 403:
        ElMessage.error('拒绝访问')
        break
      case 404:
        ElMessage.error('请求地址不存在')
        break
      case 500:
        ElMessage.error('服务器内部错误')
        break
      default:
        if (data && data.message) {
          ElMessage.error(data.message)
        } else {
          ElMessage.error('请求失败')
        }
    }
  }
  // 处理网络错误
  else if (error.code === 'ECONNABORTED' && error.message.includes('timeout')) {
    ElMessage.error('请求超时，请检查网络连接')
  } else if (error.request) {
    ElMessage.error('网络错误，请检查网络连接')
  } else {
    ElMessage.error('请求配置错误')
  }

  return Promise.reject(error)
}

request.interceptors.response.use(responseInterceptor, errorInterceptor)
musicRequest.interceptors.response.use(responseInterceptor, errorInterceptor)

export { musicRequest }
export default request
