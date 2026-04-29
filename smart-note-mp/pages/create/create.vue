<template>
	<view class="create-page">
		<view class="content">
			<u-input
				placeholder="请输入笔记标题"
				border="bottom"
				v-model="form.title"
				customStyle="margin-bottom: 30rpx; font-size: 36rpx; font-weight: bold;"
			></u-input>

			<view class="tags-section">
				<view class="tags-label">选择标签</view>
				<view class="tags-list">
					<view
						v-for="tag in myTags"
						:key="tag.tagId"
						class="tag-item"
						:class="{ active: selectedTagIds.includes(tag.tagId) }"
						@click="toggleTag(tag.tagId)"
					>
						{{ tag.name }}
					</view>
					<view class="tag-item add-tag" @click="showTagInput = true">
						+ 新增
					</view>
				</view>
			</view>

			<u-cell-group :border="false">
				<u-cell title="选择分类" :value="selectedCategoryName" isLink @click="showPicker = true"></u-cell>
				<u-cell title="是否公开">
					<template #right-icon>
						<u-switch v-model="form.isPublic" :activeValue="1" :inactiveValue="0" activeColor="#1890ff"></u-switch>
					</template>
				</u-cell>
			</u-cell-group>

			<view class="u-margin-top-30 content-editor">
				<textarea
					v-model="form.content"
					placeholder="开始记录你的想法...(支持Markdown)"
					:maxlength="-1"
					:auto-height="false"
					:adjust-position="true"
					confirm-type="newline"
					:cursor-spacing="20"
					:fixed="true"
					class="editor-textarea"
					@confirm="onTextareaConfirm"
				/>
				<view class="editor-count" v-if="form.content">{{ form.content.length }} 字</view>
			</view>

			<view class="btn-wrapper">
				<view class="btn-row">
					<u-button
						class="btn-preview"
						text="预览"
						shape="circle"
						color="#f0f0f0"
						:customStyle="{ color: '#333' }"
						@click="showPreview"
					></u-button>
					<u-button
						class="btn-save"
						type="primary"
						:text="isEdit ? '更新笔记' : '保存笔记'"
						shape="circle"
						color="#1890ff"
						@click="submit"
						:loading="submitting"
						:disabled="submitted"
					></u-button>
				</view>
			</view>
		</view>

		<u-picker :show="showPicker" :columns="[categoryNames]" @confirm="confirmCategory" @cancel="showPicker = false"></u-picker>

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
					<u-button type="primary" text="确定" shape="circle" @click="addNewTag" :loading="creatingTag"></u-button>
				</view>
			</view>
		</u-popup>

		<u-popup :show="previewVisible" mode="bottom" @close="previewVisible = false" :closeOnClickOverlay="true" round="20">
			<view class="preview-popup">
				<view class="preview-header">
					<text class="preview-title">{{ form.title || '预览' }}</text>
					<view class="preview-close" @click="previewVisible = false">
						<u-icon name="close" size="20"></u-icon>
					</view>
				</view>
				<scroll-view scroll-y class="preview-body">
					<rich-text :nodes="renderedContent"></rich-text>
				</scroll-view>
			</view>
		</u-popup>
	</view>
</template>

<script setup>
import { ref, reactive, computed } from 'vue';
import { onLoad, onShow } from '@dcloudio/uni-app';
import MarkdownIt from 'markdown-it';
import { noteApi } from '@/api/modules/note.js';
import { categoryApi } from '@/api/modules/category.js';
import { tagApi } from '@/api/modules/tag.js';

const md = new MarkdownIt({ html: false, linkify: true, breaks: true });

const showPicker = ref(false);
const submitting = ref(false);
const submitted = ref(false);
const creatingTag = ref(false);

const editNoteId = ref(null);
const isEdit = computed(() => !!editNoteId.value);

const form = reactive({
	title: '',
	categoryId: null,
	content: '',
	isPublic: 1
});

const previewVisible = ref(false);
const renderedContent = computed(() => {
	return md.render(form.content || '');
});

const categoryList = ref([]);
const categoryNames = computed(() => categoryList.value.map(c => c.name));
const selectedCategoryName = computed(() => {
	if (!form.categoryId) return '请选择';
	const cat = categoryList.value.find(c => c.categoryId === form.categoryId);
	return cat ? cat.name : '请选择';
});

const myTags = ref([]);
const selectedTagIds = ref([]);
const showTagInput = ref(false);
const newTagName = ref('');

const loadCategories = async () => {
	try {
		const tree = await categoryApi.getList();
		const flat = [];
		const flatten = (nodes) => {
			if (!nodes) return;
			nodes.forEach(node => {
				flat.push({ categoryId: node.categoryId, name: node.name });
				if (node.children && node.children.length) {
					flatten(node.children);
				}
			});
		};
		flatten(tree);
		categoryList.value = flat;
		if (flat.length > 0 && !form.categoryId) {
			form.categoryId = flat[0].categoryId;
		}
	} catch (e) {
		console.error('加载分类失败:', e);
	}
};

const loadTags = async () => {
	try {
		const list = await tagApi.getMyTags();
		myTags.value = list || [];
	} catch (e) {
		console.error('加载标签失败:', e);
	}
};

const loadNoteForEdit = async (id) => {
	try {
		const detail = await noteApi.getNoteDetail(id);
		if (!detail) {
			uni.showToast({ title: '笔记不存在', icon: 'none' });
			return;
		}
		form.title = detail.title || '';
		form.content = detail.content || '';
		form.isPublic = detail.isPublic === 1 ? 1 : 0;
		if (detail.categoryId) {
			form.categoryId = detail.categoryId;
		}
		if (detail.tagIds && detail.tagIds.length) {
			selectedTagIds.value = [...detail.tagIds];
		}
	} catch (e) {
		console.error('加载笔记详情失败:', e);
		uni.showToast({ title: '加载笔记失败', icon: 'none' });
	}
};

