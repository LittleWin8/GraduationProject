import { config } from './config.js'

// 将对象转换为查询字符串（兼容微信小程序）
const encodeQueryParams = (params) => {
  if (!params) return ''
  const queryParts = []
  for (const key in params) {
    if (params.hasOwnProperty(key) && params[key] !== undefined && params[key] !== null) {
      const value = params[key]
      queryParts.push(`${encodeURIComponent(key)}=${encodeURIComponent(value)}`)
    }
  }
  return queryParts.join('&')
}

const handleUnauthorized = (msg) => {
  uni.removeStorageSync('token')
  uni.removeStorageSync('userInfo')
  const pages = getCurrentPages()
  if (pages.length > 0) {
    const currentPage = pages[pages.length - 1]
    const path = '/' + currentPage.route
    const params = currentPage.options || {}
    const query = Object.keys(params)
      .map(k => `${k}=${params[k]}`)
      .join('&')
    const fullPath = query ? path + '?' + query : path
    if (!currentPage.route || !currentPage.route.includes('login/login')) {
      uni.setStorageSync('redirectUrl', fullPath)
    }
  }
  const curPages = getCurrentPages()
  if (
    curPages.length > 0 &&
    curPages[curPages.length - 1].route &&
    curPages[curPages.length - 1].route.includes('login/login')
  ) {
    return
  }
  uni.reLaunch({ url: '/pages/login/login' })
}

export const request = (options) => {
    return new Promise((resolve, reject) => {
        const token = uni.getStorageSync('token')
        
        // 处理 GET 和 DELETE 请求的 params 参数（兼容微信小程序）
        let url = config.baseURL + options.url
        if ((options.method === 'GET' || options.method === 'DELETE') && options.params) {
            const queryString = encodeQueryParams(options.params)
            if (queryString) {
                url += '?' + queryString
            }
        }
        
        uni.request({
            url: url,
            method: options.method || 'GET',
            data: (options.method === 'GET' || options.method === 'DELETE') ? undefined : (options.data || {}),
            timeout: config.timeout,
            header: {
                'Content-Type': 'application/json',
                'Authorization': token && options.url !== '/api/wx/auth/login' 
                    ? 'Bearer ' + token 
                    : ''
            },
            success: (res) => {
                if (res.statusCode === 200) {
                    const result = res.data
                    
                    if (result.code === 200) {
                        resolve(result.data)
                    } else if (result.code === 401) {
                        handleUnauthorized(result.msg || '登录已过期')
                        reject(new Error(result.msg || '登录已过期'))
                    } else {
                        uni.showToast({ title: result.msg || '请求失败', icon: 'none' })
                        reject(new Error(result.msg))
                    }
                } else if (res.statusCode === 401) {
                    handleUnauthorized('登录已过期')
                    reject(new Error('登录已过期'))
                } else {
                    uni.showToast({ title: `请求失败: ${res.statusCode}`, icon: 'none' })
                    reject(new Error(`HTTP ${res.statusCode}`))
                }
            },
            fail: (err) => {
                console.error('[API] 请求失败:', err)
                let errorMsg = '网络连接失败'
                if (err.errMsg) {
                    if (err.errMsg.includes('timeout')) errorMsg = '请求超时'
                    else if (err.errMsg.includes('fail')) errorMsg = '无法连接服务器'
                }
                uni.showToast({ title: errorMsg, icon: 'none', duration: 2000 })
                reject(err)
            }
        })
    })
}

export default request