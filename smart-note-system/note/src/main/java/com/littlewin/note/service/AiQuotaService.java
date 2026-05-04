package com.littlewin.note.service;

import java.util.Map;

public interface AiQuotaService {

    void checkQuota(Long userId);

    void recordUsage(Long userId, int promptTokens, int completionTokens, String modelName, Long noteId, int status, String errorMsg);

    Map<String, Object> getRemainingQuota(Long userId);
}
