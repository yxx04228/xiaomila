import { defineStore } from 'pinia'
import { ref } from 'vue'

export interface UserInfo {
  id: string
  username: string
  nickname: string
  avatar?: string
}

export const useUserStore = defineStore('user', () => {
  const userInfo = ref<UserInfo | null>(null)
  const token = ref<string | null>(null)
  const isLoggedIn = ref(false)

  const login = (userData: UserInfo, userToken: string) => {
    userInfo.value = userData
    token.value = userToken
    isLoggedIn.value = true
    localStorage.setItem('music_token', userToken)
  }

  const logout = () => {
    userInfo.value = null
    token.value = null
    isLoggedIn.value = false
    localStorage.removeItem('music_token')
  }

  const checkLoginStatus = () => {
    const savedToken = localStorage.getItem('music_token')
    if (savedToken) {
      token.value = savedToken
      isLoggedIn.value = true
    }
  }

  return {
    userInfo,
    token,
    isLoggedIn,
    login,
    logout,
    checkLoginStatus,
  }
})
