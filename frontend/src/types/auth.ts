export const ROLES = ['PATIENT', 'DOCTOR', 'ADMIN', 'RESPONDER'] as const
export type Role = (typeof ROLES)[number]

export interface UserProfile {
  id: number
  email: string
  role: Role
  isActive: boolean
  emailVerified: boolean
  firstName: string | null
  lastName: string | null
  phone: string | null
  dateOfBirth: string | null
  gender: string | null
  bloodGroup: string | null
  address: string | null
  profileImagePath: string | null
}

export interface TokenResponse {
  userId: number
  email: string
  role: Role
  accessToken: string
  refreshToken: string
}

export interface LoginPayload {
  email: string
  password: string
}

export interface RegisterPayload {
  email: string
  password: string
  role: Role
  firstName: string
  lastName: string
  phone?: string
}
