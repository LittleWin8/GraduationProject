<template>
  <div class="user-info-container main-box">
    <div class="layout-wrapper">
      <el-row :gutter="24" class="equal-height-row">
        <el-col :xs="24" :sm="24" :md="8" :lg="7" :xl="7">
          <el-card shadow="hover" class="user-card info-card">
            <div class="user-avatar-wrapper">
              <el-avatar :size="100" :src="userInfo.avatar || defaultAvatar" class="user-avatar" />
              <h3 class="user-name">{{ userInfo.name }}</h3>
              <p class="user-signature">{{ userInfo.signature || "这家伙很懒，什么都没留下" }}</p>
            </div>

            <div class="user-roles">
              <el-tag v-for="role in userInfo.roles" :key="role" effect="plain" class="role-tag" size="small">
                {{ role }}
              </el-tag>
            </div>

            <el-divider border-style="dashed" class="custom-divider" />

            <div class="user-detail-list">
              <div class="detail-item">
                <el-icon><Postcard /></el-icon>
                <span class="label">账户名：</span>
                <span class="value">{{ userInfo.account }}</span>
              </div>
              <div class="detail-item">
                <el-icon><Calendar /></el-icon>
                <span class="label">注册时间：</span>
                <span class="value">{{ userInfo.createTime?.split(" ")[0] }}</span>
              </div>
            </div>
          </el-card>
        </el-col>

        <el-col :xs="24" :sm="24" :md="16" :lg="17" :xl="17">
          <el-card shadow="hover" class="info-card right-card">
            <el-tabs v-model="activeTab" class="full-height-tabs">
              <el-tab-pane label="基本资料" name="info">
                <div class="tab-content">
                  <el-descriptions :column="responsiveColumns" border class="info-descriptions">
                    <el-descriptions-item label="用户ID">{{ userInfo.userId }}</el-descriptions-item>
                    <el-descriptions-item label="登录账号">{{ userInfo.account }}</el-descriptions-item>
                    <el-descriptions-item label="用户昵称">{{ userInfo.name }}</el-descriptions-item>
                    <el-descriptions-item label="性别">
                      <el-tag :type="userInfo.gender == 1 ? 'success' : 'danger'" size="small" effect="light">
                        {{ getDictLabel(genderDict, userInfo.gender) }}
                      </el-tag>
                    </el-descriptions-item>
                    <el-descriptions-item label="联系电话">{{ userInfo.phone || "--" }}</el-descriptions-item>
                    <el-descriptions-item label="电子邮箱">{{ userInfo.email || "--" }}</el-descriptions-item>
                    <el-descriptions-item label="所在城市">{{ userInfo.city || "--" }}</el-descriptions-item>
                    <el-descriptions-item label="出生日期">{{ userInfo.birthday || "--" }}</el-descriptions-item>
                    <el-descriptions-item label="个性签名" :span="responsiveColumns === 1 ? 1 : 2">
                      {{ userInfo.signature || "--" }}
                    </el-descriptions-item>
                  </el-descriptions>

                  <div class="tab-footer">
                    <el-button type="primary" :icon="Edit" round @click="openUserDrawer">编辑资料</el-button>
                  </div>
                </div>
              </el-tab-pane>

              <el-tab-pane label="安全设置" name="security">
                <div class="tab-content security-list">
                  <div class="security-item">
                    <div class="s-info">
                      <span class="s-title">账户密码</span>
                      <span class="s-desc">定期更换密码有助于账户安全</span>
                    </div>
                    <el-button type="primary" link :icon="Lock">修改</el-button>
                  </div>
                  <el-divider class="custom-divider" />
                  <div class="security-item">
                    <div class="s-info">
                      <span class="s-title">绑定手机</span>
                      <span class="s-desc">已绑定手机：{{ userInfo.phone || "未绑定" }}</span>
                    </div>
                    <el-button type="primary" link :icon="Iphone">更换</el-button>
                  </div>
                </div>
              </el-tab-pane>
            </el-tabs>
          </el-card>
        </el-col>
      </el-row>
    </div>
    <UserDrawer ref="userDrawerRef" />
  </div>
</template>

<script setup lang="ts" name="personalInfo">
import { ref, onMounted, onUnmounted, computed } from "vue";
import { useUserStore } from "@/stores/modules/user";
import { getDictDataByType } from "@/api/modules/dict";
import { Dict } from "@/api/interface/index";
import { Calendar, Postcard, Edit, Lock, Iphone } from "@element-plus/icons-vue";
import defaultAvatarImg from "@/assets/images/avatar.gif";
import UserDrawer from "./components/personalDrawer.vue";
import { editUser } from "@/api/modules/user";

const userStore = useUserStore();
const defaultAvatar = defaultAvatarImg;
const activeTab = ref("info");

const userInfo = computed(() => userStore.userInfo);
const genderDict = ref<Dict.ResDictData[]>([]);

const responsiveColumns = ref(2);

const userDrawerRef = ref<InstanceType<typeof UserDrawer> | null>(null);

// 5. 打开抽屉的方法
const openUserDrawer = () => {
  // 如果没有在 template 里写 <UserDrawer />，这里会报错或没反应
  const info = userStore.userInfo;

  const rowData = {
    ...info,
    userId: info.userId,
    identifier: info.account,
    nickname: info.name,
    gender: String(info.gender)
  };

  userDrawerRef.value?.acceptParams({
    row: rowData,
    api: editUser
  });
};

const handleResize = () => {
  responsiveColumns.value = window.innerWidth < 768 ? 1 : 2;
};

onMounted(async () => {
  handleResize();
  window.addEventListener("resize", handleResize);

  if (!userInfo.value.userId) {
    await userStore.getUserInfoAction();
  }

  const { data } = await getDictDataByType("user_gender");
  genderDict.value = data;
});

onUnmounted(() => {
  window.removeEventListener("resize", handleResize);
});

const getDictLabel = (dict: Dict.ResDictData[], value: any) => {
  if (!value && value !== 0) return "--";
  const target = dict.find(item => String(item.dictValue) === String(value));
  return target ? target.dictLabel : value;
};
</script>

<style scoped lang="scss" src="./personalInfo.scss"></style>
