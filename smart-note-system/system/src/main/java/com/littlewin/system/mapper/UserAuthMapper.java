package com.littlewin.system.mapper;

import com.littlewin.system.domain.entity.SysMenu;
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
}