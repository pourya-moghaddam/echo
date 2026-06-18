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

  getCommunityPosts: async (communityName: string, page = 0, size = 10): Promise<PageResponse<Post>> => {
    const res = await api.get(`/communities/${communityName}/posts`, { params: { page, size } });
    return res.data;
  },

  votePost: async (postId: number, direction: 'UP' | 'DOWN' | 'NONE'): Promise<void> => {
    await api.post(`/posts/${postId}/vote`, { direction });
  },

  createPost: async (data: { title: string; content: string; communityName: string }): Promise<Post> => {
    const { communityName, ...requestData } = data;
    const res = await api.post(`/communities/${communityName}/posts`, requestData);
    return res.data;
  },

  getPost: async (id: number): Promise<Post> => {
    const res = await api.get(`/posts/${id}`);
    return res.data;
  },

  getComments: async (postId: number): Promise<any[]> => {
    const res = await api.get(`/posts/${postId}/comments`);
    return res.data;
  },

  createComment: async (postId: number, content: string): Promise<any> => {
    const res = await api.post(`/posts/${postId}/comments`, { content });
    return res.data;
  },

  replyToComment: async (commentId: number | string, content: string): Promise<any> => {
    const res = await api.post(`/comments/${commentId}/reply`, { content });
    return res.data;
  }
};
