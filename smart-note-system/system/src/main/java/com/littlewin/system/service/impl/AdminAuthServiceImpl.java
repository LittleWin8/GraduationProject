package com.littlewin.system.service.impl;

import com.littlewin.common.constants.Constants;
import com.littlewin.common.enums.LogAction;
import com.littlewin.common.enums.LogStatus;
import com.littlewin.common.exception.ServiceException;
import com.littlewin.common.utils.JwtUtils;
import com.littlewin.common.utils.SecurityUtils;
import com.littlewin.system.domain.dto.AdminLoginDTO;
import com.littlewin.system.domain.dto.AdminLoginResponseDTO;
import com.littlewin.system.domain.entity.SysMenu;
import com.littlewin.system.domain.vo.MenuVO;
import com.littlewin.system.mapper.UserAuthMapper;
import com.littlewin.system.service.AdminAuthService;
import com.littlewin.system.service.SysLogService;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import jakarta.annotation.Resource;

import java.util.ArrayList;
import java.util.List;

@Service
public class AdminAuthServiceImpl implements AdminAuthService {

    @Resource
    private UserAuthMapper userAuthMapper;

    @Resource
    private SysLogService sysLogService;

    private final AuthenticationManager authenticationManager;

    public AdminAuthServiceImpl(AuthenticationManager authenticationManager) {
        this.authenticationManager = authenticationManager;
    }

    @Override
    public String login(String username, String password) {
        AdminLoginDTO loginUser = userAuthMapper.selectAdminLoginUser(username);
        try {
            // 1. Spring Security 认证
            UsernamePasswordAuthenticationToken authenticationToken =
                    new UsernamePasswordAuthenticationToken(username, password);
            authenticationManager.authenticate(authenticationToken);

            // 2. 登录成功日志
            sysLogService.recordAuthLog(loginUser, LogStatus.SUCCESS, LogAction.LOGIN, "登录成功", null);

            // 3. 返回 Token
            return JwtUtils.createToken(username);
        } catch (AuthenticationException e) {
            sysLogService.recordAuthLog(loginUser, LogStatus.FAIL, LogAction.LOGIN, "登录失败", e.getMessage());
            throw new ServiceException("登录失败，用户名或密码错误");
        }
    }

    @Override
    public AdminLoginResponseDTO getLoginUserData() {
        // 1. 从 Security 上下文获取当前用户名 (JwtFilter 已经解析并存入)
        String username = SecurityUtils.getUserId();
        AdminLoginDTO user = userAuthMapper.selectAdminLoginUser(username);

        if (user == null) throw new ServiceException("获取用户信息失败");

        // 2. 查询原始菜单数据
        List<SysMenu> allMenus = userAuthMapper.selectMenuListByUserId(user.getUserId());

        // 3. 构建适配 Geeker-Admin 的树形菜单
        List<MenuVO> menuTree = buildMenuTree(allMenus, 0L);

        // 4. 查询按钮权限列表
        List<String> authButtonList = userAuthMapper.selectMenuPermsByUserId(user.getUserId());

        // 5. 组装返回
        AdminLoginResponseDTO response = new AdminLoginResponseDTO();
        response.setMenuList(menuTree);
        response.setAuthButtonList(authButtonList);
        return response;
    }

    @Override
    public void logout() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth != null) {
            // 此时 auth.getName() 拿到的是 Token 解析出来的 identifier (如 "admin")
            // 直接使用 identifier 去查库记录日志，逻辑完全闭环
            AdminLoginDTO loginUser = userAuthMapper.selectAdminLoginUser(auth.getName());

            if (loginUser != null) {
                sysLogService.recordAuthLog(loginUser, LogStatus.SUCCESS, LogAction.LOGOUT, "用户退出登录", null);
            }
        }
        SecurityContextHolder.clearContext();
    }

    /**
     * 递归构建适配 Geeker-Admin 的树形菜单
     */
    private List<MenuVO> buildMenuTree(List<SysMenu> menus, Long parentId) {
        List<MenuVO> list = new ArrayList<>();
        for (SysMenu m : menus) {
            if (m.getParentId().equals(parentId)) {
                MenuVO vo = new MenuVO();
                vo.setPath(m.getPath());
                // 使用 perms 或 title 作为前端路由的唯一 name
                vo.setName(m.getPerms() != null && !m.getPerms().isEmpty() ? m.getPerms() : m.getTitle());
                vo.setComponent(m.getMenuType().equals("M") ? "Layout" : m.getComponent());

                MenuVO.MetaVO meta = new MenuVO.MetaVO();
                meta.setTitle(m.getTitle());
                meta.setIcon(m.getIcon());
                vo.setMeta(meta);

                vo.setChildren(buildMenuTree(menus, m.getMenuId()));
                list.add(vo);
            }
        }
        return list;
    }
}