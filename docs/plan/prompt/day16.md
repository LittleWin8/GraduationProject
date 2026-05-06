# Day 16：管理端数据分析问答

## 任务概览

| 序号 | 任务 | 优先级 |
|:--:|:---|:--:|
| 1a | 安全层：sanitizeSql + 敏感字段拦截 | 🔴 |
| 1b | 分析服务：SCHEMA + analyze + Controller | 🔴 |
| 2 | Web UI：仪表盘聊天卡片 | 🟡 |

---

## 提示词 1a：安全层 sanitizeSql

```
在 smart-note-system 中，新建 AiAnalyzeService 接口和 sanitizeSql 安全校验方法。

### 步骤 1：新建 AiAnalyzeService.java（com.littlewin.note.service）

public interface AiAnalyzeService {
    Map<String, Object> analyze(Long userId, String question);
}

### 步骤 2：新建 AiAnalyzeServiceImpl.java（com.littlewin.note.service.impl）

只写类结构和 sanitizeSql，analyze 方法在 1b 补充。

@Service @Slf4j @RequiredArgsConstructor
public class AiAnalyzeServiceImpl implements AiAnalyzeService {
    @Value("${ai.deepseek.api-key:sk-xxx}") private String apiKey;
    @Value("${ai.deepseek.base-url:https://api.deepseek.com}") private String baseUrl;
    @Value("${ai.deepseek.model-name:deepseek-chat}") private String modelName;

    private final AiQuotaService aiQuotaService;
    private final JdbcTemplate jdbcTemplate;
    private ChatLanguageModel chatModel;

    private static final String[] BLOCKED_COLUMNS = {"credential", "password", "phone", "email", "birthday"};

    @PostConstruct
    public void init() {
        chatModel = OpenAiChatModel.builder()
                .apiKey(apiKey).baseUrl(baseUrl).modelName(modelName).maxTokens(500).build();
    }

    String sanitizeSql(String sql) {
        if (sql == null || sql.trim().isEmpty()) throw new ServiceException("生成的 SQL 为空");
        sql = sql.replaceAll("```sql\\s*", "").replaceAll("```\\s*", "").trim();
        if (sql.endsWith(";")) sql = sql.substring(0, sql.length() - 1).trim();
        String lower = sql.toLowerCase().replaceAll("/\\*.*?\\*/", " ");
        if (!lower.startsWith("select")) throw new ServiceException("只允许 SELECT 查询");
        String[] forbidden = {"insert","update","delete","drop","alter","truncate","create","exec","execute","grant","revoke"};
        for (String kw : forbidden) {
            if (lower.contains(" " + kw + " ") || lower.contains(";" + kw))
                throw new ServiceException("禁止执行 " + kw + " 操作");
        }
        if (lower.contains("into ") && lower.contains("values")) throw new ServiceException("禁止 INSERT 操作");
        for (String col : BLOCKED_COLUMNS) {
            if (lower.contains(col)) throw new ServiceException("禁止查询敏感字段: " + col);
        }
        if (!lower.contains("limit")) sql = sql + " LIMIT 100";
        return sql;
    }

    @Override
    public Map<String, Object> analyze(Long userId, String question) {
        throw new UnsupportedOperationException("待 1b 实现");
    }
}

验证：
1. 项目编译通过
2. sanitizeSql("SELECT * FROM note; DROP TABLE note") → 抛出 "禁止执行 drop"
3. sanitizeSql("SELECT/**/credential/**/FROM user_auth") → 抛出 "禁止查询敏感字段"
4. sanitizeSql("SELECT * FROM note") → 返回带 LIMIT 100 的 SQL
```

---

## 提示词 1b：分析服务 + 接口

