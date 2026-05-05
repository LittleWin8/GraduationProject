package com.littlewin.note.controller;

import com.littlewin.common.core.Result;
import com.littlewin.common.exception.ServiceException;
import com.littlewin.common.utils.SecurityUtils;
import com.littlewin.note.domain.entity.NoteAiSummary;
import com.littlewin.note.mapper.NoteAiSummaryMapper;
import com.littlewin.note.service.AiAssistService;
import com.littlewin.note.service.AiQuotaService;
import com.littlewin.note.service.AiSummaryService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/wx/notes/ai")
public class WxAiController {

    private final AiSummaryService aiSummaryService;
    private final NoteAiSummaryMapper noteAiSummaryMapper;
    private final AiQuotaService aiQuotaService;
    private final AiAssistService aiAssistService;

    @GetMapping("/summary")
    public Result<NoteAiSummary> getSummary(@RequestParam("noteId") Long noteId) {
        return Result.success(noteAiSummaryMapper.selectById(noteId));
    }

    @PostMapping("/summary")
    public Result<Map<String, String>> generateSummary(@RequestParam("noteId") Long noteId) {
        Long userId = SecurityUtils.getLoginUser().getUserId();
        return Result.success(aiSummaryService.generateSummary(noteId, userId));
    }

    @GetMapping("/quota")
    public Result<Map<String, Object>> getQuota() {
        Long userId = SecurityUtils.getLoginUser().getUserId();
        return Result.success(aiQuotaService.getRemainingQuota(userId));
    }

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

    @PostMapping("/recommend-tags")
    public Result<List<String>> recommendTags(@RequestBody Map<String, String> body) {
        Long userId = SecurityUtils.getLoginUser().getUserId();
        String content = body.get("content");
        if (content == null || content.trim().isEmpty()) {
            throw new ServiceException("内容不能为空");
        }
        return Result.success(aiAssistService.recommendTags(userId, content));
    }
}
