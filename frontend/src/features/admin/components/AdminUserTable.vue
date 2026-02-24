<template>
  <div>
    <div v-if="headerTitle || $slots.header" class="flex items-center justify-between gap-3 mb-3">
      <div class="min-w-0">
        <div v-if="headerTitle" class="text-sm font-medium truncate">{{ headerTitle }}</div>
        <div v-if="headerSubtitle" class="text-xs text-subtle mt-1 whitespace-pre-line break-words">{{ headerSubtitle }}</div>
      </div>
      <div class="shrink-0 flex items-center gap-2">
        <slot name="header" />
        <button v-if="allowCreate" variant="primary" size="sm" @click="openCreate">
          <plus-icon class="w-4 h-4 mr-2" />
          {{ t('common.create') || '创建' }}
        </button>
      </div>
    </div>

    <loading-overlay v-if="loading" :text="String(t('common.loading') || '加载中…')" />
    <error-state
      v-else-if="error"
      class="mt-2"
      :title="String(t('common.error') || '加载失败')"
      :message="error"
      :actionText="String(t('common.retry') || '重试')"
      @action="reload"
    />

    <card v-else padding="md" tint="secondary" class="mt-2">
      <glass-table>
        <template #head>
          <tr>
            <th class="px-6 py-3 text-center text-xs font-semibold text-gray-700 dark:text-gray-200 tracking-wide whitespace-nowrap">
              {{ t('common.columns.id') || 'ID' }}
            </th>
            <th class="px-6 py-3 text-left text-xs font-semibold text-gray-700 dark:text-gray-200 tracking-wide">
              {{ personHeader }}
            </th>
            <th v-if="showRoleColumn" class="px-6 py-3 text-center text-xs font-semibold text-gray-700 dark:text-gray-200 tracking-wide whitespace-nowrap w-40">
              {{ t('common.columns.role') || 'Role' }}
            </th>
            <th class="px-6 py-3 text-center text-xs font-semibold text-gray-700 dark:text-gray-200 tracking-wide whitespace-nowrap w-40">
              {{ t('common.columns.status') || 'Status' }}
            </th>
            <th v-if="showActions" class="px-6 py-3 text-right text-xs font-semibold text-gray-700 dark:text-gray-200 tracking-wide whitespace-nowrap">
              {{ t('common.columns.actions') || 'Actions' }}
            </th>
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
            <td v-if="showRoleColumn" class="px-6 py-3 text-center w-40">
              <glass-popover-select
                :model-value="u.role"
                :options="roleEditOptions"
                size="sm"
                :disabled="!allowRoleEdit"
                @update:modelValue="(v:any) => onChangeRole(u, v)"
              />
            </td>
            <td class="px-6 py-3 text-center w-40">
              <glass-popover-select
                :model-value="u.status"
                :options="statusEditOptions"
                size="sm"
                @update:modelValue="(v:any) => onChangeStatus(u, v)"
              />
            </td>
            <td v-if="showActions" class="px-6 py-3 text-right whitespace-nowrap">
              <template v-if="mode === 'users'">
                <button size="sm" variant="outline" :disabled="opLoadingId===u.id" @click="sendReset(u)">
                  {{ t('auth.forgotPassword.send') || '重置密码' }}
                </button>
                <button size="sm" variant="outline" class="ml-2" @click="openChat(u)">
                  {{ t('shared.chat.open') || '聊天' }}
                </button>
                <button size="sm" variant="outline" class="ml-2" @click="openAudit(u)">
                  {{ t('admin.student360.auditAiVoice') || 'AI/口语审计' }}
                </button>
              </template>
              <template v-else>
                <button size="sm" variant="outline" @click="openDetail(u.id)">
                  {{ t('common.view') || '查看' }}
                </button>
                <button size="sm" variant="outline" class="ml-2" @click="openChat(u)">
                  {{ t('shared.chat.open') || '聊天' }}
                </button>
                <button size="sm" variant="outline" class="ml-2" @click="openAudit(u)">
                  {{ t('admin.student360.auditAiVoice') || 'AI/口语审计' }}
                </button>
              </template>
            </td>
          </tr>

          <tr v-if="items.length === 0">
            <td :colspan="colSpan" class="px-6 py-6">
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
import { computed, onMounted, reactive, ref, watch } from 'vue'
import { useRouter } from 'vue-router'
import { useI18n } from 'vue-i18n'
import Card from '@/components/ui/Card.vue'
import Button from '@/components/ui/Button.vue'
import ErrorState from '@/components/ui/ErrorState.vue'
import EmptyState from '@/components/ui/EmptyState.vue'
import PaginationBar from '@/components/ui/PaginationBar.vue'
import GlassPopoverSelect from '@/components/ui/filters/GlassPopoverSelect.vue'
import GlassModal from '@/components/ui/GlassModal.vue'
import GlassInput from '@/components/ui/inputs/GlassInput.vue'
import GlassTable from '@/components/ui/tables/GlassTable.vue'
import { PlusIcon } from '@heroicons/vue/24/outline'
import { adminApi, type AdminUserCreateRequest, type AdminUserListItem } from '@/api/admin.api'
import { useUIStore } from '@/stores/ui'
import { useChatStore } from '@/stores/chat'

