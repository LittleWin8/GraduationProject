<template>
	<view class="note-detail-page">
		<view v-if="loading" class="loading-box">
			<view class="loading-spinner"></view>
			<text class="loading-text">加载中...</text>
		</view>

		<view v-else-if="!noteData" class="empty-box">
			<text class="empty-text">笔记不存在或无权限访问</text>
		</view>

		<view v-else class="note-container">
			<view v-if="isOwner" class="owner-actions">
				<view class="action-btn" @click="goToEdit">
					<u-icon name="edit-pen" size="16" color="#1890ff"></u-icon>
					<text>编辑</text>
				</view>
				<view class="action-btn delete-action" @click="confirmDelete">
					<u-icon name="trash" size="16" color="#fa3534"></u-icon>
					<text>删除</text>
				</view>
			</view>

			<view class="note-head">
				<text class="note-title">{{ noteData.title }}</text>
				<view class="note-meta">
					<text class="meta-item">作者：{{ noteData.author || '未知' }}</text>
					<text class="meta-item">{{ formatTime(noteData.updateTime) }}</text>
				</view>
			</view>

			<view class="note-body">
				<rich-text :nodes="renderedContent"></rich-text>
			</view>
		</view>

		<view v-if="noteData" class="bottom-bar">
			<view class="bar-item" @click="onLike">
				<u-icon :name="isLiked ? 'heart-fill' : 'heart'" :color="isLiked ? '#fa3534' : '#909399'" size="24"></u-icon>
				<text class="bar-text" :style="{color: isLiked ? '#fa3534' : '#909399'}">{{ likeCount }}</text>
			</view>
			<view class="bar-item" @click="onCollect">
				<u-icon :name="isCollected ? 'star-fill' : 'star'" :color="isCollected ? '#ff9900' : '#909399'" size="24"></u-icon>
				<text class="bar-text" :style="{color: isCollected ? '#ff9900' : '#909399'}">{{ isCollected ? '已收藏' : '收藏' }}</text>
			</view>
			<view class="bar-item" @click="onComment">
				<u-icon name="chat" color="#909399" size="24"></u-icon>
				<text class="bar-text">{{ noteData.comments || 0 }}</text>
			</view>
		</view>
	</view>
</template>

<script setup>
import { ref } from 'vue'
import { onLoad } from '@dcloudio/uni-app'
import MarkdownIt from 'markdown-it'
import { noteApi } from '@/api/index.js'
import { interactionApi } from '@/api/modules/interaction.js'

const md = new MarkdownIt({
	html: false,
	linkify: true,
	breaks: true
})

const loading = ref(false)
const noteData = ref(null)
const renderedContent = ref('')
const noteId = ref(null)
const isOwner = ref(false)

const isLiked = ref(false)
const likeCount = ref(0)
const isCollected = ref(false)

const liking = ref(false)
const collecting = ref(false)

const formatTime = (time) => {
	if (!time) return ''
	return String(time).replace('T', ' ')
}

const checkOwnership = () => {
	try {
		const userInfo = uni.getStorageSync('userInfo')
		if (userInfo && userInfo.userId && noteData.value) {
			isOwner.value = noteData.value.userId === userInfo.userId
		}
	} catch (e) {
		console.error('判断作者身份失败:', e)
	}
}

const loadInteractionStatus = async () => {
	if (!noteId.value) return
	try {
		const status = await interactionApi.getStatus(Number(noteId.value))
		if (status) {
			isLiked.value = !!status.isLiked
			isCollected.value = !!status.isCollected
			if (status.likeCount !== undefined && status.likeCount !== null) {
				likeCount.value = status.likeCount
			}
		}
	} catch (e) {
		console.warn('获取互动状态失败:', e)
	}
}

const loadDetail = async (id) => {
	loading.value = true
	try {
		const res = await noteApi.getNoteDetail(id)
		noteData.value = res
		renderedContent.value = md.render(res?.content || '')
		uni.setNavigationBarTitle({
			title: res?.title || '笔记详情'
		})

		isLiked.value = !!res?.isLiked
		likeCount.value = res?.likes || 0

		checkOwnership()
		loadInteractionStatus()
	} catch (error) {
		console.error('加载笔记详情失败:', error)
		noteData.value = null
		renderedContent.value = ''
		uni.showToast({ title: error?.message || '加载失败', icon: 'none' })
	} finally {
		loading.value = false
	}
}

const goToEdit = () => {
	uni.navigateTo({ url: `/pages/create/create?id=${noteId.value}` })
}

