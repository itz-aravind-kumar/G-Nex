import { apiClient } from './apiClient'
import { AuthResponse, ApiResponse, User } from '@/types'

interface SignupRequest {
  username: string
  email: string
  password: string
  fullName: string
}

interface LoginRequest {
  usernameOrEmail: string
  password: string
}

export const authService = {
  async signup(data: SignupRequest): Promise<AuthResponse> {
    const response = await apiClient.post<ApiResponse<AuthResponse>>('/api/v1/auth/signup', data)
    return response.data
  },

  async login(data: LoginRequest): Promise<AuthResponse> {
    const response = await apiClient.post<ApiResponse<AuthResponse>>('/api/v1/auth/login', data)
    return response.data
  },

  async checkUsernameAvailability(username: string): Promise<boolean> {
    try {
      const response = await apiClient.get<ApiResponse<boolean>>(
        `/api/v1/auth/check-username?username=${username}`
      )
      return response.data
    } catch {
      return false
    }
  },

  async checkEmailAvailability(email: string): Promise<boolean> {
    try {
      const response = await apiClient.get<ApiResponse<boolean>>(`/api/v1/auth/check-email?email=${email}`)
      return response.data
    } catch {
      return false
    }
  },

  async generateToken(userId: string, username: string, email: string): Promise<AuthResponse> {
    const response = await apiClient.post<AuthResponse>(
      `/api/v1/auth/generate-token?userId=${userId}&username=${username}&email=${email}`
    )
    return response
  },

  async validateToken(token: string): Promise<boolean> {
    try {
      const response = await apiClient.post<{ valid: boolean }>('/api/v1/auth/validate-token', {
        token,
      })
      return response.valid
    } catch {
      return false
    }
  },

  saveAuth(authResponse: AuthResponse) {
    localStorage.setItem('auth_token', authResponse.token)
    localStorage.setItem(
      'user',
      JSON.stringify({
        userId: authResponse.userId,
        username: authResponse.username,
        email: authResponse.email,
        fullName: authResponse.fullName,
      })
    )
  },

  logout() {
    localStorage.removeItem('auth_token')
    localStorage.removeItem('user')
  },

  getToken(): string | null {
    return localStorage.getItem('auth_token')
  },

  getUser(): User | null {
    const userStr = localStorage.getItem('user')
    return userStr ? JSON.parse(userStr) : null
  },

  isAuthenticated(): boolean {
    return !!this.getToken()
  },
}
