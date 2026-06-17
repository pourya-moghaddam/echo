import { create } from 'zustand'
import { persist } from 'zustand/middleware'

export interface User {
  id: string
  username: string
  email: string
  avatarUrl?: string
  themePreference?: 'LIGHT' | 'DARK' | 'SYSTEM'
}

interface AppState {
  currentUser: User | null
  token: string | null
  isAuthModalOpen: boolean
  authModalMode: 'login' | 'signup'
  
  // Actions
  setCurrentUser: (user: User | null) => void
  setToken: (token: string | null) => void
  setAuthModalOpen: (open: boolean, mode?: 'login' | 'signup') => void
  login: (user: User, token: string) => void
  logout: () => void
}

export const useStore = create<AppState>()(
  persist(
    (set) => ({
      currentUser: null,
      token: null,
      isAuthModalOpen: false,
      authModalMode: 'login',

      setCurrentUser: (user) => set({ currentUser: user }),
      setToken: (token) => set({ token }),
      setAuthModalOpen: (open, mode) => set((state) => ({ 
        isAuthModalOpen: open,
        authModalMode: mode || state.authModalMode 
      })),
      login: (user, token) => set({ currentUser: user, token, isAuthModalOpen: false }),
      logout: () => set({ currentUser: null, token: null }),
    }),
    {
      name: 'echo-auth-storage',
      partialize: (state) => ({ 
        currentUser: state.currentUser,
        token: state.token
      }),
    }
  )
)
