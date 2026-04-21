import { createRouter } from 'uni-simple-router'

const router = createRouter({
  routes: [
    // 固定标签页
    { path: '/pages/community/community', name: 'community' },
    { path: '/pages/profile/profile', name: 'profile' },
    
    // 动态标签页（用户自定义）
    { 
      path: '/pages/tag-notes/tag-notes', 
      name: 'tag-notes',
      // 支持参数
      props: (route) => ({ tagId: route.query.tagId, tagName: route.query.tagName })
    }
  ]
})

export default router