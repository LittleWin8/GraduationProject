import request from '../request.js'
import { APIS } from '../config.js'

export const categoryApi = {
  // 获取系统分类列表
  getList() {
    return request({
      url: APIS.CATEGORY.LIST,
      method: 'GET'
    })
  }
}