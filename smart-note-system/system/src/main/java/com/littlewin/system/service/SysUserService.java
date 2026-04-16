package com.littlewin.system.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.littlewin.system.domain.dto.UserQueryDTO;
import com.littlewin.system.domain.dto.UserUpdateDTO;
import com.littlewin.system.domain.vo.UserDetailsVO;
import com.littlewin.system.domain.vo.UserListVO;

public interface SysUserService {
    /**
     * 分页查询用户账户列表
     */
    IPage<UserListVO> getUserPageList(UserQueryDTO queryDTO);

    /**
     * 获取用户详细信息
     */
    UserDetailsVO getUserDetails(Long userId);

    /**
     * 新增用户（多表关联）
     */
    void addUser(UserDetailsVO userDetailsVO);

    /**
     * 编辑用户（多表关联）
     */
    void updateUser(UserDetailsVO user);
}