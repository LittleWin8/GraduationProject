<template>
	<view class="custom-tab-bar">
		<view
			v-for="(item, index) in list"
			:key="index"
			class="tab-bar-item"
			:class="{ active: currentPage === item.pagePath }"
			@click="switchTab(item, index)"
		>
			<view class="icon-wrapper">
				<text class="iconfont" :class="item.icon"></text>
				<view v-if="item.showBadge && unreadCount > 0" class="badge">
					<text class="badge-text">{{ unreadCount > 99 ? '99+' : unreadCount }}</text>
				</view>
			</view>
			<text class="tab-bar-text">{{ item.text }}</text>
		</view>
	</view>
</template>

<script>
import { messageApi } from '@/api/modules/message.js'

export default {
	data() {
		return {
			currentPage: '',
			unreadCount: 0,
			pollTimer: null,
			list: [
				{
					pagePath: '/pages/community/community',
					text: '社区',
					icon: 'icon-shequ',
					isTab: true,
					showBadge: false
				},
				{
					pagePath: '/pages/subNote/create/create',
					text: '创作',
					icon: 'icon-chuangzuo',
					isTab: false,
					showBadge: false
				},
				{
					pagePath: '/pages/profile/profile',
					text: '我的',
					icon: 'icon-wode',
					isTab: true,
					showBadge: true
				}
			]
		}
	},
	methods: {
		switchTab(item, index) {
			if (this.currentPage === item.pagePath) return

			if (item.isTab) {
				uni.switchTab({ url: item.pagePath })
			} else {
				uni.navigateTo({ url: item.pagePath })
			}
		},
		updateCurrentPage() {
			const pages = getCurrentPages()
			if (pages.length > 0) {
				const currentPage = pages[pages.length - 1]
				let route = '/' + currentPage.route
				this.currentPage = route
			}
		},
		/** 获取未读消息数 */
		async fetchUnreadCount() {
			try {
				const res = await messageApi.getUnreadCount()
				const newCount = res?.totalCount || 0
				const oldCount = this.unreadCount
				this.unreadCount = newCount
				uni.setStorageSync('unreadCount', newCount)
				if (newCount > 0 && newCount > oldCount) {
					const diff = newCount - oldCount
					uni.showToast({
						title: `你有 ${diff} 条新消息`,
						icon: 'none',
						duration: 2000
					})
				}
			} catch (e) {
				console.warn('获取未读消息数失败:', e)
			}
		},
		/** 从缓存同步未读数 */
		syncFromCache() {
			try {
				const cached = uni.getStorageSync('unreadCount')
				if (typeof cached === 'number') {
					this.unreadCount = cached
				}
			} catch (e) {}
		},
		/** 启动轮询（每30秒刷新未读数） */
		startPolling() {
			this.stopPolling()
			this.pollTimer = setInterval(() => {
				this.fetchUnreadCount()
			}, 30000)
		},
		/** 停止轮询 */
		stopPolling() {
			if (this.pollTimer) {
				clearInterval(this.pollTimer)
				this.pollTimer = null
			}
		}
	},
	mounted() {
		this.updateCurrentPage()
		this.syncFromCache()
		this.fetchUnreadCount()
		this.startPolling()
		uni.$on('refreshUnread', () => {
			this.fetchUnreadCount()
		})
	},
	activated() {
		this.updateCurrentPage()
		this.syncFromCache()
		this.fetchUnreadCount()
	},
	beforeUnmount() {
		this.stopPolling()
		uni.$off('refreshUnread')
	}
}
</script>

<style scoped>
.custom-tab-bar {
	position: fixed;
	bottom: 0;
	left: 0;
	right: 0;
	display: flex;
	height: 100rpx;
	background-color: #FFFFFF;
	border-top: 1px solid #e5e5e5;
	padding-bottom: env(safe-area-inset-bottom);
	z-index: 999;
}

.tab-bar-item {
	flex: 1;
	display: flex;
	flex-direction: column;
	align-items: center;
	justify-content: center;
	gap: 8rpx;
}

.icon-wrapper {
	position: relative;
	display: flex;
	align-items: center;
	justify-content: center;
}

.iconfont {
	font-size: 48rpx;
	color: #909399;
}

.tab-bar-text {
	font-size: 24rpx;
	color: #909399;
}

.active .iconfont {
	color: #1890ff;
}

.active .tab-bar-text {
	color: #1890ff;
}

.badge {
	position: absolute;
	top: -10rpx;
	right: -20rpx;
	min-width: 32rpx;
	height: 32rpx;
	background: #fa3534;
	border-radius: 16rpx;
	display: flex;
	align-items: center;
	justify-content: center;
	padding: 0 8rpx;
}

.badge-text {
	font-size: 20rpx;
	color: #fff;
	line-height: 1;
}
</style>
