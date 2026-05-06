<template>
	<view class="login-container">
		<view class="content-wrapper">
			<view class="header">
				<up-image 
					class="logo"
					src="/static/logo.png" 
					width="160rpx" 
					height="160rpx" 
					shape="circle"
				></up-image>
				<view class="title">Link Mind</view>
				<view class="subtitle">Link you mind, Build your world</view>
			</view>

			<view class="form-area">
				<up-button 
					class="login-btn"
					type="primary" 
					text="微信一键登录" 
					size="large" 
					shape="circle"
					:loading="loading"
					@click="startLogin"
				></up-button>
				
				<view class="agreement">
					<label class="agree-label" @click="toggleAgree">
						<checkbox :checked="isAgree" style="transform: scale(0.8)" />
						<text class="agree-text">已阅读并同意</text>
						<text class="agree-link" @click.stop="showAgreement('user')">《用户协议》</text>
						<text class="agree-text">和</text>
						<text class="agree-link" @click.stop="showAgreement('privacy')">《隐私政策》</text>
					</label>
				</view>
			</view>

			<view class="footer">
				<text>首次登录将自动注册账号</text>
			</view>
		</view>

		<!-- 授权弹窗 -->
		<view class="auth-popup" v-if="showAuthPopup" @click.stop>
			<view class="popup-mask" @click="closeAuthPopup"></view>
			<view class="popup-content">
				<view class="popup-header">
					<view class="popup-title">完善个人资料</view>
					<view class="popup-close" @click="closeAuthPopup">×</view>
				</view>
				
				<view class="popup-desc">请授权您的微信头像和昵称</view>
				
				<!-- 头像选择 -->
				<view class="avatar-section">
					<button 
						class="avatar-btn"
						open-type="chooseAvatar"
						@chooseavatar="onChooseAvatar"
					>
						<image 
							:src="tempAvatarDisplay || '/static/default-avatar.png'" 
							class="avatar-image"
							mode="aspectFill"
						></image>
						<text class="avatar-tip">{{ tempAvatar ? '点击更换头像' : '点击选择头像' }}</text>
					</button>
				</view>

				<!-- 昵称输入 -->
				<view class="nickname-section">
					<input 
						class="nickname-input"
						type="nickname"
						placeholder="请输入昵称"
						:value="tempNickname"
						@blur="onNicknameInput"
						@confirm="onNicknameConfirm"
						maxlength="20"
					/>
				</view>

				<button 
					class="confirm-btn"
					:class="{ 'btn-active': tempAvatar && tempNickname }"
					:disabled="!tempAvatar || !tempNickname"
					@click="confirmAuth"
				>
					确认授权并登录
				</button>
			</view>
		</view>
	</view>
</template>

<script setup>
import { ref, computed } from 'vue'
import { authApi, userApi, noteApi, config } from '@/api'

const loading = ref(false)
const isAgree = ref(false)
const showAuthPopup = ref(false)

const tempAvatar = ref('')
const tempNickname = ref('')

const tempAvatarDisplay = computed(() => {
	if (!tempAvatar.value) return ''
	if (tempAvatar.value.startsWith('http://') || tempAvatar.value.startsWith('https://') || tempAvatar.value.startsWith('data:') || tempAvatar.value.startsWith('/static/')) return tempAvatar.value
	if (tempAvatar.value.startsWith('/api/')) return config.baseURL + tempAvatar.value
	return config.baseURL + tempAvatar.value
})

const toggleAgree = () => {
	isAgree.value = !isAgree.value
}

const showAgreement = (type) => {
	const title = type === 'user' ? '用户协议' : '隐私政策'
	const content = type === 'user' 
		? '这里是用户协议内容...' 
		: '这里是隐私政策内容...'
	
	uni.showModal({
		title,
		content,
		showCancel: false,
		confirmText: '我知道了'
	})
}

// 预加载所有个人数据（登录成功后调用）
const preloadUserData = async () => {
	try {
		// 并行请求所有需要缓存的数据
		const [userInfo, stats] = await Promise.all([
			userApi.getUserInfo(),
			noteApi.getStats()  // 获取统计数据（笔记数、获赞数、收藏数）
		])
		
		// 合并用户信息和统计数据
		const fullUserInfo = {
			...userInfo,
			stats: {
				notes: stats?.notes || 0,
				likes: stats?.likes || 0,
				favorites: stats?.favorites || 0
			}
		}
		
		// 存入缓存
		uni.setStorageSync('userInfo', fullUserInfo)
		
		return fullUserInfo
	} catch (error) {
		console.error('预加载用户数据失败:', error)
		// 即使失败，也至少保存基础用户信息
		const userInfo = uni.getStorageSync('userInfo')
		if (!userInfo || !userInfo.stats) {
			uni.setStorageSync('userInfo', {
				...userInfo,
				stats: { notes: 0, likes: 0, favorites: 0 }
			})
		}
	}
}

