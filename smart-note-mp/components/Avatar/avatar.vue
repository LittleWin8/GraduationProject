<template>
	<view class="avatar" :style="containerStyle">
		<image 
			:src="displaySrc" 
			:style="imageStyle"
			:mode="mode"
			@load="handleImageLoad"
			@error="handleImageError"
			class="avatar-image"
		></image>
		<!-- 加载失败时显示默认图标 -->
		<view v-if="showFallbackIcon && !imageLoaded" class="avatar-fallback">
			<u-icon :name="fallbackIcon" :color="iconColor" :size="iconSize"></u-icon>
		</view>
	</view>
</template>

<script setup>
import { computed, ref, watch, onUnmounted } from 'vue'

const props = defineProps({
	// 头像地址
	src: {
		type: String,
		default: ''
	},
	// 头像大小 (rpx)
	size: {
		type: [Number, String],
		default: 100
	},
	// 圆形/方形
	shape: {
		type: String,
		default: 'circle' // circle | square
	},
	// 图片裁剪模式
	mode: {
		type: String,
		default: 'aspectFill'
	},
	// 默认头像类型（保留兼容）
	defaultType: {
		type: String,
		default: 'default'
	},
	// 自定义默认头像地址（优先级最高）
	customDefault: {
		type: String,
		default: ''
	},
	// 是否显示加载失败的备用图标
	showFallbackIcon: {
		type: Boolean,
		default: true
	},
	// 备用图标名称
	fallbackIcon: {
		type: String,
		default: 'account'
	},
	// 备用图标颜色
	iconColor: {
		type: String,
		default: '#999'
	},
	// 备用图标大小
	iconSize: {
		type: [Number, String],
		default: 40
	}
})

const emit = defineEmits(['load', 'error'])

// 图片加载状态
const imageLoaded = ref(true)

// 默认头像地址（统一使用 gif）
const DEFAULT_AVATAR = '/static/default-avatar.gif'

// 计算最终显示的图片地址
const displaySrc = computed(() => {
	// 1. 优先使用自定义默认头像
	if (props.customDefault && props.customDefault.trim() !== '') {
		return props.customDefault
	}
	
	// 2. 如果有传入 src 且不为空，使用传入的
	if (props.src && props.src.trim() !== '') {
		return props.src
	}
	
	// 3. 否则使用默认头像
	return DEFAULT_AVATAR
})

// 处理尺寸单位
const getSizeInRpx = (size) => {
	if (typeof size === 'number') {
		return `${size}rpx`
	}
	if (typeof size === 'string') {
		if (size.endsWith('rpx')) {
			return size
		}
		if (/^\d+$/.test(size)) {
			return `${size}rpx`
		}
		return size
	}
	return '100rpx'
}

// 容器样式
const containerStyle = computed(() => {
	const sizeInRpx = getSizeInRpx(props.size)
	return {
		width: sizeInRpx,
		height: sizeInRpx,
		borderRadius: props.shape === 'circle' ? '50%' : '16rpx',
		overflow: 'hidden',
		position: 'relative',
		backgroundColor: '#f5f5f5'
	}
})

// 图片样式
const imageStyle = computed(() => {
	return {
		width: '100%',
		height: '100%'
	}
})

// 图片加载成功
const handleImageLoad = (e) => {
	imageLoaded.value = true
	emit('load', e)
}

// 图片加载失败
const handleImageError = (e) => {
	console.warn('头像加载失败:', displaySrc.value)
	imageLoaded.value = false
	emit('error', e)
}

// 监听 src 变化，重置加载状态
const stopWatch = watch(() => props.src, () => {
	imageLoaded.value = true
}, { immediate: true })

// 组件卸载时停止监听
onUnmounted(() => {
	stopWatch()
})
</script>

<style scoped>
.avatar {
	display: inline-flex;
	align-items: center;
	justify-content: center;
}

.avatar-image {
	width: 100%;
	height: 100%;
	display: block;
}

.avatar-fallback {
	position: absolute;
	top: 0;
	left: 0;
	right: 0;
	bottom: 0;
	display: flex;
	align-items: center;
	justify-content: center;
	background-color: #f5f5f5;
}
</style>