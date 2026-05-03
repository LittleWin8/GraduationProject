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

    private String name;

    private String path;

    private String component;

    private String redirect;

    private String menuType;

    private String title;

    private String icon;

    private String isLink;

    private Integer isHide;

    private Integer isFull;

    private Integer isAffix;

    private Integer isKeepAlive;

    private String activeMenu;

    private String perms;

    @TableField(exist = false)
    private String parentName;

    @TableField(exist = false)
    private String parentTitle;

    private Integer sortOrder;

    private LocalDateTime createTime;
}