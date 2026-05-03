<template>
  <div class="table-box">
    <ProTable
      ref="proTable"
      :columns="columns"
      :request-api="getTableList"
      :init-param="initParam"
      :data-callback="dataCallback"
      row-key="tagId"
      @sort-change="handleSortChange"
    >
      <template #operation="scope">
        <el-button type="danger" link :icon="Delete" v-auth="'note:tag:del'" @click="handleDelete(scope.row)">删除</el-button>
      </template>
    </ProTable>
  </div>
</template>

<script setup lang="tsx" name="tagList">
import { ref, reactive } from "vue";
import { useHandleData } from "@/hooks/useHandleData";
import ProTable from "@/components/ProTable/index.vue";
import { ProTableInstance, ColumnProps } from "@/components/ProTable/interface";
import { Delete } from "@element-plus/icons-vue";
import { getTagList, deleteTag, type TagVO } from "@/api/modules/tag";

const proTable = ref<ProTableInstance>();

const initParam = reactive({});

const sortParam = reactive({
  orderColumn: "",
  orderRule: ""
});

const handleSortChange = ({ prop, order }: any) => {
  const columnMap: Record<string, string> = {
    tagId: "t.tag_id",
    noteCount: "noteCount",
    createTime: "t.create_time"
  };
  if (!order) {
    sortParam.orderColumn = "";
    sortParam.orderRule = "";
  } else {
    sortParam.orderColumn = columnMap[prop] || "";
    sortParam.orderRule = order;
  }
  proTable.value?.getTableList();
};

const dataCallback = (data: any) => {
  return { list: data.records, total: data.total };
};

const getTableList = (params: any) => {
  const newParams = JSON.parse(JSON.stringify(params));
  if (newParams.name || newParams.userName) {
    newParams.keyword = newParams.name || newParams.userName || "";
    delete newParams.name;
    delete newParams.userName;
  }
  if (sortParam.orderColumn) {
    newParams.orderColumn = sortParam.orderColumn;
    newParams.orderRule = sortParam.orderRule;
  }
  return getTagList(newParams);
};

const columns = reactive<ColumnProps<TagVO>[]>([
  {
    prop: "tagId",
    label: "ID",
    width: 80,
    sortable: "custom"
  },
  {
    prop: "name",
    label: "标签名称",
    search: { el: "input", props: { placeholder: "搜索标签名" } }
  },
  {
    prop: "userName",
    label: "所属用户",
    width: 120,
    search: { el: "input", props: { placeholder: "搜索用户名" } }
  },
  { prop: "noteCount", label: "笔记数", width: 100, sortable: "custom" },
  {
    prop: "createTime",
    label: "创建时间",
    width: 170,
    sortable: "custom",
    render: (scope: any) => {
      return scope.row.createTime ? scope.row.createTime.replace("T", " ").substring(0, 19) : "--";
    }
  },
  { prop: "operation", label: "操作", fixed: "right", width: 100 }
]);

const handleDelete = async (row: TagVO) => {
  await useHandleData(() => deleteTag(row.tagId), {}, `删除标签【${row.name}】，删除后该标签下的笔记关联关系也将被删除`);
  proTable.value?.getTableList();
};
</script>
