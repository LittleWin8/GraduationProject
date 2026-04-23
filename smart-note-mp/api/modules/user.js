import request from '../request.js'
import { APIS } from '../config.js'

export const userApi = {
  // 获取个人信息
  getUserInfo() {
    return request({
      url: APIS.USER.DETAIL,
      method: 'GET'
    })
  },
  
  // 更新个人信息
  updateUserInfo(data) {
    return request({
      url: APIS.USER.INFO,
      method: 'PUT',
      data
    })
  },
  
  // 获取统计数据（含收藏列表）
  // 返回：{ noteCount, likeCount, favoriteCount, favorites: [] }
  getStats(page = 1, size = 20) {
    return request({
      url: APIS.USER.STATS,
      method: 'GET',
      data: { page, size }
    })
  }
}