<template>
	<view class="tag-notes-page">
		<view class="tag-header">
			<text class="tag-name">{{ tagName }}</text>
			<text class="note-count">{{ total }}篇笔记</text>
		</view>
		
		<!-- 加载更多组件（自定义） -->
		<view class="notes-list">
			<view v-if="loading && notes.length === 0" class="loading-container">
				<view class="loading-spinner"></view>
				<text class="loading-text">加载中...</text>
			</view>
			
			<view v-else-if="!loading && notes.length === 0" class="empty-container">
				<image src="/static/empty-notes.png" mode="aspectFit" class="empty-image"></image>
				<text class="empty-text">暂无笔记</text>
			</view>
			
			<note-card 
				v-for="item in notes" 
				:key="item.id" 
				:note="item"
				@like="onLike"
				@click="goToDetail"
			/>
			
			<!-- 自定义加载更多状态 -->
			<view v-if="notes.length > 0" class="loadmore-container">
				<view v-if="loading" class="loadmore-loading">
					<view class="loading-spinner small"></view>
					<text class="loadmore-text">加载中...</text>
				</view>
				<view v-else-if="!hasMore" class="loadmore-nomore">
					<text class="loadmore-text">没有更多了</text>
				</view>
				<view v-else class="loadmore-more" @click="loadMore">
					<text class="loadmore-text">点击加载更多</text>
				</view>
			</view>
		</view>
		
		<custom-tab-bar />
	</view>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { onLoad } from '@dcloudio/uni-app'
import NoteCard from '@/components/notecard/index.vue';
import CustomTabBar from '@/components/custom-tab-bar/index.vue'
import { tagApi, interactionApi } from '@/api/index.js'

const tagId = ref('')
const tagName = ref('')
const notes = ref([])
const loading = ref(false)
const pageNum = ref(1)
const pageSize = ref(10)
const total = ref(0)
const hasMore = ref(true)

// 加载笔记列表
const loadNotes = async (isRefresh = false) => {
	if (loading.value) return
	
	// 如果没有更多数据且不是刷新，则不加载
	if (!isRefresh && !hasMore.value) return
	
	loading.value = true
	
	try {
		const res = await tagApi.getNotesByTag(tagId.value, pageNum.value, pageSize.value)
		
		// 根据后端返回结构调整
		const list = res.records || res.list || res.data || []
		const totalCount = res.total || 0
		
		if (isRefresh) {
			notes.value = list
			pageNum.value = 1
		} else {
			notes.value = [...notes.value, ...list]
		}
		
		total.value = totalCount
		
		// 判断是否还有更多数据
		hasMore.value = notes.value.length < totalCount
		
	} catch (error) {
		console.error('加载标签笔记失败:', error)
		uni.showToast({ title: '加载失败', icon: 'none' })
		if (isRefresh) {
			notes.value = []
		}
	} finally {
		loading.value = false
		// 停止下拉刷新
		uni.stopPullDownRefresh()
	}
}

// 加载更多
const loadMore = () => {
	if (!loading.value && hasMore.value) {
		pageNum.value++
		loadNotes(false)
	}
}

// 刷新
const onRefresh = () => {
	pageNum.value = 1
	hasMore.value = true
	loadNotes(true)
}

// 点赞处理
const onLike = async (noteId) => {
	const item = notes.value.find(n => n.id === noteId)
	if (!item) return
	
	try {
		// 调用点赞/取消点赞接口
		await interactionApi.toggleLike(noteId)
		
		// 更新UI
		item.isLiked = !item.isLiked
		item.likes = (item.likes || 0) + (item.isLiked ? 1 : -1)
		
	} catch (error) {
		console.error('点赞操作失败:', error)
		uni.showToast({ title: '操作失败', icon: 'none' })
	}
}

// 跳转详情
const goToDetail = (noteId) => {
	uni.navigateTo({
		url: `/pages/note-detail/note-detail?id=${noteId}`
	})
}

onLoad((options) => {
	tagId.value = options.tagId
	// 解码标签名称
	tagName.value = decodeURIComponent(options.tagName || '')
	
	// 设置导航栏标题
	uni.setNavigationBarTitle({
		title: tagName.value
	})
	
	// 加载数据
	loadNotes(true)
})

// 监听下拉刷新
onMounted(() => {
	// 启用下拉刷新
	uni.startPullDownRefresh({
		success: () => {
			onRefresh()
		}
	})
})
</script>

<style scoped>
.tag-notes-page {
	min-height: 100vh;
	background: #f5f7f9;
	padding-bottom: calc(160rpx + env(safe-area-inset-bottom));
}

.tag-header {
	background: #fff;
	padding: 40rpx;
	text-align: center;
	border-bottom: 1px solid #eee;
}

.tag-name {
	font-size: 40rpx;
	font-weight: bold;
	color: #333;
}

.note-count {
	font-size: 24rpx;
	color: #999;
	margin-left: 20rpx;
}

.notes-list {
	padding: 20rpx;
}

/* 加载中样式 */
.loading-container {
	display: flex;
	flex-direction: column;
	align-items: center;
	justify-content: center;
	padding: 100rpx 0;
	gap: 30rpx;
}

.loading-spinner {
	width: 60rpx;
	height: 60rpx;
	border: 4rpx solid #f3f3f3;
	border-top: 4rpx solid #3498db;
	border-radius: 50%;
	animation: spin 1s linear infinite;
}

.loading-spinner.small {
	width: 40rpx;
	height: 40rpx;
	border-width: 3rpx;
}

@keyframes spin {
	0% { transform: rotate(0deg); }
	100% { transform: rotate(360deg); }
}

.loading-text {
	font-size: 28rpx;
	color: #999;
}

/* 空状态样式 */
.empty-container {
	display: flex;
	flex-direction: column;
	align-items: center;
	justify-content: center;
	padding: 100rpx 0;
	gap: 30rpx;
}

.empty-image {
	width: 200rpx;
	height: 200rpx;
}

.empty-text {
	color: #999;
	font-size: 28rpx;
}

/* 加载更多样式 */
.loadmore-container {
	padding: 30rpx 0;
	display: flex;
	justify-content: center;
}

.loadmore-loading,
.loadmore-nomore,
.loadmore-more {
	display: flex;
	align-items: center;
	justify-content: center;
	gap: 20rpx;
	padding: 20rpx;
}

.loadmore-text {
	font-size: 28rpx;
	color: #999;
}

.loadmore-more {
	cursor: pointer;
}

.loadmore-more:active {
	opacity: 0.7;
}
</style>