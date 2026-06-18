import { api } from "./api"

export interface CommunitySearchResult {
  id: string
  name: string
  description: string
  category: string
}

export interface PostSearchResult {
  id: string
  title: string
  content: string
  communityName: string
  authorUsername: string
}

export const searchService = {
  searchCommunities: async (q: string): Promise<CommunitySearchResult[]> => {
    const res = await api.get('/search/communities', { params: { q } })
    return res.data
  },
  searchPosts: async (q: string): Promise<PostSearchResult[]> => {
    const res = await api.get('/search/posts', { params: { q } })
    return res.data
  }
}
