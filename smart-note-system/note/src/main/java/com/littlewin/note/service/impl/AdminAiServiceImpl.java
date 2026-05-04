package com.littlewin.note.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.littlewin.common.exception.ServiceException;
import com.littlewin.note.domain.dto.AiUsageLogQueryDTO;
import com.littlewin.note.domain.dto.AiUserQuotaQueryDTO;
import com.littlewin.note.domain.entity.AiUserQuota;
import com.littlewin.note.domain.vo.AiUsageLogVO;
import com.littlewin.note.domain.vo.AiUserQuotaVO;
import com.littlewin.note.mapper.AiUsageLogMapper;
import com.littlewin.note.mapper.AiUserQuotaMapper;
import com.littlewin.note.service.AdminAiService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AdminAiServiceImpl implements AdminAiService {

    private final AiUsageLogMapper aiUsageLogMapper;
    private final AiUserQuotaMapper aiUserQuotaMapper;

    @Override
    public IPage<AiUsageLogVO> listLogs(AiUsageLogQueryDTO query) {
        Page<AiUsageLogVO> page = new Page<>(query.getPageNum(), query.getPageSize());
        return aiUsageLogMapper.selectLogPage(page, query);
    }

    @Override
    public Map<String, Object> getStats() {
        return aiUsageLogMapper.selectGlobalStats();
    }

    @Override
    public List<Map<String, Object>> getUserRanking(int limit, String startTime, String endTime) {
        return aiUsageLogMapper.selectUserRanking(limit, startTime, endTime);
    }

    @Override
    public IPage<AiUserQuotaVO> listQuotas(AiUserQuotaQueryDTO query) {
        Page<AiUserQuotaVO> page = new Page<>(query.getPageNum(), query.getPageSize());
        return aiUserQuotaMapper.selectQuotaPage(page, query);
    }

    @Override
    public void updateQuota(Long userId, Integer tokenLimit, Integer requestLimit) {
        AiUserQuota quota = aiUserQuotaMapper.selectById(userId);
        if (quota == null) {
            throw new ServiceException("用户配额记录不存在，请先触发一次 AI 调用以自动创建");
        }
        quota.setMonthlyTokenLimit(tokenLimit);
        quota.setMonthlyRequestLimit(requestLimit);
        aiUserQuotaMapper.updateById(quota);
    }
}
