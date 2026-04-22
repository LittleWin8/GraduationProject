import request from '../request.js'
import { APIS } from '../config.js'

export const logApi = {
  /**
   * 上报行为日志
   * @param {string} type 'view' 浏览 | 'search' 搜索
   * @param {string} content 笔记ID 或 搜索关键词
   */
  report(type, content) {
    return request({
      url: APIS.LOG.BEHAVIOR,
      method: 'POST',
      data: { type, content }
    })
  }
}