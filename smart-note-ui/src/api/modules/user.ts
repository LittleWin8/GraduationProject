import { ResPage, User } from "@/api/interface/index";
import { PORT1 } from "@/api/config/servicePort";
import http from "@/api";

/**
 * @name 用户管理模块 (对应后端 /api/admin/sys/user)
 */
// 1. 获取用户分页列表 (对接后端接口: GET /api/admin/sys/user/list)
export const getUserList = (params: User.ReqUserParams) => {
  // 注意：后端定义的是 GET，参数作为 query 传递
  return http.get<ResPage<User.ResUserList>>(`/admin/sys/user/list`, params, { loading: false });
};

// 2. 查看用户详情 (对接后端: GET /api/admin/sys/user/{userId})
export const getUserDetails = (userId: string | number) => {
  return http.get<User.ResUserList>(`/admin/sys/user/${userId}`);
};

// 2. 获取用户角色列表 (用于搜索栏下拉框，对接后端角色表查询)
export const getUserRole = () => {
  // 假设后端角色列表接口为 /api/admin/sys/role/list
  return http.get<User.ResRole[]>(PORT1 + `/admin/sys/role/list`);
};

// 3. 删除用户 (对接后端: DELETE /api/admin/sys/user/delete)
export const deleteUser = (ids: string[] | number[]) => {
  // 必须使用 { data: ids } 的形式，Axios 才会把数组放入请求体中
  return http.delete(`/admin/sys/user/delete`, ids, { data: ids });
};

// 4. 切换用户状态
export const changeUserStatus = (params: { userId: string; status: number }) => {
  return http.post(PORT1 + `/admin/sys/user/changeStatus`, params);
};

// 5. 重置用户密码
export const resetUserPassWord = (params: { userId: string | number }) => {
  return http.post(`/admin/sys/user/resetPassword`, params);
};

// 获取树形用户列表
export const getUserTreeList = (params: User.ReqUserParams) => {
  return http.post<ResPage<User.ResUserList>>(PORT1 + `/user/tree/list`, params);
};

// 新增用户
export const addUser = (params: { id: string }) => {
  return http.post(`/admin/sys/user/add`, params);
};

// 批量添加用户
export const BatchAddUser = (params: FormData) => {
  return http.post(PORT1 + `/user/import`, params);
};

// 编辑用户
export const editUser = (params: { id: string }) => {
  return http.put(`/admin/sys/user/edit`, params);
};

// // 删除用户
// export const deleteUser = (params: { id: string[] }) => {
//   return http.post(PORT1 + `/user/delete`, params);
// };

// // 切换用户状态
// export const changeUserStatus = (params: { id: string; status: number }) => {
//   return http.post(PORT1 + `/user/change`, params);
// };

// // 重置用户密码
// export const resetUserPassWord = (params: { id: string }) => {
//   return http.post(PORT1 + `/user/rest_password`, params);
// };

// 导出用户数据
export const exportUserInfo = (params: User.ReqUserParams) => {
  return http.download(PORT1 + `/user/export`, params);
};
