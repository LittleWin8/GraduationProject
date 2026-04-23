import { config } from './config.js'

export const request = (options) => {
    return new Promise((resolve, reject) => {
        const token = uni.getStorageSync('token')
        
        // 处理 GET 请求的 params 参数
        let url = config.baseURL + options.url
        if (options.method === 'GET' && options.params) {
            const queryParams = new URLSearchParams(options.params).toString()
            if (queryParams) {
                url += '?' + queryParams
            }
        }
        
        uni.request({
            url: url,
            method: options.method || 'GET',
            data: options.method === 'GET' ? undefined : (options.data || {}),
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
                        // 清除本地存储
                        uni.removeStorageSync('token')
                        uni.removeStorageSync('userInfo')
                        // 跳转到登录页
                        uni.reLaunch({ url: '/pages/login/login' })
                        reject(new Error(result.msg || '登录已过期'))
                    } else {
                        uni.showToast({ title: result.msg || '请求失败', icon: 'none' })
                        reject(new Error(result.msg))
                    }
                } else if (res.statusCode === 401) {
                    uni.removeStorageSync('token')
                    uni.removeStorageSync('userInfo')
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

export default request