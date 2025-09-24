// 提供难度与分类的本地化映射（中英切换）
// - 优先走 i18n 文案键；
// - 兼容中英同义词并归一映射；
// - 找不到则回退原值。

export type I18nT = (key: string) => string

function slugify(input: string): string {
  return input
    .trim()
    .toLowerCase()
    .replace(/[^a-z0-9\u4e00-\u9fa5]+/gi, '_')
    .replace(/^_+|_+$/g, '')
}

export function localizeDifficulty(raw: any, t: I18nT): string {
  if (!raw && raw !== 0) return ''
  const v = String(raw).trim().toLowerCase()
  // 归一映射
  let key = ''
  if ([
    'beginner', 'easy', '基础', '初级', '入门'
  ].includes(v)) key = 'beginner'
  else if ([
    'intermediate', 'medium', '中级', '一般'
  ].includes(v)) key = 'intermediate'
  else if ([
    'advanced', 'hard', '高级', '进阶', '困难'
  ].includes(v)) key = 'advanced'
  else if ([
    'expert', 'master', '专家', '大师'
  ].includes(v)) key = 'expert'
  else key = slugify(v)

  const path = `student.courses.detail.difficultyMap.${key}`
  const mapped = t(path)
  // 若未命中翻译，i18n 会回传 path，本函数应回退原值
  return (mapped && mapped !== path) ? mapped : String(raw)
}

export function localizeCategory(raw: any, t: I18nT): string {
  if (!raw && raw !== 0) return ''
  const v = String(raw).trim().toLowerCase()
  // 一些常见分类的归一 key
  let key = ''
  if (/(computer|cs|计算机|编程|软件|coding)/.test(v)) key = 'computer_science'
  else if (/(math|数学|statistics|统计)/.test(v)) key = 'math'
  else if (/(design|设计|art|艺术)/.test(v)) key = 'design'
  else if (/(language|语言|english|英语)/.test(v)) key = 'language'
  else key = slugify(v)

  const path = `student.courses.categories.${key}`
  const mapped = t(path)
  return (mapped && mapped !== path) ? mapped : String(raw)
}


