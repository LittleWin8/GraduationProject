import request from '../request.js'
import { APIS, config } from '../config.js'

export const userApi = {
  // 获取个人信息
  getUserInfo() {
    return request({
      url: APIS.USER.INFO,
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

  // 上传头像
  uploadAvatar(filePath) {
    return new Promise((resolve, reject) => {
      const token = uni.getStorageSync('token')
      uni.uploadFile({
        url: config.baseURL + APIS.USER.AVATAR,
        filePath,
        name: 'file',
        header: {
          'Authorization': token ? 'Bearer ' + token : ''
        },
        success: (res) => {
          try {
            const data = JSON.parse(res.data)
            if (data.code === 200) {
              resolve(data.data)
            } else {
              uni.showToast({ title: data.msg || '上传失败', icon: 'none' })
              reject(new Error(data.msg || '上传失败'))
            }
          } catch (error) {
            reject(new Error('解析响应失败'))
          }
        },
        fail: (err) => {
          uni.showToast({ title: '头像上传失败', icon: 'none' })
          reject(err)
        }
      })
    })
  },
  
  // 获取统计数据（含收藏列表）
  // 返回：{ noteCount, likeCount, favoriteCount, favorites: [] }
  getStats(page = 1, size = 20) {
    return request({
      url: APIS.USER.STATS,
      method: 'GET',
      params: { page, size }
    })
  }
}
