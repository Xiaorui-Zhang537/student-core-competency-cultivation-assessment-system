<template>
  <div class="p-6 min-h-screen">
    <div v-if="loading && !currentPost" class="text-center">{{ t('shared.community.detail.loading') }}</div>
    <div v-if="!loading && !currentPost" class="text-center">{{ t('shared.community.detail.notFound') }}</div>
    
    <div v-if="currentPost" class="max-w-4xl mx-auto">
      <PageHeader :title="currentPost.title" :subtitle="t('shared.community.detail.postedAt', { datetime: formatDate(currentPost.createdAt) })" />
      <!-- Back Button -->
      <router-link :to="backToCommunity" class="inline-flex items-center text-sm text-gray-600 dark:text-gray-400 hover:text-primary-600 mb-4">
        <svg class="w-4 h-4 mr-2" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15 19l-7-7 7-7"></path></svg>
        {{ t('shared.community.detail.back') }}
      </router-link>

      <!-- Post Header -->
      <div class="glass-regular rounded-xl p-6 mb-6" v-glass="{ strength: 'regular', interactive: true }">
        <div class="flex items-center space-x-4 text-sm text-gray-500 dark:text-gray-400">
          <div class="flex items-center space-x-2">
            <user-avatar :avatar="currentPost.author?.avatar" :size="20">
              <user-icon class="w-5 h-5" />
            </user-avatar>
            <span>{{ currentPost.author?.nickname || currentPost.author?.username || t('shared.community.list.anonymous') }}</span>
          </div>
          <span>{{ t('shared.community.detail.postedAt', { datetime: formatDate(currentPost.createdAt) }) }}</span>
           <div class="flex items-center space-x-1"><eye-icon class="w-4 h-4" /><span>{{ currentPost.viewCount }}</span></div>
           <div class="flex items-center space-x-1"><chat-bubble-left-icon class="w-4 h-4" /><span>{{ currentPost.commentCount }}</span></div>
        </div>
         <div v-if="currentPost.tags?.length" class="flex items-center space-x-2 mt-4">
            <span v-for="tag in currentPost.tags" :key="tag.id" class="text-xs px-2 py-1 rounded-full bg-gray-100 dark:bg-gray-700 text-gray-600 dark:text-gray-300">#{{ tag.name }}</span>
        </div>
      </div>

      <!-- Post Content -->
      <div class="glass-regular rounded-xl p-6 mb-6" v-glass="{ strength: 'regular', interactive: true }">
        <div class="prose dark:prose-invert max-w-none" v-html="currentPost.content"></div>
        <div v-if="attachments.length" class="mt-6 grid grid-cols-2 md:grid-cols-3 lg:grid-cols-4 gap-3">
          <div v-for="f in attachments" :key="f.id" class="rounded overflow-hidden glass-ultraThin" v-glass="{ strength: 'ultraThin', interactive: false }">
            <img v-if="isImageAttachment(f)" :src="f._previewUrl || ''" class="w-full h-32 object-cover" @error="loadPreview(f)" />
            <div class="p-2 text-xs flex items-center justify-between">
              <span class="truncate" :title="f.originalName || f.fileName">{{ f.originalName || f.fileName || ('#' + f.id) }}</span>
              <button type="button" class="text-primary-600" @click="downloadAttachment(f)">{{ t('shared.community.detail.download') }}</button>
            </div>
          </div>
        </div>
         <div class="mt-6 pt-4 border-t border-gray-200 dark:border-gray-700 flex items-center space-x-4">
           <Button size="sm" variant="ghost" @click="communityStore.toggleLikePost(currentPost.id)" :class="currentPost.isLiked ? (isDark ? 'text-red-400' : 'text-red-500') : (isDark ? 'text-gray-300' : 'text-gray-600')" class="flex items-center space-x-2">
            <hand-thumb-up-icon class="w-5 h-5" />
            <span>{{ t('shared.community.detail.like', { count: currentPost.likeCount }) }}</span>
          </Button>
            <Button variant="primary" size="sm" @click="askAiForCurrentPost">
              <sparkles-icon class="w-4 h-4 mr-2" />{{ t('shared.community.detail.askAi') }}
            </Button>
            <Button v-if="authStore.user?.id && String(authStore.user.id) === String(currentPost.author?.id || currentPost.authorId)" size="sm" variant="ghost" @click="openEditCurrentPost">{{ t('shared.community.detail.edit') }}</Button>
            <Button v-if="authStore.user?.id && String(authStore.user.id) === String(currentPost.author?.id || currentPost.authorId)" size="sm" variant="danger" @click="onDeleteCurrentPost">{{ t('shared.community.detail.delete') }}</Button>
        </div>
      </div>

      <!-- Comments Section -->
      <div class="glass-regular rounded-xl p-6" v-glass="{ strength: 'regular', interactive: true }">
        <h2 class="text-xl font-bold text-gray-900 dark:text-white mb-4">{{ t('shared.community.detail.comments', { count: totalComments }) }}</h2>
        
        <!-- Post Comment Form -->
        <div class="mb-6">
            <textarea v-model="newComment" rows="3" :placeholder="t('shared.community.detail.writeComment')" class="input w-full"></textarea>
            <div class="mt-2 flex items-center gap-2">
              <emoji-picker size="sm" variant="ghost" @select="onEmojiSelect" />
              <Button size="sm" variant="primary" @click="handlePostComment" :disabled="!newComment.trim() || loading">{{ t('shared.community.detail.postComment') }}</Button>
            </div>
        </div>

        <!-- Comments List -->
        <div class="space-y-4">
            <div class="flex items-center justify-between mb-2">
            <div class="text-xs text-gray-500">{{ t('shared.community.detail.order') }}</div>
            <div class="space-x-2">
              <Button size="xs" variant="ghost" :class="commentOrderBy==='time' ? (isDark ? 'text-blue-300' : 'text-primary-600') : (isDark ? 'text-gray-400' : '')" @click="setOrder('time')">{{ t('shared.community.detail.orderTime') }}</Button>
              <Button size="xs" variant="ghost" :class="commentOrderBy==='hot' ? (isDark ? 'text-blue-300' : 'text-primary-600') : (isDark ? 'text-gray-400' : '')" @click="setOrder('hot')">{{ t('shared.community.detail.orderHot') }}</Button>
            </div>
          </div>
          <comment-thread v-for="comment in localComments" :key="comment.id" :comment="comment" :post-id="currentPost.id" @deleted="onTopDeleted" />
          <!-- 评论分页 -->
            <div v-if="totalComments > commentsSize" class="mt-4 flex items-center justify-between">
            <span class="text-xs text-gray-500">{{ t('shared.community.detail.total', { count: totalComments }) }}</span>
            <div class="flex items-center space-x-2">
              <span class="text-xs text-gray-500">{{ t('shared.community.detail.page', { page: commentsPage }) }}</span>
              <Button size="sm" variant="outline" :disabled="commentsPage * commentsSize >= totalComments" @click="loadMoreComments">{{ t('shared.community.detail.more') }}</Button>
            </div>
          </div>
        </div>
      </div>

      <!-- Edit Current Post Modal -->
      <div v-if="editCurrent.visible" class="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50 p-4">
        <div class="modal glass-thick p-6 max-w-2xl w-full mx-4 max-h-[80vh] overflow-y-auto" v-glass="{ strength: 'thick', interactive: true }">
          <h3 class="text-lg font-medium text-gray-900 dark:text-white mb-4">{{ t('shared.community.modal.editTitle') }}</h3>
          <form @submit.prevent="handleUpdateCurrentPost" class="space-y-4">
            <div>
              <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1">{{ t('shared.community.modal.title') }}</label>
              <input v-model="editCurrent.form.title" type="text" class="input" required />
            </div>
            <div>
              <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1">{{ t('shared.community.modal.category') }}</label>
              <select v-model="editCurrent.form.category" class="input">
                <option value="study">{{ t('shared.community.categories.study') }}</option>
                <option value="help">{{ t('shared.community.categories.help') }}</option>
                <option value="share">{{ t('shared.community.categories.share') }}</option>
                <option value="qa">{{ t('shared.community.categories.qa') }}</option>
                <option value="chat">{{ t('shared.community.categories.chat') }}</option>
              </select>
            </div>
            <div>
              <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1">{{ t('shared.community.modal.content') }}</label>
              <textarea v-model="editCurrent.form.content" rows="6" class="input" required></textarea>
            </div>
            <div>
              <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1">{{ t('shared.community.modal.attachment') }}</label>
              <FileUpload
                ref="detailEditUploader"
                :accept="'.pdf,.doc,.docx,.ppt,.pptx,.xls,.xlsx,.txt,.zip,.rar,image/*,video/*'"
                :multiple="true"
                :autoUpload="true"
                :upload-url="`${baseURL}/files/upload`"
                :upload-headers="uploadHeaders"
                :upload-data="detailEditUploadData"
                @upload-success="onDetailEditUploadSuccess"
                @upload-error="onDetailEditUploadError"
              />
              <div v-if="detailEditAttachments.length" class="mt-3">
                <h4 class="text-sm font-medium mb-2">{{ t('shared.community.modal.existingTitle') }}</h4>
                <ul class="divide-y divide-gray-200 dark:divide-gray-700">
                  <li v-for="f in detailEditAttachments" :key="f.id" class="py-2 flex items-center justify-between">
                    <div class="min-w-0 mr-3">
                      <div class="text-sm truncate">{{ f.originalName || f.fileName || ('#' + f.id) }}</div>
                      <div class="text-xs text-gray-500">{{ (f.fileSize ? (f.fileSize/1024/1024).toFixed(1)+' MB' : '') }}</div>
                    </div>
                    <div class="flex items-center gap-2">
                      <button type="button" class="btn btn-sm btn-outline" @click="downloadDetailEditAttachment(f)">{{ t('shared.community.modal.download') }}</button>
                      <button type="button" class="btn btn-sm btn-danger-outline" @click="deleteDetailEditAttachment(f.id)">{{ t('shared.community.modal.delete') }}</button>
                    </div>
                  </li>
                </ul>
              </div>
            </div>
            <div class="flex justify-end space-x-3 pt-4">
              <Button type="button" variant="secondary" @click="editCurrent.visible = false">{{ t('shared.community.modal.cancel') }}</Button>
              <Button type="submit" variant="indigo" :disabled="loading">{{ t('shared.community.modal.save') }}</Button>
            </div>
          </form>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted, computed, reactive } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { storeToRefs } from 'pinia';
