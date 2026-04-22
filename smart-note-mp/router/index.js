import { createRouter } from 'uni-simple-router'

const router = createRouter({
  routes: [
    // 登录页
    { path: '/pages/login/login', name: 'login' },
    
    // 固定标签页
    { path: '/pages/community/community', name: 'community' },
    { path: '/pages/create/create', name: 'create' },
    { path: '/pages/profile/profile', name: 'profile' },
    
    // 标签管理
    { path: '/pages/tag-manage/tag-manage', name: 'tag-manage' },
    
    // 用户信息
    { path: '/pages/user-info/user-info', name: 'user-info' },
    
    // 动态标签页（用户自定义）
    { 
      path: '/pages/tag-notes/tag-notes', 
      name: 'tag-notes',
      props: (route) => ({ 
        tagId: route.query.tagId, 
        tagName: route.query.tagName 
      })
    }
  ]
})

export default router