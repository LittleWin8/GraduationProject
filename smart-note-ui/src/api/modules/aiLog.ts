import http from "@/api";

export interface ReqAiLogParams {
  pageNum?: number;
  pageSize?: number;
  userId?: number;
  status?: number;
  actionType?: string;
  startTime?: string;
  endTime?: string;
}

export interface AiLogVO {
  id: number;
  userId: number;
  userName: string;
  noteId: number;
  noteTitle: string;
  actionType: string;
  promptTokens: number;
  completionTokens: number;
  totalTokens: number;
  modelName: string;
  status: number;
  errorMsg: string;
  createTime: string;
}

export interface AiUserQuotaVO {
  userId: number;
  userName: string;
  monthlyTokenLimit: number;
  monthlyRequestLimit: number;
  usedTokens: number;
  usedRequests: number;
  quotaResetDate: string;
}

export const getAiLogList = (params: ReqAiLogParams) => {
  return http.get(`/admin/ai/logs`, params, { loading: false });
};

export const getAiStats = () => {
  return http.get(`/admin/ai/stats`);
};

export const getAiRanking = (limit = 10, params?: { startTime?: string; endTime?: string }) => {
  return http.get(`/admin/ai/ranking`, { limit, ...params });
};

export const getAiQuotaList = (params: { pageNum?: number; pageSize?: number; keyword?: string }) => {
  return http.get(`/admin/ai/quota/list`, params, { loading: false });
};

export const updateAiQuota = (userId: number, data: { monthlyTokenLimit: number; monthlyRequestLimit: number }) => {
  return http.put(`/admin/ai/quota/${userId}`, data);
};
