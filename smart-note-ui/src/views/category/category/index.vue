<template>
  <div class="main-box">
    <TreeFilter title="分类列表" label="name" id="categoryId" :request-api="getCategoryTree" @change="changeTreeFilter" />
    <div class="table-box">
      <ProTable
        ref="proTable"
        :columns="columns"
        :request-api="getCategoryList"
        :init-param="initParam"
        :pagination="false"
        :data-callback="dataCallback"
      >
        <template #tableHeader>
          <el-button v-auth="'category:add'" type="primary" :icon="CirclePlus" @click="openDrawer('新增')">新增分类</el-button>
        </template>
        <template #operation="scope">
          <el-button type="primary" link :icon="EditPen" v-auth="'category:edit'" @click="openDrawer('编辑', scope.row)">
            编辑
          </el-button>
          <el-button
            :type="scope.row.status === 1 ? 'danger' : 'success'"
            link
            :icon="Switch"
            @click="handleToggleStatus(scope.row)"
          >
            {{ scope.row.status === 1 ? "禁用" : "启用" }}
          </el-button>
          <el-button type="danger" link :icon="Delete" v-auth="'category:delete'" @click="handleDelete(scope.row)">
            删除
          </el-button>
        </template>
      </ProTable>
      <CategoryDrawer ref="drawerRef" />
    </div>
  </div>
</template>

<script setup lang="tsx" name="categoryList">
import { ref, reactive } from "vue";
import { useHandleData } from "@/hooks/useHandleData";
import ProTable from "@/components/ProTable/index.vue";
import TreeFilter from "@/components/TreeFilter/index.vue";
import { ProTableInstance, ColumnProps } from "@/components/ProTable/interface";
import { CirclePlus, EditPen, Delete, Switch } from "@element-plus/icons-vue";
import CategoryDrawer from "./components/CategoryDrawer.vue";
import {
  getCategoryTree,
  getCategoryList,
  addCategory,
  updateCategory,
  deleteCategory,
  toggleCategoryStatus,
  type CategoryListItem
} from "@/api/modules/category";

const proTable = ref<ProTableInstance>();
const drawerRef = ref<InstanceType<typeof CategoryDrawer>>();

// TreeFilter 选中参数
const initParam = reactive<{ parentId: number | string }>({ parentId: "" });

/** TreeFilter 切换时更新 parentId */
const changeTreeFilter = (val: any) => {
  initParam.parentId = val ?? "";
};

/** 适配非分页数据（pagination=false 时 useTable 直接赋值 data，不取 .list） */
const dataCallback = (data: any) => {
  return Array.isArray(data) ? data : data.records || [];
};

// 状态选项
const statusOptions = [
  { label: "启用", value: 1 },
  { label: "禁用", value: 0 }
];

// 表格列配置
const columns = reactive<ColumnProps<CategoryListItem>[]>([
  { type: "index", label: "序号", width: 80 },
  { prop: "categoryId", label: "ID", width: 80 },
  { prop: "name", label: "分类名称" },
  { prop: "sortOrder", label: "排序", width: 100 },
  {
    prop: "status",
    label: "状态",
    width: 100,
    enum: statusOptions,
    search: { el: "select", props: { placeholder: "全部状态" } },
    render: (scope: any) => {
      const enabled = scope.row.status === 1;
      return <el-tag type={enabled ? "success" : "danger"}>{enabled ? "启用" : "禁用"}</el-tag>;
    }
  },
  {
    prop: "createTime",
    label: "创建时间",
    width: 180,
    render: (scope: any) => {
      return scope.row.createTime ? scope.row.createTime.replace("T", " ").substring(0, 19) : "--";
    }
  },
  { prop: "operation", label: "操作", fixed: "right", width: 240 }
]);

/** 打开抽屉（新增/编辑） */
const openDrawer = (title: string, row: Partial<CategoryListItem> = {}) => {
  const params = {
    title,
    row: { ...row },
    api: title === "新增" ? addCategory : (data: any) => updateCategory(row.categoryId!, data),
    getTableList: proTable.value?.getTableList
  };
  drawerRef.value?.acceptParams(params);
};

/** 切换启禁用状态 */
const handleToggleStatus = async (row: CategoryListItem) => {
  const action = row.status === 1 ? "禁用" : "启用";
  await useHandleData(() => toggleCategoryStatus(row.categoryId), {}, `${action}分类【${row.name}】`);
  proTable.value?.getTableList();
};

/** 删除分类 */
const handleDelete = async (row: CategoryListItem) => {
  await useHandleData(() => deleteCategory(row.categoryId), {}, `删除分类【${row.name}】`);
  proTable.value?.getTableList();
};
</script>
