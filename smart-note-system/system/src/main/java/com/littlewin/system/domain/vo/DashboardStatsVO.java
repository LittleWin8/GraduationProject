package com.littlewin.system.domain.vo;

import lombok.Builder;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 仪表盘统计数据 VO
 */
@Data
@Builder
public class DashboardStatsVO implements Serializable {
    private static final long serialVersionUID = 1L;

    /** 总用户数 */
    private Long totalUsers;

    /** 总笔记数（正常状态） */
    private Long totalNotes;

    /** 今日新增用户 */
    private Long todayNewUsers;

    /** 今日新增笔记 */
    private Long todayNewNotes;

    /** 最近7天日期列表 */
    private List<String> dateList;

    /** 每天新增用户数 */
    private List<Long> newUserList;

    /** 每天新增笔记数 */
    private List<Long> newNoteList;

    /** 笔记状态分布 */
    private List<StatusItem> statusDistribution;

    /** 热门笔记 TOP5 */
    private List<HotNoteItem> hotNotes;

    /** 状态分布项 */
    @Data
    @Builder
    public static class StatusItem implements Serializable {
        private static final long serialVersionUID = 1L;
        private String name;
        private Long value;
    }

    /** 热门笔记项 */
    @Data
    @Builder
    public static class HotNoteItem implements Serializable {
        private static final long serialVersionUID = 1L;
        private String title;
        private Long viewCount;
    }
}
