package com.littlewin.system.domain.vo;

import com.littlewin.system.domain.entity.SysMenu;
import lombok.Data;
import java.util.List;

@Data
public class MenuTreeVO {
    private SysMenu menu;
    private List<MenuTreeVO> children;

    public MenuTreeVO(SysMenu menu) {
        this.menu = menu;
    }
}