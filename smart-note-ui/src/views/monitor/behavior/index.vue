<template>
  <div class="table-box">
    <ProTable
      ref="proTable"
      :columns="columns"
      :request-api="getTableList"
      :init-param="initParam"
      :data-callback="dataCallback"
      row-key="id"
    />
  </div>
</template>

<script setup lang="tsx" name="behaviorLog">
import { reactive } from "vue";
import ProTable from "@/components/ProTable/index.vue";
import { ColumnProps } from "@/components/ProTable/interface";
import { getBehaviorLogList, type BehaviorLogVO } from "@/api/modules/log";

const initParam = reactive({});

const dataCallback = (data: any) => {
  return {
    list: data.records,
    total: data.total
  };
};

const getTableList = (params: any) => {
  const newParams = JSON.parse(JSON.stringify(params));
  if (newParams.createTime && newParams.createTime.length) {
    newParams.startTime = newParams.createTime[0];
    newParams.endTime = newParams.createTime[1];
  }
  delete newParams.createTime;
  return getBehaviorLogList(newParams);
};

const actionTypeOptions = [
  { label: "浏览", value: 1 },
  { label: "搜索", value: 2 }
];

const actionTypeMap: Record<number, { type: string; text: string }> = {
  1: { type: "primary", text: "浏览" },
  2: { type: "warning", text: "搜索" }
};

const columns = reactive<ColumnProps<BehaviorLogVO>[]>([
  { prop: "id", label: "ID", width: 80 },
  { prop: "nickname", label: "用户", width: 120 },
  {
    prop: "actionType",
    label: "行为类型",
    width: 100,
    search: { el: "select", props: { placeholder: "全部类型" } },
    enum: actionTypeOptions,
    render: (scope: any) => {
      const item = actionTypeMap[scope.row.actionType] || { type: "info", text: "未知" };
      return <el-tag type={item.type}>{item.text}</el-tag>;
    }
  },
  { prop: "content", label: "内容", showOverflowTooltip: true },
  {
    prop: "createTime",
    label: "发生时间",
    width: 170,
    sortable: true,
    search: {
      el: "date-picker",
      span: 2,
      props: { type: "daterange", valueFormat: "YYYY-MM-DD", "start-placeholder": "开始日期", "end-placeholder": "结束日期" }
    },
    render: (scope: any) => {
      return scope.row.createTime ? scope.row.createTime.replace("T", " ").substring(0, 19) : "--";
    }
  }
]);
</script>
