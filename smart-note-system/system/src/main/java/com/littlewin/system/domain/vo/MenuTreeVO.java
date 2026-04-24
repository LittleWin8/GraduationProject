package com.littlewin.system.domain.vo;

import com.littlewin.common.core.TreeNode;
import com.littlewin.system.domain.entity.SysMenu;
import lombok.Data;
import java.util.List;

@Data
public class MenuTreeVO implements TreeNode<MenuTreeVO, Long> {
    private SysMenu menu;
    private List<MenuTreeVO> children;

    public MenuTreeVO(SysMenu menu) {
        this.menu = menu;
    }

    @Override
    public Long getId() {
        return menu != null ? menu.getMenuId() : null;
    }

    @Override
    public Long getParentId() {
        return menu != null ? menu.getParentId() : null;
    }
}