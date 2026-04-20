<template>
  <el-dialog v-model="dialogVisible" title="更换绑定手机" width="500px" destroy-on-close>
    <el-form ref="phoneFormRef" :model="phoneForm" :rules="phoneRules" label-width="100px" label-position="left">
      <el-form-item label="手机号码" prop="phone">
        <el-input v-model="phoneForm.phone" placeholder="请输入新手机号" />
      </el-form-item>
      <el-form-item label="验证码" prop="code">
        <div class="code-input">
          <el-input v-model="phoneForm.code" placeholder="验证码" />
          <el-button :disabled="!!timer" @click="getCode" class="code-btn">
            {{ timer ? `${count}s后获取` : "获取验证码" }}
          </el-button>
        </div>
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="dialogVisible = false">取消</el-button>
      <el-button type="primary" :loading="loading" @click="submit">立即更换</el-button>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import { ref, reactive } from "vue";
import { ElMessage, type FormInstance } from "element-plus";
import { updateSecurityApi } from "@/api/modules/login";
import { useUserStore } from "@/stores/modules/user";

const dialogVisible = ref(false);
const loading = ref(false);
const phoneFormRef = ref<FormInstance>();

const userStore = useUserStore();

const phoneForm = reactive({
  phone: "",
  code: ""
});

const phoneRules = reactive({
  phone: [
    { required: true, message: "请输入手机号" },
    { pattern: /^1[3-9]\d{9}$/, message: "手机号格式不正确", trigger: "blur" }
  ],
  code: [{ required: true, message: "请输入验证码" }]
});

// 验证码倒计时逻辑
const count = ref(60);
const timer = ref<any>(null);
const getCode = () => {
  if (!/^1[3-9]\d{9}$/.test(phoneForm.phone)) return ElMessage.warning("请先输入正确的手机号");
  ElMessage.success("验证码已发送（模拟）");
  timer.value = setInterval(() => {
    if (count.value > 0) count.value--;
    else {
      clearInterval(timer.value);
      timer.value = null;
      count.value = 60;
    }
  }, 1000);
};

const acceptParams = () => {
  phoneForm.phone = "";
  phoneForm.code = "";
  dialogVisible.value = true;
};

const submit = async () => {
  if (!phoneFormRef.value) return;
  await phoneFormRef.value.validate(async valid => {
    if (!valid) return;
    try {
      loading.value = true;
      // 核心修改：映射到统一接口
      await updateSecurityApi({
        type: 2, // 2 代表更换手机
        oldField: phoneForm.code, // 验证码作为旧凭证
        newField: phoneForm.phone // 新手机号作为新值
      });

      ElMessage.success("手机号更换成功");

      // 成功后刷新用户信息，确保页面显示的手机号更新
      await userStore.getUserInfoAction();

      dialogVisible.value = false;
    } finally {
      loading.value = false;
    }
  });
};

defineExpose({ acceptParams });
</script>

<style scoped>
.code-input {
  display: flex;
  gap: 10px;
  width: 100%;
}
.code-btn {
  width: 120px;
}
</style>
