package com.littlewin.system.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.littlewin.common.core.Result;
import com.littlewin.system.domain.dto.BehaviorLogQueryDTO;
import com.littlewin.system.domain.dto.OperationLogQueryDTO;
import com.littlewin.system.domain.vo.BehaviorLogVO;
import com.littlewin.system.domain.vo.OperationLogVO;
import com.littlewin.system.service.LogService;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 管理端日志查询
 */
@RestController
@RequestMapping("/api/admin/log")
public class AdminLogController {

    @Resource
    private LogService logService;

    /**
     * 操作日志分页列表
     */
    @GetMapping("/operation/list")
    public Result<IPage<OperationLogVO>> getOperationLogList(OperationLogQueryDTO query) {
        return Result.success(logService.getOperationLogPage(query));
    }

    /**
     * 行为日志分页列表
     */
    @GetMapping("/behavior/list")
    public Result<IPage<BehaviorLogVO>> getBehaviorLogList(BehaviorLogQueryDTO query) {
        return Result.success(logService.getBehaviorLogPage(query));
    }
}
