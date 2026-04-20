<template>
  <el-dialog v-model="dialogVisible" title="修改账户密码" width="500px" destroy-on-close>
    <el-form ref="pwdFormRef" :model="pwdForm" :rules="pwdRules" label-width="100px" label-position="left">
      <el-form-item label="原密码" prop="oldPassword">
        <el-input v-model="pwdForm.oldPassword" type="password" placeholder="请输入原密码" show-password />
      </el-form-item>
      <el-form-item label="新密码" prop="newPassword">
        <el-input v-model="pwdForm.newPassword" type="password" placeholder="请输入新密码" show-password />
      </el-form-item>
      <el-form-item label="确认新密码" prop="confirmPassword">
        <el-input v-model="pwdForm.confirmPassword" type="password" placeholder="请再次输入新密码" show-password />
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="dialogVisible = false">取消</el-button>
      <el-button type="primary" :loading="loading" @click="submit">确认修改</el-button>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import { ref, reactive } from "vue";
import { ElMessage, type FormInstance } from "element-plus";
// import { resetUserPassword } from "@/api/modules/user";

const dialogVisible = ref(false);
const loading = ref(false);
const pwdFormRef = ref<FormInstance>();

const pwdForm = reactive({
  oldPassword: "",
  newPassword: "",
  confirmPassword: ""
});

const validateConfirm = (rule: any, value: any, callback: any) => {
  if (value !== pwdForm.newPassword) {
    callback(new Error("两次输入的密码不一致"));
  } else {
    callback();
  }
};

const pwdRules = reactive({
  oldPassword: [{ required: true, message: "请输入原密码", trigger: "blur" }],
  newPassword: [
    { required: true, message: "请输入新密码", trigger: "blur" },
    { min: 6, message: "密码长度不能少于6位", trigger: "blur" }
  ],
  confirmPassword: [
    { required: true, message: "请再次输入新密码", trigger: "blur" },
    { validator: validateConfirm, trigger: "blur" }
  ]
});

const acceptParams = () => {
  dialogVisible.value = true;
};

const submit = async () => {
  if (!pwdFormRef.value) return;
  await pwdFormRef.value.validate(async valid => {
    if (!valid) return;
    try {
      loading.value = true;
      // 调用后端接口
      //   await resetUserPassword({
      //     password: pwdForm.newPassword
      //   });
      ElMessage.success("密码修改成功，请牢记新密码");
      dialogVisible.value = false;
    } finally {
      loading.value = false;
    }
  });
};

defineExpose({ acceptParams });
</script>
