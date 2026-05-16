<template>
  <div class="login-container flx-center">
    <div class="login-box">
      <SwitchDark class="dark" />
      <div class="login-left">
        <img class="login-left-img" :src="isDark ? loginKnowledgeNetworkDark : loginKnowledgeNetwork" alt="login" />
      </div>
      <div class="login-form">
        <div class="login-logo">
          <img class="login-icon" src="@/assets/images/logoC.png" alt="" />
          <h2 class="logo-text">Link-Mind</h2>
        </div>
        <LoginForm />
      </div>
    </div>
  </div>
</template>

<script setup lang="ts" name="login">
import { storeToRefs } from "pinia";
import { useGlobalStore } from "@/stores/modules/global";
import LoginForm from "./components/LoginForm.vue";
import SwitchDark from "@/components/SwitchDark/index.vue";
import loginKnowledgeNetwork from "@/assets/images/login_knowledge_network.svg";
import loginKnowledgeNetworkDark from "@/assets/images/login_knowledge_network_dark.svg";

const globalStore = useGlobalStore();
const { isDark } = storeToRefs(globalStore);
</script>

<style scoped lang="scss">
@import "./index.scss";
</style>
