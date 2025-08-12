<template>
  <div class="p-6">
    <div class="max-w-4xl mx-auto">
      <div class="mb-8">
        <h1 class="text-3xl font-bold">个人资料</h1>
        <p class="text-gray-500">管理您的个人信息和账户设置</p>
      </div>

      <div v-if="authStore.loading && !userProfile" class="text-center py-12">
        <p>加载用户信息中...</p>
      </div>

      <div v-else-if="userProfile" class="space-y-8">
        <!-- Profile Info -->
        <div class="card p-6">
          <h2 class="text-lg font-semibold mb-4">个人信息</h2>
          <div class="flex items-start space-x-6">
            <!-- 头像展示 -->
            <div class="w-24 flex-shrink-0">
              <UserAvatar :avatar="(userProfile as any)?.avatar" :size="96">
                <img :src="defaultAvatars[0]" alt="默认头像" class="w-24 h-24 rounded-full object-cover" />
              </UserAvatar>
            </div>
            <div class="flex-1">
              <div class="grid grid-cols-1 md:grid-cols-2 gap-4">
                <div>
                  <label class="block text-sm font-medium mb-1">用户名</label>
                  <p class="font-medium">{{ userProfile.nickname || userProfile.username }}</p>
                </div>
                <div>
                  <label class="block text-sm font-medium mb-1">邮箱</label>
                  <p>{{ userProfile.email }}</p>
                </div>
                <div>
                  <label class="block text-sm font-medium mb-1">角色</label>
                  <p>{{ userProfile.role }}</p>
                </div>
                <div>
                  <label class="block text-sm font-medium mb-1">昵称</label>
                  <p>{{ userProfile.nickname || '未设置' }}</p>
                </div>
                 <div>
                  <label class="block text-sm font-medium mb-1">性别</label>
                  <p>{{ userProfile.gender || '未设置' }}</p>
                </div>
                <div>
                  <label class="block text-sm font-medium mb-1">名字</label>
                  <p>{{ userProfile.firstName || '未设置' }}</p>
                </div>
                <div>
                  <label class="block text-sm font-medium mb-1">姓氏</label>
                  <p>{{ userProfile.lastName || '未设置' }}</p>
                </div>
                <div>
                  <label class="block text-sm font-medium mb-1">学校</label>
                  <p>{{ userProfile.school || '未设置' }}</p>
                </div>
                <div>
                  <label class="block text-sm font-medium mb-1">专业/科目</label>
                  <p>{{ userProfile.subject || '未设置' }}</p>
                </div>
                <div v-if="authStore.userRole === 'STUDENT'">
                  <label class="block text-sm font-medium mb-1">学号</label>
                  <p>{{ userProfile.studentNo || '未设置' }}</p>
                </div>
                <div v-if="authStore.userRole === 'TEACHER'">
                  <label class="block text-sm font-medium mb-1">工号</label>
                  <p>{{ userProfile.teacherNo || '未设置' }}</p>
                </div>
                 <div>
                  <label class="block text-sm font-medium mb-1">简介</label>
                  <p>{{ userProfile.bio || '未设置' }}</p>
                </div>
                 <div>
                  <label class="block text-sm font-medium mb-1">生日</label>
                  <p>{{ userProfile.birthday || '未设置' }}</p>
                </div>
                <div>
                  <label class="block text-sm font-medium mb-1">手机号</label>
                  <p>{{ userProfile.phone || '未设置' }}</p>
                </div>
                <div>
                  <label class="block text-sm font-medium mb-1">国家</label>
                  <p>{{ userProfile.country || '未设置' }}</p>
                </div>
                <div>
                  <label class="block text-sm font-medium mb-1">省份/州</label>
                  <p>{{ userProfile.province || '未设置' }}</p>
                </div>
                <div>
                  <label class="block text-sm font-medium mb-1">城市</label>
                  <p>{{ userProfile.city || '未设置' }}</p>
                </div>
              </div>
              <button @click="openEditProfile" class="btn btn-outline mt-4">编辑资料</button>
            </div>
          </div>
        </div>

        <!-- Edit Profile Form -->
        <div v-if="showEditProfile" class="card p-6">
          <h2 class="text-lg font-semibold mb-4">编辑个人信息</h2>
          <form @submit.prevent="handleUpdateProfile" class="space-y-6">
              <!-- 头像上传 -->
              <div>
                <label class="block text-sm font-medium mb-2">头像</label>
                <FileUpload
                  :accept="'image/*'"
                  :multiple="false"
                  :autoUpload="true"
                  :upload-url="`${baseURL}/files/upload`"
                  :upload-headers="uploadHeaders"
                  :upload-data="{ purpose: 'avatar' }"
                  @upload-success="onAvatarUploaded"
                  @upload-error="onAvatarUploadError"
                />
                <p v-if="profileForm.avatar" class="text-xs text-gray-500 mt-2">已选择头像（文件ID：{{ profileForm.avatar }}）</p>
                <!-- 默认头像选择 -->
                <div class="mt-4">
                  <div class="flex items-center justify-between mb-2">
                    <span class="text-sm text-gray-600 dark:text-gray-400">或选择一个默认头像</span>
                  </div>
                  <div class="grid grid-cols-5 gap-2">
                    <button
                      v-for="(url, idx) in defaultAvatars"
                      :key="idx"
                      type="button"
                      class="h-12 w-12 rounded-full overflow-hidden border transition-colors"
                      :class="profileForm.avatar === url ? 'border-primary-500' : 'border-gray-300 dark:border-gray-600'"
                      @click="selectDefaultAvatar(url)"
                      :title="'默认头像 ' + (idx+1)"
                    >
                      <img :src="url" alt="默认头像" class="w-full h-full object-cover" />
                    </button>
                  </div>
                </div>
              </div>
              <div class="grid grid-cols-1 md:grid-cols-2 gap-6">
              <div>
                <label for="nickname" class="block text-sm font-medium mb-2">昵称</label>
                <input id="nickname" v-model="profileForm.nickname" type="text" class="input" />
              </div>
              <div>
                <label for="gender" class="block text-sm font-medium mb-2">性别</label>
                <select id="gender" v-model="profileForm.gender" class="input">
                  <option value="">请选择</option>
                  <option value="MALE">男</option>
                  <option value="FEMALE">女</option>
                  <option value="OTHER">其他</option>
                </select>
              </div>
                <div>
                  <label for="birthday" class="block text-sm font-medium mb-2">生日</label>
                  <input id="birthday" v-model="profileForm.birthday" type="date" class="input" />
                </div>
                <div>
                  <label for="firstName" class="block text-sm font-medium mb-2">名字</label>
                  <input id="firstName" v-model="profileForm.firstName" type="text" class="input" />
                </div>
                <div>
                  <label for="lastName" class="block text-sm font-medium mb-2">姓氏</label>
                  <input id="lastName" v-model="profileForm.lastName" type="text" class="input" />
                </div>
                <div>
                  <label for="phone" class="block text-sm font-medium mb-2">手机号</label>
                  <input id="phone" v-model="profileForm.phone" type="tel" class="input" placeholder="请输入手机号" />
                </div>
                <div>
                  <label for="school" class="block text-sm font-medium mb-2">学校</label>
                  <input id="school" v-model="profileForm.school" type="text" class="input" placeholder="学校名称" />
                </div>
                <div>
                  <label for="subject" class="block text-sm font-medium mb-2">专业/科目</label>
                  <input id="subject" v-model="profileForm.subject" type="text" class="input" placeholder="专业/科目" />
                </div>
                <div v-if="authStore.userRole === 'STUDENT'">
                  <label for="studentNo" class="block text-sm font-medium mb-2">学号</label>
                  <input id="studentNo" v-model="profileForm.studentNo" type="text" class="input" placeholder="学号" />
                </div>
                <div v-if="authStore.userRole === 'TEACHER'">
                  <label for="teacherNo" class="block text-sm font-medium mb-2">工号</label>
                  <input id="teacherNo" v-model="profileForm.teacherNo" type="text" class="input" placeholder="工号" />
                </div>
                <div>
                  <label for="country" class="block text-sm font-medium mb-2">国家</label>
                  <input id="country" v-model="profileForm.country" type="text" class="input" placeholder="中国" />
                </div>
                <div>
                  <label for="province" class="block text-sm font-medium mb-2">省份/州</label>
                  <input id="province" v-model="profileForm.province" type="text" class="input" placeholder="浙江省" />
                </div>
                <div>
                  <label for="city" class="block text-sm font-medium mb-2">城市</label>
                  <input id="city" v-model="profileForm.city" type="text" class="input" placeholder="杭州市" />
                </div>
            </div>
            <div>
              <label for="bio" class="block text-sm font-medium mb-2">个人简介</label>
              <textarea id="bio" v-model="profileForm.bio" rows="4" class="input"></textarea>
            </div>
            <div class="flex justify-end space-x-3">
              <button type="button" @click="cancelEdit" class="btn btn-outline">取消</button>
              <button type="submit" :disabled="uiStore.loading" class="btn btn-primary">保存更改</button>
            </div>
          </form>
        </div>

        <!-- Account Security -->
        <div class="card p-6">
          <h2 class="text-lg font-semibold mb-4">账户安全</h2>
          <div class="space-y-4">
            <div class="flex items-center justify-between p-4 bg-gray-100 dark:bg-gray-700 rounded-lg">
              <div>
                <h3 class="text-sm font-medium">登录密码</h3>
                <p class="text-sm text-gray-500">定期修改密码以保证账户安全</p>
              </div>
              <button @click="showChangePassword = true" class="btn btn-outline btn-sm">修改密码</button>
            </div>
            <div class="flex items-center justify-between p-4 bg-gray-100 dark:bg-gray-700 rounded-lg">
              <div>
                <h3 class="text-sm font-medium">邮箱验证</h3>
                <p class="text-sm text-gray-500">{{ userProfile.emailVerified ? '已验证' : '未验证' }}</p>
              </div>
              <button v-if="!userProfile.emailVerified" @click="handleResendVerification" :disabled="uiStore.loading" class="btn btn-outline btn-sm">
                发送验证邮件
              </button>
            </div>
          </div>
        </div>

        <!-- Change Password Form -->
        <div v-if="showChangePassword" class="card p-6">
          <h2 class="text-lg font-semibold mb-4">修改密码</h2>
          <form @submit.prevent="handleChangePassword" class="space-y-6">
            <div>
              <label for="currentPassword" class="block text-sm font-medium mb-2">当前密码</label>
              <input id="currentPassword" v-model="passwordForm.currentPassword" type="password" required class="input" />
            </div>
            <div>
              <label for="newPassword" class="block text-sm font-medium mb-2">新密码</label>
              <input id="newPassword" v-model="passwordForm.newPassword" type="password" required class="input" />
            </div>
            <div>
              <label for="confirmNewPassword" class="block text-sm font-medium mb-2">确认新密码</label>
              <input id="confirmNewPassword" v-model="confirmNewPassword" type="password" required class="input" />
            </div>
            <div class="flex justify-end space-x-3">
              <button type="button" @click="showChangePassword = false" class="btn btn-outline">取消</button>
              <button type="submit" :disabled="uiStore.loading" class="btn btn-primary">修改密码</button>
            </div>
          </form>
        </div>

      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, computed } from 'vue';
