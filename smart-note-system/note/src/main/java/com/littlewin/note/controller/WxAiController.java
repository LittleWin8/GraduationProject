package com.littlewin.note.controller;

import com.littlewin.common.core.Result;
import com.littlewin.common.utils.SecurityUtils;
import com.littlewin.note.domain.entity.NoteAiSummary;
import com.littlewin.note.mapper.NoteAiSummaryMapper;
import com.littlewin.note.service.AiQuotaService;
import com.littlewin.note.service.AiSummaryService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/wx/notes/ai")
public class WxAiController {

    private final AiSummaryService aiSummaryService;
    private final NoteAiSummaryMapper noteAiSummaryMapper;
    private final AiQuotaService aiQuotaService;

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
}
