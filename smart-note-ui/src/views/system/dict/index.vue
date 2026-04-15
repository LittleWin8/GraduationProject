<template>
  <div class="table-box">
    <ProTable ref="proTable" :columns="columns" :request-api="getTableList" :data-callback="dataCallback">
      <template #tableHeader="scope">
        <el-button type="primary" :icon="CirclePlus" @click="openDrawer('新增')">新增字典</el-button>
        <el-button type="danger" :icon="Delete" plain :disabled="!scope.isSelected" @click="batchDelete(scope.selectedListIds)">
          批量删除
        </el-button>
      </template>

      <template #operation="scope">
        <el-button type="primary" link :icon="View" @click="toDetail(scope.row)">数据配置</el-button>
        <el-button type="primary" link :icon="EditPen" @click="openDrawer('编辑', scope.row)">编辑</el-button>
        <el-button type="primary" link :icon="Delete" @click="deleteDict(scope.row)">删除</el-button>
      </template>
    </ProTable>

    <DictDrawer ref="drawerRef" />
  </div>
</template>

<script setup lang="tsx" name="dictTypeList">
import { ref, reactive } from "vue";
import { useRouter } from "vue-router";
// import { ElMessage } from "element-plus";
import { ProTableInstance, ColumnProps } from "@/components/ProTable/interface";
import { CirclePlus, Delete, EditPen, View } from "@element-plus/icons-vue";
import ProTable from "@/components/ProTable/index.vue";
import DictDrawer from "./components/DictDrawer.vue";
import { useHandleData } from "@/hooks/useHandleData";
import { getDictTypeList, deleteDictType, addDictType, editDictType } from "@/api/modules/dict";

const router = useRouter();
const proTable = ref<ProTableInstance>();

// 1. 适配后端分页数据结构 (后端 SysDictServiceImpl 返回 IPage)
const dataCallback = (data: any) => {
  return {
    list: data.records,
    total: data.total
  };
};

// 2. 封装请求逻辑
const getTableList = (params: any) => {
  return getDictTypeList(params);
};

// 3. 切换状态逻辑 (复用编辑接口)
const changeStatus = async (row: any) => {
  // 只发送 ID 和 目标状态，后端 updateById 会自动处理局部更新
  const params = {
    dictId: row.dictId,
    status: row.status === 1 ? 0 : 1
  };

  await useHandleData(editDictType, params, `确定要${row.status === 1 ? "禁用" : "启用"}“${row.dictName}”字典吗`);
  proTable.value?.getTableList();
};

// 4. 跳转到详情
const toDetail = (row: any) => {
  router.push(`/system/dict/data/${row.dictType}`);
};

// 5. 表格配置项
const columns = reactive<ColumnProps[]>([
  { type: "selection", fixed: "left", width: 70 },
  { prop: "dictId", label: "字典编号", width: 100 },
  { prop: "dictName", label: "字典名称", search: { el: "input" } },
  { prop: "dictType", label: "字典类型", search: { el: "input" } },
  {
    prop: "status",
    label: "状态",
    enum: [
      { label: "正常", value: 1 },
      { label: "禁用", value: 0 }
    ],
    search: { el: "select" },
    render: scope => {
      return (
        <el-switch
          model-value={scope.row.status}
          active-value={1}
          inactive-value={0}
          active-text={scope.row.status === 1 ? "正常" : "禁用"}
          onClick={() => changeStatus(scope.row)}
        />
      );
    }
  },
  { prop: "remark", label: "备注", width: 200 },
  { prop: "createTime", label: "创建时间", width: 180 },
  { prop: "operation", label: "操作", fixed: "right", width: 250 }
]);

// 删除和批量删除逻辑保持不变...
const deleteDict = async (row: any) => {
  await useHandleData(deleteDictType, { id: row.dictId }, `删除字典【${row.dictName}】`);
  proTable.value?.getTableList();
};

const batchDelete = async (ids: string[]) => {
  await useHandleData(deleteDictType, { ids }, "删除所选字典数据");
  proTable.value?.clearSelection();
  proTable.value?.getTableList();
};

const drawerRef = ref();
const openDrawer = (title: string, row: any = {}) => {
  const params = {
    title,
    row: { ...row },
    api: title === "新增" ? addDictType : editDictType,
    getTableList: proTable.value?.getTableList
  };
  drawerRef.value?.acceptParams(params);
};
</script>
