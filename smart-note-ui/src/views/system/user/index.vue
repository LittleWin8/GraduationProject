<template>
  <div class="table-box">
    <ProTable
      ref="proTable"
      :columns="columns"
      :request-api="getTableList"
      :init-param="initParam"
      :data-callback="dataCallback"
      @drag-sort="sortTable"
    >
      <!-- 表格 header 按钮 -->
      <template #tableHeader="scope">
        <el-button v-auth="'add'" type="primary" :icon="CirclePlus" @click="openDrawer('新增')">新增用户</el-button>
        <el-button v-auth="'batchAdd'" type="primary" :icon="Upload" plain @click="batchAdd">批量添加用户</el-button>
        <el-button v-auth="'export'" type="primary" :icon="Download" plain @click="downloadFile">导出用户数据</el-button>
        <el-button type="danger" :icon="Delete" plain :disabled="!scope.isSelected" @click="batchDelete(scope.selectedListIds)">
          批量删除用户
        </el-button>
        <el-button type="primary" plain @click="toDetail">To 子集详情页面</el-button>
      </template>
      <!-- Expand -->
      <template #expand="scope">
        {{ scope.row }}
      </template>
      <!-- usernameHeader -->
      <template #usernameHeader="scope">
        <el-button type="primary" @click="ElMessage.success('我是通过作用域插槽渲染的表头')">
          {{ scope.column.label }}
        </el-button>
      </template>
      <!-- createTime -->
      <template #createTime="scope">
        <el-button type="primary" link @click="ElMessage.success('我是通过作用域插槽渲染的内容')">
          {{ scope.row.createTime }}
        </el-button>
      </template>
      <!-- 表格操作 -->
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
import { useRouter } from "vue-router";
import { User } from "@/api/interface";
import { useHandleData } from "@/hooks/useHandleData";
import { useDownload } from "@/hooks/useDownload";
// import { useAuthButtons } from "@/hooks/useAuthButtons";
import { ElMessage, ElMessageBox } from "element-plus";
import ProTable from "@/components/ProTable/index.vue";
import ImportExcel from "@/components/ImportExcel/index.vue";
import UserDrawer from "@/views/proTable/components/UserDrawer.vue";
import { ProTableInstance, ColumnProps, HeaderRenderScope } from "@/components/ProTable/interface";
import { CirclePlus, Delete, EditPen, Download, Upload, View, Refresh } from "@element-plus/icons-vue";
import {
  getUserList,
  deleteUser,
  editUser,
  addUser,
  // changeUserStatus,
  resetUserPassWord,
  exportUserInfo,
  BatchAddUser
  // getUserStatus,
  // getUserGender
} from "@/api/modules/user";

const router = useRouter();

// ProTable 实例
const proTable = ref<ProTableInstance>();

// 如果表格需要初始化请求参数，直接定义传给 ProTable (之后每次请求都会自动带上该参数，此参数更改之后也会一直带上，改变此参数会自动刷新表格数据)
const initParam = reactive({});

// 适配后端 MyBatis-Plus 分页结构
const dataCallback = (data: any) => {
  return {
    list: data.records, // 后端返回的是 records
    total: data.total,
    pageNum: data.current,
    pageSize: data.size
  };
};

// 请求接口适配
const getTableList = (params: any) => {
  let newParams = JSON.parse(JSON.stringify(params));
  // 处理日期筛选范围（如果 UserQueryDTO 需要 startTime/endTime）
  if (newParams.createTime) {
    newParams.startTime = newParams.createTime[0];
    newParams.endTime = newParams.createTime[1];
    delete newParams.createTime;
  }
  return getUserList(newParams);
};

// 页面按钮权限（按钮权限既可以使用 hooks，也可以直接使用 v-auth 指令，指令适合直接绑定在按钮上，hooks 适合根据按钮权限显示不同的内容）
// const { BUTTONS } = useAuthButtons();

// 跳转详情页
const toDetail = () => {
  router.push(`/system/user/detail/${Math.random().toFixed(3)}?params=detail-page`);
};

