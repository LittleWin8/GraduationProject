<template>
  <div class="table-box">
    <ProTable
      ref="proTable"
      :columns="columns"
      :request-api="getTableList"
      :init-param="initParam"
      :data-callback="dataCallback"
      @drag-sort="sortTable"
      row-key="userId"
    >
      <template #tableHeader="scope">
        <el-button type="primary" :icon="CirclePlus" @click="openDrawer('新增')">新增用户</el-button>
        <!-- <el-button type="primary" plain @click="toDetail">To 子集详情页面</el-button> -->
        <el-button type="danger" :icon="Delete" plain :disabled="!scope.isSelected" @click="batchDelete(scope.selectedListIds)">
          批量删除用户
        </el-button>
      </template>

      <template #expand="scope">
        <pre>{{ scope.row }}</pre>
      </template>

      <template #operation="scope">
        <el-button type="primary" link :icon="View" @click="openDrawer('查看', scope.row)">查看</el-button>
        <el-button type="primary" link :icon="EditPen" @click="openDrawer('编辑', scope.row)">编辑</el-button>
        <el-button type="primary" link :icon="Refresh" @click="resetPass(scope.row)">重置密码</el-button>
        <el-button type="primary" link :icon="Delete" @click="deleteAccount(scope.row)">删除</el-button>
      </template>
    </ProTable>
    <UserDrawer ref="drawerRef" />
    <ImportExcel ref="dialogRef" />
  </div>
</template>

<script setup lang="tsx" name="useProTable">
import { ref, reactive } from "vue";
// import { useRouter } from "vue-router";
// import { User } from "@/api/interface";
import { useHandleData } from "@/hooks/useHandleData";
import { ElMessage } from "element-plus";
import ProTable from "@/components/ProTable/index.vue";
import ImportExcel from "@/components/ImportExcel/index.vue";
import UserDrawer from "./components/UserDrawer.vue";
import { ProTableInstance, ColumnProps } from "@/components/ProTable/interface";
import { CirclePlus, Delete, EditPen, View, Refresh } from "@element-plus/icons-vue";
import { getUserList, deleteUser, editUser, addUser, resetUserPassword, getUserDetails } from "@/api/modules/user";
import { getDictDataByType } from "@/api/modules/dict";

// const router = useRouter();
const proTable = ref<ProTableInstance>();

// 跳转详情页
// const toDetail = () => {
//   router.push(`/proTable/useProTable/detail/${Math.random().toFixed(3)}?params=detail-page`);
// };

// 初始化请求参数
const initParam = reactive({});

// 适配后端分页数据结构 Result<data: { records: [], total: 0 }>
const dataCallback = (data: any) => {
  data.records.forEach((item: any) => {
    item.gender = String(item.gender);
  });

  return {
    list: data.records,
    total: data.total
  };
};

// 请求拦截：拆分日期范围，适配后端 startTime/endTime
const getTableList = (params: any) => {
  let newParams = JSON.parse(JSON.stringify(params));
  if (newParams.createTime && newParams.createTime.length) {
    newParams.startTime = newParams.createTime[0];
    newParams.endTime = newParams.createTime[1];
  }
  delete newParams.createTime;
  return getUserList(newParams);
};

// 表格配置项
const columns = reactive<ColumnProps<any>[]>([
  { type: "selection", fixed: "left", width: 70 },
  {
    prop: "userId",
    label: "用户ID"
  },
  {
    prop: "nickname", // 对应后端 nickname
    label: "用户昵称",
    search: { el: "input" }
  },
  {
    prop: "authType",
    label: "认证类型",
    search: { el: "select" },
    enum: () => getDictDataByType("auth_type"),
    fieldNames: { label: "dictLabel", value: "dictValue" }
  },
  {
    prop: "identifier",
    label: "用户名",
    search: { el: "input" }
  },
  {
    prop: "gender",
    label: "性别",
    search: { el: "select" },
    enum: () => getDictDataByType("user_gender"),
    fieldNames: { label: "dictLabel", value: "dictValue" }
  },
  { prop: "phone", label: "手机号", search: { el: "input" } },
  { prop: "city", label: "城市", search: { el: "input" } },
  {
    prop: "roleId",
    label: "角色",
    isShow: false,
    enum: () => getDictDataByType("role_name"),
    fieldNames: { label: "dictLabel", value: "dictValue" },
    search: {
      el: "select",
      props: {
        multiple: true,
        "collapse-tags": true,
        "collapse-tags-tooltip": true
      }
    }
  },
  {
    prop: "status",
    label: "用户状态",
    isShow: false,
    enum: () => getDictDataByType("user_status"),
    fieldNames: { label: "dictLabel", value: "dictValue" },
    search: {
      el: "select",
      props: { placeholder: "请选择用户状态" }
    }
  },
  {
    prop: "createTime",
    label: "注册时间",
    width: 180,
    isShow: false,
    search: {
      el: "date-picker",
      span: 2,
      props: { type: "datetimerange", valueFormat: "YYYY-MM-DD HH:mm:ss" }
    }
  },
  { prop: "operation", label: "操作", fixed: "right", width: 330 }
]);

// 以下逻辑保持原样，完全保留
const sortTable = ({ newIndex, oldIndex }: any) => {
  console.log("排序完成", newIndex, oldIndex);
  ElMessage.success("修改列表排序成功");
};

// 单个删除
const deleteAccount = async (params: any) => {
  await useHandleData(deleteUser, [params.userId], `删除【${params.nickname}】用户`);
  proTable.value?.getTableList();
};

// 批量删除
const batchDelete = async (ids: string[]) => {
  await useHandleData(deleteUser, ids, "删除所选用户信息");
  proTable.value?.clearSelection();
  proTable.value?.getTableList();
};

const resetPass = async (params: any) => {
  await useHandleData(resetUserPassword, { userId: params.userId }, `重置【${params.nickname}】密码`);
  proTable.value?.getTableList();
};

const drawerRef = ref<InstanceType<typeof UserDrawer> | null>(null);
const openDrawer = async (title: string, row: any = {}) => {
  let detailData = { ...row };

  if (title !== "新增" && row.userId) {
    try {
      const { data } = await getUserDetails(row.userId);
      detailData = data;

      // 将详情数据中的数字字段转为字符串，确保与字典匹配
      if (detailData.gender !== undefined) detailData.gender = String(detailData.gender);
      if (detailData.status !== undefined) detailData.status = String(detailData.status);
      // 如果角色是数组，也要确保里面的 ID 是字符串
      if (Array.isArray(detailData.roleIds)) {
        detailData.roleIds = detailData.roleIds.map((id: any) => String(id));
      }
    } catch (error) {
      return console.error("获取用户详情失败", error);
    }
  }

  // 从 proTable 的列配置中安全提取字典
  const getEnumData = (prop: string) => {
    const rawEnum = proTable.value?.enumMap.get(prop) || [];
    return rawEnum.map((item: any) => ({
      ...item,
      dictValue: String(item.dictValue)
    }));
  };

  const dicts = {
    gender: getEnumData("gender"),
    status: getEnumData("status"),
    roles: getEnumData("roleId")
  };

  const params = {
    title,
    isView: title === "查看",
    row: detailData,
    dicts,
    api: title === "新增" ? addUser : title === "编辑" ? editUser : undefined,
    getTableList: proTable.value?.getTableList
  };
  drawerRef.value?.acceptParams(params);
};
</script>
