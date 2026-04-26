import request from '../request.js'
import { APIS } from '../config.js'

export const tagApi = {
  // 获取我的标签列表
  getMyTags() {
    return request({
      url: APIS.TAG.LIST,
      method: 'GET'
    })
  },
  
  // 创建标签
  createTag(name) {
    return request({
      url: APIS.TAG.CREATE,
      method: 'POST',
      data: { name }
    })
  },
  
  // 删除标签
  deleteTag(id) {
    return request({
      url: `${APIS.TAG.DELETE}/${id}`,
      method: 'DELETE'
    })
  },
  
  // 获取标签下的笔记
  getNotesByTag(tagId, page = 1, size = 20) {
    return request({
      url: `${APIS.TAG.NOTES_BY_TAG}/${tagId}/notes`,
      method: 'GET',
      params: { page, size }
    })
  }
}