```
补充 AiAnalyzeServiceImpl.analyze 方法，新增 Controller 接口。

⚠️ 前置：1a 已完成。

### 步骤 1：补充 SCHEMA 常量（在 AiAnalyzeServiceImpl 类中）

private static final String SCHEMA = """
        数据库表（MySQL）：
        1. sys_user (user_id, nickname, avatar, status, del_flag, create_time, update_time)
        2. user_auth (id, user_id, auth_type, identifier)
        3. user_info (user_id, gender, city, signature)
        4. note (note_id, user_id, category_id, title, content, is_public, status, view_count, like_count, comment_count, summary, del_flag, create_time, update_time)
        5. note_tag (tag_id, name, user_id, create_time)
        6. note_tag_rel (note_id, tag_id)
        7. note_comment (comment_id, note_id, user_id, content, parent_id, del_flag, create_time)
        8. note_reaction (id, note_id, user_id, attitude, is_favorite, create_time, update_time)
        9. note_ai_summary (note_id, summary, keywords, model_name, create_time)
        10. user_message (id, receiver_id, sender_id, title, note_id, type, is_read, create_time)
        11. ai_usage_log (id, user_id, note_id, action_type, prompt_tokens, completion_tokens, total_tokens, model_name, status, create_time)
        12. ai_user_quota (user_id, monthly_token_limit, monthly_request_limit, used_tokens, used_requests, quota_reset_date)
        13. sys_category (category_id, name, parent_id, sort_order, status, create_time)
        14. sys_role (role_id, role_key, role_name, status, create_time)
        15. sys_menu (menu_id, parent_id, name, path, component, menu_type, title, icon, perms, sort_order)
        16. sys_role_menu (role_id, menu_id)
        17. sys_user_role (user_id, role_id)
        18. note_attachment (attach_id, note_id, user_id, file_url, file_name, file_suffix, file_size, create_time)
        19. sys_log_behavior (id, user_id, action_type, content, create_time)
        20. sys_log_operation (id, user_id, username, module, action_type, request_method, ip_address, status, create_time)
        21. sys_dict_type (dict_id, dict_name, dict_type, status, remark, create_time)
        22. sys_dict_data (data_id, dict_type, dict_label, dict_value, tag_type, sort_order, status)
        字段说明：note.status:0草稿/1正常/2回收站/3下架，user_message.type:1评论/2回复/3审核通过/4审核不通过/5违规下架/6公告/7点赞/8收藏
        """;

⚠️ SCHEMA 中不列出 credential、phone、email、birthday、user_message.content 等敏感字段。

### 步骤 2：替换 analyze 方法

@Override
public Map<String, Object> analyze(Long userId, String question) {
    aiQuotaService.checkQuota(userId);
    int promptTokens = 0, completionTokens = 0, status = 1;
    String errorMsg = null, answer = null, sql = null;
    List<Map<String, Object>> data = null;
    try {
        // 1. LLM 生成 SQL
        ChatRequest sqlReq = ChatRequest.builder()
                .messages(List.of(UserMessage.from(
                        SCHEMA + "\n只返回 SELECT SQL，不加代码块。\n问题：" + question)))
                .build();
        ChatResponse sqlResp = chatModel.chat(sqlReq);
        sql = sqlResp.aiMessage().text().trim();
        TokenUsage u1 = sqlResp.tokenUsage();
        promptTokens += u1.inputTokenCount(); completionTokens += u1.outputTokenCount();
        // 2. 安全校验
        sql = sanitizeSql(sql);
        // 3. 执行（3s 超时）
        String fsql = sql;
        data = jdbcTemplate.execute((java.sql.Statement stmt) -> {
            stmt.setQueryTimeout(3);
            java.sql.ResultSet rs = stmt.executeQuery(fsql);
            java.sql.ResultSetMetaData meta = rs.getMetaData();
            int cc = meta.getColumnCount();
            List<Map<String, Object>> rows = new ArrayList<>();
            while (rs.next()) {
                Map<String, Object> row = new LinkedHashMap<>();
                for (int i = 1; i <= cc; i++) row.put(meta.getColumnLabel(i), rs.getObject(i));
                rows.add(row);
            }
            return rows;
        });
        // 4. LLM 总结
        ChatRequest sumReq = ChatRequest.builder()
                .messages(List.of(UserMessage.from(
                        "问题：" + question + "\nSQL：" + sql + "\n结果：" + data + "\n用中文简洁总结。")))
                .build();
        ChatResponse sumResp = chatModel.chat(sumReq);
        answer = sumResp.aiMessage().text().trim();
        TokenUsage u2 = sumResp.tokenUsage();
        promptTokens += u2.inputTokenCount(); completionTokens += u2.outputTokenCount();
    } catch (ServiceException e) { throw e;
    } catch (Exception e) { status = 0; errorMsg = e.getMessage(); log.error("数据分析失败", e); }
    aiQuotaService.recordUsage(userId, promptTokens, completionTokens, modelName, null, status, errorMsg);
    if (status == 0) throw new ServiceException("数据分析失败：" + errorMsg);
    Map<String, Object> result = new LinkedHashMap<>();
    result.put("answer", answer); result.put("sql", sql); result.put("data", data);
    return result;
}

### 步骤 3：修改 AdminAiController.java 新增接口

注入：private final AiAnalyzeService aiAnalyzeService;

新增：
@PostMapping("/analyze")
public Result<Map<String, Object>> analyze(@RequestBody Map<String, String> body) {
    Long userId = SecurityUtils.getLoginUser().getUserId();
    String question = body.get("question");
    if (question == null || question.trim().isEmpty()) throw new ServiceException("问题不能为空");
    return Result.success(aiAnalyzeService.analyze(userId, question));
}

验证：
1. POST /api/admin/ai/analyze { question: "本月新增笔记数" } → { answer, sql, data }
2. 问"查用户密码" → "禁止查询敏感字段"
3. 配额不足 → 403
```

