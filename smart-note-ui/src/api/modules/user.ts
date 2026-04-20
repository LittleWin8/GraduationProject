import { ResPage, User } from "@/api/interface/index";
import http from "@/api";

/**
 * @name 用户管理模块 (对应后端 /api/admin/sys/user)
 */

// 1. 获取用户分页列表
export const getUserList = (params: User.ReqUserParams) => {
  return http.get<ResPage<User.UserListVO>>(`/admin/sys/user/list`, params, { loading: false });
};

// 2. 查看用户详情
export const getUserDetails = (userId: number) => {
  return http.get<User.UserDetailsVO>(`/admin/sys/user/${userId}`);
};

// 3. 新增用户
export const addUser = (data: User.UserDetailsVO) => {
  return http.post(`/admin/sys/user/add`, data);
};

// 4. 修改用户
export const editUser = (data: User.UserDetailsVO) => {
  return http.put(`/admin/sys/user/edit`, data);
};

// 5. 重置密码
export const resetUserPassword = (data: User.UserUpdateDTO) => {
  return http.post(`/admin/sys/user/resetPassword`, data);
};

// 6. 批量删除用户
export const deleteUser = (ids: number[]) => {
  return http.delete(`/admin/sys/user/delete`, {
    data: ids
  });
};
