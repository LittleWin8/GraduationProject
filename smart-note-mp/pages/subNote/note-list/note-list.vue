<template>
	<view class="note-list-container">
		<!-- 搜索栏 -->
		<view class="search-bar">
			<u-search
				v-model="searchKeyword"
				placeholder="搜索标题或内容"
				@search="onSearch"
				@clear="onSearch"
				:show-action="false"
				shape="round"
				bg-color="#f5f5f5"
			></u-search>
		</view>

		<!-- 筛选栏 -->
		<view class="filter-bar">
			<view class="filter-item" @click="showCategoryPicker = true">
				<text :class="{ active: queryParams.categoryId }">
					{{ queryParams.categoryId ? selectedCategoryName : '全部分类' }}
				</text>
				<u-icon name="arrow-down" size="14" color="#999"></u-icon>
			</view>

			<view class="filter-item" @click="showSortPicker = true">
				<text>{{ sortOptions.find(s => s.value === queryParams.orderBy)?.label || '时间' }}</text>
				<u-icon name="arrow-down" size="14" color="#999"></u-icon>
			</view>
		</view>

		<!-- 列表内容 -->
		<scroll-view
			class="list-scroll"
			scroll-y
			@scrolltolower="loadMore"
			:refresher-enabled="true"
			:refresher-triggered="refreshing"
			@refresherrefresh="onRefresh"
		>
			<view v-if="loading && list.length === 0" class="loading-wrap">
				<u-loading-icon></u-loading-icon>
			</view>

			<u-empty v-else-if="list.length === 0" mode="list" text="暂无数据" marginTop="100"></u-empty>

			<view v-else class="list-wrapper">
				<u-swipe-action
					v-for="(item, i) in list"
					:key="i"
					:options="swipeOptions"
					@click="onSwipeClick($event, item, i)"
					bg-color="transparent"
				>
					<view class="note-item" @click="goToNoteDetail(item.noteId)">
						<view class="note-item-left">
							<u-icon
								:name="iconName"
								size="18"
								customStyle="margin-right:10rpx"
								:color="iconColor"
							></u-icon>
						</view>
						<view class="note-item-content">
							<text class="note-item-title">{{ item.title }}</text>
							<text class="note-item-time">{{ item.updateTime }}</text>
						</view>
						<!-- 收藏/赞过列表右侧取消图标 -->
						<view v-if="type === 'favorites'" class="action-icon" @click.stop="removeItem(item.noteId, i, 'collect')">
							<u-icon name="star-fill" size="20" color="#f5a623"></u-icon>
						</view>
						<view v-else-if="type === 'liked'" class="action-icon" @click.stop="removeItem(item.noteId, i, 'like')">
							<u-icon name="heart-fill" size="20" color="#fa3534"></u-icon>
						</view>
						<u-icon v-else name="arrow-right" size="14" color="#ccc"></u-icon>
					</view>
				</u-swipe-action>

				<view class="load-more">
					<text v-if="loadingMore">加载中...</text>
					<text v-else-if="!hasMore">没有更多了</text>
					<text v-else class="load-trigger" @click="loadMore">上拉加载更多</text>
				</view>
			</view>
		</scroll-view>

		<!-- 分类选择器 -->
		<u-picker
			:show="showCategoryPicker"
			:columns="categories"
			keyName="name"
			@confirm="onCategoryConfirm"
			@cancel="showCategoryPicker = false"
		></u-picker>

		<!-- 排序选择器 -->
		<u-picker
			:show="showSortPicker"
			:columns="[sortOptions]"
			keyName="label"
			@confirm="onSortConfirm"
			@cancel="showSortPicker = false"
		></u-picker>
	</view>
</template>

<script setup>
import { ref, computed } from 'vue'
import { onLoad } from '@dcloudio/uni-app'
import { noteApi, categoryApi } from '@/api'
import { interactionApi } from '@/api/modules/interaction.js'

const type = ref('notes')
const title = ref('')

const list = ref([])
const pageNum = ref(1)
const pageSize = 15
const hasMore = ref(true)
const loading = ref(false)
const loadingMore = ref(false)
const refreshing = ref(false)

const searchKeyword = ref('')
const queryParams = ref({
	keyword: '',
	categoryId: null,
	orderBy: 'updateTime',
	orderDirection: 'DESC'
})

const categories = ref([])
const showCategoryPicker = ref(false)
const selectedCategoryName = ref('')

const sortOptions = [
	{ label: '按时间降序', value: 'updateTime', direction: 'DESC' },
	{ label: '按时间升序', value: 'updateTime', direction: 'ASC' },
	{ label: '按浏览降序', value: 'viewCount', direction: 'DESC' },
	{ label: '按浏览升序', value: 'viewCount', direction: 'ASC' }
]
const showSortPicker = ref(false)

const removingIds = ref(new Set())

/** 列表项左侧图标 */
const iconName = computed(() => {
	if (type.value === 'favorites') return 'star'
	if (type.value === 'liked') return 'heart'
	return 'file-text'
})

const iconColor = computed(() => {
	if (type.value === 'favorites') return '#f5a623'
	if (type.value === 'liked') return '#fa3534'
	return '#999'
})

/** 左滑操作按钮（仅"我的笔记"显示删除） */
const swipeOptions = computed(() => {
	if (type.value === 'notes') {
		return [{ text: '删除', style: { backgroundColor: '#fa3534' } }]
	}
	return []
})