---

## 提示词 2：Web 数据分析 UI

```
在 dashboard/index.vue 底部添加数据分析聊天卡片。

⚠️ 前置：1b 已完成。

### 步骤 1：dashboard.ts 新增 API

export interface AnalyzeResult { answer: string; sql: string; data: Record<string, any>[]; }
export const analyzeData = (question: string) => {
  return http.post<AnalyzeResult>(`/admin/ai/analyze`, { question });
};

### 步骤 2：dashboard/index.vue 添加聊天卡片

在最后一个 el-row 之后添加：

<el-row :gutter="20" class="mt20">
  <el-col :span="24">
    <el-card shadow="hover" class="analyze-card">
      <template #header>
        <div style="display:flex;align-items:center;gap:8px">
          <span>数据分析助手</span><el-tag type="info" size="small">AI 驱动</el-tag>
        </div>
      </template>
      <div ref="chatContainer" class="chat-messages">
        <div v-if="!chatHistory.length" class="chat-empty">试试问我：本月新增了多少笔记？</div>
        <div v-for="(msg,i) in chatHistory" :key="i" class="chat-item" :class="msg.role">
          <div class="chat-bubble">
            <div v-if="msg.role==='user'">{{ msg.question }}</div>
            <template v-if="msg.role==='assistant'">
              <div>{{ msg.answer }}</div>
              <el-collapse v-if="msg.sql"><el-collapse-item title="查看SQL"><code>{{ msg.sql }}</code></el-collapse-item></el-collapse>
              <el-table v-if="msg.data?.length" :data="msg.data" size="small" max-height="200" stripe style="margin-top:8px">
                <el-table-column v-for="col in Object.keys(msg.data[0])" :key="col" :prop="col" :label="col" min-width="100" />
              </el-table>
            </template>
          </div>
        </div>
        <div v-if="analyzeLoading" class="chat-item assistant"><div class="chat-bubble">正在分析...</div></div>
      </div>
      <div class="chat-input">
        <el-input v-model="analyzeQuestion" placeholder="输入数据问题" :disabled="analyzeLoading" @keyup.enter="onAnalyze" clearable>
          <template #append><el-button :icon="Search" :loading="analyzeLoading" @click="onAnalyze" /></template>
        </el-input>
      </div>
    </el-card>
  </el-col>
</el-row>

script 新增：
import { analyzeData } from "@/api/modules/dashboard";
import { Search } from "@element-plus/icons-vue";
import { nextTick } from "vue";

const analyzeQuestion = ref("");
const analyzeLoading = ref(false);
const chatHistory = ref<any[]>([]);
const chatContainer = ref<HTMLElement>();

const onAnalyze = async () => {
  const q = analyzeQuestion.value.trim();
  if (!q || analyzeLoading.value) return;
  chatHistory.value.push({ role: "user", question: q });
  analyzeQuestion.value = "";
  analyzeLoading.value = true;
  nextTick(() => { if (chatContainer.value) chatContainer.value.scrollTop = chatContainer.value.scrollHeight; });
  try {
    const { data } = await analyzeData(q);
    if (data) chatHistory.value.push({ role: "assistant", answer: data.answer, sql: data.sql, data: data.data });
  } catch { chatHistory.value.push({ role: "assistant", answer: "分析失败" });
  } finally {
    analyzeLoading.value = false;
    nextTick(() => { if (chatContainer.value) chatContainer.value.scrollTop = chatContainer.value.scrollHeight; });
  }
};

CSS：
.mt20 { margin-top: 20px; }
.analyze-card {
  .chat-messages { max-height: 400px; overflow-y: auto; margin-bottom: 12px; }
  .chat-empty { text-align: center; color: #909399; padding: 40px 0; }
  .chat-item { margin-bottom: 12px;
    &.user { text-align: right; .chat-bubble { display: inline-block; background: #ecf5ff; color: #409eff; padding: 8px 14px; border-radius: 12px 12px 2px 12px; max-width: 80%; } }
    &.assistant { text-align: left; .chat-bubble { display: inline-block; background: #f4f4f5; color: #303133; padding: 10px 14px; border-radius: 12px 12px 12px 2px; max-width: 90%; } }
  }
  .chat-input { border-top: 1px solid #f0f0f0; padding-top: 12px; }
}

验证：输入问题→显示分析结果+SQL+表格，连续提问累积历史。
```