const confirmDelete = () => {
	uni.showModal({
		title: '确认删除',
		content: '确定将此笔记移入回收站？',
		confirmColor: '#fa3534',
		success: async (res) => {
			if (res.confirm) {
				try {
					await noteApi.deleteNote(noteId.value, false)
					uni.showToast({ title: '已移入回收站', icon: 'success' })
					setTimeout(() => {
						uni.navigateBack()
					}, 1500)
				} catch (error) {
					console.error('删除笔记失败:', error)
				}
			}
		}
	})
}

const onLike = async () => {
	if (liking.value) return
	liking.value = true

	const prevLiked = isLiked.value
	const prevCount = likeCount.value

	isLiked.value = !isLiked.value
	likeCount.value += isLiked.value ? 1 : -1

	try {
		const result = await interactionApi.interact(Number(noteId.value), 'like')
		isLiked.value = !!result.isLiked
		likeCount.value = result.likeCount ?? likeCount.value
		uni.showToast({ title: isLiked.value ? '已点赞' : '已取消点赞', icon: 'none' })
	} catch (e) {
		console.error('点赞操作失败:', e)
		isLiked.value = prevLiked
		likeCount.value = prevCount
		uni.showToast({ title: '操作失败', icon: 'none' })
	} finally {
		setTimeout(() => { liking.value = false }, 500)
	}
}

const onCollect = async () => {
	if (collecting.value) return
	collecting.value = true

	const prevCollected = isCollected.value

	isCollected.value = !isCollected.value

	try {
		const result = await interactionApi.interact(Number(noteId.value), 'collect')
		isCollected.value = !!result.isCollected
		uni.showToast({ title: isCollected.value ? '已收藏' : '已取消收藏', icon: 'none' })
	} catch (e) {
		console.error('收藏操作失败:', e)
		isCollected.value = prevCollected
		uni.showToast({ title: '操作失败', icon: 'none' })
	} finally {
		setTimeout(() => { collecting.value = false }, 500)
	}
}

const onComment = () => {
	uni.showToast({ title: '评论功能即将上线', icon: 'none' })
}

onLoad((options) => {
	const id = options?.id
	if (!id) {
		uni.showToast({ title: '参数缺失', icon: 'none' })
		return
	}
	noteId.value = id
	loadDetail(id)
})
</script>

<style scoped>
.note-detail-page {
	min-height: 100vh;
	background: #f5f7f9;
	padding: 24rpx;
	padding-bottom: 120rpx;
}

.note-container {
	background: #fff;
	border-radius: 16rpx;
	padding: 32rpx;
}

.owner-actions {
	display: flex;
	justify-content: flex-end;
	gap: 30rpx;
	margin-bottom: 20rpx;
	padding-bottom: 20rpx;
	border-bottom: 1px solid #f0f0f0;
}

.owner-actions .action-btn {
	display: flex;
	align-items: center;
	gap: 6rpx;
	font-size: 26rpx;
	padding: 10rpx 24rpx;
	border-radius: 8rpx;
	background: #f5f7f9;
	color: #606266;
}

.owner-actions .action-btn.delete-action {
	color: #fa3534;
	background: rgba(250, 53, 52, 0.06);
}

.note-head {
	margin-bottom: 28rpx;
}

.note-title {
	font-size: 40rpx;
	font-weight: 700;
	color: #303133;
	line-height: 1.4;
}

.note-meta {
	margin-top: 16rpx;
	display: flex;
	gap: 20rpx;
	flex-wrap: wrap;
}

.meta-item {
	font-size: 24rpx;
	color: #909399;
}

.note-body {
	font-size: 30rpx;
	color: #303133;
	line-height: 1.8;
	word-break: break-word;
}

.bottom-bar {
	position: fixed;
	bottom: 0;
	left: 0;
	right: 0;
	height: 100rpx;
	background: #fff;
	border-top: 1rpx solid #eee;
	display: flex;
	align-items: center;
	justify-content: space-around;
	z-index: 100;
	padding-bottom: env(safe-area-inset-bottom);
}

.bar-item {
	display: flex;
	flex-direction: column;
	align-items: center;
	justify-content: center;
	flex: 1;
	gap: 4rpx;
}

.bar-text {
	font-size: 22rpx;
	color: #909399;
	line-height: 1;
}

.loading-box,
.empty-box {
	min-height: 60vh;
	display: flex;
	flex-direction: column;
	align-items: center;
	justify-content: center;
	gap: 20rpx;
}

.loading-spinner {
	width: 56rpx;
	height: 56rpx;
	border: 4rpx solid #f3f3f3;
	border-top: 4rpx solid #3498db;
	border-radius: 50%;
	animation: spin 1s linear infinite;
}

.loading-text,
.empty-text {
	font-size: 28rpx;
	color: #909399;
}

@keyframes spin {
	0% { transform: rotate(0deg); }
	100% { transform: rotate(360deg); }
}
</style>
