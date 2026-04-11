package com.littlewin.system.mapper;

import com.littlewin.system.domain.dto.UserQueryDTO;
import com.littlewin.system.domain.entity.SysUser;
import com.littlewin.system.domain.entity.UserInfo;
import com.littlewin.system.domain.vo.UserListVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface SysUserMapper {
    /**
     * 分页查询用户列表（关联 user_info, user_auth, sys_role）
     */
    List<UserListVO> selectUserList(UserQueryDTO query);

    /**
     * 更新 sys_user 表基础信息
     */
    int updateSysUser(SysUser user);

    /**
     * 更新 user_info 表详细信息
     */
    int updateUserInfo(UserInfo info);

    /**
     * 修改用户状态（用于启用、禁用、逻辑删除）
     */
    int updateUserStatus(@Param("userId") Long userId, @Param("status") Integer status);

    /**
     * 查询用户拥有的角色ID集合
     */
    List<Long> selectRoleIdsByUserId(Long userId);

    /**
     * 删除用户与角色的所有关联
     */
    int deleteUserRoles(Long userId);

    /**
     * 批量插入用户与角色的关联
     */
    int batchInsertUserRoles(@Param("userId") Long userId, @Param("roleIds") List<Long> roleIds);
}