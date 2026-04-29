<template>
	<view class="note-detail-page">
		<view v-if="loading" class="skeleton-wrapper">
			<view class="skeleton-title"></view>
			<view class="skeleton-meta"></view>
			<view class="skeleton-line"></view>
			<view class="skeleton-line"></view>
			<view class="skeleton-line short"></view>
			<view class="skeleton-line"></view>
			<view class="skeleton-line short"></view>
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

			<!-- 评论区 -->
			<view class="comment-section">
				<view class="comment-header">
					<text class="comment-title">评论 ({{ noteData.comments || 0 }})</text>
				</view>

				<u-empty v-if="commentList.length === 0 && !commentLoading" mode="list" text="暂无评论，快来抢沙发" marginTop="40" iconSize="120"></u-empty>

				<view v-else class="comment-list">
					<view class="comment-item" v-for="(item, i) in commentList" :key="item.commentId">
						<image class="comment-avatar" :src="resolveAvatar(item.avatar)" mode="aspectFill"></image>
						<view class="comment-main">
							<view class="comment-top">
								<text class="comment-author">{{ item.author || '匿名' }}</text>
								<text v-if="item.isOwner" class="comment-delete" @click="onDeleteComment(item.commentId, i)">删除</text>
							</view>
							<text class="comment-time">{{ formatTime(item.createTime) }}</text>
							<view class="comment-content-wrap">
								<text v-if="item.parentAuthor" class="comment-reply-prefix">回复 @{{ item.parentAuthor }}：</text>
								<text class="comment-content">{{ item.content }}</text>
							</view>
							<text class="comment-reply-btn" @click="openReply(item)">回复</text>
						</view>
					</view>

					<view class="comment-load-more" v-if="commentList.length > 0">
						<text v-if="commentLoading" class="load-more-text">加载中...</text>
						<text v-else-if="!commentHasMore" class="load-more-text">没有更多评论了</text>
						<text v-else class="load-more-text load-more-action" @click="loadMoreComments">加载更多</text>
					</view>
				</view>
			</view>
		</view>

		<!-- 底部互动栏 -->
		<view v-if="noteData" class="bottom-bar">
			<view class="bar-item" @click="onLike">
				<u-icon :name="isLiked ? 'heart-fill' : 'heart'" :color="isLiked ? '#fa3534' : '#909399'" size="24"></u-icon>
				<text class="bar-text" :style="{color: isLiked ? '#fa3534' : '#909399'}">{{ likeCount }}</text>
			</view>
			<view class="bar-item" @click="onCollect">
				<u-icon :name="isCollected ? 'star-fill' : 'star'" :color="isCollected ? '#ff9900' : '#909399'" size="24"></u-icon>
				<text class="bar-text" :style="{color: isCollected ? '#ff9900' : '#909399'}">{{ isCollected ? '已收藏' : '收藏' }}</text>
			</view>
			<view class="bar-item" @click="openCommentInput">
				<u-icon name="chat" color="#909399" size="24"></u-icon>
				<text class="bar-text">{{ noteData.comments || 0 }}</text>
			</view>
		</view>

		<!-- 评论输入弹窗 -->
		<u-popup :show="showCommentPopup" mode="bottom" round="16" @close="closeCommentPopup">
			<view class="comment-popup">
				<view class="popup-header">
					<text class="popup-title">{{ replyTarget ? '回复 @' + replyTarget.author : '发表评论' }}</text>
					<view class="popup-send" :class="{ disabled: !commentInput.trim() || sending }" @click="onSendComment">
						{{ sending ? '发送中' : '发送' }}
					</view>
				</view>
				<textarea
					class="popup-textarea"
					v-model="commentInput"
					:placeholder="replyTarget ? '回复 @' + replyTarget.author + '...' : '写下你的评论...'"
					maxlength="500"
					:auto-height="true"
					:adjust-position="true"
					:focus="showCommentPopup"
				></textarea>
				<view class="popup-count">
					<text>{{ commentInput.length }}/500</text>
				</view>
			</view>
		</u-popup>
	</view>
</template>

<script setup>
import { ref } from 'vue'
import { onLoad } from '@dcloudio/uni-app'
import MarkdownIt from 'markdown-it'
import { noteApi } from '@/api/index.js'
import { interactionApi } from '@/api/modules/interaction.js'
import { commentApi } from '@/api/modules/comment.js'
import { logApi } from '@/api/modules/log.js'
import { config } from '@/api/config.js'

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

