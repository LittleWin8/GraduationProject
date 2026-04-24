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
			<!-- 加载中 -->
			<view v-if="loading && list.length === 0" class="loading-wrap">
				<u-loading-icon></u-loading-icon>
			</view>
			
			<!-- 空状态 -->
			<u-empty v-else-if="list.length === 0" mode="list" text="暂无数据" marginTop="100"></u-empty>
			
			<!-- 列表 -->
			<u-cell-group v-else>
				<u-cell 
					v-for="(item, i) in list" 
					:key="i" 
					:title="item.title" 
					:label="item.updateTime" 
					isLink 
					@click="goToNoteDetail(item.noteId)"
				>
					<template #icon>
						<u-icon 
							:name="iconName" 
							size="18" 
							customStyle="margin-right:10rpx" 
							:color="iconColor"
						></u-icon>
					</template>
				</u-cell>
				
				<!-- 加载更多状态 -->
				<view class="load-more">
					<text v-if="loadingMore">加载中...</text>
					<text v-else-if="!hasMore">没有更多了</text>
					<text v-else class="load-trigger" @click="loadMore">上拉加载更多</text>
				</view>
			</u-cell-group>
		</scroll-view>
		
		<!-- 分类选择器 -->
		<u-picker
			:show="showCategoryPicker"
			:columns="[categoryList]"
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

// 路由参数
const type = ref('notes')  // notes, favorites, liked
const title = ref('')

// 列表数据
const list = ref([])
const pageNum = ref(1)
const pageSize = 15
const hasMore = ref(true)
const loading = ref(false)
const loadingMore = ref(false)
const refreshing = ref(false)

// 搜索筛选参数
const searchKeyword = ref('')
const queryParams = ref({
	keyword: '',
	categoryId: null,
	orderBy: 'updateTime',
	orderDirection: 'DESC'
})

// 分类相关
const categoryList = ref([])
const showCategoryPicker = ref(false)
const selectedCategoryName = ref('')

// 排序选项
const sortOptions = [
	{ label: '按时间降序', value: 'updateTime', direction: 'DESC' },
	{ label: '按时间升序', value: 'updateTime', direction: 'ASC' },
	{ label: '按浏览降序', value: 'viewCount', direction: 'DESC' },
	{ label: '按浏览升序', value: 'viewCount', direction: 'ASC' }
]
const showSortPicker = ref(false)

// 图标配置（根据类型）
const iconName = computed(() => {
	if (type.value === 'favorites') return 'star'
	if (type.value === 'liked') return 'thumb-up'
	return 'file-text'
})

const iconColor = computed(() => {
	if (type.value === 'favorites') return '#f5a623'
	if (type.value === 'liked') return '#1890ff'
	return '#999'
})

// 获取分类列表
const fetchCategories = async () => {
	try {
		const res = await categoryApi.getList()
		if (res) {
			categoryList.value = [{ categoryId: null, name: '全部分类' }, ...res]
		}
	} catch (error) {
		console.error('获取分类失败:', error)
	}
}

// 构建请求参数
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

// 加载数据
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
			
			// 判断是否还有更多
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

// 加载更多
const loadMore = () => {
	if (loadingMore.value || !hasMore.value) return
	pageNum.value++
	loadData(true)
}

// 刷新
const onRefresh = () => {
	refreshing.value = true
	loadData(false)
}

// 搜索
const onSearch = () => {
	queryParams.value.keyword = searchKeyword.value
	loadData(false)
}

// 分类筛选确认
const onCategoryConfirm = (e) => {
	const category = e.value[0]
	showCategoryPicker.value = false
	queryParams.value.categoryId = category.categoryId
	selectedCategoryName.value = category.name
	loadData(false)
}

// 排序确认
const onSortConfirm = (e) => {
	const sort = e.value[0]
	showSortPicker.value = false
	queryParams.value.orderBy = sort.value
	queryParams.value.orderDirection = sort.direction
	loadData(false)
}

// 跳转笔记详情
const goToNoteDetail = (noteId) => {
	uni.navigateTo({ url: `/pages/note-detail/note-detail?id=${noteId}` })
}

// 页面加载
onLoad((options) => {
	if (options.type) {
		type.value = options.type
	}
	if (options.title) {
		title.value = options.title
		uni.setNavigationBarTitle({ title: options.title })
	}
	
	// 加载初始数据
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