package com.littlewin.system.service.impl;

import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.littlewin.common.enums.LogAction;
import com.littlewin.common.enums.LogModule;
import com.littlewin.common.enums.LogStatus;
import com.littlewin.common.exception.ServiceException;
import com.littlewin.common.log.annotation.Log;
import com.littlewin.common.log.context.LogContext;
import com.littlewin.common.utils.PasswordUtils;
import com.littlewin.common.utils.SecurityUtils;
import com.littlewin.common.core.AdminLoginDTO;
import com.littlewin.system.domain.dto.UserQueryDTO;
import com.littlewin.system.domain.dto.UserUpdateDTO;
import com.littlewin.system.domain.entity.*;
import com.littlewin.system.domain.vo.UserDetailsVO;
import com.littlewin.system.domain.vo.UserListVO;
import com.littlewin.system.mapper.*;
import com.littlewin.system.service.SysUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

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
    @Log(module = LogModule.USER, action = LogAction.CREATE)
    @Transactional(rollbackFor = Exception.class)
    public void addUser(UserDetailsVO vo) {
        LogContext.setDesc("添加用户【" + vo.getIdentifier() + "】");

        // 1. 业务逻辑校验
        validateUserBefore(vo,false);

        // 2. 插入 sys_user 主表
        SysUser user = new SysUser();
        user.setNickname(vo.getNickname());
        user.setAvatar(vo.getAvatar());
        user.setStatus(vo.getStatus() != null ? vo.getStatus() : 1);
        user.setCreateTime(LocalDateTime.now());
        user.setUpdateTime(LocalDateTime.now());
        user.setDelFlag(0);
        int successUser = sysUserMapper.insert(user);
        Long userId = user.getUserId();

        // 绑定业务ID到日志
        LogContext.setBusinessId(userId);

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
    }

    @Override
    @Log(module = LogModule.USER, action = LogAction.UPDATE)
    @Transactional(rollbackFor = Exception.class)
    public void updateUser(UserDetailsVO vo) {

        // 设置动态描述
        LogContext.setDesc("修改用户【" + vo.getIdentifier() + "】个人信息");
        LogContext.setBusinessId(vo.getUserId());

        validateUserBefore(vo,true);

        Long userId = vo.getUserId();
        // 1. 更新主表
        SysUser user = new SysUser();
        BeanUtils.copyProperties(vo, user);
        user.setUpdateTime(LocalDateTime.now());
        sysUserMapper.updateById(user);

        // 2. 更新认证表 (修改账号名)
        userAuthMapper.updateIdentifierByUserId(userId, vo.getIdentifier());

        // 3. 更新详细信息表
        UserInfo info = new UserInfo();
        BeanUtils.copyProperties(vo, info);
        info.setUserId(userId);
        userInfoMapper.updateById(info);

        // 4. 更新角色 (先删再增)
        userRoleMapper.delete(new LambdaQueryWrapper<SysUserRole>()
                .eq(SysUserRole::getUserId, userId));
        if (!CollectionUtils.isEmpty(vo.getRoleIds())) {
            userRoleMapper.batchInsertUserRoles(userId, vo.getRoleIds());
        }

    }


    @Override
    @Log(module = LogModule.USER, action = LogAction.UPDATE, desc = "重置用户密码")
    @Transactional(rollbackFor = Exception.class)
    public void resetPassword(UserUpdateDTO updateDTO) {
        // 1. 在服务层判断 ID 是否为空
        if (updateDTO == null || updateDTO.getUserId() == null) {
            throw new ServiceException("用户ID不能为空");
        }

        Long userId = updateDTO.getUserId();
        LogContext.setBusinessId(userId); // 记录被重置的用户ID

        // 2. 检查用户是否存在且未被逻辑删除
        SysUser user = sysUserMapper.selectById(userId);
        if (user == null || (user.getDelFlag() != null && user.getDelFlag() == 1)) {
            throw new ServiceException("操作失败：该用户不存在或已被删除");
        }

        // 3. 执行更新：重置 credential 字段
        String defaultPassword = PasswordUtils.encodeDefaultPassword();
        LambdaUpdateWrapper<UserAuth> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(UserAuth::getUserId, userId)
                .eq(UserAuth::getAuthType, "password")
                .set(UserAuth::getCredential, defaultPassword);

        int rows = userAuthMapper.update(null, updateWrapper);
        if (rows == 0) {
            throw new ServiceException("该用户没有本地密码账号（可能是第三方登录），无法重置");
        }

    }

    /**
     * 删除用户
     */
    @Override
    @Log(module = LogModule.USER, action = LogAction.DELETE, desc = "逻辑删除用户")
    @Transactional(rollbackFor = Exception.class)
    public void deleteUser(Long userId) {
        if (userId == null) {
            throw new ServiceException("待删除的用户ID不能为空");
        }
        LogContext.setBusinessId(userId);

        // 1. 安全判断：防止管理员“误删自己”
        String currentAdminId = SecurityUtils.getUserId();
        AdminLoginDTO loginDTO = userAuthMapper.selectAdminLoginUser(currentAdminId);
        if (userId.toString().equals(loginDTO.getUserId().toString())) {
            throw new ServiceException("系统保护：无法删除当前登录中的管理员账号");
        }

        // 2. 执行逻辑删除（更新 del_flag 为 1）
        SysUser user = new SysUser();
        user.setUserId(userId);
        user.setDelFlag(1);
        user.setUpdateTime(LocalDateTime.now());

        int success = sysUserMapper.updateById(user);
        if (success == 0) {
            throw new ServiceException("删除失败，用户数据可能已被变动");
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void batchDeleteUsers(List<Long> ids) {
        if (CollUtil.isEmpty(ids)) {
            throw new ServiceException("请选择要删除的用户");
        }
        // 循环调用单条删除逻辑，复用“自杀检查”和日志记录
        for (Long id : ids) {
            deleteUser(id);
        }
    }

    /**
     * 内部校验逻辑
     */
    private void validateUserBefore(UserDetailsVO vo, boolean isUpdate) {

        // 检查新增的账户 identifier 字段是否为空
        if (!StringUtils.hasText(vo.getIdentifier())) {
            throw new ServiceException("账户名不可为空");
        }

        // 检查新增的账户 identifier 字段是否已经存在
        AdminLoginDTO existingUser = userAuthMapper.checkIdentifierUnique(vo.getIdentifier());
        if (existingUser != null) {
            // 如果是更新操作：查到的用户 ID 不等于 当前编辑的用户 ID，说明撞名了
            if (isUpdate && !existingUser.getUserId().toString().equals(vo.getUserId().toString())) {
                throw new ServiceException("账号名 " + vo.getIdentifier() + " 已被其他用户占用");
            }
            // 如果是新增操作：只要查到数据，就说明已存在
            if (!isUpdate) {
                throw new ServiceException("账号名 " + vo.getIdentifier() + " 已存在");
            }
        }
    }

}