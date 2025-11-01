// Unified display-name resolver for users
// Priority: lastName + firstName > nickname > username/name/id

export function resolveUserDisplayName(user: any): string {
  try {
    if (!user) return ''
    const lastName = user?.lastName ?? user?.last_name ?? user?.surname ?? ''
    const firstName = user?.firstName ?? user?.first_name ?? ''
    const lf = [lastName, firstName].filter((s: any) => String(s || '').trim().length > 0).join('')
    if (lf) return String(lf)
    const nickname = user?.nickname ?? user?.nickName ?? user?.displayName ?? user?.display_name
    if (nickname) return String(nickname)
    const nameField = user?.name
    if (nameField && String(nameField).trim().length > 0) return String(nameField)
    const username = user?.username ?? user?.userName ?? user?.name ?? user?.studentName ?? user?.teacherName
    if (username) return String(username)
    if (user?.id != null) return `#${String(user.id)}`
    return ''
  } catch {
    return ''
  }
}