---

## 执行顺序

1a → 1b → 2 串行。

## 文件清单

| 文件 | 改动 |
|:---|:---|
| AiAnalyzeService.java | 新建 |
| AiAnalyzeServiceImpl.java | 新建 |
| AdminAiController.java | 修改 |
| dashboard.ts | 修改 |
| dashboard/index.vue | 修改 |

---

## 提示词 3：工作台拆分为两个子菜单

```
将"工作台"从单页面改为父菜单，下设"仪表盘"和"数据分析助手"两个子菜单。

⚠️ 当前状态：
- menu_id=1100，C 型菜单，path=/dashboard/index，component=/dashboard/index
- dashboard/index.vue 包含统计卡片+图表+数据分析聊天卡片

⚠️ 目标：
- 1100 改为 M 型（目录），不再直接对应页面
- 1101：仪表盘（原有图表内容）
- 1102：数据分析助手（聊天卡片独立页面）

### 步骤 1：修改 init_sys_data.sql 菜单配置

将原 1100 记录：
(1100, 0, 'dashboard', '/dashboard/index', '/dashboard/index', 'C', '工作台', 'Odometer', 2)

改为 M 型目录 + 两个子菜单：
(1100, 0, 'dashboard', '/dashboard', '', 'M', '工作台', 'Odometer', 2);
(1101, 1100, 'dashboardIndex', '/dashboard/index', '/dashboard/index', 'C', '仪表盘', 'DataLine', 1);
(1102, 1100, 'analyze', '/dashboard/analyze', '/dashboard/analyze/index', 'C', '数据分析', 'ChatLineRound', 2);

### 步骤 2：新建数据分析页面 src/views/dashboard/analyze/index.vue

将 dashboard/index.vue 中的数据分析聊天卡片代码（el-row analyze-card 整块 + script 中的 analyzeQuestion/analyzeLoading/chatHistory/onAnalyze + CSS）移到新文件。

新文件结构：
<template>
  <div class="analyze-box">
    <!-- 直接粘贴原 dashboard/index.vue 中的 analyze-card 区域 -->
  </div>
</template>

<script setup lang="ts" name="analyze">
import { ref, nextTick } from "vue";
import { Search } from "@element-plus/icons-vue";
import { analyzeData } from "@/api/modules/dashboard";

// 从 dashboard/index.vue 搬过来的状态和方法
const analyzeQuestion = ref("");
const analyzeLoading = ref(false);
const chatHistory = ref<any[]>([]);
const chatContainer = ref<HTMLElement>();

const onAnalyze = async () => { /* 原逻辑不变 */ };
</script>

<style scoped lang="scss">
/* 从 dashboard/index.vue 搬过来的 .analyze-card 相关样式 */
</style>

### 步骤 3：清理 dashboard/index.vue

从 dashboard/index.vue 中删除：
- template 中的 analyze-card el-row 整块
- script 中的 analyzeQuestion、analyzeLoading、chatHistory、chatContainer、onAnalyze
- script 中的 analyzeData import
- CSS 中的 .mt20、.analyze-card 相关样式
- Search 图标 import（如果只用于分析卡片）

保留：统计卡片 + 折线图 + 饼图 + 柱状图

### 步骤 4：确认首页重定向

检查 config/index.ts 中 HOME_URL 是否为 /dashboard/index（不是 /dashboard），确保登录后跳转到仪表盘子页面。

验证：
1. 左侧菜单"工作台"展开为两个子菜单："仪表盘"和"数据分析"
2. 点击"仪表盘"→ 显示统计卡片+图表（无聊天卡片）
3. 点击"数据分析"→ 显示聊天卡片（输入问题→AI回答）
4. 登录后默认跳转到仪表盘
```

## 文件清单（提示词 3 补充）

| 文件 | 改动 |
|:---|:---|
| init_sys_data.sql | 修改：1100 改 M 型 + 新增 1101、1102 |
| dashboard/analyze/index.vue | 新建：数据分析聊天卡片页面 |
| dashboard/index.vue | 修改：删除聊天卡片代码 |
