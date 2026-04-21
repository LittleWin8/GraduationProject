<template>
	<view class="content">
		<up-navbar title="智能笔记测试" placeholder></up-navbar>
		
		<view class="test-area">
			<view class="logo-box">
				<up-image src="/static/logo.png" width="80px" height="80px" shape="circle"></up-image>
			</view>
			
			<view class="text-area">
				<text class="title">SmartNote 项目已启动</text>
			</view>
			
			<view class="button-box">
				<up-button type="primary" text="点击测试组件库" @click="handleTest"></up-button>
			</view>
			
			<view style="margin-top: 20px; text-align: center;">
				<up-icon name="checkmark-circle-fill" color="#2979ff" size="28"></up-icon>
				<text style="margin-left: 10px; color: #2979ff;">如果看到蓝色图标，说明配置成功！</text>
			</view>
		</view>
	</view>
</template>

<script setup>
	// 使用 Vue 3 的 setup 语法糖
	const handleTest = () => {
		uni.showToast({
			title: '组件运行正常！',
			icon: 'success'
		})
	}
</script>

<style lang="scss">
	.content {
		display: flex;
		flex-direction: column;
	}

	.test-area {
		padding: 30px;
		
		.logo-box {
			display: flex;
			justify-content: center;
			margin-top: 50px;
		}
		
		.text-area {
			display: flex;
			justify-content: center;
			margin: 20px 0;
			
			.title {
				font-size: 18px;
				color: #333;
				font-weight: bold;
			}
		}
		
		.button-box {
			margin-top: 30px;
		}
	}
</style>