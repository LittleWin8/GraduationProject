# 📋 Day 6 任务清单：小程序体验优化

## 任务概览

| 序号 |           任务           | 优先级 |                 说明                 |
| :--: | :----------------------: | :----: | :----------------------------------: |
|  1   | Token 过期自动跳转登录页 |  🔴 高  | request.js 已有 401 处理但需优化体验 |
|  2   |   全局加载态 + 骨架屏    |  🔴 高  |  社区页/详情页/个人中心首屏加载体验  |
|  3   |     下拉刷新统一优化     |  🔴 高  |     部分 TabBar 页未配置下拉刷新     |
|  4   |      空状态统一优化      |  🟡 中  |     各列表页空状态图标和文案统一     |
|  5   | 页面切换动画 + 交互细节  |  🟡 中  |    防重复提交、操作反馈、返回刷新    |

   

## 当前已有基础
- `request.js `：已有 401 拦截，清除 token + reLaunch('/pages/login/login')
- `pages.json `：community 和 recycle-bin 已配置` enablePullDownRefresh`
- 各列表页已有 `u-empty` 空状态组件
- 各列表页已有上拉加载更多逻辑
- 登录页已有完整的微信登录 + 新用户授权流程

## 需要优化的问题清单

| 序号 |              问题               |           当前状态            |                      目标                      |
| :--: | :-----------------------------: | :---------------------------: | :--------------------------------------------: |
|  1   | 401 跳转登录页后无法返回原页面  |      reLaunch 清空页面栈      |           改为存储来源页，登录后跳回           |
|  2   |  社区页首次加载无骨架屏，白屏   |         直接显示空白          |                  加骨架屏占位                  |
|  3   |       详情页加载无过渡态        | 有 loading-spinner 但样式简陋 |                  优化为骨架屏                  |
|  4   |    个人中心页未配置下拉刷新     |     无 onPullDownRefresh      | 已有但 pages.json 未配置 enablePullDownRefresh |
|  5   | 笔记列表页(note-list)无下拉刷新 |  scroll-view 自带 refresher   |                 已有，无需改动                 |
|  6   |        消息页无下拉刷新         |              无               |                  添加下拉刷新                  |
|  7   |         发布页重复提交          |     已有 submitted 防重复     |                已处理，无需改动                |
|  8   |          点赞/收藏防抖          |     已有 500ms Set 防重复     |                已处理，无需改动                |



# 📝 Day 6 提示词
## 提示词 1：Token 过期跳转优化 + 登录后返回

```
在 smart-note-mp 小程序中，优化 Token 过期后的跳转体验，实现登录后自动返回原页面。

当前代码状态：
- 请求封装：api/request.js（401 时 uni.reLaunch('/pages/login/login')，清空页面栈）
- 登录页：pages/login/login.vue（登录成功后 uni.reLaunch('/pages/community/community')，固定跳社区）
- 登录成功回调：handleLoginSuccess 中 setTimeout → uni.reLaunch

需要修改：

1. api/request.js — 401 拦截优化
   - 401 时，先获取当前页面路径和参数
   - 将来源页信息存入 uni.setStorageSync('redirectUrl', fullPath)
   - fullPath 格式如：/pages/note-detail/note-detail?id=123
   - 然后执行 uni.reLaunch({ url: '/pages/login/login' })
   - 添加防抖：如果已经在登录页，不再重复跳转

2. pages/login/login.vue — 登录成功后跳转优化
   - 在 handleLoginSuccess 中，登录成功后检查 uni.getStorageSync('redirectUrl')
   - 如果有 redirectUrl，跳转到该页面，并清除缓存
   - 如果没有 redirectUrl，保持原有逻辑跳转社区页
   - 跳转方式：uni.reLaunch({ url: redirectUrl })

   修改位置：handleLoginSuccess 函数中 setTimeout 回调
   原代码：uni.reLaunch({ url: '/pages/community/community' })
   改为：
   const redirectUrl = uni.getStorageSync('redirectUrl') || '/pages/community/community'
   uni.removeStorageSync('redirectUrl')
   uni.reLaunch({ url: redirectUrl })

3. pages.json — 登录页不需要改动（已是 custom navigationStyle）
```

## 提示词 2：社区页骨架屏

