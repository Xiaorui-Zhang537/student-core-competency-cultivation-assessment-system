<template>
  <div class="p-6">
    <div class="max-w-4xl mx-auto">
      <page-header :title="t('shared.profile.title')" :subtitle="t('shared.profile.subtitle')">
        <template #actions>
          <Button
            v-if="authStore.userRole === 'TEACHER'"
            size="sm"
            variant="outline"
            @click="router.push('/teacher/profile/basic')"
          >
            {{ t('teacher.profileBasic.title') || '教师基本信息' }}
          </Button>
        </template>
      </page-header>

      <div v-if="authStore.loading && !userProfile" class="text-center py-12">
        <p>{{ t('shared.profile.messages.loadUser') }}</p>
      </div>

      <div v-else-if="userProfile" class="space-y-8">
        <!-- Profile Info -->
        <div class="rounded-xl p-6 glass-tint-primary" v-glass="{ strength: 'regular', interactive: true }">
          <h2 class="text-lg font-semibold mb-4">{{ t('shared.profile.section.profileInfo') }}</h2>
          <div class="flex items-start space-x-6">
            <!-- 头像展示 -->
            <div class="w-24 flex-shrink-0">
              <user-avatar :avatar="(userProfile as any)?.avatar" :size="96" :rounded="true" :fit="'cover'" :fallback-src="defaultAvatars[0]" />
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
                  <p>{{ userProfile.role ? t('shared.profile.roles.' + String(userProfile.role).toLowerCase()) : t('shared.profile.status.notSet') }}</p>
                </div>
                <div>
                  <label class="block text-sm font-medium mb-1">{{ t('shared.profile.fields.nickname') }}</label>
                  <p>{{ userProfile.nickname || t('shared.profile.status.notSet') }}</p>
                </div>
                 <div>
                  <label class="block text-sm font-medium mb-1">{{ t('shared.profile.fields.gender') }}</label>
                  <p>{{ userProfile.gender ? t('shared.profile.genders.' + String(userProfile.gender).toLowerCase()) : t('shared.profile.status.notSet') }}</p>
                </div>
                <div>
                  <label class="block text-sm font-medium mb-1">{{ t('shared.profile.fields.mbti') || 'MBTI' }}</label>
                  <p>{{ (userProfile as any).mbti || t('shared.profile.status.notSet') }}</p>
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
              <Button variant="indigo" class="mt-4" @click="openEditProfile">
                <svg xmlns="http://www.w3.org/2000/svg" class="h-4 w-4 mr-2" fill="none" viewBox="0 0 24 24" stroke="currentColor"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M11 5H6a2 2 0 00-2 2v11a2 2 0 002 2h11a2 2 0 002-2v-5M18.5 2.5a2.121 2.121 0 113 3L12 15l-4 1 1-4 9.5-9.5z"/></svg>
                {{ t('shared.profile.actions.edit') }}
              </Button>
            </div>
          </div>
        </div>

        <!-- Edit Profile Form -->
        <div v-if="showEditProfile" class="rounded-xl p-6 glass-tint-secondary" v-glass="{ strength: 'regular', interactive: true }">
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
                  :upload-data="{ purpose: 'avatar', relatedId: (userProfile as any)?.id }"
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
                      class="p-0 h-12 w-12 rounded-full overflow-hidden border transition-colors bg-transparent"
                      :class="profileForm.avatar === url ? 'border-primary-500 ring-2 ring-primary-500 ring-offset-2 ring-offset-white dark:ring-offset-gray-800 shadow-md' : 'border-gray-300 dark:border-gray-600 hover:border-primary-400'"
                      @click="selectDefaultAvatar(url)"
                      :title="'Avatar ' + (idx+1)"
                    >
                      <user-avatar :avatar="url" :size="48" :rounded="true" :fit="'cover'" />
                    </button>
                  </div>
                </div>
              </div>
              <div class="grid grid-cols-1 md:grid-cols-2 gap-6">
              <div>
                  <label for="nickname" class="block text-sm font-medium mb-2">{{ t('shared.profile.fields.nickname') }}</label>
                <glass-input id="nickname" v-model="profileForm.nickname" type="text" />
              </div>
              <div>
                  <label for="gender" class="block text-sm font-medium mb-2">{{ t('shared.profile.fields.gender') }}</label>
                <glass-popover-select
                  v-model="profileForm.gender"
                  :options="genderOptions"
                  size="md"
                />
              </div>
                <div>
                  <label for="mbti" class="block text-sm font-medium mb-2">MBTI</label>
                  <glass-popover-select
                    v-model="profileForm.mbti as any"
                    :options="mbtiOptions"
                    size="md"
                  />
                </div>
                <div>
                  <label for="birthday" class="block text-sm font-medium mb-2">{{ t('shared.profile.fields.birthday') }}</label>
                  <glass-date-time-picker id="birthday" v-model="profileForm.birthday" :label="'' as any" :date-only="true" />
                </div>
                <div>
                  <label for="firstName" class="block text-sm font-medium mb-2">{{ t('shared.profile.fields.firstName') }}</label>
                  <glass-input id="firstName" v-model="profileForm.firstName" type="text" />
                </div>
                <div>
                  <label for="lastName" class="block text-sm font-medium mb-2">{{ t('shared.profile.fields.lastName') }}</label>
                  <glass-input id="lastName" v-model="profileForm.lastName" type="text" />
                </div>
                <div>
                  <label for="phone" class="block text-sm font-medium mb-2">{{ t('shared.profile.fields.phone') }}</label>
                  <glass-input id="phone" v-model="profileForm.phone" type="tel" placeholder="请输入手机号" />
                </div>
                <div>
                  <label for="school" class="block text-sm font-medium mb-2">{{ t('shared.profile.fields.school') }}</label>
                  <glass-input id="school" v-model="profileForm.school" type="text" placeholder="学校名称" />
                </div>
                <div>
                  <label for="subject" class="block text-sm font-medium mb-2">{{ t('shared.profile.fields.subject') }}</label>
                  <glass-input id="subject" v-model="profileForm.subject" type="text" placeholder="专业/科目" />
                </div>
                <div v-if="authStore.userRole === 'STUDENT'">
                  <label for="studentNo" class="block text-sm font-medium mb-2">{{ t('shared.profile.fields.studentNo') }}</label>
                  <glass-input id="studentNo" v-model="profileForm.studentNo" type="text" placeholder="学号" />
                </div>
                <div v-if="authStore.userRole === 'TEACHER'">
                  <label for="teacherNo" class="block text-sm font-medium mb-2">{{ t('shared.profile.fields.teacherNo') }}</label>
                  <glass-input id="teacherNo" v-model="profileForm.teacherNo" type="text" placeholder="工号" />
                </div>
                <div>
                  <label for="country" class="block text-sm font-medium mb-2">{{ t('shared.profile.fields.country') }}</label>
                  <glass-input id="country" v-model="profileForm.country" type="text" placeholder="中国" />
                </div>
                <div>
                  <label for="province" class="block text-sm font-medium mb-2">{{ t('shared.profile.fields.province') }}</label>
                  <glass-input id="province" v-model="profileForm.province" type="text" placeholder="" />
                </div>
                <div>
                  <label for="city" class="block text-sm font-medium mb-2">{{ t('shared.profile.fields.city') }}</label>
                  <glass-input id="city" v-model="profileForm.city" type="text" placeholder="" />
                </div>
            </div>
            <div>
              <label for="bio" class="block text-sm font-medium mb-2">{{ t('shared.profile.fields.bio') }}</label>
              <glass-textarea id="bio" v-model="profileForm.bio" :rows="4" />
            </div>
            <div class="flex justify-end space-x-3">
              <Button type="button" variant="secondary" @click="cancelEdit">
                <svg xmlns="http://www.w3.org/2000/svg" class="h-4 w-4 mr-2" fill="none" viewBox="0 0 24 24" stroke="currentColor"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12"/></svg>
                {{ t('shared.profile.actions.cancel') }}
              </Button>
              <Button type="submit" :disabled="uiStore.loading" variant="indigo">
                <svg xmlns="http://www.w3.org/2000/svg" class="h-4 w-4 mr-2" fill="none" viewBox="0 0 24 24" stroke="currentColor"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M5 13l4 4L19 7"/></svg>
                {{ t('shared.profile.actions.save') }}
              </Button>
            </div>
          </form>
        </div>

        <!-- Account Security -->
        <div class="rounded-xl p-6 glass-tint-accent" v-glass="{ strength: 'regular', interactive: true }">
          <h2 class="text-lg font-semibold mb-4">{{ t('shared.profile.section.accountSecurity') }}</h2>
          <div class="space-y-4">
            <div class="flex items-center justify-between p-4 rounded-lg" v-glass="{ strength: 'thin', interactive: true }">
              <div>
                <h3 class="text-sm font-medium">{{ t('shared.profile.section.changePassword') }}</h3>
                <p class="text-sm text-gray-500"></p>
              </div>
               <Button variant="outline" size="sm" @click="showChangePassword = true">
                 <svg xmlns="http://www.w3.org/2000/svg" class="h-4 w-4 mr-2" fill="none" viewBox="0 0 24 24" stroke="currentColor"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 11c-1.657 0-3 1.343-3 3v4h6v-4c0-1.657-1.343-3-3-3z"/><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M7 11V7a5 5 0 1110 0v4"/></svg>
                 {{ t('shared.profile.actions.changePassword') }}
               </Button>
            </div>
            <div class="flex items-center justify-between p-4 rounded-lg" v-glass="{ strength: 'thin', interactive: true }">
              <div>
                <h3 class="text-sm font-medium">{{ t('shared.profile.fields.email') }}</h3>
                <p class="text-sm text_gray-500">{{ userProfile.emailVerified ? t('shared.profile.status.verified') : t('shared.profile.status.notVerified') }}</p>
              </div>
               <Button v-if="!userProfile.emailVerified" variant="secondary" size="sm" :disabled="uiStore.loading" @click="handleResendVerification">
                 <svg xmlns="http://www.w3.org/2000/svg" class="h-4 w-4 mr-2" fill="none" viewBox="0 0 24 24" stroke="currentColor"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M4 4v16h16"/><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M10 8l4 4-4 4"/></svg>
                 {{ t('shared.profile.actions.sendVerification') }}
               </Button>
            </div>
          </div>
        </div>

        <!-- Change Password Form -->
        <div v-if="showChangePassword" class="rounded-xl p-6 glass-tint-info" v-glass="{ strength: 'regular', interactive: true }">
          <h2 class="text-lg font-semibold mb-4">{{ t('shared.profile.section.changePassword') }}</h2>
          <form @submit.prevent="handleChangePassword" class="space-y-6">
            <div>
              <label for="currentPassword" class="block text-sm font-medium mb-2">{{ t('shared.profile.changePwd.current') || 'Current Password' }}</label>
              <input id="currentPassword" v-model="passwordForm.currentPassword" type="password" required class="input" />
            </div>
            <div>
              <label for="newPassword" class="block text-sm font-medium mb-2">{{ t('shared.profile.changePwd.new') || 'New Password' }}</label>
              <input id="newPassword" v-model="passwordForm.newPassword" type="password" required class="input" />
            </div>
            <div>
              <label for="confirmNewPassword" class="block text-sm font-medium mb-2">{{ t('shared.profile.changePwd.confirm') || 'Confirm New Password' }}</label>
              <input id="confirmNewPassword" v-model="confirmNewPassword" type="password" required class="input" />
            </div>
            <div class="flex justify-end space-x-3">
              <Button type="button" variant="secondary" @click="showChangePassword = false">
                <svg xmlns="http://www.w3.org/2000/svg" class="h-4 w-4 mr-2" fill="none" viewBox="0 0 24 24" stroke="currentColor"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12"/></svg>
                {{ t('shared.profile.actions.cancel') }}
              </Button>
              <Button type="submit" :disabled="uiStore.loading" variant="indigo">
                <template #icon>
                  <svg xmlns="http://www.w3.org/2000/svg" class="h-4 w-4" fill="none" viewBox="0 0 24 24" stroke="currentColor"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 11c-1.657 0-3 1.343-3 3v4h6v-4c0-1.657-1.343-3-3-3z"/><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M7 11V7a5 5 0 1110 0v4"/></svg>
                </template>
                {{ t('shared.profile.actions.changePassword') }}
              </Button>
            </div>
          </form>
        </div>

      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import GlassDateTimePicker from '@/components/ui/inputs/GlassDateTimePicker.vue'
