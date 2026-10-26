import apiClient from './client'
import type { LoginPayload, RegisterPayload, TokenResponse, UserProfile } from '../types/auth'

export async function loginUser(payload: LoginPayload): Promise<TokenResponse> {
  const response = await apiClient.post<{ success: boolean; data: TokenResponse }>('/auth/login', payload)
  return response.data.data
}

export async function registerUser(payload: RegisterPayload): Promise<TokenResponse> {
  const response = await apiClient.post<{ success: boolean; data: TokenResponse }>('/auth/register', payload)
  return response.data.data
}

export async function refreshUserToken(refreshToken: string): Promise<TokenResponse> {
  const response = await apiClient.post<{ success: boolean; data: TokenResponse }>('/auth/refresh', { refreshToken })
  return response.data.data
}

export async function logoutUser(): Promise<void> {
  try {
    await apiClient.post('/auth/logout')
  } catch {
    // ignore network errors on logout
  }
}

export async function fetchCurrentUser(): Promise<UserProfile> {
  const response = await apiClient.get<{ success: boolean; data: UserProfile }>('/auth/me')
  return response.data.data
}
