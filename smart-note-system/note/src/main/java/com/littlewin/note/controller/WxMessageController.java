package com.littlewin.note.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.littlewin.common.core.Result;
import com.littlewin.common.utils.SecurityUtils;
import com.littlewin.note.domain.vo.MessageVO;
import com.littlewin.note.service.MessageService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 小程序端站内消息接口
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/wx/messages")
public class WxMessageController {

    private final MessageService messageService;

    /** 未读消息数（分组） */
    @GetMapping("/unread-count")
    public Result<Map<String, Integer>> unreadCount() {
        Long userId = SecurityUtils.getLoginUser().getUserId();
        return Result.success(messageService.getUnreadCountGrouped(userId));
    }

    /** 消息列表（分页，支持按类型组过滤，查询后自动标记已读） */
    @GetMapping
    public Result<IPage<MessageVO>> list(
            @RequestParam(value = "group", required = false) String group,
            @RequestParam(value = "page", defaultValue = "1") int page,
            @RequestParam(value = "size", defaultValue = "20") int size) {
        Long userId = SecurityUtils.getLoginUser().getUserId();
        return Result.success(messageService.listMessages(userId, group, page, size));
    }

    /** 全部标记已读 */
    @PostMapping("/read-all")
    public Result<Void> readAll() {
        Long userId = SecurityUtils.getLoginUser().getUserId();
        messageService.markAllRead(userId);
        return Result.success(null);
    }

    /** 删除单条消息（仅本人） */
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable("id") Long messageId) {
        Long userId = SecurityUtils.getLoginUser().getUserId();
        messageService.deleteMessage(userId, messageId);
        return Result.success(null);
    }
}
