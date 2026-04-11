package com.littlewin.system.domain.vo;

import lombok.Data;
import java.util.List;

/**
 * 适配 Geeker-Admin 的路由菜单 VO
 */
@Data
public class MenuVO {
    private String path;
    private String name;
    private String component;
    private String redirect;
    private MetaVO meta;
    private List<MenuVO> children;

    @Data
    public static class MetaVO {
        private String title;
        private String icon;
        private String isLink = "";
        private Boolean isHide = false;
        private Boolean isFull = false;
        private Boolean isAffix = false;
        private Boolean isKeepAlive = true;
        private String activeMenu;
    }
}