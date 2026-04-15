import { ResPage } from "@/api/interface/index";
import http from "@/api";

/**
 * @name 字典管理模块 (对应后端 /api/admin/sys/dict)
 */
export namespace Dict {
  // 请求参数
  export interface ReqDictTypeParams {
    pageNum: number;
    pageSize: number;
    dictName?: string;
    status?: number;
  }
  // 响应对象
  export interface ResDictType {
    dictId: number;
    dictName: string;
    dictType: string;
    status: number;
    remark: string;
    createTime: string;
  }
}

/**
 * 1. 获取字典类型分页列表 (对接后端接口: GET /api/admin/sys/dict/type/list)
 * 对应你刚才改造的后端逻辑
 */
export const getDictTypeList = (params: Dict.ReqDictTypeParams) => {
  // 按照你的后端路径习惯：/api/admin/sys/dict/type/list
  return http.get<ResPage<Dict.ResDictType>>(`/admin/sys/dict/type/list`, params, { loading: false });
};

/**
 * 2. 获取字典项详情列表 (用于字典管理页面点击“查看”或“数据配置”时)
 * 对接后端：GET /api/admin/sys/dict/type/{dictType}
 */
export const getDictDataList = (dictType: string) => {
  return http.get<any[]>(`/admin/sys/dict/type/${dictType}`);
};

/**
 * 3. 新增字典类型
 */
export const addDictType = (params: any) => {
  return http.post(`/admin/sys/dict/type/add`, params);
};

/**
 * 4. 编辑字典类型
 */
export const editDictType = (params: any) => {
  return http.put(`/admin/sys/dict/type/edit`, params);
};

/**
 * 5. 删除字典类型
 * 按照你之前的风格，通常是传递 ID
 */
export const deleteDictType = (params: { dictId: number | number[] }) => {
  return http.post(`/admin/sys/dict/type/delete`, params);
};

/**
 * 6. 导出字典数据
 */
export const exportDictType = (params: Dict.ReqDictTypeParams) => {
  return http.download(`/admin/sys/dict/type/export`, params);
};
