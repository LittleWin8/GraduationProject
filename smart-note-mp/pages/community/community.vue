<template>
	<view class="page-container">
		<u-sticky bgColor="#fff">
			<view class="u-padding-20">
				<u-search placeholder="搜索笔记内容" v-model="keyword" :showAction="false" @search="onSearch" @clear="onClearSearch"></u-search>
			</view>
			<u-tabs :list="tabList" @click="handleTabClick" lineColor="#1890ff" :activeStyle="{color: '#1890ff', fontWeight: 'bold'}"></u-tabs>
		</u-sticky>

		<view class="list-body">
			<note-card
				v-for="item in noteList"
				:key="item.id"
				:note="item"
				@like="onLike"
				@click="goDetail"
			/>
			<u-loadmore :status="loadStatus" line />
		</view>
		<custom-tab-bar />
	</view>
</template>

<script setup>
import { ref } from 'vue';
import { onShow, onPullDownRefresh, onReachBottom } from '@dcloudio/uni-app';
import { noteApi } from '@/api/modules/note.js';
import NoteCard from '@/components/notecard/index.vue';
import CustomTabBar from '@/components/custom-tab-bar/index.vue';

const keyword = ref('');
const noteList = ref([]);
const page = ref(1);
const pageSize = 10;
const hasMore = ref(true);
const loading = ref(false);
const currentTab = ref(0);
const tabList = [{ name: '最新' }, { name: '最热' }];

const loadStatus = ref('loadmore');

/**
 * 将后端 NoteListVO 映射为 NoteCard 组件所需格式
 */
const mapNoteItem = (item) => ({
	id: item.noteId,
	avatar: item.avatar || '',
	author: item.author || '匿名',
	type: item.categoryName || '未分类',
	title: item.title || '',
	summary: item.summary || '',
	isLiked: false,
	likes: item.likeCount || 0,
	comments: item.commentCount || 0
});

/**
 * 加载笔记列表
 * @param {boolean} reset 是否重置列表（下拉刷新时为 true）
 */
const loadNotes = async (reset = false) => {
	if (loading.value) return;
	if (!reset && !hasMore.value) return;

	loading.value = true;
	if (reset) {
		page.value = 1;
		hasMore.value = true;
	}

	loadStatus.value = 'loading';

	try {
		const filters = {};
		if (keyword.value) {
			filters.keyword = keyword.value;
		}
		if (currentTab.value === 1) {
			filters.orderBy = 'viewCount';
			filters.orderDirection = 'DESC';
		} else {
			filters.orderBy = 'createTime';
			filters.orderDirection = 'DESC';
		}

		const res = await noteApi.getNotes('public', page.value, pageSize, filters);
		const records = (res && res.records) || [];
		const total = (res && res.total) || 0;
		const mapped = records.map(mapNoteItem);

		if (reset) {
			noteList.value = mapped;
		} else {
			noteList.value = [...noteList.value, ...mapped];
		}

		hasMore.value = noteList.value.length < total;
		page.value++;

		loadStatus.value = hasMore.value ? 'loadmore' : 'nomore';
	} catch (e) {
		console.error('加载笔记列表失败:', e);
		loadStatus.value = 'loadmore';
	} finally {
		loading.value = false;
	}
};

/**
 * 下拉刷新
 */
onPullDownRefresh(async () => {
	await loadNotes(true);
	uni.stopPullDownRefresh();
});

/**
 * 上拉加载更多
 */
onReachBottom(() => {
	loadNotes(false);
});

/**
 * 搜索触发
 */
const onSearch = () => {
	loadNotes(true);
};

/**
 * 清空搜索
 */
const onClearSearch = () => {
	keyword.value = '';
	loadNotes(true);
};

/**
 * Tab 切换：最新 / 最热
 */
const handleTabClick = (item) => {
	currentTab.value = item.index;
	loadNotes(true);
};

/**
 * 点赞
 */
const onLike = (id) => {
	const item = noteList.value.find(n => n.id === id);
	if (item) {
		item.isLiked = !item.isLiked;
		item.likes += item.isLiked ? 1 : -1;
	}
};

/**
 * 跳转笔记详情
 */
const goDetail = (id) => {
	uni.navigateTo({ url: `/pages/note-detail/note-detail?id=${id}` });
};

onShow(() => {
	loadNotes(true);
});
</script>

<style>
.page-container { background-color: #f5f7f9; min-height: 100vh; }
.list-body { padding-top: 10rpx; }
</style>

<style scoped>
view {
  padding-bottom: 100rpx;
}
</style>
