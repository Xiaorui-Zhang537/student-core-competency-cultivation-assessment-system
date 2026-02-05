import { api } from './config'

export interface VoiceSession {
  id: number
  sessionId?: number
  title?: string
  model?: string
  mode?: string
  locale?: string
  scenario?: string
  pinned?: boolean
  deleted?: boolean
  createdAt?: string
  updatedAt?: string
}

export interface VoiceTurn {
  id: number
  sessionId: number
  userTranscript?: string
  assistantText?: string
  userAudioFileId?: number
  assistantAudioFileId?: number
  createdAt?: string
}

export type VoiceReplayReportRequest = {
  sessionId?: number
  turnId?: number
  audioRole: 'user' | 'assistant'
  deltaSeconds: number
  fileId?: number
  messageId?: string
}

export const voicePracticeApi = {
  createSession: (data: { title?: string; model?: string; mode?: string; locale?: string; scenario?: string }) =>
    api.post('/ai/voice/sessions', data),

  listSessions: (params?: { q?: string; page?: number; size?: number }) =>
    api.get('/ai/voice/sessions', { params }),

  getSession: (sessionId: string | number) =>
    api.get(`/ai/voice/sessions/${sessionId}`),

  updateSession: (sessionId: string | number, data: { title?: string; pinned?: boolean }) =>
    api.patch(`/ai/voice/sessions/${sessionId}`, data),

  deleteSession: (sessionId: string | number) =>
    api.delete(`/ai/voice/sessions/${sessionId}`),

  listTurns: (sessionId: string | number, params?: { page?: number; size?: number }) =>
    api.get(`/ai/voice/sessions/${sessionId}/turns`, { params }),

  saveTurn: (data: {
    sessionId: number
    model?: string
    userTranscript?: string
    assistantText?: string
    userAudioFileId?: number
    assistantAudioFileId?: number
    scenario?: string
    locale?: string
  }) => api.post('/ai/voice/turns', data),

  /**
   * 口语训练：上报音频复听/回放的增量时长（按秒）。
   *
   * 注意：仅用于行为证据事实记录，不代表评价/分数。
   */
  reportReplay: (data: VoiceReplayReportRequest) => api.post('/ai/voice/replay', data)
}

