<template>
	<view class="note-card" @tap="$emit('click', note.id)">
		<view class="u-flex u-row-between u-margin-bottom-20">
			<view class="u-flex">
				<u-avatar :src="note.avatar" size="24"></u-avatar>
				<text class="u-margin-left-15 u-font-24 u-tips-color">{{ note.author }}</text>
			</view>
			<u-tag :text="note.type" size="mini" type="primary" plain></u-tag>
		</view>
		
		<view class="title u-line-1 u-margin-bottom-10">{{ note.title }}</view>
		<view class="summary u-line-2 u-margin-bottom-20">{{ note.summary }}</view>
		
		<view class="u-flex u-row-right">
			<view class="action-item" @tap.stop="handleLike">
				<u-icon :name="note.isLiked ? 'heart-fill' : 'heart'" :color="note.isLiked ? '#fa3534' : '#909399'" size="18"></u-icon>
				<text class="u-margin-left-5">{{ note.likes }}</text>
			</view>
			<view class="action-item u-margin-left-30">
				<u-icon name="chat" color="#909399" size="18"></u-icon>
				<text class="u-margin-left-5">{{ note.comments }}</text>
			</view>
		</view>
	</view>
</template>

<script setup>
const props = defineProps(['note']);
const emit = defineEmits(['like', 'click']);
const handleLike = () => emit('like', props.note.id);
</script>

<style lang="scss" scoped>
.note-card {
	background: #fff; margin: 20rpx; padding: 30rpx; border-radius: 16rpx;
	box-shadow: 0 4rpx 20rpx rgba(0,0,0,0.03);
	.title { font-size: 32rpx; font-weight: bold; color: #303133; }
	.summary { font-size: 26rpx; color: #606266; line-height: 1.6; }
	.action-item { display: flex; align-items: center; font-size: 24rpx; color: #909399; }
}
</style>