const commentList = ref([])
const commentPage = ref(1)
const commentHasMore = ref(true)
const commentLoading = ref(false)

const showCommentPopup = ref(false)
const commentInput = ref('')
const replyTarget = ref(null)
const sending = ref(false)

/** 拼接头像URL */
const resolveAvatar = (avatar) => {
	if (!avatar) return '/static/default-avatar.png'
	if (avatar.startsWith('http://') || avatar.startsWith('https://') || avatar.startsWith('data:')) return avatar
	if (avatar.startsWith('/static/')) return avatar
	if (avatar.startsWith('/api/')) return config.baseURL + avatar
	return config.baseURL + '/api/wx/user/files' + avatar
}

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

/** 加载评论列表 */
const loadComments = async (reset = true) => {
	if (!noteId.value) return
	if (commentLoading.value) return

	if (reset) {
		commentPage.value = 1
		commentHasMore.value = true
	}

	commentLoading.value = true
	try {
		const res = await commentApi.getComments(Number(noteId.value), commentPage.value, 10)
		const records = res?.records || []

		const parentIdSet = new Set()
		records.forEach(r => { if (r.parentId) parentIdSet.add(r.parentId) })

		let parentAuthorMap = {}
		if (parentIdSet.size > 0) {
			commentList.value.forEach(c => {
				if (parentIdSet.has(c.commentId)) {
					parentAuthorMap[c.commentId] = c.author || '匿名'
				}
			})
			if (Object.keys(parentAuthorMap).length < parentIdSet.size) {
				const allLoaded = [...commentList.value, ...records]
				allLoaded.forEach(c => {
					if (parentIdSet.has(c.commentId)) {
						parentAuthorMap[c.commentId] = c.author || '匿名'
					}
				})
			}
		}

		const mapped = records.map(r => ({
			...r,
			parentAuthor: r.parentId ? (parentAuthorMap[r.parentId] || '') : ''
		}))

		if (reset) {
			commentList.value = mapped
		} else {
			commentList.value.push(...mapped)
		}
		commentHasMore.value = records.length >= 10
	} catch (e) {
		console.error('加载评论失败:', e)
	} finally {
		commentLoading.value = false
	}
}

