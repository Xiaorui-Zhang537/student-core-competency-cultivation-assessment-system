export type ClassValue = string | number | null | undefined | false | Record<string, boolean>

export function cn(...inputs: ClassValue[]): string {
  const classes: string[] = []
  for (const input of inputs) {
    if (!input) continue
    if (typeof input === 'string' || typeof input === 'number') {
      classes.push(String(input))
    } else if (typeof input === 'object') {
      for (const key in input) {
        if (Object.prototype.hasOwnProperty.call(input, key) && (input as any)[key]) {
          classes.push(key)
        }
      }
    }
  }
  return classes.join(' ')
}


