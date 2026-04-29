<template>
	<view class="message-page">
		<!-- 顶部操作栏 -->
		<view class="top-bar">
			<text class="top-title">消息</text>
			<text class="read-all-btn" @click="onReadAll">全部已读</text>
		</view>

		<u-empty v-if="messageList.length === 0 && !loading" mode="list" text="暂无消息" marginTop="120" iconSize="120"></u-empty>

		<view v-else class="message-list">
			<view
				class="message-item"
				v-for="(item, i) in messageList"
				:key="item.id"
				@click="goToDetail(item)"
			>
				<!-- 未读标识 -->
				<view v-if="!item.isRead" class="unread-dot"></view>

				<image class="sender-avatar" :src="resolveAvatar(item.senderAvatar)" mode="aspectFill"></image>

				<view class="message-main">
					<view class="message-top">
						<text class="sender-name">{{ item.senderName || '匿名' }}</text>
						<text class="message-time">{{ formatTime(item.createTime) }}</text>
					</view>
					<view class="message-content">
						<text class="action-text">{{ item.type === 2 ? '回复了你的笔记' : '评论了你的笔记' }}</text>
						<text class="note-title-text">「{{ item.noteTitle || '已删除的笔记' }}」</text>
					</view>
					<text v-if="item.content" class="message-summary">{{ item.content }}</text>
				</view>

				<!-- 删除按钮 -->
				<view class="delete-btn" @click.stop="onDelete(item.id, i)">
					<u-icon name="trash" size="16" color="#c0c4cc"></u-icon>
				</view>
			</view>

			<view class="load-more-area">
				<text v-if="loading" class="load-more-text">加载中...</text>
				<text v-else-if="!hasMore" class="load-more-text">没有更多消息了</text>
				<text v-else class="load-more-text load-more-action" @click="loadMore">加载更多</text>
			</view>
		</view>
	</view>
</template>

<script setup>
import { ref } from 'vue'
import { onShow, onReachBottom } from '@dcloudio/uni-app'
import { messageApi } from '@/api/modules/message.js'
import { config } from '@/api/config.js'

const messageList = ref([])
const page = ref(1)
const hasMore = ref(true)
const loading = ref(false)

const resolveAvatar = (avatar) => {
	if (!avatar) return '/static/default-avatar.png'
	if (avatar.startsWith('http://') || avatar.startsWith('https://') || avatar.startsWith('data:')) return avatar
	if (avatar.startsWith('/static/')) return avatar
	if (avatar.startsWith('/api/')) return config.baseURL + avatar
	return config.baseURL + '/api/wx/user/files' + avatar
}

const formatTime = (time) => {
	if (!time) return ''
	return String(time).replace('T', ' ').substring(0, 16)
}

/** 加载消息列表 */
const loadMessages = async (reset = true) => {
	if (loading.value) return
	if (!reset && !hasMore.value) return

	loading.value = true
	if (reset) {
		page.value = 1
		hasMore.value = true
	}

	try {
		const res = await messageApi.getList(page.value, 20)
		const records = res?.records || []

		if (reset) {
			messageList.value = records
		} else {
			messageList.value.push(...records)
		}
		hasMore.value = records.length >= 20

		// 更新全局未读数缓存
		const unreadRes = await messageApi.getUnreadCount()
		const count = unreadRes?.count || 0
		uni.setStorageSync('unreadCount', count)
		uni.$emit('refreshUnread')
	} catch (e) {
		console.error('加载消息失败:', e)
	} finally {
		loading.value = false
	}
}

const loadMore = () => {
	if (!hasMore.value || loading.value) return
	page.value++
	loadMessages(false)
}

/** 全部标记已读 */
const onReadAll = async () => {
	try {
		await messageApi.readAll()
		messageList.value.forEach(m => { m.isRead = true })
		uni.setStorageSync('unreadCount', 0)
		uni.$emit('refreshUnread')
		uni.showToast({ title: '已全部标记为已读', icon: 'success' })
	} catch (e) {
		console.error('标记已读失败:', e)
	}
}

/** 删除单条消息 */
const onDelete = async (id, index) => {
	uni.showModal({
		title: '确认删除',
		content: '确定删除这条消息？',
		confirmColor: '#fa3534',
		success: async (res) => {
			if (res.confirm) {
				try {
					await messageApi.remove(id)
					messageList.value.splice(index, 1)
					uni.showToast({ title: '已删除', icon: 'success' })
				} catch (e) {
					console.error('删除消息失败:', e)
					uni.showToast({ title: '删除失败', icon: 'none' })
				}
			}
		}
	})
}

/** 点击消息跳转笔记详情 */
const goToDetail = (item) => {
	if (item.noteId) {
		uni.navigateTo({ url: `/pages/note-detail/note-detail?id=${item.noteId}` })
	} else {
		uni.showToast({ title: '笔记已删除', icon: 'none' })
	}
}

onReachBottom(() => {
	loadMore()
})

onShow(() => {
	loadMessages(true)
})
</script>

<style scoped>
.message-page {
	min-height: 100vh;
	background: #f5f7f9;
	padding-bottom: 40rpx;
}

.top-bar {
	display: flex;
	align-items: center;
	justify-content: space-between;
	padding: 24rpx 32rpx;
	background: #fff;
	border-bottom: 1rpx solid #f0f0f0;
}

.top-title {
	font-size: 32rpx;
	font-weight: 600;
	color: #303133;
}

.read-all-btn {
	font-size: 26rpx;
	color: #1890ff;
}

.message-list {
	padding: 0 24rpx;
}

.message-item {
	display: flex;
	align-items: flex-start;
	gap: 16rpx;
	background: #fff;
	border-radius: 16rpx;
	padding: 24rpx;
	margin-top: 16rpx;
	position: relative;
}

.unread-dot {
	position: absolute;
	left: 12rpx;
	top: 28rpx;
	width: 14rpx;
	height: 14rpx;
	border-radius: 50%;
	background: #1890ff;
}

.sender-avatar {
	width: 72rpx;
	height: 72rpx;
	border-radius: 50%;
	flex-shrink: 0;
	background: #f5f7f9;
}

.message-main {
	flex: 1;
	min-width: 0;
}

.message-top {
	display: flex;
	align-items: center;
	justify-content: space-between;
}

.sender-name {
	font-size: 28rpx;
	color: #303133;
	font-weight: 500;
}

.message-time {
	font-size: 22rpx;
	color: #c0c4cc;
}

.message-content {
	margin-top: 8rpx;
}

.action-text {
	font-size: 26rpx;
	color: #606266;
}

.note-title-text {
	font-size: 26rpx;
	color: #1890ff;
}

.message-summary {
	display: block;
	margin-top: 8rpx;
	font-size: 24rpx;
	color: #909399;
	overflow: hidden;
	text-overflow: ellipsis;
	white-space: nowrap;
}

.delete-btn {
	flex-shrink: 0;
	padding: 8rpx;
}

.load-more-area {
	padding: 30rpx 0;
	text-align: center;
}

.load-more-text {
	font-size: 24rpx;
	color: #c0c4cc;
}

.load-more-action {
	color: #409eff;
}
</style>
