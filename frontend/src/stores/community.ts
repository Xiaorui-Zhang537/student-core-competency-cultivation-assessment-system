import { defineStore } from 'pinia';
import { ref, computed } from 'vue';
import { communityApi } from '@/api/community.api';
import { handleApiCall } from '@/utils/api-handler';
import { useUIStore } from './ui';
import type {
  Post,
  PostComment,
  CommunityStats,
  HotTopic,
  ActiveUser,
  PostCreationRequest,
} from '@/types/community';
import type { PaginatedResponse } from '@/types/api';

export const useCommunityStore = defineStore('community', () => {
  const uiStore = useUIStore();

  // State
  const posts = ref<Post[]>([]);
  const totalPosts = ref(0);
  const currentPost = ref<Post | null>(null);
  const comments = ref<PostComment[]>([]);
  const totalComments = ref(0);
  const stats = ref<CommunityStats | null>(null);
  const hotTopics = ref<HotTopic[]>([]);
  const activeUsers = ref<ActiveUser[]>([]);

  const loading = computed(() => uiStore.loading);

  // Actions
  const fetchPosts = async (params: {
    page?: number;
    size?: number;
    category?: string;
    keyword?: string;
    orderBy?: 'latest' | 'popular';
  }) => {
    // 兼容后端排序参数：将 popular 映射为 hot
    const orderBy = params?.orderBy === 'popular' ? 'hot' : params?.orderBy;
    const response = await handleApiCall(
      () => communityApi.getPosts({ ...params, orderBy }),
      uiStore,
      '获取帖子列表失败'
    );
    if (response) {
      const data = response as unknown as PaginatedResponse<any>;
      // 字段兼容映射
      posts.value = (data.items || []).map((p: any) => ({
        ...p,
        viewCount: p.viewCount ?? p.views ?? 0,
        likeCount: p.likeCount ?? p.likesCount ?? 0,
        commentCount: p.commentCount ?? p.commentsCount ?? 0,
        isLiked: p.isLiked ?? p.liked ?? false,
        author: p.author || (p.authorUsername || p.author_display_name ? { username: p.authorUsername || p.author_username, displayName: p.authorDisplayName || p.author_display_name, avatar: p.authorAvatar || p.author_avatar } : undefined),
      })) as Post[];
      totalPosts.value = data.total;
    }
  };

  const fetchPostById = async (id: number) => {
    const response = await handleApiCall(
      () => communityApi.getPostById(id),
      uiStore,
      '获取帖子详情失败'
    );
    if (response) {
      const r: any = response;
      currentPost.value = {
        ...(r as any),
        viewCount: r.viewCount ?? r.views ?? 0,
        likeCount: r.likeCount ?? r.likesCount ?? r.likes ?? 0,
        commentCount: r.commentCount ?? r.commentsCount ?? r.comment_count ?? 0,
        isLiked: r.isLiked ?? r.liked ?? false,
        author: r.author || (r.authorUsername || r.author_display_name
          ? { username: r.authorUsername || r.author_username, displayName: r.authorDisplayName || r.author_display_name, avatar: r.authorAvatar || r.author_avatar }
          : undefined),
      } as unknown as Post;
    }
  };

  const createPost = async (data: PostCreationRequest) => {
    const response = await handleApiCall(
      () => communityApi.createPost(data),
      uiStore,
      '发布帖子失败',
      { successMessage: '帖子发布成功' }
    );
    if (response) {
      await fetchPosts({}); // Refresh posts list
    }
    return (response as any)?.id ? response : { data: response };
  };
  
  const toggleLikePost = async (id: number) => {
    const response = await handleApiCall(
      () => communityApi.likePost(id),
      uiStore,
      '操作失败'
    );
    if (response) {
      const liked = (response as any).liked as boolean;
      // Update post in the list
      const postInList = posts.value.find(p => p.id === id);
      if (postInList) {
        postInList.isLiked = liked;
        const current = Number((postInList as any).likeCount) || 0;
        (postInList as any).likeCount = current + (liked ? 1 : -1);
      }
      // Update current post if it's the one
      if (currentPost.value && currentPost.value.id === id) {
        currentPost.value.isLiked = liked;
        const current = Number((currentPost.value as any).likeCount) || 0;
        (currentPost.value as any).likeCount = current + (liked ? 1 : -1);
      }
    }
  };

  const deletePost = async (postId: number) => {
    const response = await handleApiCall(
      () => communityApi.deletePost(postId),
      uiStore,
      '删除帖子失败',
      { successMessage: '删除成功' }
    );
    if (response) {
      posts.value = posts.value.filter(p => p.id !== postId);
      if (currentPost.value?.id === postId) currentPost.value = null;
    }
  }

  const updatePost = async (id: number, data: Partial<PostCreationRequest>) => {
    const response = await handleApiCall(
      () => communityApi.updatePost(id, data as any),
      uiStore,
      '编辑帖子失败',
      { successMessage: '保存成功' }
    );
    if (response) {
      // 列表中同步更新
      const idx = posts.value.findIndex(p => p.id === id)
      if (idx >= 0) {
        posts.value[idx] = { ...posts.value[idx], ...(response as any) }
      }
      if (currentPost.value?.id === id) {
        currentPost.value = { ...(currentPost.value as any), ...(response as any) } as Post
      }
    }
  }

  const fetchComments = async (postId: number, params: { page?: number; size?: number; orderBy?: 'time' | 'hot'; parentId?: number }) => {
    const response = await handleApiCall(
      () => communityApi.getCommentsByPostId(postId, params),
      uiStore,
      '获取评论失败'
    );
    if (response) {
      const data = response as unknown as PaginatedResponse<any>;
      const toBool = (v: any) => v === true || v === 1 || v === '1' || v === 'true';
      const toNum = (v: any) => {
        const n = Number(v);
        return Number.isFinite(n) ? Math.max(0, n) : 0;
      };
      // 规范字段：likesCount/liked -> likeCount/isLiked；作者映射
      comments.value = (data.items || []).map((c: any) => ({
        ...c,
        likeCount: toNum(c.likeCount ?? c.likesCount ?? 0),
        isLiked: toBool(c.isLiked ?? c.liked ?? false),
        author: c.author || (c.authorUsername || c.author_display_name
          ? { username: c.authorUsername || c.author_username, displayName: c.authorDisplayName || c.author_display_name, avatar: c.authorAvatar || c.author_avatar }
          : undefined),
      })) as PostComment[];
      totalComments.value = data.total;
    }
  };

  const toggleLikeComment = async (commentId: number) => {
    const response = await handleApiCall(
      () => communityApi.likeComment(commentId),
      uiStore,
      '操作失败'
    );
    if (response) {
      const liked = (response as any).liked as boolean;
      return liked
    }
    return undefined
  }

  const postComment = async (postId: number, content: string, parentId?: number) => {
    const response = await handleApiCall(
        () => communityApi.createComment(postId, content, parentId),
        uiStore,
        '发表评论失败',
        { successMessage: '评论成功' }
    );
    if (response) {
        await fetchComments(postId, {}); // Refresh comments
    }
  };

  const deleteComment = async (commentId: number, postId: number) => {
    const response = await handleApiCall(
      () => communityApi.deleteComment(commentId),
      uiStore,
      '删除评论失败',
      { successMessage: '删除成功' }
    );
    if (response) {
      await fetchComments(postId, {});
    }
  }

  const fetchCommunityStats = async () => {
      const response = await handleApiCall(() => communityApi.getCommunityStats(), uiStore, '获取社区统计数据失败');
      if(response) {
        const r: any = response;
        stats.value = {
          totalPosts: r.totalPosts ?? r.total_posts ?? 0,
          totalUsers: r.totalUsers ?? r.active_members ?? r.activeUsers ?? 0,
          totalComments: r.totalComments ?? r.total_comments ?? 0,
          activeUsersToday: r.activeUsersToday ?? r.today_active_users ?? r.active_users_today ?? r.today_posts ?? 0,
        } as CommunityStats;
      }
  };

  const fetchHotTopics = async () => {
    const response = await handleApiCall(() => communityApi.getHotTopics(), uiStore, '获取热门话题失败');
    if(response) {
      const arr = (response as any[]) || [];
      hotTopics.value = arr.map((t: any) => ({
        topic: t.topic ?? t.name ?? t.tag ?? '',
        postCount: t.postCount ?? t.count ?? 0,
      }));
    }
  };
  
  const fetchActiveUsers = async () => {
    const response = await handleApiCall(() => communityApi.getActiveUsers(), uiStore, '获取活跃用户失败');
    if(response) {
      const arr = (response as any[]) || [];
      activeUsers.value = arr.map((u: any) => ({
        userId: u.userId ?? u.id,
        username: u.username ?? u.name,
        avatarUrl: u.avatarUrl ?? u.avatar,
        postCount: u.postCount ?? u.post_count ?? 0,
      }));
    }
  };


  return {
    posts,
    totalPosts,
    currentPost,
    comments,
    totalComments,
    stats,
    hotTopics,
    activeUsers,
    loading,
    fetchPosts,
    fetchPostById,
    createPost,
    toggleLikePost,
    deletePost,
    updatePost,
    fetchComments,
    postComment,
    toggleLikeComment,
    deleteComment,
    fetchCommunityStats,
    fetchHotTopics,
    fetchActiveUsers,
  };
});
