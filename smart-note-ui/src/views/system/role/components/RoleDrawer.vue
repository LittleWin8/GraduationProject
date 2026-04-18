<template>
  <el-drawer v-model="drawerVisible" :destroy-on-close="true" size="500px" :title="`${drawerProps.title}角色`">
    <el-form ref="ruleFormRef" label-width="100px" label-suffix=" :" :rules="rules" :model="drawerProps.row">
      <el-form-item label="角色名称" prop="roleName">
        <el-input v-model="drawerProps.row!.roleName" placeholder="请填写角色名称" clearable></el-input>
      </el-form-item>
      <el-form-item label="权限字符" prop="roleKey">
        <el-input v-model="drawerProps.row!.roleKey" placeholder="请填写权限字符" clearable></el-input>
      </el-form-item>
      <el-form-item label="显示顺序" prop="sortOrder">
        <el-input-number v-model="drawerProps.row!.sortOrder" :min="0" controls-position="right" />
      </el-form-item>
      <el-form-item label="状态" prop="status">
        <el-radio-group v-model="drawerProps.row!.status">
          <el-radio :label="1">正常</el-radio>
          <el-radio :label="0">停用</el-radio>
        </el-radio-group>
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="drawerVisible = false">取消</el-button>
      <el-button type="primary" @click="handleSubmit">确定</el-button>
    </template>
  </el-drawer>
</template>

<script setup lang="ts" name="RoleDrawer">
import { ref, reactive } from "vue";
import { ElMessage, FormInstance } from "element-plus";
import { Role } from "@/api/interface/index";

interface DrawerProps {
  title: string;
  row: Partial<Role.RoleDTO>;
  api?: (params: any) => Promise<any>;
  getTableList?: () => void;
}

const drawerVisible = ref(false);
const drawerProps = ref<DrawerProps>({
  title: "",
  row: {}
});

const rules = reactive({
  roleName: [{ required: true, message: "请填写角色名称" }],
  roleKey: [{ required: true, message: "请填写权限字符" }]
});

const acceptParams = (params: DrawerProps) => {
  drawerProps.value = params;
  if (drawerProps.value.row.status === undefined) {
    drawerProps.value.row.status = 1;
  }
  if (drawerProps.value.row.sortOrder === undefined) {
    drawerProps.value.row.sortOrder = 0;
  }
  drawerVisible.value = true;
};

const ruleFormRef = ref<FormInstance>();
const handleSubmit = () => {
  ruleFormRef.value!.validate(async valid => {
    if (!valid) return;
    try {
      await drawerProps.value.api!(drawerProps.value.row);
      ElMessage.success({ message: `${drawerProps.value.title}成功！` });
      drawerProps.value.getTableList!();
      drawerVisible.value = false;
    } catch (error) {
      console.log(error);
    }
  });
};

defineExpose({ acceptParams });
</script>