/** 左滑点击事件 */
const onSwipeClick = async (e, item, index) => {
	if (e.index === 0) {
		uni.showModal({
			title: '确认删除',
			content: '确定将此笔记移入回收站？',
			success: async (res) => {
				if (res.confirm) {
					try {
						await noteApi.deleteNote(item.noteId, false)
						uni.showToast({ title: '已移入回收站', icon: 'success' })
						loadData(false)
					} catch (error) {
						console.error('删除笔记失败:', error)
					}
				}
			}
		})
	}
}

/** 取消收藏/取消点赞 */
const removeItem = async (noteId, index, actionType) => {
	if (removingIds.value.has(noteId)) return
	removingIds.value.add(noteId)
	try {
		await interactionApi.interact(noteId, actionType)
		list.value.splice(index, 1)
		uni.showToast({ title: actionType === 'collect' ? '已取消收藏' : '已取消点赞', icon: 'none' })
	} catch (e) {
		console.error('取消操作失败:', e)
		uni.showToast({ title: '操作失败', icon: 'none' })
	} finally {
		removingIds.value.delete(noteId)
	}
}

const fetchCategories = async () => {
	try {
		const res = await categoryApi.getList()
		let categoryData = res
		if (res && res.code === 200 && res.data) {
			categoryData = res.data
		}
		if (categoryData && Array.isArray(categoryData) && categoryData.length > 0) {
			const flatList = []
			const flatten = (nodes, level = 0) => {
				nodes.forEach(node => {
					flatList.push({
						categoryId: node.categoryId,
						name: (level > 0 ? '　'.repeat(level) + '-' : '') + node.name
					})
					if (node.children && node.children.length > 0) {
						flatten(node.children, level + 1)
					}
				})
			}
			flatten(categoryData)
			const allCategories = [{ categoryId: '', name: '全部分类' }, ...flatList]
			categories.value = [allCategories]
		} else {
			categories.value = [[{ categoryId: '', name: '全部分类' }]]
		}
	} catch (error) {
		console.error('获取分类失败:', error)
		categories.value = [[{ categoryId: '', name: '全部分类' }]]
	}
}

const buildParams = () => {
	return {
		pageNum: pageNum.value,
		pageSize: pageSize,
		keyword: queryParams.value.keyword || undefined,
		categoryId: queryParams.value.categoryId || undefined,
		orderBy: queryParams.value.orderBy,
		orderDirection: queryParams.value.orderDirection
	}
}

/** 根据 type 调用对应 API 加载数据 */
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
		let res
		const params = buildParams()

		if (type.value === 'notes') {
			res = await noteApi.getMyNotes(params)
		} else if (type.value === 'favorites') {
			res = await noteApi.getFavorites(params)
		} else {
			res = await noteApi.getLiked(params)
		}

		if (res && res.records) {
			const newList = res.records.map(item => ({
				noteId: item.noteId,
				title: item.title,
				updateTime: item.updateTime ? item.updateTime.split('T')[0] : ''
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
		console.error('加载数据失败:', error)
		uni.showToast({ title: '加载失败', icon: 'none' })
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

const onSearch = () => {
	queryParams.value.keyword = searchKeyword.value
	loadData(false)
}

const onCategoryConfirm = (e) => {
	const category = e.value[0]
	showCategoryPicker.value = false
	if (category) {
		queryParams.value.categoryId = category.categoryId || null
		selectedCategoryName.value = category.name
		loadData(false)
	}
}

const onSortConfirm = (e) => {
	const sort = e.value[0]
	showSortPicker.value = false
	queryParams.value.orderBy = sort.value
	queryParams.value.orderDirection = sort.direction
	loadData(false)
}

const goToNoteDetail = (noteId) => {
	uni.navigateTo({ url: `/pages/note-detail/note-detail?id=${noteId}` })
}

onLoad((options) => {
	if (options.type) {
		type.value = options.type
	}
	if (options.title) {
		title.value = options.title
		uni.setNavigationBarTitle({ title: options.title })
	}
	fetchCategories()
	loadData(false)
})
</script>

<style lang="scss" scoped>
.note-list-container {
	min-height: 100vh;
	background: #f5f7f9;
	display: flex;
	flex-direction: column;
}

.search-bar {
	padding: 20rpx 30rpx;
	background: #fff;
}

.filter-bar {
	display: flex;
	padding: 20rpx 30rpx;
	background: #fff;
	border-top: 1px solid #f0f0f0;
	gap: 40rpx;

	.filter-item {
		display: flex;
		align-items: center;
		gap: 8rpx;
		font-size: 28rpx;
		color: #666;

		.active {
			color: #1890ff;
		}
	}
}

.list-scroll {
	flex: 1;
	height: calc(100vh - 200rpx);
}

.list-wrapper {
	background: #fff;
}

.note-item {
	display: flex;
	align-items: center;
	padding: 24rpx 30rpx;
	border-bottom: 1px solid #f5f5f5;

	.note-item-left {
		flex-shrink: 0;
	}

	.note-item-content {
		flex: 1;
		margin-left: 10rpx;
		overflow: hidden;

		.note-item-title {
			display: block;
			font-size: 30rpx;
			color: #303133;
			overflow: hidden;
			text-overflow: ellipsis;
			white-space: nowrap;
		}

		.note-item-time {
			display: block;
			font-size: 24rpx;
			color: #909399;
			margin-top: 8rpx;
		}
	}

	.action-icon {
		flex-shrink: 0;
		padding: 10rpx;
	}
}

.loading-wrap {
	padding: 100rpx;
	display: flex;
	justify-content: center;
}

.load-more {
	padding: 30rpx;
	text-align: center;
	font-size: 24rpx;
	color: #999;

	.load-trigger {
		color: #1890ff;
	}
}
</style>
