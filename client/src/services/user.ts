import { api } from './api'

export const userService = {
  updateTheme: async (theme: 'DARK' | 'LIGHT' | 'SYSTEM') => {
    const response = await api.put('/users/theme', { themePreference: theme })
    return response.data
  },
  updateAvatar: async (avatarUrl: string) => {
    const response = await api.put('/users/avatar', { avatarUrl })
    return response.data
  },
  getUserProfile: async (username: string) => {
    const response = await api.get(`/users/${username}`)
    return response.data
  }
}
