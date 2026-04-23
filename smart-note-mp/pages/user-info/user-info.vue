<template>
	<view class="user-info-page">
		<!-- 头像 -->
		<view class="avatar-section">
			<Avatar 
				:src="userInfo.avatar"
				:size="120"
				shape="circle"
				default-type="user"
				:show-fallback-icon="true"
				fallback-icon="account"
			></Avatar>
			<view class="avatar-edit" @click="changeAvatar">
				<u-icon name="camera" color="#fff" size="28"></u-icon>
			</view>
		</view>

		<!-- 信息列表 -->
		<u-cell-group>
			<u-cell title="昵称" :value="userInfo.name || userInfo.nickname" isLink @click="editField('name')"></u-cell>
			<u-cell title="个性签名" :value="userInfo.signature || '暂无'" isLink @click="editField('signature')"></u-cell>
			<u-cell title="邮箱" :value="userInfo.email || '未设置'" isLink @click="editField('email')"></u-cell>
			<u-cell title="手机号" :value="userInfo.phone || '未设置'" isLink @click="editField('phone')"></u-cell>
			<u-cell title="性别" :value="genderText" isLink @click="editField('gender')"></u-cell>
			<u-cell title="地区" :value="userInfo.city || '未设置'" isLink @click="editField('city')"></u-cell>
			<u-cell title="生日" :value="userInfo.birthday || '未设置'" isLink @click="editField('birthday')"></u-cell>
			<u-cell title="注册时间" :value="userInfo.createTime"></u-cell>
		</u-cell-group>

		<!-- 退出登录按钮 -->
		<view class="logout-btn">
			<u-button type="error" text="退出登录" shape="circle" @click="logout"></u-button>
		</view>
		
		<custom-tab-bar />
		
		<!-- 编辑弹窗（普通文本） -->
		<u-popup :show="showEditPopup && editFieldType !== 'birthday'" @close="showEditPopup = false" mode="center" round="16">
			<view class="edit-popup">
				<view class="edit-title">编辑{{ editFieldName }}</view>
				<input 
					class="edit-input" 
					v-model="editValue" 
					:placeholder="'请输入' + editFieldName"
					:type="editFieldType === 'gender' ? 'text' : 'text'"
				/>
				<view class="edit-buttons">
					<u-button text="取消" @click="showEditPopup = false"></u-button>
					<u-button type="primary" text="保存" @click="saveEdit"></u-button>
				</view>
			</view>
		</u-popup>

		<!-- 生日选择器弹窗 -->
		<u-datetime-picker
			:show="showDatePicker"
			v-model="pickerValue"
			mode="date"
			:minDate="minDate"
			:maxDate="maxDate"
			@confirm="confirmBirthday"
			@cancel="showDatePicker = false"
		></u-datetime-picker>
	</view>
</template>

<script setup>
import { ref, computed } from 'vue'
import { onShow } from '@dcloudio/uni-app'
import CustomTabBar from '@/components/custom-tab-bar/index.vue'
import { userApi, authApi } from '@/api'

const userInfo = ref({
	userId: '',
	avatar: '',
	name: '',
	nickname: '',
	signature: '',
	email: '',
	phone: '',
	gender: 0,
	city: '',
	birthday: '',
	createTime: '',
	registerTime: ''
})

// 加载状态
const loading = ref(false)
// 是否已经请求过后端（避免重复请求）
const hasFetched = ref(false)

// 性别显示
const genderText = computed(() => {
	const gender = userInfo.value.gender
	if (gender === 0) return '未知'
	if (gender === 1) return '男'
	if (gender === 2) return '女'
	return '未设置'
})

// 编辑相关
const showEditPopup = ref(false)
const editFieldType = ref('')
const editFieldName = ref('')
const editValue = ref('')
const currentEditField = ref('')

// 生日选择器相关
const showDatePicker = ref(false)
const pickerValue = ref(0)
const minDate = ref(new Date(1900, 0, 1).getTime())
const maxDate = ref(new Date().getTime())

// 将日期字符串转换为时间戳
const dateToTimestamp = (dateStr) => {
	if (!dateStr || dateStr === '未设置') {
		return new Date().getTime()
	}
	const parts = dateStr.split('-')
	if (parts.length !== 3) return new Date().getTime()
	return new Date(parseInt(parts[0]), parseInt(parts[1]) - 1, parseInt(parts[2])).getTime()
}

// 将时间戳转换为日期字符串
const timestampToDate = (timestamp) => {
	const date = new Date(timestamp)
	const year = date.getFullYear()
	const month = String(date.getMonth() + 1).padStart(2, '0')
	const day = String(date.getDate()).padStart(2, '0')
	return `${year}-${month}-${day}`
}

