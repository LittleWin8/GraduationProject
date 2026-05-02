package com.littlewin.note.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.littlewin.common.core.Result;
import com.littlewin.common.log.enums.LogAction;
import com.littlewin.common.log.enums.LogModule;
import com.littlewin.common.log.annotation.Log;
import com.littlewin.common.log.context.LogContext;
import com.littlewin.common.utils.SecurityUtils;
import com.littlewin.note.domain.dto.CommentCreateDTO;
import com.littlewin.note.domain.vo.CommentVO;
import com.littlewin.note.service.CommentService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 小程序端评论接口（列表查询、发表、删除）
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/wx/comments")
public class WxCommentController {

    private final CommentService commentService;

    /** 评论列表（分页，按时间倒序） */
    @GetMapping
    public Result<IPage<CommentVO>> list(
            @RequestParam("noteId") Long noteId,
            @RequestParam(value = "page", defaultValue = "1") int page,
            @RequestParam(value = "size", defaultValue = "10") int size) {
        Long userId = SecurityUtils.getLoginUser().getUserId();
        return Result.success(commentService.listComments(noteId, userId, page, size));
    }

    /** 发表评论 */
    @PostMapping
    @Log(module = LogModule.NOTE, action = LogAction.CREATE, desc = "发表评论")
    public Result<CommentVO> create(@RequestBody CommentCreateDTO dto) {
        Long userId = SecurityUtils.getLoginUser().getUserId();
        CommentVO vo = commentService.createComment(userId, dto);
        LogContext.setBusinessId(dto.getNoteId());
        LogContext.setDesc("评论笔记: noteId=" + dto.getNoteId());
        return Result.success(vo);
    }

    /** 删除评论（逻辑删除，仅本人） */
    @DeleteMapping("/{id}")
    @Log(module = LogModule.NOTE, action = LogAction.DELETE, desc = "删除评论")
    public Result<Void> delete(@PathVariable("id") Long commentId) {
        Long userId = SecurityUtils.getLoginUser().getUserId();
        commentService.deleteComment(userId, commentId);
        LogContext.setBusinessId(commentId);
        LogContext.setDesc("删除评论: commentId=" + commentId);
        return Result.success(null);
    }
}
