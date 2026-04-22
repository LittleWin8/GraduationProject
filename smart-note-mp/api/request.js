import { config } from './config.js'

export const request = (options) => {
    return new Promise((resolve, reject) => {
        const token = uni.getStorageSync('token')
        
        uni.request({
            url: config.baseURL + options.url,
            method: options.method || 'GET',
            data: options.data || {},
            timeout: config.timeout,
            header: {
                'Content-Type': 'application/json',
                'Authorization': token && options.url !== '/api/wx/auth/login' 
                    ? 'Bearer ' + token 
                    : ''
            },
            success: (res) => {
                console.log('[API] 响应:', res.statusCode, res.data)
                
                if (res.statusCode === 200) {
                    const result = res.data
                    
                    if (result.code === 200) {
                        resolve(result.data)
                    } else if (result.code === 401) {
                        uni.removeStorageSync('token')
                        uni.reLaunch({ url: '/pages/login/login' })
                        reject(new Error(result.msg || '登录已过期'))
                    } else {
                        uni.showToast({ title: result.msg || '请求失败', icon: 'none' })
                        reject(new Error(result.msg))
                    }
                } else if (res.statusCode === 401) {
                    uni.removeStorageSync('token')
                    uni.reLaunch({ url: '/pages/login/login' })
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