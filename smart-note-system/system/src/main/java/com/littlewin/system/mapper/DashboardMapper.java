package com.littlewin.system.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
 * 仪表盘统计 Mapper（跨 sys_user / note 表聚合查询）
 */
@Mapper
public interface DashboardMapper {

    /** 总用户数 */
    Long countTotalUsers();

    /** 总笔记数（正常状态） */
    Long countTotalNotes();

    /** 今日新增用户 */
    Long countTodayNewUsers();

    /** 今日新增笔记 */
    Long countTodayNewNotes();

    /** 最近7天每天新增用户数，返回 [{date: "2026-04-23", count: 5}, ...] */
    List<Map<String, Object>> countNewUsersByDay();

    /** 最近7天每天新增笔记数 */
    List<Map<String, Object>> countNewNotesByDay();

    /** 笔记状态分布，返回 [{status: 0, count: 3}, ...] */
    List<Map<String, Object>> countNotesByStatus();

    /** 热门笔记 TOP5，返回 [{title: "xxx", view_count: 100}, ...] */
    List<Map<String, Object>> selectHotNotesTop5();
}
