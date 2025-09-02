import { defineStore } from 'pinia';
import { ref, computed, nextTick } from 'vue';
import { authApi } from '@/api/auth.api';
import { userApi } from '@/api/user.api';
import type { User, LoginRequest, RegisterRequest, AuthResponse } from '@/types/auth';
import type { UpdateProfileRequest } from '@/types/user';
import { useUIStore } from './ui';
import router from '@/router';
import { i18n } from '@/i18n'

export const useAuthStore = defineStore('auth', () => {
  const uiStore = useUIStore();
  
  const user = ref<User | null>(null);
  const token = ref<string | null>(localStorage.getItem('token'));
  const loading = ref(false);
  const error = ref<string | null>(null);

  const isAuthenticated = computed(() => !!token.value && !!user.value);
  const userRole = computed(() => user.value?.role);

  const handleApiCall = async <T>(apiCall: () => Promise<T>): Promise<T | null> => {
    loading.value = true;
    error.value = null;
    try {
      return await apiCall();
    } catch (e: any) {
      const errorMessage = e.message || (i18n.global.t('app.notifications.error.unknown') as string);
      error.value = errorMessage;
      uiStore.showNotification({ type: 'error', title: i18n.global.t('app.notifications.error.title') as string, message: errorMessage });
      return null;
    } finally {
      loading.value = false;
    }
  };

  const setAuthData = (data: AuthResponse) => {
    const userData: User = { ...data.user, id: String(data.user.id) };
    userData.role = (data.user.role || 'STUDENT').toUpperCase() as 'STUDENT' | 'TEACHER' | 'ADMIN';
    user.value = userData;
    token.value = data.accessToken;
    localStorage.setItem('token', data.accessToken);
    localStorage.setItem('refreshToken', data.refreshToken);
  };

  const clearAuthData = () => {
    user.value = null;
    token.value = null;
    localStorage.removeItem('token');
    localStorage.removeItem('refreshToken');
  };

  // ◇ Utility to handle possible AxiosResponse wrappers
  function unwrap<T>(res: any): T {
    return res && res.data !== undefined ? res.data : res;
  }

  const login = async (credentials: LoginRequest) => {
    const response = await handleApiCall(() => authApi.login(credentials));
    if (!response) return;
    const data = unwrap<AuthResponse>(response);
    setAuthData(data);
    uiStore.showNotification({
      type: 'success',
      title: i18n.global.t('app.auth.loginSuccess.title') as string,
      message: i18n.global.t('app.auth.loginSuccess.msg', { name: data.user.username }) as string
    });
    await nextTick();
    // 支持 redirect 参数，默认学生到 /student/home，教师到 /teacher/dashboard
    const queryRedirect = (router.currentRoute.value.query?.redirect as string) || '';
    if (queryRedirect) {
      await router.push(queryRedirect);
      return;
    }
    const target = data.user.role === 'TEACHER' ? '/teacher/dashboard' : '/student/home';
    await router.push(target);
  };

  const register = async (details: RegisterRequest) => {
    const response = await handleApiCall(() => authApi.register(details));
    if (!response) return;
    uiStore.showNotification({
      type: 'success',
      title: i18n.global.t('app.auth.registerSuccess.title') as string,
      message: i18n.global.t('app.auth.registerSuccess.msg') as string
    });
    await nextTick();
    await router.push('/auth/check-email');
  };

  const logout = async () => {
    await handleApiCall(authApi.logout);
    clearAuthData();
    await router.push('/auth/login');
  };

  const fetchUser = async () => {
    if (!token.value) return;
    const response = await handleApiCall(userApi.getProfile);
    if (response) {
      const data = unwrap<User>(response);
      const userData: User = { ...data, id: String(data.id) };
      userData.role = (data.role || 'STUDENT').toUpperCase() as 'STUDENT' | 'TEACHER' | 'ADMIN';
      user.value = userData;
    } else {
      clearAuthData();
    }
  };

  const updateUserProfile = async (data: UpdateProfileRequest) => {
      const response = await handleApiCall(() => userApi.updateProfile(data));
      if (response && user.value) {
          const resData = unwrap<UpdateProfileRequest & { avatar: string; nickname: string }>(response);
          user.value.nickname = resData.nickname;
          user.value.avatar = resData.avatar;
          uiStore.showNotification({
            type: 'success',
            title: '个人资料已更新',
            message: '您的信息已成功保存。'
          });
      }
  }

  return {
    user,
    token,
    loading,
    error,
    isAuthenticated,
    userRole,
    login,
    register,
    logout,
    fetchUser,
    updateUserProfile,
  };
});
