import { computed } from 'vue'
import { i18n, setLocale, type SupportedLocale } from './index'

export function useLocale() {
	const locale = computed<SupportedLocale>(() => i18n.global.locale.value as SupportedLocale)
	return {
		locale,
		setLocale,
		t: i18n.global.t.bind(i18n.global)
	}
}


