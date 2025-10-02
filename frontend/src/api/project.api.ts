import { api } from './config'

export interface ProjectTreeNode {
  name: string
  directory: boolean
  children?: ProjectTreeNode[]
}

export function getProjectTree(params?: { depth?: number }) {
  const d = params?.depth ? `?depth=${encodeURIComponent(String(params.depth))}` : ''
  return api.get<ProjectTreeNode[]>(`/public/project/tree${d}`)
}


