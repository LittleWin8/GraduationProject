<template>
  <div class="table-box">
    <ProTable ref="proTable" :columns="columns" :request-api="getTableList" :data-callback="dataCallback" row-key="roleId">
      <template #tableHeader="scope">
        <el-button type="primary" :icon="CirclePlus" @click="openDrawer('新增')">新增角色</el-button>
        <el-button type="danger" :icon="Delete" plain :disabled="!scope.isSelected" @click="batchDelete(scope.selectedListIds)">
          批量删除
        </el-button>
      </template>

      <template #operation="scope">
        <el-button type="primary" link :icon="Lock" @click="openPermissionDrawer(scope.row)">分配权限</el-button>
        <el-button type="primary" link :icon="EditPen" @click="openDrawer('编辑', scope.row)">编辑</el-button>
        <el-button type="primary" link :icon="Delete" @click="deleteRoleItem(scope.row)">删除</el-button>
      </template>
    </ProTable>

    <RoleDrawer ref="drawerRef" />
    <RolePermissionDrawer ref="permissionRef" />
  </div>
</template>

<script setup lang="tsx" name="roleList">
import { ref, reactive } from "vue";
import { ProTableInstance, ColumnProps } from "@/components/ProTable/interface";
import { CirclePlus, Delete, EditPen, Lock } from "@element-plus/icons-vue";
import ProTable from "@/components/ProTable/index.vue";
import RoleDrawer from "./components/RoleDrawer.vue";
import RolePermissionDrawer from "./components/RolePermissionDrawer.vue";
import { useHandleData } from "@/hooks/useHandleData";
import { getRoleList, deleteRole, deleteRoles, addRole, editRole, changeRoleStatus } from "@/api/modules/role";

const proTable = ref<ProTableInstance>();

// 1. 适配后端数据结构
// 后端接口 GET /sys/role/list 返回的是 { data: [...] } 直接是数组
const dataCallback = (data: any) => {
  return {
    list: data,
    total: data.length // 如果后端后续支持分页，此处再改回 data.total
  };
};

// 2. 封装请求逻辑
const getTableList = (params: any) => {
  return getRoleList(params);
};

// 3. 修改角色状态
const handleStatusChange = async (row: any) => {
  const targetStatus = row.status === 1 ? 0 : 1;
  await useHandleData(
    () => changeRoleStatus(row.roleId, targetStatus),
    {},
    `确定要${targetStatus === 1 ? "启用" : "停用"}“${row.roleName}”角色吗`
  );
  proTable.value?.getTableList();
};

// 4. 表格列配置
const columns = reactive<ColumnProps[]>([
  { type: "selection", fixed: "left", width: 70 },
  { prop: "roleName", label: "角色名称", search: { el: "input" } },
  { prop: "roleKey", label: "权限字符", search: { el: "input" } },
  { prop: "sortOrder", label: "排序", width: 80 },
  {
    prop: "status",
    label: "状态",
    enum: [
      { label: "正常", value: 1 },
      { label: "停用", value: 0 }
    ],
    search: { el: "select" },
    render: scope => {
      return (
        <el-switch
          model-value={scope.row.status}
          active-value={1}
          inactive-value={0}
          onClick={() => handleStatusChange(scope.row)}
        />
      );
    }
  },
  { prop: "createTime", label: "创建时间", width: 180 },
  { prop: "operation", label: "操作", fixed: "right", width: 280 }
]);

// 5. 删除逻辑
const deleteRoleItem = async (row: any) => {
  await useHandleData(deleteRole, row.roleId, `删除角色【${row.roleName}】`);
  proTable.value?.getTableList();
};

const batchDelete = async (ids: string[]) => {
  await useHandleData(deleteRoles, ids.map(Number), "批量删除所选角色");
  proTable.value?.clearSelection();
  proTable.value?.getTableList();
};

// 6. 抽屉逻辑
const drawerRef = ref();
const openDrawer = (title: string, row: any = {}) => {
  const params = {
    title,
    row: { ...row },
    api: title === "新增" ? addRole : editRole,
    getTableList: proTable.value?.getTableList
  };
  drawerRef.value?.acceptParams(params);
};

const permissionRef = ref();
const openPermissionDrawer = (row: any) => {
  permissionRef.value?.acceptParams(row);
};
</script>
