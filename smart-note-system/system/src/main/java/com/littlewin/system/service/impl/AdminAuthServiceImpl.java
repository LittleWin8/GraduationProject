package com.littlewin.system.service.impl;

import com.littlewin.common.enums.LogAction;
import com.littlewin.common.enums.LogModule;
import com.littlewin.common.exception.ServiceException;
import com.littlewin.common.log.annotation.Log;
import com.littlewin.common.log.context.LogContext;
import com.littlewin.common.utils.JwtUtils;
import com.littlewin.common.utils.PasswordUtils;
import com.littlewin.common.utils.SecurityUtils;
import com.littlewin.common.utils.ServletUtils;
import com.littlewin.common.core.LoginDTO;
import com.littlewin.system.domain.dto.SecurityUpdateDTO;
import com.littlewin.system.domain.entity.SysMenu;
import com.littlewin.system.domain.entity.UserAuth;
import com.littlewin.system.domain.entity.UserInfo;
import com.littlewin.system.domain.vo.MenuVO;
import com.littlewin.system.domain.vo.UserInfoVO;
import com.littlewin.system.mapper.UserAuthMapper;
import com.littlewin.system.mapper.UserInfoMapper;
import com.littlewin.system.service.AdminAuthService;
import org.springframework.security.authentication.*;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;