import { useCommunityStore } from '@/stores/community';
import { useAuthStore } from '@/stores/auth';
import { UserIcon, EyeIcon, HandThumbUpIcon, ChatBubbleLeftIcon, SparklesIcon } from '@heroicons/vue/24/outline';
import Button from '@/components/ui/Button.vue'
import CommentThread from '@/components/comments/CommentThread.vue'
import EmojiPicker from '@/components/ui/EmojiPicker.vue'
import UserAvatar from '@/components/ui/UserAvatar.vue'
import { baseURL } from '@/api/config';
import { fileApi } from '@/api/file.api';
import FileUpload from '@/components/forms/FileUpload.vue';
import { useI18n } from 'vue-i18n'
import PageHeader from '@/components/ui/PageHeader.vue'

const route = useRoute();
const router = useRouter();
const authStore = useAuthStore();
const communityStore = useCommunityStore();
const { t } = useI18n()
const isDark = computed(() => document.documentElement.classList.contains('dark'))

// 分类映射：保持前端选项值使用稳定 id，提交与回显时与后端中文互转
const categoryIdToLabel: Record<string, string> = {
  study: '学习讨论',
  help: '作业求助',
  share: '经验分享',
  qa: '问答',
  chat: '闲聊',
}
const labelToCategoryId: Record<string, string> = Object.fromEntries(
  Object.entries(categoryIdToLabel).map(([k, v]) => [v, k])
) as Record<string, string>

