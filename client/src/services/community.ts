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
};
