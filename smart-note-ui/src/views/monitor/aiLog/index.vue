<template>
  <div class="ai-monitor">
    <!-- 区域 1：统计卡片 -->
    <el-row :gutter="16" class="stats-row">
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-label">总请求次数</div>
          <div class="stat-value">{{ statsData.totalRequests ?? "--" }}</div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-label">总 Token</div>
          <div class="stat-value">{{ formatNumber(statsData.totalTokens) }}</div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-label">输入 Token</div>
          <div class="stat-value">{{ formatNumber(statsData.totalPromptTokens) }}</div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-label">输出 Token</div>
          <div class="stat-value">{{ formatNumber(statsData.totalCompletionTokens) }}</div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 区域 2 + 3：排行 + 配额管理 -->
    <el-row :gutter="16" class="main-row">
      <!-- 用户排行 -->
      <el-col :span="8">
        <el-card shadow="hover" class="ranking-card">
          <template #header>
            <div style="display: flex; justify-content: space-between; align-items: center">
              <span>用户 Token 用量排行</span>
              <el-date-picker
                v-model="rankingDateRange"
                type="daterange"
                value-format="YYYY-MM-DD"
                start-placeholder="开始日期"
                end-placeholder="结束日期"
                size="small"
                style="width: 240px"
                @change="loadRanking"
              />
            </div>
          </template>
          <el-table :data="rankingData" stripe size="small" style="width: 100%; flex: 1">
            <el-table-column label="排名" width="60" align="center">
              <template #default="{ $index }">
                <el-tag :type="$index < 3 ? 'danger' : 'info'" size="small" effect="dark">
                  {{ $index + 1 }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="userName" label="用户名" show-overflow-tooltip />
            <el-table-column prop="requestCount" label="请求次数" width="90" align="center" />
            <el-table-column prop="totalTokens" label="Token 用量" width="100" align="center">
              <template #default="{ row }">
                {{ formatNumber(row.totalTokens) }}
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-col>

      <!-- 用户配额管理 -->
      <el-col :span="16">
        <el-card shadow="hover" class="quota-card">
          <div class="table-box">
            <ProTable
              ref="quotaTableRef"
              :columns="quotaColumns"
              :request-api="getQuotaTableList"
              :init-param="quotaInitParam"
              :data-callback="dataCallback"
              row-key="userId"
            >
              <template #operation="scope">
                <el-button type="primary" link @click="openQuotaDialog(scope.row)">修改配额</el-button>
              </template>
            </ProTable>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 区域 4：调用日志 -->
    <el-card shadow="hover" class="log-card">
      <div class="table-box">
        <ProTable
          ref="logTableRef"
          :columns="logColumns"
          :request-api="getLogTableList"
          :init-param="logInitParam"
          :data-callback="dataCallback"
          row-key="id"
        />
      </div>
    </el-card>

    <!-- 修改配额弹窗 -->
    <el-dialog v-model="quotaDialogVisible" title="修改用户配额" width="420px" destroy-on-close>
      <el-form :model="quotaForm" label-width="100px">
        <el-form-item label="用户">
          <el-input :model-value="quotaForm.userName" disabled />
        </el-form-item>
        <el-form-item label="Token 限额">
          <el-input-number v-model="quotaForm.monthlyTokenLimit" :min="1000" :step="10000" style="width: 100%" />
        </el-form-item>
        <el-form-item label="请求限额">
          <el-input-number v-model="quotaForm.monthlyRequestLimit" :min="1" :step="10" style="width: 100%" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="quotaDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleUpdateQuota">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="tsx" name="aiLog">
import { ref, reactive, onMounted } from "vue";
import { ElMessage } from "element-plus";
import ProTable from "@/components/ProTable/index.vue";
import { ProTableInstance, ColumnProps } from "@/components/ProTable/interface";
import {
  getAiLogList,
  getAiStats,
  getAiRanking,
  getAiQuotaList,
  updateAiQuota,
  type AiLogVO,
  type AiUserQuotaVO
} from "@/api/modules/aiLog";

// ==================== 统计卡片 ====================
const statsData = ref<Record<string, number>>({});

const formatNumber = (num?: number) => {
  if (num == null) return "--";
  return num.toLocaleString();
};

const loadStats = async () => {
  try {
    const { data } = await getAiStats();
    statsData.value = (data as Record<string, number>) ?? {};
  } catch {
    // ignore
  }
};

// ==================== 用户排行 ====================
const rankingData = ref<any[]>([]);
const rankingDateRange = ref<string[]>([]);

const loadRanking = async () => {
  try {
    const params: any = { limit: 10 };
    if (rankingDateRange.value && rankingDateRange.value.length === 2) {
      params.startTime = rankingDateRange.value[0];
      params.endTime = rankingDateRange.value[1];
    }
    const { data } = await getAiRanking(10, params);
    rankingData.value = (data as any[]) || [];
  } catch {
    // ignore
  }
};

onMounted(() => {
  loadStats();
  loadRanking();
});

// ==================== 调用日志 ====================
const logTableRef = ref<ProTableInstance>();
const logInitParam = reactive({});

const dataCallback = (data: any) => {
  return {
    list: data.records,
    total: data.total
  };
};

const getLogTableList = (params: any) => {
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

const logColumns = reactive<ColumnProps<AiLogVO>[]>([
  { prop: "id", label: "ID", width: 70 },
  { prop: "userName", label: "用户", width: 100 },
  { prop: "noteTitle", label: "笔记标题", width: 100, showOverflowTooltip: true },
  { prop: "actionType", label: "操作类型", width: 100 },
  { prop: "promptTokens", label: "输入Token", width: 100, align: "right" },
  { prop: "completionTokens", label: "输出Token", width: 100, align: "right" },
  { prop: "totalTokens", label: "总Token", width: 100, align: "right" },
  { prop: "modelName", label: "模型", width: 120 },
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
    width: 200,
    showOverflowTooltip: true,
    render: (scope: any) => {
      return scope.row.status === 0 ? scope.row.errorMsg || "--" : "--";
    }
  },
  {
    prop: "createTime",
    label: "调用时间",
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

// ==================== 配额管理 ====================
const quotaTableRef = ref<ProTableInstance>();
const quotaInitParam = reactive({});

const getQuotaTableList = (params: any) => {
  return getAiQuotaList(params);
};

const quotaColumns = reactive<ColumnProps<AiUserQuotaVO>[]>([
  { prop: "userId", label: "用户ID", width: 90 },
  { prop: "userName", label: "用户名", width: 120 },
  { prop: "monthlyTokenLimit", label: "Token 限额", width: 120, align: "right" },
  { prop: "monthlyRequestLimit", label: "请求限额", width: 100, align: "right" },
  { prop: "usedTokens", label: "已用 Token", width: 120, align: "right" },
  { prop: "usedRequests", label: "已用请求", width: 100, align: "right" },
  {
    prop: "quotaResetDate",
    label: "重置日期",
    width: 120,
    render: (scope: any) => {
      return scope.row.quotaResetDate || "--";
    }
  },
  {
    prop: "keyword",
    label: "搜索",
    isShow: false,
    search: { el: "input", props: { placeholder: "搜索用户名" } }
  },
  { prop: "operation", label: "操作", fixed: "right", width: 120 }
]);

// ==================== 配额弹窗 ====================
const quotaDialogVisible = ref(false);
const quotaForm = reactive({
  userId: 0,
  userName: "",
  monthlyTokenLimit: 100000,
  monthlyRequestLimit: 50
});

const openQuotaDialog = (row: AiUserQuotaVO) => {
  quotaForm.userId = row.userId;
  quotaForm.userName = row.userName;
  quotaForm.monthlyTokenLimit = row.monthlyTokenLimit;
  quotaForm.monthlyRequestLimit = row.monthlyRequestLimit;
  quotaDialogVisible.value = true;
};

const handleUpdateQuota = async () => {
  try {
    await updateAiQuota(quotaForm.userId, {
      monthlyTokenLimit: quotaForm.monthlyTokenLimit,
      monthlyRequestLimit: quotaForm.monthlyRequestLimit
    });
    ElMessage.success("配额修改成功");
    quotaDialogVisible.value = false;
    quotaTableRef.value?.getTableList();
  } catch {
    // error handled by http interceptor
  }
};
</script>

<style scoped lang="scss">
.ai-monitor {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.stats-row {
  .stat-card {
    text-align: center;

    .stat-label {
      font-size: 14px;
      color: #909399;
      margin-bottom: 8px;
    }

    .stat-value {
      font-size: 28px;
      font-weight: 600;
      color: #303133;
    }
  }
}

.main-row {
  display: flex;
  align-items: stretch;

  .el-col {
    display: flex;
  }

  .ranking-card {
    flex: 1;
    display: flex;
    flex-direction: column;

    :deep(.el-card__body) {
      flex: 1;
      display: flex;
      flex-direction: column;
      overflow: hidden;
    }
  }

  .quota-card {
    flex: 1;
    display: flex;
    flex-direction: column;

    :deep(.el-card__body) {
      flex: 1;
      display: flex;
      flex-direction: column;
      padding: 0;
      overflow: hidden;
    }
  }
}

.log-card {
  :deep(.el-card__body) {
    padding: 0;
  }
}
</style>
