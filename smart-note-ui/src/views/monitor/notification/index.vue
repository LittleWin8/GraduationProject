<template>
  <div class="notification-container">
    <el-card shadow="hover">
      <template #header>
        <div class="card-header">
          <span>发送系统通知</span>
        </div>
      </template>

      <el-form ref="formRef" :model="formData" :rules="rules" label-width="100px" label-position="left" class="notification-form">
        <el-form-item label="通知类型" prop="type">
          <el-select v-model="formData.type" placeholder="请选择通知类型" style="width: 100%">
            <el-option label="审核通过" :value="3" />
            <el-option label="审核不通过" :value="4" />
            <el-option label="违规下架" :value="5" />
            <el-option label="系统公告" :value="6" />
          </el-select>
        </el-form-item>

        <el-form-item label="通知标题" prop="title">
          <el-input v-model="formData.title" placeholder="请输入通知标题" maxlength="50" show-word-limit />
        </el-form-item>

        <el-form-item label="通知内容" prop="content">
          <el-input
            v-model="formData.content"
            type="textarea"
            placeholder="请输入通知内容"
            :rows="4"
            maxlength="500"
            show-word-limit
          />
        </el-form-item>

        <el-form-item label="接收范围" prop="scope">
          <el-radio-group v-model="formData.scope">
            <el-radio value="all">全部用户</el-radio>
            <el-radio value="specific">指定用户</el-radio>
          </el-radio-group>
        </el-form-item>

        <el-form-item v-if="formData.scope === 'specific'" label="指定用户" prop="userIds">
          <el-input v-model="formData.userIds" placeholder="请输入用户ID，多个用英文逗号分隔，如：1,2,3" />
        </el-form-item>

        <el-form-item>
          <el-button type="primary" :loading="submitting" v-auth="'sys:notification:send'" @click="handleSubmit">发送</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup lang="ts" name="notification">
import { ref, reactive } from "vue";
import { ElMessage } from "element-plus";
import type { FormInstance, FormRules } from "element-plus";
import { sendNotification } from "@/api/modules/notification";

const formRef = ref<FormInstance>();
const submitting = ref(false);

const formData = reactive({
  type: null as number | null,
  title: "",
  content: "",
  scope: "all",
  userIds: ""
});

const rules = reactive<FormRules>({
  type: [{ required: true, message: "请选择通知类型", trigger: "change" }],
  title: [{ required: true, message: "请输入通知标题", trigger: "blur" }],
  content: [{ required: true, message: "请输入通知内容", trigger: "blur" }]
});

const handleSubmit = async () => {
  const valid = await formRef.value?.validate().catch(() => false);
  if (!valid) return;

  if (formData.scope === "specific") {
    if (!formData.userIds.trim()) {
      ElMessage.warning("请输入指定用户ID");
      return;
    }
    const idList = formData.userIds.split(",").map(s => Number(s.trim()));
    if (idList.some(id => isNaN(id) || id <= 0)) {
      ElMessage.warning("用户ID格式不正确，请输入正整数并用逗号分隔");
      return;
    }
  }

  submitting.value = true;
  try {
    const payload: any = {
      title: formData.title,
      content: formData.content,
      type: formData.type
    };

    if (formData.scope === "specific") {
      payload.userIds = formData.userIds.split(",").map(s => Number(s.trim()));
    }

    await sendNotification(payload);
    ElMessage.success("通知发送成功");
    handleReset();
  } catch (e: any) {
    console.error("发送通知失败:", e);
  } finally {
    submitting.value = false;
  }
};

const handleReset = () => {
  formRef.value?.resetFields();
  formData.type = null;
  formData.title = "";
  formData.content = "";
  formData.scope = "all";
  formData.userIds = "";
};
</script>

<style scoped lang="scss">
.notification-container {
  padding: 20px;
  max-width: 800px;

  .card-header {
    font-size: 16px;
    font-weight: 600;
    color: #303133;
  }

  .notification-form {
    margin-top: 10px;
  }
}
</style>
