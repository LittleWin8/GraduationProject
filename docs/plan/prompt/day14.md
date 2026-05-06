# 📋 Day 14 任务清单：小程序优化

## 任务概览

| 序号 | 任务 | 优先级 | 说明 |
|:--:|:---|:--:|:---|
| 1 | request.js 401 去重 | 🔴 高 | 提取 handleUnauthorized() 公共函数 |
| 2 | 环境配置优化 | 🟡 中 | config.js 区分 dev/prod 环境 baseURL |
| 3 | MarkdownIt 全局复用 | 🟡 中 | 提取到 utils/markdown.js 单例 |
| 4 | 分包加载优化 | 🟡 中 | 低频页面放入分包 |
| 5 | Web 头像上传接口补全 | 🔴 高 | 新增管理端 /api/file/upload/img 接口 |

## 当前已有基础

- `request.js`：401 处理逻辑重复两处（result.code===401 和 statusCode===401），代码完全相同
- `config.js`：已有 dev/prod 环境结构，但 prod baseURL 为空字符串
- `MarkdownIt`：note-detail.vue 和 create.vue 各自 import 并 new 一次，未复用
- `pages.json`：所有页面在主包，无分包配置

---

# 📝 Day 14 提示词

## 提示词 1：request.js 401 去重

```
重构 smart-note-mp 的 request.js，提取重复的 401 处理逻辑为公共函数。

⚠️ 问题描述：
request.js 中 401 处理逻辑出现两次（约 L48-L70 和 L77-L99），代码完全相同：
- 清除 token/userInfo
- 记录来源页到 redirectUrl
- 防抖判断（已在登录页则不再跳转）
- 跳转到登录页

### 修改文件：api/request.js

(1) 在文件顶部（import 语句之后）提取公共函数：

const handleUnauthorized = (msg) => {
  // 清除本地存储
  uni.removeStorageSync('token')
  uni.removeStorageSync('userInfo')
  // 记录来源页，登录后自动跳回
  const pages = getCurrentPages()
  if (pages.length > 0) {
    const currentPage = pages[pages.length - 1]
    const path = '/' + currentPage.route
    const params = currentPage.options || {}
    const query = Object.keys(params).map(k => `${k}=${params[k]}`).join('&')
    const fullPath = query ? path + '?' + query : path
    if (!currentPage.route || !currentPage.route.includes('login/login')) {
      uni.setStorageSync('redirectUrl', fullPath)
    }
  }
  // 防抖：已在登录页则不再跳转
  const curPages = getCurrentPages()
  if (curPages.length > 0 && curPages[curPages.length - 1].route && curPages[curPages.length - 1].route.includes('login/login')) {
    return
  }
  uni.reLaunch({ url: '/pages/login/login' })
}

(2) 将两处 401 处理替换为调用公共函数：

第一处（result.code === 401 时）：
handleUnauthorized(result.msg || '登录已过期')
reject(new Error(result.msg || '登录已过期'))

第二处（statusCode === 401 时）：
handleUnauthorized('登录已过期')
reject(new Error('登录已过期'))

验证：
1. 401 处理逻辑只在 handleUnauthorized 函数中写一次
2. 两处 401 分支都调用 handleUnauthorized
3. 功能不变：清除存储 → 记录来源页 → 防抖 → 跳转登录页
```

---

## 提示词 2：环境配置优化

```
优化 smart-note-mp 的 config.js，使环境切换更清晰。

⚠️ 当前状态：已有 dev/prod 结构，但 prod baseURL 为空字符串，切换方式不明显。

### 修改文件：api/config.js

将环境配置改为：

// 环境配置
const ENV = {
  dev: {
    baseURL: 'http://10.152.38.241:8080'
  },
  prod: {
    baseURL: 'https://your-domain.com'  // ⚠️ 替换为实际域名
  }
}

// 切换环境：改这里即可
const currentEnv = 'dev'  // 'dev' 或 'prod'

export const config = {
  baseURL: ENV[currentEnv].baseURL,
  timeout: 15000
}

⚠️ 注意：
- 开发时 currentEnv = 'dev'
- 打包上线时改为 'prod'
- prod 的 baseURL 需要替换为真实服务器域名
- 不需要引入 process.env 等 Node.js 概念（小程序不支持）

验证：
1. 切换 currentEnv 为 'dev'/'prod'，请求地址正确变化
2. 小程序正常运行
```