import { useAuthStore } from '@/stores/auth';
import { useUIStore } from '@/stores/ui';
import { userApi } from '@/api/user.api';
import type { UpdateProfileRequest, ChangePasswordRequest, UserProfileResponse } from '@/types/user';
import { handleApiCall } from '@/utils/api-handler';
import FileUpload from '@/components/forms/FileUpload.vue';
import { baseURL } from '@/api/config';
import UserAvatar from '@/components/ui/UserAvatar.vue';

const authStore = useAuthStore();
const uiStore = useUIStore();

const userProfile = ref<UserProfileResponse | null>(null);
const showEditProfile = ref(false);
const showChangePassword = ref(false);
const uploadHeaders = { Authorization: localStorage.getItem('token') ? `Bearer ${localStorage.getItem('token')}` : '' } as Record<string, string>;
// 头像展示统一由 UserAvatar 处理
const defaultAvatars = [
  'https://api.dicebear.com/7.x/adventurer-neutral/svg?seed=Nova',
  'https://api.dicebear.com/7.x/adventurer/svg?seed=Luna',
  'https://api.dicebear.com/7.x/avataaars/svg?seed=Kai',
  'https://api.dicebear.com/7.x/notionists-neutral/svg?seed=Iris',
  'https://api.dicebear.com/7.x/big-smile/svg?seed=Leo',
  'https://api.dicebear.com/7.x/thumbs/svg?seed=Mila',
  'https://api.dicebear.com/7.x/micah/svg?seed=Aiden',
  'https://api.dicebear.com/7.x/miniavs/svg?seed=Sage',
  'https://api.dicebear.com/7.x/adventurer-neutral/svg?seed=Zoe',
  'https://api.dicebear.com/7.x/notionists/svg?seed=Eli'
] as string[]

