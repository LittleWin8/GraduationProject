// utils/request.js
const baseURL = 'http://192.168.1.4:8080'; // 确认这个地址正确

export const request = (options) => {
    return new Promise((resolve, reject) => {
        // 获取 token
        const token = uni.getStorageSync('token');
        
        uni.request({
            url: baseURL + options.url,
            method: options.method || 'GET',
            data: options.data || {},
            timeout: 15000,
            header: {
                'Content-Type': 'application/json',
                // 登录接口不需要 Authorization
                'Authorization': token && options.url !== '/api/wx/auth/login' ? 'Bearer ' + token : ''
            },
            success: (res) => {
                console.log('请求成功 - 状态码:', res.statusCode);
                console.log('请求成功 - 返回数据:', res.data);
                
                if (res.statusCode === 200) {
                    resolve(res.data);
                } else if (res.statusCode === 401) {
                    // token 过期，清除并跳转登录
                    uni.removeStorageSync('token');
                    uni.reLaunch({ url: '/pages/login/login' });
                    reject(new Error('登录已过期'));
                } else {
                    reject(new Error(`HTTP ${res.statusCode}: ${res.data?.msg || '请求失败'}`));
                }
            },
            fail: (err) => {
                console.error('请求失败详情:', err);
                
                let errorMsg = '网络连接失败';
                if (err.errMsg) {
                    if (err.errMsg.includes('timeout')) {
                        errorMsg = '请求超时，请检查网络连接';
                    } else if (err.errMsg.includes('fail')) {
                        errorMsg = '无法连接服务器，请检查后端服务是否启动';
                    }
                }
                
                uni.showToast({ 
                    title: errorMsg, 
                    icon: 'none',
                    duration: 2000
                });
                reject(err);
            }
        });
    });
};