const { currentPost, comments, totalComments, loading } = storeToRefs(communityStore);
const localComments = ref<any[]>([])

const newComment = ref('');
const attachments = ref<any[]>([]);
const uploadHeaders = {
  Authorization: localStorage.getItem('token') ? `Bearer ${localStorage.getItem('token')}` : ''
};
const detailEditUploader = ref();
const detailEditUploadData = reactive<{ purpose: string; relatedId?: string | number }>({ purpose: 'community_post' });
const detailEditAttachments = ref<any[]>([]);
const commentsPage = ref(1);
const commentsSize = ref(20);
const commentOrderBy = ref<'time' | 'hot'>('time')
const backToCommunity = computed(() => authStore.userRole === 'TEACHER' ? '/teacher/community' : '/student/community');
// 改为递归组件内部管理回复，无需本地 replyBoxFor/commentReplies

const handlePostComment = async () => {
  if (!currentPost.value) return;
  await communityStore.postComment(currentPost.value.id, newComment.value);
  const { useUIStore } = await import('@/stores/ui')
  const ui = useUIStore()
  ui.showNotification({ type: 'success', title: t('shared.community.notify.commentPostedTitle') as string, message: t('shared.community.notify.commentPostedMsg') as string })
  newComment.value = '';
  // 重新拉取第一页评论，保证新评论立即可见
  commentsPage.value = 1;
  await communityStore.fetchComments(currentPost.value.id, { page: commentsPage.value, size: commentsSize.value, orderBy: commentOrderBy.value });
  localComments.value = comments.value.slice();
};

