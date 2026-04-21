<template>
	<view class="profile-container">
		<!-- 头像区域 - 添加点击事件 -->
		<view class="header-box u-flex u-col-center" @click="goToUserInfo">
			<u-avatar :src="userInfo.avatar" size="60"></u-avatar>
			<view class="u-margin-left-30">
				<view class="u-font-36 u-main-color u-font-weight">{{ userInfo.nickname }}</view>
				<view class="u-font-24 u-tips-color u-margin-top-10">{{ userInfo.bio }}</view>
			</view>
			<view class="edit-icon">
				<u-icon name="edit-pen" color="#999" size="32"></u-icon>
			</view>
		</view>

		<u-grid :border="false" col="3" customStyle="background:#fff; padding:30rpx 0;">
			<u-grid-item>
				<text class="num">{{ userInfo.stats.notes }}</text>
				<text class="label">笔记</text>
			</u-grid-item>
			<u-grid-item>
				<text class="num">{{ userInfo.stats.likes }}</text>
				<text class="label">获赞</text>
			</u-grid-item>
			<u-grid-item>
				<text class="num">{{ userInfo.stats.favorites }}</text>
				<text class="label">收藏</text>
			</u-grid-item>
		</u-grid>

		<view class="u-margin-top-20">
			<u-tabs :list="tabs" @click="e => current = e.index"></u-tabs>
			<view class="list-content">
				<u-empty v-if="current === 1" mode="list" text="暂无收藏" marginTop="100"></u-empty>
				<u-cell-group v-else>
					<u-cell v-for="(n, i) in myNotes" :key="i" :title="n.title" :label="n.updateTime" isLink>
						<template #icon>
							<u-icon name="file-text" size="18" customStyle="margin-right:10rpx"></u-icon>
						</template>
					</u-cell>
				</u-cell-group>
			</view>
		</view>

		<view class="u-margin-top-20">
			<u-cell-group>
				<u-cell icon="tags" title="我的标签" isLink @click="goToTagManage"></u-cell>
				<u-cell icon="setting" title="设置" isLink @click="msg"></u-cell>
				<u-cell icon="info-circle" title="关于我们" isLink @click="msg"></u-cell>
			</u-cell-group>
		</view>
		
		<!-- 添加自定义 TabBar -->
		<custom-tab-bar />
	</view>
</template>

<script setup>
import { ref } from 'vue';
import { onShow } from '@dcloudio/uni-app'
import CustomTabBar from '@/components/custom-tab-bar/index.vue'

const current = ref(0);
const tabs = [{name: '我的笔记'}, {name: '我的收藏'}, {name: '赞过'}];

// 用户信息（模拟数据）
const userInfo = ref({
	avatar: 'https://via.placeholder.com/100',
	nickname: '极客开发者',
	bio: '坚持记录，沉淀知识。',
	email: 'geek@example.com',
	phone: '138****8888',
	gender: '男',
	location: '中国·北京',
	registerTime: '2024-01-15',
	stats: {
		notes: 12,
		likes: 128,
		favorites: 45
	}
})

// 我的笔记列表（模拟数据）
const myNotes = ref([
	{ id: 1, title: 'Vue3 组合式API详解', updateTime: '2026-04-20' },
	{ id: 2, title: '微信小程序开发踩坑记录', updateTime: '2026-04-18' },
	{ id: 3, title: 'TypeScript 泛型使用指南', updateTime: '2026-04-15' }
])

// 模拟接口：获取用户信息
const fetchUserInfo = async () => {
	// 模拟网络请求延迟
	await new Promise(resolve => setTimeout(resolve, 500))
	
	// 模拟后端返回的数据
	const mockData = {
		code: 200,
		data: {
			avatar: 'https://via.placeholder.com/100',
			nickname: '极客开发者',
			bio: '坚持记录，沉淀知识。',
			email: 'geek@example.com',
			phone: '138****8888',
			gender: '男',
			location: '中国·北京',
			registerTime: '2024-01-15',
			stats: {
				notes: 12,
				likes: 128,
				favorites: 45
			}
		}
	}
	
	return mockData
}

// 模拟接口：获取我的笔记列表
const fetchMyNotes = async () => {
	await new Promise(resolve => setTimeout(resolve, 300))
	
	return {
		code: 200,
		data: [
			{ id: 1, title: 'Vue3 组合式API详解', updateTime: '2026-04-20' },
			{ id: 2, title: '微信小程序开发踩坑记录', updateTime: '2026-04-18' },
			{ id: 3, title: 'TypeScript 泛型使用指南', updateTime: '2026-04-15' }
		]
	}
}

// 加载页面数据
const loadPageData = async () => {
	uni.showLoading({ title: '加载中...' })
	
	try {
		// 并行请求用户信息和笔记列表
		const [userRes, notesRes] = await Promise.all([
			fetchUserInfo(),
			fetchMyNotes()
		])
		
		if (userRes.code === 200) {
			userInfo.value = userRes.data
		}
		
		if (notesRes.code === 200) {
			myNotes.value = notesRes.data
		}
	} catch (error) {
		console.error('加载数据失败:', error)
		uni.showToast({ title: '加载失败', icon: 'none' })
	} finally {
		uni.hideLoading()
	}
}

// 跳转到个人详细信息页
const goToUserInfo = () => {
	// 将用户信息作为参数传递
	const userData = encodeURIComponent(JSON.stringify(userInfo.value))
	uni.navigateTo({
		url: `/pages/user-info/user-info?data=${userData}`
	})
}

const goToTagManage = () => {
	uni.navigateTo({
		url: '/pages/tag-manage/tag-manage'
	})
}

const msg = () => {
	uni.showToast({ title: '开发中', icon: 'none' })
}

// 页面显示时加载数据
onShow(() => {
	loadPageData()
})
</script>

<style lang="scss">
.profile-container { 
	background: #f5f7f9; 
	min-height: 100vh; 
	padding-bottom: 100rpx; 
}

.header-box { 
	background: #fff; 
	padding: 60rpx 40rpx; 
	border-bottom: 1px solid #f5f5f5;
	position: relative;
	
	.edit-icon {
		position: absolute;
		right: 40rpx;
		top: 50%;
		transform: translateY(-50%);
	}
}

.num { 
	font-size: 34rpx; 
	font-weight: bold; 
	color: #1890ff; 
}

.label { 
	font-size: 24rpx; 
	color: #909399; 
	margin-top: 10rpx; 
}

.list-content { 
	background: #fff; 
	min-height: 300rpx; 
}
</style>