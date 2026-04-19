<template>
  <div class="user-info-container main-box">
    <el-row :gutter="20">
      <el-col :xs="24" :sm="24" :md="8" :lg="6">
        <el-card shadow="hover" class="user-card">
          <div class="user-avatar">
            <el-avatar :size="100" :src="userInfo.avatar || defaultAvatar" />
            <h3 class="user-name">{{ userInfo.name }}</h3>
            <p class="user-signature">{{ userInfo.signature || "暂无个性签名" }}</p>
          </div>
          <div class="user-tags">
            <el-tag v-for="roleName in userInfo.roles" :key="roleName" size="small" effect="light" class="role-tag">
              {{ roleName }}
            </el-tag>
          </div>
          <el-divider border-style="dashed" />
          <div class="user-meta">
            <div class="meta-item">
              <el-icon><Calendar /></el-icon>
              <span>注册时间：{{ userInfo.createTime?.split(" ")[0] }}</span>
            </div>
          </div>
        </el-card>
      </el-col>

      <el-col :xs="24" :sm="24" :md="16" :lg="18">
        <el-card shadow="hover">
          <template #header>
            <div class="card-header">
              <span>个人详细资料</span>
              <el-button type="primary" link icon="Edit">编辑资料</el-button>
            </div>
          </template>

          <el-descriptions :column="2" border>
            <el-descriptions-item label="登录账号">{{ userInfo.account }}</el-descriptions-item>
            <el-descriptions-item label="用户昵称">{{ userInfo.name }}</el-descriptions-item>

            <el-descriptions-item label="性别">
              <el-tag :type="userInfo.gender == 1 ? undefined : userInfo.gender == 2 ? 'danger' : 'info'">
                {{ getDictLabel(genderDict, userInfo.gender) }}
              </el-tag>
            </el-descriptions-item>

            <el-descriptions-item label="联系电话">{{ userInfo.phone || "--" }}</el-descriptions-item>
            <el-descriptions-item label="电子邮箱">{{ userInfo.email || "--" }}</el-descriptions-item>
            <el-descriptions-item label="所在城市">{{ userInfo.city || "--" }}</el-descriptions-item>
            <el-descriptions-item label="出生日期">{{ userInfo.birthday || "--" }}</el-descriptions-item>

            <el-descriptions-item label="个性签名" :span="2">
              {{ userInfo.signature || "--" }}
            </el-descriptions-item>
          </el-descriptions>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup lang="ts" name="personalInfo">
import { ref, onMounted } from "vue";
import { getUserInfoApi } from "@/api/modules/login";
import { getDictDataByType } from "@/api/modules/dict";
import { Dict, Login } from "@/api/interface/index";
import { Calendar } from "@element-plus/icons-vue";

const defaultAvatar = "https://api.dicebear.com/7.x/avataaars/svg?seed=Felix";

// 使用 Partial 避免初始化空对象时的 TS 报错
const userInfo = ref<Partial<Login.ResUserInfo>>({});
// 显式声明泛型，解决 never[] 报错
const genderDict = ref<Dict.ResDictData[]>([]);

/**
 * 字典翻译逻辑（针对性别）
 */
const getDictLabel = (dict: Dict.ResDictData[], value: any) => {
  if (value === undefined || value === null || value === "") return "--";
  const target = dict.find(item => String(item.dictValue) === String(value));
  return target ? target.dictLabel : value;
};

const initData = async () => {
  try {
    // 仅获取用户信息和性别字典（角色直接使用后端字符串）
    const [userRes, genderRes] = await Promise.all([getUserInfoApi(), getDictDataByType("user_gender")]);

    userInfo.value = userRes.data;
    genderDict.value = genderRes.data;
  } catch (error) {
    console.error("加载个人资料失败", error);
  }
};

onMounted(() => {
  initData();
});
</script>

<style scoped lang="scss">
.user-info-container {
  .user-card {
    .user-avatar {
      display: flex;
      flex-direction: column;
      align-items: center;
      padding: 10px 0;
      .user-name {
        margin: 15px 0 5px;
        font-size: 20px;
        font-weight: bold;
      }
      .user-signature {
        font-size: 13px;
        color: var(--el-text-color-secondary);
        margin-bottom: 15px;
      }
    }
    .user-tags {
      display: flex;
      flex-wrap: wrap;
      justify-content: center;
      gap: 6px;
      margin-bottom: 10px;
    }
    .user-meta {
      display: flex;
      justify-content: center;
      font-size: 13px;
      color: var(--el-text-color-regular);
      .meta-item {
        display: flex;
        align-items: center;
        gap: 5px;
      }
    }
  }
  .card-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
  }
}
</style>
