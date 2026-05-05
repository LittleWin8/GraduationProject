package com.littlewin.note.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.littlewin.common.exception.ServiceException;
import com.littlewin.note.domain.entity.NoteTag;
import com.littlewin.note.mapper.NoteTagMapper;
import com.littlewin.note.service.AiAssistService;
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
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

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

        List<NoteTag> myTags = noteTagMapper.selectList(
                new LambdaQueryWrapper<NoteTag>().eq(NoteTag::getUserId, userId));
        if (myTags.isEmpty()) {
            return List.of();
        }
        String tagNames = myTags.stream().map(NoteTag::getName).collect(Collectors.joining("、"));

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
