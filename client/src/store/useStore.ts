import { create } from 'zustand'
import { persist } from 'zustand/middleware'
import type { PostProps } from '@/components/shared/PostCard'

interface User {
  id: string
  email: string
  username: string
  avatarUrl?: string
  themePreference?: 'DARK' | 'LIGHT' | 'SYSTEM'
}

interface AppState {
  currentUser: User | null
  token: string | null
  isAuthModalOpen: boolean
  authModalMode: 'login' | 'signup'
  setAuthModalOpen: (isOpen: boolean, mode?: 'login' | 'signup') => void
  login: (user: User, token: string) => void
  logout: () => void

  posts: PostProps[]
  upvotePost: (id: string) => void
  downvotePost: (id: string) => void
}

const INITIAL_POSTS: PostProps[] = [
  {
    id: "1",
    community: "reactjs",
    author: "pourya",
    timeAgo: "2 hours ago",
    title: "Just finished Phase 3 of the frontend development for Echo! What do you think?",
    contentSnippet: "We implemented the PostCard, CommentCard, and VoteWidget components. The design is heavily inspired by modern minimalism, using Shadcn UI and Tailwind CSS.",
    score: 1245,
    commentCount: 42,
    tags: ["Showoff", "Discussion"],
    userVote: null
  },
  {
    id: "2",
    community: "webdev",
    author: "alex",
    timeAgo: "5 hours ago",
    title: "Zustand vs Redux Toolkit in 2026? What is everyone using?",
    contentSnippet: "I've been using Redux for years but recently tried Zustand and it feels so much lighter. Are there any edge cases where Redux is still mandatory?",
    score: 834,
    commentCount: 156,
    tags: ["Question"],
    userVote: 'up'
  },
  {
    id: "3",
    community: "design",
    author: "sarah_ui",
    timeAgo: "12 hours ago",
    title: "A deep dive into OKLCH color spaces for web development.",
    contentSnippet: "OKLCH is the new standard. Here's why you should completely ditch HSL and RGB in your next modern web application...",
    score: 3402,
    commentCount: 89,
    tags: ["Article", "Design"],
    userVote: null
  }
]

export const useStore = create<AppState>()(
  persist(
    (set) => ({
      currentUser: null,
      token: null,
      isAuthModalOpen: false,
      authModalMode: 'login',
      setAuthModalOpen: (isOpen, mode) => set((state) => ({ 
        isAuthModalOpen: isOpen, 
        authModalMode: mode || state.authModalMode 
      })),
      login: (user, token) => set({ currentUser: user, token, isAuthModalOpen: false }),
      logout: () => set({ currentUser: null, token: null }),
      
      posts: INITIAL_POSTS,
      
      upvotePost: (id) => set((state) => ({
        posts: state.posts.map(post => {
          if (post.id === id) {
            if (post.userVote === 'up') return { ...post, userVote: null, score: post.score - 1 }
            const scoreDiff = post.userVote === 'down' ? 2 : 1
            return { ...post, userVote: 'up', score: post.score + scoreDiff }
          }
          return post
        })
      })),

      downvotePost: (id) => set((state) => ({
        posts: state.posts.map(post => {
          if (post.id === id) {
            if (post.userVote === 'down') return { ...post, userVote: null, score: post.score + 1 }
            const scoreDiff = post.userVote === 'up' ? -2 : -1
            return { ...post, userVote: 'down', score: post.score + scoreDiff }
          }
          return post
        })
      }))
    }),
    {
      name: 'echo-auth-storage',
      partialize: (state) => ({ currentUser: state.currentUser, token: state.token }),
    }
  )
)
