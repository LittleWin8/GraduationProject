package com.littlewin.system.controller;

import com.littlewin.common.core.Result;
import com.littlewin.system.domain.vo.DashboardStatsVO;
import com.littlewin.system.service.AdminDashboardService;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 管理端仪表盘统计
 */
@RestController
@RequestMapping("/api/admin/dashboard")
public class AdminDashboardController {

    @Resource
    private AdminDashboardService adminDashboardService;

    /**
     * 获取仪表盘统计数据
     */
    @GetMapping("/stats")
    public Result<DashboardStatsVO> getStats() {
        return Result.success(adminDashboardService.getStats());
    }
}
