package com.littlewin.system.service;

import com.littlewin.system.domain.vo.DashboardStatsVO;

/**
 * 管理端仪表盘统计服务
 */
public interface AdminDashboardService {

    /**
     * 获取仪表盘统计数据
     */
    DashboardStatsVO getStats();
}
