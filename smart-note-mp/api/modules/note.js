import request from '../request.js'
import { APIS, config } from '../config.js'

export const noteApi = {
  /**
   * 获取笔记列表
   * @param {string} type 'public' 公开（社区）| 'my' 我的
   * @param {number} page 页码
   * @param {number} size 每页数量
   * @param {object} filters { categoryId, tagId, status }
   */
  getNotes(type = 'public', page = 1, size = 20, filters = {}) {
    return request({
      url: APIS.NOTE.LIST,
      method: 'GET',
      params: { type, pageNum: page, pageSize: size, ...filters }
    })
  },
  
  // 获取笔记详情
  getNoteDetail(id) {
    return request({
      url: `${APIS.NOTE.DETAIL}/${id}`,
      method: 'GET'
    })
  },
  
  // 创建笔记
  createNote(data) {
    return request({
      url: APIS.NOTE.CREATE,
      method: 'POST',
      data
    })
  },
  
  // 更新笔记
  updateNote(id, data) {
    return request({
      url: `${APIS.NOTE.UPDATE}/${id}`,
      method: 'PUT',
      data
    })
  },
  
  /**
   * 删除笔记
   * @param {number} id 笔记ID
   * @param {boolean} permanent true永久删除 false移入回收站
   */
  deleteNote(id, permanent = false) {
    return request({
      url: `${APIS.NOTE.DELETE}/${id}`,
      method: 'DELETE',
      params: { permanent }
    })
  },
  
  // 恢复笔记（从回收站）
  restoreNote(id) {
    return request({
      url: `${APIS.NOTE.RESTORE}/${id}/restore`,
      method: 'PUT'
    })
  },
  
  // AI生成摘要和关键词
  aiSummary(content) {
    return request({
      url: APIS.NOTE.AI_SUMMARY,
      method: 'POST',
      data: { content }
    })
  },
  
  // 上传附件
  uploadAttachment(filePath, noteId = null) {
    return new Promise((resolve, reject) => {
      const token = uni.getStorageSync('token')
      
      uni.uploadFile({
        url: config.baseURL + APIS.NOTE.UPLOAD,
        filePath: filePath,
        name: 'file',
        formData: { noteId: noteId || '' },
        header: {
          'Authorization': token ? `Bearer ${token}` : ''
        },
        success: (res) => {
          try {
            const data = JSON.parse(res.data)
            if (data.code === 200) {
              resolve(data.data)
            } else {
              uni.showToast({ title: data.msg || '上传失败', icon: 'none' })
              reject(data)
            }
          } catch (e) {
            reject(e)
          }
        },
        fail: (err) => {
          uni.showToast({ title: '上传失败', icon: 'none' })
          reject(err)
        }
      })
    })
  },
  
  // ========== 我的笔记相关 API（支持搜索筛选） ==========
  
  /**
   * 获取我的笔记列表（支持分页+搜索+筛选）
   * @param {Object} params - 查询参数
   * @param {number} params.pageNum - 页码
   * @param {number} params.pageSize - 每页大小
   * @param {string} params.keyword - 关键词搜索
   * @param {number} params.categoryId - 分类ID
   * @param {string} params.startTime - 开始时间
   * @param {string} params.endTime - 结束时间
   * @param {string} params.orderBy - 排序字段
   * @param {string} params.orderDirection - 排序方向
   * @param {number} params.status - 状态（0草稿/1正常/2回收站）
   */
  getMyNotes(params = {}) {
    return request({
      url: APIS.NOTE.MY_NOTES,
      method: 'GET',
      params: {
        pageNum: params.pageNum || 1,
        pageSize: params.pageSize || 10,
        keyword: params.keyword,
        categoryId: params.categoryId,
        startTime: params.startTime,
        endTime: params.endTime,
        orderBy: params.orderBy,
        orderDirection: params.orderDirection,
        status: params.status
      }
    })
  },
  
  /**
   * 获取我的收藏列表
   */
  getFavorites(params = {}) {
    return request({
      url: APIS.NOTE.FAVORITES,
      method: 'GET',
      params: {
        pageNum: params.pageNum || 1,
        pageSize: params.pageSize || 10,
        keyword: params.keyword,
        categoryId: params.categoryId,
        startTime: params.startTime,
        endTime: params.endTime,
        orderBy: params.orderBy,
        orderDirection: params.orderDirection
      }
    })
  },
  
  /**
   * 获取我的点赞列表
   */
  getLiked(params = {}) {
    return request({
      url: APIS.NOTE.LIKED,
      method: 'GET',
      params: {
        pageNum: params.pageNum || 1,
        pageSize: params.pageSize || 10,
        keyword: params.keyword,
        categoryId: params.categoryId,
        startTime: params.startTime,
        endTime: params.endTime,
        orderBy: params.orderBy,
        orderDirection: params.orderDirection
      }
    })
  },
  
  // 获取统计数据
  getStats() {
    return request({
      url: APIS.NOTE.STATS,
      method: 'GET'
    })
  }
}