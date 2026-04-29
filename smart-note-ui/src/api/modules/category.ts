import http from "@/api";

/**
 * @name 分类管理模块 (对应后端 /api/admin/categories)
 */

/** 分类树节点 */
export interface CategoryTreeNode {
  categoryId: number;
  name: string;
  parentId: number;
  sortOrder: number;
  status: number;
  children?: CategoryTreeNode[];
}

/** 分类表单数据 */
export interface CategoryFormData {
  name: string;
  parentId?: number;
  sortOrder?: number;
}

/** 分类列表项（扁平） */
export interface CategoryListItem {
  categoryId: number;
  name: string;
  parentId: number;
  sortOrder: number;
  status: number;
  createTime: string;
}

// 获取分类树（无参数，返回树形结构，供 TreeFilter）
export const getCategoryTree = () => {
  return http.get<CategoryTreeNode[]>(`/admin/categories/list`);
};

// 获取分类扁平列表（带 parentId，供 ProTable）
export const getCategoryList = (params?: { parentId?: number }) => {
  return http.get<CategoryListItem[]>(`/admin/categories/list`, params || {});
};

// 新增分类
export const addCategory = (data: CategoryFormData) => {
  return http.post(`/admin/categories`, data);
};

// 修改分类
export const updateCategory = (id: number, data: CategoryFormData) => {
  return http.put(`/admin/categories/${id}`, data);
};

// 删除分类
export const deleteCategory = (id: number) => {
  return http.delete(`/admin/categories/${id}`);
};

// 切换分类状态（启用/禁用）
export const toggleCategoryStatus = (id: number) => {
  return http.put(`/admin/categories/${id}/status`);
};
