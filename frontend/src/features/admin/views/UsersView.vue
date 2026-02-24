<template>
  <div class="p-6">
    <PageHeader :title="t('admin.sidebar.users')" :subtitle="t('admin.title')">
      <template #actions>
        <button variant="primary" @click="openCreate">
          <plus-icon class="w-4 h-4 mr-2" />
          {{ t('common.create') || '创建' }}
        </button>
      </template>
    </PageHeader>

    <card padding="md" tint="secondary" class="mt-4">
      <div class="flex flex-col md:flex-row gap-3 md:items-center">
        <glass-search-input v-model="keyword" :placeholder="String(t('common.search') || '搜索用户名/邮箱/学号/工号')" size="sm" tint="secondary" />
        <div class="flex gap-2 flex-wrap">
          <glass-popover-select v-model="role" :options="roleOptions" size="sm" width="140px" />
          <glass-popover-select v-model="status" :options="statusOptions" size="sm" width="140px" />
          <button size="sm" variant="outline" :disabled="loading" @click="reload">{{ String(t('common.search') || '查询') }}</button>
        </div>
      </div>
    </card>

    <loading-overlay v-if="loading" class="mt-4" :text="String(t('common.loading') || '加载中…')" />
    <error-state
      v-else-if="error"
      class="mt-4"
      :title="String(t('common.error') || '加载失败')"
      :message="error"
      :actionText="String(t('common.retry') || '重试')"
      @action="reload"
    />

    <card v-else padding="md" tint="secondary" class="mt-4">
      <glass-table>
        <template #head>
          <tr>
            <th class="px-6 py-3 text-center text-xs font-semibold text-gray-700 dark:text-gray-200 tracking-wide whitespace-nowrap">{{ t('common.columns.id') || 'ID' }}</th>
            <th class="px-6 py-3 text-left text-xs font-semibold text-gray-700 dark:text-gray-200 tracking-wide">{{ t('common.columns.user') || 'User' }}</th>
            <th class="px-6 py-3 text-center text-xs font-semibold text-gray-700 dark:text-gray-200 tracking-wide whitespace-nowrap w-40">{{ t('common.columns.role') || 'Role' }}</th>
            <th class="px-6 py-3 text-center text-xs font-semibold text-gray-700 dark:text-gray-200 tracking-wide whitespace-nowrap w-40">{{ t('common.columns.status') || 'Status' }}</th>
            <th class="px-6 py-3 text-right text-xs font-semibold text-gray-700 dark:text-gray-200 tracking-wide whitespace-nowrap">{{ t('common.columns.actions') || 'Actions' }}</th>
          </tr>
        </template>
        <template #body>
          <tr v-for="u in items" :key="u.id" class="hover:bg-white/10 transition-colors duration-150">
            <td class="px-6 py-3 text-center font-mono text-xs">{{ u.id }}</td>
            <td class="px-6 py-3">
              <div class="font-medium">{{ u.nickname || u.username }}</div>
              <div class="text-xs text-subtle">{{ u.email }}</div>
              <div class="text-xs text-subtle" v-if="u.studentNo || u.teacherNo">{{ u.studentNo || u.teacherNo }}</div>
            </td>
            <td class="px-6 py-3 w-40 text-center">
              <glass-popover-select
                :model-value="u.role"
                :options="roleEditOptions"
                size="sm"
                @update:modelValue="(v:any) => onChangeRole(u, v)"
              />
            </td>
            <td class="px-6 py-3 w-40 text-center">
              <glass-popover-select
                :model-value="u.status"
                :options="statusEditOptions"
                size="sm"
                @update:modelValue="(v:any) => onChangeStatus(u, v)"
              />
            </td>
            <td class="px-6 py-3 text-right">
              <button size="sm" variant="outline" :disabled="opLoadingId===u.id" @click="sendReset(u)">
                {{ t('auth.forgotPassword.send') || '重置密码' }}
              </button>
            </td>
          </tr>

          <tr v-if="items.length === 0">
            <td colspan="5" class="px-6 py-6">
              <empty-state :title="String(t('common.empty') || '暂无数据')" />
            </td>
          </tr>
        </template>
      </glass-table>

      <pagination-bar
        :page="page"
        :page-size="pageSize"
        :total-items="total"
        :total-pages="totalPages"
        :disabled="loading"
        @update:page="(v: number) => { page = v; reload() }"
        @update:pageSize="(v: number) => { pageSize = v; page = 1; reload() }"
      />
    </card>

    <glass-modal v-if="showCreate" :title="String(t('common.create') || '创建用户')" size="md" heightVariant="tall" @close="closeCreate">
      <form class="space-y-3" @submit.prevent="create">
        <div class="grid grid-cols-1 md:grid-cols-2 gap-3">
          <div>
            <label class="block text-sm font-medium mb-1">{{ t('auth.register.form.username.label') || '用户名' }}</label>
            <glass-input v-model="form.username" />
          </div>
          <div>
            <label class="block text-sm font-medium mb-1">{{ t('auth.register.form.email.label') || '邮箱' }}</label>
            <glass-input v-model="form.email" type="email" />
          </div>
          <div>
            <label class="block text-sm font-medium mb-1">{{ t('auth.register.form.password.label') || '密码' }}</label>
            <glass-input v-model="form.password" type="password" />
          </div>
          <div>
            <label class="block text-sm font-medium mb-1">{{ t('common.columns.role') || '角色' }}</label>
            <glass-popover-select v-model="form.role" :options="roleCreateOptions" size="sm" />
          </div>
          <div>
            <label class="block text-sm font-medium mb-1">{{ t('common.columns.status') || '状态' }}</label>
            <glass-popover-select v-model="form.status" :options="statusEditOptions" size="sm" />
          </div>
          <div>
            <label class="block text-sm font-medium mb-1">{{ t('admin.people.fields.nickname') || '昵称' }}</label>
            <glass-input v-model="form.nickname" />
          </div>
          <div>
            <label class="block text-sm font-medium mb-1">{{ t('admin.people.fields.studentNo') || '学号' }}</label>
            <glass-input v-model="form.studentNo" />
          </div>
          <div>
            <label class="block text-sm font-medium mb-1">{{ t('admin.people.fields.teacherNo') || '工号' }}</label>
            <glass-input v-model="form.teacherNo" />
          </div>
        </div>

        <div class="flex justify-end gap-2 pt-2">
          <button variant="ghost" type="button" @click="closeCreate">{{ t('common.cancel') || '取消' }}</button>
          <button variant="primary" type="submit" :disabled="createLoading">{{ t('common.confirm') || '确认' }}</button>
        </div>
      </form>
    </glass-modal>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { useI18n } from 'vue-i18n'
