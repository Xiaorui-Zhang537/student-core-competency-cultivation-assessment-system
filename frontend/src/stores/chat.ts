import { defineStore } from 'pinia'
import { ref } from 'vue'
import notificationAPI from '@/api/notification.api'
import { studentApi } from '@/api/student.api'

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

  function openChat(id?: string | number | null, name: string | null = null, cId: string | number | null = null) {
    // 支持无参：仅打开抽屉，停留在列表态
    if (id !== undefined && id !== null && id !== '') {
      peerId.value = id
      peerName.value = name
      courseId.value = cId
    }
    isOpen.value = true
    // 记录到本地最近会话（轻量缓存）
    try {
      const key = 'chat:recent'
      const raw = localStorage.getItem(key)
      let arr: any[] = []
      try { arr = raw ? JSON.parse(raw) : [] } catch { arr = [] }
      const pid = id != null ? String(id) : ''
      if (pid) {
        const existingIdx = arr.findIndex((x: any) => String(x.peerId) === pid && String(x.courseId||'') === String(cId||''))
        const item = { peerId: pid, peerName: name || '', courseId: cId ? String(cId) : '' }
        if (existingIdx >= 0) arr.splice(existingIdx, 1)
        arr.unshift(item)
        localStorage.setItem(key, JSON.stringify(arr.slice(0, 50)))
      }
    } catch { /* ignore */ }
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
      const key = 'chat:recent'
      const raw = localStorage.getItem(key)
      const arr: any[] = raw ? JSON.parse(raw) : []
      const next = arr.filter((x: any) => !(String(x.peerId) === pid && (cid ? String(x.courseId||'') === cid : true)))
      localStorage.setItem(key, JSON.stringify(next))
    } catch { /* ignore */ }
  }

  async function loadLists(opts?: { courseId?: string | number; keyword?: string }) {
    try {
      loadingLists.value = true
      // 最近会话：复用我的消息通知（message 类型），前端可二次聚合
      try {
        // 仅获取聊天消息，不包含帖子评论等
        const res: any = await notificationAPI.getMyNotifications({ page: 1, size: 500, type: 'message' })
        const items = (res as any)?.items || (res?.data?.items) || []
        let merged = items.slice()

        // 合并本地最近，以保证从其他入口打开的会话也能出现
        try {
          const raw = localStorage.getItem('chat:recent')
          const localArr: any[] = raw ? JSON.parse(raw) : []
          for (const r of localArr) {
            const exists = merged.some((n: any) => (
              String(n.senderId) === String(r.peerId) || String(n.recipientId) === String(r.peerId)
            ) && (String(r.courseId||'') === '' || String(r.courseId||'') === String(n.relatedId || n.courseId || '')))
            if (!exists) {
              merged.unshift({
                senderId: r.peerId,
                recipientId: null,
                relatedId: r.courseId || null,
                type: 'message',
                category: 'chat',
                title: r.peerName || '',
                content: '',
                data: null,
                createdAt: '1970-01-01T00:00:00.000Z'
              })
            }
          }
        } catch { /* ignore */ }

        // 仅保留聊天项，过滤帖子/社区/评论等
        const toLower = (v: any) => String(v || '').toLowerCase()
        merged = merged.filter((n: any) => {
          const typeOk = toLower(n.type) === 'message'
          const hasPeer = n.senderId != null || n.recipientId != null
          const related = toLower(n.relatedType)
          const category = toLower(n.category)
          const title = toLower(n.title)
          const content = toLower(n.content)
          const actionUrl = toLower((n as any).actionUrl)
          const isForum = ['post','community','comment','reply','forum','discussion'].some(k => related.includes(k) || category.includes(k))
            || ['post','comment','reply','discussion','论坛','帖子','回复','评论','社区'].some(k => title.includes(k) || content.includes(k))
            || actionUrl.includes('/community') || actionUrl.includes('/posts')
          return typeOk && hasPeer && !isForum
        })

        // 聚合为“会话”，并计算最新时间与未读
        const myId = String(localStorage.getItem('userId') || '')
        const convMap = new Map<string, any>()
        for (const n of merged) {
          const createdAt = n.createdAt || n.created_at || n.createdTime || n.created_time || 0
          const createdTs = createdAt ? new Date(createdAt).getTime() : 0
          const peer = String(n.senderId) === myId ? n.recipientId : n.senderId
          const pid = peer != null ? String(peer) : ''
          if (!pid) continue
          const cid = n.relatedId || n.courseId || ''
          const key = `${pid}|${String(cid||'')}`
          const data = (() => { try { return typeof n.data === 'string' ? JSON.parse(n.data) : (n.data || null) } catch { return null } })()
          const avatar = (data && (data.avatar || data.studentAvatar || data.student_avatar)) || ''
          const name = n.senderName || n.peerName || (data && (data.senderName || data.studentName || data.name || data.sender_name || data.student_name)) || n.title || ''
          const unreadInc = (n.isRead === false && String(n.recipientId) === myId) ? 1 : 0
          const content = n.content || ''
          const preview = n.title || n.content || ''
          const exist = convMap.get(key)
          if (!exist) {
            convMap.set(key, {
              peerId: pid,
              peerName: name,
              courseId: cid ? String(cid) : '',
              avatar,
              content,
              preview,
              lastAt: createdTs,
              unread: unreadInc
            })
          } else {
            // 更新最新消息与未读计数
            exist.unread += unreadInc
            if (createdTs > exist.lastAt) {
              exist.lastAt = createdTs
              exist.content = content
              exist.preview = preview
              if (name) exist.peerName = exist.peerName || name
              if (avatar) exist.avatar = exist.avatar || avatar
            }
          }
        }

        // 合并本地最近持久化（保留最新的 lastAt/content/preview）
        try {
          const rawLocal = localStorage.getItem('chat:recent')
          const localArr: any[] = rawLocal ? JSON.parse(rawLocal) : []
          for (const lr of localArr) {
            const pid = String(lr.peerId || '')
            const cid = String(lr.courseId || '')
            if (!pid) continue
            const key = `${pid}|${cid}`
            const exist = convMap.get(key)
            const lrLastAt = Number(lr.lastAt || 0)
            if (exist) {
              if (lrLastAt && lrLastAt > (exist.lastAt || 0)) {
                exist.lastAt = lrLastAt
                if (lr.content) exist.content = lr.content
                if (lr.preview) exist.preview = lr.preview
              }
              if (lr.peerName && !exist.peerName) exist.peerName = lr.peerName
            } else {
              convMap.set(key, {
                peerId: pid,
                courseId: cid,
                peerName: lr.peerName || '',
                avatar: '',
                content: lr.content || lr.preview || '',
                preview: lr.preview || lr.content || '',
                lastAt: lrLastAt || 0,
                unread: 0
              })
            }
          }
        } catch { /* ignore */ }

        // 置顶与时间排序
        try {
          const rawPinned = localStorage.getItem('chat:pinned')
          pinned.value = rawPinned ? JSON.parse(rawPinned) : []
        } catch { pinned.value = [] }
        const keyOf = (id: any, cid: any) => `${String(id)}|${String(cid||'')}`
        const pinnedSet = new Set((pinned.value||[]).map(x => keyOf(x.peerId, x.courseId)))
        const allConvs = Array.from(convMap.values())
        allConvs.sort((a: any, b: any) => (b.lastAt || 0) - (a.lastAt || 0))
        const pinnedConvs = allConvs.filter(c => pinnedSet.has(keyOf(c.peerId, c.courseId)))
        const normalConvs = allConvs.filter(c => !pinnedSet.has(keyOf(c.peerId, c.courseId)))
        recentConversations.value = [...pinnedConvs, ...normalConvs]
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

      // 联系人分支：学生端仅显示“我加入的课程”的参与者（任课教师 + 同班同学）
      contacts.value = []
      contactGroups.value = []
      try {
        // 获取我加入的课程（学生端接口）
        const resMyCourses: any = await (studentApi as any).getMyCourses({ page: 1, size: 1000 })
        const myCourses = (resMyCourses?.data?.items?.items) || (resMyCourses?.data?.items) || (resMyCourses?.items) || resMyCourses || []
        const groups: any[] = []
        for (const c of myCourses) {
          const cId = String(c.id || c.courseId || '')
          const cName = c.title || c.name || `Course ${cId}`
          try {
            // 从学生端统一接口获取课程参与者
            const participantsRes: any = await (studentApi as any).getCourseParticipants(cId, opts?.keyword)
            const teachers = ((participantsRes?.data?.teachers) || (participantsRes?.teachers) || []).map((t: any) => ({ id: String(t.id), name: t.name, avatar: t.avatar }))
            const classmates = ((participantsRes?.data?.classmates) || (participantsRes?.classmates) || []).map((s: any) => ({ id: String(s.id), name: s.name, avatar: s.avatar }))
            const members = [...teachers, ...classmates].filter((m: any) => String(m.id) !== String(localStorage.getItem('userId') || ''))
            if (members.length === 0) continue
            groups.push({ courseId: cId, courseName: cName, expanded: true, students: members })
            contacts.value.push(...members)
          } catch { /* ignore one course */ }
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
      const raw = localStorage.getItem('chat:pinned')
      const arr: any[] = raw ? JSON.parse(raw) : []
      const idx = arr.findIndex((x: any) => String(x.peerId) === pid && String(x.courseId||'') === cid)
      if (idx >= 0) { arr.splice(idx, 1) } else { arr.unshift({ peerId: pid, courseId: cid }) }
      localStorage.setItem('chat:pinned', JSON.stringify(arr.slice(0, 100)))
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
    let item = list.find((n: any) => String(n.peerId) === pid && String(n.courseId || '') === cid)
    if (!item) {
      item = { peerId: pid, courseId: cid, peerName: targetPeerName || '', avatar: '', unread: 0, content: '', preview: '', lastAt: 0 }
      list.push(item)
    }
    if (latestContent) item.content = latestContent
    if (latestPreview) item.preview = latestPreview
    item.lastAt = now
    item.unread = 0
    // 持久化到本地，便于重新打开抽屉时恢复
    try {
      const key = 'chat:recent'
      const raw = localStorage.getItem(key)
      const arr: any[] = raw ? JSON.parse(raw) : []
      const idx = arr.findIndex((x: any) => String(x.peerId) === pid && String(x.courseId||'') === cid)
      const rec = { peerId: pid, courseId: cid, peerName: item.peerName || targetPeerName || '', content: item.content, preview: item.preview, lastAt: now }
      if (idx >= 0) arr.splice(idx, 1)
      arr.unshift(rec)
      localStorage.setItem(key, JSON.stringify(arr.slice(0, 100)))
    } catch { /* ignore */ }
    // 排序：置顶优先 + 最近时间倒序
    const keyOf = (id: any, c: any) => `${String(id)}|${String(c||'')}`
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

  return { isOpen, peerId, peerName, courseId, recentConversations, contacts, contactGroups, systemMessages, loadingLists, openChat, setPeer, closeChat, loadLists, removeRecent, togglePin, isPinned, upsertRecentAfterSend, toggleContactGroup }
})


