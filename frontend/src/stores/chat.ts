import { defineStore } from 'pinia'
import { ref } from 'vue'
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
  // 用户隔离的本地存储键：chat:ROLE:USERID:name
  const getStorageKey = (name: 'recent' | 'pinned') => {
    try {
      const uid = String(localStorage.getItem('userId') || '')
      const auth = useAuthStore()
      const role = String(auth.userRole || '').toUpperCase()
      const roleTag = role ? role : 'GUEST'
      const idTag = uid || 'anon'
      return `chat:${roleTag}:${idTag}:${name}`
    } catch {
      return `chat:GUEST:anon:${name}`
    }
  }

  function openChat(id?: string | number | null, name: string | null = null, cId: string | number | null = null) {
    // 支持无参：仅打开抽屉，停留在列表态
    if (id !== undefined && id !== null && id !== '') {
      peerId.value = id
      peerName.value = name
      courseId.value = cId
    }
    isOpen.value = true
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
  }

  function removeRecent(targetPeerId: string | number, targetCourseId?: string | number | null) {
    const pid = String(targetPeerId)
    const cid = targetCourseId != null ? String(targetCourseId) : ''
    // 更新内存列表（新结构：peerId/courseId）
    recentConversations.value = (recentConversations.value || []).filter((n: any) => {
      const isSamePeer = String(n.peerId) === pid
      const courseMatch = cid ? (String(n.courseId || '') === cid) : true
      return !(isSamePeer && courseMatch)
    })
    // 更新本地持久化
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
      loadingLists.value = true
      // 最近会话：改为服务端统一接口
      try {
        const resp: any = await chatApi.getMyConversations({ page: 1, size: 200 })
        const items: any[] = (resp?.data?.items) || resp?.items || []
        const mapped = items.map((x: any) => ({
          peerId: String(x.peerId),
          peerName: String(x.peerUsername || ''),
          courseId: x.courseId ? String(x.courseId) : '',
          avatar: x.peerAvatar || '',
          content: x.lastContent || '',
          preview: x.lastContent || '',
          lastAt: x.lastMessageAt ? new Date(x.lastMessageAt).getTime() : 0,
          unread: Number(x.unread || 0)
        }))
        // 去重：同一 username 只显示一条（选取最近一条，未读累加）
        const byUser = new Map<string, any>()
        for (const c of mapped) {
          const key = (c.peerName || String(c.peerId)).toLowerCase()
          const exist = byUser.get(key)
          if (!exist) {
            byUser.set(key, { ...c })
          } else {
            exist.unread = Number(exist.unread || 0) + Number(c.unread || 0)
            if ((c.lastAt || 0) > (exist.lastAt || 0)) {
              exist.lastAt = c.lastAt
              exist.content = c.content
              exist.preview = c.preview
              if (c.avatar) exist.avatar = c.avatar
              exist.courseId = c.courseId
              exist.peerId = c.peerId
              if (c.peerName) exist.peerName = c.peerName
            }
          }
        }
        const deduped = Array.from(byUser.values())
        // 置顶与时间排序
        try {
          const rawPinned = localStorage.getItem(getStorageKey('pinned'))
          pinned.value = rawPinned ? JSON.parse(rawPinned) : []
        } catch { pinned.value = [] }
        // 置顶按 peerId 维度判断（与去重一致）
        const pinnedPeerIds = new Set((pinned.value || []).map((x: any) => String(x.peerId)))
        deduped.sort((a: any, b: any) => (b.lastAt || 0) - (a.lastAt || 0))
        const pinnedConvs = deduped.filter(c => pinnedPeerIds.has(String(c.peerId)))
        const normalConvs = deduped.filter(c => !pinnedPeerIds.has(String(c.peerId)))
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
              const teachers = ((participantsRes?.data?.teachers) || (participantsRes?.teachers) || []).map((t: any) => ({ id: String(t.id), name: (t.username || t.userName || t.name), avatar: t.avatar }))
              const classmates = ((participantsRes?.data?.classmates) || (participantsRes?.classmates) || []).map((s: any) => ({ id: String(s.id), name: (s.username || s.userName || s.name), avatar: s.avatar }))
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
              const students = ((g?.students) || []).map((s: any) => ({ id: String(s.id), name: s.username || `#${s.id}` , avatar: s.avatar }))
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
                const students = ((basicRes?.items) || (basicRes?.data?.items) || []).map((s: any) => ({ id: String(s.id || s.studentId || s.student_id), name: s.username || s.userName || s.name || s.nickname || `#${s.id}`, avatar: s.avatar }))
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

  function isPinned(targetPeerId: string | number, targetCourseId?: string | number | null) {
    const pid = String(targetPeerId)
    const cid = targetCourseId != null ? String(targetCourseId) : ''
    return (pinned.value || []).some(x => String(x.peerId) === pid && String(x.courseId||'') === cid)
  }

  function upsertRecentAfterSend(targetPeerId: string | number, targetCourseId?: string | number | null, latestContent?: string, latestPreview?: string, targetPeerName?: string) {
    const pid = String(targetPeerId)
    const cid = targetCourseId != null ? String(targetCourseId) : ''
    const now = Date.now()
    const list = (recentConversations.value || []).slice()
    const isStudent = (() => { try { return window.location.pathname.startsWith('/student') } catch { return false } })()
    let item = list.find((n: any) => String(n.peerId) === pid && (isStudent ? true : String(n.courseId || '') === cid))
    if (!item) {
      item = { peerId: pid, courseId: isStudent ? '' : cid, peerName: targetPeerName || '', avatar: '', unread: 0, content: '', preview: '', lastAt: 0 }
      list.push(item)
    }
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

  function toggleContactGroup(targetCourseId: string | number) {
    const cid = String(targetCourseId)
    const grp = (contactGroups.value || []).find((g: any) => String(g.courseId) === cid)
    if (grp) grp.expanded = !grp.expanded
  }

  function suppressNextPreviewUpdateOnce() {
    suppressPreviewOnce.value = true
  }

  return { isOpen, peerId, peerName, courseId, recentConversations, contacts, contactGroups, systemMessages, loadingLists, openChat, setPeer, closeChat, loadLists, removeRecent, togglePin, isPinned, upsertRecentAfterSend, toggleContactGroup, suppressNextPreviewUpdateOnce }
})