const handleDeleteComment = async (commentId: number) => {
  if (!currentPost.value) return;
  if (!confirm(t('shared.community.confirm.deleteComment') as string)) return;
  await communityStore.deleteComment(commentId, currentPost.value.id);
  const { useUIStore } = await import('@/stores/ui')
  const ui = useUIStore()
  ui.showNotification({ type: 'success', title: t('shared.community.notify.commentDeletedTitle') as string, message: t('shared.community.notify.commentDeletedMsg') as string })
}

const onDeleteCurrentPost = async () => {
  if (!currentPost.value) return;
  if (!confirm(t('shared.community.confirm.deletePost') as string)) return;
  await communityStore.deletePost(currentPost.value.id);
  const { useUIStore } = await import('@/stores/ui')
  const ui = useUIStore()
  ui.showNotification({ type: 'success', title: t('shared.community.notify.postDeletedTitle') as string, message: t('shared.community.notify.postDeletedMsg') as string })
  // 返回社区
  window.history.back();
}

const editCurrent = reactive<{ visible: boolean; form: { title: string; category: string; content: string } }>({
  visible: false,
  form: { title: '', category: 'study', content: '' }
})

const openEditCurrentPost = () => {
  if (!currentPost.value) return
  editCurrent.visible = true
  editCurrent.form.title = currentPost.value.title
  // 将后端中文分类转换为前端英文 id（保持选项值稳定）
  editCurrent.form.category = labelToCategoryId[currentPost.value.category] || currentPost.value.category
  editCurrent.form.content = currentPost.value.content
  detailEditUploadData.relatedId = currentPost.value.id
  refreshDetailEditAttachments()
}

const handleUpdateCurrentPost = async () => {
  if (!currentPost.value) return
  await communityStore.updatePost(currentPost.value.id, {
    title: editCurrent.form.title,
    // 提交给后端中文分类（由稳定 id -> 中文标签）
    category: categoryIdToLabel[editCurrent.form.category] || editCurrent.form.category,
    content: editCurrent.form.content,
  })
  await communityStore.fetchPostById(currentPost.value.id)
  await refreshMainAttachments()
  editCurrent.visible = false
}