import jakarta.annotation.Resource;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class AdminAuthServiceImpl implements AdminAuthService {

    @Resource
    private UserAuthMapper userAuthMapper;
    @Resource
    private UserInfoMapper userInfoMapper;

    private final AuthenticationManager authenticationManager;

    public AdminAuthServiceImpl(AuthenticationManager authenticationManager) {
        this.authenticationManager = authenticationManager;
    }

    @Override
    @Log(module = LogModule.AUTH, action = LogAction.LOGIN)
    public String login(String username, String password) {


        LogContext.setDesc("用户登录：" + username);

        try {
            UsernamePasswordAuthenticationToken authenticationToken =
                    new UsernamePasswordAuthenticationToken(username, password);
            authenticationManager.authenticate(authenticationToken);

            LoginDTO loginUser = userAuthMapper.selectAdminLoginUser(username);

            // 往用户信息表插入最后登录ip和登录时间
            String ip = ServletUtils.getClientIp();
            userAuthMapper.updateLoginInfo(loginUser.getUserId(), ip, LocalDateTime.now());

            // 记录登录日志
            LogContext.setBusinessId(loginUser.getUserId());
            LogContext.setUsername(loginUser.getUsername());
            LogContext.setDesc("登录成功【" + username + "】");

            return JwtUtils.createToken(username);
        } catch (AuthenticationException e) {
            String msg;
            if (e instanceof BadCredentialsException) {
                msg = "用户密码错误";
            } else if (e instanceof DisabledException) {
                msg = "您的账号已被禁用，请联系管理员";
            } else if (e instanceof AccountExpiredException) {
                msg = "您的账号已注销";
            } else if (e instanceof UsernameNotFoundException) {
                msg = e.getMessage();
            } else {
                msg = "登录失败：" + e.getMessage();
            }

            // 记录失败日志并抛出自定义异常给前端
            LogContext.setUsername(username);
            throw new ServiceException(msg);
        }
    }

    @Override
    @Log(module = LogModule.AUTH, action = LogAction.LOGOUT, desc = "用户退出登录")
    public void logout() {
        LoginDTO loginUser = SecurityUtils.getLoginUser();
        if (loginUser != null) {
            LogContext.setBusinessId(loginUser.getUserId());
            LogContext.setDesc("退出登录【" + loginUser.getUsername()+ "】");
        }
    }


    @Override
    public UserInfoVO getUserInfo() {
        // 1. 获取当前登录用户
        LoginDTO authUser = SecurityUtils.getLoginUser();
        if (authUser == null) throw new ServiceException("用户不存在");

        // 2. 优化：1次联查获取 3 表数据
        UserInfoVO userInfo = userAuthMapper.selectFullUserInfoById(authUser.getUserId());
        if (userInfo == null) throw new ServiceException("获取详细资料失败");

        // 3. 角色列表是 1:N 关系，保留独立查询以避免 SQL 结果集膨胀
        List<String> roles = userAuthMapper.selectRoleKeysByUserId(authUser.getUserId());
        userInfo.setRoles(roles);

        return userInfo;
    }

    @Log(module = LogModule.AUTH, action = LogAction.UPDATE)
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateSecurityInfo(SecurityUpdateDTO dto) {
        // 假设通过工具类获取当前登录用户的 ID
        LoginDTO loginDTO = SecurityUtils.getLoginUser();

        LogContext.setBusinessId(loginDTO.getUserId());
        LogContext.setDesc("修改密码");

        if (Integer.valueOf(1).equals(dto.getType())) {
            // --- 逻辑 A：修改密码 ---
            // 1. 查询该用户类型为 'password' 的认证记录
            UserAuth auth = userAuthMapper.selectOne(new LambdaQueryWrapper<UserAuth>()
                    .eq(UserAuth::getUserId, loginDTO.getUserId())
                    .eq(UserAuth::getAuthType, "password"));

            if (auth == null) throw new ServiceException("未找到密码认证记录");

            // 2. 校验旧密码（oldField 存的是原密码）
            if (!PasswordUtils.matches(dto.getOldField(), auth.getCredential())) {
                throw new ServiceException("原密码输入错误");
            }

            // 3. 加密新密码（newField 存的是新密码）并保存
            auth.setCredential(PasswordUtils.encode(dto.getNewField()));
            userAuthMapper.updateById(auth);

        } else if (Integer.valueOf(2).equals(dto.getType())) {
            // --- 逻辑 B：更换手机号 ---
            String newPhone = dto.getNewField();
            String code = dto.getOldField();

            LogContext.setBusinessId(loginDTO.getUserId());
            LogContext.setDesc("修改绑定手机号");

            // 1. 校验验证码 (暂时不打算实现)
            // if (!smsService.verify(newPhone, code)) throw new RuntimeException("验证码无效");

            // 2. 检查新手机号是否被其他人占用
            boolean exists = userInfoMapper.exists(new LambdaQueryWrapper<UserInfo>()
                    .eq(UserInfo::getPhone, newPhone)
                    .ne(UserInfo::getUserId, loginDTO.getUserId()));
            if (exists) throw new ServiceException("该手机号已被其他账号绑定");

            // 3. 更新 user_info 表
            UserInfo info = userInfoMapper.selectById(loginDTO.getUserId());
            if (info == null) {
                // 如果该用户在 user_info 中尚无记录，则新增
                info = new UserInfo();
                info.setUserId(loginDTO.getUserId());
                info.setPhone(newPhone);
                userInfoMapper.insert(info);
            } else {
                info.setPhone(newPhone);
                userInfoMapper.updateById(info);
            }

        } else {
            throw new ServiceException("不支持的修改类型");
        }
    }

    /**
     * 菜单（动态路由）
     */
    @Override
    public List<MenuVO> getAuthMenuList() {
        LoginDTO user = SecurityUtils.getLoginUser();
        if (user == null) throw new ServiceException("获取用户信息失败");

        // 只查询类型为 M(目录) 和 C(菜单) 的数据
        List<SysMenu> menus = userAuthMapper.selectMenuListByUserId(user.getUserId());
        return buildMenuTree(menus, 0L);
    }

    /**
     * 获取按钮权限列表
     * 适配 Geeker-Admin 数据格式: { "authButton": [...], "useProTable": [...] }
     */
    @Override
    public Map<String, List<String>> getAuthButtonList() {
        LoginDTO user = SecurityUtils.getLoginUser();
        if (user == null) return new HashMap<>();

        // 1. 从数据库获取该用户拥有的所有按钮级权限 (menu_type = 'F')
        List<SysMenu> buttons = userAuthMapper.selectButtonListByUserId(user.getUserId());

        // 2. 提取所有非空的 perms 标识符
        List<String> allPerms = buttons.stream()
                .map(SysMenu::getPerms)
                .filter(Objects::nonNull)
                .filter(p -> !p.isEmpty())
                .distinct()
                .collect(Collectors.toList());

        Map<String, List<String>> result = new HashMap<>();

        // 3. 组装 authButton: 包含页面上所有基础操作权限 (如 add, edit, delete, export 以及自定义 perms)
        result.put("authButton", allPerms);

        // 4. 组装 useProTable: 专门过滤出 ProTable 组件识别的增强功能权限
        // 这些字符串需要与 SQL 中的 perms 字段严格对应
        List<String> proTableKeys = List.of("add", "batchAdd", "export", "batchDelete", "status");
        List<String> useProTablePerms = allPerms.stream()
                .filter(proTableKeys::contains)
                .collect(Collectors.toList());

        result.put("useProTable", useProTablePerms);

        return result;
    }

    /**
     * 递归构建菜单树
     */
    private List<MenuVO> buildMenuTree(List<SysMenu> menus, Long parentId) {
        List<MenuVO> list = new ArrayList<>();
        for (SysMenu m : menus) {
            if (m.getParentId().equals(parentId)) {
                MenuVO vo = new MenuVO();
                vo.setPath(m.getPath());
                vo.setName(m.getName());
                vo.setComponent(m.getComponent());
                vo.setRedirect(m.getRedirect());

                // 填充前端 Meta 信息
                MenuVO.MetaVO meta = new MenuVO.MetaVO();
                meta.setTitle(m.getTitle());
                meta.setIcon(m.getIcon());
                meta.setIsLink(m.getIsLink() == null ? "" : m.getIsLink());
                meta.setIsHide(m.getIsHide() != null && m.getIsHide() == 1);
                meta.setIsFull(m.getIsFull() != null && m.getIsFull() == 1);
                meta.setIsAffix(m.getIsAffix() != null && m.getIsAffix() == 1);
                meta.setIsKeepAlive(m.getIsKeepAlive() != null && m.getIsKeepAlive() == 1);
                meta.setActiveMenu(m.getActiveMenu());

                vo.setMeta(meta);

                // 递归查找子菜单
                List<MenuVO> children = buildMenuTree(menus, m.getMenuId());
                if (!children.isEmpty()) {
                    vo.setChildren(children);
                }
                list.add(vo);
            }
        }
        return list;
    }
}