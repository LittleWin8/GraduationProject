<template>
  <el-drawer v-model="drawerVisible" :destroy-on-close="true" size="450px" :title="`${drawerProps.title}字典`">
    <el-form
      ref="ruleFormRef"
      label-width="100px"
      label-suffix=" :"
      :rules="rules"
      :disabled="drawerProps.isView"
      :model="drawerProps.row"
      :hide-required-asterisk="drawerProps.isView"
    >
      <el-form-item label="字典名称" prop="dictName">
        <el-input v-model="drawerProps.row!.dictName" placeholder="请填写字典名称" clearable></el-input>
      </el-form-item>
      <el-form-item label="字典类型" prop="dictType">
        <el-input v-model="drawerProps.row!.dictType" placeholder="请填写字典类型" clearable></el-input>
      </el-form-item>
      <el-form-item label="状态" prop="status">
        <el-radio-group v-model="drawerProps.row!.status">
          <el-radio :label="1">正常</el-radio>
          <el-radio :label="0">禁用</el-radio>
        </el-radio-group>
      </el-form-item>
      <el-form-item label="备注" prop="remark">
        <el-input v-model="drawerProps.row!.remark" type="textarea" placeholder="请填写备注" :rows="3" clearable></el-input>
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="drawerVisible = false">取消</el-button>
      <el-button v-show="!drawerProps.isView" type="primary" @click="handleSubmit">确定</el-button>
    </template>
  </el-drawer>
</template>

<script setup lang="ts" name="DictDrawer">
import { ref, reactive } from "vue";
import { ElMessage, FormInstance } from "element-plus";

// 根据后端 SysDictType 实体类定义前端接口
interface DictRow {
  dictId?: number;
  dictName: string;
  dictType: string;
  status: number;
  remark?: string;
}

interface DrawerProps {
  title: string;
  isView: boolean;
  row: Partial<DictRow>;
  api?: (params: any) => Promise<any>;
  getTableList?: () => void;
}

const drawerVisible = ref(false);
const drawerProps = ref<DrawerProps>({
  isView: false,
  title: "",
  row: {
    status: 1 // 默认正常
  }
});

// 校验规则
const rules = reactive({
  dictName: [{ required: true, message: "请填写字典名称" }],
  dictType: [{ required: true, message: "请填写字典类型" }],
  status: [{ required: true, message: "请选择状态" }]
});

// 接收父组件参数
const acceptParams = (params: DrawerProps) => {
  drawerProps.value = params;
  // 如果是新增，确保有默认值
  if (!drawerProps.value.row.status && drawerProps.value.row.status !== 0) {
    drawerProps.value.row.status = 1;
  }
  drawerVisible.value = true;
};

// 提交数据
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

defineExpose({
  acceptParams
});
</script>
