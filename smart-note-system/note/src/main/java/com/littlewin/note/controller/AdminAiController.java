package com.littlewin.note.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.littlewin.common.core.Result;
import com.littlewin.common.exception.ServiceException;
import com.littlewin.common.log.annotation.Log;
import com.littlewin.common.log.enums.LogAction;
import com.littlewin.common.log.enums.LogModule;
import com.littlewin.common.utils.SecurityUtils;
import com.littlewin.note.domain.dto.AiUsageLogQueryDTO;
import com.littlewin.note.domain.dto.AiUserQuotaQueryDTO;
import com.littlewin.note.domain.vo.AiUsageLogVO;
import com.littlewin.note.domain.vo.AiUserQuotaVO;
import com.littlewin.note.service.AdminAiService;
import com.littlewin.note.service.AiAnalyzeService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin/ai")
public class AdminAiController {

    private final AdminAiService adminAiService;
    private final AiAnalyzeService aiAnalyzeService;

    @GetMapping("/logs")
    public Result<IPage<AiUsageLogVO>> listLogs(AiUsageLogQueryDTO query) {
        return Result.success(adminAiService.listLogs(query));
    }

    @GetMapping("/stats")
    public Result<Map<String, Object>> getStats() {
        return Result.success(adminAiService.getStats());
    }

    @GetMapping("/ranking")
    public Result<List<Map<String, Object>>> getUserRanking(
            @RequestParam(defaultValue = "10") int limit,
            @RequestParam(required = false) String startTime,
            @RequestParam(required = false) String endTime) {
        return Result.success(adminAiService.getUserRanking(limit, startTime, endTime));
    }

    @GetMapping("/quota/list")
    public Result<IPage<AiUserQuotaVO>> listQuotas(AiUserQuotaQueryDTO query) {
        return Result.success(adminAiService.listQuotas(query));
    }

    @PutMapping("/quota/{userId}")
    @Log(module = LogModule.AI, action = LogAction.UPDATE, desc = "修改用户AI配额")
    public Result<Void> updateQuota(@PathVariable("userId") Long userId,
                                    @RequestBody Map<String, Integer> body) {
        adminAiService.updateQuota(userId, body.get("monthlyTokenLimit"), body.get("monthlyRequestLimit"));
        return Result.success();
    }

    @PostMapping("/analyze")
    public Result<Map<String, Object>> analyze(@RequestBody Map<String, String> body) {
        Long userId = SecurityUtils.getLoginUser().getUserId();
        String question = body.get("question");
        if (question == null || question.trim().isEmpty()) {
            throw new ServiceException("问题不能为空");
        }
        return Result.success(aiAnalyzeService.analyze(userId, question));
    }
}
