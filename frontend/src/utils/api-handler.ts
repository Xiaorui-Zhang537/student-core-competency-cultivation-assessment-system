import type { useUIStore } from '@/stores/ui';
import type { Ref } from 'vue';

type UIStore = ReturnType<typeof useUIStore>;

export const handleApiCall = async <T>(
  apiCall: () => Promise<T>,
  uiStore: UIStore,
  errorMessage: string,
  options?: { successMessage?: string; loadingRef?: Ref<boolean> }
): Promise<T | null> => {
  const loadingRef = options?.loadingRef;
  if (loadingRef) loadingRef.value = true;
  uiStore.loading = true;

  try {
    const response = await apiCall();
    if (options?.successMessage) {
      uiStore.showNotification({ type: 'success', title: '成功', message: options.successMessage });
    }
    return response;
  } catch (e: any) {
    const message = e?.response?.data?.message || e.message || errorMessage;
    uiStore.showNotification({ type: 'error', title: '操作失败', message });
    return null;
  } finally {
    if (loadingRef) loadingRef.value = false;
    uiStore.loading = false;
  }
};
