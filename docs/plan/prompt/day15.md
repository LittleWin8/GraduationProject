# 📋 Day 15 任务清单：AI 笔记助手 + 标签推荐

## 任务概览

| 序号 | 任务 | 优先级 | 说明 |
|:--:|:---|:--:|:---|
| 1 | 后端 AI 笔记助手接口 | 🔴 高 | 扩写/润色/总结，复用已有 AI 基础设施 |
| 2 | 后端标签推荐接口 | 🔴 高 | LLM 从已有标签中匹配推荐 |
| 3 | 小程序笔记助手 UI | 🔴 高 | create.vue 加 AI 按钮组 |
| 4 | 小程序标签推荐 UI | 🟡 中 | create.vue 推荐标签一键添加 |

## 当前已有基础

- `AiSummaryServiceImpl`：LangChain4j + DeepSeek 调用链路已通（@PostConstruct 初始化、ChatRequest、TokenUsage）
- `AiQuotaService`：配额校验 + recordUsage 已有
- `WxAiController`：已有 /summary、/quota 接口
- `create.vue`：已有标签选择（myTags + selectedTagIds）、分类选择、Markdown 编辑
- `tag.js`：已有 getMyTags() 方法
- `note.js`：已有 getAiSummary / generateAiSummary / getAiQuota

---

# 📝 Day 15 提示词

## 提示词 1：后端 AI 笔记助手 + 标签推荐接口

