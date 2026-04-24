<template>
	<view class="tag-manage">
		<!-- 添加标签 -->
		<view class="add-tag-section">
			<u-input 
				placeholder="输入新标签名称" 
				v-model="newTagName"
				border="bottom"
				customStyle="flex: 1;"
			/>
			<u-button 
				type="primary" 
				size="large" 
				text="添加" 
				@click="addTag"
				style="margin-left: 20rpx;"
				customStyle="font-size: 30rpx;"
			/>
		</view>

		<!-- 我的标签列表 -->
		<view class="tag-list">
			<view 
				v-for="tag in myTags" 
				:key="tag.id"
				class="tag-item"
				@click="goToTagNotes(tag)"
			>
				<text class="tag-name">{{ tag.name }}</text>
				<text class="tag-count">{{ tag.noteCount || 0 }}篇</text>
				<u-icon name="close-circle-fill" color="#ff6b6b" size="32" @click.stop="deleteTag(tag.id)"></u-icon>
			</view>
			
			<view v-if="myTags.length === 0" class="empty-tip">
				<text>暂无自定义标签，点击上方添加</text>
			</view>
		</view>

		<!-- 推荐标签 -->
		<view class="recommend-section">
			<view class="section-title">推荐标签</view>
			<view class="recommend-list">
				<view 
					v-for="tag in recommendTags" 
					:key="tag"
					class="recommend-tag"
					@click="addRecommendTag(tag)"
				>
					+ {{ tag }}
				</view>
			</view>
		</view>
		
		<custom-tab-bar />
	</view>
</template>

<script setup>
import { onShow } from '@dcloudio/uni-app'
import { ref } from 'vue'
import CustomTabBar from '@/components/custom-tab-bar/index.vue'
import { tagApi } from '@/api/index.js'

const newTagName = ref('')
const myTags = ref([])
const loading = ref(false)
const addLoading = ref(false)

// 从后端加载标签
const loadTags = async () => {
	loading.value = true
	try {
		const tags = await tagApi.getMyTags()
		myTags.value = tags || []
	} catch (error) {
		console.error('加载标签失败:', error)
		uni.showToast({ title: '加载标签失败', icon: 'none' })
		// 如果后端没有数据，使用空数组
		myTags.value = []
	} finally {
		loading.value = false
	}
}

// 添加标签
const addTag = async () => {
	const name = newTagName.value.trim()
	if (!name) {
		uni.showToast({ title: '请输入标签名称', icon: 'none' })
		return
	}
	
	// 检查是否重复
	if (myTags.value.some(t => t.name === name)) {
		uni.showToast({ title: '标签已存在', icon: 'none' })
		return
	}
	
	addLoading.value = true
	try {
		// 调用后端API创建标签
		const newTag = await tagApi.createTag(name)
		
		// 添加到列表
		myTags.value.push({
			id: newTag.id,
			name: newTag.name,
			noteCount: 0
		})
		
		newTagName.value = ''
		uni.showToast({ title: '添加成功', icon: 'success' })
	} catch (error) {
		console.error('添加标签失败:', error)
		// 如果错误信息包含"已存在"，显示友好提示
		if (error.message && error.message.includes('已存在')) {
			uni.showToast({ title: '标签已存在', icon: 'none' })
		} else {
			uni.showToast({ title: '添加失败，请重试', icon: 'none' })
		}
	} finally {
		addLoading.value = false
	}
}

// 删除标签
const deleteTag = async (id) => {
	uni.showModal({
		title: '提示',
		content: '确定要删除这个标签吗？删除后笔记将不再关联此标签。',
		success: async (res) => {
			if (res.confirm) {
				try {
					// 调用后端API删除标签
					await tagApi.deleteTag(id)
					
					// 从列表中移除
					myTags.value = myTags.value.filter(t => t.id !== id)
					uni.showToast({ title: '删除成功', icon: 'success' })
				} catch (error) {
					console.error('删除标签失败:', error)
					uni.showToast({ title: '删除失败，请重试', icon: 'none' })
				}
			}
		}
	})
}

// 跳转到标签笔记页
const goToTagNotes = (tag) => {
	uni.navigateTo({
		url: `/pages/tag-notes/tag-notes?tagId=${tag.id}&tagName=${encodeURIComponent(tag.name)}`
	})
}

// 推荐标签列表
const recommendTags = ['AI', '前端', '后端', '产品', '设计', '创业', '读书', '生活', '技术', '美食']

const addRecommendTag = async (tagName) => {
	// 检查是否重复
	if (myTags.value.some(t => t.name === tagName)) {
		uni.showToast({ title: '标签已存在', icon: 'none' })
		return
	}
	
	try {
		// 调用后端API创建标签
		const newTag = await tagApi.createTag(tagName)
		
		// 添加到列表
		myTags.value.push({
			id: newTag.id,
			name: newTag.name,
			noteCount: 0
		})
		
		uni.showToast({ title: '添加成功', icon: 'success' })
	} catch (error) {
		console.error('添加推荐标签失败:', error)
		if (error.message && error.message.includes('已存在')) {
			uni.showToast({ title: '标签已存在', icon: 'none' })
		} else {
			uni.showToast({ title: '添加失败，请重试', icon: 'none' })
		}
	}
}

onShow(() => {
	loadTags()
})
</script>

<style scoped>
.tag-manage {
	min-height: 100vh;
	background: #f5f7f9;
	padding: 30rpx;
	padding-bottom: calc(160rpx + env(safe-area-inset-bottom));
}

.add-tag-section {
	display: flex;
	align-items: center;
	background: #fff;
	padding: 30rpx;
	border-radius: 20rpx;
	margin-bottom: 30rpx;
}

.tag-list {
	background: #fff;
	border-radius: 20rpx;
	overflow: hidden;
	margin-bottom: 40rpx;
}

.tag-item {
	display: flex;
	align-items: center;
	justify-content: space-between;
	padding: 30rpx;
	border-bottom: 1px solid #eee;
}

.tag-item:last-child {
	border-bottom: none;
}

.tag-name {
	font-size: 32rpx;
	font-weight: 500;
	color: #333;
	flex: 1;
}

.tag-count {
	font-size: 24rpx;
	color: #999;
	margin-right: 20rpx;
}

.empty-tip {
	padding: 60rpx;
	text-align: center;
	color: #999;
	font-size: 28rpx;
}

.recommend-section {
	background: #fff;
	border-radius: 20rpx;
	padding: 30rpx;
}

.section-title {
	font-size: 32rpx;
	font-weight: bold;
	color: #333;
	margin-bottom: 30rpx;
}

.recommend-list {
	display: flex;
	flex-wrap: wrap;
	gap: 20rpx;
}

.recommend-tag {
	background: #f0f2f5;
	padding: 15rpx 30rpx;
	border-radius: 40rpx;
	font-size: 28rpx;
	color: #1890ff;
}
</style>