import Card from '@/components/ui/Card.vue'
import Button from '@/components/ui/Button.vue'
import ErrorState from '@/components/ui/ErrorState.vue'
import EmptyState from '@/components/ui/EmptyState.vue'
import PaginationBar from '@/components/ui/PaginationBar.vue'
import GlassPopoverSelect from '@/components/ui/filters/GlassPopoverSelect.vue'
import GlassSearchInput from '@/components/ui/inputs/GlassSearchInput.vue'
import GlassModal from '@/components/ui/GlassModal.vue'
import GlassInput from '@/components/ui/inputs/GlassInput.vue'
import GlassTable from '@/components/ui/tables/GlassTable.vue'
import { PlusIcon } from '@heroicons/vue/24/outline'
import { adminApi, type AdminUserCreateRequest, type AdminUserListItem } from '@/api/admin.api'
import { useUIStore } from '@/stores/ui'

const { t } = useI18n()
const ui = useUIStore()

const loading = ref(false)
const error = ref<string | null>(null)
const items = ref<AdminUserListItem[]>([])
const page = ref(1)
const pageSize = ref(20)
const total = ref(0)
const totalPages = ref(1)

const keyword = ref('')
const role = ref('') // filter
const status = ref('') // filter

const roleOptions = computed(() => ([
  { label: String(t('admin.people.allRoles') || t('common.all') || 'All'), value: '' },
  { label: String(t('admin.roles.student') || 'student'), value: 'student' },
  { label: String(t('admin.roles.teacher') || 'teacher'), value: 'teacher' },
  { label: String(t('admin.roles.admin') || 'admin'), value: 'admin' },
]))
const statusOptions = computed(() => ([
  { label: String(t('admin.people.allStatus') || t('common.all') || 'All'), value: '' },
  { label: String(t('admin.people.userStatus.active') || 'active'), value: 'active' },
  { label: String(t('admin.people.userStatus.disabled') || 'disabled'), value: 'disabled' },
]))
const roleEditOptions = computed(() => ([
  { label: String(t('admin.roles.student') || 'student'), value: 'student' },
  { label: String(t('admin.roles.teacher') || 'teacher'), value: 'teacher' },
  { label: String(t('admin.roles.admin') || 'admin'), value: 'admin' },
]))
const roleCreateOptions = roleEditOptions
const statusEditOptions = computed(() => ([
  { label: String(t('admin.people.userStatus.active') || 'active'), value: 'active' },
  { label: String(t('admin.people.userStatus.disabled') || 'disabled'), value: 'disabled' },
]))

