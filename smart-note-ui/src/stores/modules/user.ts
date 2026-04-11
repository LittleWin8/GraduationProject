import { defineStore } from "pinia";
import { UserState } from "@/stores/interface";
import piniaPersistConfig from "@/stores/helper/persist";
import { useAuthStore } from "./auth";
import { getUserInfoApi } from "@/api/modules/login";

export const useUserStore = defineStore({
  id: "Link-Mind",
  state: (): UserState => ({
    token: "",
    userInfo: {
      userId: 0,
      name: "",
      account: "",
      avatar: "",
      roles: []
    }
  }),
  getters: {},
  actions: {
    // Set Token
    setToken(token: string) {
      this.token = token;
    },
    // Set setUserInfo
    setUserInfo(userInfo: UserState["userInfo"]) {
      this.userInfo = userInfo;
    },

    // 异步获取用户信息动作
    async getUserInfoAction() {
      try {
        const { data } = await getUserInfoApi(); // 调用后端 /api/admin/auth/getUserInfo
        this.setUserInfo(data);
        return data;
      } catch (error) {
        return Promise.reject(error);
      }
    },

    // 退出登录动作 - 必须放在 actions 内部
    async logoutAction() {
      // 1. 清空当前 store 状态（会恢复到 state 定义的初始值）
      this.$reset();

      // 2. 如果有权限相关的 store，建议也在这里清除
      const authStore = useAuthStore();
      authStore.$reset();
    }
  },
  persist: piniaPersistConfig("Link-Mind")
});
