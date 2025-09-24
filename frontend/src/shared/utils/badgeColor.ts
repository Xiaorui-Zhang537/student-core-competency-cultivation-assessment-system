// 语义色映射工具：为 Badge.vue 的 variant 提供统一映射
// 说明：保持可复用与可扩展，避免在各视图散落颜色判断

export type BadgeVariant = 'primary' | 'secondary' | 'success' | 'warning' | 'danger' | 'info'

/**
 * 根据难度字符串映射到统一的 Badge 颜色。
 * 允许多语言与大小写输入（例如：beginner/初级）。
 */
export function getDifficultyVariant(difficulty: string | undefined | null): BadgeVariant {
  if (!difficulty) return 'secondary'
  const v = String(difficulty).trim().toLowerCase()
  // 常见英文/中文别名归一
  if (['beginner', 'easy', '基础', '初级', '入门'].includes(v)) return 'success'
  if (['intermediate', 'medium', '中级', '一般'].includes(v)) return 'info'
  if (['advanced', 'hard', '高级', '进阶', '困难'].includes(v)) return 'warning'
  if (['expert', 'master', '专家', '大师'].includes(v)) return 'danger'
  return 'secondary'
}

/**
 * 根据课程分类映射颜色：可按关键词微调品牌语义色。
 */
export function getCategoryVariant(category: string | undefined | null): BadgeVariant {
  if (!category) return 'secondary'
  const v = String(category).trim().toLowerCase()
  if (/(computer|cs|编程|计算机|软件|coding)/.test(v)) return 'primary'
  if (/(math|数学|statistics|统计)/.test(v)) return 'info'
  if (/(design|设计|art|艺术)/.test(v)) return 'warning'
  if (/(language|语言|english|英语)/.test(v)) return 'success'
  return 'secondary'
}

/**
 * 标签颜色：对常见标签给出稳定色，其余落在 primary。
 */
export function getTagVariant(tag: string | undefined | null): BadgeVariant {
  if (!tag) return 'secondary'
  const v = String(tag).trim().toLowerCase()
  if (['new', 'hot', '推荐', '热门'].includes(v)) return 'danger'
  if (['project', '实战', 'lab', '实验'].includes(v)) return 'warning'
  if (['exam', '考试', 'quiz'].includes(v)) return 'info'
  if (['fundamental', '基础', 'core', '核心'].includes(v)) return 'success'
  return 'primary'
}

/**
 * 根据 MBTI 气质分组映射 Badge 颜色
 * 颜色要求：紫色、蓝色、黄色、绿色
 * - NT（分析家）：紫色 → primary
 * - SJ（守护者）：蓝色 → info
 * - SP（探险家）：黄色 → warning
 * - NF（外交家）：绿色 → success
 */
export function getMbtiVariant(mbti: string | undefined | null): BadgeVariant {
  if (!mbti) return 'secondary'
  const v = String(mbti).trim().toUpperCase()
  const NT = new Set(['INTJ','INTP','ENTJ','ENTP'])
  const NF = new Set(['INFJ','INFP','ENFJ','ENFP'])
  const SJ = new Set(['ISTJ','ISFJ','ESTJ','ESFJ'])
  const SP = new Set(['ISTP','ISFP','ESTP','ESFP'])
  if (NT.has(v)) return 'primary'   // 紫色（项目主色）
  if (SJ.has(v)) return 'info'      // 蓝色
  if (SP.has(v)) return 'warning'   // 黄色
  if (NF.has(v)) return 'success'   // 绿色
  return 'secondary'
}


