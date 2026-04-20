<template>
  <el-drawer v-model="drawerVisible" :destroy-on-close="true" size="550px" :title="`${drawerProps.title}用户`">
    <el-form
      ref="ruleFormRef"
      label-width="100px"
      label-suffix=" :"
      :rules="rules"
      :disabled="drawerProps.isView"
      :model="drawerProps.row"
      :hide-required-asterisk="drawerProps.isView"
    >
      <el-form-item label="用户头像" prop="avatar">
        <UploadImg v-model:image-url="drawerProps.row!.avatar" width="120px" height="120px" :file-size="3">
          <template #empty>
            <el-icon><Avatar /></el-icon>
            <span>请上传头像</span>
          </template>
          <template #tip> 头像大小不能超过 3M </template>
        </UploadImg>
      </el-form-item>

      <el-form-item label="登录账号" prop="identifier">
        <el-input
          v-model="drawerProps.row!.identifier"
          placeholder="请填写登录账号"
          clearable
          :disabled="drawerProps.isView"
        ></el-input>
      </el-form-item>

      <el-form-item label="用户昵称" prop="nickname">
        <el-input v-model="drawerProps.row!.nickname" placeholder="请填写用户昵称" clearable></el-input>
      </el-form-item>

      <el-form-item label="认证类型" prop="authType">
        <el-tag type="info">{{ drawerProps.row!.authType === "password" ? "管理端用户" : "客户端用户" }}</el-tag>
      </el-form-item>

      <el-form-item label="账号状态" prop="status">
        <el-radio-group v-model="drawerProps.row!.status">
          <el-radio v-for="item in drawerProps.dicts?.status" :key="item.dictValue" :label="item.dictValue">
            {{ item.dictLabel }}
          </el-radio>
        </el-radio-group>
      </el-form-item>

      <el-form-item label="用户性别" prop="gender">
        <el-select v-model="drawerProps.row!.gender" placeholder="请选择性别" clearable style="width: 100%">
          <el-option
            v-for="item in drawerProps.dicts?.gender"
            :key="item.dictValue"
            :label="item.dictLabel"
            :value="item.dictValue"
          />
        </el-select>
      </el-form-item>

      <el-form-item label="手机号码" prop="phone">
        <el-input v-model="drawerProps.row!.phone" placeholder="请填写手机号码" clearable></el-input>
      </el-form-item>

      <el-form-item label="电子邮箱" prop="email">
        <el-input v-model="drawerProps.row!.email" placeholder="请填写邮箱" clearable></el-input>
      </el-form-item>

      <el-form-item label="出生日期" prop="birthday">
        <el-date-picker
          v-model="drawerProps.row!.birthday"
          type="date"
          placeholder="选择日期"
          value-format="YYYY-MM-DD"
          style="width: 100%"
        />
      </el-form-item>

      <el-form-item label="所在城市" prop="city">
        <el-input v-model="drawerProps.row!.city" placeholder="请填写城市" clearable></el-input>
      </el-form-item>

      <el-form-item label="用户角色" prop="roleIds">
        <el-select
          v-model="drawerProps.row!.roleIds"
          multiple
          collapse-tags
          collapse-tags-tooltip
          :max-collapse-tags="3"
          placeholder="请选择角色"
          style="width: 100%"
        >
          <el-option
            v-for="item in drawerProps.dicts?.roles"
            :key="item.dictValue"
            :label="item.dictLabel"
            :value="item.dictValue"
          />
        </el-select>
      </el-form-item>

      <el-form-item label="个人签名" prop="signature">
        <el-input v-model="drawerProps.row!.signature" type="textarea" :rows="3" placeholder="介绍一下自己吧"></el-input>
      </el-form-item>
    </el-form>

    <template #footer>
      <el-button @click="drawerVisible = false">取消</el-button>
      <el-button v-show="!drawerProps.isView" type="primary" @click="handleSubmit">确定</el-button>
    </template>
  </el-drawer>
</template>

<script setup lang="ts" name="UserDrawer">
import { ref, reactive } from "vue";
import { ElMessage, FormInstance } from "element-plus";
import UploadImg from "@/components/Upload/Img.vue";
import { Avatar } from "@element-plus/icons-vue";

const rules = reactive({
  nickname: [{ required: true, message: "请填写用户昵称" }],
  identifier: [{ required: true, message: "请填写用户账号" }],
  gender: [{ required: true, message: "请选择性别" }],
  status: [{ required: true, message: "请选择状态" }],
  phone: [{ pattern: /^1[3-9]\d{9}$/, message: "手机号格式不正确", trigger: "blur" }],
  email: [{ type: "email", message: "邮箱格式不正确", trigger: "blur" }]
});

interface DrawerProps {
  title: string;
  isView: boolean;
  row: any;
  dicts?: any;
  api?: (params: any) => Promise<any>;
  getTableList?: () => void;
}

const drawerVisible = ref(false);
const drawerProps = ref<DrawerProps>({
  isView: false,
  title: "",
  row: {},
  dicts: {}
});

const acceptParams = (params: DrawerProps) => {
  drawerProps.value = params;
  drawerVisible.value = true;
};

const ruleFormRef = ref<FormInstance>();
const handleSubmit = () => {
  ruleFormRef.value!.validate(async valid => {
    if (!valid) return;
    try {
      await drawerProps.value.api!(drawerProps.value.row);
      ElMessage.success({ message: `${drawerProps.value.title}用户成功！` });
      drawerProps.value.getTableList!();
      drawerVisible.value = false;
    } catch (error) {
      console.log(error);
    }
  });
};

defineExpose({
  acceptParams
});
</script>

<style scoped>
.mr-1 {
  margin-right: 4px;
}
</style>
