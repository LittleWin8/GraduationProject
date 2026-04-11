package com.littlewin.system.mapper;

import com.littlewin.system.domain.entity.SysMenu;
import com.littlewin.system.domain.entity.SysUser;
import org.apache.ibatis.annotations.Mapper;
import com.littlewin.system.domain.dto.AdminLoginDTO;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface UserAuthMapper {

    /**
     * 查询登录用户信息（基础信息+密码）
     */
    AdminLoginDTO selectAdminLoginUser(String username);
    /**
     * 根据用户ID查询所有权限标识 (perms)
     */
    List<String> selectMenuPermsByUserId(@Param("userId") Long userId);

    /**
     * 根据用户ID查询所有菜单记录 (M目录和C菜单)
     */
    List<SysMenu> selectMenuListByUserId(@Param("userId") Long userId);
    List<SysMenu> selectButtonListByUserId(@Param("userId") Long userId);

    /**
     * 根据用户ID查询用户主表基本信息
     */
    SysUser selectSysUserById(@Param("userId") Long userId);

    /**
     * 根据用户ID查询该用户拥有的所有角色Key (如: ['admin', 'editor'])
     */
    List<String> selectRoleKeysByUserId(@Param("userId") Long userId);

    /**
     * 根据用户ID查询登录账号 (identifier)
     * * 限定类型为 'password'，确保拿的是主账号名
     */
    String selectIdentifierByUserId(@Param("userId") Long userId);
}