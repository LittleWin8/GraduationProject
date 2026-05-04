package com.littlewin.note.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.littlewin.note.domain.dto.AiUsageLogQueryDTO;
import com.littlewin.note.domain.dto.AiUserQuotaQueryDTO;
import com.littlewin.note.domain.vo.AiUsageLogVO;
import com.littlewin.note.domain.vo.AiUserQuotaVO;

import java.util.List;
import java.util.Map;

public interface AdminAiService {

    IPage<AiUsageLogVO> listLogs(AiUsageLogQueryDTO query);

    Map<String, Object> getStats();

    List<Map<String, Object>> getUserRanking(int limit, String startTime, String endTime);

    IPage<AiUserQuotaVO> listQuotas(AiUserQuotaQueryDTO query);

    void updateQuota(Long userId, Integer tokenLimit, Integer requestLimit);
}
