package com.littlewin.system.domain.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@TableName("sys_menu")
public class SysMenu implements Serializable {

    @TableId(value = "menu_id", type = IdType.AUTO)
    private Long menuId;

    private Long parentId;

    private String title;

    private String path;

    private String component;

    private String perms;

    private String icon;

    private String menuType;

    private Integer sortOrder;

    private LocalDateTime createTime;
}