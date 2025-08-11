<template>
  <div class="p-6 bg-gray-50 dark:bg-gray-900 min-h-screen">
    <div v-if="loading && !currentPost" class="text-center">加载中...</div>
    <div v-if="!loading && !currentPost" class="text-center">帖子未找到。</div>
    
    <div v-if="currentPost" class="max-w-4xl mx-auto">
      <!-- Back Button -->
      <router-link :to="backToCommunity" class="inline-flex items-center text-sm text-gray-600 dark:text-gray-400 hover:text-primary-600 mb-4">
        <svg class="w-4 h-4 mr-2" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15 19l-7-7 7-7"></path></svg>
        返回社区
      </router-link>

      <!-- Post Header -->
      <div class="bg-white dark:bg-gray-800 rounded-lg shadow p-6 mb-6">
        <h1 class="text-3xl font-bold text-gray-900 dark:text-white mb-2">{{ currentPost.title }}</h1>
        <div class="flex items-center space-x-4 text-sm text-gray-500 dark:text-gray-400">
          <div class="flex items-center space-x-2">
            <user-icon class="w-5 h-5" />
            <span>{{ currentPost.author?.username || '匿名用户' }}</span>
          </div>
          <span>发布于 {{ formatDate(currentPost.createdAt) }}</span>
           <div class="flex items-center space-x-1"><eye-icon class="w-4 h-4" /><span>{{ currentPost.viewCount }}</span></div>
           <div class="flex items-center space-x-1"><chat-bubble-left-icon class="w-4 h-4" /><span>{{ currentPost.commentCount }}</span></div>
        </div>
         <div v-if="currentPost.tags?.length" class="flex items-center space-x-2 mt-4">
            <span v-for="tag in currentPost.tags" :key="tag.id" class="text-xs px-2 py-1 rounded-full bg-gray-100 dark:bg-gray-700 text-gray-600 dark:text-gray-300">#{{ tag.name }}</span>
        </div>
      </div>

      <!-- Post Content -->
      <div class="bg-white dark:bg-gray-800 rounded-lg shadow p-6 mb-6">
        <div class="prose dark:prose-invert max-w-none" v-html="currentPost.content"></div>
        <div v-if="attachments.length" class="mt-6 grid grid-cols-2 md:grid-cols-3 lg:grid-cols-4 gap-3">
          <div v-for="f in attachments" :key="f.id" class="border border-gray-200 dark:border-gray-700 rounded overflow-hidden">
            <img v-if="f.isImage || f.mimeType?.startsWith('image/')" :src="`${baseURL}/files/${f.id}/preview`" class="w-full h-32 object-cover" />
            <div class="p-2 text-xs flex items-center justify-between">
              <span class="truncate" :title="f.originalName || f.fileName">{{ f.originalName || f.fileName || ('附件#' + f.id) }}</span>
              <a class="text-primary-600" :href="`${baseURL}/files/${f.id}/download`">下载</a>
            </div>
          </div>
        </div>
         <div class="mt-6 pt-4 border-t border-gray-200 dark:border-gray-700 flex items-center space-x-4">
           <button @click="communityStore.toggleLikePost(currentPost.id)" :class="currentPost.isLiked ? 'text-red-500' : 'text-gray-600 dark:text-gray-400'" class="flex items-center space-x-2 btn btn-ghost">
            <hand-thumb-up-icon class="w-5 h-5" />
            <span>{{ currentPost.likeCount }} 点赞</span>
          </button>
            <button v-if="authStore.user?.id && String(authStore.user.id) === String(currentPost.author?.id || currentPost.authorId)" class="btn btn-ghost" @click="openEditCurrentPost">编辑帖子</button>
            <button v-if="authStore.user?.id && String(authStore.user.id) === String(currentPost.author?.id || currentPost.authorId)" class="btn btn-ghost text-red-600" @click="onDeleteCurrentPost">删除帖子</button>
        </div>
      </div>

      <!-- Comments Section -->
      <div class="bg-white dark:bg-gray-800 rounded-lg shadow p-6">
        <h2 class="text-xl font-bold text-gray-900 dark:text-white mb-4">{{ totalComments }} 条评论</h2>
        
        <!-- Post Comment Form -->
        <div class="mb-6">
            <textarea v-model="newComment" rows="3" placeholder="写下你的评论..." class="input w-full"></textarea>
            <div class="mt-2 flex items-center gap-2">
              <EmojiPicker @select="(e) => newComment = (newComment || '') + e" />
              <button @click="handlePostComment" :disabled="!newComment.trim() || loading" class="btn btn-primary">发表评论</button>
            </div>
        </div>

        <!-- Comments List -->
        <div class="space-y-4">
          <div class="flex items-center justify-between mb-2">
            <div class="text-xs text-gray-500">排序：</div>
            <div class="space-x-2">
              <button class="btn btn-ghost btn-sm" :class="commentOrderBy==='time' ? 'text-primary-600' : ''" @click="setOrder('time')">按时间</button>
              <button class="btn btn-ghost btn-sm" :class="commentOrderBy==='hot' ? 'text-primary-600' : ''" @click="setOrder('hot')">按热度</button>
            </div>
          </div>
          <CommentThread v-for="comment in localComments" :key="comment.id" :comment="comment" :post-id="currentPost.id" @deleted="onTopDeleted" />
          <!-- 评论分页 -->
          <div v-if="totalComments > commentsSize" class="mt-4 flex items-center justify-between">
            <span class="text-xs text-gray-500">共 {{ totalComments }} 条</span>
            <div class="flex items-center space-x-2">
              <span class="text-xs text-gray-500">第 {{ commentsPage }} 页</span>
              <button class="btn btn-ghost btn-sm" :disabled="commentsPage * commentsSize >= totalComments" @click="loadMoreComments">加载更多</button>
            </div>
          </div>
        </div>
      </div>

      <!-- Edit Current Post Modal -->
      <div v-if="editCurrent.visible" class="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50">
        <div class="bg-white dark:bg-gray-800 rounded-lg p-6 max-w-2xl w-full mx-4">
          <h3 class="text-lg font-medium text-gray-900 dark:text-white mb-4">编辑帖子</h3>
          <form @submit.prevent="handleUpdateCurrentPost" class="space-y-4">
            <div>
              <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1">帖子标题</label>
              <input v-model="editCurrent.form.title" type="text" class="input" required />
            </div>
            <div>
              <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1">分类</label>
              <select v-model="editCurrent.form.category" class="input">
                <option value="学习讨论">学习讨论</option>
                <option value="作业求助">作业求助</option>
                <option value="经验分享">经验分享</option>
                <option value="问答">问答</option>
                <option value="闲聊">闲聊</option>
              </select>
            </div>
            <div>
              <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1">帖子内容</label>
              <textarea v-model="editCurrent.form.content" rows="6" class="input" required></textarea>
            </div>
            <div class="flex justify-end space-x-3 pt-4">
              <button type="button" @click="editCurrent.visible = false" class="btn btn-outline">取消</button>
              <button type="submit" :disabled="loading" class="btn btn-primary">保存</button>
            </div>
          </form>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted, computed, reactive } from 'vue';
