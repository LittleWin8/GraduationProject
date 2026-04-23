<template>
	<view class="profile-container">
		<!-- 头像区域 - 添加点击事件 -->
		<view class="header-box u-flex u-col-center" @click="goToUserInfo">
			<Avatar 
				:src="userInfo.avatar"
				:size="100"
				shape="circle"
				default-type="user"
			></Avatar>
			<view class="u-margin-left-30">
				<view class="u-font-36 u-main-color u-font-weight">{{ userInfo.nickname || userInfo.name }}</view>
				<view class="u-font-24 u-tips-color u-margin-top-10">{{ userInfo.signature || userInfo.bio || '暂无签名' }}</view>
			</view>
			<view class="edit-icon">
				<u-icon name="edit-pen" color="#999" size="32"></u-icon>
			</view>
		</view>

		<u-grid :border="false" col="3" customStyle="background:#fff; padding:30rpx 0;">
			<u-grid-item>
				<text class="num">{{ userInfo.stats?.notes || 0 }}</text>
				<text class="label">笔记</text>
			</u-grid-item>
			<u-grid-item>
				<text class="num">{{ userInfo.stats?.likes || 0 }}</text>
				<text class="label">获赞</text>
			</u-grid-item>
			<u-grid-item>
				<text class="num">{{ userInfo.stats?.favorites || 0 }}</text>
				<text class="label">收藏</text>
			</u-grid-item>
		</u-grid>

		<view class="u-margin-top-20">
			<u-tabs :list="tabs" @click="e => current = e.index"></u-tabs>
			<view class="list-content">
				<!-- 我的收藏列表 -->
				<u-empty v-if="current === 1 && favoritesList.length === 0" mode="list" text="暂无收藏" marginTop="100"></u-empty>
				<u-cell-group v-else-if="current === 1">
					<u-cell v-for="(item, i) in favoritesList" :key="i" :title="item.title" :label="item.updateTime" isLink @click="goToNoteDetail(item.noteId)">
						<template #icon>
							<u-icon name="star" size="18" customStyle="margin-right:10rpx" color="#f5a623"></u-icon>
						</template>
					</u-cell>
				</u-cell-group>
				
				<!-- 我的笔记列表 -->
				<u-empty v-else-if="current === 0 && myNotesList.length === 0" mode="list" text="暂无笔记" marginTop="100"></u-empty>
				<u-cell-group v-else-if="current === 0">
					<u-cell v-for="(item, i) in myNotesList" :key="i" :title="item.title" :label="item.updateTime" isLink @click="goToNoteDetail(item.noteId)">
						<template #icon>
							<u-icon name="file-text" size="18" customStyle="margin-right:10rpx"></u-icon>
						</template>
					</u-cell>
				</u-cell-group>
				
				<!-- 赞过列表 -->
				<u-empty v-else-if="current === 2 && likedList.length === 0" mode="list" text="暂无赞过" marginTop="100"></u-empty>
				<u-cell-group v-else-if="current === 2">
					<u-cell v-for="(item, i) in likedList" :key="i" :title="item.title" :label="item.updateTime" isLink @click="goToNoteDetail(item.noteId)">
						<template #icon>
							<u-icon name="thumb-up" size="18" customStyle="margin-right:10rpx" color="#1890ff"></u-icon>
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
import { ref } from 'vue'
import { onShow, onPullDownRefresh } from '@dcloudio/uni-app'
import CustomTabBar from '@/components/custom-tab-bar/index.vue'
import { noteApi } from '@/api'

const current = ref(0)
const tabs = [{name: '我的笔记'}, {name: '我的收藏'}, {name: '赞过'}]

// 用户信息（从缓存读取）
const userInfo = ref({
	avatar: '',
	nickname: '',
	name: '',
	signature: '',
	bio: '',
	stats: {
		notes: 0,
		likes: 0,
		favorites: 0
	}
})

// 我的笔记列表
const myNotesList = ref([])

// 我的收藏列表
const favoritesList = ref([])

// 赞过列表
const likedList = ref([])

// 加载状态
const loading = ref(false)

// 从缓存加载用户信息
const loadUserInfoFromCache = () => {
	try {
		const cached = uni.getStorageSync('userInfo')
		if (cached && cached.userId) {
			console.log('从缓存加载用户信息:', cached)
			userInfo.value = {
				...userInfo.value,
				avatar: cached.avatar || '',
				nickname: cached.nickname || cached.name || '用户',
				name: cached.name || '',
				signature: cached.signature || '',
				bio: cached.signature || cached.bio || '暂无签名',
				location: cached.city || '',
				registerTime: cached.createTime || '',
				stats: cached.stats || { notes: 0, likes: 0, favorites: 0 }
			}
			return true
		}
		console.log('缓存中无用户信息')
		return false
	} catch (error) {
		console.error('读取用户缓存失败:', error)
		return false
	}
}

