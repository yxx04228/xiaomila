import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { musicApi } from '@/api/user'
import type { UserLoginParams, UserLoginResponse } from '@/types/user'

export const useUserStore = defineStore('user', () => {
  // 状态
  const token = ref<string>('')
  const userInfo = ref<{
    id: string
    username: string
    nickname: string
  } | null>(null)
  const isLoggedIn = computed(() => !!token.value)

  // 从 localStorage 初始化状态
  const initFromStorage = () => {
    const savedToken = localStorage.getItem('music_token')
    const savedUser = localStorage.getItem('music_user')

    if (savedToken) {
      token.value = savedToken
    }

    if (savedUser) {
      try {
        userInfo.value = JSON.parse(savedUser)
      } catch (error) {
        console.error('解析用户信息失败:', error)
        localStorage.removeItem('music_user')
      }
    }
  }

  // 登录方法
  const userLogin = async (params: UserLoginParams): Promise<UserLoginResponse> => {
    try {
      // 调用 API
      const response = await musicApi.userLogin(params)

      if (response.success) {
        // 更新状态
        token.value = response.data.token
        userInfo.value = {
          id: response.data.id,
          username: response.data.username,
          nickname: response.data.nickname
        }

        // 保存到 localStorage
        localStorage.setItem('music_token', response.data.token)
        localStorage.setItem('music_user', JSON.stringify(userInfo.value))

        return response
      } else {
        throw new Error(response.message || '登录失败')
      }
    } catch (error: any) {
      // 清除可能的部分状态
      clearUserInfo()
      throw error
    }
  }

  // 退出登录
  const userLogout = () => {
    clearUserInfo()
    // 可以调用后端退出接口（如果需要）
    // await musicApi.userLogout()
  }

  // 清除用户信息
  const clearUserInfo = () => {
    token.value = ''
    userInfo.value = null
    localStorage.removeItem('music_token')
    localStorage.removeItem('music_user')
  }

  // 检查 token 是否有效（简单检查）
  const checkTokenValid = (): boolean => {
    if (!token.value) return false

    // 这里可以添加更复杂的 token 验证逻辑
    // 比如检查过期时间等
    return true
  }

  // 初始化
  initFromStorage()

  return {
    // 状态
    token,
    userInfo,
    isLoggedIn,

    // 方法
    userLogin,
    userLogout,
    clearUserInfo,
    checkTokenValid,
    initFromStorage
  }
})
