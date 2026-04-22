<template>
	<view class="page-container">
		<u-sticky bgColor="#fff">
			<view class="u-padding-20">
				<u-search placeholder="搜索笔记内容" v-model="keyword" :showAction="false" @change="onSearch"></u-search>
			</view>
			<u-tabs :list="tabList" @click="handleTabClick" lineColor="#1890ff" :activeStyle="{color: '#1890ff', fontWeight: 'bold'}"></u-tabs>
		</u-sticky>

		<view class="list-body">
			<note-card 
				v-for="item in filteredList" 
				:key="item.id" 
				:note="item"
				@like="onLike"
			/>
			<u-loadmore status="nomore" line />
		</view>
		<custom-tab-bar />
	</view>
</template>

<script setup>
import { ref, computed } from 'vue';
import { mockNotes } from '@/common/mock.js';
import NoteCard from '@/components/notecard/index.vue';
import CustomTabBar from '@/components/custom-tab-bar/index.vue'

// ✅ 移除 export default，在 <script setup> 中导入的组件可以直接使用

const keyword = ref('');
const list = ref(mockNotes);
const tabList = [{name: '最新'}, {name: '最热'}];

const filteredList = computed(() => {
	return list.value.filter(item => item.title.includes(keyword.value));
});

const onLike = (id) => {
	const item = list.value.find(n => n.id === id);
	if(item) {
		item.isLiked = !item.isLiked;
		item.likes += item.isLiked ? 1 : -1;
	}
};

// 添加搜索方法（你的模板里用到了 @change）
const onSearch = () => {
	// 搜索逻辑，computed 已经自动处理了，这里可以留空或添加额外逻辑
};

// 添加 tab 切换方法（模板里用到了 @click）
const handleTabClick = (index) => {
	// 你的 tab 切换逻辑
	console.log('切换到:', tabList[index.index].name);
};
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