```
在 smart-note-system 中，新增 AI 笔记助手和标签推荐两个接口，复用已有的 AI 基础设施。

⚠️ 设计决策：
- 复用 AiQuotaService 配额校验 + recordUsage 日志记录
- 复用 chatModel（@PostConstruct 初始化的 OpenAiChatModel）
- 笔记助手支持 3 种操作：expand（扩写）、polish（润色）、summarize（总结）
- 标签推荐：从用户已有标签中匹配，不创建新标签
- 不需要新建 Service 接口，直接在 WxAiController 中注入已有依赖

### 步骤 1：新建 AiAssistService.java（com.littlewin.note.service）

public interface AiAssistService {
    /** AI 笔记助手：扩写/润色/总结 */
    String assist(Long userId, String content, String action);
    /** AI 标签推荐：从已有标签中匹配 */
    List<String> recommendTags(Long userId, String content);
}

### 步骤 2：新建 AiAssistServiceImpl.java（com.littlewin.note.service.impl）

@Service
@Slf4j
@RequiredArgsConstructor
public class AiAssistServiceImpl implements AiAssistService {

    @Value("${ai.deepseek.api-key:sk-xxx}")
    private String apiKey;
    @Value("${ai.deepseek.base-url:https://api.deepseek.com}")
    private String baseUrl;
    @Value("${ai.deepseek.model-name:deepseek-chat}")
    private String modelName;
    @Value("${ai.deepseek.max-tokens:1000}")
    private Integer maxTokens;

    private final AiQuotaService aiQuotaService;
    private final NoteTagMapper noteTagMapper;

    private ChatLanguageModel chatModel;

    @PostConstruct
    public void init() {
        chatModel = OpenAiChatModel.builder()
                .apiKey(apiKey)
                .baseUrl(baseUrl)
                .modelName(modelName)
                .maxTokens(maxTokens)
                .build();
    }

    @Override
    public String assist(Long userId, String content, String action) {
        aiQuotaService.checkQuota(userId);

        String prompt;
        switch (action) {
            case "expand":
                prompt = "请将以下文字扩写到200-300字，保持原意，丰富细节，使用流畅的中文：\n\n" + content;
                break;
            case "polish":
                prompt = "请润色以下文字，使其更加通顺、专业，保持原意不变：\n\n" + content;
                break;
            case "summarize":
                prompt = "请用50字以内总结以下文字的核心内容：\n\n" + content;
                break;
            default:
                throw new ServiceException("不支持的操作类型: " + action);
        }

        int promptTokens = 0;
        int completionTokens = 0;
        String result = null;
        int status = 1;
        String errorMsg = null;

        try {
            ChatRequest request = ChatRequest.builder()
                    .messages(List.of(UserMessage.from(prompt)))
                    .build();
            ChatResponse response = chatModel.chat(request);
            result = response.aiMessage().text().trim();

            TokenUsage usage = response.tokenUsage();
            promptTokens = usage.inputTokenCount();
            completionTokens = usage.outputTokenCount();
        } catch (Exception e) {
            status = 0;
            errorMsg = e.getMessage();
            log.error("AI 笔记助手失败: action={}", action, e);
        }

        aiQuotaService.recordUsage(userId, promptTokens, completionTokens, modelName, null, status, errorMsg);

        if (status == 0) {
            throw new ServiceException("AI 处理失败：" + errorMsg);
        }
        return result;
    }

    @Override
    public List<String> recommendTags(Long userId, String content) {
        aiQuotaService.checkQuota(userId);

        // 1. 获取用户已有标签
        List<NoteTag> myTags = noteTagMapper.selectList(
                new LambdaQueryWrapper<NoteTag>().eq(NoteTag::getUserId, userId));
        if (myTags.isEmpty()) {
            return List.of();
        }
        String tagNames = myTags.stream().map(NoteTag::getName).collect(Collectors.joining("、"));

        // 2. 让 LLM 从已有标签中匹配
        String prompt = "以下是我的标签列表：[" + tagNames + "]\n\n"
                + "请从上面的标签中，选出与下面这篇笔记最相关的1-3个标签。\n"
                + "只返回标签名，多个用逗号分隔，不要返回其他内容。如果没有匹配的，返回空。\n\n"
                + "笔记内容：\n" + content.substring(0, Math.min(content.length(), 1000));

        int promptTokens = 0;
        int completionTokens = 0;
        List<String> result = new ArrayList<>();
        int status = 1;
        String errorMsg = null;

        try {
            ChatRequest request = ChatRequest.builder()
                    .messages(List.of(UserMessage.from(prompt)))
                    .build();
            ChatResponse response = chatModel.chat(request);
            String text = response.aiMessage().text().trim();

            TokenUsage usage = response.tokenUsage();
            promptTokens = usage.inputTokenCount();
            completionTokens = usage.outputTokenCount();

            // 解析返回的标签名，与已有标签做交集
            Set<String> myTagSet = myTags.stream().map(NoteTag::getName).collect(Collectors.toSet());
            for (String tag : text.split("[,，、\\s]+")) {
                String t = tag.trim();
                if (!t.isEmpty() && myTagSet.contains(t)) {
                    result.add(t);
                }
            }
        } catch (Exception e) {
            status = 0;
            errorMsg = e.getMessage();
            log.error("AI 标签推荐失败", e);
        }

        aiQuotaService.recordUsage(userId, promptTokens, completionTokens, modelName, null, status, errorMsg);

        if (status == 0) {
            throw new ServiceException("标签推荐失败：" + errorMsg);
        }
        return result;
    }
}

### 步骤 3：修改 WxAiController.java — 新增 2 个接口

在已有接口之后添加：

private final AiAssistService aiAssistService;

/** AI 笔记助手 */
@PostMapping("/assist")
public Result<String> assist(@RequestBody Map<String, String> body) {
    Long userId = SecurityUtils.getLoginUser().getUserId();
    String content = body.get("content");
    String action = body.get("action");
    if (content == null || content.trim().isEmpty()) {
        throw new ServiceException("内容不能为空");
    }
    if (action == null || action.trim().isEmpty()) {
        throw new ServiceException("操作类型不能为空");
    }
    return Result.success(aiAssistService.assist(userId, content, action));
}

/** AI 标签推荐 */
@PostMapping("/recommend-tags")
public Result<List<String>> recommendTags(@RequestBody Map<String, String> body) {
    Long userId = SecurityUtils.getLoginUser().getUserId();
    String content = body.get("content");
    if (content == null || content.trim().isEmpty()) {
        throw new ServiceException("内容不能为空");
    }
    return Result.success(aiAssistService.recommendTags(userId, content));
}

⚠️ 注意：
- 路径保持 /api/wx/notes/ai/assist 和 /api/wx/notes/ai/recommend-tags
- 需要 import AiAssistService、Map、List

验证：
1. POST /api/wx/notes/ai/assist { content: "Spring Boot是一个框架", action: "expand" } → 返回扩写后的文字
2. POST /api/wx/notes/ai/assist { content: "很长的文章...", action: "summarize" } → 返回50字总结
3. POST /api/wx/notes/ai/recommend-tags { content: "Spring Boot自动配置..." } → 返回匹配的标签名数组
4. 配额不足时返回 403
5. ai_usage_log 表有对应记录
```

