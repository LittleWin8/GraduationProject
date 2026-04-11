package com.littlewin.system.service.impl;

import com.littlewin.common.enums.LogAction;
import com.littlewin.common.enums.LogStatus;
import com.littlewin.common.exception.ServiceException;
import com.littlewin.common.utils.JwtUtils;
import com.littlewin.common.utils.SecurityUtils;
import com.littlewin.system.domain.dto.AdminLoginDTO;
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

import java.util.*;
import java.util.stream.Collectors;

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
            UsernamePasswordAuthenticationToken authenticationToken =
                    new UsernamePasswordAuthenticationToken(username, password);
            authenticationManager.authenticate(authenticationToken);

            sysLogService.recordAuthLog(loginUser, LogStatus.SUCCESS, LogAction.LOGIN, "登录成功", null);

            return JwtUtils.createToken(username);
        } catch (AuthenticationException e) {
            sysLogService.recordAuthLog(loginUser, LogStatus.FAIL, LogAction.LOGIN, "登录失败", e.getMessage());
            throw new ServiceException("登录失败，用户名或密码错误");
        }
    }

    @Override
    public void logout() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth != null) {
            AdminLoginDTO loginUser = userAuthMapper.selectAdminLoginUser(auth.getName());
            if (loginUser != null) {
                sysLogService.recordAuthLog(loginUser, LogStatus.SUCCESS, LogAction.LOGOUT, "用户退出登录", null);
            }
        }
        SecurityContextHolder.clearContext();
    }

    /**
     * 菜单（动态路由）
     */
    @Override
    public List<MenuVO> getAuthMenuList() {
        String username = SecurityUtils.getUserId();
        AdminLoginDTO user = userAuthMapper.selectAdminLoginUser(username);
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
        String username = SecurityUtils.getUserId();
        AdminLoginDTO user = userAuthMapper.selectAdminLoginUser(username);
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