import { api } from './api';

export interface Community {
  id: number;
  name: string;
  description: string;
  category: string;
  createdAt: string;
  creatorUsername: string;
  memberCount: number;
}

export const communityService = {
  getAllCommunities: async (): Promise<Community[]> => {
    const res = await api.get('/communities');
    return res.data;
  },

  getCommunity: async (name: string): Promise<Community> => {
    const res = await api.get(`/communities/${name}`);
    return res.data;
  },

  joinCommunity: async (name: string): Promise<void> => {
    await api.post(`/communities/${name}/join`);
  },

  leaveCommunity: async (name: string): Promise<void> => {
    await api.post(`/communities/${name}/leave`);
  },

  getJoinedCommunities: async (): Promise<Community[]> => {
    const res = await api.get('/users/me/communities');
    return res.data;
  },

  createCommunity: async (data: { name: string; description: string; category: string }): Promise<Community> => {
    const res = await api.post('/communities', data);
    return res.data;
  },
};
