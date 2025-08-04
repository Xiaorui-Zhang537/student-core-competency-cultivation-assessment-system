import type { useUIStore } from '@/stores/ui';

type UIStore = ReturnType<typeof useUIStore>;

export const handleApiCall = async <T>(
  apiCall: () => Promise<T>,
  uiStore: UIStore,
  errorMessage: string
): Promise<T | null> => {
  uiStore.setLoading(true);
  try {
    return await apiCall();
  } catch (e: any) {
    const message = e?.response?.data?.message || e.message || errorMessage;
    uiStore.showNotification({ type: 'error', title: '操作失败', message });
    return null;
  } finally {
    uiStore.setLoading(false);
  }
};
