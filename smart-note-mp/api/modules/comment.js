import request from '../request.js'
import { APIS } from '../config.js'

export const commentApi = {
  // 获取评论列表
  getComments(noteId, page = 1, size = 20) {
    return request({
      url: APIS.COMMENT.LIST,
      method: 'GET',
      data: { noteId, page, size }
    })
  },
  
  // 发表评论
  addComment(noteId, content, parentId = null) {
    return request({
      url: APIS.COMMENT.CREATE,
      method: 'POST',
      data: { noteId, content, parentId }
    })
  },
  
  // 删除评论
  deleteComment(commentId) {
    return request({
      url: `${APIS.COMMENT.DELETE}/${commentId}`,
      method: 'DELETE'
    })
  }
}