const startLogin = async () => {
	if (!isAgree.value) {
		uni.showToast({ 
			title: '请先阅读并同意用户协议和隐私政策', 
			icon: 'none'
		})
		return
	}
	
	loading.value = true;
	try {
		// 获取微信 code
		const loginRes = await uni.login({ provider: 'weixin' });
		
		// 尝试静默登录（不传昵称头像）
		const res = await authApi.login(loginRes.code, {});
		
		if (res.isNewUser) {
			// 是新用户：显示授权弹窗
			showAuthPopup.value = true;
		} else {
			// 是老用户：直接处理登录成功
			uni.showLoading({ title: '正在登录...', mask: true });
			await handleLoginSuccess(res.token);
		}
	} catch (error) {
		console.error('登录失败:', error)
		uni.showToast({ title: '登录失败', icon: 'none' });
	} finally {
		loading.value = false;
	}
}

// 提取公共登录成功逻辑
const handleLoginSuccess = async (token) => {
	uni.setStorageSync('token', token)
	uni.hideLoading();
	
	// 预加载所有个人数据（用户信息 + 统计数据）
	uni.showLoading({ title: '加载中...', mask: true });
	await preloadUserData();
	uni.hideLoading();
	
	uni.showToast({ title: '登录成功', icon: 'success' });
	setTimeout(() => {
		const redirectUrl = uni.getStorageSync('redirectUrl') || '/pages/community/community'
		uni.removeStorageSync('redirectUrl')
		uni.reLaunch({ url: redirectUrl });
	}, 1500);
};

const closeAuthPopup = () => {
	showAuthPopup.value = false
	tempAvatar.value = ''
	tempAvatarPath.value = ''
	tempNickname.value = ''
}

const tempAvatarPath = ref('') // 本地临时路径，登录后再上传

const onChooseAvatar = (e) => {
	const avatarUrl = e.detail.avatarUrl;
	if (!avatarUrl) return;
	// 先存本地路径，登录成功后再上传到后端
	tempAvatarPath.value = avatarUrl;
	tempAvatar.value = avatarUrl;
	uni.showToast({ title: '头像已选择', icon: 'success' });
}

const onNicknameInput = (e) => {
	tempNickname.value = (e.detail.value || '').trim()
}

const onNicknameConfirm = (e) => {
	tempNickname.value = (e.detail.value || '').trim()
	if (tempAvatar.value && tempNickname.value) {
		confirmAuth()
	}
}

const confirmAuth = async () => {
	const nickname = tempNickname.value.trim()

	// 验证头像
	if (!tempAvatarPath.value) {
		uni.showToast({ title: '请先点击选择头像', icon: 'none' })
		return
	}

	// 验证昵称
	if (!nickname) {
		uni.showToast({ title: '请填写昵称', icon: 'none' })
		return
	}

	// 关闭弹窗
	showAuthPopup.value = false
	loading.value = true
	uni.showLoading({ title: '正在创建账号...', mask: true });

	try {
		const loginRes = await uni.login({ provider: 'weixin' });

		// 先不传头像URL（本地路径后端不识别），只传昵称
		const res = await authApi.login(loginRes.code, {
			nickName: nickname,
			avatarUrl: ''
		});

		if (res.token) {
			// 登录成功后，上传头像文件到后端
			await uploadAvatarAfterLogin(tempAvatarPath.value, res.token);
			await handleLoginSuccess(res.token);
		} else if (res.isNewUser) {
			showAuthPopup.value = true
			uni.showToast({ title: '请完善头像和昵称', icon: 'none' })
		}
	} catch (error) {
		console.error('授权登录失败:', error)
		uni.hideLoading();
		showAuthPopup.value = true
		uni.showToast({ title: error.message || '授权登录失败', icon: 'none' });
	} finally {
		loading.value = false;
	}
}

