import { api } from './config';
import type { PaginatedResponse } from '@/types/api';
import type {
  Post,
  PostComment,
  Tag,
  PostCreationRequest,
  CommunityStats,
  HotTopic,
  ActiveUser,
} from '@/types/community';

export const communityApi = {
  /**
   * 获取帖子列表
   */
  getPosts: (params: {
    page?: number;
    size?: number;
    category?: string;
    keyword?: string;
    orderBy?: 'latest' | 'popular' | 'hot' | 'comments' | 'likes' | 'views';
  }): Promise<PaginatedResponse<Post>> => {
    return api.get('/community/posts', { params });
  },

  /**
   * 获取帖子详情
   */
  getPostById: (id: number): Promise<Post> => {
    return api.get(`/community/posts/${id}`);
  },

  /**
   * 创建新帖子
   */
  createPost: (data: PostCreationRequest): Promise<Post> => {
    return api.post('/community/posts', data);
  },

  /**
   * 点赞或取消点赞帖子
   */
  likePost: (id: number): Promise<{ liked: boolean }> => {
    return api.post(`/community/posts/${id}/like`).then((r: any) => ({ liked: (r?.liked ?? r?.data?.liked) as boolean }));
  },

  /**
   * 获取帖子的评论列表
   */
  getCommentsByPostId: (
    postId: number,
    params: { page?: number; size?: number; parentId?: number; orderBy?: 'time' | 'hot' }
  ): Promise<PaginatedResponse<PostComment>> => {
    return api.get(`/community/posts/${postId}/comments`, { params });
  },
  likeComment: (commentId: number): Promise<{ liked: boolean }> => {
    return api.post(`/community/comments/${commentId}/like`).then((r: any) => ({ liked: (r?.liked ?? r?.data?.liked) as boolean }));
  },

  deleteComment: (commentId: number) => {
    return api.delete(`/community/comments/${commentId}`);
  },

  /**
   * 发表评论
   */
  createComment: (
    postId: number,
    content: string,
    parentId?: number
  ): Promise<PostComment> => {
    return api.post(`/community/posts/${postId}/comments`, { content, parentId });
  },

  /**
   * 获取社区统计信息
   */
  getCommunityStats: (): Promise<CommunityStats> => {
    return api.get('/community/stats');
  },

  /**
   * 获取热门话题
   */
  getHotTopics: (limit = 10): Promise<HotTopic[]> => {
    return api.get('/community/hot-topics', { params: { limit } });
  },

  /**
   * 获取活跃用户
   */
  getActiveUsers: (limit = 10): Promise<ActiveUser[]> => {
    return api.get('/community/active-users', { params: { limit } });
  },

  /**
   * 获取我发布的帖子
   */
  getMyPosts: (params: {
    page?: number;
    size?: number;
  }): Promise<PaginatedResponse<Post>> => {
    return api.get('/community/my-posts', { params });
  },

  /**
   * 搜索标签
   */
  searchTags: (keyword: string, limit = 10): Promise<Tag[]> => {
    return api.get('/community/search/tags', { params: { keyword, limit } });
  },

  /** 删除帖子（仅作者） */
  deletePost: (postId: number) => {
    return api.delete(`/community/posts/${postId}`);
  },

  /** 编辑帖子（仅作者） */
  updatePost: (
    id: number,
    data: Partial<PostCreationRequest> & {
      title?: string;
      content?: string;
      category?: string;
      tags?: string[];
    }
  ): Promise<Post> => {
    return api.put(`/community/posts/${id}`, data);
  },
};
