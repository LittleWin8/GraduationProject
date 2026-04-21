<template>
	<view class="create-page">
		<view class="content">
			<u-input 
				placeholder="请输入笔记标题" 
				border="bottom" 
				v-model="form.title" 
				customStyle="margin-bottom: 30rpx; font-size: 36rpx; font-weight: bold;"
			></u-input>
			
			<!-- 标签选择区域（新增） -->
			<view class="tags-section">
				<view class="tags-label">选择标签</view>
				<view class="tags-list">
					<view 
						v-for="tag in myTags" 
						:key="tag.id"
						class="tag-item"
						:class="{ active: selectedTags.includes(tag.id) }"
						@click="toggleTag(tag.id)"
					>
						{{ tag.name }}
					</view>
					<view class="tag-item add-tag" @click="showTagInput = true">
						+ 新增
					</view>
				</view>
			</view>
			
			<u-cell-group :border="false">
				<u-cell title="选择分类" :value="form.category" isLink @click="showPicker = true"></u-cell>
				<u-cell title="是否公开">
					<template #right-icon>
						<u-switch v-model="form.isPublic" activeColor="#1890ff"></u-switch>
					</template>
				</u-cell>
			</u-cell-group>

			<view class="u-margin-top-30">
				<u-textarea 
					v-model="form.content" 
					placeholder="开始记录你的想法...(支持Markdown)" 
					count 
					height="400" 
					border="none"
				></u-textarea>
			</view>

			<!-- 按钮容器 -->
			<view class="btn-wrapper">
				<u-button type="primary" text="保存笔记" shape="circle" color="#1890ff" @click="submit"></u-button>
			</view>
		</view>

		<u-picker :show="showPicker" :columns="[categories]" @confirm="confirmCategory" @cancel="showPicker = false"></u-picker>
		
		<!-- 新增标签弹窗 -->
		<u-popup :show="showTagInput" mode="center" @close="showTagInput = false" round="20">
			<view class="tag-popup">
				<view class="popup-title">新增标签</view>
				<u-input 
					placeholder="输入标签名称（最多8个字）" 
					v-model="newTagName"
					border="bottom"
					maxlength="8"
				/>
				<view class="popup-buttons">
					<u-button text="取消" shape="circle" @click="showTagInput = false"></u-button>
					<u-button type="primary" text="确定" shape="circle" @click="addNewTag"></u-button>
				</view>
			</view>
		</u-popup>
		
		<!-- 自定义 TabBar -->
		<custom-tab-bar />
	</view>
</template>

<script setup>
import { ref, reactive } from 'vue';
import { onShow } from '@dcloudio/uni-app';
import CustomTabBar from '@/components/custom-tab-bar/index.vue'

const showPicker = ref(false);
const categories = ['技术', '生活', '读书', '其他'];
const form = reactive({
	title: '',
	category: '技术',
	content: '',
	isPublic: true,
	tags: []  // 新增：选中的标签ID数组
});

// 标签相关
const myTags = ref([])  // 用户的标签列表
const selectedTags = ref([])  // 当前选中的标签ID
const showTagInput = ref(false)  // 是否显示新增标签弹窗
const newTagName = ref('')  // 新标签名称

// 加载用户标签
const loadTags = () => {
	const saved = uni.getStorageSync('user_tags')
	if (saved) {
		myTags.value = JSON.parse(saved)
	} else {
		// 默认标签
		myTags.value = [
			{ id: '1', name: '技术', noteCount: 0 },
			{ id: '2', name: '生活', noteCount: 0 },
			{ id: '3', name: '读书', noteCount: 0 }
		]
	}
}

// 保存标签到本地
const saveTags = () => {
	uni.setStorageSync('user_tags', JSON.stringify(myTags.value))
}

// 切换标签选中状态
const toggleTag = (tagId) => {
	const index = selectedTags.value.indexOf(tagId)
	if (index === -1) {
		// 最多选3个标签
		if (selectedTags.value.length >= 3) {
			uni.showToast({ title: '最多选择3个标签', icon: 'none' })
			return
		}
		selectedTags.value.push(tagId)
	} else {
		selectedTags.value.splice(index, 1)
	}
}

// 新增标签
const addNewTag = () => {
	const name = newTagName.value.trim()
	if (!name) {
		uni.showToast({ title: '请输入标签名称', icon: 'none' })
		return
	}
	
	if (name.length > 8) {
		uni.showToast({ title: '标签名称不能超过8个字', icon: 'none' })
		return
	}
	
	// 检查是否重复
	if (myTags.value.some(t => t.name === name)) {
		uni.showToast({ title: '标签已存在', icon: 'none' })
		return
	}
	
	// 添加新标签
	const newTag = {
		id: Date.now().toString(),
		name: name,
		noteCount: 0
	}
	myTags.value.push(newTag)
	saveTags()
	
	// 自动选中新标签
	selectedTags.value.push(newTag.id)
	
	// 关闭弹窗并清空输入
	showTagInput.value = false
	newTagName.value = ''
	uni.showToast({ title: '添加成功', icon: 'success' })
}

const confirmCategory = (e) => {
	form.category = e.value[0];
	showPicker.value = false;
};

const submit = () => {
	// 获取选中的标签名称
	const selectedTagNames = selectedTags.value.map(id => {
		const tag = myTags.value.find(t => t.id === id)
		return tag ? tag.name : ''
	}).filter(name => name)
	
	const submitData = {
		...form,
		tags: selectedTagNames  // 提交选中的标签名称
	}
	
	console.log("提交数据:", submitData);
	
	// 更新标签的笔记计数
	selectedTags.value.forEach(tagId => {
		const tag = myTags.value.find(t => t.id === tagId)
		if (tag) {
			tag.noteCount = (tag.noteCount || 0) + 1
		}
	})
	saveTags()
	
	uni.showToast({ title: '保存成功' });
	
	// 可选：清空表单
	// form.title = ''
	// form.content = ''
	// selectedTags.value = []
};

// 页面显示时加载标签
onShow(() => {
	loadTags()
})
</script>

<style scoped>
.create-page {
	min-height: 100vh;
	background-color: #fff;
}

.content {
	padding: 30rpx;
	padding-bottom: calc(160rpx + env(safe-area-inset-bottom));
}

/* 标签选择区域样式 */
.tags-section {
	margin-bottom: 30rpx;
	background: #fff;
}

.tags-label {
	font-size: 28rpx;
	color: #333;
	margin-bottom: 20rpx;
	font-weight: 500;
}

.tags-list {
	display: flex;
	flex-wrap: wrap;
	gap: 20rpx;
}

.tag-item {
	padding: 12rpx 30rpx;
	background: #f5f7f9;
	border-radius: 40rpx;
	font-size: 26rpx;
	color: #666;
	transition: all 0.2s;
}

.tag-item.active {
	background: #1890ff;
	color: #fff;
}

.tag-item.add-tag {
	background: #fff;
	border: 1px dashed #ccc;
	color: #999;
}

.btn-wrapper {
	margin-top: 60rpx;
}

/* 弹窗样式 */
.tag-popup {
	width: 500rpx;
	padding: 40rpx;
	background: #fff;
	border-radius: 20rpx;
}

.popup-title {
	font-size: 34rpx;
	font-weight: bold;
	color: #333;
	text-align: center;
	margin-bottom: 30rpx;
}

.popup-buttons {
	display: flex;
	gap: 20rpx;
	margin-top: 40rpx;
}

.popup-buttons button {
	flex: 1;
}
</style>