```
在 smart-note-mp 小程序的社区页中，添加首次加载骨架屏效果。

当前代码状态：
- 社区页：pages/community/community.vue
- 首次加载时 noteList 为空，直接显示 u-loadmore 或空白
- 使用 uview-plus 组件库

需要修改 community.vue：

1. 新增状态变量
   const firstLoading = ref(true)  // 首次加载标记

2. 在 loadNotes 方法中：
   - 请求完成后设置 firstLoading.value = false
   - 只在首次加载时为 true，后续刷新/加载更多不触发

3. 模板中添加骨架屏（在 list-body 区域，noteList 渲染之前）
   <view v-if="firstLoading" class="skeleton-wrapper">
     <!-- 模拟笔记卡片骨架 -->
     <view v-for="i in 4" :key="i" class="skeleton-card">
       <view class="skeleton-avatar"></view>
       <view class="skeleton-content">
         <view class="skeleton-title"></view>
         <view class="skeleton-text"></view>
         <view class="skeleton-text short"></view>
       </view>
     </view>
   </view>
   <view v-else class="list-body">
     <!-- 原有 note-card 列表 -->
   </view>

4. 骨架屏样式（添加到 <style> 中）
   .skeleton-wrapper { padding: 20rpx; }
   .skeleton-card {
     display: flex; padding: 24rpx; background: #fff;
     margin-bottom: 16rpx; border-radius: 12rpx;
   }
   .skeleton-avatar {
     width: 72rpx; height: 72rpx; border-radius: 50%;
     background: linear-gradient(90deg, #f0f0f0 25%, #e8e8e8 50%, #f0f0f0 75%);
     background-size: 200% 100%; animation: skeleton-pulse 1.5s infinite;
     flex-shrink: 0;
   }
   .skeleton-content { flex: 1; margin-left: 20rpx; }
   .skeleton-title {
     height: 32rpx; width: 60%; border-radius: 8rpx;
     background: linear-gradient(90deg, #f0f0f0 25%, #e8e8e8 50%, #f0f0f0 75%);
     background-size: 200% 100%; animation: skeleton-pulse 1.5s infinite;
     margin-bottom: 16rpx;
   }
   .skeleton-text {
     height: 24rpx; width: 90%; border-radius: 6rpx;
     background: linear-gradient(90deg, #f0f0f0 25%, #e8e8e8 50%, #f0f0f0 75%);
     background-size: 200% 100%; animation: skeleton-pulse 1.5s infinite;
     margin-bottom: 12rpx;
   }
   .skeleton-text.short { width: 50%; }
   @keyframes skeleton-pulse {
     0% { background-position: 200% 0; }
     100% { background-position: -200% 0; }
   }
```

## 提示词 3：笔记详情页骨架屏

```
在 smart-note-mp 小程序的笔记详情页中，优化加载过渡态为骨架屏。

当前代码状态：
- 详情页：pages/note-detail/note-detail.vue
- 已有 loading 状态，loading 时显示 loading-spinner
- 已有空状态（笔记不存在）

需要修改 note-detail.vue：

1. 将 loading 区域的 loading-spinner 替换为骨架屏
   <view v-if="loading" class="skeleton-wrapper">
     <view class="skeleton-title"></view>
     <view class="skeleton-meta"></view>
     <view class="skeleton-line"></view>
     <view class="skeleton-line"></view>
     <view class="skeleton-line short"></view>
     <view class="skeleton-line"></view>
     <view class="skeleton-line short"></view>
   </view>

2. 骨架屏样式
   .skeleton-wrapper { padding: 40rpx; }
   .skeleton-title {
     height: 44rpx; width: 70%; border-radius: 8rpx; margin-bottom: 24rpx;
     background: linear-gradient(90deg, #f0f0f0 25%, #e8e8e8 50%, #f0f0f0 75%);
     background-size: 200% 100%; animation: skeleton-pulse 1.5s infinite;
   }
   .skeleton-meta {
     height: 24rpx; width: 40%; border-radius: 6rpx; margin-bottom: 40rpx;
     background: linear-gradient(90deg, #f0f0f0 25%, #e8e8e8 50%, #f0f0f0 75%);
     background-size: 200% 100%; animation: skeleton-pulse 1.5s infinite;
   }
   .skeleton-line {
     height: 28rpx; width: 100%; border-radius: 6rpx; margin-bottom: 20rpx;
     background: linear-gradient(90deg, #f0f0f0 25%, #e8e8e8 50%, #f0f0f0 75%);
     background-size: 200% 100%; animation: skeleton-pulse 1.5s infinite;
   }
   .skeleton-line.short { width: 60%; }
   @keyframes skeleton-pulse {
     0% { background-position: 200% 0; }
     100% { background-position: -200% 0; }
   }
```

## 提示词 4：个人中心 + 消息页下拉刷新配置

