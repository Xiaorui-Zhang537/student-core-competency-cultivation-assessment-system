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
    const response = await handleApiCall(
      () => communityApi.getPosts(params),
      uiStore,
      '获取帖子列表失败'
    );
    if (response) {
      const data = response.data as PaginatedResponse<Post>;
      posts.value = data.items;
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
      currentPost.value = response.data;
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
    return response?.data;
  };
  
  const toggleLikePost = async (id: number) => {
    const response = await handleApiCall(
      () => communityApi.likePost(id),
      uiStore,
      '操作失败'
    );
    if (response) {
        // Update post in the list
        const postInList = posts.value.find(p => p.id === id);
        if (postInList) {
            postInList.isLiked = response.data.liked;
            postInList.likeCount += response.data.liked ? 1 : -1;
        }
        // Update current post if it's the one
        if (currentPost.value && currentPost.value.id === id) {
            currentPost.value.isLiked = response.data.liked;
            currentPost.value.likeCount += response.data.liked ? 1 : -1;
        }
    }
  };

  const fetchComments = async (postId: number, params: { page?: number; size?: number }) => {
    const response = await handleApiCall(
      () => communityApi.getCommentsByPostId(postId, params),
      uiStore,
      '获取评论失败'
    );
    if (response) {
      const data = response.data as PaginatedResponse<PostComment>;
      comments.value = data.items;
      totalComments.value = data.total;
    }
  };

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

  const fetchCommunityStats = async () => {
      const response = await handleApiCall(() => communityApi.getCommunityStats(), uiStore, '获取社区统计数据失败');
      if(response) stats.value = response.data;
  };

  const fetchHotTopics = async () => {
    const response = await handleApiCall(() => communityApi.getHotTopics(), uiStore, '获取热门话题失败');
    if(response) hotTopics.value = response.data;
  };
  
  const fetchActiveUsers = async () => {
    const response = await handleApiCall(() => communityApi.getActiveUsers(), uiStore, '获取活跃用户失败');
    if(response) activeUsers.value = response.data;
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
    fetchComments,
    postComment,
    fetchCommunityStats,
    fetchHotTopics,
    fetchActiveUsers,
  };
});
