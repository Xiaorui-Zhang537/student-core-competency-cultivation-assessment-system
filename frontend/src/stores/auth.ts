import { defineStore } from 'pinia';
import { ref, computed } from 'vue';
import { authApi } from '@/api/auth.api';
import { userApi } from '@/api/user.api';
import type { User, LoginRequest, RegisterRequest } from '@/types/auth';
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
      error.value = e.message || 'An unknown error occurred';
      uiStore.showNotification({ type: 'error', title: '操作失败', message: error.value });
      return null;
    } finally {
      loading.value = false;
    }
  };

  const setAuthData = (data: { user: User; token: string; refreshToken: string }) => {
    user.value = data.user;
    token.value = data.token;
    localStorage.setItem('token', data.token);
    localStorage.setItem('refreshToken', data.refreshToken);
    // Set token for subsequent API calls
    // This needs to be implemented in api/config.ts
  };

  const clearAuthData = () => {
    user.value = null;
    token.value = null;
    localStorage.removeItem('token');
    localStorage.removeItem('refreshToken');
    // Clear token from api config
  };

  const login = async (credentials: LoginRequest) => {
    const response = await handleApiCall(() => authApi.login(credentials));
    if (response) {
      setAuthData(response.data);
      uiStore.showNotification({ type: 'success', title: '登录成功' });
      router.push(user.value?.role === 'TEACHER' ? '/teacher/dashboard' : '/student/dashboard');
    }
  };

  const register = async (details: RegisterRequest) => {
    const response = await handleApiCall(() => authApi.register(details));
    if (response) {
      setAuthData(response.data);
      uiStore.showNotification({ type: 'success', title: '注册成功' });
      router.push(user.value?.role === 'TEACHER' ? '/teacher/dashboard' : '/student/dashboard');
    }
  };

  const logout = async () => {
    await handleApiCall(authApi.logout);
    clearAuthData();
    router.push('/login');
  };

  const fetchUser = async () => {
    if (!token.value) return;
    const response = await handleApiCall(userApi.getProfile);
    if (response) {
      user.value = { ...response.data, token: token.value, refreshToken: '' }; // refreshToken is not available here
    } else {
      // Token is invalid
      clearAuthData();
    }
  };
  
  const updateUserProfile = async (data: UpdateProfileRequest) => {
      const response = await handleApiCall(() => userApi.updateProfile(data));
      if (response && user.value) {
          user.value.nickname = response.data.nickname;
          user.value.avatar = response.data.avatar;
          uiStore.showNotification({ type: 'success', title: '个人资料已更新' });
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
