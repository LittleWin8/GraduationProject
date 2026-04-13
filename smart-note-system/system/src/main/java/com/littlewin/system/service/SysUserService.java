package com.littlewin.system.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.littlewin.system.domain.dto.UserQueryDTO;
import com.littlewin.system.domain.vo.UserListVO;

public interface SysUserService {
    /**
     * 分页查询用户账户列表
     */
    IPage<UserListVO> getUserPageList(UserQueryDTO queryDTO);
}