import { useRoute } from 'vue-router';
import { storeToRefs } from 'pinia';
import { useCommunityStore } from '@/stores/community';
import { useAuthStore } from '@/stores/auth';
import { UserIcon, EyeIcon, HandThumbUpIcon, ChatBubbleLeftIcon } from '@heroicons/vue/24/outline';
import CommentThread from '@/components/comments/CommentThread.vue'
import EmojiPicker from '@/components/ui/EmojiPicker.vue'
import { baseURL } from '@/api/config';
import { fileApi } from '@/api/file.api';

const route = useRoute();
const authStore = useAuthStore();
const communityStore = useCommunityStore();

const { currentPost, comments, totalComments, loading } = storeToRefs(communityStore);
const localComments = ref<any[]>([])

const newComment = ref('');
const attachments = ref<any[]>([]);
const commentsPage = ref(1);
const commentsSize = ref(20);
const commentOrderBy = ref<'time' | 'hot'>('time')
const backToCommunity = computed(() => authStore.userRole === 'TEACHER' ? '/teacher/community' : '/student/community');
// 改为递归组件内部管理回复，无需本地 replyBoxFor/commentReplies

const handlePostComment = async () => {
  if (!currentPost.value) return;
  await communityStore.postComment(currentPost.value.id, newComment.value);
  newComment.value = '';
  // 重新拉取第一页评论，保证新评论立即可见
  commentsPage.value = 1;
  await communityStore.fetchComments(currentPost.value.id, { page: commentsPage.value, size: commentsSize.value, orderBy: commentOrderBy.value });
  localComments.value = comments.value.slice();
};

