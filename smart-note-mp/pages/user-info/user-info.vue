<template>
	<view class="user-info-page">
		<!-- 头像 -->
		<view class="avatar-section">
			<u-avatar :src="userInfo.avatar" size="100"></u-avatar>
			<view class="avatar-edit" @click="changeAvatar">
				<u-icon name="camera" color="#fff" size="28"></u-icon>
			</view>
		</view>

		<!-- 信息列表 -->
		<u-cell-group>
			<u-cell title="昵称" :value="userInfo.nickname" isLink @click="editField('nickname')"></u-cell>
			<u-cell title="个性签名" :value="userInfo.bio" isLink @click="editField('bio')"></u-cell>
			<u-cell title="邮箱" :value="userInfo.email" isLink @click="editField('email')"></u-cell>
			<u-cell title="手机号" :value="userInfo.phone" isLink @click="editField('phone')"></u-cell>
			<u-cell title="性别" :value="userInfo.gender" isLink @click="editField('gender')"></u-cell>
			<u-cell title="地区" :value="userInfo.location" isLink @click="editField('location')"></u-cell>
			<u-cell title="注册时间" :value="userInfo.registerTime"></u-cell>
		</u-cell-group>

		<!-- 退出登录按钮 -->
		<view class="logout-btn">
			<u-button type="error" text="退出登录" shape="circle" @click="logout"></u-button>
		</view>
		
		<custom-tab-bar />
	</view>
</template>

<script setup>
import { ref } from 'vue'
import { onLoad } from '@dcloudio/uni-app'
import CustomTabBar from '@/components/custom-tab-bar/index.vue'

const userInfo = ref({
	avatar: '',
	nickname: '',
	bio: '',
	email: '',
	phone: '',
	gender: '',
	location: '',
	registerTime: '',
	stats: {}
})

onLoad((options) => {
	if (options.data) {
		try {
			userInfo.value = JSON.parse(decodeURIComponent(options.data))
		} catch (e) {
			console.error('解析用户数据失败:', e)
		}
	}
})

// 编辑字段
const editField = (field) => {
	const fieldNames = {
		nickname: '昵称',
		bio: '个性签名',
		email: '邮箱',
		phone: '手机号',
		gender: '性别',
		location: '地区'
	}
	
	uni.showToast({ 
		title: `编辑${fieldNames[field]}功能开发中`, 
		icon: 'none' 
	})
}

// 更换头像
const changeAvatar = () => {
	uni.showToast({ title: '更换头像功能开发中', icon: 'none' })
}

// 退出登录
const logout = () => {
	uni.showModal({
		title: '提示',
		content: '确定要退出登录吗？',
		success: (res) => {
			if (res.confirm) {
				// 清除本地存储
				uni.removeStorageSync('token')
				uni.removeStorageSync('userInfo')
				
				// 跳转到登录页
				uni.reLaunch({
					url: '/pages/login/login'
				})
			}
		}
	})
}
</script>

<style scoped>
.user-info-page {
	min-height: 100vh;
	background: #f5f7f9;
	padding-bottom: calc(160rpx + env(safe-area-inset-bottom));
}

.avatar-section {
	position: relative;
	display: flex;
	justify-content: center;
	padding: 60rpx 0;
	background: #fff;
	margin-bottom: 20rpx;
}

.avatar-edit {
	position: absolute;
	bottom: 50rpx;
	right: calc(50% - 70rpx);
	background: rgba(0, 0, 0, 0.6);
	width: 56rpx;
	height: 56rpx;
	border-radius: 50%;
	display: flex;
	align-items: center;
	justify-content: center;
}

.logout-btn {
	padding: 60rpx 30rpx;
}
</style>