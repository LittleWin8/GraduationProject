import request from '../request.js'
import { APIS } from '../config.js'

export const authApi = {
  // 微信登录
  login(code, userInfo = {}) {
    return request({
      url: APIS.AUTH.LOGIN,
      method: 'POST',
      data: { code, ...userInfo }
    })
  },
  
  // 退出登录
  logout() {
    return request({
      url: APIS.AUTH.LOGOUT,
      method: 'POST'
    })
  }
}