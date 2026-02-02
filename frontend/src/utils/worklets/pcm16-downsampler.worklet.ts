/**
 * AudioWorklet: downsample Float32 PCM -> Int16 PCM16 (ArrayBuffer) and post to main thread.
 *
 * Notes:
 * - Runs in AudioWorkletGlobalScope (no DOM APIs).
 * - We deliberately keep the implementation small & robust.
 */
class Pcm16DownsamplerProcessor extends AudioWorkletProcessor {
  private readonly targetSampleRate: number;
  private ratio: number;
  private carry: Float32Array = new Float32Array(0);

  constructor(options?: AudioWorkletNodeOptions) {
    super();
    const target = (options as any)?.processorOptions?.targetSampleRate;
    this.targetSampleRate = Number(target) > 0 ? Number(target) : 16000;
    this.ratio = sampleRate / this.targetSampleRate;
    if (!Number.isFinite(this.ratio) || this.ratio <= 0) {
      this.ratio = 1;
    }
  }

  process(inputs: Float32Array[][]): boolean {
    const input = inputs?.[0]?.[0];
    if (!input || input.length === 0) return true;

    // Concatenate carry + current frame
    let data: Float32Array;
    if (this.carry.length > 0) {
      data = new Float32Array(this.carry.length + input.length);
      data.set(this.carry, 0);
      data.set(input, this.carry.length);
    } else {
      data = input;
    }

    const outLen = Math.floor(data.length / this.ratio);
    if (outLen <= 0) {
      // Not enough data yet, keep buffering
      this.carry = data.slice(0);
      return true;
    }

    const out = new Int16Array(outLen);
    let pos = 0;
    for (let i = 0; i < outLen; i++) {
      const s = data[Math.floor(pos)] ?? 0;
      const v = Math.max(-1, Math.min(1, s));
      out[i] = v < 0 ? (v * 0x8000) : (v * 0x7fff);
      pos += this.ratio;
    }

    const consumed = Math.floor(pos);
    const remain = data.length - consumed;
    this.carry = remain > 0 ? data.slice(consumed) : new Float32Array(0);

    // Transfer ArrayBuffer to avoid copy
    this.port.postMessage(
      { type: 'pcm16', sampleRate: this.targetSampleRate, pcm16: out.buffer },
      [out.buffer]
    );
    return true;
  }
}

registerProcessor('pcm16-downsampler', Pcm16DownsamplerProcessor);

