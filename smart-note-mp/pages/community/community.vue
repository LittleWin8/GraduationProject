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
				@collect="onCollect"
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
import { interactionApi } from '@/api/modules/interaction.js';
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

const likingIds = ref(new Set());
const collectingIds = ref(new Set());

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
});

const fillInteractionStatus = async (notes) => {
	if (!notes || notes.length === 0) return;
	const noteIds = notes.map(n => n.id);
	try {
		const statusMap = await interactionApi.getStatus(noteIds);
		for (const note of notes) {
			const status = statusMap[String(note.id)];
			if (status) {
				note.isLiked = !!status.isLiked;
				note.isCollected = !!status.isCollected;
				note.likes = status.likeCount ?? note.likes;
			}
		}
	} catch (e) {
		console.warn('批量查询互动状态失败:', e);
	}
};

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

		await fillInteractionStatus(mapped);

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

onPullDownRefresh(async () => {
	await loadNotes(true);
	uni.stopPullDownRefresh();
});

onReachBottom(() => {
	loadNotes(false);
});

const onSearch = () => {
	loadNotes(true);
};

const onClearSearch = () => {
	keyword.value = '';
	loadNotes(true);
};

const handleTabClick = (item) => {
	currentTab.value = item.index;
	loadNotes(true);
};

const onLike = async (id) => {
	if (likingIds.value.has(id)) return;
	likingIds.value.add(id);

	const item = noteList.value.find(n => n.id === id);
	if (!item) {
		likingIds.value.delete(id);
		return;
	}

	const prevLiked = item.isLiked;
	const prevLikes = item.likes;

	item.isLiked = !item.isLiked;
	item.likes += item.isLiked ? 1 : -1;

	try {
		const result = await interactionApi.interact(id, 'like');
		item.isLiked = !!result.isLiked;
		item.likes = result.likeCount ?? item.likes;
	} catch (e) {
		console.error('点赞操作失败:', e);
		item.isLiked = prevLiked;
		item.likes = prevLikes;
		uni.showToast({ title: '操作失败', icon: 'none' });
	} finally {
		setTimeout(() => {
			likingIds.value.delete(id);
		}, 500);
	}
};

const goDetail = (id) => {
	uni.navigateTo({ url: `/pages/note-detail/note-detail?id=${id}` });
};

const onCollect = async (id) => {
	if (collectingIds.value.has(id)) return;
	collectingIds.value.add(id);

	const item = noteList.value.find(n => n.id === id);
	if (!item) {
		collectingIds.value.delete(id);
		return;
	}

	const prevCollected = item.isCollected;

	item.isCollected = !item.isCollected;

	try {
		const result = await interactionApi.interact(id, 'collect');
		item.isCollected = !!result.isCollected;
	} catch (e) {
		console.error('收藏操作失败:', e);
		item.isCollected = prevCollected;
		uni.showToast({ title: '操作失败', icon: 'none' });
	} finally {
		setTimeout(() => {
			collectingIds.value.delete(id);
		}, 500);
	}
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