// 格式化日期时间（用于显示注册时间）
const formatDateTime = (dateTimeStr) => {
	if (!dateTimeStr) return '未设置'
	// 如果是 '2024-01-15T10:30:00' 格式，只取日期部分
	if (dateTimeStr.includes('T')) {
		return dateTimeStr.split('T')[0]
	}
	// 如果是 '2024-01-15 10:30:00' 格式
	if (dateTimeStr.includes(' ')) {
		return dateTimeStr.split(' ')[0]
	}
	return dateTimeStr
}

// 从缓存加载用户信息（同步，立即渲染）
const loadFromCache = () => {
	try {
		const cached = uni.getStorageSync('userInfo')
		if (cached && cached.userId) {
			userInfo.value = cached
			console.log('从缓存加载用户信息成功:', cached)
			return true
		}
		console.log('缓存中无用户信息')
		return false
	} catch (error) {
		console.error('读取缓存失败:', error)
		return false
	}
}

// 从后端获取最新用户信息（异步，静默更新）
const fetchLatestUserInfo = async () => {
	if (loading.value) return
	
	loading.value = true
	
	try {
		console.log('开始请求后端获取最新用户信息...')
		const data = await userApi.getUserInfo()
		console.log('获取最新用户信息成功:', data)
		
		// 构建完整的用户信息对象
		const latestUserInfo = {
			userId: data.userId || '',
			avatar: data.avatar || '',
			name: data.name || '',
			nickname: data.name || data.nickname || '',
			signature: data.signature || '',
			email: data.email || '',
			phone: data.phone || '',
			gender: data.gender ?? 0,
			city: data.city || '',
			birthday: data.birthday || '',
			createTime: formatDateTime(data.createTime),
			registerTime: formatDateTime(data.createTime)
		}
		
		// 检查数据是否有变化，避免不必要的更新
		const hasChanged = JSON.stringify(userInfo.value) !== JSON.stringify(latestUserInfo)
		
		if (hasChanged) {
			// 更新页面数据（触发重新渲染）
			userInfo.value = latestUserInfo
			console.log('用户信息已更新')
			
			// 更新缓存
			uni.setStorageSync('userInfo', latestUserInfo)
			
			// 可选：显示轻提示告知用户数据已更新
			// uni.showToast({ title: '信息已同步', icon: 'none', duration: 1000 })
		} else {
			console.log('用户信息无变化，无需更新')
		}
		
		hasFetched.value = true
		
	} catch (error) {
		console.error('获取最新用户信息失败:', error)
		
		// 如果后端请求失败且缓存也没有，显示错误提示
		const cached = uni.getStorageSync('userInfo')
		if (!cached || !cached.userId) {
			uni.showToast({ title: '加载用户信息失败', icon: 'none' })
		}
	} finally {
		loading.value = false
	}
}

// 页面显示时加载数据
onShow(() => {
	// 1. 先立即从缓存加载并渲染（同步，用户立即看到内容）
	const hasCache = loadFromCache()
	
	if (hasCache) {
		// 有缓存数据：后台静默请求最新数据
		console.log('有缓存数据，后台静默更新中...')
		fetchLatestUserInfo()
	} else {
		// 无缓存数据：直接请求后端（显示加载提示）
		console.log('无缓存数据，直接请求后端...')
		uni.showLoading({ title: '加载中...', mask: true })
		fetchLatestUserInfo().finally(() => {
			uni.hideLoading()
		})
	}
})

// 手动刷新用户信息（用于编辑保存后的回调）
const refreshUserInfo = async () => {
	// 编辑保存后，重新请求最新数据
	await fetchLatestUserInfo()
}

// 确认生日选择
const confirmBirthday = async (e) => {
	const selectedTimestamp = e.value
	const birthday = timestampToDate(selectedTimestamp)
	
	uni.showLoading({ title: '保存中...', mask: true })
	
	try {
		// 调用真实后端接口
		await userApi.updateUserInfo({ birthday })
		
		// 更新本地数据
		userInfo.value.birthday = birthday
		uni.setStorageSync('userInfo', userInfo.value)
		showDatePicker.value = false
		uni.showToast({ title: '保存成功', icon: 'success' })
		
	} catch (error) {
		console.error('保存生日失败:', error)
		uni.showToast({ title: '保存失败', icon: 'none' })
	} finally {
		uni.hideLoading()
	}
}

