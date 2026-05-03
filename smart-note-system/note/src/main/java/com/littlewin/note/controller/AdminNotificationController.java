package com.littlewin.note.controller;

import com.littlewin.common.core.Result;
import com.littlewin.common.log.annotation.Log;
import com.littlewin.common.log.enums.LogAction;
import com.littlewin.common.log.enums.LogModule;
import com.littlewin.common.utils.SecurityUtils;
import com.littlewin.note.domain.dto.AdminNotificationDTO;
import com.littlewin.note.service.AdminNotificationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin/notifications")
public class AdminNotificationController {

    private final AdminNotificationService adminNotificationService;

    @PostMapping("/send")
    @Log(module = LogModule.SYSTEM, action = LogAction.CREATE, desc = "发送系统公告")
    public Result<Void> send(@RequestBody @Valid AdminNotificationDTO dto) {
        Long senderId = SecurityUtils.getLoginUser().getUserId();
        adminNotificationService.sendNotification(senderId, dto);
        return Result.success(null);
    }
}
