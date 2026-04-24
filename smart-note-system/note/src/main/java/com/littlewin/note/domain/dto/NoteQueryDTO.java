package com.littlewin.note.domain.dto;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class NoteQueryDTO {
    // 分页参数
    private Integer pageNum = 1;
    private Integer pageSize = 10;

    // 搜索条件
    private String keyword;              // 标题或内容模糊搜索
    private Long categoryId;             // 分类ID
    private List<Long> tagIds;           // 标签ID列表（多选，AND逻辑）
    private LocalDateTime startTime;     // 开始时间（按创建时间）
    private LocalDateTime endTime;       // 结束时间

    // 排序
    private String orderBy = "updateTime";   // createTime, updateTime, viewCount
    private String orderDirection = "DESC";  // ASC, DESC

    // 我的笔记专用（赞过/收藏默认只查status=1）
    private Integer status;              // 0-草稿, 1-正常, 2-回收站
}