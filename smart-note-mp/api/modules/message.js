import request from '../request.js'
import { APIS } from '../config.js'

export const messageApi = {
  // 获取未读消息数（分组）
  getUnreadCount() {
    return request({
      url: APIS.MESSAGE.UNREAD_COUNT,
      method: 'GET'
    })
  },

  // 获取消息列表（支持 group 过滤）
  getList(page = 1, size = 20, group = '') {
    const params = { page, size }
    if (group) params.group = group
    return request({
      url: APIS.MESSAGE.LIST,
      method: 'GET',
      params
    })
  },

  // 全部标记已读
  readAll() {
    return request({
      url: APIS.MESSAGE.READ_ALL,
      method: 'POST'
    })
  },

  // 删除单条消息
  remove(id) {
    return request({
      url: `${APIS.MESSAGE.DELETE}/${id}`,
      method: 'DELETE'
    })
  }
}