---

## 提示词 3：MarkdownIt 全局复用

```
将 MarkdownIt 提取为全局单例，避免多个页面重复创建实例。

⚠️ 当前状态：
- note-detail.vue：import MarkdownIt + new MarkdownIt(...)
- create.vue：import MarkdownIt + new MarkdownIt(...)
- 配置完全相同：{ html: false, linkify: true, breaks: true }

### 步骤 1：新建 utils/markdown.js

在 smart-note-mp/utils/ 目录下新建 markdown.js：

import MarkdownIt from 'markdown-it'

const md = new MarkdownIt({
  html: false,
  linkify: true,
  breaks: true
})

export default md

### 步骤 2：修改 note-detail.vue

将：
import MarkdownIt from 'markdown-it'
// ...
const md = new MarkdownIt({
  html: false,
  linkify: true,
  breaks: true
})

改为：
import md from '@/utils/markdown.js'

删除原来的 new MarkdownIt(...) 代码。

### 步骤 3：修改 create.vue

同样替换：
import MarkdownIt from 'markdown-it'
// ...
const md = new MarkdownIt({ html: false, linkify: true, breaks: true });

改为：
import md from '@/utils/markdown.js'

删除原来的 new MarkdownIt(...) 代码。

验证：
1. 笔记详情页 Markdown 渲染正常
2. 创建笔记页 Markdown 预览正常
3. 只有 utils/markdown.js 一处 import MarkdownIt
```

---

## 提示词 4：分包加载优化

```
将低频页面移入分包，减小主包体积，提升小程序启动速度。

⚠️ 分包策略：
- 主包：登录、社区、个人中心、笔记详情（高频 + tabBar 页面）
- 分包 subNote：创建笔记、笔记列表、标签管理、标签笔记（笔记相关操作）
- 分包 subTools：回收站、用户信息编辑（低频工具页面）

⚠️ 限制：tabBar 页面（community、profile）必须在主包。

### 修改文件：pages.json

将 pages 数组拆分为主包 + 分包：

{
  "easycom": { ... },  // 保持不变
  "pages": [
    { "path": "pages/login/login", "style": { "navigationBarTitleText": "登录", "navigationStyle": "custom" } },
    { "path": "pages/community/community", "style": { "navigationBarTitleText": "笔记社区", "enablePullDownRefresh": true } },
    { "path": "pages/profile/profile", "style": { "navigationBarTitleText": "个人中心", "enablePullDownRefresh": true } },
    { "path": "pages/note-detail/note-detail", "style": { "navigationBarTitleText": "笔记详情" } },
    { "path": "pages/message/message", "style": { "navigationBarTitleText": "消息", "enablePullDownRefresh": true } }
  ],
  "subPackages": [
    {
      "root": "pages/subNote",
      "pages": [
        { "path": "create/create", "style": { "navigationBarTitleText": "发布笔记" } },
        { "path": "note-list/note-list", "style": { "navigationBarTitleText": "" } },
        { "path": "tag-manage/tag-manage", "style": { "navigationBarTitleText": "我的标签" } },
        { "path": "tag-notes/tag-notes", "style": { "navigationBarTitleText": "标签笔记" } }
      ]
    },
    {
      "root": "pages/subTools",
      "pages": [
        { "path": "recycle-bin/recycle-bin", "style": { "navigationBarTitleText": "回收站", "enablePullDownRefresh": true } },
        { "path": "user-info/user-info", "style": { "navigationBarTitleText": "" } }
      ]
    }
  ],
  "globalStyle": { ... },  // 保持不变
  "tabBar": { ... }        // 保持不变
}

### 移动文件

(1) 创建分包目录：
mkdir -p pages/subNote/create
mkdir -p pages/subNote/note-list
mkdir -p pages/subNote/tag-manage
mkdir -p pages/subNote/tag-notes
mkdir -p pages/subTools/recycle-bin
mkdir -p pages/subTools/user-info

(2) 移动页面文件：
mv pages/create/* pages/subNote/create/
mv pages/note-list/* pages/subNote/note-list/
mv pages/tag-manage/* pages/subNote/tag-manage/
mv pages/tag-notes/* pages/subNote/tag-notes/
mv pages/recycle-bin/* pages/subTools/recycle-bin/
mv pages/user-info/* pages/subTools/user-info/

(3) 删除旧的空目录：
rmdir pages/create pages/note-list pages/tag-manage pages/tag-notes pages/recycle-bin pages/user-info

### 更新页面跳转路径

搜索整个项目中所有引用已移动页面路径的地方，更新为新路径：

涉及的路径变更：
- pages/create/create → pages/subNote/create/create
- pages/note-list/note-list → pages/subNote/note-list/note-list
- pages/tag-manage/tag-manage → pages/subNote/tag-manage/tag-manage
- pages/tag-notes/tag-notes → pages/subNote/tag-notes/tag-notes
- pages/recycle-bin/recycle-bin → pages/subTools/recycle-bin/recycle-bin
- pages/user-info/user-info → pages/subTools/user-info/user-info

需要搜索的文件：
- 所有 .vue 文件中的 uni.navigateTo / uni.redirectTo / uni.reLaunch 调用
- router/index.js 中硬编码的页面路径（create、tag-manage、user-info、tag-notes）
- components/ 目录下的文件（如 custom-tab-bar/index.vue 中的 pagePath 跳转路径）
- pages.json 中已更新（上面已完成）
- tabBar 配置不受影响（community 和 profile 未移动）

⚠️ 注意：
- 分包页面之间的跳转不需要额外配置
- 主包跳转分包页面，路径直接写完整路径即可（uni-app 自动处理）
- 分包页面中的 import 路径（如 @/api/xxx）不需要改（@ 指向项目根目录）
- router/index.js 中 4 个路径需要更新：
  /pages/create/create → /pages/subNote/create/create
  /pages/tag-manage/tag-manage → /pages/subNote/tag-manage/tag-manage
  /pages/user-info/user-info → /pages/subTools/user-info/user-info
  /pages/tag-notes/tag-notes → /pages/subNote/tag-notes/tag-notes
- custom-tab-bar/index.vue 中 pagePath 也需要更新：
  /pages/create/create → /pages/subNote/create/create
- 文件移动优先用 shell 命令（mv），如环境不支持则逐个读取+写入新路径+删除旧文件

验证：
1. 编译无报错
2. 主包页面正常打开（社区、个人中心、笔记详情、消息）
3. 分包页面正常打开（创建笔记、笔记列表、标签管理、回收站、用户信息）
4. 页面间跳转正常
5. 主包体积减小（编译后查看 dist 目录）
```

