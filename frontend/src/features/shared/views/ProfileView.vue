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
            <div class="flex-1">
              <div class="grid grid-cols-1 md:grid-cols-2 gap-4">
                <div>
                  <label class="block text-sm font-medium mb-1">用户名</label>
                  <p class="font-medium">{{ userProfile.username }}</p>
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
                  <label class="block text-sm font-medium mb-1">简介</label>
                  <p>{{ userProfile.bio || '未设置' }}</p>
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
import { ref, reactive, onMounted } from 'vue';
import { useAuthStore } from '@/stores/auth';
import { useUIStore } from '@/stores/ui';
import { userApi } from '@/api/user.api';
import type { UpdateProfileRequest, ChangePasswordRequest, UserProfileResponse } from '@/types/user';
import { handleApiCall } from '@/utils/api-handler';

const authStore = useAuthStore();
const uiStore = useUIStore();

const userProfile = ref<UserProfileResponse | null>(null);
const showEditProfile = ref(false);
const showChangePassword = ref(false);

const profileForm = reactive<UpdateProfileRequest>({
  nickname: '',
  gender: '',
  bio: '',
});

const passwordForm = reactive<ChangePasswordRequest>({
  currentPassword: '',
  newPassword: '',
});
const confirmNewPassword = ref('');

const fetchUserProfile = async () => {
    const response = await handleApiCall(() => userApi.getProfile(), uiStore, '获取用户信息失败');
    if(response) {
        userProfile.value = response.data;
    }
}

const setProfileForm = () => {
  if (userProfile.value) {
    profileForm.nickname = userProfile.value.nickname || '';
    profileForm.gender = userProfile.value.gender || '';
    profileForm.bio = userProfile.value.bio || '';
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
        authStore.user.nickname = response.data.nickname;
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
</script>
