<template>
  <el-drawer v-model="drawerVisible" :destroy-on-close="true" size="450px" :title="`${drawerProps.title}字典数据`">
    <el-form
      ref="ruleFormRef"
      label-width="100px"
      label-suffix=" :"
      :rules="rules"
      :disabled="drawerProps.isView"
      :model="drawerProps.row"
      :hide-required-asterisk="drawerProps.isView"
    >
      <el-form-item label="字典类型" prop="dictType">
        <el-input v-model="drawerProps.row!.dictType" disabled></el-input>
      </el-form-item>

      <el-form-item label="数据标签" prop="dictLabel">
        <el-input v-model="drawerProps.row!.dictLabel" placeholder="如：正常、男、北京" clearable></el-input>
      </el-form-item>

      <el-form-item label="数据键值" prop="dictValue">
        <el-input v-model="drawerProps.row!.dictValue" placeholder="如：1、MALE、BJ" clearable></el-input>
      </el-form-item>

      <el-form-item label="UI样式" prop="tagType">
        <el-select v-model="drawerProps.row!.tagType" placeholder="选择标签展示样式" clearable>
          <el-option label="Primary (蓝色)" value="primary" />
          <el-option label="Success (绿色)" value="success" />
          <el-option label="Info (灰色)" value="info" />
          <el-option label="Warning (黄色)" value="warning" />
          <el-option label="Danger (红色)" value="danger" />
        </el-select>
      </el-form-item>

      <el-form-item label="显示排序" prop="sortOrder">
        <el-input-number v-model="drawerProps.row!.sortOrder" :min="0" controls-position="right" />
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

<script setup lang="ts" name="DictDataDrawer">
import { ref, reactive } from "vue";
import { ElMessage, FormInstance } from "element-plus";

// 对应后端的 SysDictData 实体
interface DictDataRow {
  dataId?: number;
  dictType: string;
  dictLabel: string;
  dictValue: string;
  tagType: string;
  sortOrder: number;
  status: number;
  remark?: string;
}

interface DrawerProps {
  title: string;
  isView: boolean;
  row: Partial<DictDataRow>;
  api?: (params: any) => Promise<any>;
  getTableList?: () => void;
}

const drawerVisible = ref(false);
const drawerProps = ref<DrawerProps>({
  isView: false,
  title: "",
  row: {}
});

// 校验规则
const rules = reactive({
  dictLabel: [{ required: true, message: "请填写数据标签" }],
  dictValue: [{ required: true, message: "请填写数据键值" }],
  status: [{ required: true, message: "请选择状态" }],
  sortOrder: [{ required: true, message: "请选择排序" }]
});

// 接收父组件参数
const acceptParams = (params: DrawerProps) => {
  drawerProps.value = params;

  // 新增时的初始化默认值
  if (params.title === "新增") {
    drawerProps.value.row.status = 1;
    drawerProps.value.row.sortOrder = 0;
    drawerProps.value.row.tagType = "primary";
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
