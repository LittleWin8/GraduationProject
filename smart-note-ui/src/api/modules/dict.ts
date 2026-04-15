import { ResPage } from "@/api/interface/index";
import http from "@/api";

/**
 * @name 字典管理模块 (对应后端 /api/admin/sys/dict)
 */
export namespace Dict {
  // 字典类型请求参数
  export interface ReqDictTypeParams {
    pageNum: number;
    pageSize: number;
    dictName?: string;
    status?: number;
  }
  // 字典类型响应对象
  export interface ResDictType {
    dictId: number;
    dictName: string;
    dictType: string;
    status: number;
    remark: string;
    createTime: string;
  }
  // 字典数据请求参数
  export interface ReqDictDataParams {
    pageNum: number;
    pageSize: number;
    dictType: string; // 必传：关联的字典类型
    dictLabel?: string; // 搜索：标签
    status?: number; // 搜索：状态
  }
  // 字典数据响应对象
  export interface ResDictData {
    dataId: number;
    dictType: string;
    dictLabel: string;
    dictValue: string;
    tagType: string;
    sortOrder: number;
    status: number;
    remark: string;
    createTime: string;
  }
}

/**
 * ================= 字典类型 (Type) 管理 =================
 */
/**
 * 1. 获取字典类型分页列表 (对接后端接口: GET /api/admin/sys/dict/type/list)
 * 对应你刚才改造的后端逻辑
 */
export const getDictTypeList = (params: Dict.ReqDictTypeParams) => {
  return http.get<ResPage<Dict.ResDictType>>(`/admin/sys/dict/type/list`, params, { loading: false });
};

/**
 * 2. 新增字典类型
 */
export const addDictType = (params: any) => {
  return http.post(`/admin/sys/dict/type/add`, params);
};

/**
 * 3. 编辑字典类型
 */
export const editDictType = (params: any) => {
  return http.put(`/admin/sys/dict/type/edit`, params);
};

/**
 * 4. 删除字典类型
 * 按照你之前的风格，通常是传递 ID
 */
export const deleteDictType = (params: { ids: string[] | number[] }) => {
  return http.delete(`/admin/sys/dict/type/delete`, params);
};

/**
 * 5. 导出字典数据
 * 后端暂时不实现
 */
// export const exportDictType = (params: Dict.ReqDictTypeParams) => {
//   return http.download(`/admin/sys/dict/type/export`, params);
// };

/**
 * ================= 字典数据 (Data) 管理 =================
 */

/**
 * 1. 获取字典数据分页列表 (详情页管理使用)
 * 对接后端: GET /api/admin/sys/dict/data/list
 */
export const getDictDataList = (params: Dict.ReqDictDataParams) => {
  return http.get<ResPage<Dict.ResDictData>>(`/admin/sys/dict/data/list`, params, { loading: false });
};

/**
 * 2. 新增字典数据
 * 对接后端: POST /api/admin/sys/dict/data/add
 */
export const addDictData = (params: any) => {
  return http.post(`/admin/sys/dict/data/add`, params);
};

/**
 * 3. 编辑字典数据
 * 对接后端: PUT /api/admin/sys/dict/data/edit
 */
export const editDictData = (params: any) => {
  return http.put(`/admin/sys/dict/data/edit`, params);
};

/**
 * 4. 删除字典数据
 * 对接后端: DELETE /api/admin/sys/dict/data/delete
 */
export const deleteDictData = (params: { ids: string[] | number[] }) => {
  return http.delete(`/admin/sys/dict/data/delete`, params);
};

/**
 * 5. 根据字典类型查询字典数据 (用于表单下拉框回显，返回全量列表)
 * 对接后端: GET /api/admin/sys/dict/type/{dictType}
 */
export const getDictDataByType = (dictType: string) => {
  return http.get<Dict.ResDictData[]>(`/admin/sys/dict/type/${dictType}`);
};