// 登录成功后上传头像文件
const uploadAvatarAfterLogin = (filePath, token) => {
	return new Promise((resolve, reject) => {
		uni.uploadFile({
			url: config.baseURL + '/api/wx/user/avatar',
			filePath: filePath,
			name: 'file',
			header: {
				'Authorization': 'Bearer ' + token
			},
			success: (uploadRes) => {
				const res = JSON.parse(uploadRes.data);
				if (res.code === 200) {
					tempAvatar.value = res.data.url;
					resolve(res.data.url);
				} else {
					console.warn('头像上传失败，使用默认头像');
					resolve('');
				}
			},
			fail: (err) => {
				console.warn('头像上传失败:', err);
				resolve(''); // 上传失败不阻断登录流程
			}
		});
	});
}
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
	justify-content: center;
	padding: 40px 30px;
	box-sizing: border-box;
}

.header {
	display: flex;
	flex-direction: column;
	align-items: center;
	margin-bottom: 60px;
}

.logo {
	margin-bottom: 20px;
}

.title {
	margin-top: 20px;
	font-size: 48rpx;
	font-weight: bold;
	color: #333;
}

.subtitle {
	margin-top: 10px;
	font-size: 28rpx;
	color: #999;
}

.form-area {
	width: 100%;
}

.login-btn {
	background: linear-gradient(135deg, #07c160, #05a54e);
	border: none;
	
	&:active {
		opacity: 0.8;
	}
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
	font-size: 24rpx;
	color: #666;
}

.agree-link {
	font-size: 24rpx;
	color: #07c160;
}

.footer {
	margin-top: 40px;
	text-align: center;
	font-size: 24rpx;
	color: #ccc;
}


.auth-popup {
	position: fixed;
	top: 0;
	left: 0;
	right: 0;
	bottom: 0;
	z-index: 1000;
	
	.popup-mask {
		position: absolute;
		top: 0;
		left: 0;
		right: 0;
		bottom: 0;
		background: rgba(0, 0, 0, 0.5);
		animation: fadeIn 0.3s ease;
	}
	
	.popup-content {
		position: absolute;
		bottom: 0;
		left: 0;
		right: 0;
		background: #fff;
		border-radius: 32rpx 32rpx 0 0;
		padding: 48rpx 32rpx 64rpx;
		animation: slideUp 0.3s ease;
		
		.popup-header {
			display: flex;
			justify-content: space-between;
			align-items: center;
			margin-bottom: 16rpx;
			
			.popup-title {
				font-size: 36rpx;
				font-weight: bold;
				color: #333;
			}
			
			.popup-close {
				font-size: 48rpx;
				color: #999;
				line-height: 1;
				padding: 8rpx;
				cursor: pointer;
				
				&:active {
					opacity: 0.6;
				}
			}
		}
		
		.popup-desc {
			font-size: 28rpx;
			color: #999;
			margin-bottom: 48rpx;
			padding-bottom: 16rpx;
			border-bottom: 1rpx solid #f0f0f0;
		}
		
		.avatar-section {
			display: flex;
			justify-content: center;
			margin-bottom: 48rpx;
			
			.avatar-btn {
				background: transparent;
				display: flex;
				flex-direction: column;
				align-items: center;
				padding: 0;
				margin: 0;
				border: none;
				
				&::after {
					border: none;
				}
				
				.avatar-image {
					width: 120rpx;
					height: 120rpx;
					border-radius: 50%;
					background: #f5f5f5;
					border: 2rpx solid #e0e0e0;
					margin-bottom: 16rpx;
				}
				
				.avatar-tip {
					font-size: 24rpx;
					color: #07c160;
				}
			}
		}
		
		.nickname-section {
			margin-bottom: 48rpx;
			
			.nickname-input {
				width: 100%;
				height: 88rpx;
				border: 2rpx solid #e0e0e0;
				border-radius: 16rpx;
				padding: 0 24rpx;
				font-size: 28rpx;
				box-sizing: border-box;
				background: #fff;
				
				&:focus {
					border-color: #07c160;
				}
				
				&::placeholder {
					color: #ccc;
				}
			}
		}
		
		.confirm-btn {
			width: 100%;
			height: 88rpx;
			background: #e0e0e0;
			color: #fff;
			border-radius: 44rpx;
			font-size: 32rpx;
			font-weight: 500;
			border: none;
			transition: all 0.3s ease;
			
			&.btn-active {
				background: linear-gradient(135deg, #07c160, #05a54e);
				
				&:active {
					opacity: 0.8;
				}
			}
			
			&[disabled] {
				opacity: 0.6;
			}
		}
	}
}

@keyframes slideUp {
	from {
		transform: translateY(100%);
	}
	to {
		transform: translateY(0);
	}
}

@keyframes fadeIn {
	from {
		opacity: 0;
	}
	to {
		opacity: 1;
	}
}
</style>