---

# ⏱️ Day 14 执行顺序

| 顺序 | 提示词 | 前置依赖 | 说明 |
|:--:|:---|:--:|:---|
| 1️⃣ | 提示词 1：request.js 401 去重 | 无 | 代码重构 |
| 2️⃣ | 提示词 2：环境配置优化 | 无 | 配置调整 |
| 3️⃣ | 提示词 3：MarkdownIt 全局复用 | 无 | 提取公共模块 |
| 4️⃣ | 提示词 4：分包加载优化 | 无 | 页面迁移 |
| 5️⃣ | 提示词 5：Web 头像上传接口补全 | 无 | 后端新增接口 |

> 5 个提示词互相独立，可按任意顺序执行。

---

# 🔍 Day 14 涉及的文件清单

| 文件 | 改动类型 | 说明 |
|:---|:---|:---|
| api/request.js | 修改 | 提取 handleUnauthorized() |
| api/config.js | 修改 | 环境配置优化 |
| utils/markdown.js | 新建 | MarkdownIt 单例 |
| pages/note-detail/note-detail.vue | 修改 | 改用全局 md |
| pages/create/create.vue | 修改 | 改用全局 md |
| pages.json | 修改 | 分包配置 |
| router/index.js | 修改 | 更新 4 个页面路径 |
| components/custom-tab-bar/index.vue | 修改 | 更新 create 页面路径 |
| pages/subNote/* | 移动 | 创建/列表/标签管理/标签笔记 |
| pages/subTools/* | 移动 | 回收站/用户信息 |
| 各页面跳转路径 | 修改 | 更新 navigateTo 路径 |
| AdminFileController.java | 新建 | 管理端文件上传接口（/api/file/upload/img） |

---

## 提示词 5：Web 端头像上传接口补全

```
Web 端个人中心和用户管理页面有头像上传功能，但后端缺少对应的上传接口，前后端路径都需要修正。

⚠️ 问题描述：
- Web 端 UploadImg 组件调用 POST /api/file/upload/img（upload.ts 中 PORT1 + '/file/upload/img'）
- 后端没有 /api/file/upload/img 接口，Web 端头像上传不工作
- 小程序端已有 POST /api/wx/user/avatar（WxUserController），返回 { url, fileName, originalName, size }
- Web 端 UploadImg 期望返回 { fileUrl: "..." }

⚠️ 修复方案：
1. 后端：新增管理端文件上传 Controller，复用 FileUploadUtils + uploadConfig
2. 前端：upload.ts 路径改为管理端统一路径 /api/admin/file/upload/img

### 步骤 1：新建 AdminFileController.java（com.littlewin.system.controller）

参考 WxUserController 的 uploadAvatar 逻辑：

@RestController
@RequestMapping("/api/admin/file")
public class AdminFileController {

    @Resource
    private UploadConfig uploadConfig;

    @PostMapping("/upload/img")
    @Log(module = LogModule.USER, action = LogAction.CREATE, desc = "上传图片")
    public Result<Map<String, String>> uploadImg(@RequestParam("file") MultipartFile file) {
        // 1. 校验文件
        if (file == null || file.isEmpty()) {
            throw new ServiceException("请选择要上传的文件");
        }
        if (file.getSize() > uploadConfig.getMaxAvatarSize()) {
            throw new ServiceException("图片大小不能超过 " + (uploadConfig.getMaxAvatarSize() / 1024 / 1024) + "MB");
        }

        String originalName = file.getOriginalFilename() == null ? "image" : file.getOriginalFilename();
        String suffix = FileUploadUtils.getFileSuffix(originalName).toLowerCase();
        if (!uploadConfig.getAllowedImageSuffixes().contains(suffix)) {
            throw new ServiceException("仅支持 " + String.join("、", uploadConfig.getAllowedImageSuffixes()) + " 图片格式");
        }

        // 2. 文件头校验
        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new ServiceException("非法文件类型");
        }

        // 3. 保存文件（复用小程序的 FileUploadUtils）
        String relativePath = FileUploadUtils.upload(
                file,
                uploadConfig.getLocalRootPath(),
                uploadConfig.getUserAvatarPath(),
                "web"
        );
        String url = "/api/wx/user/files" + relativePath;

        // 4. 返回 UploadImg 组件期望的格式
        return Result.success(Map.of("fileUrl", url));
    }
}

⚠️ 注意：
- 路径 /api/admin/file/upload/img（与其他管理端接口风格一致）
- 返回格式 { fileUrl: "..." } 匹配 UploadImg 组件的 data.fileUrl
- 文件存储复用小程序的 uploadConfig.getUserAvatarPath()，统一目录
- 文件访问复用小程序的 /api/wx/user/files/ 前缀（getLocalFile 接口已有）
- import：FileUploadUtils（common 模块）、UploadConfig、ServiceException、LogModule、LogAction

### 步骤 2：修改前端 upload.ts — 路径对齐

将：
export const uploadImg = (params: FormData) => {
  return http.post<Upload.ResFileUrl>(PORT1 + `/file/upload/img`, params, { cancel: false });
};

改为：
export const uploadImg = (params: FormData) => {
  return http.post<Upload.ResFileUrl>(`/admin/file/upload/img`, params, { cancel: false });
};

⚠️ 注意：
- 去掉 PORT1 前缀（http 模块已自动拼接 /api 前缀）
- 路径改为 /admin/file/upload/img，与后端 AdminFileController 的 @RequestMapping 一致

### 步骤 3：确认 UploadConfig 已注入

检查 UploadConfig 类是否有 @Component 或 @Configuration 注解。如果没有，在 AdminFileController 中改用 @Value 注入配置值。

### 步骤 4：确认 SecurityConfig 放行

管理端接口需要认证即可访问，不需要额外放行。确认 /api/admin/** 路径在 SecurityConfig 中已被认证用户访问。

验证：
1. 打开 Web 端个人中心 → 编辑个人资料 → 点击头像 → 上传成功，头像显示
2. 打开用户管理 → 新增/编辑用户 → 上传头像 → 上传成功
3. 上传的图片文件存在于服务器 upload 目录
4. 上传的图片通过 /api/wx/user/files/... URL 可访问
5. 超过大小限制 → 提示"图片大小不能超过 XMB"
6. 非图片文件 → 提示"非法文件类型"
```
