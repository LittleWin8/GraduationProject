package com.littlewin.note.service.impl;

import com.littlewin.common.exception.ServiceException;
import com.littlewin.note.service.AiAnalyzeService;
import com.littlewin.note.service.AiQuotaService;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.chat.request.ChatRequest;
import dev.langchain4j.model.chat.response.ChatResponse;
import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.model.output.TokenUsage;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
@RequiredArgsConstructor
public class AiAnalyzeServiceImpl implements AiAnalyzeService {

    @Value("${ai.deepseek.api-key:sk-xxx}")
    private String apiKey;
    @Value("${ai.deepseek.base-url:https://api.deepseek.com}")
    private String baseUrl;
    @Value("${ai.deepseek.model-name:deepseek-chat}")
    private String modelName;

    private final AiQuotaService aiQuotaService;
    private final JdbcTemplate jdbcTemplate;

    private ChatLanguageModel chatModel;

    private static final String[] BLOCKED_COLUMNS = {"credential", "password", "phone", "email", "birthday"};

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

    @PostConstruct
    public void init() {
        chatModel = OpenAiChatModel.builder()
                .apiKey(apiKey)
                .baseUrl(baseUrl)
                .modelName(modelName)
                .maxTokens(500)
                .build();
    }

    String sanitizeSql(String sql) {
        if (sql == null || sql.trim().isEmpty()) {
            throw new ServiceException("生成的 SQL 为空");
        }
        sql = sql.replaceAll("```sql\\s*", "").replaceAll("```\\s*", "").trim();
        if (sql.endsWith(";")) {
            sql = sql.substring(0, sql.length() - 1).trim();
        }
        String lower = sql.toLowerCase().replaceAll("/\\*.*?\\*/", " ");
        if (!lower.startsWith("select")) {
            throw new ServiceException("只允许 SELECT 查询");
        }
        String[] forbidden = {"insert", "update", "delete", "drop", "alter", "truncate", "create", "exec", "execute", "grant", "revoke"};
        for (String kw : forbidden) {
            if (lower.contains(" " + kw + " ") || lower.contains(";" + kw)) {
                throw new ServiceException("禁止执行 " + kw + " 操作");
            }
        }
        if (lower.contains("into ") && lower.contains("values")) {
            throw new ServiceException("禁止 INSERT 操作");
        }
        for (String col : BLOCKED_COLUMNS) {
            if (lower.contains(col)) {
                throw new ServiceException("禁止查询敏感字段: " + col);
            }
        }
        if (!lower.contains("limit")) {
            sql = sql + " LIMIT 100";
        }
        return sql;
    }

    @Override
    public Map<String, Object> analyze(Long userId, String question) {
        aiQuotaService.checkQuota(userId);
        int promptTokens = 0, completionTokens = 0, status = 1;
        String errorMsg = null, answer = null, sql = null;
        List<Map<String, Object>> data = null;
        try {
            ChatRequest sqlReq = ChatRequest.builder()
                    .messages(List.of(UserMessage.from(
                            SCHEMA + "\n只返回 SELECT SQL，不加代码块。\n问题：" + question)))
                    .build();
            ChatResponse sqlResp = chatModel.chat(sqlReq);
            sql = sqlResp.aiMessage().text().trim();
            TokenUsage u1 = sqlResp.tokenUsage();
            promptTokens += u1.inputTokenCount();
            completionTokens += u1.outputTokenCount();

            sql = sanitizeSql(sql);

            String fsql = sql;
            data = jdbcTemplate.execute((java.sql.Statement stmt) -> {
                stmt.setQueryTimeout(3);
                java.sql.ResultSet rs = stmt.executeQuery(fsql);
                java.sql.ResultSetMetaData meta = rs.getMetaData();
                int cc = meta.getColumnCount();
                List<Map<String, Object>> rows = new ArrayList<>();
                while (rs.next()) {
                    Map<String, Object> row = new LinkedHashMap<>();
                    for (int i = 1; i <= cc; i++) {
                        row.put(meta.getColumnLabel(i), rs.getObject(i));
                    }
                    rows.add(row);
                }
                return rows;
            });

            ChatRequest sumReq = ChatRequest.builder()
                    .messages(List.of(UserMessage.from(
                            "问题：" + question + "\nSQL：" + sql + "\n结果：" + data + "\n用中文简洁总结。")))
                    .build();
            ChatResponse sumResp = chatModel.chat(sumReq);
            answer = sumResp.aiMessage().text().trim();
            TokenUsage u2 = sumResp.tokenUsage();
            promptTokens += u2.inputTokenCount();
            completionTokens += u2.outputTokenCount();
        } catch (ServiceException e) {
            throw e;
        } catch (Exception e) {
            status = 0;
            errorMsg = e.getMessage();
            log.error("数据分析失败", e);
        }
        aiQuotaService.recordUsage(userId, promptTokens, completionTokens, modelName, null, status, errorMsg);
        if (status == 0) {
            throw new ServiceException("数据分析失败：" + errorMsg);
        }
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("answer", answer);
        result.put("sql", sql);
        result.put("data", data);
        return result;
    }
}
