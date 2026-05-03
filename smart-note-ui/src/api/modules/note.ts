import http from "@/api";

/**
 * @name 笔记管理模块 (对应后端 /api/admin/notes)
 */

/** 笔记查询参数 */
export interface ReqNoteParams {
  pageNum?: number;
  pageSize?: number;
  status?: number;
  categoryId?: number;
  keyword?: string;
  userId?: number;
  startTime?: string;
  endTime?: string;
  reviewed?: number;
}

/** 笔记列表项 */
export interface NoteListVO {
  noteId: number;
  title: string;
  summary: string;
  status: number;
  isPublic: number;
  viewCount: number;
  likeCount: number;
  commentCount: number;
  reviewed: number;
  userId: number;
  author: string;
  avatar: string;
  categoryId: number;
  categoryName: string;
  createTime: string;
  updateTime: string;
}

/** 笔记详情 */
export interface NoteDetailVO {
  noteId: number;
  userId: number;
  title: string;
  content: string;
  author: string;
  avatar: string;
  isPublic: number;
  categoryId: number;
  likes: number;
  comments: number;
  updateTime: string;
}

/** 分类树节点 */
export interface CategoryTreeNode {
  categoryId: number;
  name: string;
  parentId: number;
  sortOrder: number;
  status: number;
  children?: CategoryTreeNode[];
}

// 获取笔记分页列表
export const getNoteList = (params: ReqNoteParams) => {
  return http.get(`/admin/notes/list`, params, { loading: false });
};

// 获取笔记详情
export const getNoteDetail = (noteId: number) => {
  return http.get<NoteDetailVO>(`/admin/notes/${noteId}`);
};

// 审核笔记（上架/下架）
export const auditNote = (noteId: number, status: number) => {
  return http.put(`/admin/notes/${noteId}/audit`, { status });
};

// 删除笔记
export const deleteNote = (noteId: number) => {
  return http.delete(`/admin/notes/${noteId}`);
};

// 标记笔记已审核
export const reviewNote = (noteId: number) => {
  return http.put(`/admin/notes/${noteId}/review`);
};

// 获取分类树（管理端，不过滤状态）
export const getCategoryTree = () => {
  return http.get<CategoryTreeNode[]>(`/admin/categories/list`);
};

// 获取分类扁平列表（按 parentId）
export const getCategoryList = (parentId?: number) => {
  return http.get<CategoryTreeNode[]>(`/admin/categories/list`, parentId !== undefined ? { parentId } : {});
};