const loadMoreComments = () => {
	if (!commentHasMore.value || commentLoading.value) return
	commentPage.value++
	loadComments(false)
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
		loadComments(true)
		logApi.report('view', String(id)).catch(() => {})
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
					uni.$emit('noteUpdated')
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

/** 打开评论输入弹窗（新评论） */
const openCommentInput = () => {
	replyTarget.value = null
	commentInput.value = ''
	showCommentPopup.value = true
}

/** 打开评论输入弹窗（回复某条评论） */
const openReply = (item) => {
	replyTarget.value = item
	commentInput.value = ''
	showCommentPopup.value = true
}

const closeCommentPopup = () => {
	showCommentPopup.value = false
	replyTarget.value = null
	commentInput.value = ''
}

/** 发送评论 */
const onSendComment = async () => {
	const content = commentInput.value.trim()
	if (!content || sending.value) return

	sending.value = true
	try {
		const params = {
			noteId: Number(noteId.value),
			content
		}
		if (replyTarget.value) {
			params.parentId = replyTarget.value.commentId
		}
		await commentApi.addComment(params.noteId, params.content, params.parentId || null)
		uni.showToast({ title: '评论成功', icon: 'success' })
		closeCommentPopup()
		loadComments(true)
		if (noteData.value) {
			noteData.value.comments = (noteData.value.comments || 0) + 1
		}
		setTimeout(() => {
			uni.pageScrollTo({ selector: '.comment-section', duration: 300 })
		}, 300)
	} catch (e) {
		console.error('发表评论失败:', e)
		uni.showToast({ title: '评论失败', icon: 'none' })
	} finally {
		sending.value = false
	}
}

/** 删除评论 */
const onDeleteComment = (commentId, index) => {
	uni.showModal({
		title: '确认删除',
		content: '确定删除这条评论？',
		confirmColor: '#fa3534',
		success: async (res) => {
			if (res.confirm) {
				try {
					await commentApi.deleteComment(commentId)
					commentList.value.splice(index, 1)
					if (noteData.value && noteData.value.comments > 0) {
						noteData.value.comments--
					}
					uni.showToast({ title: '已删除', icon: 'success' })
				} catch (e) {
					console.error('删除评论失败:', e)
					uni.showToast({ title: '删除失败', icon: 'none' })
				}
			}
		}
	})
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
	padding-bottom: 24rpx;
	border-bottom: 1px solid #f0f0f0;
}

/* 评论区 */
.comment-section {
	margin-top: 24rpx;
}

.comment-header {
	margin-bottom: 20rpx;
}

.comment-title {
	font-size: 32rpx;
	font-weight: 600;
	color: #303133;
}

.comment-list {
	display: flex;
	flex-direction: column;
	gap: 20rpx;
}

.comment-item {
	display: flex;
	gap: 16rpx;
}

.comment-avatar {
	width: 60rpx;
	height: 60rpx;
	border-radius: 50%;
	flex-shrink: 0;
	background: #f5f7f9;
}

.comment-main {
	flex: 1;
	min-width: 0;
}

.comment-top {
	display: flex;
	align-items: center;
	justify-content: space-between;
}

.comment-author {
	font-size: 26rpx;
	color: #303133;
	font-weight: 500;
}

.comment-delete {
	font-size: 22rpx;
	color: #fa3534;
}

.comment-time {
	font-size: 22rpx;
	color: #c0c4cc;
	margin-top: 4rpx;
}

.comment-content-wrap {
	margin-top: 8rpx;
}

.comment-reply-prefix {
	font-size: 28rpx;
	color: #409eff;
}

.comment-content {
	font-size: 28rpx;
	color: #606266;
	line-height: 1.6;
	word-break: break-word;
}

.comment-reply-btn {
	font-size: 22rpx;
	color: #909399;
	margin-top: 8rpx;
}

.comment-load-more {
	padding: 20rpx 0;
	text-align: center;
}

.load-more-text {
	font-size: 24rpx;
	color: #c0c4cc;
}

.load-more-action {
	color: #409eff;
}

/* 底部互动栏 */
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

/* 评论输入弹窗 */
.comment-popup {
	padding: 24rpx 32rpx;
	padding-bottom: calc(24rpx + env(safe-area-inset-bottom));
}

.popup-header {
	display: flex;
	align-items: center;
	justify-content: space-between;
	margin-bottom: 20rpx;
}

.popup-title {
	font-size: 30rpx;
	font-weight: 600;
	color: #303133;
}

.popup-send {
	font-size: 28rpx;
	color: #fff;
	background: #409eff;
	padding: 10rpx 32rpx;
	border-radius: 8rpx;
}

.popup-send.disabled {
	background: #c0c4cc;
}

.popup-textarea {
	width: 100%;
	min-height: 160rpx;
	font-size: 28rpx;
	color: #303133;
	line-height: 1.6;
	padding: 16rpx;
	background: #f5f7f9;
	border-radius: 12rpx;
	box-sizing: border-box;
}

.popup-count {
	text-align: right;
	font-size: 22rpx;
	color: #c0c4cc;
	margin-top: 8rpx;
}

.empty-box {
	min-height: 60vh;
	display: flex;
	flex-direction: column;
	align-items: center;
	justify-content: center;
	gap: 20rpx;
}

.empty-text {
	font-size: 28rpx;
	color: #909399;
}

.skeleton-wrapper {
	padding: 40rpx;
	background: #fff;
	border-radius: 16rpx;
}

.skeleton-title {
	height: 44rpx;
	width: 70%;
	border-radius: 8rpx;
	margin-bottom: 24rpx;
	background: linear-gradient(90deg, #f0f0f0 25%, #e8e8e8 50%, #f0f0f0 75%);
	background-size: 200% 100%;
	animation: skeleton-pulse 1.5s infinite;
}

.skeleton-meta {
	height: 24rpx;
	width: 40%;
	border-radius: 6rpx;
	margin-bottom: 40rpx;
	background: linear-gradient(90deg, #f0f0f0 25%, #e8e8e8 50%, #f0f0f0 75%);
	background-size: 200% 100%;
	animation: skeleton-pulse 1.5s infinite;
}

.skeleton-line {
	height: 28rpx;
	width: 100%;
	border-radius: 6rpx;
	margin-bottom: 20rpx;
	background: linear-gradient(90deg, #f0f0f0 25%, #e8e8e8 50%, #f0f0f0 75%);
	background-size: 200% 100%;
	animation: skeleton-pulse 1.5s infinite;
}

.skeleton-line.short {
	width: 60%;
}

@keyframes skeleton-pulse {
	0% { background-position: 200% 0; }
	100% { background-position: -200% 0; }
}
</style>
