package com.littlewin.note.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.littlewin.common.core.TreeNode;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 系统预设分类实体类
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("sys_category")
public class SysCategory implements TreeNode<SysCategory, Long> {

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

    /**
     * 子分类列表（由 TreeUtils 构建时填充）
     */
    @TableField(exist = false)
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private List<SysCategory> children;

    // --- 实现 TreeNode 接口方法 ---

    @Override
    public Long getId() {
        return this.categoryId;
    }

    @Override
    public Long getParentId() {
        return this.parentId;
    }

    @Override
    public List<SysCategory> getChildren() {
        return this.children;
    }

    @Override
    public void setChildren(List<SysCategory> children) {
        this.children = children;
    }
}