const profileForm = reactive<UpdateProfileRequest>({
  nickname: '',
  avatar: '',
  gender: '',
  bio: '',
  birthday: '',
  country: '',
  province: '',
  city: '',
  phone: '',
});

const passwordForm = reactive<ChangePasswordRequest>({
  currentPassword: '',
  newPassword: '',
});
const confirmNewPassword = ref('');

const fetchUserProfile = async () => {
    const response = await handleApiCall(() => userApi.getProfile(), uiStore, '获取用户信息失败');
    if(response) {
        const data = (response as any)?.data ?? response
        userProfile.value = data as UserProfileResponse;
        // 头像展示交由模板中的 UserAvatar 处理
    }
}

const setProfileForm = () => {
  if (userProfile.value) {
    profileForm.nickname = userProfile.value.nickname || '';
    profileForm.avatar = (userProfile.value as any).avatar || '';
    profileForm.gender = userProfile.value.gender || '';
    profileForm.bio = userProfile.value.bio || '';
    profileForm.birthday = userProfile.value.birthday || '';
    profileForm.firstName = userProfile.value.firstName || '';
    profileForm.lastName = userProfile.value.lastName || '';
    profileForm.school = userProfile.value.school || '';
    profileForm.subject = userProfile.value.subject || '';
    profileForm.studentNo = userProfile.value.studentNo || '';
    profileForm.teacherNo = userProfile.value.teacherNo || '';
    profileForm.country = userProfile.value.country || '';
    profileForm.province = userProfile.value.province || '';
    profileForm.city = userProfile.value.city || '';
    profileForm.phone = userProfile.value.phone || '';
  }
};

