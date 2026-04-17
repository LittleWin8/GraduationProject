package com.littlewin.system.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.littlewin.system.domain.entity.SysRole;
import com.littlewin.system.domain.dto.RoleDTO;
import java.util.List;

public interface SysRoleService extends IService<SysRole> {

    /**
     * 查询角色列表
     */
    List<SysRole> selectRoleList(SysRole role);

    /**
     * 新增角色
     */
    boolean insertRole(RoleDTO roleDTO);

    /**
     * 修改角色
     */
    boolean updateRole(RoleDTO roleDTO);

    /**
     * 删除角色
     */
    boolean deleteRoleById(Long roleId);

    /**
     * 批量删除角色
     */
    boolean deleteRoleByIds(List<Long> roleIds);

    /**
     * 检查角色名称是否唯一
     */
    boolean checkRoleNameUnique(String roleName, Long roleId);

    /**
     * 检查角色权限字符是否唯一
     */
    boolean checkRoleKeyUnique(String roleKey, Long roleId);

    /**
     * 修改角色状态
     */
    boolean changeStatus(Long roleId, Integer status);
}