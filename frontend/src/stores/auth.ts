import { defineStore } from 'pinia';
import { ref, computed, nextTick } from 'vue';
import { authApi } from '@/api/auth.api';
import { userApi } from '@/api/user.api';
import type { User, LoginRequest, RegisterRequest, AuthResponse } from '@/types/auth';
import type { UpdateProfileRequest } from '@/types/user';
import { useUIStore } from './ui';
import router from '@/router';

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
      const errorMessage = e.message || 'An unknown error occurred';
      error.value = errorMessage;
      uiStore.showNotification({ type: 'error', title: '操作失败', message: errorMessage });
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

  const login = async (credentials: LoginRequest) => {
    const response = await handleApiCall(() => authApi.login(credentials));
    if (response) {
      setAuthData(response.data);
      uiStore.showNotification({ type: 'success', title: '登录成功', message: `欢迎回来, ${response.data.user.username}!` });
      await nextTick();
      await router.push(user.value?.role === 'TEACHER' ? '/teacher/dashboard' : '/student/dashboard');
    }
  };

  const register = async (details: RegisterRequest) => {
    const response = await handleApiCall(() => authApi.register(details));
    if (response) {
      setAuthData(response.data);
      uiStore.showNotification({ type: 'success', title: '注册成功', message: '欢迎加入我们的平台！' });
      await nextTick();
      await router.push(user.value?.role === 'TEACHER' ? '/teacher/dashboard' : '/student/dashboard');
    }
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
      const userData: User = { ...response.data, id: String(response.data.id) };
      userData.role = (response.data.role || 'STUDENT').toUpperCase() as 'STUDENT' | 'TEACHER' | 'ADMIN';
      user.value = userData;
    } else {
      clearAuthData();
    }
  };
  
  const updateUserProfile = async (data: UpdateProfileRequest) => {
      const response = await handleApiCall(() => userApi.updateProfile(data));
      if (response && user.value) {
          user.value.nickname = response.data.nickname;
          user.value.avatar = response.data.avatar;
          uiStore.showNotification({ type: 'success', title: '个人资料已更新', message: '您的信息已成功保存。' });
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
