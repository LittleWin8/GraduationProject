package com.littlewin.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.littlewin.system.domain.entity.SysRoleMenu;
import com.littlewin.system.mapper.SysRoleMenuMapper;
import com.littlewin.system.service.SysRoleMenuService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SysRoleMenuServiceImpl extends ServiceImpl<SysRoleMenuMapper, SysRoleMenu> implements SysRoleMenuService {

    private final SysRoleMenuMapper roleMenuMapper;

    @Override
    public List<Long> selectMenuIdsByRoleId(Long roleId) {
        return roleMenuMapper.selectMenuIdsByRoleId(roleId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean assignMenus(Long roleId, List<Long> menuIds) {
        // 删除原有关联
        LambdaQueryWrapper<SysRoleMenu> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysRoleMenu::getRoleId, roleId);
        remove(wrapper);

        // 新增关联
        if (menuIds != null && !menuIds.isEmpty()) {
            List<SysRoleMenu> list = new ArrayList<>();
            for (Long menuId : menuIds) {
                SysRoleMenu rm = new SysRoleMenu();
                rm.setRoleId(roleId);
                rm.setMenuId(menuId);
                list.add(rm);
            }
            return roleMenuMapper.batchInsert(list) > 0;
        }
        return true;
    }
}