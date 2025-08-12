import type { UserProfileResponse } from './user';

/**
 * 帖子实体
 */
export interface Post {
  id: number;
  title: string;
  content: string;
  authorId: number;
  author?: UserProfileResponse; // 作者信息，通常在后端组装
  category: string;
  tags: Tag[];
  viewCount: number;
  likeCount: number;
  commentCount: number;
  isLiked: boolean; // 当前用户是否点赞
  isPinned: boolean;
  status: 'PUBLISHED' | 'DRAFT' | 'ARCHIVED';
  createdAt: string;
  updatedAt: string;
}

/**
 * 帖子评论实体
 */
export interface PostComment {
  id: number;
  postId: number;
  content: string;
  authorId: number;
  author?: UserProfileResponse; // 评论者信息
  parentId?: number; // 用于回复另一条评论
  likeCount: number;
  isLiked: boolean; // 当前用户是否点赞
  createdAt: string;
  updatedAt:string;
  children?: PostComment[]; // 子评论
}

/**
 * 标签实体
 */
export interface Tag {
  id: number;
  name: string;
  description?: string;
  postCount?: number;
}

/**
 * 创建帖子的请求体
 */
export interface PostCreationRequest {
  title: string;
  content: string;
  category: string;
  tags?: string[]; // 发送标签名称数组
}

/**
 * 社区统计数据
 */
export interface CommunityStats {
  totalPosts: number;
  totalUsers: number;
  totalComments: number;
  activeUsersToday: number;
}

/**
 * 热门话题
 */
export interface HotTopic {
  topic: string; // 或 tag name
  postCount: number;
}

/**
 * 活跃用户
 */
export interface ActiveUser {
  userId: number;
  username: string;
  nickname?: string;
  avatarUrl?: string;
  postCount: number;
}
