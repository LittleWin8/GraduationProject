package com.littlewin.system.controller;

import com.littlewin.common.core.Result;
import com.littlewin.system.domain.dto.BehaviorLogDTO;
import com.littlewin.system.service.LogService;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

/**
 * 小程序端日志上报
 */
@RestController
@RequestMapping("/api/wx/log")
public class WxLogController {

    @Resource
    private LogService logService;

    /**
     * 上报行为日志
     */
    @PostMapping("/behavior")
    public Result reportBehavior(@RequestBody BehaviorLogDTO dto) {
        logService.reportBehavior(dto);
        return Result.success(null);
    }
}
