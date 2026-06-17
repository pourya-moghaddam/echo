import { api } from './api'

export interface AuthResponse {
  token: string;
  user: {
    id: string;
    email: string;
    username: string;
    avatarUrl?: string;
    themePreference?: 'DARK' | 'LIGHT' | 'SYSTEM';
  };
}

export const authService = {
  login: async (data: any) => {
    const response = await api.post<AuthResponse>('/auth/login', data)
    return response.data
  },
  signup: async (data: any) => {
    const response = await api.post<AuthResponse>('/auth/signup', data)
    return response.data
  },
  logout: async () => {
    const response = await api.post('/auth/logout')
    return response.data
  }
}
