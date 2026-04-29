package com.littlewin.system.service.impl;

import com.littlewin.system.domain.vo.DashboardStatsVO;
import com.littlewin.system.mapper.DashboardMapper;
import com.littlewin.system.service.AdminDashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 管理端仪表盘统计服务实现
 */
@Service
@RequiredArgsConstructor
public class AdminDashboardServiceImpl implements AdminDashboardService {

    private final DashboardMapper dashboardMapper;

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("MM-dd");
    private static final DateTimeFormatter FULL_DATE_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    /** 状态名称映射 */
    private static final Map<Integer, String> STATUS_NAME_MAP = new HashMap<>();

    static {
        STATUS_NAME_MAP.put(0, "草稿");
        STATUS_NAME_MAP.put(1, "正常");
        STATUS_NAME_MAP.put(2, "回收站");
        STATUS_NAME_MAP.put(3, "下架");
    }

    @Override
    public DashboardStatsVO getStats() {
        // 核心指标
        Long totalUsers = dashboardMapper.countTotalUsers();
        Long totalNotes = dashboardMapper.countTotalNotes();
        Long todayNewUsers = dashboardMapper.countTodayNewUsers();
        Long todayNewNotes = dashboardMapper.countTodayNewNotes();

        // 最近7天趋势
        List<Map<String, Object>> userTrend = dashboardMapper.countNewUsersByDay();
        List<Map<String, Object>> noteTrend = dashboardMapper.countNewNotesByDay();
        Map<String, Long> userTrendMap = toTrendMap(userTrend);
        Map<String, Long> noteTrendMap = toTrendMap(noteTrend);

        List<String> dateList = new ArrayList<>();
        List<Long> newUserList = new ArrayList<>();
        List<Long> newNoteList = new ArrayList<>();

        // 补齐7天数据
        LocalDate today = LocalDate.now();
        for (int i = 6; i >= 0; i--) {
            LocalDate date = today.minusDays(i);
            String fullDate = date.format(FULL_DATE_FMT);
            String shortDate = date.format(DATE_FMT);
            dateList.add(shortDate);
            newUserList.add(userTrendMap.getOrDefault(fullDate, 0L));
            newNoteList.add(noteTrendMap.getOrDefault(fullDate, 0L));
        }

        // 笔记状态分布
        List<Map<String, Object>> statusData = dashboardMapper.countNotesByStatus();
        List<DashboardStatsVO.StatusItem> statusDistribution = new ArrayList<>();
        for (Map<String, Object> item : statusData) {
            Integer status = ((Number) item.get("status")).intValue();
            Long count = ((Number) item.get("count")).longValue();
            statusDistribution.add(DashboardStatsVO.StatusItem.builder()
                    .name(STATUS_NAME_MAP.getOrDefault(status, "未知"))
                    .value(count)
                    .build());
        }

        // 热门笔记 TOP5
        List<Map<String, Object>> hotNotesData = dashboardMapper.selectHotNotesTop5();
        List<DashboardStatsVO.HotNoteItem> hotNotes = hotNotesData.stream()
                .map(item -> DashboardStatsVO.HotNoteItem.builder()
                        .title((String) item.get("title"))
                        .viewCount(((Number) item.get("viewCount")).longValue())
                        .build())
                .collect(Collectors.toList());

        return DashboardStatsVO.builder()
                .totalUsers(totalUsers)
                .totalNotes(totalNotes)
                .todayNewUsers(todayNewUsers)
                .todayNewNotes(todayNewNotes)
                .dateList(dateList)
                .newUserList(newUserList)
                .newNoteList(newNoteList)
                .statusDistribution(statusDistribution)
                .hotNotes(hotNotes)
                .build();
    }

    /** 将 Mapper 返回的趋势数据转为 Map<日期, 数量> */
    private Map<String, Long> toTrendMap(List<Map<String, Object>> trendData) {
        Map<String, Long> map = new LinkedHashMap<>();
        if (trendData != null) {
            for (Map<String, Object> item : trendData) {
                String date = item.get("date").toString();
                Long count = ((Number) item.get("count")).longValue();
                map.put(date, count);
            }
        }
        return map;
    }
}
