/**
 * 音频工具：PCM 转换 / 重采样 / Base64 / WAV 封装
 *
 * 说明：
 * - Gemini Live 音频输入要求（文档）：16-bit little-endian PCM（常见 16kHz 单声道）
 * - 浏览器音频回调通常是 Float32Array（AudioContext sampleRate 通常 48k）
 *
 * @author System
 * @since 2026-02-02
 */

export function downsampleFloat32Buffer(input: Float32Array, inputSampleRate: number, targetSampleRate: number): Float32Array {
  if (targetSampleRate >= inputSampleRate) return input
  const ratio = inputSampleRate / targetSampleRate
  const newLen = Math.round(input.length / ratio)
  const output = new Float32Array(newLen)
  let offsetResult = 0
  let offsetBuffer = 0
  while (offsetResult < output.length) {
    const nextOffsetBuffer = Math.round((offsetResult + 1) * ratio)
    let acc = 0
    let count = 0
    for (let i = offsetBuffer; i < nextOffsetBuffer && i < input.length; i++) {
      acc += input[i]
      count++
    }
    output[offsetResult] = count > 0 ? acc / count : 0
    offsetResult++
    offsetBuffer = nextOffsetBuffer
  }
  return output
}

export function float32ToInt16PCM(input: Float32Array): Int16Array {
  const out = new Int16Array(input.length)
  for (let i = 0; i < input.length; i++) {
    const s = Math.max(-1, Math.min(1, input[i]))
    out[i] = s < 0 ? s * 0x8000 : s * 0x7fff
  }
  return out
}

export function base64FromArrayBuffer(buf: ArrayBuffer): string {
  const bytes = new Uint8Array(buf)
  let binary = ''
  const chunk = 0x8000
  for (let i = 0; i < bytes.length; i += chunk) {
    const sub = bytes.subarray(i, i + chunk)
    binary += String.fromCharCode(...sub)
  }
  return btoa(binary)
}

export function arrayBufferFromBase64(b64: string): ArrayBuffer {
  const binary = atob(b64)
  const len = binary.length
  const bytes = new Uint8Array(len)
  for (let i = 0; i < len; i++) {
    bytes[i] = binary.charCodeAt(i)
  }
  return bytes.buffer
}

export function encodeWavFromInt16PCM(pcm: Int16Array, sampleRate: number, numChannels = 1): Blob {
  const bytesPerSample = 2
  const blockAlign = numChannels * bytesPerSample
  const byteRate = sampleRate * blockAlign
  const dataSize = pcm.length * bytesPerSample
  const buffer = new ArrayBuffer(44 + dataSize)
  const view = new DataView(buffer)

  const writeString = (offset: number, s: string) => {
    for (let i = 0; i < s.length; i++) view.setUint8(offset + i, s.charCodeAt(i))
  }

  // RIFF header
  writeString(0, 'RIFF')
  view.setUint32(4, 36 + dataSize, true)
  writeString(8, 'WAVE')
  // fmt chunk
  writeString(12, 'fmt ')
  view.setUint32(16, 16, true) // PCM
  view.setUint16(20, 1, true) // audio format = PCM
  view.setUint16(22, numChannels, true)
  view.setUint32(24, sampleRate, true)
  view.setUint32(28, byteRate, true)
  view.setUint16(32, blockAlign, true)
  view.setUint16(34, 16, true) // bits per sample
  // data chunk
  writeString(36, 'data')
  view.setUint32(40, dataSize, true)

  // PCM samples
  let offset = 44
  for (let i = 0; i < pcm.length; i++, offset += 2) {
    view.setInt16(offset, pcm[i], true)
  }

  return new Blob([buffer], { type: 'audio/wav' })
}

export function parsePcmRateFromMimeType(mimeType?: string | null): number | null {
  if (!mimeType) return null
  const m = String(mimeType)
  const match = m.match(/rate=(\d+)/i)
  if (!match) return null
  const n = Number(match[1])
  return Number.isFinite(n) && n > 0 ? n : null
}

