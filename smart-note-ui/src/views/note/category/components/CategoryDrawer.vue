<template>
  <el-drawer v-model="drawerVisible" :destroy-on-close="true" size="500px" :title="`${drawerProps.title}分类`">
    <el-form ref="ruleFormRef" label-width="100px" label-suffix=" :" :rules="rules" :model="drawerProps.row">
      <el-form-item label="分类名称" prop="name">
        <el-input v-model="drawerProps.row!.name" placeholder="请填写分类名称" clearable></el-input>
      </el-form-item>
      <el-form-item label="父分类" prop="parentId">
        <el-tree-select
          v-model="drawerProps.row!.parentId"
          :data="treeData"
          :props="{ label: 'name', children: 'children' }"
          node-key="categoryId"
          placeholder="选择父分类（空为顶级）"
          check-strictly
          clearable
          default-expand-all
          style="width: 100%"
        />
      </el-form-item>
      <el-form-item label="排序" prop="sortOrder">
        <el-input-number v-model="drawerProps.row!.sortOrder" :min="0" controls-position="right" />
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="drawerVisible = false">取消</el-button>
      <el-button type="primary" @click="handleSubmit">确定</el-button>
    </template>
  </el-drawer>
</template>

<script setup lang="ts" name="CategoryDrawer">
import { ref, reactive } from "vue";
import { ElMessage, FormInstance } from "element-plus";
import { getCategoryTree, type CategoryTreeNode, type CategoryFormData } from "@/api/modules/category";

interface DrawerProps {
  title: string;
  row: Partial<CategoryFormData> & { categoryId?: number };
  api?: (params: any) => Promise<any>;
  getTableList?: () => void;
}

const drawerVisible = ref(false);
const drawerProps = ref<DrawerProps>({
  title: "",
  row: {}
});

const treeData = ref<CategoryTreeNode[]>([]);

const rules = reactive({
  name: [{ required: true, message: "请填写分类名称" }]
});

const acceptParams = async (params: DrawerProps) => {
  drawerProps.value = params;
  // 新增默认值
  if (drawerProps.value.row.parentId === undefined) {
    drawerProps.value.row.parentId = 0;
  }
  if (drawerProps.value.row.sortOrder === undefined) {
    drawerProps.value.row.sortOrder = 0;
  }
  // 加载分类树供父分类选择
  try {
    const { data } = await getCategoryTree();
    treeData.value = data || [];
  } catch (e) {
    console.warn("加载分类树失败:", e);
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
