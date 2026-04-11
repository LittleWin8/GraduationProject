import { Login } from "@/api/interface/index";
import http from "@/api";

/**
 * @name 登录模块
 */
// 用户登录
export const loginApi = (params: Login.ReqLoginForm) => {
  // 对应后端 @PostMapping("/login")，路径补全为 /api/admin/auth/login
  // 注意：如果你的 http 实例没有配置 base_url 包含 /api，请手动补全
  return http.post<Login.ResLogin>(`/admin/auth/login`, params, { loading: false });
};

// 获取当前登录用户信息
export const getUserInfoApi = () => {
  // 对应后端 @GetMapping("/getUserInfo")
  return http.get<Login.ResUserInfo>(`/admin/auth/getUserInfo`, {}, { loading: false });
};

// 获取菜单列表 (动态路由)
export const getAuthMenuListApi = () => {
  // 对应后端 @GetMapping("/getAuthMenuList")
  return http.get<Menu.MenuOptions[]>(`/admin/auth/getAuthMenuList`, {}, { loading: false });
};

// 获取按钮权限
export const getAuthButtonListApi = () => {
  // 对应后端 @GetMapping("/getAuthButtonList")
  // 后端返回：{ authButton: [], useProTable: [] }，完全适配 Geeker 结构
  return http.get<Login.ResAuthButtons>(`/admin/auth/getAuthButtonList`, {}, { loading: false });
};

// 用户退出登录
export const logoutApi = () => {
  // 对应后端 @PostMapping("/logout")
  return http.post(`/admin/auth/logout`);
};