const opLoadingId = ref<number | null>(null)

async function reload() {
  loading.value = true
  error.value = null
  try {
    const res = await adminApi.pageUsers({
      page: page.value,
      size: pageSize.value,
      keyword: keyword.value || undefined,
      role: role.value || undefined,
      status: status.value || undefined
    })
    items.value = res.items || []
    total.value = Number(res.total || 0)
    totalPages.value = Number(res.totalPages || 1)
  } catch (e: any) {
    error.value = e?.message || 'Failed to load'
  } finally {
    loading.value = false
  }
}

async function onChangeRole(u: AdminUserListItem, v: string) {
  opLoadingId.value = u.id
  try {
    await adminApi.updateUserRole(u.id, String(v))
    u.role = String(v)
    ui.showNotification({ type: 'success', title: 'OK', message: 'Role updated' })
  } catch (e: any) {
    ui.showNotification({ type: 'error', title: 'Error', message: e?.message || 'Failed' })
  } finally {
    opLoadingId.value = null
  }
}

async function onChangeStatus(u: AdminUserListItem, v: string) {
  opLoadingId.value = u.id
  try {
    await adminApi.updateUserStatus(u.id, String(v))
    u.status = String(v)
    ui.showNotification({ type: 'success', title: 'OK', message: 'Status updated' })
  } catch (e: any) {
    ui.showNotification({ type: 'error', title: 'Error', message: e?.message || 'Failed' })
  } finally {
    opLoadingId.value = null
  }
}

async function sendReset(u: AdminUserListItem) {
  opLoadingId.value = u.id
  try {
    await adminApi.sendResetPasswordEmail(u.id, (localStorage.getItem('locale') || 'zh-CN') as any)
    ui.showNotification({ type: 'success', title: 'OK', message: 'Reset email sent' })
  } catch (e: any) {
    ui.showNotification({ type: 'error', title: 'Error', message: e?.message || 'Failed' })
  } finally {
    opLoadingId.value = null
  }
}

const showCreate = ref(false)
const createLoading = ref(false)
const form = reactive<AdminUserCreateRequest>({
  username: '',
  email: '',
  password: '',
  role: 'student',
  status: 'active',
  nickname: '',
  studentNo: '',
  teacherNo: '',
})

function openCreate() {
  showCreate.value = true
}
function closeCreate() {
  showCreate.value = false
}

async function create() {
  createLoading.value = true
  try {
    await adminApi.createUser(form)
    ui.showNotification({ type: 'success', title: 'OK', message: 'User created' })
    closeCreate()
    page.value = 1
    await reload()
  } catch (e: any) {
    ui.showNotification({ type: 'error', title: 'Error', message: e?.message || 'Failed' })
  } finally {
    createLoading.value = false
  }
}

onMounted(reload)
</script>

