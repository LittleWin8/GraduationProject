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

<script setup lang="tsx" name="aiLog">
import { reactive } from "vue";
import ProTable from "@/components/ProTable/index.vue";
import { ColumnProps } from "@/components/ProTable/interface";
import { getAiLogList, type AiLogVO } from "@/api/modules/aiLog";

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
  return getAiLogList(newParams);
};

const statusOptions = [
  { label: "成功", value: 1 },
  { label: "失败", value: 0 }
];

const columns = reactive<ColumnProps<AiLogVO>[]>([
  { prop: "id", label: "ID", width: 80 },
  { prop: "noteTitle", label: "笔记标题", showOverflowTooltip: true },
  { prop: "summary", label: "摘要内容", showOverflowTooltip: true },
  { prop: "keywords", label: "关键词", width: 150 },
  {
    prop: "status",
    label: "状态",
    width: 90,
    search: { el: "select", props: { placeholder: "全部状态" } },
    enum: statusOptions,
    render: (scope: any) => {
      return <el-tag type={scope.row.status === 1 ? "success" : "danger"}>{scope.row.status === 1 ? "成功" : "失败"}</el-tag>;
    }
  },
  {
    prop: "errorMsg",
    label: "错误信息",
    showOverflowTooltip: true,
    render: (scope: any) => {
      return scope.row.status === 0 ? scope.row.errorMsg || "--" : "--";
    }
  },
  {
    prop: "createTime",
    label: "生成时间",
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
