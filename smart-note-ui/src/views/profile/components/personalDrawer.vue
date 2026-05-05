<template>
  <el-drawer v-model="drawerVisible" :destroy-on-close="true" size="450px" title="编辑个人资料">
    <el-form ref="ruleFormRef" label-width="100px" label-suffix=" :" :rules="rules" :model="drawerProps.row">
      <el-form-item label="用户头像" prop="avatar">
        <UploadImg v-model:image-url="drawerProps.row!.avatar" width="120px" height="120px" :file-size="3" border-radius="50%">
          <template #empty>
            <el-icon><Avatar /></el-icon>
            <span>请上传头像</span>
          </template>
          <template #tip> 点击图片可更换 </template>
        </UploadImg>
      </el-form-item>

      <el-form-item label="用户ID">
        <el-input v-model="drawerProps.row!.userId" disabled />
      </el-form-item>

      <el-form-item label="登录账号">
        <el-input v-model="drawerProps.row!.identifier" disabled />
      </el-form-item>

      <el-form-item label="用户昵称" prop="nickname">
        <el-input v-model="drawerProps.row!.nickname" placeholder="请填写用户昵称" clearable />
      </el-form-item>

      <el-form-item label="用户性别">
        <el-radio-group v-model="drawerProps.row!.gender">
          <el-radio label="1">男</el-radio>
          <el-radio label="2">女</el-radio>
        </el-radio-group>
      </el-form-item>

      <!-- <el-form-item label="手机号码" prop="phone">
        <el-input v-model="drawerProps.row!.phone" placeholder="请填写手机号码" clearable />
      </el-form-item> -->

      <el-form-item label="电子邮箱" prop="email">
        <el-input v-model="drawerProps.row!.email" placeholder="请填写邮箱" clearable />
      </el-form-item>

      <el-form-item label="所在城市">
        <el-input v-model="drawerProps.row!.city" placeholder="请填写城市" clearable />
      </el-form-item>

      <el-form-item label="出生日期">
        <el-date-picker v-model="drawerProps.row!.birthday" type="date" value-format="YYYY-MM-DD" style="width: 100%" />
      </el-form-item>

      <el-form-item label="个人签名">
        <el-input v-model="drawerProps.row!.signature" type="textarea" :rows="3" />
      </el-form-item>
    </el-form>

    <template #footer>
      <el-button @click="drawerVisible = false">取消</el-button>
      <el-button type="primary" :loading="submitLoading" @click="handleSubmit">确定</el-button>
    </template>
  </el-drawer>
</template>

<script setup lang="ts" name="UserDrawer">
import { ref, reactive } from "vue";
import { ElMessage, FormInstance, FormRules } from "element-plus";
import { useUserStore } from "@/stores/modules/user";
import UploadImg from "@/components/Upload/Img.vue";
import { Avatar } from "@element-plus/icons-vue";

const userStore = useUserStore();
const drawerVisible = ref(false);
const submitLoading = ref(false);
const ruleFormRef = ref<FormInstance>();

interface DrawerProps {
  row: any;
  api?: (params: any) => Promise<any>;
}

const drawerProps = ref<DrawerProps>({ row: {} });

const rules = reactive<FormRules>({
  nickname: [{ required: true, message: "请填写用户昵称", trigger: "blur" }],
  phone: [{ pattern: /^1[3-9]\d{9}$/, message: "手机号格式不正确", trigger: "blur" }],
  email: [{ type: "email", message: "邮箱格式不正确", trigger: "blur" }]
});

const acceptParams = (params: DrawerProps) => {
  drawerProps.value.row = JSON.parse(JSON.stringify(params.row));
  drawerProps.value.api = params.api;
  drawerVisible.value = true;
};

const handleSubmit = () => {
  if (!ruleFormRef.value) return;
  ruleFormRef.value.validate(async valid => {
    if (!valid) return;
    try {
      submitLoading.value = true;
      // 执行复用的 editUser 接口
      await drawerProps.value.api!(drawerProps.value.row);

      // 成功后必须调用 Action 重新拉取用户信息
      // 这样主页 index.vue、Header 顶栏的名字和头像都会即时同步更新
      await userStore.getUserInfoAction();

      ElMessage.success("修改成功！");
      drawerVisible.value = false;
    } catch (error) {
      console.log(error);
    } finally {
      submitLoading.value = false;
    }
  });
};

defineExpose({ acceptParams });
</script>
