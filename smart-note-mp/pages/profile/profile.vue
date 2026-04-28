<template>
	<view class="profile-container">
		<!-- 头像区域 -->
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

		<!-- 统计数据 -->
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

		<!-- Tab 列表 -->
		<view class="u-margin-top-20">
			<u-tabs :list="tabs" @click="handleTabChange" :current="current"></u-tabs>
			<view class="list-content">
				<!-- 我的笔记 -->
				<block v-if="current === 0">
					<u-empty v-if="myNotesList.length === 0" mode="list" text="暂无笔记" marginTop="100"></u-empty>
					<u-cell-group v-else>
						<u-cell 
							v-for="(item, i) in myNotesList" 
							:key="i" 
							:title="item.title" 
							:label="item.updateTime" 
							isLink 
							@click="goToNoteDetail(item.noteId)"
						>
							<template #icon>
								<u-icon name="file-text" size="18" customStyle="margin-right:10rpx"></u-icon>
							</template>
						</u-cell>
						<!-- 查看全部按钮 -->
						<u-cell title="查看全部笔记" isLink @click="goToFullList('notes')">
							<template #icon>
								<u-icon name="arrow-right" size="16" customStyle="margin-right:10rpx" color="#999"></u-icon>
							</template>
						</u-cell>
					</u-cell-group>
				</block>
				
				<!-- 我的收藏 -->
				<block v-if="current === 1">
					<u-empty v-if="favoritesList.length === 0" mode="list" text="暂无收藏" marginTop="100"></u-empty>
					<u-cell-group v-else>
						<u-cell 
							v-for="(item, i) in favoritesList" 
							:key="i" 
							:title="item.title" 
							:label="item.updateTime" 
							isLink 
							@click="goToNoteDetail(item.noteId)"
						>
							<template #icon>
								<u-icon name="star" size="18" customStyle="margin-right:10rpx" color="#f5a623"></u-icon>
							</template>
						</u-cell>
						<u-cell title="查看全部收藏" isLink @click="goToFullList('favorites')">
							<template #icon>
								<u-icon name="arrow-right" size="16" customStyle="margin-right:10rpx" color="#999"></u-icon>
							</template>
						</u-cell>
					</u-cell-group>
				</block>
				
				<!-- 赞过 -->
				<block v-if="current === 2">
					<u-empty v-if="likedList.length === 0" mode="list" text="暂无赞过" marginTop="100"></u-empty>
					<u-cell-group v-else>
						<u-cell 
							v-for="(item, i) in likedList" 
							:key="i" 
							:title="item.title" 
							:label="item.updateTime" 
							isLink 
							@click="goToNoteDetail(item.noteId)"
						>
							<template #icon>
								<u-icon name="thumb-up" size="18" customStyle="margin-right:10rpx" color="#1890ff"></u-icon>
							</template>
						</u-cell>
						<u-cell title="查看全部赞过" isLink @click="goToFullList('liked')">
							<template #icon>
								<u-icon name="arrow-right" size="16" customStyle="margin-right:10rpx" color="#999"></u-icon>
							</template>
						</u-cell>
					</u-cell-group>
				</block>
			</view>
		</view>

		<!-- 其他菜单 -->
		<view class="u-margin-top-20">
			<u-cell-group>
				<u-cell icon="tags" title="我的标签" isLink @click="goToTagManage"></u-cell>
				<u-cell icon="trash" title="回收站" isLink @click="goToRecycleBin"></u-cell>
				<u-cell icon="setting" title="设置" isLink @click="msg"></u-cell>
				<u-cell icon="info-circle" title="关于我们" isLink @click="msg"></u-cell>
			</u-cell-group>
		</view>
		
		<custom-tab-bar />
	</view>
</template>

<script setup>
import { ref, computed } from 'vue'
import { onShow, onPullDownRefresh } from '@dcloudio/uni-app'
import CustomTabBar from '@/components/custom-tab-bar/index.vue'
import { noteApi } from '@/api'

const current = ref(0)
const tabs = [{name: '我的笔记'}, {name: '我的收藏'}, {name: '赞过'}]

// 用户信息
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

// 各列表数据（只存最近3条）
const myNotesList = ref([])
const favoritesList = ref([])
const likedList = ref([])

