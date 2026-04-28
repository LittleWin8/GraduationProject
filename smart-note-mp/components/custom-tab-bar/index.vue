<template>
	<view class="custom-tab-bar">
		<view 
			v-for="(item, index) in list" 
			:key="index"
			class="tab-bar-item"
			:class="{ active: currentPage === item.pagePath }"
			@click="switchTab(item, index)"
		>
			<text class="iconfont" :class="item.icon"></text>
			<text class="tab-bar-text">{{ item.text }}</text>
		</view>
	</view>
</template>

<script>
export default {
	data() {
		return {
			currentPage: '',
			list: [
				{ 
					pagePath: '/pages/community/community', 
					text: '社区',
					icon: 'icon-shequ',
					isTab: true
				},
				{ 
					pagePath: '/pages/create/create', 
					text: '创作',
					icon: 'icon-chuangzuo',
					isTab: false
				},
				{ 
					pagePath: '/pages/profile/profile', 
					text: '我的',
					icon: 'icon-wode',
					isTab: true
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
		}
	},
	mounted() {
		this.updateCurrentPage()
	},
	activated() {
		this.updateCurrentPage()
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
</style>
