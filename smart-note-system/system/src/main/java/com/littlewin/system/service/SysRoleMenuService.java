package com.littlewin.system.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.littlewin.system.domain.entity.SysRoleMenu;
import java.util.List;

public interface SysRoleMenuService extends IService<SysRoleMenu> {

    /**
     * 根据角色ID查询菜单ID列表
     */
    List<Long> selectMenuIdsByRoleId(Long roleId);

    /**
     * 分配菜单权限
     */
    boolean assignMenus(Long roleId, List<Long> menuIds);
}