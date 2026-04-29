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

<script setup lang="tsx" name="operationLog">
import { reactive } from "vue";
import ProTable from "@/components/ProTable/index.vue";
import { ColumnProps } from "@/components/ProTable/interface";
import { getOperationLogList, type OperationLogVO } from "@/api/modules/log";

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
  return getOperationLogList(newParams);
};

const moduleOptions = [
  { label: "AUTH", value: "AUTH" },
  { label: "USER", value: "USER" },
  { label: "NOTE", value: "NOTE" },
  { label: "DICT", value: "DICT" },
  { label: "AI", value: "AI" },
  { label: "ROLE", value: "ROLE" }
];

const actionTypeOptions = [
  { label: "登录", value: 1 },
  { label: "退出", value: 2 },
  { label: "创建", value: 3 },
  { label: "修改", value: 4 },
  { label: "删除", value: 5 }
];

const statusOptions = [
  { label: "成功", value: 1 },
  { label: "失败", value: 0 }
];

const moduleColorMap: Record<string, string> = {
  AUTH: "",
  USER: "success",
  NOTE: "primary",
  DICT: "info",
  AI: "warning",
  ROLE: "danger"
};

const actionTypeMap: Record<number, { type: string; text: string }> = {
  1: { type: "success", text: "登录" },
  2: { type: "info", text: "退出" },
  3: { type: "primary", text: "创建" },
  4: { type: "warning", text: "修改" },
  5: { type: "danger", text: "删除" }
};

const methodColorMap: Record<string, string> = {
  GET: "success",
  POST: "primary",
  PUT: "warning",
  DELETE: "danger"
};

const columns = reactive<ColumnProps<OperationLogVO>[]>([
  { prop: "id", label: "ID", width: 80 },
  { prop: "username", label: "操作人", width: 100 },
  {
    prop: "module",
    label: "模块",
    width: 80,
    search: { el: "select", props: { placeholder: "全部模块" } },
    enum: moduleOptions,
    render: (scope: any) => {
      const type = moduleColorMap[scope.row.module] || "";
      return <el-tag type={type}>{scope.row.module}</el-tag>;
    }
  },
  {
    prop: "actionType",
    label: "操作类型",
    width: 90,
    search: { el: "select", props: { placeholder: "全部类型" } },
    enum: actionTypeOptions,
    render: (scope: any) => {
      const item = actionTypeMap[scope.row.actionType] || { type: "info", text: "未知" };
      return <el-tag type={item.type}>{item.text}</el-tag>;
    }
  },
  { prop: "description", label: "描述", showOverflowTooltip: true },
  { prop: "requestUrl", label: "请求URL", width: 200, showOverflowTooltip: true },
  {
    prop: "requestMethod",
    label: "方法",
    width: 80,
    render: (scope: any) => {
      const type = methodColorMap[scope.row.requestMethod] || "info";
      return <el-tag type={type}>{scope.row.requestMethod}</el-tag>;
    }
  },
  { prop: "ipAddress", label: "IP", width: 130 },
  {
    prop: "status",
    label: "状态",
    width: 80,
    search: { el: "select", props: { placeholder: "全部状态" } },
    enum: statusOptions,
    render: (scope: any) => {
      return <el-tag type={scope.row.status === 1 ? "success" : "danger"}>{scope.row.status === 1 ? "成功" : "失败"}</el-tag>;
    }
  },
  {
    prop: "errorMsg",
    label: "错误信息",
    width: 150,
    showOverflowTooltip: true,
    render: (scope: any) => {
      return scope.row.status === 0 ? scope.row.errorMsg || "--" : "--";
    }
  },
  {
    prop: "createTime",
    label: "操作时间",
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
  },
  {
    prop: "username",
    label: "操作人",
    isShow: false,
    search: { el: "input", props: { placeholder: "搜索操作人" } }
  }
]);
</script>
