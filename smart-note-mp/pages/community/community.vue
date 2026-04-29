<template>
	<view class="page-container">
		<u-sticky bgColor="#fff">
			<!-- 搜索框 -->
			<view class="u-padding-20">
				<u-search placeholder="搜索笔记内容" v-model="keyword" :showAction="false" @search="onSearch" @clear="onClearSearch"></u-search>
			</view>

			<!-- 一级分类 Tab -->
			<u-tabs
				:list="topCategoryTabs"
				@click="onTopCategoryChange"
				lineColor="#1890ff"
				:activeStyle="{color: '#1890ff', fontWeight: 'bold'}"
				:current="topCategoryIndex"
			></u-tabs>

			<!-- 二级分类横向滚动（选中非"全部"大分类时显示） -->
			<scroll-view v-if="subCategories.length > 0" class="sub-category-scroll" scroll-x enable-flex>
				<view
					v-for="sub in subCategories"
					:key="sub.categoryId ?? 'all'"
					class="sub-tag"
					:class="{ active: subCategorySelected === sub.categoryId }"
					@click="onSubCategoryClick(sub.categoryId)"
				>
					{{ sub.name }}
				</view>
			</scroll-view>

			<!-- 排序 Tab -->
			<u-tabs :list="tabList" @click="handleTabClick" lineColor="#1890ff" :activeStyle="{color: '#1890ff', fontWeight: 'bold'}" :current="currentTab"></u-tabs>
		</u-sticky>

		<view class="list-body">
			<note-card
				v-for="item in noteList"
				:key="item.id"
				:note="item"
				@like="onLike"
				@collect="onCollect"
				@click="goDetail"
			/>
			<u-loadmore :status="loadStatus" line />
		</view>
		<custom-tab-bar />
	</view>
</template>

<script setup>
import { ref, computed } from 'vue'
import { onShow, onPullDownRefresh, onReachBottom } from '@dcloudio/uni-app'
import { noteApi } from '@/api/modules/note.js'
import { interactionApi } from '@/api/modules/interaction.js'
import { categoryApi } from '@/api/modules/category.js'
import NoteCard from '@/components/notecard/index.vue'
import CustomTabBar from '@/components/custom-tab-bar/index.vue'

const keyword = ref('')
const noteList = ref([])
const page = ref(1)
const pageSize = 10
const hasMore = ref(true)
const loading = ref(false)
const currentTab = ref(0)
const tabList = [{ name: '最新' }, { name: '最热' }]

const loadStatus = ref('loadmore')

const likingIds = ref(new Set())
const collectingIds = ref(new Set())

/** 分类相关状态 */
const categoryTree = ref([])
const topCategoryIndex = ref(0)
const subCategorySelected = ref(null)
let noteRequestId = 0

/** 一级分类 Tab 列表（前面加"全部"） */
const topCategoryTabs = computed(() => {
	const all = [{ name: '全部', categoryId: null }]
	return all.concat(categoryTree.value.map(c => ({ name: c.name, categoryId: c.categoryId })))
})

/** 当前选中一级分类 */
const currentTopCategory = computed(() => {
	if (topCategoryIndex.value === 0) return null
	return categoryTree.value[topCategoryIndex.value - 1] || null
})

/** 二级分类列表（前面加"全部"） */
const subCategories = computed(() => {
	const children = currentTopCategory.value?.children || []
	if (children.length === 0) {
		return []
	}
	const all = [{ name: '全部', categoryId: null }]
	return all.concat(children.map(c => ({ name: c.name, categoryId: c.categoryId })))
})

/** 当前实际筛选的 categoryId */
const activeCategoryId = computed(() => {
	if (subCategorySelected.value !== null) return subCategorySelected.value
	if (currentTopCategory.value) return currentTopCategory.value.categoryId
	return null
})

const mapNoteItem = (item) => ({
	id: item.noteId,
	avatar: item.avatar || '',
	author: item.author || '匿名',
	type: item.categoryName || '未分类',
	title: item.title || '',
	summary: item.summary || '',
	isLiked: false,
	isCollected: false,
	likes: item.likeCount || 0,
	comments: item.commentCount || 0
})

const fillInteractionStatus = async (notes) => {
	if (!notes || notes.length === 0) return
	const noteIds = notes.map(n => n.id)
	try {
		const statusMap = await interactionApi.getStatus(noteIds)
		for (const note of notes) {
			const status = statusMap[String(note.id)]
			if (status) {
				note.isLiked = !!status.isLiked
				note.isCollected = !!status.isCollected
				note.likes = status.likeCount ?? note.likes
			}
		}
	} catch (e) {
		console.warn('批量查询互动状态失败:', e)
	}
}

const normalizeCategoryTree = (nodes = []) => {
	if (!Array.isArray(nodes)) return []
	return nodes.map(node => {
		const children = node.children || node.childList || node.subCategories || node.subCategoryList || node.childs || []
		return {
			...node,
			categoryId: node.categoryId ?? node.id,
			name: node.name || node.categoryName || '',
			children: normalizeCategoryTree(children)
		}
	})
}

