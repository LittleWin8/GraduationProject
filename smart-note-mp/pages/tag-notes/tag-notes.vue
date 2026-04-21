<template>
	<view class="tag-notes-page">
		<view class="tag-header">
			<text class="tag-name">{{ tagName }}</text>
			<text class="note-count">{{ notes.length }}篇笔记</text>
		</view>
		
		<view class="notes-list">
			<note-card 
				v-for="item in notes" 
				:key="item.id" 
				:note="item"
				@like="onLike"
			/>
			<u-loadmore v-if="notes.length === 0" status="nomore" text="暂无笔记" />
		</view>
		
		<custom-tab-bar />
	</view>
</template>

<script setup>
import { ref } from 'vue'
import { onLoad } from '@dcloudio/uni-app'
import { mockNotes } from '@/common/mock.js'
import NoteCard from '@/components/NoteCard.vue'
import CustomTabBar from '@/components/custom-tab-bar/index.vue'

const tagId = ref('')
const tagName = ref('')
const notes = ref([])

onLoad((options) => {
	tagId.value = options.tagId
	tagName.value = options.tagName
	// 根据标签ID筛选笔记（这里用标签名称模拟，实际应根据 tagId 筛选）
	notes.value = mockNotes.filter(note => note.title && note.title.includes(tagName.value))
})

const onLike = (id) => {
	const item = notes.value.find(n => n.id === id)
	if(item) {
		item.isLiked = !item.isLiked
		item.likes += item.isLiked ? 1 : -1
	}
}
</script>

<style scoped>
.tag-notes-page {
	min-height: 100vh;
	background: #f5f7f9;
	padding-bottom: calc(160rpx + env(safe-area-inset-bottom));
}

.tag-header {
	background: #fff;
	padding: 40rpx;
	text-align: center;
	border-bottom: 1px solid #eee;
}

.tag-name {
	font-size: 40rpx;
	font-weight: bold;
	color: #333;
}

.note-count {
	font-size: 24rpx;
	color: #999;
	margin-left: 20rpx;
}

.notes-list {
	padding: 20rpx;
}
</style>