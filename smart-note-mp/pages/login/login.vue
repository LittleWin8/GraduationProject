<template>
	<view class="login-container">
		<view class="content-wrapper">
			<view class="header">
				<up-image src="/static/logo.png" width="80px" height="80px" shape="circle"></up-image>
				<view class="title">Link Mind</view>
				<view class="subtitle">Link you mind, Build your world</view>
			</view>

			<view class="form-area">
				<up-button 
					type="primary" 
					icon="weixin-fill" 
					text="微信一键登录" 
					size="large" 
					shape="circle"
					:loading="loading"
					@click="handleWechatLogin"
				></up-button>
				
				<view class="auth-tip" v-if="needAuth">
					<text>首次登录需要授权获取昵称和头像</text>
				</view>
				
				<view class="agreement">
					<label class="agree-label" @click="toggleAgree">
						<checkbox :checked="isAgree" style="transform: scale(0.8)" />
						<text class="agree-text">已阅读并同意</text>
						<text class="agree-link">《用户协议》</text>
						<text class="agree-text">和</text>
						<text class="agree-link">《隐私政策》</text>
					</label>
				</view>
			</view>

			<view class="footer">
				<text>首次登录将自动注册账号</text>
			</view>
		</view>
	</view>
</template>

<script setup>
import { ref } from 'vue';
import { request } from '@/utils/request.js';

const loading = ref(false);
const isAgree = ref(false);
const needAuth = ref(false);

// 切换协议勾选状态
const toggleAgree = () => {
	isAgree.value = !isAgree.value;
	console.log('协议勾选状态:', isAgree.value);
};

// 微信登录
const handleWechatLogin = async () => {
	// 检查是否勾选协议
	if (!isAgree.value) {
		uni.showToast({ 
			title: '请先阅读并同意用户协议和隐私政策', 
			icon: 'none',
			duration: 2000
		});
		return;
	}
	
	// 检查微信登录是否可用
	if (!uni.login) {
		uni.showToast({ 
			title: '当前环境不支持微信登录', 
			icon: 'none' 
		});
		return;
	}
	
	loading.value = true;
	needAuth.value = false;
	
	uni.login({
		provider: 'weixin',
		success: (loginRes) => {
			console.log('获取微信code成功:', loginRes.code);
			loginToServer(loginRes.code);
		},
		fail: (err) => {
			console.error('微信登录失败:', err);
			loading.value = false;
			
			// 详细的错误提示
			let errorMsg = '微信登录失败';
			if (err.errMsg) {
				if (err.errMsg.includes('cancel')) {
					errorMsg = '取消登录';
				} else if (err.errMsg.includes('unauthorized')) {
					errorMsg = '授权失败，请重试';
				}
			}
			uni.showToast({ 
				title: errorMsg, 
				icon: 'none' 
			});
		}
	});
};

// 调用后端登录接口
const loginToServer = async (code, userInfo) => {
  try {
    console.log('请求后端登录接口:', { code, userInfo });
    
    const res = await request({
      url: '/api/wx/auth/login',
      method: 'POST',
      data: { 
        code: code,
        nickName: userInfo?.nickName,
        avatarUrl: userInfo?.avatarUrl
      }
    });
    
    console.log('后端返回数据:', res);
    
    if (res && res.code === 200) {
      if (res.data && res.data.token) {
        // 保存用户信息到本地
        if (userInfo) {
          uni.setStorageSync('userInfo', userInfo);
        }
        uni.setStorageSync('token', res.data.token);
        
        uni.showToast({ title: '登录成功', icon: 'success', duration: 1500 });
        
        setTimeout(() => {
          uni.reLaunch({ url: '/pages/community/community' });
        }, 1500);
      } else {
        uni.showToast({ title: res.msg || '登录失败', icon: 'none' });
      }
    } else {
      const errorMsg = res?.msg || res?.message || '登录失败，请重试';
      uni.showToast({ title: errorMsg, icon: 'none' });
    }
  } catch (error) {
    console.error('登录请求异常:', error);
  } finally {
    loading.value = false;
  }
};
</script>

<style lang="scss" scoped>
.login-container {
	min-height: 100vh;
	display: flex;
	background-color: #fff;
}

.content-wrapper {
	flex: 1;
	display: flex;
	flex-direction: column;
	justify-content: center;  /* 关键：垂直居中 */
	padding: 40px 30px;
	box-sizing: border-box;
}

.header {
	display: flex;
	flex-direction: column;
	align-items: center;
	margin-bottom: 60px;  /* 改用 margin-bottom，不用 margin-top */
}

.title {
	margin-top: 20px;
	font-size: 24px;
	font-weight: bold;
	color: #333;
}

.subtitle {
	margin-top: 10px;
	font-size: 14px;
	color: #999;
}

.form-area {
	width: 100%;
}

.auth-tip {
	margin-top: 16px;
	text-align: center;
	font-size: 12px;
	color: #ff6b6b;
}

.agreement {
	margin-top: 24px;
	display: flex;
	justify-content: center;
}

.agree-label {
	display: flex;
	align-items: center;
	gap: 4px;
	flex-wrap: wrap;
	justify-content: center;
}

.agree-text {
	font-size: 12px;
	color: #666;
}

.agree-link {
	font-size: 12px;
	color: #007aff;
}

.footer {
	margin-top: 40px;
	text-align: center;
	font-size: 12px;
	color: #ccc;
}
</style>