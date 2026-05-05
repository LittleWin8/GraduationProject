import http from "@/api";

/**
 * @name 仪表盘模块 (对应后端 /api/admin/dashboard)
 */

/** 仪表盘统计数据 */
export interface DashboardStats {
  totalUsers: number;
  totalNotes: number;
  todayNewUsers: number;
  todayNewNotes: number;
  dateList: string[];
  newUserList: number[];
  newNoteList: number[];
  statusDistribution: { name: string; value: number }[];
  hotNotes: { title: string; viewCount: number }[];
}

// 获取仪表盘统计数据
export const getDashboardStats = () => {
  return http.get<DashboardStats>("/admin/dashboard/stats");
};

/** 数据分析结果 */
export interface AnalyzeResult {
  answer: string;
  sql: string;
  data: Record<string, any>[];
}

export const analyzeData = (question: string) => {
  return http.post<AnalyzeResult>("/admin/ai/analyze", { question });
};
