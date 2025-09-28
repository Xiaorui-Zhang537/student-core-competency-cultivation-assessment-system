import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { baseURL } from '@/api/config'
import notificationAPI from '@/api/notification.api'
import { studentApi } from '@/api/student.api'
import { teacherStudentApi } from '@/api/teacher-student.api'
import { teacherApi } from '@/api/teacher.api'
import { useAuthStore } from '@/stores/auth'
import chatApi from '@/api/chat.api'

export const useChatStore = defineStore('chat', () => {
  const isOpen = ref(false)
  const peerId = ref<string | number | null>(null)
  const peerName = ref<string | null>(null)
  const courseId = ref<string | number | null>(null)
  const recentConversations = ref<any[]>([])
  const contacts = ref<any[]>([])
  const contactGroups = ref<any[]>([])
  const loadingLists = ref(false)
  const systemMessages = ref<any[]>([])
  const pinned = ref<Array<{ peerId: string, courseId?: string }>>([])
  const suppressPreviewOnce = ref(false)
  // 未读消息总数（用于红点）
  const totalUnread = computed(() => {
    try {
      return (recentConversations.value || []).reduce((acc: number, c: any) => acc + (Number(c.unread || 0)), 0)
    } catch { return 0 }
  })

  // SSE：聊天事件连接状态
  const sseConnected = ref(false)
  let es: EventSource | null = null
  // 用户隔离的本地存储键：chat:ROLE:USERID:name
  const getStorageKey = (name: 'recent' | 'pinned') => {
    try {
      // 优先使用当前登录用户ID，避免不同用户共用同一 recent
      const auth = useAuthStore()
      const uid = String((auth.user as any)?.id ?? localStorage.getItem('userId') ?? '')
      const role = String(auth.userRole || '').toUpperCase()
      const roleTag = role ? role : 'GUEST'
      const idTag = uid || 'anon'
      return `chat:${roleTag}:${idTag}:${name}`
    } catch {
      return `chat:GUEST:anon:${name}`
    }
  }

  // 记录最近使用的存储键，检测用户切换时重置状态，避免“所有人共用最近”问题
  const lastStorageKey = ref<string>('')

  function openChat(id?: string | number | null, name: string | null = null, cId: string | number | null = null) {
    // 支持无参：仅打开抽屉，停留在列表态
    if (id !== undefined && id !== null && id !== '') {
      peerId.value = id
      peerName.value = name
      courseId.value = cId
    }
    isOpen.value = true
    try { console.debug('[chat] openChat -> isOpen:', isOpen.value, 'peerId:', peerId.value, 'courseId:', courseId.value) } catch {}
  }

  function setPeer(id: string | number, name: string | null = null, cId: string | number | null = null) {
    peerId.value = id
    peerName.value = name
    courseId.value = cId
    isOpen.value = true
  }

  function closeChat() {
    isOpen.value = false
    peerId.value = null
    peerName.value = null
    courseId.value = null
    try { console.debug('[chat] closeChat -> isOpen:', isOpen.value) } catch {}
  }

  async function removeRecent(targetPeerId: string | number, targetCourseId?: string | number | null) {
    const pid = String(targetPeerId)
    const cid = targetCourseId != null ? String(targetCourseId) : ''
    // 找到会话ID
    const item = (recentConversations.value || []).find((n: any) => String(n.peerId) === pid && (cid ? String(n.courseId||'') === cid : true))
    let convId = item?.id
    if (!convId) {
      try {
        const resp: any = await chatApi.getMyConversations({ page: 1, size: 200, archived: false })
        const items: any[] = (resp?.data?.items) || resp?.items || []
        const found = items.find((x: any) => String(x.peerId) === pid && (cid ? String(x.courseId||'') === cid : true))
        if (found) convId = found.id
      } catch {}
    }
    if (convId) {
      try {
        await chatApi.archiveConversation(convId, true)
      } catch {}
    }
    // 前端即时反馈
    recentConversations.value = (recentConversations.value || []).filter((n: any) => !(String(n.peerId) === pid && (cid ? String(n.courseId||'') === cid : true)))
    try {
      const key = getStorageKey('recent')
      const raw = localStorage.getItem(key)
      const arr: any[] = raw ? JSON.parse(raw) : []
      const next = arr.filter((x: any) => !(String(x.peerId) === pid && (cid ? String(x.courseId||'') === cid : true)))
      localStorage.setItem(key, JSON.stringify(next))
    } catch { /* ignore */ }
  }

  async function loadLists(opts?: { courseId?: string | number; keyword?: string }) {
    try {
      // 未登录：清空并直接返回，避免触发任何远端请求
      try {
        const auth = useAuthStore()
        if (!auth.isAuthenticated) {
          recentConversations.value = []
          contacts.value = []
          contactGroups.value = []
          systemMessages.value = []
          return
        }
      } catch {}
      // 用户隔离守卫：存储键变化则清空并重新加载
      try {
        const key = getStorageKey('recent')
        if (lastStorageKey.value && lastStorageKey.value !== key) {
          recentConversations.value = []
          contacts.value = []
          contactGroups.value = []
          pinned.value = []
        }
        lastStorageKey.value = key
      } catch {}
      loadingLists.value = true
      // 最近会话：改为服务端统一接口
      try {
        const resp: any = await chatApi.getMyConversations({ page: 1, size: 200, archived: false })
        const items: any[] = (resp?.data?.items) || resp?.items || []
        const mapped = items.map((x: any) => ({
          id: x.id, // 后端会话ID，用于归档/删除持久化
          peerId: String(x.peerId),
          peerName: String(x.peerUsername || ''),
          courseId: x.courseId ? String(x.courseId) : '',
          avatar: x.peerAvatar || '',
          content: x.lastContent || '',
          preview: x.lastContent || '',
          lastAt: x.lastMessageAt ? new Date(x.lastMessageAt).getTime() : 0,
          unread: Number(x.unread || 0)
        }))
        // 去重：教师端按 peerId+courseId 维度；学生端按 peerId
        const isStudent = (() => { try { return window.location.pathname.startsWith('/student') } catch { return false } })()
        const keyOf = (c: any) => isStudent ? String(c.peerId) : `${String(c.peerId)}|${String(c.courseId||'')}`
        const byPeer = new Map<string, any>()
        for (const c of mapped) {
          const key = keyOf(c)
          const exist = byPeer.get(key)
          if (!exist) {
            byPeer.set(key, { ...c })
          } else {
            // 若同一 peerId 出现多条，选择较新的并合并未读
            const merged = { ...exist }
            merged.unread = Number(exist.unread || 0) + Number(c.unread || 0)
            if ((c.lastAt || 0) > (exist.lastAt || 0)) {
              merged.lastAt = c.lastAt
              merged.content = c.content
              merged.preview = c.preview
              if (c.avatar) merged.avatar = c.avatar
              merged.courseId = c.courseId
              if (c.peerName) merged.peerName = c.peerName
              merged.id = c.id
            }
            byPeer.set(key, merged)
          }
        }
        let deduped = Array.from(byPeer.values())
        // 合并本地持久化的最近（作为兜底），避免后端聚合尚未覆盖时发完消息关闭再开看不到
        try {
          const key = getStorageKey('recent')
          const raw = localStorage.getItem(key)
          const localArr: any[] = raw ? JSON.parse(raw) : []
          for (const r of (localArr || [])) {
            const k = isStudent ? String(r.peerId) : `${String(r.peerId)}|${String(r.courseId||'')}`
            if (!byPeer.has(k)) {
              deduped.push({
                id: undefined,
                peerId: String(r.peerId),
                peerName: String(r.peerName || ''),
                courseId: isStudent ? '' : String(r.courseId || ''),
                avatar: '',
                content: String(r.content || r.preview || ''),
                preview: String(r.preview || r.content || ''),
                lastAt: Number(r.lastAt || Date.now()),
                unread: 0
              })
            }
          }
        } catch {}
        // 置顶与时间排序（置顶按相同维度）
        try {
          const rawPinned = localStorage.getItem(getStorageKey('pinned'))
          pinned.value = rawPinned ? JSON.parse(rawPinned) : []
        } catch { pinned.value = [] }
        const pinnedSet = new Set((pinned.value || []).map((x: any) => isStudent ? String(x.peerId) : `${String(x.peerId)}|${String(x.courseId||'')}`))
        deduped.sort((a: any, b: any) => (b.lastAt || 0) - (a.lastAt || 0))
        // 若当前激活会话未出现在后端返回中，尝试从本地 recent 合并一个临时占位，避免发送后刷新丢失
        try {
          const activePid = String(peerId.value || '')
          const activeCid = String(courseId.value || '')
          const present = deduped.some((c: any) => (isStudent ? String(c.peerId) === activePid : (String(c.peerId) === activePid && String(c.courseId||'') === activeCid)))
          if (!present && activePid) {
            const key = getStorageKey('recent')
            const raw = localStorage.getItem(key)
            const arr: any[] = raw ? JSON.parse(raw) : []
            const found = arr.find((x: any) => (isStudent ? String(x.peerId) === activePid : (String(x.peerId) === activePid && String(x.courseId||'') === activeCid))) || {}
            const synthetic = {
              id: undefined,
              peerId: activePid,
              peerName: String(peerName.value || found.peerName || ''),
              courseId: isStudent ? '' : activeCid,
              avatar: '',
              content: String(found.content || ''),
              preview: String(found.preview || found.content || ''),
              lastAt: Date.now(),
              unread: 0
            }
            deduped = [synthetic, ...deduped]
          }
        } catch {}

        const pinnedConvs = deduped.filter(c => pinnedSet.has(keyOf(c)))
        const normalConvs = deduped.filter(c => !pinnedSet.has(keyOf(c)))
        recentConversations.value = [...pinnedConvs, ...normalConvs]
        suppressPreviewOnce.value = false
      } catch {
        recentConversations.value = []
      }
      // 系统消息（非聊天）：单独缓存，供系统消息标签页使用
      try {
        const resAll: any = await notificationAPI.getMyNotifications({ page: 1, size: 100 })
        const all = (resAll as any)?.items || (resAll?.data?.items) || []
        const toLower = (v: any) => String(v || '').toLowerCase()
        systemMessages.value = all.filter((n: any) => toLower(n.type) !== 'message')
      } catch { systemMessages.value = [] }

      // 联系人分支：按角色加载
      contacts.value = []
      contactGroups.value = []
      try {
        const auth = useAuthStore()
        const role = String(auth.userRole || '').toUpperCase()
        const groups: any[] = []
        if (role === 'STUDENT') {
          const resMyCourses: any = await (studentApi as any).getMyCourses({ page: 1, size: 1000 })
          const myCourses = (resMyCourses?.data?.items?.items) || (resMyCourses?.data?.items) || (resMyCourses?.items) || resMyCourses || []
          for (const c of myCourses) {
            const cId = String(c.id || c.courseId || '')
            const cName = c.title || c.name || `Course ${cId}`
            try {
              const participantsRes: any = await (studentApi as any).getCourseParticipants(cId, opts?.keyword)
              const teachers = ((participantsRes?.data?.teachers) || (participantsRes?.teachers) || []).map((t: any) => ({ id: String(t.id), name: (t.nickname || t.name || t.username || t.userName), nickname: t.nickname || null, username: t.username || t.userName || null, avatar: t.avatar }))
              const classmates = ((participantsRes?.data?.classmates) || (participantsRes?.classmates) || []).map((s: any) => ({ id: String(s.id), name: (s.nickname || s.name || s.username || s.userName), nickname: s.nickname || null, username: s.username || s.userName || null, avatar: s.avatar }))
              const members = [...teachers, ...classmates].filter((m: any) => String(m.id) !== String(localStorage.getItem('userId') || ''))
              if (members.length === 0) continue
              groups.push({ courseId: cId, courseName: cName, expanded: true, students: members })
              contacts.value.push(...members)
            } catch { /* ignore one course */ }
          }
        } else {
          // 教师端：优先一次性聚合接口 /teachers/contacts；失败则回退“我的课程+逐课学生”
          const assignedCourseId = String(opts?.courseId || courseId.value || '')
          let usedAggregate = false
          try {
            const resp: any = await (teacherApi as any).getContacts({ keyword: opts?.keyword })
            const aggGroups = (resp?.data?.groups) || (resp?.groups) || []
            const filtered = assignedCourseId ? aggGroups.filter((g: any) => String(g.courseId) === assignedCourseId) : aggGroups
            for (const g of filtered) {
              const students = ((g?.students) || []).map((s: any) => ({ id: String(s.id), name: (s.nickname || s.displayName || s.name || s.username || `#${s.id}`), nickname: s.nickname || null, username: s.username || null, avatar: s.avatar }))
              if (students.length === 0) continue
              groups.push({ courseId: String(g.courseId), courseName: g.courseTitle || `Course ${g.courseId}`, expanded: !!assignedCourseId || groups.length === 0, students })
              contacts.value.push(...students)
            }
            usedAggregate = groups.length > 0
          } catch { /* fall back */ }

          if (!usedAggregate) {
            // 回退方案：我的课程→逐课学生
            let teacherCourses: any[] = []
            try {
              if (assignedCourseId) {
                teacherCourses = [{ id: assignedCourseId, title: `Course ${assignedCourseId}` }]
              } else {
                const myCoursesRes: any = await (teacherApi as any).getMyCourses()
                teacherCourses = (myCoursesRes?.data) || (myCoursesRes?.items) || []
              }
            } catch { teacherCourses = [] }

            for (const c of teacherCourses) {
              const cId = String(c.id || c.courseId || '')
              if (!cId) continue
              const cName = c.title || c.name || `Course ${cId}`
              try {
                const basicRes: any = await (teacherStudentApi as any).getCourseStudentsBasic(cId, 1, 10000, opts?.keyword)
                const students = ((basicRes?.items) || (basicRes?.data?.items) || []).map((s: any) => ({ id: String(s.id || s.studentId || s.student_id), name: (s.nickname || s.name || s.username || s.userName || `#${s.id}`), nickname: s.nickname || null, username: s.username || s.userName || null, avatar: s.avatar }))
                if (students.length === 0) continue
                groups.push({ courseId: cId, courseName: cName, expanded: !!assignedCourseId || groups.length === 0, students })
                contacts.value.push(...students)
              } catch { /* per-course ignore */ }
            }
          }
        }
        contactGroups.value = groups
      } catch { /* ignore all */ }
    } finally {
      loadingLists.value = false
    }
    // 在首次加载后确保 SSE 已连接
    try { if (!sseConnected.value) connectChatSse() } catch {}
  }

  function togglePin(targetPeerId: string | number, targetCourseId?: string | number | null) {
    const pid = String(targetPeerId)
    const cid = targetCourseId != null ? String(targetCourseId) : ''
    try {
      const raw = localStorage.getItem(getStorageKey('pinned'))
      const arr: any[] = raw ? JSON.parse(raw) : []
      const idx = arr.findIndex((x: any) => String(x.peerId) === pid && String(x.courseId||'') === cid)
      if (idx >= 0) { arr.splice(idx, 1) } else { arr.unshift({ peerId: pid, courseId: cid }) }
      localStorage.setItem(getStorageKey('pinned'), JSON.stringify(arr.slice(0, 100)))
      pinned.value = arr
      // 前端重排（新结构）
      const keyOf = (id: any, c: any) => `${String(id)}|${String(c||'')}`
      const set = new Set(arr.map((x: any) => keyOf(x.peerId, x.courseId)))
      const list = (recentConversations.value || []).slice()
      list.sort((a: any, b: any) => {
        const ap = set.has(keyOf(a.peerId, a.courseId))
        const bp = set.has(keyOf(b.peerId, b.courseId))
        if (ap !== bp) return ap ? -1 : 1
        return (b.lastAt || 0) - (a.lastAt || 0)
      })
      recentConversations.value = list
    } catch { /* ignore */ }
  }

  function markPeerRead(targetPeerId: string | number, targetCourseId?: string | number | null) {
    const pid = String(targetPeerId)
    const cid = targetCourseId != null ? String(targetCourseId) : ''
    const list = (recentConversations.value || []).slice()
    for (const item of list) {
      const samePeer = String(item.peerId) === pid
      const sameCourse = cid ? String(item.courseId || '') === cid : true
      if (samePeer && sameCourse) {
        item.unread = 0
      }
    }
    recentConversations.value = list
  }

  function isPinned(targetPeerId: string | number, targetCourseId?: string | number | null) {
    const pid = String(targetPeerId)
    const cid = targetCourseId != null ? String(targetCourseId) : ''
    return (pinned.value || []).some(x => String(x.peerId) === pid && String(x.courseId||'') === cid)
  }

  function upsertRecentAfterSend(targetPeerId: string | number, targetCourseId?: string | number | null, latestContent?: string, latestPreview?: string, targetPeerName?: string, conversationId?: string | number) {
    const pid = String(targetPeerId)
    const cid = targetCourseId != null ? String(targetCourseId) : ''
    const now = Date.now()
    const list = (recentConversations.value || []).slice()
    const isStudent = (() => { try { return window.location.pathname.startsWith('/student') } catch { return false } })()
    let item = list.find((n: any) => String(n.peerId) === pid && (isStudent ? true : String(n.courseId || '') === cid))
    if (!item) {
      item = { id: conversationId, peerId: pid, courseId: isStudent ? '' : cid, peerName: targetPeerName || '', avatar: '', unread: 0, content: '', preview: '', lastAt: 0 }
      list.push(item)
    }
    if (conversationId && !item.id) item.id = conversationId
    if (latestContent) item.content = latestContent
    if (latestPreview) item.preview = latestPreview
    item.lastAt = now
    item.unread = 0
    // 持久化到本地，便于重新打开抽屉时恢复
    try {
      const key = getStorageKey('recent')
      const raw = localStorage.getItem(key)
      const arr: any[] = raw ? JSON.parse(raw) : []
      const idx = arr.findIndex((x: any) => String(x.peerId) === pid && (isStudent ? true : String(x.courseId||'') === cid))
      const rec = { peerId: pid, courseId: isStudent ? '' : cid, peerName: item.peerName || targetPeerName || '', content: item.content, preview: item.preview, lastAt: now }
      if (idx >= 0) arr.splice(idx, 1)
      arr.unshift(rec)
      localStorage.setItem(key, JSON.stringify(arr.slice(0, 100)))
    } catch { /* ignore */ }
    // 排序：置顶优先 + 最近时间倒序
    const keyOf = (id: any, c: any) => {
      const studentMode = (() => { try { return window.location.pathname.startsWith('/student') } catch { return false } })()
      return `${String(id)}|${studentMode ? '' : String(c||'')}`
    }
    const set = new Set((pinned.value || []).map((x: any) => keyOf(x.peerId, x.courseId)))
    list.sort((a: any, b: any) => {
      const ap = set.has(keyOf(a.peerId, a.courseId))
      const bp = set.has(keyOf(b.peerId, b.courseId))
      if (ap !== bp) return ap ? -1 : 1
      return (b.lastAt || 0) - (a.lastAt || 0)
    })
    recentConversations.value = list
  }

  // 收到新消息时更新最近会话与未读，并向窗口广播事件以便 ChatDrawer 感知
  function applyIncomingMessage(fromPeerId: string | number, course: string | number | null, content: string, peerName?: string) {
    const pid = String(fromPeerId)
    const cid = course != null ? String(course) : ''
    const list = (recentConversations.value || []).slice()
    let item = list.find((n: any) => String(n.peerId) === pid)
    if (!item) {
      item = { peerId: pid, courseId: cid, peerName: peerName || '', avatar: '', unread: 0, content: '', preview: '', lastAt: 0 }
      list.unshift(item)
    }
    item.content = content
    item.preview = content
    item.lastAt = Date.now()
    item.unread = Number(item.unread || 0) + 1
    recentConversations.value = list
    try { window.dispatchEvent(new CustomEvent('chat:new-message', { detail: { peerId: pid, courseId: cid, content } })) } catch {}
  }

  function connectChatSse() {
    if (sseConnected.value || es) return
    const token = (() => {
      try { return (localStorage.getItem('token') || '').replace(/^Bearer\s+/i, '') } catch { return '' }
    })()
    if (!token) return
    const url = `${baseURL.replace(/\/$/, '')}/notifications/stream?token=${encodeURIComponent(token)}`
    try {
      es = new EventSource(url, { withCredentials: true })
    } catch { es = null; return }
    sseConnected.value = true
    es.addEventListener('new', (ev: MessageEvent) => {
      try {
        const payload = ev?.data ? JSON.parse(ev.data) : null
        if (!payload || typeof payload !== 'object') return
        // 仅处理聊天消息
        const t = String(payload.type || '').toLowerCase()
        if (t !== 'message') return
        const senderId = payload.senderId ?? payload.sender_id
        const peerId = senderId != null ? String(senderId) : ''
        const course = payload.relatedId ?? payload.related_id ?? ''
        const content = String(payload.content || '')
        const name = payload.data ? (function () { try { const d = typeof payload.data === 'string' ? JSON.parse(payload.data) : payload.data; return d?.senderName || d?.username || '' } catch { return '' } })() : ''
        if (peerId) applyIncomingMessage(peerId, course ? String(course) : '', content, name)
      } catch {}
    })
    es.onerror = () => {
      try { es?.close() } catch {}
      es = null
      sseConnected.value = false
      // 退避重连
      setTimeout(() => { try { connectChatSse() } catch {} }, 3000)
    }
  }

  function toggleContactGroup(targetCourseId: string | number) {
    const cid = String(targetCourseId)
    const grp = (contactGroups.value || []).find((g: any) => String(g.courseId) === cid)
    if (grp) grp.expanded = !grp.expanded
  }

  function suppressNextPreviewUpdateOnce() {
    suppressPreviewOnce.value = true
  }

  return { isOpen, peerId, peerName, courseId, recentConversations, contacts, contactGroups, systemMessages, loadingLists, totalUnread, sseConnected, openChat, setPeer, closeChat, loadLists, removeRecent, togglePin, isPinned, upsertRecentAfterSend, toggleContactGroup, suppressNextPreviewUpdateOnce, markPeerRead, connectChatSse }
})


