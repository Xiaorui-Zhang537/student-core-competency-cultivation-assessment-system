<template>
  <div class="p-6">
    <PageHeader :title="t('admin.sidebar.users')" :subtitle="t('admin.title')">
      <template #actions>
        <Button variant="primary" @click="openCreate">
          <PlusIcon class="w-4 h-4 mr-2" />
          {{ t('common.create') || '创建' }}
        </Button>
      </template>
    </PageHeader>

    <Card padding="md" tint="secondary" class="mt-4">
      <div class="flex flex-col md:flex-row gap-3 md:items-center">
        <GlassSearchInput v-model="keyword" :placeholder="String(t('common.search') || '搜索用户名/邮箱/学号/工号')" size="sm" tint="secondary" />
        <div class="flex gap-2 flex-wrap">
          <GlassPopoverSelect v-model="role" :options="roleOptions" size="sm" width="140px" />
          <GlassPopoverSelect v-model="status" :options="statusOptions" size="sm" width="140px" />
          <Button size="sm" variant="outline" :disabled="loading" @click="reload">{{ String(t('common.search') || '查询') }}</Button>
        </div>
      </div>
    </Card>

    <loading-overlay v-if="loading" class="mt-4" :text="String(t('common.loading') || '加载中…')" />
    <ErrorState
      v-else-if="error"
      class="mt-4"
      :title="String(t('common.error') || '加载失败')"
      :message="error"
      :actionText="String(t('common.retry') || '重试')"
      @action="reload"
    />

    <Card v-else padding="md" tint="secondary" class="mt-4">
      <div class="overflow-x-auto">
        <table class="table w-full">
          <thead>
            <tr>
              <th>ID</th>
              <th>User</th>
              <th>Role</th>
              <th>Status</th>
              <th class="text-right">Actions</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="u in items" :key="u.id">
              <td class="font-mono text-xs">{{ u.id }}</td>
              <td>
                <div class="font-medium">{{ u.nickname || u.username }}</div>
                <div class="text-xs text-subtle">{{ u.email }}</div>
                <div class="text-xs text-subtle" v-if="u.studentNo || u.teacherNo">{{ u.studentNo || u.teacherNo }}</div>
              </td>
              <td class="w-40">
                <GlassPopoverSelect
                  :model-value="u.role"
                  :options="roleEditOptions"
                  size="sm"
                  @update:modelValue="(v:any) => onChangeRole(u, v)"
                />
              </td>
              <td class="w-40">
                <GlassPopoverSelect
                  :model-value="u.status"
                  :options="statusEditOptions"
                  size="sm"
                  @update:modelValue="(v:any) => onChangeStatus(u, v)"
                />
              </td>
              <td class="text-right">
                <Button size="sm" variant="outline" :disabled="opLoadingId===u.id" @click="sendReset(u)">
                  {{ t('auth.forgotPassword.send') || '重置密码' }}
                </Button>
              </td>
            </tr>
            <tr v-if="items.length === 0">
              <td colspan="5">
                <EmptyState :title="String(t('common.empty') || '暂无数据')" />
              </td>
            </tr>
          </tbody>
        </table>
      </div>

      <PaginationBar
        :page="page"
        :page-size="pageSize"
        :total-items="total"
        :total-pages="totalPages"
        :disabled="loading"
        @update:page="(v: number) => { page = v; reload() }"
        @update:pageSize="(v: number) => { pageSize = v; page = 1; reload() }"
      />
    </Card>

    <GlassModal v-if="showCreate" :title="String(t('common.create') || '创建用户')" size="md" heightVariant="tall" @close="closeCreate">
      <form class="space-y-3" @submit.prevent="create">
        <div class="grid grid-cols-1 md:grid-cols-2 gap-3">
          <div>
            <label class="block text-sm font-medium mb-1">Username</label>
            <GlassInput v-model="form.username" />
          </div>
          <div>
            <label class="block text-sm font-medium mb-1">Email</label>
            <GlassInput v-model="form.email" type="email" />
          </div>
          <div>
            <label class="block text-sm font-medium mb-1">Password</label>
            <GlassInput v-model="form.password" type="password" />
          </div>
          <div>
            <label class="block text-sm font-medium mb-1">Role</label>
            <GlassPopoverSelect v-model="form.role" :options="roleCreateOptions" size="sm" />
          </div>
          <div>
            <label class="block text-sm font-medium mb-1">Status</label>
            <GlassPopoverSelect v-model="form.status" :options="statusEditOptions" size="sm" />
          </div>
          <div>
            <label class="block text-sm font-medium mb-1">Nickname</label>
            <GlassInput v-model="form.nickname" />
          </div>
          <div>
            <label class="block text-sm font-medium mb-1">StudentNo</label>
            <GlassInput v-model="form.studentNo" />
          </div>
          <div>
            <label class="block text-sm font-medium mb-1">TeacherNo</label>
            <GlassInput v-model="form.teacherNo" />
          </div>
        </div>

        <div class="flex justify-end gap-2 pt-2">
          <Button variant="ghost" type="button" @click="closeCreate">{{ t('common.cancel') || '取消' }}</Button>
          <Button variant="primary" type="submit" :disabled="createLoading">{{ t('common.confirm') || '确认' }}</Button>
        </div>
      </form>
    </GlassModal>
  </div>
</template>

<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
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

const roleOptions = [
  { label: 'All roles', value: '' },
  { label: 'student', value: 'student' },
  { label: 'teacher', value: 'teacher' },
  { label: 'admin', value: 'admin' },
]
const statusOptions = [
  { label: 'All status', value: '' },
  { label: 'active', value: 'active' },
  { label: 'disabled', value: 'disabled' },
]
const roleEditOptions = [
  { label: 'student', value: 'student' },
  { label: 'teacher', value: 'teacher' },
  { label: 'admin', value: 'admin' },
]
const roleCreateOptions = roleEditOptions
const statusEditOptions = [
  { label: 'active', value: 'active' },
  { label: 'disabled', value: 'disabled' },
]

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