import { ref, reactive, onMounted, computed } from 'vue';
import { useAuthStore } from '@/stores/auth';
import { useUIStore } from '@/stores/ui';
import { useRouter } from 'vue-router'
import { userApi } from '@/api/user.api';
import type { UpdateProfileRequest, ChangePasswordRequest, UserProfileResponse } from '@/types/user';
import { handleApiCall } from '@/utils/api-handler';
import FileUpload from '@/components/forms/FileUpload.vue';
import { baseURL } from '@/api/config';
import UserAvatar from '@/components/ui/UserAvatar.vue';
import Button from '@/components/ui/Button.vue'
import { DEFAULT_AVATARS } from '@/shared/utils/avatars'
import { useI18n } from 'vue-i18n'
import PageHeader from '@/components/ui/PageHeader.vue'
import GlassInput from '@/components/ui/inputs/GlassInput.vue'
import GlassTextarea from '@/components/ui/inputs/GlassTextarea.vue'
import GlassPopoverSelect from '@/components/ui/filters/GlassPopoverSelect.vue'

const authStore = useAuthStore();
const uiStore = useUIStore();
const router = useRouter()
const { t } = useI18n()

const userProfile = ref<UserProfileResponse | null>(null);
const genderOptions = computed(() => [
  { label: t('shared.profile.genders.male') as string, value: 'MALE' },
  { label: t('shared.profile.genders.female') as string, value: 'FEMALE' },
  { label: t('shared.profile.genders.other') as string, value: 'OTHER' },
])
const mbtiOptions = [
  'ISTJ','ISFJ','INFJ','INTJ','ISTP','ISFP','INFP','INTP','ESTP','ESFP','ENFP','ENTP','ESTJ','ESFJ','ENFJ','ENTJ'
].map(v => ({ label: v, value: v }))
const showEditProfile = ref(false);
const showChangePassword = ref(false);
const uploadHeaders = { Authorization: localStorage.getItem('token') ? `Bearer ${localStorage.getItem('token')}` : '' } as Record<string, string>;
// 头像展示统一由 UserAvatar 处理
const defaultAvatars = DEFAULT_AVATARS as string[]

