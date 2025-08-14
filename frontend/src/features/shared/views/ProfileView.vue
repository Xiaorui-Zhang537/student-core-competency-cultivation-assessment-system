<template>
  <div class="p-6">
    <div class="max-w-4xl mx-auto">
      <div class="mb-8">
        <h1 class="text-3xl font-bold">{{ t('shared.profile.title') }}</h1>
        <p class="text-gray-500">{{ t('shared.profile.subtitle') }}</p>
      </div>

      <div v-if="authStore.loading && !userProfile" class="text-center py-12">
        <p>{{ t('shared.profile.messages.loadUser') }}</p>
      </div>

      <div v-else-if="userProfile" class="space-y-8">
        <!-- Profile Info -->
        <div class="card p-6">
          <h2 class="text-lg font-semibold mb-4">{{ t('shared.profile.section.profileInfo') }}</h2>
          <div class="flex items-start space-x-6">
            <!-- 头像展示 -->
            <div class="w-24 flex-shrink-0">
              <user-avatar :avatar="(userProfile as any)?.avatar" :size="96">
                <img :src="defaultAvatars[0]" alt="默认头像" class="w-24 h-24 rounded-full object-cover" />
              </user-avatar>
            </div>
            <div class="flex-1">
              <div class="grid grid-cols-1 md:grid-cols-2 gap-4">
                <div>
                  <label class="block text-sm font-medium mb-1">{{ t('shared.profile.fields.username') }}</label>
                  <p class="font-medium">{{ userProfile.nickname || userProfile.username }}</p>
                </div>
                <div>
                  <label class="block text-sm font-medium mb-1">{{ t('shared.profile.fields.email') }}</label>
                  <p>{{ userProfile.email }}</p>
                </div>
                <div>
                  <label class="block text-sm font-medium mb-1">{{ t('shared.profile.fields.role') }}</label>
                  <p>{{ userProfile.role }}</p>
                </div>
                <div>
                  <label class="block text-sm font-medium mb-1">{{ t('shared.profile.fields.nickname') }}</label>
                  <p>{{ userProfile.nickname || t('shared.profile.status.notSet') }}</p>
                </div>
                 <div>
                  <label class="block text-sm font-medium mb-1">{{ t('shared.profile.fields.gender') }}</label>
                  <p>{{ userProfile.gender || t('shared.profile.status.notSet') }}</p>
                </div>
                <div>
                  <label class="block text-sm font-medium mb-1">{{ t('shared.profile.fields.firstName') }}</label>
                  <p>{{ userProfile.firstName || t('shared.profile.status.notSet') }}</p>
                </div>
                <div>
                  <label class="block text-sm font-medium mb-1">{{ t('shared.profile.fields.lastName') }}</label>
                  <p>{{ userProfile.lastName || t('shared.profile.status.notSet') }}</p>
                </div>
                <div>
                  <label class="block text-sm font-medium mb-1">{{ t('shared.profile.fields.school') }}</label>
                  <p>{{ userProfile.school || t('shared.profile.status.notSet') }}</p>
                </div>
                <div>
                  <label class="block text-sm font-medium mb-1">{{ t('shared.profile.fields.subject') }}</label>
                  <p>{{ userProfile.subject || t('shared.profile.status.notSet') }}</p>
                </div>
                <div v-if="authStore.userRole === 'STUDENT'">
                  <label class="block text-sm font-medium mb-1">{{ t('shared.profile.fields.studentNo') }}</label>
                  <p>{{ userProfile.studentNo || t('shared.profile.status.notSet') }}</p>
                </div>
                <div v-if="authStore.userRole === 'TEACHER'">
                  <label class="block text-sm font-medium mb-1">{{ t('shared.profile.fields.teacherNo') }}</label>
                  <p>{{ userProfile.teacherNo || t('shared.profile.status.notSet') }}</p>
                </div>
                 <div>
                  <label class="block text-sm font-medium mb-1">{{ t('shared.profile.fields.bio') }}</label>
                  <p>{{ userProfile.bio || t('shared.profile.status.notSet') }}</p>
                </div>
                 <div>
                  <label class="block text-sm font-medium mb-1">{{ t('shared.profile.fields.birthday') }}</label>
                  <p>{{ userProfile.birthday || t('shared.profile.status.notSet') }}</p>
                </div>
                <div>
                  <label class="block text-sm font-medium mb-1">{{ t('shared.profile.fields.phone') }}</label>
                  <p>{{ userProfile.phone || t('shared.profile.status.notSet') }}</p>
                </div>
                <div>
                  <label class="block text-sm font-medium mb-1">{{ t('shared.profile.fields.country') }}</label>
                  <p>{{ userProfile.country || t('shared.profile.status.notSet') }}</p>
                </div>
                <div>
                  <label class="block textsm font-medium mb-1">{{ t('shared.profile.fields.province') }}</label>
                  <p>{{ userProfile.province || t('shared.profile.status.notSet') }}</p>
                </div>
                <div>
                  <label class="block text-sm font-medium mb-1">{{ t('shared.profile.fields.city') }}</label>
                  <p>{{ userProfile.city || t('shared.profile.status.notSet') }}</p>
                </div>
              </div>
              <button @click="openEditProfile" class="btn btn-outline mt-4">{{ t('shared.profile.actions.edit') }}</button>
            </div>
          </div>
        </div>

        <!-- Edit Profile Form -->
        <div v-if="showEditProfile" class="card p-6">
          <h2 class="text-lg font-semibold mb-4">{{ t('shared.profile.section.editProfile') }}</h2>
          <form @submit.prevent="handleUpdateProfile" class="space-y-6">
              <!-- 头像上传 -->
              <div>
                <label class="block text-sm font-medium mb-2">{{ t('shared.profile.fields.avatar') }}</label>
                <file-upload
                  :accept="'image/*'"
                  :multiple="false"
                  :autoUpload="true"
                  :upload-url="`${baseURL}/files/upload`"
                  :upload-headers="uploadHeaders"
                  :upload-data="{ purpose: 'avatar' }"
                  @upload-success="onAvatarUploaded"
                  @upload-error="onAvatarUploadError"
                />
                <p v-if="profileForm.avatar" class="text-xs text-gray-500 mt-2">{{ t('shared.profile.messages.updateSuccess') }}（ID: {{ profileForm.avatar }}）</p>
                <!-- 默认头像选择 -->
                <div class="mt-4">
                  <div class="flex items-center justify-between mb-2">
                    <span class="text-sm text-gray-600 dark:text-gray-400">{{ t('shared.profile.fields.avatar') }}</span>
                  </div>
                  <div class="grid grid-cols-5 gap-2">
                    <button
                      v-for="(url, idx) in defaultAvatars"
                      :key="idx"
                      type="button"
                      class="h-12 w-12 rounded-full overflow-hidden border transition-colors"
                      :class="profileForm.avatar === url ? 'border-primary-500' : 'border-gray-300 dark:border-gray-600'"
                      @click="selectDefaultAvatar(url)"
                       :title="'Avatar ' + (idx+1)"
                    >
                      <img :src="url" alt="默认头像" class="w-full h-full object-cover" />
                    </button>
                  </div>
                </div>
              </div>
              <div class="grid grid-cols-1 md:grid-cols-2 gap-6">
              <div>
                  <label for="nickname" class="block text-sm font-medium mb-2">{{ t('shared.profile.fields.nickname') }}</label>
                <input id="nickname" v-model="profileForm.nickname" type="text" class="input" />
              </div>
              <div>
                  <label for="gender" class="block text-sm font-medium mb-2">{{ t('shared.profile.fields.gender') }}</label>
                <select id="gender" v-model="profileForm.gender" class="input">
                  <option value="">{{ t('common.select') || '请选择' }}</option>
                  <option value="MALE">{{ t('shared.profile.fields.gender') }}-M</option>
                  <option value="FEMALE">{{ t('shared.profile.fields.gender') }}-F</option>
                  <option value="OTHER">{{ t('shared.profile.fields.gender') }}-O</option>
                </select>
              </div>
                <div>
                  <label for="birthday" class="block text-sm font-medium mb-2">{{ t('shared.profile.fields.birthday') }}</label>
                  <input id="birthday" v-model="profileForm.birthday" type="date" class="input" />
                </div>
                <div>
                  <label for="firstName" class="block text-sm font-medium mb-2">{{ t('shared.profile.fields.firstName') }}</label>
                  <input id="firstName" v-model="profileForm.firstName" type="text" class="input" />
                </div>
                <div>
                  <label for="lastName" class="block text-sm font-medium mb-2">{{ t('shared.profile.fields.lastName') }}</label>
                  <input id="lastName" v-model="profileForm.lastName" type="text" class="input" />
                </div>
                <div>
                  <label for="phone" class="block text-sm font-medium mb-2">{{ t('shared.profile.fields.phone') }}</label>
                  <input id="phone" v-model="profileForm.phone" type="tel" class="input" placeholder="请输入手机号" />
                </div>
                <div>
                  <label for="school" class="block text-sm font-medium mb-2">{{ t('shared.profile.fields.school') }}</label>
                  <input id="school" v-model="profileForm.school" type="text" class="input" placeholder="学校名称" />
                </div>
                <div>
                  <label for="subject" class="block text-sm font-medium mb-2">{{ t('shared.profile.fields.subject') }}</label>
                  <input id="subject" v-model="profileForm.subject" type="text" class="input" placeholder="专业/科目" />
                </div>
                <div v-if="authStore.userRole === 'STUDENT'">
                  <label for="studentNo" class="block text-sm font-medium mb-2">{{ t('shared.profile.fields.studentNo') }}</label>
                  <input id="studentNo" v-model="profileForm.studentNo" type="text" class="input" placeholder="学号" />
                </div>
                <div v-if="authStore.userRole === 'TEACHER'">
                  <label for="teacherNo" class="block text-sm font-medium mb-2">{{ t('shared.profile.fields.teacherNo') }}</label>
                  <input id="teacherNo" v-model="profileForm.teacherNo" type="text" class="input" placeholder="工号" />
                </div>
                <div>
                  <label for="country" class="block text-sm font-medium mb-2">{{ t('shared.profile.fields.country') }}</label>
                  <input id="country" v-model="profileForm.country" type="text" class="input" placeholder="中国" />
                </div>
                <div>
                  <label for="province" class="block text-sm font-medium mb-2">{{ t('shared.profile.fields.province') }}</label>
                  <input id="province" v-model="profileForm.province" type="text" class="input" placeholder="" />
                </div>
                <div>
                  <label for="city" class="block text-sm font-medium mb-2">{{ t('shared.profile.fields.city') }}</label>
                  <input id="city" v-model="profileForm.city" type="text" class="input" placeholder="" />
                </div>
            </div>
            <div>
              <label for="bio" class="block text-sm font-medium mb-2">{{ t('shared.profile.fields.bio') }}</label>
              <textarea id="bio" v-model="profileForm.bio" rows="4" class="input"></textarea>
            </div>
            <div class="flex justify-end space-x-3">
              <button type="button" @click="cancelEdit" class="btn btn-outline">{{ t('shared.profile.actions.cancel') }}</button>
              <button type="submit" :disabled="uiStore.loading" class="btn btn-primary">{{ t('shared.profile.actions.save') }}</button>
            </div>
          </form>
        </div>

        <!-- Account Security -->
        <div class="card p-6">
          <h2 class="text-lg font-semibold mb-4">{{ t('shared.profile.section.accountSecurity') }}</h2>
          <div class="space-y-4">
            <div class="flex items-center justify-between p-4 bg-gray-100 dark:bg-gray-700 rounded-lg">
              <div>
                <h3 class="text-sm font-medium">{{ t('shared.profile.section.changePassword') }}</h3>
                <p class="text-sm text-gray-500"></p>
              </div>
               <button @click="showChangePassword = true" class="btn btn-outline btn-sm">{{ t('shared.profile.actions.changePassword') }}</button>
            </div>
            <div class="flex items-center justify-between p-4 bg-gray-100 dark:bg-gray-700 rounded-lg">
              <div>
                <h3 class="text-sm font-medium">{{ t('shared.profile.fields.email') }}</h3>
                <p class="text-sm text-gray-500">{{ userProfile.emailVerified ? t('shared.profile.status.verified') : t('shared.profile.status.notVerified') }}</p>
              </div>
               <button v-if="!userProfile.emailVerified" @click="handleResendVerification" :disabled="uiStore.loading" class="btn btn-outline btn-sm">
                 {{ t('shared.profile.actions.sendVerification') }}
               </button>
            </div>
          </div>
        </div>

        <!-- Change Password Form -->
        <div v-if="showChangePassword" class="card p-6">
          <h2 class="text-lg font-semibold mb-4">{{ t('shared.profile.section.changePassword') }}</h2>
          <form @submit.prevent="handleChangePassword" class="space-y-6">
            <div>
              <label for="currentPassword" class="block text-sm font-medium mb-2">Current Password</label>
              <input id="currentPassword" v-model="passwordForm.currentPassword" type="password" required class="input" />
            </div>
            <div>
              <label for="newPassword" class="block text-sm font-medium mb-2">New Password</label>
              <input id="newPassword" v-model="passwordForm.newPassword" type="password" required class="input" />
            </div>
            <div>
              <label for="confirmNewPassword" class="block text-sm font-medium mb-2">Confirm New Password</label>
              <input id="confirmNewPassword" v-model="confirmNewPassword" type="password" required class="input" />
            </div>
            <div class="flex justify-end space-x-3">
              <button type="button" @click="showChangePassword = false" class="btn btn-outline">{{ t('shared.profile.actions.cancel') }}</button>
              <button type="submit" :disabled="uiStore.loading" class="btn btn-primary">{{ t('shared.profile.actions.changePassword') }}</button>
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
import { useI18n } from 'vue-i18n'

const authStore = useAuthStore();
const uiStore = useUIStore();
const { t } = useI18n()

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
    uiStore.showNotification({ type: 'error', title: t('app.notifications.error.title'), message: t('shared.profile.messages.passwordMismatch') });
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
  await handleApiCall(() => userApi.resendVerification(), uiStore, t('shared.profile.messages.updateFail'), { successMessage: t('shared.profile.messages.emailSent') });
};

// 头像上传回调
const onAvatarUploaded = (res: any) => {
  const data = res?.data ?? res;
  if (data && (data.id || data.fileId)) {
    profileForm.avatar = String(data.id ?? data.fileId);
  }
};
const onAvatarUploadError = (msg: string) => {
  uiStore.showNotification({ type: 'error', title: t('shared.profile.messages.uploadFailedTitle'), message: msg || t('shared.profile.messages.uploadFail') });
};

const selectDefaultAvatar = (url: string) => {
  profileForm.avatar = url
}
</script>
