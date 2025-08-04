<template>
  <div class="p-6 bg-gray-50 dark:bg-gray-900 min-h-screen">
    <div v-if="loading && !currentPost" class="text-center">加载中...</div>
    <div v-if="!loading && !currentPost" class="text-center">帖子未找到。</div>
    
    <div v-if="currentPost" class="max-w-4xl mx-auto">
      <!-- Back Button -->
      <router-link to="/community" class="inline-flex items-center text-sm text-gray-600 dark:text-gray-400 hover:text-primary-600 mb-4">
        <svg class="w-4 h-4 mr-2" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15 19l-7-7 7-7"></path></svg>
        返回社区
      </router-link>

      <!-- Post Header -->
      <div class="bg-white dark:bg-gray-800 rounded-lg shadow p-6 mb-6">
        <h1 class="text-3xl font-bold text-gray-900 dark:text-white mb-2">{{ currentPost.title }}</h1>
        <div class="flex items-center space-x-4 text-sm text-gray-500 dark:text-gray-400">
          <div class="flex items-center space-x-2">
            <UserIcon class="w-5 h-5" />
            <span>{{ currentPost.author?.username || '匿名用户' }}</span>
          </div>
          <span>发布于 {{ formatDate(currentPost.createdAt) }}</span>
           <div class="flex items-center space-x-1"><EyeIcon class="w-4 h-4" /><span>{{ currentPost.viewCount }}</span></div>
           <div class="flex items-center space-x-1"><ChatBubbleLeftIcon class="w-4 h-4" /><span>{{ currentPost.commentCount }}</span></div>
        </div>
         <div v-if="currentPost.tags?.length" class="flex items-center space-x-2 mt-4">
            <span v-for="tag in currentPost.tags" :key="tag.id" class="text-xs px-2 py-1 rounded-full bg-gray-100 dark:bg-gray-700 text-gray-600 dark:text-gray-300">#{{ tag.name }}</span>
        </div>
      </div>

      <!-- Post Content -->
      <div class="bg-white dark:bg-gray-800 rounded-lg shadow p-6 mb-6">
        <div class="prose dark:prose-invert max-w-none" v-html="currentPost.content"></div>
        <div class="mt-6 pt-4 border-t border-gray-200 dark:border-gray-700 flex items-center space-x-4">
           <button @click="communityStore.toggleLikePost(currentPost.id)" :class="currentPost.isLiked ? 'text-red-500' : 'text-gray-600 dark:text-gray-400'" class="flex items-center space-x-2 btn btn-ghost">
            <HandThumbUpIcon class="w-5 h-5" />
            <span>{{ currentPost.likeCount }} 点赞</span>
          </button>
        </div>
      </div>

      <!-- Comments Section -->
      <div class="bg-white dark:bg-gray-800 rounded-lg shadow p-6">
        <h2 class="text-xl font-bold text-gray-900 dark:text-white mb-4">{{ totalComments }} 条评论</h2>
        
        <!-- Post Comment Form -->
        <div class="mb-6">
            <textarea v-model="newComment" rows="3" placeholder="写下你的评论..." class="input w-full"></textarea>
            <button @click="handlePostComment" :disabled="!newComment.trim() || loading" class="btn btn-primary mt-2">发表评论</button>
        </div>

        <!-- Comments List -->
        <div class="space-y-4">
          <div v-for="comment in comments" :key="comment.id" class="flex items-start space-x-3">
            <div class="w-10 h-10 rounded-full bg-gray-200 dark:bg-gray-700 flex-shrink-0 flex items-center justify-center">
              <UserIcon class="w-5 h-5 text-gray-500"/>
            </div>
            <div class="flex-1">
              <div class="bg-gray-100 dark:bg-gray-700 rounded-lg p-3">
                <p class="text-sm font-semibold text-gray-900 dark:text-white">{{ comment.author?.username || '匿名用户' }}</p>
                <p class="text-sm text-gray-700 dark:text-gray-200 mt-1">{{ comment.content }}</p>
              </div>
              <div class="text-xs text-gray-500 mt-1 flex items-center space-x-3">
                <span>{{ formatDate(comment.createdAt) }}</span>
              </div>
            </div>
          </div>
           <!-- Pagination for comments can be added here if needed -->
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted } from 'vue';
import { useRoute } from 'vue-router';
import { storeToRefs } from 'pinia';
import { useCommunityStore } from '@/stores/community';
import { UserIcon, EyeIcon, HandThumbUpIcon, ChatBubbleLeftIcon } from '@heroicons/vue/24/outline';

const route = useRoute();
const communityStore = useCommunityStore();

const { currentPost, comments, totalComments, loading } = storeToRefs(communityStore);

const newComment = ref('');

const handlePostComment = async () => {
    if (!currentPost.value) return;
    await communityStore.postComment(currentPost.value.id, newComment.value);
    newComment.value = '';
};

const formatDate = (dateString: string) => {
  if (!dateString) return '';
  const date = new Date(dateString);
  return date.toLocaleString('zh-CN');
};

onMounted(async () => {
  const postId = Number(route.params.id);
  if (postId) {
    await communityStore.fetchPostById(postId);
    await communityStore.fetchComments(postId, { page: 1, size: 20 });
  }
});

onUnmounted(() => {
  communityStore.currentPost = null;
  communityStore.comments = [];
});
</script>
