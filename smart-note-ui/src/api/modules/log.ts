import http from "@/api";

/**
 * @name 日志管理模块 (对应后端 /api/admin/log)
 */

/** 操作日志查询参数 */
export interface ReqOperationLogParams {
  pageNum?: number;
  pageSize?: number;
  module?: string;
  actionType?: number;
  username?: string;
  status?: number;
  startTime?: string;
  endTime?: string;
}

/** 操作日志列表项 */
export interface OperationLogVO {
  id: number;
  userId: number;
  username: string;
  module: string;
  actionType: number;
  businessId: number;
  description: string;
  requestUrl: string;
  requestMethod: string;
  ipAddress: string;
  status: number;
  errorMsg: string;
  createTime: string;
}

/** 行为日志查询参数 */
export interface ReqBehaviorLogParams {
  pageNum?: number;
  pageSize?: number;
  actionType?: number;
  userId?: number;
  startTime?: string;
  endTime?: string;
}

/** 行为日志列表项 */
export interface BehaviorLogVO {
  id: number;
  userId: number;
  nickname: string;
  actionType: number;
  content: string;
  createTime: string;
}

// 获取操作日志分页列表
export const getOperationLogList = (params: ReqOperationLogParams) => {
  return http.get(`/admin/log/operation/list`, params, { loading: false });
};

// 获取行为日志分页列表
export const getBehaviorLogList = (params: ReqBehaviorLogParams) => {
  return http.get(`/admin/log/behavior/list`, params, { loading: false });
};
