package com.littlewin.note.service.impl;

import com.littlewin.common.exception.ServiceException;
import com.littlewin.note.domain.entity.AiUsageLog;
import com.littlewin.note.domain.entity.AiUserQuota;
import com.littlewin.note.mapper.AiUsageLogMapper;
import com.littlewin.note.mapper.AiUserQuotaMapper;
import com.littlewin.note.service.AiQuotaService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AiQuotaServiceImpl implements AiQuotaService {

    private final AiUserQuotaMapper quotaMapper;
    private final AiUsageLogMapper logMapper;

    @Override
    public void checkQuota(Long userId) {
        AiUserQuota quota = quotaMapper.selectById(userId);
        if (quota == null) {
            quota = new AiUserQuota();
            quota.setUserId(userId);
            quota.setMonthlyTokenLimit(100000);
            quota.setMonthlyRequestLimit(50);
            quota.setUsedTokens(0);
            quota.setUsedRequests(0);
            quotaMapper.insert(quota);
        }

        LocalDate today = LocalDate.now();
        if (quota.getQuotaResetDate() == null || quota.getQuotaResetDate().getMonth() != today.getMonth()) {
            quota.setUsedTokens(0);
            quota.setUsedRequests(0);
            quota.setQuotaResetDate(today.withDayOfMonth(1));
            quotaMapper.updateById(quota);
        }

        if (quota.getUsedRequests() >= quota.getMonthlyRequestLimit()) {
            throw new ServiceException(403, "本月 AI 请求次数已用完，限额 " + quota.getMonthlyRequestLimit() + " 次");
        }

        int usedTokens = logMapper.sumTokensByUserThisMonth(userId);
        if (usedTokens >= quota.getMonthlyTokenLimit()) {
            throw new ServiceException(403, "本月 AI token 用量已用完，限额 " + quota.getMonthlyTokenLimit() + " tokens");
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void recordUsage(Long userId, int promptTokens, int completionTokens, String modelName, Long noteId, int status, String errorMsg) {
        AiUsageLog log = new AiUsageLog();
        log.setUserId(userId);
        log.setNoteId(noteId);
        log.setActionType("summary");
        log.setPromptTokens(promptTokens);
        log.setCompletionTokens(completionTokens);
        log.setTotalTokens(promptTokens + completionTokens);
        log.setModelName(modelName);
        log.setStatus(status);
        log.setErrorMsg(errorMsg);
        log.setCreateTime(LocalDateTime.now());
        logMapper.insert(log);

        AiUserQuota quota = quotaMapper.selectById(userId);
        if (quota != null) {
            quota.setUsedTokens(quota.getUsedTokens() + promptTokens + completionTokens);
            quota.setUsedRequests(quota.getUsedRequests() + 1);
            quotaMapper.updateById(quota);
        }
    }

    @Override
    public Map<String, Object> getRemainingQuota(Long userId) {
        AiUserQuota quota = quotaMapper.selectById(userId);
        if (quota == null) {
            return Map.of("tokenLimit", 100000, "requestLimit", 50, "usedTokens", 0, "usedRequests", 0);
        }
        return Map.of(
                "tokenLimit", quota.getMonthlyTokenLimit(),
                "requestLimit", quota.getMonthlyRequestLimit(),
                "usedTokens", quota.getUsedTokens(),
                "usedRequests", quota.getUsedRequests()
        );
    }
}