type Mode = 'users' | 'students' | 'teachers'

const props = withDefaults(defineProps<{
  mode: Mode
  keyword?: string
  status?: string
  /** 固定 role 过滤（students/teachers tab 使用） */
  roleFixed?: string
  allowCreate?: boolean
  allowRoleEdit?: boolean
  headerTitle?: string
  headerSubtitle?: string
}>(), {
  keyword: '',
  status: '',
  roleFixed: '',
  allowCreate: false,
  allowRoleEdit: true,
  headerTitle: '',
  headerSubtitle: '',
})

const { t } = useI18n()
const ui = useUIStore()
const router = useRouter()
const chat = useChatStore()

const loading = ref(false)
const error = ref<string | null>(null)
const items = ref<AdminUserListItem[]>([])
const page = ref(1)
const pageSize = ref(20)
const total = ref(0)
const totalPages = ref(1)

const opLoadingId = ref<number | null>(null)

const showRoleColumn = computed(() => props.mode === 'users')
const showActions = computed(() => true)
const colSpan = computed(() => 4 + (showRoleColumn.value ? 1 : 0))

const personHeader = computed(() => {
  if (props.mode === 'students') return t('admin.tabs.students') || '学生'
  if (props.mode === 'teachers') return t('admin.tabs.teachers') || '教师'
  return t('admin.tabs.users') || '用户'
})

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

async function reload() {
  loading.value = true
  error.value = null
  try {
    const res = await adminApi.pageUsers({
      page: page.value,
      size: pageSize.value,
      keyword: (props.keyword || '').trim() || undefined,
      role: (props.roleFixed || '').trim() || undefined,
      status: (props.status || '').trim() || undefined,
    })
    items.value = (res?.items || []) as any
    total.value = Number((res as any)?.total || 0)
    totalPages.value = Number((res as any)?.totalPages || 1)
  } catch (e: any) {
    error.value = e?.message || 'Failed to load'
  } finally {
    loading.value = false
  }
}

async function onChangeRole(u: AdminUserListItem, v: string) {
  if (!props.allowRoleEdit) return
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

function openDetail(id: number) {
  if (props.mode === 'students') return router.push(`/admin/students/${id}`)
  if (props.mode === 'teachers') return router.push(`/admin/teachers/${id}`)
}

/**
 * 打开与指定用户的一对一聊天抽屉。
 */
function openChat(u: AdminUserListItem) {
  const name = String(u.nickname || u.username || `#${u.id}`)
  chat.openChat(u.id, name, null)
}

function openAudit(u: AdminUserListItem) {
  const name = String(u.nickname || u.username || `#${u.id}`)
  router.push({ path: `/admin/audit/${u.id}`, query: { name } })
}

// Create modal
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

watch(() => [props.keyword, props.status, props.roleFixed], () => {
  page.value = 1
  reload()
})

onMounted(reload)
</script>

