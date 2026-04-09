import { defineStore } from "pinia";
import { UserState } from "@/stores/interface";
import piniaPersistConfig from "@/stores/helper/persist";

export const useUserStore = defineStore({
  id: "geeker-user",
  state: (): UserState => ({
    token: "",
    userInfo: { name: "xiaowen" }
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
    // 退出登录动作 - 必须放在 actions 内部
    async logoutAction() {
      // 1. 清空当前 store 状态（会恢复到 state 定义的初始值）
      this.$reset();

      // 2. 如果有权限相关的 store，建议也在这里清除
      // const authStore = useAuthStore();
      // authStore.$reset();
    }
  },
  persist: piniaPersistConfig("geeker-user")
});
