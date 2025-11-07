export interface User {
  id: string
  username: string
  nickname: string
  avatar?: string
  email?: string
}

export interface UserLoginParams {
  username: string
  password: string
}

export interface UserLoginResponse {
  code:number
  success: boolean
  message: string
  data: {
    id: string
    username: string
    nickname: string
    token: string
    expiresIn: number
  }
}
