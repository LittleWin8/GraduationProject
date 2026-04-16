package com.littlewin.system.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.littlewin.common.enums.LogAction;
import com.littlewin.common.enums.LogStatus;
import com.littlewin.common.exception.ServiceException;
import com.littlewin.common.utils.PasswordUtils;
import com.littlewin.common.utils.SecurityUtils;
import com.littlewin.system.domain.dto.AdminLoginDTO;
import com.littlewin.system.domain.dto.UserQueryDTO;
import com.littlewin.system.domain.dto.UserUpdateDTO;
import com.littlewin.system.domain.entity.*;
import com.littlewin.system.domain.vo.UserDetailsVO;
import com.littlewin.system.domain.vo.UserListVO;
import com.littlewin.system.mapper.*;
import com.littlewin.system.service.SysLogService;
import com.littlewin.system.service.SysUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.security.Security;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SysUserServiceImpl implements SysUserService {

    private final SysUserMapper sysUserMapper;
    private final UserInfoMapper userInfoMapper;
    private final SysUserRoleMapper userRoleMapper;
    private final SysRoleMapper sysRoleMapper;
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

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void addUser(UserDetailsVO vo) {
        // 1. 业务逻辑校验
        validateUserBeforeAdd(vo);

        // 2. 插入 sys_user 主表
        SysUser user = new SysUser();
        user.setNickname(vo.getNickname());
        user.setAvatar(vo.getAvatar());
        user.setStatus(vo.getStatus() != null ? vo.getStatus() : 1);
        user.setCreateTime(LocalDateTime.now());
        user.setUpdateTime(LocalDateTime.now());
        user.setDel_flag(0);
        int successUser = sysUserMapper.insert(user);
        Long userId = user.getUserId();

        // 3. 插入 user_auth 认证表
        UserAuth auth = new UserAuth();
        auth.setUserId(userId);
        auth.setAuthType(StringUtils.hasText(vo.getAuthType()) ? vo.getAuthType() : "password");
        auth.setIdentifier(vo.getIdentifier());
        auth.setCredential(PasswordUtils.encodeDefaultPassword());          // 设置加密密码：123456
        auth.setCreateTime(LocalDateTime.now());
        int successUseAuth = userAuthMapper.insert(auth);

        // 4. 插入 user_info 详细信息表
        UserInfo info = new UserInfo();
        info.setUserId(userId);
        info.setGender(vo.getGender());
        info.setPhone(vo.getPhone());
        info.setEmail(vo.getEmail());
        info.setBirthday(vo.getBirthday());
        info.setCity(vo.getCity());
        info.setSignature(vo.getSignature());
        int successUserInfo = userInfoMapper.insert(info);
        // 5. 插入 sys_user_role 角色关联表
        if (!CollectionUtils.isEmpty(vo.getRoleIds())) {
            userRoleMapper.batchInsertUserRoles(userId, vo.getRoleIds());
        }

        String userIdStr = SecurityUtils.getUserId();
        AdminLoginDTO loginDTO = userAuthMapper.selectAdminLoginUser(userIdStr);
        if (successUser == 1
                && successUserInfo == 1
                && successUseAuth == 1) {
            String desc = "添加用户【" + vo.getIdentifier() + "】成功";
            sysLogService.recordUserLog(loginDTO, userId, LogAction.CREATE,
                    LogStatus.SUCCESS, desc, null);
        }else {
            String desc = "添加用户【" + vo.getIdentifier() + "】失败";
            sysLogService.recordUserLog(loginDTO, userId, LogAction.CREATE,
                    LogStatus.FAIL, desc, null);
        }

    }

    @Override
    public void updateUser(UserDetailsVO user) {

    }

    /**
     * 内部校验逻辑
     */
    private void validateUserBeforeAdd(UserDetailsVO vo) {

        // 检查新增的账户 identifier 字段是否为空
        if (!StringUtils.hasText(vo.getIdentifier())) {
            throw new ServiceException("新增账户账户名不可为空");
        }

        // 检查新增的账户 identifier 字段是否已经存在
        AdminLoginDTO adminLoginDTO = userAuthMapper.checkIdentifierUnique(vo.getIdentifier());
        if (adminLoginDTO != null) {
            throw new ServiceException("账号 '" + vo.getIdentifier() + "' 已存在");
        }
    }

}