const handleDeleteComment = async (commentId: number) => {
  if (!currentPost.value) return;
  if (!confirm('确认删除该评论？')) return;
  await communityStore.deleteComment(commentId, currentPost.value.id);
}

const onDeleteCurrentPost = async () => {
  if (!currentPost.value) return;
  if (!confirm('确认删除该帖子？')) return;
  await communityStore.deletePost(currentPost.value.id);
  // 返回社区
  window.history.back();
}

const editCurrent = reactive<{ visible: boolean; form: { title: string; category: string; content: string } }>({
  visible: false,
  form: { title: '', category: '学习讨论', content: '' }
})

const openEditCurrentPost = () => {
  if (!currentPost.value) return
  editCurrent.visible = true
  editCurrent.form.title = currentPost.value.title
  editCurrent.form.category = currentPost.value.category
  editCurrent.form.content = currentPost.value.content
}

const handleUpdateCurrentPost = async () => {
  if (!currentPost.value) return
  await communityStore.updatePost(currentPost.value.id, {
    title: editCurrent.form.title,
    category: editCurrent.form.category,
    content: editCurrent.form.content,
  })
  await communityStore.fetchPostById(currentPost.value.id)
  editCurrent.visible = false
}

const formatDate = (dateString: string) => {
  if (!dateString) return '';
  const date = new Date(dateString);
  return date.toLocaleString('zh-CN');
};

onMounted(async () => {
  const postId = Number(route.params.id);
  if (postId) {
    await communityStore.fetchPostById(postId);
    await communityStore.fetchComments(postId, { page: commentsPage.value, size: commentsSize.value, orderBy: commentOrderBy.value });
    localComments.value = comments.value.slice();
    const files: any = await fileApi.getRelatedFiles('community_post', postId);
    attachments.value = files?.data || files || [];
  }
});

onUnmounted(() => {
  currentPost.value = null;
  comments.value = [];
  attachments.value = [];
});

const changeCommentsPage = async (page: number) => {
  if (!currentPost.value) return;
  if (page < 1) return;
  commentsPage.value = page;
  await communityStore.fetchComments(currentPost.value.id, { page: commentsPage.value, size: commentsSize.value });
}

const loadMoreComments = async () => {
  if (!currentPost.value) return;
  if (commentsPage.value * commentsSize.value >= totalComments.value) return;
  commentsPage.value += 1;
  await communityStore.fetchComments(currentPost.value.id, { page: commentsPage.value, size: commentsSize.value, orderBy: commentOrderBy.value });
  // 追加到本地列表
  localComments.value = [...localComments.value, ...comments.value];
}

const setOrder = async (order: 'time' | 'hot') => {
  if (!currentPost.value) return
  if (commentOrderBy.value === order) return
  commentOrderBy.value = order
  commentsPage.value = 1
  await communityStore.fetchComments(currentPost.value.id, { page: commentsPage.value, size: commentsSize.value, orderBy: commentOrderBy.value })
  localComments.value = comments.value.slice()
}

const onTopDeleted = (id: number) => {
  // 从顶层移除已删除评论
  localComments.value = localComments.value.filter(c => c.id !== id)
}


</script>
