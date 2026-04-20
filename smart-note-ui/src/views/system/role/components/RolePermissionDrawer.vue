<template>
  <el-drawer v-model="drawerVisible" :destroy-on-close="true" size="450px" :title="`分配角色权限 - ${roleInfo.roleName}`">
    <div v-loading="loading" class="permission-tree-wrapper">
      <el-form label-position="top">
        <el-form-item label="菜单权限配置">
          <el-tree
            ref="treeRef"
            :data="menuTree"
            :props="treeProps"
            node-key="id"
            show-checkbox
            default-expand-all
            :default-checked-keys="checkedKeys"
          >
            <template #default="{ data }">
              <span class="custom-tree-node">
                <el-icon v-if="data.icon">
                  <component :is="data.icon" />
                </el-icon>
                <span style="margin-left: 5px">{{ data.label }}</span>
                <el-tag v-if="data.type === 'F'" size="small" type="warning" style="margin-left: 8px">按钮</el-tag>
                <el-tag v-else-if="data.type === 'M'" size="small" style="margin-left: 8px">目录</el-tag>
              </span>
            </template>
          </el-tree>
        </el-form-item>
      </el-form>
    </div>
    <template #footer>
      <el-button @click="drawerVisible = false">取消</el-button>
      <el-button type="primary" :loading="submitLoading" @click="handleSubmit">确定</el-button>
    </template>
  </el-drawer>
</template>

<script setup lang="ts" name="RolePermissionDrawer">
import { ref, nextTick } from "vue";
import { ElTree, ElMessage } from "element-plus";
import { Role } from "@/api/interface/index";
import { getMenuTree, getRoleMenus, assignMenus } from "@/api/modules/role";

const drawerVisible = ref(false);
const loading = ref(false);
const submitLoading = ref(false);
const treeRef = ref<InstanceType<typeof ElTree>>();

const roleInfo = ref<Partial<Role.SysRole>>({});
const menuTree = ref<any[]>([]);
const checkedKeys = ref<number[]>([]);

const treeProps = {
  label: "label",
  children: "children"
};

/**
 * @description 格式化后端原始树结构
 */
const formatTreeData = (data: Role.MenuTreeVO[]): any[] => {
  return data.map(item => ({
    id: item.menu.menuId,
    label: item.menu.title,
    icon: item.menu.icon,
    type: item.menu.menuType,
    children: item.children && item.children.length > 0 ? formatTreeData(item.children) : []
  }));
};

const acceptParams = async (row: Role.SysRole) => {
  roleInfo.value = { ...row };
  drawerVisible.value = true;
  loading.value = true;

  try {
    const [treeRes, menuIdsRes] = await Promise.all([getMenuTree(), getRoleMenus(row.roleId)]);
    menuTree.value = formatTreeData(treeRes.data);
    checkedKeys.value = menuIdsRes.data;

    await nextTick();
    treeRef.value?.setCheckedKeys(checkedKeys.value);
  } catch (error) {
    console.error(error);
  } finally {
    loading.value = false;
  }
};

const handleSubmit = async () => {
  if (!treeRef.value) return;

  const selectedKeys = treeRef.value.getCheckedKeys() as number[];
  const halfSelectedKeys = treeRef.value.getHalfCheckedKeys() as number[];
  const allKeys = [...selectedKeys, ...halfSelectedKeys];

  if (allKeys.length === 0) {
    return ElMessage.warning("请至少选择一个权限");
  }

  submitLoading.value = true;
  try {
    await assignMenus({
      roleId: roleInfo.value.roleId!,
      menuIds: allKeys
    });
    ElMessage.success("权限分配成功！");
    drawerVisible.value = false;
  } catch (error) {
    console.error(error);
  } finally {
    submitLoading.value = false;
  }
};

defineExpose({ acceptParams });
</script>

<style scoped>
.permission-tree-wrapper {
  /* 移除了固定高度和溢出隐藏，允许随内容撑开 */
  padding: 0 10px;
}
.custom-tree-node {
  display: flex;
  align-items: center;
  font-size: 14px;
  padding: 2px 0;
}
/* 去掉 tree 的默认边框，使其在抽屉里更自然 */
:deep(.el-tree) {
  background: transparent;
}
</style>
