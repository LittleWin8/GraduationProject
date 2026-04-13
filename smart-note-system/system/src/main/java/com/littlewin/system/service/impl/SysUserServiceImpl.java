package com.littlewin.system.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.littlewin.system.domain.dto.UserQueryDTO;
import com.littlewin.system.domain.vo.UserListVO;
import com.littlewin.system.mapper.SysUserMapper;
import com.littlewin.system.service.SysUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SysUserServiceImpl implements SysUserService {

    private final SysUserMapper sysUserMapper;

    @Override
    public IPage<UserListVO> getUserPageList(UserQueryDTO queryDTO) {
        // 创建 MyBatis-Plus 分页对象
        Page<UserListVO> page = new Page<>(queryDTO.getPageNum(), queryDTO.getPageSize());
        // 执行关联查询
        return sysUserMapper.selectUserPageList(page, queryDTO);
    }
}