// 自定义渲染表头（使用tsx语法）
const headerRender = (scope: HeaderRenderScope<User.ResUserList>) => {
  return (
    <el-button type="primary" onClick={() => ElMessage.success("我是通过 tsx 语法渲染的表头")}>
      {scope.column.label}
    </el-button>
  );
};

// 表格配置项
const columns = reactive<ColumnProps<User.ResUserList>[]>([
  { type: "selection", fixed: "left", width: 70 },
  { prop: "userId", label: "ID", width: 80 },
  {
    // 适配后端 "nickname"
    prop: "nickname",
    label: "用户昵称",
    search: { el: "input" }
  },
  {
    // 适配后端 "identifier"
    prop: "identifier",
    label: "账号/用户名",
    search: { el: "input" }
  },
  {
    // 适配后端 "authType"
    prop: "authType",
    label: "登录类型",
    // 这里的 enum 可以根据后端逻辑转换显示
    enum: [
      { label: "密码", value: "password" },
      { label: "微信", value: "wx_openid" }
    ],
    search: { el: "select" }
  },
  {
    prop: "gender",
    label: "性别",
    // 适配后端 0, 1, 2
    enum: [
      { label: "女", value: 2 },
      { label: "男", value: 1 },
      { label: "未知", value: 0 }
    ],
    search: { el: "select" }
  },
  { prop: "phone", label: "手机号" },
  {
    prop: "city",
    label: "城市",
    search: { el: "input" }
  },
  {
    prop: "createTime",
    label: "创建时间",
    headerRender,
    width: 180,
    search: {
      el: "date-picker",
      span: 2,
      props: { type: "datetimerange", valueFormat: "YYYY-MM-DD HH:mm:ss" }
      // defaultValue: ["2022-11-12 11:35:00", "2022-12-12 11:35:00"]
    }
  },
  { prop: "operation", label: "操作", fixed: "right", width: 330 }
]);

// 表格拖拽排序
const sortTable = ({ newIndex, oldIndex }: { newIndex?: number; oldIndex?: number }) => {
  console.log(newIndex, oldIndex);
  console.log(proTable.value?.tableData);
  ElMessage.success("修改列表排序成功");
};

// 删除用户信息
const deleteAccount = async (params: User.ResUserList) => {
  await useHandleData(deleteUser, { id: [params.userId] }, `删除【${params.nickname}】用户`);
  proTable.value?.getTableList();
};

// 批量删除用户信息
const batchDelete = async (id: string[]) => {
  await useHandleData(deleteUser, { id }, "删除所选用户信息");
  proTable.value?.clearSelection();
  proTable.value?.getTableList();
};

// 重置用户密码
const resetPass = async (params: User.ResUserList) => {
  await useHandleData(resetUserPassWord, { id: params.userId }, `重置【${params.nickname}】用户密码`);
  proTable.value?.getTableList();
};

// 切换用户状态
// const changeStatus = async (row: User.ResUserList) => {
//   await useHandleData(changeUserStatus, { id: row.userId, status: row.status == 1 ? 0 : 1 }, `切换【${row.nickname}】用户状态`);
//   proTable.value?.getTableList();
// };

// 导出用户列表
const downloadFile = async () => {
  ElMessageBox.confirm("确认导出用户数据?", "温馨提示", { type: "warning" }).then(() =>
    useDownload(exportUserInfo, "用户列表", proTable.value?.searchParam)
  );
};

// 批量添加用户
const dialogRef = ref<InstanceType<typeof ImportExcel> | null>(null);
const batchAdd = () => {
  const params = {
    title: "用户",
    tempApi: exportUserInfo,
    importApi: BatchAddUser,
    getTableList: proTable.value?.getTableList
  };
  dialogRef.value?.acceptParams(params);
};

// 打开 drawer(新增、查看、编辑)
const drawerRef = ref<InstanceType<typeof UserDrawer> | null>(null);
const openDrawer = (title: string, row: Partial<User.ResUserList> = {}) => {
  const params = {
    title,
    isView: title === "查看",
    row: { ...row },
    api: title === "新增" ? addUser : title === "编辑" ? editUser : undefined,
    getTableList: proTable.value?.getTableList
  };
  drawerRef.value?.acceptParams(params);
};
</script>
