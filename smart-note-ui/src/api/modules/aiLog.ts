import http from "@/api";

export interface ReqAiLogParams {
  pageNum?: number;
  pageSize?: number;
  status?: number;
  startTime?: string;
  endTime?: string;
}

export interface AiLogVO {
  id: number;
  noteId: number;
  noteTitle: string;
  summary: string;
  keywords: string;
  status: number;
  errorMsg: string;
  createTime: string;
}

export const getAiLogList = (params: ReqAiLogParams) => {
  return http.get(`/admin/ai/logs`, params, { loading: false });
};
