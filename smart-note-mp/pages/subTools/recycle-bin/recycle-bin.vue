<template>
	<view class="recycle-bin-page">
		<!-- 加载中 -->
		<view v-if="loading && list.length === 0" class="loading-wrap">
			<u-loading-icon></u-loading-icon>
		</view>

		<!-- 空状态 -->
		<u-empty v-else-if="list.length === 0" mode="data" text="回收站是空的" marginTop="100"></u-empty>

		<!-- 列表 -->
		<scroll-view
			v-else
			class="list-scroll"
			scroll-y
			@scrolltolower="loadMore"
			:refresher-enabled="true"
			:refresher-triggered="refreshing"
			@refresherrefresh="onRefresh"
		>
			<u-swipe-action
				v-for="(item, i) in list"
				:key="i"
				:options="swipeOptions"
				@click="onSwipeClick($event, item)"
				bg-color="transparent"
			>
				<view class="recycle-item">
					<view class="recycle-item-content">
						<text class="recycle-item-title">{{ item.title }}</text>
						<text class="recycle-item-time">删除时间：{{ item.updateTime }}</text>
					</view>
					<view class="recycle-item-actions">
						<view class="action-btn restore-btn" @click.stop="restoreNote(item)">
							<u-icon name="reload" size="16" color="#1890ff"></u-icon>
							<text>恢复</text>
						</view>
						<view class="action-btn delete-btn" @click.stop="permanentDelete(item)">
							<u-icon name="trash" size="16" color="#fa3534"></u-icon>
							<text>永久删除</text>
						</view>
					</view>
				</view>
			</u-swipe-action>

			<view class="load-more">
				<text v-if="loadingMore">加载中...</text>
				<text v-else-if="!hasMore">没有更多了</text>
			</view>
		</scroll-view>
	</view>
</template>

<script setup>
import { ref } from 'vue'
import { onShow, onPullDownRefresh } from '@dcloudio/uni-app'
import { noteApi } from '@/api/modules/note.js'

const list = ref([])
const pageNum = ref(1)
const pageSize = 15
const hasMore = ref(true)
const loading = ref(false)
const loadingMore = ref(false)
const refreshing = ref(false)

/**
 * 左滑操作按钮
 */
const swipeOptions = [
	{ text: '恢复', style: { backgroundColor: '#1890ff' } },
	{ text: '永久删除', style: { backgroundColor: '#fa3534' } }
]

/**
 * 加载回收站笔记列表
 */
const loadData = async (isLoadMore = false) => {
	if (!isLoadMore) {
		loading.value = true
		pageNum.value = 1
		hasMore.value = true
	} else {
		if (!hasMore.value) return
		loadingMore.value = true
	}

	try {
		const res = await noteApi.getNotes('my', pageNum.value, pageSize, { status: 2 })
		if (res && res.records) {
			const newList = res.records.map(item => ({
				noteId: item.noteId,
				title: item.title,
				updateTime: item.updateTime ? String(item.updateTime).replace('T', ' ').split('.')[0] : ''
			}))
			if (isLoadMore) {
				list.value.push(...newList)
			} else {
				list.value = newList
			}
			hasMore.value = res.records.length >= pageSize
		} else {
			if (!isLoadMore) list.value = []
			hasMore.value = false
		}
	} catch (error) {
		console.error('加载回收站列表失败:', error)
	} finally {
		loading.value = false
		loadingMore.value = false
		refreshing.value = false
	}
}

const loadMore = () => {
	if (loadingMore.value || !hasMore.value) return
	pageNum.value++
	loadData(true)
}

const onRefresh = () => {
	refreshing.value = true
	loadData(false)
}

/**
 * 恢复笔记
 */
const restoreNote = (item) => {
	uni.showModal({
		title: '确认恢复',
		content: `确定恢复笔记"${item.title}"？`,
		success: async (res) => {
			if (res.confirm) {
				try {
					await noteApi.restoreNote(item.noteId)
					uni.showToast({ title: '已恢复', icon: 'success' })
					loadData(false)
				} catch (error) {
					console.error('恢复笔记失败:', error)
				}
			}
		}
	})
}

/**
 * 永久删除笔记
 */
const permanentDelete = (item) => {
	uni.showModal({
		title: '永久删除',
		content: `确定永久删除笔记"${item.title}"？此操作不可恢复！`,
		confirmColor: '#fa3534',
		success: async (res) => {
			if (res.confirm) {
				try {
					await noteApi.deleteNote(item.noteId, true)
					uni.showToast({ title: '已永久删除', icon: 'success' })
					loadData(false)
				} catch (error) {
					console.error('永久删除失败:', error)
				}
			}
		}
	})
}

/**
 * 左滑点击事件
 */
const onSwipeClick = (e, item) => {
	if (e.index === 0) {
		restoreNote(item)
	} else if (e.index === 1) {
		permanentDelete(item)
	}
}

onPullDownRefresh(async () => {
	await loadData(false)
	uni.stopPullDownRefresh()
})

onShow(() => {
	loadData(false)
})
</script>

<style lang="scss" scoped>
.recycle-bin-page {
	min-height: 100vh;
	background: #f5f7f9;
}

.loading-wrap {
	padding: 100rpx;
	display: flex;
	justify-content: center;
}

.list-scroll {
	height: 100vh;
}

.recycle-item {
	background: #fff;
	padding: 24rpx 30rpx;
	border-bottom: 1px solid #f0f0f0;

	.recycle-item-content {
		.recycle-item-title {
			display: block;
			font-size: 30rpx;
			color: #303133;
			overflow: hidden;
			text-overflow: ellipsis;
			white-space: nowrap;
		}

		.recycle-item-time {
			display: block;
			font-size: 24rpx;
			color: #909399;
			margin-top: 8rpx;
		}
	}

	.recycle-item-actions {
		display: flex;
		gap: 30rpx;
		margin-top: 16rpx;

		.action-btn {
			display: flex;
			align-items: center;
			gap: 6rpx;
			font-size: 24rpx;
			padding: 8rpx 20rpx;
			border-radius: 8rpx;

			&.restore-btn {
				color: #1890ff;
				background: rgba(24, 144, 255, 0.08);
			}

			&.delete-btn {
				color: #fa3534;
				background: rgba(250, 53, 52, 0.08);
			}
		}
	}
}

.load-more {
	padding: 30rpx;
	text-align: center;
	font-size: 24rpx;
	color: #999;
}
</style>
