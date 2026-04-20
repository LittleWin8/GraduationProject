package com.littlewin.system.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.littlewin.common.core.Result;
import com.littlewin.system.domain.vo.MenuTreeVO;
import com.littlewin.system.domain.dto.RoleDTO;
import com.littlewin.system.domain.dto.RoleMenuDTO;
import com.littlewin.system.domain.entity.SysMenu;
import com.littlewin.system.domain.entity.SysRole;
import com.littlewin.system.mapper.SysMenuMapper;
import com.littlewin.system.mapper.SysRoleMenuMapper;
import com.littlewin.system.service.SysRoleMenuService;
import com.littlewin.system.service.SysRoleService;
import lombok.RequiredArgsConstructor;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("api/admin/sys/role")
@RequiredArgsConstructor
public class SysRoleController {

    private final SysRoleService roleService;
    private final SysRoleMenuService roleMenuService;
    private final SysRoleMenuMapper roleMenuMapper;
    private final SysMenuMapper menuMapper;

    /**
     * 1. 查询角色列表
     */
    @GetMapping("/list")
    public Result<List<SysRole>> list(SysRole role) {
        List<SysRole> list = roleService.selectRoleList(role);
        return Result.success(list);
    }

    /**
     * 2. 新增角色
     */
    @PostMapping
    public Result<?> add(@RequestBody RoleDTO roleDTO) {
        // 检查角色名称是否唯一
        if (!roleService.checkRoleNameUnique(roleDTO.getRoleName(), null)) {
            return Result.error("角色名称已存在");
        }
        // 检查角色权限字符是否唯一
        if (!roleService.checkRoleKeyUnique(roleDTO.getRoleKey(), null)) {
            return Result.error("角色权限字符已存在");
        }
        if (roleService.insertRole(roleDTO)) {
            return Result.success();
        }
        return Result.error("新增角色失败");
    }

    /**
     * 3. 修改角色
     */
    @PutMapping
    public Result<?> edit(@RequestBody RoleDTO roleDTO) {
        if (roleDTO.getRoleId() == null) {
            return Result.error("角色ID不能为空");
        }
        // 检查角色名称是否唯一
        if (!roleService.checkRoleNameUnique(roleDTO.getRoleName(), roleDTO.getRoleId())) {
            return Result.error("角色名称已存在");
        }
        // 检查角色权限字符是否唯一
        if (!roleService.checkRoleKeyUnique(roleDTO.getRoleKey(), roleDTO.getRoleId())) {
            return Result.error("角色权限字符已存在");
        }
        if (roleService.updateRole(roleDTO)) {
            return Result.success();
        }
        return Result.error("修改角色失败");
    }

    /**
     * 4. 删除角色
     */
    @DeleteMapping("/{roleId}")
    public Result<?> remove(@PathVariable("roleId") Long roleId) {
        if (roleService.deleteRoleById(roleId)) {
            return Result.success();
        }
        return Result.error("删除角色失败");
    }

    /**
     * 5. 批量删除角色
     */
    @DeleteMapping("/batch")
    public Result<?> removeBatch(@RequestBody List<Long> roleIds) {
        if (roleService.deleteRoleByIds(roleIds)) {
            return Result.success();
        }
        return Result.error("删除角色失败");
    }

    /**
     * 6. 查询角色详情
     */
    @GetMapping("/{roleId}")
    public Result<SysRole> getInfo(@PathVariable("roleId") Long roleId) {
        SysRole role = roleService.getById(roleId);
        return Result.success(role);
    }

    /**
     * 7. 修改角色状态
     */
    @PutMapping("/status")
    public Result<?> changeStatus(@RequestParam("roleId") Long roleId, @RequestParam("status") Integer status) {
        if (roleService.changeStatus(roleId, status)) {
            return Result.success();
        }
        return Result.error("修改状态失败");
    }

    /**
     * 8. 查询角色已分配菜单ID列表
     */
    @GetMapping("/menu/{roleId}")
    public Result<List<Long>> getRoleMenus(@PathVariable("roleId") Long roleId) {
        List<Long> menuIds = roleMenuService.selectMenuIdsByRoleId(roleId);
        return Result.success(menuIds);
    }

    /**
     * 9. 分配菜单权限给角色
     */
    @PutMapping("/menu")
    public Result<?> assignMenus(@RequestBody RoleMenuDTO roleMenuDTO) {
        if (roleMenuService.assignMenus(roleMenuDTO.getRoleId(), roleMenuDTO.getMenuIds())) {
            return Result.success();
        }
        return Result.error("分配菜单权限失败");
    }

    /**
     * 10. 获取全部菜单树
     */
    @GetMapping("/menu/tree")
    public Result<List<MenuTreeVO>> getMenuTree() {
        List<SysMenu> menuList = menuMapper.selectMenuTree();
        List<MenuTreeVO> tree = buildMenuTree(menuList, 0L);
        return Result.success(tree);
    }

    /**
     * 11. 获取角色按钮权限标识列表
     */
    @GetMapping("/perms/{roleId}")
    public Result<List<String>> getRolePerms(@PathVariable("roleId") Long roleId) {
        List<String> perms = menuMapper.selectPermsByRoleId(roleId);
        return Result.success(perms);
    }

    /**
     * 12. 获取所有角色列表（下拉选择用）
     */
    @GetMapping("/options")
    public Result<List<SysRole>> getRoleOptions() {
        LambdaQueryWrapper<SysRole> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysRole::getStatus, 1)
                .orderByAsc(SysRole::getSortOrder);
        List<SysRole> list = roleService.list(wrapper);
        return Result.success(list);
    }

    /**
     * 构建菜单树
     */
    private List<MenuTreeVO> buildMenuTree(List<SysMenu> menuList, Long parentId) {
        List<MenuTreeVO> treeList = new ArrayList<>();
        for (SysMenu menu : menuList) {
            if (menu.getParentId().equals(parentId)) {
                MenuTreeVO node = new MenuTreeVO(menu);
                node.setChildren(buildMenuTree(menuList, menu.getMenuId()));
                treeList.add(node);
            }
        }
        return treeList;
    }
}