---

## 提示词 2：小程序 API + UI 改造

```
在 smart-note-mp 中，为 create.vue 添加 AI 笔记助手和标签推荐功能。

⚠️ 前置条件：提示词 1 已完成（后端接口已就绪）。

### 步骤 1：api/config.js — 新增 AI 接口路径

在 NOTE 对象中新增：
AI_ASSIST: '/api/wx/notes/ai/assist',
AI_RECOMMEND_TAGS: '/api/wx/notes/ai/recommend-tags'

### 步骤 2：api/modules/note.js — 新增方法

在 getAiQuota 方法之后添加：

assist(content, action) {
    return request({
      url: APIS.NOTE.AI_ASSIST,
      method: 'POST',
      data: { content, action }
    })
  },
  recommendTags(content) {
    return request({
      url: APIS.NOTE.AI_RECOMMEND_TAGS,
      method: 'POST',
      data: { content }
    })
  }

⚠️ 注意：这两个接口用 POST + data（body 传参），不是 params。

### 步骤 3：改造 create.vue — 添加 AI 功能

(1) 在 textarea 编辑区上方（content-editor 内部，textarea 之前），添加 AI 助手按钮组：

<view class="ai-assist-bar">
  <view class="ai-btn" :class="{ disabled: !form.content || aiLoading }" @click="onAiAssist('expand')">
    <text>扩写</text>
  </view>
  <view class="ai-btn" :class="{ disabled: !form.content || aiLoading }" @click="onAiAssist('polish')">
    <text>润色</text>
  </view>
  <view class="ai-btn" :class="{ disabled: !form.content || aiLoading }" @click="onAiAssist('summarize')">
    <text>总结</text>
  </view>
  <view v-if="aiLoading" class="ai-loading">
    <text>AI 处理中...</text>
  </view>
</view>

(2) 在标签区域底部（tags-section 内部，add-tag 之后），添加推荐标签按钮：

<view class="recommend-tags-section">
  <view class="recommend-btn" :class="{ disabled: !form.content || recommendLoading }" @click="onRecommendTags">
    <text>{{ recommendLoading ? '推荐中...' : '推荐标签' }}</text>
  </view>
  <view v-if="recommendedTags.length" class="recommended-list">
    <view
      v-for="tag in recommendedTags"
      :key="tag"
      class="recommended-tag"
      :class="{ active: selectedTagNames.includes(tag) }"
      @click="onSelectRecommendedTag(tag)"
    >
      {{ tag }}
    </view>
  </view>
</view>

(3) script 中新增状态和方法：

const aiLoading = ref(false);
const recommendLoading = ref(false);
const recommendedTags = ref([]);

// 选中的标签名列表（用于推荐标签高亮）
const selectedTagNames = computed(() => {
  return selectedTagIds.value.map(id => {
    const tag = myTags.value.find(t => t.tagId === id);
    return tag ? tag.name : '';
  }).filter(Boolean);
});

// AI 笔记助手
const onAiAssist = async (action) => {
  if (!form.content || aiLoading.value) return;
  aiLoading.value = true;
  try {
    const result = await noteApi.assist(form.content, action);
    if (result) {
      // 扩写和润色替换原文，总结追加到末尾
      if (action === 'summarize') {
        form.content = form.content + '\n\n> 摘要：' + result;
      } else {
        form.content = result;
      }
      uni.showToast({ title: '处理成功', icon: 'success' });
    }
  } catch (e) {
    // 错误已被 request.js 拦截处理
  } finally {
    aiLoading.value = false;
  }
};

// 推荐标签
const onRecommendTags = async () => {
  if (!form.content || recommendLoading.value) return;
  recommendLoading.value = true;
  try {
    const tags = await noteApi.recommendTags(form.content);
    recommendedTags.value = tags || [];
    if (!tags || tags.length === 0) {
      uni.showToast({ title: '没有匹配的标签', icon: 'none' });
    }
  } catch (e) {
    // error handled
  } finally {
    recommendLoading.value = false;
  }
};

// 选中/取消推荐标签
const onSelectRecommendedTag = (tagName) => {
  const tag = myTags.value.find(t => t.name === tagName);
  if (!tag) return;
  toggleTag(tag.tagId);
};

(4) 新增 CSS 样式：

.ai-assist-bar {
  display: flex;
  align-items: center;
  gap: 16rpx;
  margin-bottom: 20rpx;
  padding: 16rpx 0;
  border-bottom: 1rpx solid #f0f0f0;
}

.ai-btn {
  padding: 8rpx 24rpx;
  border-radius: 24rpx;
  background: #e8f4fd;
  font-size: 24rpx;
  color: #1890ff;
}

.ai-btn.disabled {
  opacity: 0.5;
  pointer-events: none;
}

.ai-loading {
  margin-left: auto;
  font-size: 24rpx;
  color: #909399;
}

.recommend-tags-section {
  margin-top: 16rpx;
}

.recommend-btn {
  display: inline-block;
  padding: 8rpx 24rpx;
  border-radius: 24rpx;
  background: #f0f9eb;
  font-size: 24rpx;
  color: #67c23a;
}

.recommend-btn.disabled {
  opacity: 0.5;
  pointer-events: none;
}

.recommended-list {
  display: flex;
  flex-wrap: wrap;
  gap: 12rpx;
  margin-top: 12rpx;
}

.recommended-tag {
  padding: 6rpx 20rpx;
  border-radius: 20rpx;
  background: #f5f5f5;
  font-size: 22rpx;
  color: #606266;
  border: 1rpx solid #e4e7ed;
}

.recommended-tag.active {
  background: #e8f4fd;
  color: #1890ff;
  border-color: #1890ff;
}

验证：
1. 输入笔记内容 → 点"扩写" → 内容被替换为扩写版本
2. 输入笔记内容 → 点"润色" → 内容被替换为润色版本
3. 输入笔记内容 → 点"总结" → 内容末尾追加摘要
4. 输入笔记内容 → 点"推荐标签" → 显示匹配的标签列表
5. 点击推荐标签 → 标签被选中（高亮），再次点击取消选中
6. 内容为空时按钮置灰不可点
7. AI 处理中显示"AI 处理中..."
8. 配额不足时提示"本月 AI 请求次数已用完"
```

---

# ⏱️ Day 15 执行顺序

| 顺序 | 提示词 | 前置依赖 | 说明 |
|:--:|:---|:--:|:---|
| 1️⃣ | 提示词 1：后端接口 | 无 | AI 助手 + 标签推荐 |
| 2️⃣ | 提示词 2：小程序 API + UI | 提示词 1 | 前端对接后端 |

> 提示词 1 必须先执行。提示词 2 依赖提示词 1 的接口。

---

# 🔍 Day 15 涉及的文件清单

| 文件 | 改动类型 | 说明 |
|:---|:---|:---|
| AiAssistService.java | 新建 | AI 助手服务接口 |
| AiAssistServiceImpl.java | 新建 | AI 助手实现（扩写/润色/总结/标签推荐） |
| WxAiController.java | 修改 | 新增 /assist 和 /recommend-tags 接口 |
| api/config.js | 修改 | 新增 AI_ASSIST、AI_RECOMMEND_TAGS 路径 |
| api/modules/note.js | 修改 | 新增 assist()、recommendTags() 方法 |
| pages/subNote/create/create.vue | 修改 | AI 按钮组 + 推荐标签 UI |
