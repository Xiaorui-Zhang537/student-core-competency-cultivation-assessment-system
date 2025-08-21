import type { useUIStore } from '@/stores/ui';
import type { Ref } from 'vue';
import { i18n } from '@/i18n'

type UIStore = ReturnType<typeof useUIStore>;

export const handleApiCall = async <T>(
  apiCall: () => Promise<T>,
  uiStore: UIStore,
  errorMessage: string,
  options?: {
    successMessage?: string;
    successI18n?: { titleKey: string; messageKey?: string; params?: Record<string, any> };
    loadingRef?: Ref<boolean>;
  }
): Promise<T | null> => {
  const loadingRef = options?.loadingRef;
  if (loadingRef) loadingRef.value = true;
  uiStore.setLoading(true);

  try {
    const response = await apiCall();
    if (options?.successI18n) {
      const title = i18n.global.t(options.successI18n.titleKey, options.successI18n.params) as string
      const message = options.successI18n.messageKey
        ? (i18n.global.t(options.successI18n.messageKey, options.successI18n.params) as string)
        : ''
      uiStore.showNotification({ type: 'success', title, message });
    } else if (options?.successMessage) {
      uiStore.showNotification({ type: 'success', title: i18n.global.t('app.notifications.success.title') as string, message: options.successMessage });
    }
    return response;
  } catch (e: any) {
    const message = e?.response?.data?.message || e.message || errorMessage;
    uiStore.showNotification({ type: 'error', title: i18n.global.t('app.notifications.error.title') as string, message });
    return null;
  } finally {
    if (loadingRef) loadingRef.value = false;
    uiStore.setLoading(false);
  }
};
