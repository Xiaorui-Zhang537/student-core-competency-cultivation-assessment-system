import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import * as communityApi from '@/api/community'
import type { PostData, CommunityStats, HotTopic, ActiveUser } from '@/api/community'

export const useCommunityStore = defineStore('community', () => {
  // 状态
  const posts = ref<PostData[]>([])
  const currentPost = ref<PostData | null>(null)
  const stats = ref<CommunityStats>({
    totalPosts: 0,
    activeMembers: 0,
    todayPosts: 0,
    totalViews: 0
  })
  const hotTopics = ref<HotTopic[]>([])
  const activeUsers = ref<ActiveUser[]>([])
  const loading = ref(false)
  const error = ref<string | null>(null)

  // 分页信息
  const pagination = ref({
    current: 1,
    pageSize: 20,
    total: 0
  })

  // 查询条件
  const filters = ref({
    category: '',
    keyword: '',
    orderBy: 'latest'
  })

  // 计算属性
  const filteredPosts = computed(() => {
    let result = posts.value

    // 分类筛选
    if (filters.value.category && filters.value.category !== 'all') {
      result = result.filter(post => post.category === filters.value.category)
    }

    // 搜索筛选
    if (filters.value.keyword) {
      const keyword = filters.value.keyword.toLowerCase()
      result = result.filter(post => 
        post.title.toLowerCase().includes(keyword) ||
        post.content.toLowerCase().includes(keyword) ||
        post.tags.some(tag => tag.toLowerCase().includes(keyword))
      )
    }

    // 排序
    result.sort((a, b) => {
      // 置顶帖子始终在前
      if (a.pinned && !b.pinned) return -1
      if (!a.pinned && b.pinned) return 1
      
      switch (filters.value.orderBy) {
        case 'latest':
          return new Date(b.createdAt).getTime() - new Date(a.createdAt).getTime()
        case 'popular':
          return b.replies - a.replies
        case 'hottest':
          return b.likes - a.likes
        case 'views':
          return b.views - a.views
        default:
          return 0
      }
    })

    return result
  })

  // 方法
  const fetchPosts = async (params?: {
    page?: number
    size?: number
    category?: string
    keyword?: string
    orderBy?: string
  }) => {
    loading.value = true
    error.value = null
    
    try {
      const response = await communityApi.getPostList({
        page: params?.page || pagination.value.current,
        size: params?.size || pagination.value.pageSize,
        category: params?.category || filters.value.category,
        keyword: params?.keyword || filters.value.keyword,
        orderBy: params?.orderBy || filters.value.orderBy
      })
      
      if (response.data.code === 200) {
        const result = response.data.data
        posts.value = result.items
        pagination.value = {
          current: result.current,
          pageSize: result.size,
          total: result.total
        }
      } else {
        throw new Error(response.data.message)
      }
    } catch (err) {
      error.value = err instanceof Error ? err.message : '获取帖子列表失败'
      console.error('Error fetching posts:', err)
    } finally {
      loading.value = false
    }
  }

  const fetchPostDetail = async (id: string) => {
    loading.value = true
    error.value = null
    
    try {
      const response = await communityApi.getPostDetail(id)
      
      if (response.data.code === 200) {
        currentPost.value = response.data.data
        return response.data.data
      } else {
        throw new Error(response.data.message)
      }
    } catch (err) {
      error.value = err instanceof Error ? err.message : '获取帖子详情失败'
      console.error('Error fetching post detail:', err)
      return null
    } finally {
      loading.value = false
    }
  }

  const createPost = async (postData: {
    title: string
    content: string
    category: string
    tagsInput: string
    anonymous?: boolean
    allowComments?: boolean
  }) => {
    loading.value = true
    error.value = null
    
    try {
      const response = await communityApi.createPost(postData)
      
      if (response.data.code === 200) {
        // 重新获取帖子列表
        await fetchPosts()
        return response.data.data
      } else {
        throw new Error(response.data.message)
      }
    } catch (err) {
      error.value = err instanceof Error ? err.message : '发布帖子失败'
      console.error('Error creating post:', err)
      throw err
    } finally {
      loading.value = false
    }
  }

  const likePost = async (id: string) => {
    try {
      const response = await communityApi.likePost(id)
      
      if (response.data.code === 200) {
        const liked = response.data.data.liked
        
        // 更新本地状态
        const post = posts.value.find(p => p.id === id)
        if (post) {
          post.liked = liked
          post.likes += liked ? 1 : -1
        }
        
        if (currentPost.value && currentPost.value.id === id) {
          currentPost.value.liked = liked
          currentPost.value.likes += liked ? 1 : -1
        }
        
        return liked
      } else {
        throw new Error(response.data.message)
      }
    } catch (err) {
      error.value = err instanceof Error ? err.message : '操作失败'
      console.error('Error liking post:', err)
      throw err
    }
  }

  const fetchStats = async () => {
    try {
      const response = await communityApi.getCommunityStats()
      
      if (response.data.code === 200) {
        stats.value = response.data.data
      }
    } catch (err) {
      console.error('Error fetching stats:', err)
    }
  }

  const fetchHotTopics = async () => {
    try {
      const response = await communityApi.getHotTopics()
      
      if (response.data.code === 200) {
        hotTopics.value = response.data.data.map((topic: any, index: number) => ({
          id: topic.id,
          name: topic.name,
          rank: index + 1,
          postCount: topic.postCount
        }))
      }
    } catch (err) {
      console.error('Error fetching hot topics:', err)
    }
  }

  const fetchActiveUsers = async () => {
    try {
      const response = await communityApi.getActiveUsers()
      
      if (response.data.code === 200) {
        activeUsers.value = response.data.data
      }
    } catch (err) {
      console.error('Error fetching active users:', err)
    }
  }

  const setFilters = (newFilters: Partial<typeof filters.value>) => {
    Object.assign(filters.value, newFilters)
  }

  const resetFilters = () => {
    filters.value = {
      category: '',
      keyword: '',
      orderBy: 'latest'
    }
  }

  const getCategoryStats = async () => {
    try {
      const response = await communityApi.getCommunityStats()
      if (response.data.code === 200) {
        return response.data.data.categoryStats || []
      }
      return []
    } catch (err) {
      console.error('Error fetching category stats:', err)
      return []
    }
  }

  return {
    // 状态
    posts,
    currentPost,
    stats,
    hotTopics,
    activeUsers,
    loading,
    error,
    pagination,
    filters,
    
    // 计算属性
    filteredPosts,
    
    // 方法
    fetchPosts,
    fetchPostDetail,
    createPost,
    likePost,
    fetchStats,
    fetchHotTopics,
    fetchActiveUsers,
    setFilters,
    resetFilters,
    getCategoryStats
  }
}) 