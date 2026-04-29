<template>
  <div class="dashboard-box">
    <el-row :gutter="20" class="mb20">
      <el-col :xs="12" :sm="12" :md="6" :lg="6" :xl="6" v-for="item in statCards" :key="item.key">
        <div class="stat-card" :style="{ borderTop: `3px solid ${item.color}` }">
          <div class="stat-card__content">
            <div class="stat-card__number">{{ item.value }}</div>
            <div class="stat-card__label">{{ item.label }}</div>
          </div>
          <el-icon :size="40" :style="{ color: item.color }" class="stat-card__icon">
            <component :is="item.icon" />
          </el-icon>
        </div>
      </el-col>
    </el-row>

    <el-row :gutter="20" class="mb20">
      <el-col :xs="24" :sm="24" :md="16" :lg="16" :xl="16">
        <el-card shadow="hover">
          <ECharts :option="lineOption" height="350px" />
        </el-card>
      </el-col>
      <el-col :xs="24" :sm="24" :md="8" :lg="8" :xl="8">
        <el-card shadow="hover">
          <ECharts :option="pieOption" height="350px" />
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="20">
      <el-col :span="24">
        <el-card shadow="hover">
          <ECharts :option="barOption" height="300px" />
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup lang="ts" name="dashboard">
import { ref, computed, onMounted } from "vue";
import { UserFilled, Document, User, EditPen } from "@element-plus/icons-vue";
import ECharts from "@/components/ECharts/index.vue";
import { ECOption } from "@/components/ECharts/config";
import { getDashboardStats, type DashboardStats } from "@/api/modules/dashboard";

const loading = ref(false);
const stats = ref<DashboardStats>({
  totalUsers: 0,
  totalNotes: 0,
  todayNewUsers: 0,
  todayNewNotes: 0,
  dateList: [],
  newUserList: [],
  newNoteList: [],
  statusDistribution: [],
  hotNotes: []
});

/** 统计卡片配置 */
const statCards = computed(() => [
  { key: "totalUsers", label: "总用户数", value: stats.value.totalUsers, icon: UserFilled, color: "#409eff" },
  { key: "totalNotes", label: "总笔记数", value: stats.value.totalNotes, icon: Document, color: "#67c23a" },
  { key: "todayNewUsers", label: "今日新增用户", value: stats.value.todayNewUsers, icon: User, color: "#e6a23c" },
  { key: "todayNewNotes", label: "今日新增笔记", value: stats.value.todayNewNotes, icon: EditPen, color: "#f56c6c" }
]);

/** 增长趋势折线图 */
const lineOption = computed<ECOption>(() => ({
  title: { text: "增长趋势（近7天）", left: "center", textStyle: { fontSize: 15 } },
  tooltip: { trigger: "axis" },
  legend: { data: ["新增用户", "新增笔记"], bottom: 0 },
  grid: { top: 50, right: 20, bottom: 40, left: 50 },
  xAxis: { type: "category", data: stats.value.dateList, boundaryGap: false },
  yAxis: { type: "value", minInterval: 1 },
  series: [
    {
      name: "新增用户",
      type: "line",
      data: stats.value.newUserList,
      smooth: true,
      itemStyle: { color: "#409eff" },
      areaStyle: { color: "rgba(64,158,255,0.1)" }
    },
    {
      name: "新增笔记",
      type: "line",
      data: stats.value.newNoteList,
      smooth: true,
      itemStyle: { color: "#67c23a" },
      areaStyle: { color: "rgba(103,194,58,0.1)" }
    }
  ]
}));

/** 笔记状态饼图 */
const pieOption = computed<ECOption>(() => {
  const colorMap: Record<string, string> = { 草稿: "#909399", 正常: "#67c23a", 回收站: "#e6a23c", 下架: "#f56c6c" };
  return {
    title: { text: "笔记状态分布", left: "center", textStyle: { fontSize: 15 } },
    tooltip: { trigger: "item", formatter: "{b}: {c} ({d}%)" },
    legend: { bottom: 0 },
    color: stats.value.statusDistribution.map(item => colorMap[item.name] || "#409eff"),
    series: [
      {
        type: "pie",
        radius: ["40%", "70%"],
        data: stats.value.statusDistribution,
        emphasis: { itemStyle: { shadowBlur: 10, shadowOffsetX: 0, shadowColor: "rgba(0,0,0,0.3)" } },
        label: { formatter: "{b}\n{d}%" },
        itemStyle: { borderRadius: 6, borderColor: "#fff", borderWidth: 2 }
      }
    ]
  };
});

/** 热门笔记 TOP5 横向柱状图 */
const barOption = computed<ECOption>(() => ({
  title: { text: "热门笔记 TOP5", left: "center", textStyle: { fontSize: 15 } },
  tooltip: { trigger: "axis", axisPointer: { type: "shadow" } },
  grid: { top: 50, right: 40, bottom: 20, left: 120 },
  xAxis: { type: "value", name: "浏览量" },
  yAxis: {
    type: "category",
    data: stats.value.hotNotes.map(n => (n.title.length > 10 ? n.title.substring(0, 10) + "..." : n.title)),
    inverse: true
  },
  series: [
    {
      type: "bar",
      data: stats.value.hotNotes.map(n => n.viewCount),
      itemStyle: { color: "#409eff", borderRadius: [0, 4, 4, 0] },
      barWidth: 20,
      label: { show: true, position: "right", formatter: "{c}" }
    }
  ]
}));

/** 加载统计数据 */
const fetchStats = async () => {
  loading.value = true;
  try {
    const { data } = await getDashboardStats();
    if (data) stats.value = data;
  } catch (e) {
    console.warn("加载仪表盘数据失败:", e);
  } finally {
    loading.value = false;
  }
};

onMounted(() => {
  fetchStats();
});
</script>

<style scoped lang="scss">
.dashboard-box {
  padding: 4px;
}

.stat-card {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 20px 24px;
  background: #fff;
  border-radius: 6px;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.06);
  transition: box-shadow 0.3s;

  &:hover {
    box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
  }

  .stat-card__content {
    .stat-card__number {
      font-size: 28px;
      font-weight: 700;
      color: #303133;
      line-height: 1.2;
    }
    .stat-card__label {
      font-size: 13px;
      color: #909399;
      margin-top: 6px;
    }
  }

  .stat-card__icon {
    opacity: 0.8;
  }
}

.mb20 {
  margin-bottom: 20px;
}

:deep(.el-card__body) {
  padding: 12px;
}
</style>
