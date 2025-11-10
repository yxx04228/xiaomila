// utils/request.ts
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

// 响应拦截器 - 统一处理成功和业务错误
const responseInterceptor = (response) => {
  const data = response.data

  // 检查业务逻辑是否成功
  if (data && data.success === false) {
    // Token 过期或无效处理
    if (
      data.message &&
      (data.message.includes('Token已过期') ||
        data.message.includes('Token过期') ||
        data.message.includes('Token格式错误') ||
        data.message.includes('Token签名验证失败') ||
        data.message.includes('Token验证失败') ||
        data.message.includes('无效的token') ||
        data.message.includes('token失效') ||
        data.message.includes('缺少访问令牌'))
    ) {
      console.log('检测到Token错误，直接处理')

      // 避免重复弹窗
      if (!response.config._retry) {
        response.config._retry = true

        // 清除用户信息
        const userStore = useUserStore()
        userStore.clearUserInfo()

        // 显示过期提示
        ElMessageBox.confirm('登录状态已过期，请重新登录', '系统提示', {
          confirmButtonText: '重新登录',
          cancelButtonText: '取消',
          type: 'warning',
        })
          .then(() => {
            // 跳转到登录页，带上重定向参数
            const currentPath = router.currentRoute.value.fullPath
            router.push(`/login?redirect=${encodeURIComponent(currentPath)}&tokenExpired=true`)
          })
          .catch(() => {
            // 用户取消，不做任何操作
          })
      }

      // 返回一个永远不会resolve的Promise，阻止后续then执行
      return new Promise(() => {})
    }

    // 其他业务错误，抛出异常让catch块处理
    const error = new Error(data.message || '请求失败')
    return Promise.reject(error)
  }

  // 成功响应，直接返回数据
  return data
}

// 响应拦截器 - 错误处理（只处理网络错误和HTTP错误）
const errorInterceptor = (error) => {
  console.error('请求失败:', error)

  // 处理响应错误
  if (error.response) {
    const { status, data } = error.response

    // HTTP 401 错误处理
    if (status === 401) {
      // 避免重复弹窗
      if (!error.config._retry) {
        error.config._retry = true

        // 清除用户信息
        const userStore = useUserStore()
        userStore.clearUserInfo()

        // 显示过期提示
        ElMessageBox.confirm('登录状态已过期，请重新登录', '系统提示', {
          confirmButtonText: '重新登录',
          cancelButtonText: '取消',
          type: 'warning',
        })
          .then(() => {
            // 跳转到登录页，带上重定向参数
            const currentPath = router.currentRoute.value.fullPath
            router.push(`/login?redirect=${encodeURIComponent(currentPath)}&tokenExpired=true`)
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
        ElMessage.error('请求失败')
    }
  }
  // 处理网络错误
  else if (error.code === 'ECONNABORTED' && error.message.includes('timeout')) {
    ElMessage.error('请求超时，请检查网络连接')
  } else if (error.request) {
    ElMessage.error('网络错误，请检查网络连接')
  } else {
    ElMessage.error(error.message || '请求配置错误')
  }

  return Promise.reject(error)
}

request.interceptors.response.use(responseInterceptor, errorInterceptor)
musicRequest.interceptors.response.use(responseInterceptor, errorInterceptor)

export { musicRequest }
export default request
