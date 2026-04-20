<template>
  <el-dropdown trigger="click">
    <div class="avatar">
      <img :src="avatarSrc" alt="avatar" />
    </div>
    <template #dropdown>
      <el-dropdown-menu>
        <el-dropdown-item @click="router.push('/profile/index')">
          <el-icon><User /></el-icon>{{ $t("header.personalData") }}
        </el-dropdown-item>
        <el-dropdown-item divided @click="logout">
          <el-icon><SwitchButton /></el-icon>{{ $t("header.logout") }}
        </el-dropdown-item>
      </el-dropdown-menu>
    </template>
  </el-dropdown>
</template>

<script setup lang="ts">
import { computed } from "vue";
import { useRouter } from "vue-router";
import { ElMessageBox, ElMessage } from "element-plus";
import { LOGIN_URL } from "@/config";
import { logoutApi } from "@/api/modules/login";
import { useUserStore } from "@/stores/modules/user";
import defaultAvatar from "@/assets/images/avatar.gif";

const router = useRouter();
const userStore = useUserStore();

// 头像计算属性：优先从 store 获取，没有则使用本地 gif
const avatarSrc = computed(() => {
  return userStore.userInfo.avatar || defaultAvatar;
});

/**
 * @description 退出登录
 */
const logout = () => {
  ElMessageBox.confirm("您是否确认退出登录?", "温馨提示", {
    confirmButtonText: "确定",
    cancelButtonText: "取消",
    type: "warning"
  }).then(async () => {
    try {
      // 1. 调用后端退出接口
      await logoutApi();
    } catch (error) {
      console.warn("后端退出接口调用失败，正在强制清理本地缓存...");
    } finally {
      // 2. 清除 Pinia 存储的所有状态 (token, userInfo, auth 等)
      await userStore.logoutAction();

      // 3. 重定向到登录页
      router.replace(LOGIN_URL);
      ElMessage.success("退出登录成功！");
    }
  });
};
</script>

<style scoped lang="scss">
.avatar {
  width: 40px;
  height: 40px;
  overflow: hidden;
  cursor: pointer;
  border-radius: 50%;
  img {
    width: 100%;
    height: 100%;
    object-fit: cover; // 确保头像比例正常
  }
}
</style>
