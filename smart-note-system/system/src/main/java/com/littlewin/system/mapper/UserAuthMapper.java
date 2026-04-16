package com.littlewin.system.mapper;

import com.littlewin.system.domain.entity.SysMenu;
import org.apache.ibatis.annotations.Mapper;
import com.littlewin.system.domain.dto.AdminLoginDTO;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

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
     * 根据用户ID查询该用户拥有的所有角色Key (如: ['admin', 'editor'])
     */
    List<String> selectRoleKeysByUserId(@Param("userId") Long userId);

    /**
     * 联查获取用户所有详细信息（User + Info + Auth）
     */
    Map<String, Object> selectFullUserInfoById(@Param("userId") Long userId);

    /**
     * 更新用户最后登录信息
     */
    int updateLoginInfo(@Param("userId") Long userId,
                        @Param("ip") String ip,
                        @Param("loginTime") LocalDateTime loginTime);
}