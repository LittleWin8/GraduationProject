// 统一导出所有 API 模块
export { authApi } from './modules/auth.js'
export { userApi } from './modules/user.js'
export { noteApi } from './modules/note.js'
export { categoryApi } from './modules/category.js'
export { tagApi } from './modules/tag.js'
export { interactionApi } from './modules/interaction.js'
export { commentApi } from './modules/comment.js'
export { logApi } from './modules/log.js'

// 导出 request 和 config 供特殊情况使用
export { default as request } from './request.js'
export { config, APIS } from './config.js'