const formatDate = (dateString: string) => {
  if (!dateString) return '';
  const date = new Date(dateString);
  return date.toLocaleString();
};

onMounted(async () => {
  const postId = Number(route.params.id);
  if (postId) {
    await communityStore.fetchPostById(postId);
    await communityStore.fetchComments(postId, { page: commentsPage.value, size: commentsSize.value, orderBy: commentOrderBy.value });
    localComments.value = comments.value.slice();
    await refreshMainAttachments();
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

const askAiForCurrentPost = () => {
  if (!currentPost.value) return
  const content = (currentPost.value.title ? `【问题标题】${currentPost.value.title}\n` : '') +
                  (currentPost.value.content ? `【问题内容】${currentPost.value.content}` : '')
  // 角色统一使用教师AI页面；如需学生也支持，可按角色跳不同路由
  const target = authStore.userRole === 'TEACHER' ? { path: '/teacher/assistant' } : { path: '/student/assistant' }
  router.push({ ...target, query: { q: content } })
}

const refreshDetailEditAttachments = async () => {
  if (!currentPost.value) return
  const res: any = await fileApi.getRelatedFiles('community_post', currentPost.value.id);
  detailEditAttachments.value = res?.data || res || [];
};

const onDetailEditUploadSuccess = async () => {
  await refreshDetailEditAttachments();
  const { useUIStore } = await import('@/stores/ui')
  const ui = useUIStore()
  ui.showNotification({ type: 'success', title: t('shared.community.notify.attachmentUploadedTitle') as string, message: t('shared.community.notify.attachmentUploadedMsg') as string })
};

const onDetailEditUploadError = (msg: string) => {
  console.error('帖子详情附件上传失败:', msg);
};

const deleteDetailEditAttachment = async (fileId: number | string) => {
  await fileApi.deleteFile(String(fileId));
  await refreshDetailEditAttachments();
  const { useUIStore } = await import('@/stores/ui')
  const ui = useUIStore()
  ui.showNotification({ type: 'success', title: t('shared.community.notify.attachmentDeletedTitle') as string, message: t('shared.community.notify.attachmentDeletedMsg') as string })
};

const downloadDetailEditAttachment = async (f: any) => {
  await fileApi.downloadFile(f.id, f.originalName || f.fileName || `attachment_${f.id}`);
};


// 正文区域附件下载（带鉴权）
const downloadAttachment = async (f: any) => {
  await fileApi.downloadFile(f.id, f.originalName || f.fileName || `attachment_${f.id}`);
};

// 工具：是否为图片
const isImageAttachment = (f: any) => {
  const mime = (f.mimeType || f.contentType || '').toLowerCase();
  const name = String(f.originalName || f.fileName || '').toLowerCase();
  const ext = name.split('.').pop() || '';
  return mime.startsWith('image/') || ['png','jpg','jpeg','gif','webp','bmp'].includes(ext);
};

// 加载/刷新正文附件与预览
const refreshMainAttachments = async () => {
  if (!currentPost.value) return;
  const files: any = await fileApi.getRelatedFiles('community_post', currentPost.value.id);
  const list = (files?.data || files || []) as any[];
  attachments.value = list;
  // 为图片准备预览 URL（鉴权 blob）
  for (const f of attachments.value) {
    if (isImageAttachment(f)) {
      await loadPreview(f);
    }
  }
};

// 选择表情回调（避免模板中隐式 any）
const onEmojiSelect = (emoji: string) => {
  newComment.value = (newComment.value || '') + emoji
}

const loadPreview = async (f: any) => {
  try {
    const blob = await fileApi.getPreview(f.id);
    f._previewUrl = URL.createObjectURL(blob);
  } catch (_) {
    f._previewUrl = '';
  }
};


</script>
