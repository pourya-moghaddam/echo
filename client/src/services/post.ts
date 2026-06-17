import { api } from './api';

export interface Post {
  id: number;
  title: string;
  content: string;
  authorUsername: string;
  communityName: string;
  score: number;
  createdAt: string;
}

export interface PageResponse<T> {
  content: T[];
  totalPages: number;
  totalElements: number;
  size: number;
  number: number;
  last: boolean;
}

export const postService = {
  getFeed: async (page = 0, size = 10): Promise<PageResponse<Post>> => {
    const res = await api.get('/feed', { params: { page, size } });
    return res.data;
  },
  
  getPopular: async (page = 0, size = 10): Promise<PageResponse<Post>> => {
    const res = await api.get('/posts/popular', { params: { page, size } });
    return res.data;
  },

  votePost: async (postId: number, direction: 'UP' | 'DOWN' | 'NONE'): Promise<void> => {
    await api.post(`/posts/${postId}/vote`, { direction });
  }
};
