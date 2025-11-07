import request from '@/utils/request'

import type { UserLoginParams, UserLoginResponse } from '@/types/user'

export const musicApi = {
  // 用户登录
  userLogin(params: UserLoginParams) {
    return request.post<UserLoginResponse>('/user/login', params)
  },

  // 用户注册
  userRegister(params: UserLoginParams) {
    return request.post<UserLoginResponse>('/user/register', params)
  },

  // 获取用户信息
  getUserInfo() {
    return request.get('/user/info')
  },

  // 用户退出登录
  userLogout() {
    return request.post('/user/logout')
  },
}
