package com.littlewin.note.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 系统预设分类实体类
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("sys_category")
public class SysCategory {

    @TableId(value = "category_id", type = IdType.AUTO)
    private Long categoryId;

    /**
     * 分类名称
     */
    private String name;

    /**
     * 父分类ID
     */
    private Long parentId;

    /**
     * 排序
     */
    private Integer sortOrder;

    /**
     * 状态：1 启用, 0 禁用
     */
    private Integer status;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;
}