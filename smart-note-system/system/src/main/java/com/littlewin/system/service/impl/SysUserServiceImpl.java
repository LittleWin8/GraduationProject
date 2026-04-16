package com.littlewin.system.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.littlewin.common.utils.SecurityUtils;
import com.littlewin.system.domain.dto.AdminLoginDTO;
import com.littlewin.system.domain.dto.UserQueryDTO;
import com.littlewin.system.domain.dto.UserUpdateDTO;
import com.littlewin.system.domain.entity.SysRole;
import com.littlewin.system.domain.entity.SysUser;
import com.littlewin.system.domain.entity.SysUserRole;
import com.littlewin.system.domain.entity.UserInfo;
import com.littlewin.system.domain.vo.UserDetailsVO;
import com.littlewin.system.domain.vo.UserListVO;
import com.littlewin.system.mapper.*;
import com.littlewin.system.service.SysLogService;
import com.littlewin.system.service.SysUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SysUserServiceImpl implements SysUserService {

    private final SysUserMapper sysUserMapper;
    private final UserInfoMapper userInfoMapper;
    private final SysUserRoleMapper userRoleMapper; // 补全
    private final SysRoleMapper sysRoleMapper;      // 补全
    private final UserAuthMapper userAuthMapper;
    private final SysLogService sysLogService;

    /**
     * 分页获取用户列表
     */
    @Override
    public IPage<UserListVO> getUserPageList(UserQueryDTO queryDTO) {
        // 创建 MyBatis-Plus 分页对象
        Page<UserListVO> page = new Page<>(queryDTO.getPageNum(), queryDTO.getPageSize());
        // 执行关联查询
        return sysUserMapper.selectUserPageList(page, queryDTO);
    }

    /**
     * 查看用户详情
     */
    @Override
    public UserDetailsVO getUserDetails(Long userId) {
        // 1. 查询基础信息（含auth和info表聚合）
        UserDetailsVO details = sysUserMapper.selectUserDetailsById(userId);

        if (details != null) {
            // 2. 查询关联角色
            List<SysRole> roles = sysRoleMapper.selectRolesByUserId(userId);
            if (CollUtil.isNotEmpty(roles)) {
                // 设置 ID 列表（用于前端勾选框回显）
                details.setRoleIds(roles.stream().map(SysRole::getRoleId).collect(Collectors.toList()));
                // 设置名称列表（用于前端展示文本）
                details.setRoleNames(roles.stream().map(SysRole::getRoleName).collect(Collectors.toList()));
            }
        }
        return details;
    }

}