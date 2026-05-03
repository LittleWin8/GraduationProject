<template>
  <div class="table-box">
    <ProTable
      ref="proTable"
      :columns="columns"
      :request-api="getTableList"
      :init-param="initParam"
      :data-callback="dataCallback"
      row-key="noteId"
    >
      <template #operation="scope">
        <el-button type="primary" link :icon="View" @click="viewDetail(scope.row)">详情</el-button>
        <el-button
          v-auth="'note:audit'"
          :type="scope.row.status === 1 ? 'danger' : scope.row.status === 3 ? 'success' : 'info'"
          link
          :disabled="scope.row.status !== 1 && scope.row.status !== 3"
          @click="handleAudit(scope.row, scope.row.status === 1 ? 3 : 1)"
        >
          {{ scope.row.status === 1 ? "下架" : scope.row.status === 3 ? "上架" : "私密" }}
        </el-button>
        <el-button
          v-auth="'note:review'"
          :type="scope.row.reviewed === 0 ? 'warning' : 'success'"
          link
          :disabled="scope.row.reviewed === 1"
          @click="handleReview(scope.row)"
        >
          {{ scope.row.reviewed === 0 ? "标记已审核" : "审核已通过" }}
        </el-button>
        <el-button v-auth="'note:delete'" type="danger" link :icon="Delete" @click="handleDelete(scope.row)">删除</el-button>
      </template>
    </ProTable>

    <NoteDrawer ref="noteDrawerRef" />
  </div>
</template>

<script setup lang="tsx" name="noteList">
import { ref, reactive, onMounted } from "vue";
import { useHandleData } from "@/hooks/useHandleData";
import { ElMessage } from "element-plus";
import ProTable from "@/components/ProTable/index.vue";
import { ProTableInstance, ColumnProps } from "@/components/ProTable/interface";
import { View, Delete } from "@element-plus/icons-vue";
import NoteDrawer from "./components/NoteDrawer.vue";
import {
  getNoteList,
  getNoteDetail,
  auditNote,
  deleteNote,
  reviewNote,
  getCategoryTree,
  type NoteListVO,
  type CategoryTreeNode
} from "@/api/modules/note";

const proTable = ref<ProTableInstance>();
const noteDrawerRef = ref<InstanceType<typeof NoteDrawer>>();

// 初始化请求参数
const initParam = reactive({});

// 适配后端分页数据结构
const dataCallback = (data: any) => {
  return {
    list: data.records,
    total: data.total
  };
};

// 请求拦截：拆分日期范围
const getTableList = (params: any) => {
  const newParams = JSON.parse(JSON.stringify(params));
  if (newParams.createTime && newParams.createTime.length) {
    newParams.startTime = newParams.createTime[0];
    newParams.endTime = newParams.createTime[1];
  }
  delete newParams.createTime;
  return getNoteList(newParams);
};

// 分类扁平化选项
const categoryOptions = ref<{ label: string; value: number }[]>([]);

/** 从分类树中提取扁平选项 */
const flattenCategoryTree = (tree: CategoryTreeNode[], result: { label: string; value: number }[] = []) => {
  for (const node of tree) {
    result.push({ label: node.name, value: node.categoryId });
    if (node.children && node.children.length > 0) {
      flattenCategoryTree(node.children, result);
    }
  }
  return result;
};

/** 加载分类选项 */
const loadCategories = async () => {
  try {
    const { data } = await getCategoryTree();
    categoryOptions.value = flattenCategoryTree(data || []);
  } catch (e) {
    console.warn("加载分类失败:", e);
  }
};

onMounted(() => {
  loadCategories();
});

// 状态选项
const statusOptions = [
  { label: "草稿", value: 0 },
  { label: "正常", value: 1 },
  { label: "回收站", value: 2 },
  { label: "下架", value: 3 }
];

const reviewedOptions = [
  { label: "未审核", value: 0 },
  { label: "已审核", value: 1 }
];

// 表格列配置
const columns = reactive<ColumnProps<NoteListVO>[]>([
  { prop: "noteId", label: "ID", width: 80, sortable: true },
  { prop: "title", label: "标题", showOverflowTooltip: true },
  { prop: "author", label: "作者", width: 100 },
  { prop: "categoryName", label: "分类", width: 100 },
  {
    prop: "status",
    label: "状态",
    width: 90,
    search: { el: "select", props: { placeholder: "全部状态" } },
    enum: statusOptions,
    render: (scope: any) => {
      const map: Record<number, { type: string; text: string }> = {
        0: { type: "info", text: "草稿" },
        1: { type: "success", text: "正常" },
        2: { type: "warning", text: "回收站" },
        3: { type: "danger", text: "下架" }
      };
      const item = map[scope.row.status] || { type: "info", text: "未知" };
      return <el-tag type={item.type}>{item.text}</el-tag>;
    }
  },
  {
    prop: "isPublic",
    label: "公开",
    width: 80,
    render: (scope: any) => {
      const isPublic = scope.row.isPublic === 1;
      return <el-tag type={isPublic ? "success" : "info"}>{isPublic ? "公开" : "私密"}</el-tag>;
    }
  },
  {
    prop: "reviewed",
    label: "审核",
    width: 90,
    search: { el: "select", props: { placeholder: "审核状态" } },
    enum: reviewedOptions,
    render: (scope: any) => {
      const reviewed = scope.row.reviewed === 1;
      return <el-tag type={reviewed ? "success" : "warning"}>{reviewed ? "已审核" : "未审核"}</el-tag>;
    }
  },
  { prop: "viewCount", label: "浏览", width: 80 },
  { prop: "likeCount", label: "点赞", width: 80 },
  { prop: "commentCount", label: "评论", width: 80 },
  {
    prop: "createTime",
    label: "创建时间",
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
    prop: "keyword",
    label: "关键词",
    isShow: false,
    search: { el: "input", props: { placeholder: "搜索标题/内容" } }
  },
  {
    prop: "categoryId",
    label: "分类",
    isShow: false,
    search: { el: "select", props: { placeholder: "全部分类" } },
    enum: categoryOptions
  },
  { prop: "operation", label: "操作", fixed: "right", width: 300 }
]);

/** 查看笔记详情（侧边抽屉） */
const viewDetail = async (row: NoteListVO) => {
  try {
    const { data } = await getNoteDetail(row.noteId);
    noteDrawerRef.value?.acceptParams({ row: data });
  } catch (e) {
    ElMessage.error("获取笔记详情失败");
  }
};

/** 审核笔记（上架/下架） */
const handleAudit = async (row: NoteListVO, status: number) => {
  const action = status === 3 ? "下架" : "上架";
  await useHandleData(() => auditNote(row.noteId, status), {}, `${action}笔记【${row.title}】`);
  proTable.value?.getTableList();
};

/** 标记笔记已审核 */
const handleReview = async (row: NoteListVO) => {
  await useHandleData(() => reviewNote(row.noteId), {}, `标记笔记【${row.title}】已审核`);
  proTable.value?.getTableList();
};

/** 删除笔记 */
const handleDelete = async (row: NoteListVO) => {
  await useHandleData(deleteNote, row.noteId, `删除笔记【${row.title}】`);
  proTable.value?.getTableList();
};
</script>