/** 加载分类树 */
const loadCategories = async () => {
	try {
		const res = await categoryApi.getList()
		let data = res
		if (res && res.code === 200 && res.data) {
			data = res.data
		}
		if (Array.isArray(data)) {
			categoryTree.value = normalizeCategoryTree(data)
		}
	} catch (e) {
		console.error('加载分类失败:', e)
	}
}

const loadNotes = async (reset = false) => {
	if (loading.value && !reset) return
	if (!reset && !hasMore.value) return

	const requestId = ++noteRequestId
	loading.value = true
	if (reset) {
		page.value = 1
		hasMore.value = true
	}

	loadStatus.value = 'loading'

	try {
		const filters = {}
		if (keyword.value) {
			filters.keyword = keyword.value
		}
		if (currentTab.value === 1) {
			filters.orderBy = 'viewCount'
			filters.orderDirection = 'DESC'
		} else {
			filters.orderBy = 'createTime'
			filters.orderDirection = 'DESC'
		}
		if (activeCategoryId.value !== null) {
			filters.categoryId = activeCategoryId.value
		}

		const res = await noteApi.getNotes('public', page.value, pageSize, filters)
		const records = (res && res.records) || []
		const total = (res && res.total) || 0
		const mapped = records.map(mapNoteItem)

		await fillInteractionStatus(mapped)
		if (requestId !== noteRequestId) return

		if (reset) {
			noteList.value = mapped
		} else {
			noteList.value = [...noteList.value, ...mapped]
		}

		hasMore.value = noteList.value.length < total
		page.value++

		loadStatus.value = hasMore.value ? 'loadmore' : 'nomore'
	} catch (e) {
		if (requestId !== noteRequestId) return
		console.error('加载笔记列表失败:', e)
		loadStatus.value = 'loadmore'
	} finally {
		if (requestId === noteRequestId) {
			loading.value = false
		}
	}
}

/** 一级分类切换 */
const onTopCategoryChange = (item) => {
	topCategoryIndex.value = item.index
	subCategorySelected.value = null
	loadNotes(true)
}

/** 二级分类点击 */
const onSubCategoryClick = (categoryId) => {
	subCategorySelected.value = categoryId
	loadNotes(true)
}

onPullDownRefresh(async () => {
	await loadNotes(true)
	uni.stopPullDownRefresh()
})

onReachBottom(() => {
	loadNotes(false)
})

const onSearch = () => {
	loadNotes(true)
}

const onClearSearch = () => {
	keyword.value = ''
	loadNotes(true)
}

const handleTabClick = (item) => {
	currentTab.value = item.index
	loadNotes(true)
}

const onLike = async (id) => {
	if (likingIds.value.has(id)) return
	likingIds.value.add(id)

	const item = noteList.value.find(n => n.id === id)
	if (!item) {
		likingIds.value.delete(id)
		return
	}

	const prevLiked = item.isLiked
	const prevLikes = item.likes

	item.isLiked = !item.isLiked
	item.likes += item.isLiked ? 1 : -1

	try {
		const result = await interactionApi.interact(id, 'like')
		item.isLiked = !!result.isLiked
		item.likes = result.likeCount ?? item.likes
	} catch (e) {
		console.error('点赞操作失败:', e)
		item.isLiked = prevLiked
		item.likes = prevLikes
		uni.showToast({ title: '操作失败', icon: 'none' })
	} finally {
		setTimeout(() => {
			likingIds.value.delete(id)
		}, 500)
	}
}

const goDetail = (id) => {
	uni.navigateTo({ url: `/pages/note-detail/note-detail?id=${id}` })
}

const onCollect = async (id) => {
	if (collectingIds.value.has(id)) return
	collectingIds.value.add(id)

	const item = noteList.value.find(n => n.id === id)
	if (!item) {
		collectingIds.value.delete(id)
		return
	}

	const prevCollected = item.isCollected

	item.isCollected = !item.isCollected

	try {
		const result = await interactionApi.interact(id, 'collect')
		item.isCollected = !!result.isCollected
	} catch (e) {
		console.error('收藏操作失败:', e)
		item.isCollected = prevCollected
		uni.showToast({ title: '操作失败', icon: 'none' })
	} finally {
		setTimeout(() => {
			collectingIds.value.delete(id)
		}, 500)
	}
}

onShow(() => {
	if (categoryTree.value.length === 0) {
		loadCategories()
	}
	loadNotes(true)
})
</script>

<style>
.page-container {
	background-color: #f5f7f9;
	min-height: 100vh;
	padding-bottom: 100rpx;
	box-sizing: border-box;
}
.list-body { padding-top: 10rpx; }
</style>

<style scoped>
/* 二级分类横向滚动 */
.sub-category-scroll {
	white-space: nowrap;
	padding: 16rpx 20rpx;
	background: #fff;
	border-bottom: 1rpx solid #f0f0f0;
}

.sub-tag {
	display: inline-block;
	padding: 12rpx 28rpx;
	font-size: 24rpx;
	color: #909399;
	background: #f5f7f9;
	border-radius: 30rpx;
	margin-right: 16rpx;
	flex-shrink: 0;
}

.sub-tag.active {
	color: #fff;
	background: #1890ff;
}
</style>
