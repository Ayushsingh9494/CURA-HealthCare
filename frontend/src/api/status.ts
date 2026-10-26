import apiClient from './client'

/**
 * Status API — calls the backend health/status endpoint.
 * Used by the frontend on startup to verify connectivity.
 */
export interface StatusResponse {
  status: string
  service: string
  version: string
  timestamp: string
  disclaimer: string
}

export async function fetchStatus(): Promise<StatusResponse> {
  const response = await apiClient.get<{ success: boolean; data: StatusResponse }>('/status')
  return response.data.data
}
