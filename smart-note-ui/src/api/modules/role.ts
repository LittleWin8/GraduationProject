import { ResPage, Role } from "@/api/interface/index";
import http from "@/api";

/**
 * @name 角色管理模块
 * @baseUrl /sys/role
 */

// 1. 查询角色列表
export const getRoleList = (params: Role.ReqRoleParams) => {
  return http.get<ResPage<Role.SysRole>>(`/admin/sys/role/list`, params, { loading: false });
};

// 2. 新增角色
export const addRole = (data: Role.RoleDTO) => {
  return http.post(`/admin/sys/role`, data);
};

// 3. 修改角色
export const editRole = (data: Role.RoleDTO) => {
  return http.put(`/admin/sys/role`, data);
};

// 4. 删除角色
export const deleteRole = (roleId: number) => {
  return http.delete(`/admin/sys/role/${roleId}`);
};

// 5. 批量删除角色
export const deleteRoles = (roleIds: number[]) => {
  return http.delete(`/admin/sys/role/batch`, {
    data: roleIds
  });
};

// 6. 查询角色详情
export const getRoleDetail = (roleId: number) => {
  return http.get<Role.SysRole>(`/admin/sys/role/${roleId}`);
};

// 7. 修改角色状态
export const changeRoleStatus = (roleId: number, status: number) => {
  return http.put(`/admin/sys/role/status`, undefined, {
    params: { roleId, status }
  });
};

// 8. 查询角色已分配菜单ID列表
export const getRoleMenus = (roleId: number) => {
  return http.get<number[]>(`/admin/sys/role/menu/${roleId}`);
};

// 9. 分配菜单权限给角色
export const assignMenus = (data: Role.RoleMenuDTO) => {
  return http.put(`/admin/sys/role/menu`, data);
};

// 10. 获取全部菜单树
export const getMenuTree = () => {
  return http.get<Role.MenuTreeVO[]>(`/admin/sys/role/menu/tree`);
};

// 11. 获取角色按钮权限标识列表
export const getRolePerms = (roleId: number) => {
  return http.get<string[]>(`/admin/sys/role/perms/${roleId}`);
};

// 12. 获取所有角色列表（下拉）
export const getRoleOptions = () => {
  return http.get<Role.SysRole[]>(`/admin/sys/role/options`);
};
