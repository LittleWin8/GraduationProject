import http from "@/api";

export interface ReqTagParams {
  pageNum?: number;
  pageSize?: number;
  keyword?: string;
  orderColumn?: string;
  orderRule?: string;
}

export interface TagVO {
  tagId: number;
  name: string;
  userId: number;
  userName: string;
  noteCount: number;
  createTime: string;
}

export const getTagList = (params: ReqTagParams) => {
  return http.get(`/admin/tags/list`, params, { loading: false });
};

export const deleteTag = (id: number) => {
  return http.delete(`/admin/tags/${id}`);
};
