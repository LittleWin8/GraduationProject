import request from '../request.js'
import { APIS } from '../config.js'

export const interactionApi = {
  /**
   * 点赞/取消点赞 或 收藏/取消收藏
   * @param {number} noteId 笔记ID
   * @param {string} type 'like' 或 'collect'
   */
  interact(noteId, type) {
    return request({
      url: APIS.INTERACTION.ACTION,
      method: 'POST',
      data: { noteId, type }
    })
  },
  
  /**
   * 获取互动状态（支持单条或批量）
   * @param {number|array} noteId 单个笔记ID 或 笔记ID数组
   */
  getStatus(noteId) {
    // 批量查询
    if (Array.isArray(noteId)) {
      return request({
        url: APIS.INTERACTION.STATUS,
        method: 'POST',
        data: { noteIds: noteId }
      })
    }
    // 单条查询
    return request({
      url: `${APIS.INTERACTION.STATUS}/${noteId}`,
      method: 'GET'
    })
  }
}