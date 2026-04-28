// 环境配置
const ENV = {
  dev: {
    baseURL: 'http://192.168.1.8:8080'
  },
  prod: {
    baseURL: ''
  }
}

const currentEnv = 'dev'

export const config = {
  baseURL: ENV[currentEnv].baseURL,
  timeout: 15000
}

// 接口路径配置
export const APIS = {
  // 认证模块
  AUTH: {
    LOGIN: '/api/wx/auth/login',
    LOGOUT: '/api/wx/auth/logout'
  },
  
  // 用户模块（合并了统计和收藏列表）
  USER: {
    INFO: '/api/wx/user',
    DETAIL: '/api/wx/user',
    AVATAR: '/api/wx/user/avatar',
    STATS: '/api/wx/note/stats'     // 返回：笔记数、点赞数、收藏数、收藏列表
  },
  
  // 笔记模块
  NOTE: {
    LIST: '/api/wx/notes',          // GET?type=public/my&page=&size=&categoryId=&tagId=&status=
    DETAIL: '/api/wx/notes',        // /{id}
    CREATE: '/api/wx/notes',
    UPDATE: '/api/wx/notes',        // /{id}
    DELETE: '/api/wx/notes',        // /{id}?permanent=false/true
    RESTORE: '/api/wx/notes',       // /{id}/restore
    AI_SUMMARY: '/api/wx/notes/ai/summary',
    UPLOAD: '/api/wx/notes/attachment',
	MY_NOTES: '/api/wx/note/my-notes',  // 我的笔记列表
	STATS: '/api/wx/note/stats'         ,// 统计数据
	FAVORITES: '/api/wx/note/favorites',  // 新增：收藏列表
	LIKED: '/api/wx/note/liked'           // 新增：点赞列表
  },
  
  // 分类模块
  CATEGORY: {
    LIST: '/api/wx/categories/list'
  },
  
  // 标签模块
  TAG: {
    LIST: '/api/wx/tags',
    CREATE: '/api/wx/tags',
    DELETE: '/api/wx/tags',         // /{id}
    NOTES_BY_TAG: '/api/wx/tags'    // /{id}/notes
  },
  
  // 互动模块（合并了单条和批量状态查询）
  INTERACTION: {
    ACTION: '/api/wx/interactions',              // POST?type=like/collect
    STATUS: '/api/wx/interactions/status'        // GET?noteId=xxx 或 POST批量
  },
  
  // 评论模块
  COMMENT: {
    LIST: '/api/wx/comments',        // GET?noteId=&page=&size=
    CREATE: '/api/wx/comments',
    DELETE: '/api/wx/comments'       // /{id}
  },
  
  // 日志模块
  LOG: {
    BEHAVIOR: '/api/wx/log/behavior' // POST?type=view/search
  }
}