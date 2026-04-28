package com.littlewin.note.controller;

import com.littlewin.common.core.Result;
import com.littlewin.common.enums.LogAction;
import com.littlewin.common.enums.LogModule;
import com.littlewin.common.log.annotation.Log;
import com.littlewin.common.log.context.LogContext;
import com.littlewin.common.utils.SecurityUtils;
import com.littlewin.note.domain.vo.InteractionResultVO;
import com.littlewin.note.domain.vo.InteractionStatusVO;
import com.littlewin.note.service.InteractionService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 小程序端互动接口（点赞/收藏切换、状态查询）
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/wx/interactions")
public class WxInteractionController {

    private final InteractionService interactionService;

    /** 点赞/收藏切换，请求体: { noteId, type("like"|"collect") } */
    @PostMapping
    @Log(module = LogModule.NOTE, action = LogAction.CREATE, desc = "点赞/收藏切换")
    public Result<InteractionResultVO> toggle(@RequestBody Map<String, Object> params) {
        Long noteId = Long.valueOf(params.get("noteId").toString());
        String type = (String) params.get("type");
        Long currentUserId = SecurityUtils.getLoginUser().getUserId();
        InteractionResultVO result = interactionService.toggle(currentUserId, noteId, type);
        LogContext.setBusinessId(noteId);
        LogContext.setDesc(("like".equals(type) ? "点赞" : "收藏") + "切换: noteId=" + noteId);
        return Result.success(result);
    }

    /** 查询单条笔记的互动状态 */
    @GetMapping("/status/{noteId}")
    public Result<InteractionStatusVO> getStatus(@PathVariable("noteId") Long noteId) {
        Long currentUserId = SecurityUtils.getLoginUser().getUserId();
        return Result.success(interactionService.getStatus(currentUserId, noteId));
    }

    /** 批量查询互动状态，请求体: { noteIds: [100, 101] } */
    @PostMapping("/status")
    public Result<Map<String, InteractionStatusVO>> batchGetStatus(@RequestBody Map<String, List<Long>> params) {
        List<Long> noteIds = params.get("noteIds");
        Long currentUserId = SecurityUtils.getLoginUser().getUserId();
        return Result.success(interactionService.batchGetStatus(currentUserId, noteIds));
    }
}
