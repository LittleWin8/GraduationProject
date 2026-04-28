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
		<view v-if="showFallbackIcon && !imageLoaded" class="avatar-fallback">
			<u-icon :name="fallbackIcon" :color="iconColor" :size="iconSize"></u-icon>
		</view>
	</view>
</template>

<script setup>
import { computed, ref, watch, onUnmounted } from 'vue'
import { config } from '@/api/config.js'

const props = defineProps({
	src: {
		type: String,
		default: ''
	},
	size: {
		type: [Number, String],
		default: 100
	},
	shape: {
		type: String,
		default: 'circle'
	},
	mode: {
		type: String,
		default: 'aspectFill'
	},
	defaultType: {
		type: String,
		default: 'default'
	},
	customDefault: {
		type: String,
		default: ''
	},
	showFallbackIcon: {
		type: Boolean,
		default: true
	},
	fallbackIcon: {
		type: String,
		default: 'account'
	},
	iconColor: {
		type: String,
		default: '#999'
	},
	iconSize: {
		type: [Number, String],
		default: 40
	}
})

const emit = defineEmits(['load', 'error'])

const imageLoaded = ref(true)

const DEFAULT_AVATAR = '/static/default-avatar.gif'

const resolveAvatarUrl = (avatar) => {
	if (!avatar || avatar.trim() === '') return ''
	if (avatar.startsWith('http://') || avatar.startsWith('https://') || avatar.startsWith('data:')) return avatar
	if (avatar.startsWith('/static/')) return avatar
	if (avatar.startsWith('/api/')) return config.baseURL + avatar
	return config.baseURL + '/api/wx/user/files' + avatar
}

const displaySrc = computed(() => {
	if (props.customDefault && props.customDefault.trim() !== '') {
		return props.customDefault
	}
	
	if (props.src && props.src.trim() !== '') {
		return resolveAvatarUrl(props.src)
	}
	
	return DEFAULT_AVATAR
})

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

const imageStyle = computed(() => {
	return {
		width: '100%',
		height: '100%'
	}
})

const handleImageLoad = (e) => {
	imageLoaded.value = true
	emit('load', e)
}

const handleImageError = (e) => {
	console.warn('头像加载失败:', displaySrc.value)
	imageLoaded.value = false
	emit('error', e)
}

const stopWatch = watch(() => props.src, () => {
	imageLoaded.value = true
}, { immediate: true })

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
