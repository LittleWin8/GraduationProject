<template>
	<view class="profile-container">
		<!-- 你原有的内容保持不变 -->
		<view class="header-box u-flex u-col-center">
			<u-avatar src="https://via.placeholder.com/100" size="60"></u-avatar>
			<view class="u-margin-left-30">
				<view class="u-font-36 u-main-color u-font-weight">极客开发者</view>
				<view class="u-font-24 u-tips-color u-margin-top-10">个人介绍：坚持记录，沉淀知识。</view>
			</view>
		</view>

		<u-grid :border="false" col="3" customStyle="background:#fff; padding:30rpx 0;">
			<u-grid-item>
				<text class="num">12</text>
				<text class="label">笔记</text>
			</u-grid-item>
			<u-grid-item>
				<text class="num">128</text>
				<text class="label">获赞</text>
			</u-grid-item>
			<u-grid-item>
				<text class="num">45</text>
				<text class="label">收藏</text>
			</u-grid-item>
		</u-grid>

		<view class="u-margin-top-20">
			<u-tabs :list="tabs" @click="e => current = e.index"></u-tabs>
			<view class="list-content">
				<u-empty v-if="current === 1" mode="list" text="暂无收藏" marginTop="100"></u-empty>
				<u-cell-group v-else>
					<u-cell v-for="(n, i) in 3" :key="i" :title="'我的笔记内容预览 ' + (i+1)" label="2026-04-21" isLink>
						<template #icon>
							<u-icon name="file-text" size="18" customStyle="margin-right:10rpx"></u-icon>
						</template>
					</u-cell>
				</u-cell-group>
			</view>
		</view>

		<view class="u-margin-top-20">
			<u-cell-group>
				<u-cell icon="setting" title="设置" isLink @click="msg"></u-cell>
				<u-cell icon="info-circle" title="关于我们" isLink @click="msg"></u-cell>
			</u-cell-group>
		</view>
		
		<!-- 添加自定义 TabBar -->
		<custom-tab-bar />
	</view>
</template>

<script setup>
import { ref } from 'vue';
import CustomTabBar from '@/components/custom-tab-bar/index.vue'

const current = ref(0);
const tabs = [{name: '我的笔记'}, {name: '我的收藏'}, {name: '赞过'}];
const msg = () => uni.showToast({title: '开发中', icon:'none'});
</script>

<style lang="scss">
.profile-container { background: #f5f7f9; min-height: 100vh; padding-bottom: 100rpx; }
.header-box { background: #fff; padding: 60rpx 40rpx; border-bottom: 1px solid #f5f5f5; }
.num { font-size: 34rpx; font-weight: bold; color: #1890ff; }
.label { font-size: 24rpx; color: #909399; margin-top: 10rpx; }
.list-content { background: #fff; min-height: 300rpx; }
</style>