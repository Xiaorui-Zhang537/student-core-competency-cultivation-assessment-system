import { api } from './config';
import type { ApiResponse, PaginatedResponse } from '@/types/api';
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
    orderBy?: 'latest' | 'popular';
  }): Promise<ApiResponse<PaginatedResponse<Post>>> => {
    return api.get('/community/posts', { params });
  },

  /**
   * 获取帖子详情
   */
  getPostById: (id: number): Promise<ApiResponse<Post>> => {
    return api.get(`/community/posts/${id}`);
  },

  /**
   * 创建新帖子
   */
  createPost: (data: PostCreationRequest): Promise<ApiResponse<Post>> => {
    return api.post('/community/posts', data);
  },

  /**
   * 点赞或取消点赞帖子
   */
  likePost: (id: number): Promise<ApiResponse<{ liked: boolean }>> => {
    return api.post(`/community/posts/${id}/like`);
  },

  /**
   * 获取帖子的评论列表
   */
  getCommentsByPostId: (
    postId: number,
    params: { page?: number; size?: number }
  ): Promise<ApiResponse<PaginatedResponse<PostComment>>> => {
    return api.get(`/community/posts/${postId}/comments`, { params });
  },

  /**
   * 发表评论
   */
  createComment: (
    postId: number,
    content: string,
    parentId?: number
  ): Promise<ApiResponse<PostComment>> => {
    return api.post(`/community/posts/${postId}/comments`, { content, parentId });
  },

  /**
   * 获取社区统计信息
   */
  getCommunityStats: (): Promise<ApiResponse<CommunityStats>> => {
    return api.get('/community/stats');
  },

  /**
   * 获取热门话题
   */
  getHotTopics: (limit = 10): Promise<ApiResponse<HotTopic[]>> => {
    return api.get('/community/hot-topics', { params: { limit } });
  },

  /**
   * 获取活跃用户
   */
  getActiveUsers: (limit = 10): Promise<ApiResponse<ActiveUser[]>> => {
    return api.get('/community/active-users', { params: { limit } });
  },

  /**
   * 获取我发布的帖子
   */
  getMyPosts: (params: {
    page?: number;
    size?: number;
  }): Promise<ApiResponse<PaginatedResponse<Post>>> => {
    return api.get('/community/my-posts', { params });
  },

  /**
   * 搜索标签
   */
  searchTags: (keyword: string, limit = 10): Promise<ApiResponse<Tag[]>> => {
    return api.get('/community/search/tags', { params: { keyword, limit } });
  },
};
