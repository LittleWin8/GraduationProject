<template>
	<view class="u-padding-30" style="padding-bottom: 100rpx;">
		<!-- 你原有的内容保持不变 -->
		<u-input placeholder="请输入笔记标题" border="bottom" v-model="form.title" customStyle="margin-bottom: 30rpx; font-size: 36rpx; font-weight: bold;"></u-input>
		
		<u-cell-group :border="false">
			<u-cell title="选择分类" :value="form.category" isLink @click="showPicker = true"></u-cell>
			<u-cell title="是否公开">
				<template #right-icon>
					<u-switch v-model="form.isPublic" activeColor="#1890ff"></u-switch>
				</template>
			</u-cell>
		</u-cell-group>

		<view class="u-margin-top-30">
			<u-textarea v-model="form.content" placeholder="开始记录你的想法...(支持Markdown)" count height="400" border="none"></u-textarea>
		</view>

		<view class="u-margin-top-40">
			<u-button type="primary" text="保存笔记" shape="circle" color="#1890ff" @click="submit"></u-button>
		</view>

		<u-picker :show="showPicker" :columns="[categories]" @confirm="confirmCategory" @cancel="showPicker = false"></u-picker>
		
		<!-- 添加自定义 TabBar -->
		<custom-tab-bar />
	</view>
</template>

<script setup>
import { ref, reactive } from 'vue';
import CustomTabBar from '@/components/custom-tab-bar/index.vue'

const showPicker = ref(false);
const categories = ['技术', '生活', '读书', '其他'];
const form = reactive({
	title: '',
	category: '技术',
	content: '',
	isPublic: true
});

const confirmCategory = (e) => {
	form.category = e.value[0];
	showPicker.value = false;
};

const submit = () => {
	console.log("提交数据:", form);
	uni.showToast({ title: '保存成功' });
};
</script>