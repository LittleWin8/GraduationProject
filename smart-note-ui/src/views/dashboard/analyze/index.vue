<template>
  <div class="analyze-box">
    <el-card shadow="hover" class="analyze-card">
      <template #header>
        <div style="display: flex; align-items: center; gap: 8px">
          <span>数据分析助手</span>
          <el-tag type="info" size="small">AI 驱动</el-tag>
        </div>
      </template>
      <div ref="chatContainer" class="chat-messages">
        <div v-if="!chatHistory.length" class="chat-empty">试试问我：本月新增了多少笔记？</div>
        <div v-for="(msg, i) in chatHistory" :key="i" class="chat-item" :class="msg.role">
          <div class="chat-bubble">
            <div v-if="msg.role === 'user'">{{ msg.question }}</div>
            <template v-if="msg.role === 'assistant'">
              <div>{{ msg.answer }}</div>
              <el-collapse v-if="msg.sql">
                <el-collapse-item title="查看SQL">
                  <code>{{ msg.sql }}</code>
                </el-collapse-item>
              </el-collapse>
              <el-table v-if="msg.data?.length" :data="msg.data" size="small" max-height="200" stripe style="margin-top: 8px">
                <el-table-column v-for="col in Object.keys(msg.data[0])" :key="col" :prop="col" :label="col" min-width="100" />
              </el-table>
            </template>
          </div>
        </div>
        <div v-if="analyzeLoading" class="chat-item assistant">
          <div class="chat-bubble">正在分析...</div>
        </div>
      </div>
      <div class="chat-input">
        <el-input
          v-model="analyzeQuestion"
          placeholder="输入数据问题"
          :disabled="analyzeLoading"
          @keyup.enter="onAnalyze"
          clearable
        >
          <template #append>
            <el-button :icon="Search" :loading="analyzeLoading" @click="onAnalyze" />
          </template>
        </el-input>
      </div>
    </el-card>
  </div>
</template>

<script setup lang="ts" name="analyze">
import { ref, nextTick } from "vue";
import { Search } from "@element-plus/icons-vue";
import { analyzeData } from "@/api/modules/dashboard";

const analyzeQuestion = ref("");
const analyzeLoading = ref(false);
const chatHistory = ref<any[]>([]);
const chatContainer = ref<HTMLElement>();

const onAnalyze = async () => {
  const q = analyzeQuestion.value.trim();
  if (!q || analyzeLoading.value) return;
  chatHistory.value.push({ role: "user", question: q });
  analyzeQuestion.value = "";
  analyzeLoading.value = true;
  nextTick(() => {
    if (chatContainer.value) chatContainer.value.scrollTop = chatContainer.value.scrollHeight;
  });
  try {
    const { data } = await analyzeData(q);
    if (data) chatHistory.value.push({ role: "assistant", answer: data.answer, sql: data.sql, data: data.data });
  } catch {
    chatHistory.value.push({ role: "assistant", answer: "分析失败" });
  } finally {
    analyzeLoading.value = false;
    nextTick(() => {
      if (chatContainer.value) chatContainer.value.scrollTop = chatContainer.value.scrollHeight;
    });
  }
};
</script>

<style scoped lang="scss">
.analyze-box {
  padding: 4px;
}

.analyze-card {
  .chat-messages {
    max-height: 400px;
    overflow-y: auto;
    margin-bottom: 12px;
  }

  .chat-empty {
    text-align: center;
    color: #909399;
    padding: 40px 0;
  }

  .chat-item {
    margin-bottom: 12px;

    &.user {
      text-align: right;

      .chat-bubble {
        display: inline-block;
        background: #ecf5ff;
        color: #409eff;
        padding: 8px 14px;
        border-radius: 12px 12px 2px 12px;
        max-width: 80%;
      }
    }

    &.assistant {
      text-align: left;

      .chat-bubble {
        display: inline-block;
        background: #f4f4f5;
        color: #303133;
        padding: 10px 14px;
        border-radius: 12px 12px 12px 2px;
        max-width: 90%;
      }
    }
  }

  .chat-input {
    border-top: 1px solid #f0f0f0;
    padding-top: 12px;
  }
}
</style>
