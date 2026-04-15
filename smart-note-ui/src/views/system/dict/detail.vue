<template>
  <div class="table-box">
    <ProTable
      ref="proTable"
      :columns="columns"
      :request-api="getTableList"
      :data-callback="dataCallback"
      :init-param="initParam"
      row-key="dataId"
    >
      <template #tableHeader="scope">
        <el-button type="primary" :icon="CirclePlus" @click="openDrawer('新增')">新增数据</el-button>
        <el-button type="danger" :icon="Delete" plain :disabled="!scope.isSelected" @click="batchDelete(scope.selectedListIds)">
          批量删除
        </el-button>
        <el-button type="info" :icon="Back" plain @click="router.back()">返回</el-button>
      </template>

      <template #operation="scope">
        <el-button type="primary" link :icon="EditPen" @click="openDrawer('编辑', scope.row)">编辑</el-button>
        <el-button type="primary" link :icon="Delete" @click="deleteData(scope.row)">删除</el-button>
      </template>
    </ProTable>

    <DictDataDrawer ref="drawerRef" />
  </div>
</template>

<script setup lang="tsx" name="dictDataList">
import { ref, reactive } from "vue";
import { useRoute, useRouter } from "vue-router";
import { ProTableInstance, ColumnProps } from "@/components/ProTable/interface";
import { CirclePlus, Delete, EditPen, Back } from "@element-plus/icons-vue";
import ProTable from "@/components/ProTable/index.vue";
import DictDataDrawer from "./components/DictDataDrawer.vue"; // 需要新建此抽屉
import { useHandleData } from "@/hooks/useHandleData";
import { getDictDataList, deleteDictData, addDictData, editDictData } from "@/api/modules/dict";

const route = useRoute();
const router = useRouter();
const proTable = ref<ProTableInstance>();

// 获取路由传递的 dictType 参数
const dictType = route.params.dictType as string;

// 初始化请求参数，确保每次查询都带上 dictType
const initParam = reactive({ dictType });

// 适配分页结构
const dataCallback = (data: any) => {
  return {
    list: data.records,
    total: data.total
  };
};

// 封装请求逻辑
const getTableList = (params: any) => {
  return getDictDataList(params);
};

// 状态切换逻辑
const changeStatus = async (row: any) => {
  const params = {
    dataId: row.dataId,
    status: row.status === 1 ? 0 : 1
  };
  await useHandleData(editDictData, params, `确定要${row.status === 1 ? "禁用" : "启用"}“${row.dictLabel}”吗`);
  proTable.value?.getTableList();
};

// 表格列配置项
const columns = reactive<ColumnProps[]>([
  { type: "selection", fixed: "left", width: 70 },
  {
    prop: "dictLabel",
    label: "字典标签",
    search: { el: "input" },
    render: scope => {
      // 根据后端 tagType 字段显示不同样式的标签
      return <el-tag type={scope.row.tagType}>{scope.row.dictLabel}</el-tag>;
    }
  },
  { prop: "dictValue", label: "字典键值" },
  { prop: "sortOrder", label: "排序" },
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
        <el-switch model-value={scope.row.status} active-value={1} inactive-value={0} onClick={() => changeStatus(scope.row)} />
      );
    }
  },
  { prop: "remark", label: "备注", width: 200 },
  { prop: "createTime", label: "创建时间", width: 180 },
  { prop: "operation", label: "操作", fixed: "right", width: 180 }
]);

// 删除逻辑
const deleteData = async (row: any) => {
  await useHandleData(deleteDictData, { ids: [row.dataId] }, `删除数据项【${row.dictLabel}】`);
  proTable.value?.getTableList();
};

const batchDelete = async (ids: string[]) => {
  await useHandleData(deleteDictData, { ids }, "删除所选字典数据项");
  proTable.value?.clearSelection();
  proTable.value?.getTableList();
};

const drawerRef = ref();
const openDrawer = (title: string, row: any = {}) => {
  const params = {
    title,
    // 新增时自动注入当前的 dictType，编辑时保留原样
    row: { ...row, dictType: row.dictType || dictType },
    api: title === "新增" ? addDictData : editDictData,
    getTableList: proTable.value?.getTableList
  };
  drawerRef.value?.acceptParams(params);
};
</script>