const toggleTag = (tagId) => {
	const index = selectedTagIds.value.indexOf(tagId);
	if (index === -1) {
		if (selectedTagIds.value.length >= 3) {
			uni.showToast({ title: '最多选择3个标签', icon: 'none' });
			return;
		}
		selectedTagIds.value.push(tagId);
	} else {
		selectedTagIds.value.splice(index, 1);
	}
};

const addNewTag = async () => {
	const name = newTagName.value.trim();
	if (!name) {
		uni.showToast({ title: '请输入标签名称', icon: 'none' });
		return;
	}
	if (name.length > 8) {
		uni.showToast({ title: '标签名称不能超过8个字', icon: 'none' });
		return;
	}
	if (myTags.value.some(t => t.name === name)) {
		uni.showToast({ title: '标签已存在', icon: 'none' });
		return;
	}

	creatingTag.value = true;
	try {
		const newTag = await tagApi.createTag(name);
		myTags.value.push(newTag);
		if (newTag && newTag.tagId) {
			selectedTagIds.value.push(newTag.tagId);
		}
		showTagInput.value = false;
		newTagName.value = '';
		uni.showToast({ title: '添加成功', icon: 'success' });
	} catch (e) {
		console.error('创建标签失败:', e);
	} finally {
		creatingTag.value = false;
	}
};

const confirmCategory = (e) => {
	const name = e.value[0];
	const cat = categoryList.value.find(c => c.name === name);
	if (cat) {
		form.categoryId = cat.categoryId;
	}
	showPicker.value = false;
};

const showPreview = () => {
	if (!form.content.trim()) {
		uni.showToast({ title: '请先输入内容再预览', icon: 'none' });
		return;
	}
	previewVisible.value = true;
};

const onTextareaConfirm = () => {
	const { platform } = uni.getSystemInfoSync();
	if (platform === 'devtools') {
		form.content += '\n';
	}
};

const resetForm = () => {
	form.title = '';
	form.content = '';
	form.categoryId = null;
	form.isPublic = 1;
	selectedTagIds.value = [];
	editNoteId.value = null;
	submitted.value = false;
	uni.setNavigationBarTitle({ title: '发布笔记' });
};

const submit = async () => {
	if (submitted.value) return;
	if (!form.title.trim()) {
		uni.showToast({ title: '请输入笔记标题', icon: 'none' });
		return;
	}
	if (!form.content.trim()) {
		uni.showToast({ title: '请输入笔记内容', icon: 'none' });
		return;
	}

	submitting.value = true;
	try {
		const data = {
			title: form.title.trim(),
			content: form.content.trim(),
			categoryId: form.categoryId,
			isPublic: form.isPublic,
			tagIds: selectedTagIds.value.length > 0 ? selectedTagIds.value : undefined
		};

		if (isEdit.value) {
			await noteApi.updateNote(editNoteId.value, data);
			submitted.value = true;
			uni.$emit('noteUpdated')
			uni.showToast({ title: '更新成功', icon: 'success' });
			setTimeout(() => {
				uni.navigateBack();
			}, 1500);
		} else {
			await noteApi.createNote(data);
			submitted.value = true;
			uni.$emit('noteCreated')
			uni.showToast({ title: '保存成功', icon: 'success' });
			setTimeout(() => {
				resetForm();
				uni.switchTab({ url: '/pages/community/community' });
			}, 1500);
		}
	} catch (e) {
		console.error(isEdit.value ? '更新笔记失败:' : '创建笔记失败:', e);
	} finally {
		submitting.value = false;
	}
};

onLoad((options) => {
	if (options && options.id) {
		editNoteId.value = Number(options.id);
		uni.setNavigationBarTitle({ title: '编辑笔记' });
	} else {
		uni.setNavigationBarTitle({ title: '发布笔记' });
	}
});

onShow(() => {
	loadCategories();
	loadTags();
	if (editNoteId.value) {
		loadNoteForEdit(editNoteId.value);
	}
});
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

.content-editor {
	position: relative;
}

.editor-textarea {
	width: 100%;
	min-height: 500rpx;
	padding: 20rpx;
	font-size: 30rpx;
	line-height: 1.8;
	color: #303133;
	box-sizing: border-box;
	background: #f5f7f9;
	border-radius: 12rpx;
}

.editor-count {
	text-align: right;
	font-size: 24rpx;
	color: #909399;
	margin-top: 8rpx;
}

.btn-row {
	display: flex;
	gap: 24rpx;
}

.btn-preview {
	flex: 1;
}

.btn-save {
	flex: 2;
}

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

.preview-popup {
	height: 80vh;
	display: flex;
	flex-direction: column;
	background: #fff;
}

.preview-header {
	display: flex;
	align-items: center;
	justify-content: space-between;
	padding: 24rpx 30rpx;
	border-bottom: 1px solid #f0f0f0;
}

.preview-title {
	font-size: 34rpx;
	font-weight: bold;
	color: #303133;
	flex: 1;
	overflow: hidden;
	text-overflow: ellipsis;
	white-space: nowrap;
}

.preview-close {
	padding: 10rpx;
}

.preview-body {
	flex: 1;
	padding: 30rpx;
	font-size: 30rpx;
	line-height: 1.8;
	color: #303133;
}
</style>