// 保存编辑（普通字段）
const saveEdit = async () => {
	if (!editValue.value.trim() && currentEditField.value !== 'gender') {
		uni.showToast({ title: '请输入内容', icon: 'none' })
		return
	}
	
	uni.showLoading({ title: '保存中...', mask: true })
	
	try {
		let updateData = {}
		
		if (currentEditField.value === 'gender') {
			let genderValue = 0
			if (editValue.value === '男') genderValue = 1
			else if (editValue.value === '女') genderValue = 2
			updateData.gender = genderValue
		} else if (currentEditField.value === 'name') {
			updateData.name = editValue.value.trim()
		} else {
			updateData[currentEditField.value] = editValue.value.trim()
		}
		
		// 调用真实后端接口
		await userApi.updateUserInfo(updateData)
		
		// 更新本地数据
		if (currentEditField.value === 'gender') {
			userInfo.value.gender = updateData.gender
		} else if (currentEditField.value === 'name') {
			userInfo.value.name = updateData.name
			userInfo.value.nickname = updateData.name
		} else {
			userInfo.value[currentEditField.value] = updateData[currentEditField.value]
		}
		uni.setStorageSync('userInfo', userInfo.value)
		
		showEditPopup.value = false
		uni.showToast({ title: '保存成功', icon: 'success' })
		
	} catch (error) {
		console.error('保存失败:', error)
		uni.showToast({ title: '保存失败', icon: 'none' })
	} finally {
		uni.hideLoading()
	}
}

// 上传头像
const uploadAvatar = async (filePath) => {
	return new Promise((resolve, reject) => {
		const token = uni.getStorageSync('token')
		
		uni.uploadFile({
			url: 'http://192.168.1.5:8080/api/wx/notes/attachment',
			filePath: filePath,
			name: 'file',
			header: {
				'Authorization': token ? 'Bearer ' + token : ''
			},
			success: (res) => {
				try {
					const data = JSON.parse(res.data)
					if (data.code === 200) {
						resolve(data.data)
					} else {
						reject(new Error(data.msg || '上传失败'))
					}
				} catch (e) {
					reject(new Error('解析响应失败'))
				}
			},
			fail: (err) => {
				reject(err)
			}
		})
	})
}

// 更换头像
const changeAvatar = () => {
	uni.chooseImage({
		count: 1,
		sizeType: ['compressed'],
		sourceType: ['album', 'camera'],
		success: async (res) => {
			const tempFilePath = res.tempFilePaths[0]
			uni.showLoading({ title: '上传中...', mask: true })
			
			try {
				// 上传图片
				const uploadRes = await uploadAvatar(tempFilePath)
				const avatarUrl = uploadRes.url || uploadRes
				
				// 更新用户头像
				await userApi.updateUserInfo({ avatar: avatarUrl })
				
				// 更新本地数据
				userInfo.value.avatar = avatarUrl
				uni.setStorageSync('userInfo', userInfo.value)
				
				uni.showToast({ title: '头像更新成功', icon: 'success' })
			} catch (error) {
				console.error('头像上传失败:', error)
				uni.showToast({ title: '头像上传失败', icon: 'none' })
			} finally {
				uni.hideLoading()
			}
		}
	})
}

// 退出登录
const logout = () => {
	uni.showModal({
		title: '提示',
		content: '确定要退出登录吗？',
		confirmText: '退出',
		confirmColor: '#ff4d4f',
		success: async (res) => {
			if (res.confirm) {
				uni.showLoading({ title: '正在退出...', mask: true })
				
				// 调用后端退出接口
				try {
					await authApi.logout()
					console.log('后端退出成功')
				} catch (error) {
					console.error('后端退出失败:', error)
				}
				
				// 清除所有本地存储
				uni.clearStorageSync()
				
				// 跳转到登录页
				uni.reLaunch({ url: '/pages/login/login' })
				
				uni.hideLoading()
				uni.showToast({ title: '已退出登录', icon: 'success' })
			}
		}
	})
}

// 暴露刷新方法（供其他组件调用）
defineExpose({
	refreshUserInfo
})
</script>

<style scoped>
.user-info-page {
	min-height: 100vh;
	background: #f5f7f9;
	padding-bottom: calc(160rpx + env(safe-area-inset-bottom));
}

.avatar-section {
	position: relative;
	display: flex;
	justify-content: center;
	padding: 60rpx 0;
	background: #fff;
	margin-bottom: 20rpx;
}

.avatar-edit {
	position: absolute;
	bottom: 50rpx;
	right: calc(50% - 70rpx);
	background: rgba(0, 0, 0, 0.6);
	width: 56rpx;
	height: 56rpx;
	border-radius: 50%;
	display: flex;
	align-items: center;
	justify-content: center;
}

.logout-btn {
	padding: 60rpx 30rpx;
}

.edit-popup {
	width: 560rpx;
	padding: 48rpx 32rpx;
	background: #fff;
	border-radius: 32rpx;
}

.edit-title {
	font-size: 36rpx;
	font-weight: bold;
	color: #333;
	text-align: center;
	margin-bottom: 32rpx;
}

.edit-input {
	width: 100%;
	height: 88rpx;
	border: 2rpx solid #e0e0e0;
	border-radius: 16rpx;
	padding: 0 24rpx;
	font-size: 28rpx;
	box-sizing: border-box;
	margin-bottom: 32rpx;
}

.edit-buttons {
	display: flex;
	gap: 24rpx;
}

.edit-buttons button {
	flex: 1;
}
</style>