onMounted(async () => {
  await fetchUserProfile();
});

const openEditProfile = () => {
    setProfileForm();
    showEditProfile.value = true;
}

const cancelEdit = () => {
  showEditProfile.value = false;
}

const handleUpdateProfile = async () => {
  const response = await handleApiCall(() => userApi.updateProfile(profileForm), uiStore, '更新失败', { successMessage: '个人信息已更新' });
  if (response) {
    await fetchUserProfile();
    if (authStore.user) {
        (authStore.user as any).nickname = (response as any).nickname;
        (authStore.user as any).avatar = (response as any).avatar;
        (authStore.user as any).gender = (response as any).gender;
    }
    showEditProfile.value = false;
  }
};

const handleChangePassword = async () => {
  if (passwordForm.newPassword !== confirmNewPassword.value) {
    uiStore.showNotification({ type: 'error', title: '错误', message: '新密码和确认密码不匹配。' });
    return;
  }
  
  const response = await handleApiCall(() => userApi.changePassword(passwordForm), uiStore, '修改密码失败', { successMessage: '密码已成功修改' });

  if (response) {
    showChangePassword.value = false;
    passwordForm.currentPassword = '';
    passwordForm.newPassword = '';
    confirmNewPassword.value = '';
  }
};

const handleResendVerification = async () => {
  await handleApiCall(() => userApi.resendVerification(), uiStore, '发送邮件失败', { successMessage: '验证邮件已发送，请检查您的收件箱。' });
};

// 头像上传回调
const onAvatarUploaded = (res: any) => {
  const data = res?.data ?? res;
  if (data && (data.id || data.fileId)) {
    profileForm.avatar = String(data.id ?? data.fileId);
  }
};
const onAvatarUploadError = (msg: string) => {
  uiStore.showNotification({ type: 'error', title: '上传失败', message: msg || '头像上传失败' });
};

const selectDefaultAvatar = (url: string) => {
  profileForm.avatar = url
}
</script>
