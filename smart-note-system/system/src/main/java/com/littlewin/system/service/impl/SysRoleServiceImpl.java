package com.littlewin.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.littlewin.system.domain.dto.RoleDTO;
import com.littlewin.system.domain.entity.SysRole;
import com.littlewin.system.domain.entity.SysRoleMenu;
import com.littlewin.system.domain.entity.SysUserRole;
import com.littlewin.system.mapper.SysRoleMapper;
import com.littlewin.system.mapper.SysRoleMenuMapper;
import com.littlewin.system.mapper.SysUserRoleMapper;
import com.littlewin.system.service.SysRoleService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SysRoleServiceImpl extends ServiceImpl<SysRoleMapper, SysRole> implements SysRoleService {

    private final SysRoleMapper roleMapper;
    private final SysRoleMenuMapper roleMenuMapper;
    private final SysUserRoleMapper userRoleMapper;

    @Override
    public List<SysRole> selectRoleList(SysRole role) {
        LambdaQueryWrapper<SysRole> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(StringUtils.hasText(role.getRoleName()), SysRole::getRoleName, role.getRoleName())
                .like(StringUtils.hasText(role.getRoleKey()), SysRole::getRoleKey, role.getRoleKey())
                .eq(role.getStatus() != null, SysRole::getStatus, role.getStatus())
                .orderByAsc(SysRole::getSortOrder);
        return list(wrapper);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean insertRole(RoleDTO roleDTO) {
        SysRole role = new SysRole();
        role.setRoleName(roleDTO.getRoleName());
        role.setRoleKey(roleDTO.getRoleKey());
        role.setSortOrder(roleDTO.getSortOrder());
        role.setStatus(roleDTO.getStatus() != null ? roleDTO.getStatus() : 1);
        role.setCreateTime(LocalDateTime.now());

        boolean result = save(role);

        // 如果角色创建成功且有菜单权限，则分配菜单
        if (result && roleDTO.getMenuIds() != null && !roleDTO.getMenuIds().isEmpty()) {
            List<SysRoleMenu> roleMenus = new ArrayList<>();
            for (Long menuId : roleDTO.getMenuIds()) {
                SysRoleMenu rm = new SysRoleMenu();
                rm.setRoleId(role.getRoleId());
                rm.setMenuId(menuId);
                roleMenus.add(rm);
            }
            roleMenuMapper.batchInsert(roleMenus);
        }
        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateRole(RoleDTO roleDTO) {
        SysRole role = new SysRole();
        role.setRoleId(roleDTO.getRoleId());
        role.setRoleName(roleDTO.getRoleName());
        role.setRoleKey(roleDTO.getRoleKey());
        role.setSortOrder(roleDTO.getSortOrder());
        role.setStatus(roleDTO.getStatus());

        boolean result = updateById(role);

        // 更新菜单权限（先删后增）
        if (result && roleDTO.getMenuIds() != null) {
            // 删除原有菜单关联
            LambdaQueryWrapper<SysRoleMenu> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(SysRoleMenu::getRoleId, roleDTO.getRoleId());
            roleMenuMapper.delete(wrapper);

            // 新增菜单关联
            if (!roleDTO.getMenuIds().isEmpty()) {
                List<SysRoleMenu> roleMenus = new ArrayList<>();
                for (Long menuId : roleDTO.getMenuIds()) {
                    SysRoleMenu rm = new SysRoleMenu();
                    rm.setRoleId(roleDTO.getRoleId());
                    rm.setMenuId(menuId);
                    roleMenus.add(rm);
                }
                roleMenuMapper.batchInsert(roleMenus);
            }
        }
        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteRoleById(Long roleId) {
        // 删除角色菜单关联
        LambdaQueryWrapper<SysRoleMenu> rmWrapper = new LambdaQueryWrapper<>();
        rmWrapper.eq(SysRoleMenu::getRoleId, roleId);
        roleMenuMapper.delete(rmWrapper);

        // 删除用户角色关联
        LambdaQueryWrapper<SysUserRole> urWrapper = new LambdaQueryWrapper<>();
        urWrapper.eq(SysUserRole::getRoleId, roleId);
        userRoleMapper.delete(urWrapper);

        // 删除角色
        return removeById(roleId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteRoleByIds(List<Long> roleIds) {
        for (Long roleId : roleIds) {
            deleteRoleById(roleId);
        }
        return true;
    }

    @Override
    public boolean checkRoleNameUnique(String roleName, Long roleId) {
        LambdaQueryWrapper<SysRole> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysRole::getRoleName, roleName);
        if (roleId != null) {
            wrapper.ne(SysRole::getRoleId, roleId);
        }
        return count(wrapper) == 0;
    }

    @Override
    public boolean checkRoleKeyUnique(String roleKey, Long roleId) {
        int count = roleMapper.checkRoleKeyUnique(roleKey, roleId);
        return count == 0;
    }

    @Override
    public boolean changeStatus(Long roleId, Integer status) {
        LambdaUpdateWrapper<SysRole> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(SysRole::getRoleId, roleId)
                .set(SysRole::getStatus, status);
        return update(wrapper);
    }
}