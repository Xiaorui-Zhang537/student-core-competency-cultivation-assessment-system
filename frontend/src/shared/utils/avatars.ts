// 为避免部分浏览器对 SVG 作为背景渲染造成的裁切/拉伸问题，这里统一使用 PNG 输出
// 可调整 size=128 获得更清晰的小头像
export const DEFAULT_AVATARS: string[] = [
  'https://api.dicebear.com/7.x/adventurer-neutral/png?seed=Nova&size=128',
  'https://api.dicebear.com/7.x/adventurer/png?seed=Luna&size=128',
  'https://api.dicebear.com/7.x/avataaars/png?seed=Kai&size=128',
  'https://api.dicebear.com/7.x/notionists-neutral/png?seed=Iris&size=128',
  'https://api.dicebear.com/7.x/big-smile/png?seed=Leo&size=128',
  'https://api.dicebear.com/7.x/thumbs/png?seed=Mila&size=128',
  'https://api.dicebear.com/7.x/micah/png?seed=Aiden&size=128',
  'https://api.dicebear.com/7.x/miniavs/png?seed=Sage&size=128',
  'https://api.dicebear.com/7.x/adventurer-neutral/png?seed=Zoe&size=128',
  'https://api.dicebear.com/7.x/notionists/png?seed=Eli&size=128',
  'https://api.dicebear.com/7.x/bottts/png?seed=Alex&size=128',
  'https://api.dicebear.com/7.x/identicon/png?seed=Blair&size=128',
  'https://api.dicebear.com/7.x/pixel-art/png?seed=Remy&size=128',
  'https://api.dicebear.com/7.x/shapes/png?seed=Noa&size=128',
  'https://api.dicebear.com/7.x/fun-emoji/png?seed=Quinn&size=128',
  'https://api.dicebear.com/7.x/thumbs/png?seed=Riley&size=128',
  'https://api.dicebear.com/7.x/avataaars-neutral/png?seed=Sky&size=128',
  'https://api.dicebear.com/7.x/micah/png?seed=Robin&size=128',
  'https://api.dicebear.com/7.x/adventurer/png?seed=Jordan&size=128',
  'https://api.dicebear.com/7.x/notionists-neutral/png?seed=Taylor&size=128'
]

export const pickDefaultAvatar = (index = 0): string => DEFAULT_AVATARS[Math.abs(index) % DEFAULT_AVATARS.length]