```
在 smart-note-mp 小程序中，为个人中心页和消息页添加下拉刷新支持。

当前代码状态：
- 个人中心：pages/profile/profile.vue（已有 onPullDownRefresh 处理函数）
- 消息页：pages/message/message.vue（无下拉刷新）
- pages.json：profile 和 message 未配置 enablePullDownRefresh

需要修改：

1. pages.json — 添加下拉刷新配置
   profile 页面 style 中添加："enablePullDownRefresh": true
   message 页面 style 中添加："enablePullDownRefresh": true

2. pages/message/message.vue — 添加下拉刷新处理
   import 中添加 onPullDownRefresh：
   import { onShow, onReachBottom, onPullDownRefresh } from '@dcloudio/uni-app'

   添加 onPullDownRefresh 钩子：
   onPullDownRefresh(async () => {
     await loadMessages(true)
     uni.stopPullDownRefresh()
   })

3. pages/profile/profile.vue — 确认已有 onPullDownRefresh（已有，无需改动）
```

## 提示词 5：空状态统一优化

```
在 smart-note-mp 小程序中，统一各列表页的空状态展示。

当前代码状态：
- 社区页：无独立空状态处理（u-loadmore 的 nomore 状态即为止）
- 个人中心三个 Tab：已有 u-empty mode="list"
- 笔记列表页：已有 u-empty mode="list"
- 回收站：已有 u-empty mode="data"
- 消息页：已有 u-empty mode="list"

需要修改：

1. pages/community/community.vue — 添加社区空状态
   在 list-body 中，noteList 为空且非首次加载时显示空状态：
   <u-empty v-if="!firstLoading && noteList.length === 0" mode="search" text="没有找到相关笔记" marginTop="100"></u-empty>

   注意：firstLoading 是提示词 2 中新增的变量，如果提示词 2 未执行，用 !loading 替代

2. 各页面空状态文案统一规范：
   - 社区页无结果：mode="search"，text="没有找到相关笔记"
   - 个人中心-我的笔记：mode="list"，text="暂无笔记，去社区看看吧"
   - 个人中心-收藏：mode="list"，text="暂无收藏"
   - 个人中心-赞过：mode="list"，text="暂无赞过"
   - 笔记列表页：mode="list"，text="暂无数据"
   - 回收站：mode="data"，text="回收站是空的"
   - 消息页：mode="list"，text="暂无消息"

   以上大部分已实现，只需确认文案一致即可，重点改社区页。
```

## 提示词 6：交互细节优化（防抖 + 反馈 + 返回刷新）

```
在 smart-note-mp 小程序中，优化若干交互细节。

需要修改：

1. pages/note-detail/note-detail.vue — 评论发送后滚动到评论区
   在评论发送成功的回调中，添加滚动到评论区的逻辑：
   uni.pageScrollTo({ selector: '.comment-section', duration: 300 })

2. pages/community/community.vue — onShow 刷新逻辑优化
   当前每次 onShow 都 loadNotes(true)，从其他页面返回时会重新加载整个列表
   优化：只在特定场景刷新（如从发布页返回）
   - 使用 uni.$emit / uni.$on 事件通知
   - create.vue 中发布成功后 uni.$emit('noteCreated')
   - community.vue 中 onShow 改为：
     onShow(() => {
       if (categoryTree.value.length === 0) loadCategories()
       // 只在首次或收到刷新事件时加载
     })
   - onMounted 中注册 uni.$on('noteCreated', () => loadNotes(true))
   - onUnmounted 中移除 uni.$off('noteCreated')

   ⚠️ 这个优化是可选的，如果觉得每次 onShow 刷新体验也可以接受，可以跳过

3. pages/note-detail/note-detail.vue — 删除笔记后返回并刷新
   删除笔记成功后，navigateBack 之前发送事件通知列表刷新：
   uni.$emit('noteUpdated')
   uni.navigateBack()

4. pages/profile/profile.vue — 从详情页返回后统计数据可能变化
   当前 onShow 已有 fetchStats()，确认返回后刷新正常即可，无需改动
```

# ⏱️ Day 6 执行顺序


| 顺序 |            提示词            |  前置依赖  |
| :--: | :--------------------------: | :--------: |
|  1️⃣   | 提示词 1：Token 过期跳转优化 |     无     |
|  2️⃣   |    提示词 2：社区页骨架屏    |     无     |
|  3️⃣   |    提示词 3：详情页骨架屏    |     无     |
|  4️⃣   |    提示词 4：下拉刷新配置    |     无     |
|  5️⃣   |     提示词 5：空状态统一     |  提示词 2  |
|  6️⃣   |    提示词 6：交互细节优化    | 提示词 2+3 |

> 提示词 1-4 互相独立，可以并行执行。提示词 5-6 依赖前面的骨架屏变量。提示词 6 中的事件通知优化是可选的，核心是提示词 1-4。