// 更新缓存中的统计数据
const updateStatsCache = (stats) => {
	try {
		const cached = uni.getStorageSync('userInfo')
		if (cached) {
			cached.stats = stats
			uni.setStorageSync('userInfo', cached)
			// 同时更新当前显示
			userInfo.value.stats = stats
		}
	} catch (error) {
		console.error('更新统计数据缓存失败:', error)
	}
}

// 获取我的笔记列表（分页）
const fetchMyNotes = async (isLoadMore = false) => {
	try {
		const res = await noteApi.getMyNotes(1, 50)
		if (res && res.records) {
			myNotesList.value = res.records.map(item => ({
				noteId: item.noteId,
				title: item.title,
				updateTime: item.updateTime ? item.updateTime.split('T')[0] : ''
			}))
		}
	} catch (error) {
		console.error('获取我的笔记列表失败:', error)
	}
}

// 获取收藏列表
const fetchFavorites = async () => {
	try {
		const res = await noteApi.getFavorites(1, 50)
		if (res && res.records) {
			favoritesList.value = res.records.map(item => ({
				noteId: item.noteId,
				title: item.title,
				updateTime: item.updateTime ? item.updateTime.split('T')[0] : ''
			}))
		}
	} catch (error) {
		console.error('获取收藏列表失败:', error)
	}
}

// 获取赞过列表
const fetchLiked = async () => {
	try {
		const res = await noteApi.getLiked(1, 50)
		if (res && res.records) {
			likedList.value = res.records.map(item => ({
				noteId: item.noteId,
				title: item.title,
				updateTime: item.updateTime ? item.updateTime.split('T')[0] : ''
			}))
		}
	} catch (error) {
		console.error('获取赞过列表失败:', error)
	}
}

// 静默获取最新统计数据
const fetchLatestStats = async () => {
	try {
		const stats = await noteApi.getStats()
		console.log('获取最新统计数据:', stats)
		if (stats) {
			updateStatsCache({
				notes: stats.notes || 0,
				likes: stats.likes || 0,
				favorites: stats.favorites || 0
			})
		}
	} catch (error) {
		console.error('获取统计数据失败:', error)
	}
}

// 静默刷新所有数据（后台更新）
const silentRefresh = async () => {
	console.log('开始静默刷新个人中心数据...')
	try {
		// 并行请求所有数据
		await Promise.all([
			fetchLatestStats(),      // 更新统计数据
			fetchMyNotes(),          // 更新我的笔记
			fetchFavorites(),        // 更新收藏列表
			fetchLiked()             // 更新赞过列表
		])
		console.log('静默刷新完成')
	} catch (error) {
		console.error('静默刷新失败:', error)
	}
}

// 页面显示时加载数据
onShow(() => {
	// 1. 先从缓存加载用户信息（立即显示）
	const hasCache = loadUserInfoFromCache()
	
	if (hasCache) {
		// 有缓存：立即显示缓存数据 + 静默更新
		console.log('有缓存数据，后台静默更新中...')
		silentRefresh()
	} else {
		// 无缓存：显示 loading 并请求数据
		console.log('无缓存数据，请求后端...')
		uni.showLoading({ title: '加载中...', mask: true })
		Promise.all([
			fetchLatestStats(),
			fetchMyNotes(),
			fetchFavorites(),
			fetchLiked()
		]).finally(() => {
			uni.hideLoading()
		})
	}
})

// 下拉刷新
onPullDownRefresh(() => {
	console.log('下拉刷新...')
	Promise.all([
		fetchLatestStats(),
		fetchMyNotes(),
		fetchFavorites(),
		fetchLiked()
	]).finally(() => {
		uni.stopPullDownRefresh()
		uni.showToast({ title: '刷新成功', icon: 'success', duration: 1000 })
	})
})

// 跳转到个人信息页
const goToUserInfo = () => {
	uni.navigateTo({
		url: '/pages/user-info/user-info'
	})
}

// 跳转到笔记详情
const goToNoteDetail = (noteId) => {
	uni.navigateTo({
		url: `/pages/note-detail/note-detail?id=${noteId}`
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