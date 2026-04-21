<template>
	<view class="custom-tab-bar">
		<view 
			v-for="(item, index) in list" 
			:key="index"
			class="tab-bar-item"
			:class="{ active: currentPage === item.pagePath }"
			@click="switchTab(item, index)"
		>
			<!-- 只用一套图标，选中时只变颜色 -->
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
					icon: 'icon-shequ'
				},
				{ 
					pagePath: '/pages/create/create', 
					text: '创作',
					icon: 'icon-chuangzuo'
				},
				{ 
					pagePath: '/pages/profile/profile', 
					text: '我的',
					icon: 'icon-wode'
				}
			]
		}
	},
	methods: {
		switchTab(item, index) {
			if (this.currentPage === item.pagePath) return
			
			// 跳转页面
			uni.switchTab({
				url: item.pagePath
			})
		},
		// 更新当前选中的页面
		updateCurrentPage() {
			const pages = getCurrentPages()
			if (pages.length > 0) {
				const currentPage = pages[pages.length - 1]
				// 获取页面路径，加上开头的斜杠
				let route = '/' + currentPage.route
				this.currentPage = route
			}
		}
	},
	mounted() {
		// 初始化时更新选中状态
		this.updateCurrentPage()
	},
	// 页面显示时重新获取当前页面
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
	color: #909399;  /* 未选中：灰色 */
}

.tab-bar-text {
	font-size: 24rpx;
	color: #909399;  /* 未选中：灰色 */
}

/* 选中时：图标和文字都变成蓝色 */
.active .iconfont {
	color: #1890ff;
}

.active .tab-bar-text {
	color: #1890ff;
}
</style>