// Tab 切换时懒加载
const handleTabChange = (e) => {
	const newIndex = e.index
	if (current.value === newIndex) return
	current.value = newIndex
	
	// 根据切换的 Tab 加载对应数据
	if (newIndex === 0 && myNotesList.value.length === 0) {
		fetchMyNotes()
	} else if (newIndex === 1 && favoritesList.value.length === 0) {
		fetchFavorites()
	} else if (newIndex === 2 && likedList.value.length === 0) {
		fetchLiked()
	}
}

// 加载我的笔记（最近3条）
const fetchMyNotes = async () => {
	try {
		const res = await noteApi.getMyNotes({ pageNum: 1, pageSize: 3 })
		if (res && res.records) {
			myNotesList.value = res.records.map(item => ({
				noteId: item.noteId,
				title: item.title,
				updateTime: item.updateTime ? item.updateTime.split('T')[0] : ''
			}))
		}
	} catch (error) {
		console.error('获取我的笔记失败:', error)
	}
}

// 加载收藏列表（最近3条）
const fetchFavorites = async () => {
	try {
		const res = await noteApi.getFavorites({ pageNum: 1, pageSize: 3 })
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

// 加载赞过列表（最近3条）
const fetchLiked = async () => {
	try {
		const res = await noteApi.getLiked({ pageNum: 1, pageSize: 3 })
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

// 获取统计数据
const fetchStats = async () => {
	try {
		const stats = await noteApi.getStats()
		if (stats) {
			userInfo.value.stats = {
				notes: stats.notes || 0,
				likes: stats.likes || 0,
				favorites: stats.favorites || 0
			}
			// 更新缓存
			const cached = uni.getStorageSync('userInfo')
			if (cached) {
				cached.stats = userInfo.value.stats
				uni.setStorageSync('userInfo', cached)
			}
		}
	} catch (error) {
		console.error('获取统计数据失败:', error)
	}
}

// 从缓存加载用户信息
const loadUserInfoFromCache = () => {
	try {
		const cached = uni.getStorageSync('userInfo')
		if (cached && cached.userId) {
			userInfo.value = {
				...userInfo.value,
				avatar: cached.avatar || '',
				nickname: cached.nickname || cached.name || '用户',
				name: cached.name || '',
				signature: cached.signature || '',
				bio: cached.signature || cached.bio || '暂无签名',
				stats: cached.stats || { notes: 0, likes: 0, favorites: 0 }
			}
			return true
		}
		return false
	} catch (error) {
		console.error('读取用户缓存失败:', error)
		return false
	}
}

// 页面显示
onShow(() => {
	const hasCache = loadUserInfoFromCache()
	
	// 统计数据总是更新
	fetchStats()
	
	// 懒加载：只加载当前 Tab 的数据
	if (current.value === 0) {
		fetchMyNotes()
	} else if (current.value === 1) {
		fetchFavorites()
	} else if (current.value === 2) {
		fetchLiked()
	}
})

// 下拉刷新
onPullDownRefresh(() => {
	Promise.all([
		fetchStats(),
		fetchMyNotes(),
		fetchFavorites(),
		fetchLiked()
	]).finally(() => {
		uni.stopPullDownRefresh()
		uni.showToast({ title: '刷新成功', icon: 'success', duration: 1000 })
	})
})

// 跳转到完整列表页
const goToFullList = (type) => {
	let title = ''
	if (type === 'notes') title = '我的笔记'
	else if (type === 'favorites') title = '我的收藏'
	else title = '我的赞过'
	
	uni.navigateTo({
		url: `/pages/note-list/note-list?type=${type}&title=${title}`
	})
}

// 跳转到个人信息页
const goToUserInfo = () => {
	uni.navigateTo({ url: '/pages/user-info/user-info' })
}

// 跳转到笔记详情
const goToNoteDetail = (noteId) => {
	uni.navigateTo({ url: `/pages/note-detail/note-detail?id=${noteId}` })
}

const goToTagManage = () => {
	uni.navigateTo({ url: '/pages/tag-manage/tag-manage' })
}

const goToRecycleBin = () => {
	uni.navigateTo({ url: '/pages/recycle-bin/recycle-bin' })
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