const profileForm = reactive<UpdateProfileRequest>({
  nickname: '',
  avatar: '',
  gender: '',
  mbti: '',
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
    // @ts-ignore
    profileForm.mbti = (userProfile.value as any).mbti || '';
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
  const response = await handleApiCall(
    () => userApi.updateProfile(profileForm),
    uiStore,
    t('shared.profile.messages.updateFail') as string,
    { successI18n: { titleKey: 'app.notifications.success.title', messageKey: 'shared.profile.messages.updateSuccess' } }
  );
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
  
  const response = await handleApiCall(
    () => userApi.changePassword(passwordForm),
    uiStore,
    t('shared.profile.messages.updateFail') as string,
    { successI18n: { titleKey: 'app.notifications.success.title', messageKey: 'shared.profile.messages.passwordChanged' } }
  );

  if (response) {
    showChangePassword.value = false;
    passwordForm.currentPassword = '';
    passwordForm.newPassword = '';
    confirmNewPassword.value = '';
  }
};

const handleResendVerification = async () => {
  await handleApiCall(
    () => userApi.resendVerification(),
    uiStore,
    t('shared.profile.messages.updateFail') as string,
    { successI18n: { titleKey: 'app.notifications.success.title', messageKey: 'shared.profile.messages.emailSent' } }
  );
};

// 头像上传回调
const onAvatarUploaded = async (res: any) => {
  const data = res?.data ?? res;
  const newId = data && (data.id || data.fileId);
  if (newId) {
    await handleApiCall(
      () => userApi.updateAvatar(Number(newId)),
      uiStore,
      t('shared.profile.messages.updateFail') as string,
      { successI18n: { titleKey: 'app.notifications.success.title', messageKey: 'shared.profile.messages.updateSuccess' } }
    );
    await fetchUserProfile();
  }
};
const onAvatarUploadError = (msg: string) => {
  uiStore.showNotification({ type: 'error', title: t('shared.profile.messages.uploadFailedTitle'), message: msg || t('shared.profile.messages.uploadFail') });
};

const selectDefaultAvatar = (url: string) => {
  profileForm.avatar = url
}
</script>
