package com.littlewin.note.controller;

import com.littlewin.common.core.Result;
import com.littlewin.common.log.enums.LogAction;
import com.littlewin.common.log.enums.LogModule;
import com.littlewin.common.log.annotation.Log;
import com.littlewin.common.log.context.LogContext;
import com.littlewin.common.utils.SecurityUtils;
import com.littlewin.note.domain.dto.InteractionBatchDTO;
import com.littlewin.note.domain.dto.InteractionToggleDTO;
import com.littlewin.note.domain.vo.InteractionResultVO;
import com.littlewin.note.domain.vo.InteractionStatusVO;
import com.littlewin.note.service.WxInteractionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 小程序端互动接口（点赞/收藏切换、状态查询）
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/wx/interactions")
public class WxInteractionController {

    private final WxInteractionService interactionService;

    /** 点赞/收藏切换 */
    @PostMapping
    @Log(module = LogModule.NOTE, action = LogAction.CREATE, desc = "点赞/收藏切换")
    public Result<InteractionResultVO> toggle(@RequestBody @Valid InteractionToggleDTO dto) {
        Long currentUserId = SecurityUtils.getLoginUser().getUserId();
        InteractionResultVO result = interactionService.toggle(currentUserId, dto.getNoteId(), dto.getType());
        LogContext.setBusinessId(dto.getNoteId());
        LogContext.setDesc(("like".equals(dto.getType()) ? "点赞" : "收藏") + "切换: noteId=" + dto.getNoteId());
        return Result.success(result);
    }

    /** 查询单条笔记的互动状态 */
    @GetMapping("/status/{noteId}")
    public Result<InteractionStatusVO> getStatus(@PathVariable("noteId") Long noteId) {
        Long currentUserId = SecurityUtils.getLoginUser().getUserId();
        return Result.success(interactionService.getStatus(currentUserId, noteId));
    }

    /** 批量查询互动状态 */
    @PostMapping("/status")
    public Result<Map<String, InteractionStatusVO>> batchGetStatus(@RequestBody @Valid InteractionBatchDTO dto) {
        Long currentUserId = SecurityUtils.getLoginUser().getUserId();
        return Result.success(interactionService.batchGetStatus(currentUserId, dto.getNoteIds()));
    }
}
