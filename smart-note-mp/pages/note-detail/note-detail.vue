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
	</view>
</template>

<script setup>
import { ref } from 'vue'
import { onLoad } from '@dcloudio/uni-app'
import MarkdownIt from 'markdown-it'
import { noteApi } from '@/api/index.js'

const md = new MarkdownIt({
	html: false,
	linkify: true,
	breaks: true
})

const loading = ref(false)
const noteData = ref(null)
const renderedContent = ref('')

const formatTime = (time) => {
	if (!time) return ''
	return String(time).replace('T', ' ')
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
	} catch (error) {
		console.error('加载笔记详情失败:', error)
		noteData.value = null
		renderedContent.value = ''
		uni.showToast({ title: error?.message || '加载失败', icon: 'none' })
	} finally {
		loading.value = false
	}
}

onLoad((options) => {
	const noteId = options?.id
	if (!noteId) {
		uni.showToast({ title: '参数缺失', icon: 'none' })
		return
	}
	loadDetail(noteId)
})
</script>

<style scoped>
.note-detail-page {
	min-height: 100vh;
	background: #f5f7f9;
	padding: 24rpx;
}

.note-container {
	background: #fff;
	border-radius: 16rpx;
	padding: 32rpx;
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
