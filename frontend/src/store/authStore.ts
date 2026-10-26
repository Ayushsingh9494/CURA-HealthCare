import { create } from 'zustand'
import { persist } from 'zustand/middleware'
import type { TokenResponse, UserProfile } from '../types/auth'

interface AuthState {
  user: UserProfile | null
  accessToken: string | null
  refreshToken: string | null
  isAuthenticated: boolean
  setAuth: (tokens: TokenResponse, profile?: UserProfile) => void
  setAccessToken: (token: string) => void
  setUser: (profile: UserProfile) => void
  logout: () => void
}

export const useAuthStore = create<AuthState>()(
  persist(
    (set) => ({
      user: null,
      accessToken: null,
      refreshToken: null,
      isAuthenticated: false,

      setAuth: (tokens: TokenResponse, profile?: UserProfile) => {
        set({
          accessToken: tokens.accessToken,
          refreshToken: tokens.refreshToken,
          isAuthenticated: true,
          user: profile || {
            id: tokens.userId,
            email: tokens.email,
            role: tokens.role,
            isActive: true,
            emailVerified: false,
            firstName: null,
            lastName: null,
            phone: null,
            dateOfBirth: null,
            gender: null,
            bloodGroup: null,
            address: null,
            profileImagePath: null,
          },
        })
      },

      setAccessToken: (token: string) => set({ accessToken: token }),

      setUser: (profile: UserProfile) => set({ user: profile }),

      logout: () => {
        set({
          user: null,
          accessToken: null,
          refreshToken: null,
          isAuthenticated: false,
        })
      },
    }),
    {
      name: 'cura_auth',
      partialize: (state) => ({
        refreshToken: state.refreshToken,
        user: state.user,
        isAuthenticated: state.isAuthenticated,
      }),
    }
  )
)
