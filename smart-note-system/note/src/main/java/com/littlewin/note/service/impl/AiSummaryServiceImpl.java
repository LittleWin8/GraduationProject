package com.littlewin.note.service.impl;

import com.littlewin.common.exception.ServiceException;
import com.littlewin.note.domain.entity.Note;
import com.littlewin.note.domain.entity.NoteAiSummary;
import com.littlewin.note.mapper.NoteAiSummaryMapper;
import com.littlewin.note.mapper.NoteMapper;
import com.littlewin.note.service.AiQuotaService;
import com.littlewin.note.service.AiSummaryService;
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
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
@RequiredArgsConstructor
public class AiSummaryServiceImpl implements AiSummaryService {

    @Value("${ai.deepseek.api-key:sk-xxx}")
    private String apiKey;
    @Value("${ai.deepseek.base-url:https://api.deepseek.com}")
    private String baseUrl;
    @Value("${ai.deepseek.model-name:deepseek-chat}")
    private String modelName;
    @Value("${ai.deepseek.max-tokens:500}")
    private Integer maxTokens;

    private final NoteMapper noteMapper;
    private final NoteAiSummaryMapper noteAiSummaryMapper;
    private final AiQuotaService aiQuotaService;

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
    @Transactional(rollbackFor = Exception.class)
    public Map<String, String> generateSummary(Long noteId, Long userId) {
        aiQuotaService.checkQuota(userId);

        Note note = noteMapper.selectById(noteId);
        if (note == null || note.getDelFlag() == 1) {
            throw new ServiceException("笔记不存在");
        }

        int promptTokens = 0;
        int completionTokens = 0;
        String summary = null;
        String keywords = null;
        int status = 1;
        String errorMsg = null;

        try {
            String prompt = "请为以下笔记生成一段简洁的摘要（100字以内），并提取3-5个关键词（逗号分隔）。\n\n"
                    + "标题：" + note.getTitle() + "\n"
                    + "内容：" + note.getContent().substring(0, Math.min(note.getContent().length(), 2000));

            ChatRequest request = ChatRequest.builder()
                    .messages(List.of(UserMessage.from(prompt)))
                    .build();
            ChatResponse response = chatModel.chat(request);
            String result = response.aiMessage().text();

            TokenUsage usage = response.tokenUsage();
            promptTokens = usage.inputTokenCount();
            completionTokens = usage.outputTokenCount();

            String[] parts = result.split("\n", 2);
            summary = parts[0].trim();
            keywords = parts.length > 1 ? parts[1].trim() : "";

        } catch (ServiceException e) {
            throw e;
        } catch (Exception e) {
            status = 0;
            errorMsg = e.getMessage();
            log.error("AI 摘要生成失败: noteId={}", noteId, e);
        }

        aiQuotaService.recordUsage(userId, promptTokens, completionTokens, modelName, noteId, status, errorMsg);

        if (status == 0) {
            throw new ServiceException("AI 摘要生成失败：" + errorMsg);
        }

        NoteAiSummary aiSummary = new NoteAiSummary();
        aiSummary.setNoteId(noteId);
        aiSummary.setSummary(summary);
        aiSummary.setKeywords(keywords);
        aiSummary.setModelName(modelName);
        aiSummary.setCreateTime(LocalDateTime.now());
        noteAiSummaryMapper.insertOrUpdate(aiSummary);

        return Map.of("summary", summary, "keywords", keywords);
    }
}
