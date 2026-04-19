<template>
  <div class="user-info-container main-box">
    <div class="layout-wrapper">
      <el-row :gutter="24" class="equal-height-row">
        <el-col :xs="24" :sm="24" :md="8" :lg="7" :xl="7" class="stretch-col">
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

        <el-col :xs="24" :sm="24" :md="16" :lg="17" :xl="17" class="stretch-col">
          <el-card shadow="hover" class="info-card right-card">
            <el-tabs v-model="activeTab" class="profile-tabs">
              <el-tab-pane label="基本资料" name="info">
                <div class="tab-content-wrapper">
                  <el-descriptions :column="responsiveColumns" border class="info-descriptions">
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
                </div>

                <div class="tab-footer">
                  <el-button type="primary" :icon="Edit" round>编辑资料</el-button>
                </div>
              </el-tab-pane>

              <el-tab-pane label="安全设置" name="security">
                <div class="tab-content-wrapper security-list">
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
  </div>
</template>

<script setup lang="ts" name="personalInfo">
import { ref, onMounted, computed } from "vue";
// 替换为你自己项目实际的引入路径
import { getUserInfoApi } from "@/api/modules/login";
import { getDictDataByType } from "@/api/modules/dict";
import { Dict, Login } from "@/api/interface/index";
import { Calendar, Postcard, Edit, Lock, Iphone } from "@element-plus/icons-vue";

const defaultAvatar = "https://api.dicebear.com/7.x/avataaars/svg?seed=Felix";
const activeTab = ref("info");

const userInfo = ref<Partial<Login.ResUserInfo>>({});
const genderDict = ref<Dict.ResDictData[]>([]);

// 响应式列数：屏幕小于768px时显示1列，否则2列
const responsiveColumns = computed(() => {
  if (typeof window !== "undefined" && window.innerWidth < 768) return 1;
  return 2;
});

const getDictLabel = (dict: Dict.ResDictData[], value: any) => {
  if (!value && value !== 0) return "--";
  const target = dict.find(item => String(item.dictValue) === String(value));
  return target ? target.dictLabel : value;
};

onMounted(async () => {
  // 模拟接口调用，保留你原来的逻辑
  const [userRes, genderRes] = await Promise.all([getUserInfoApi(), getDictDataByType("user_gender")]);
  userInfo.value = userRes.data;
  genderDict.value = genderRes.data;
});
</script>

<style scoped lang="scss">
.user-info-container {
  padding: 24px;
  background: var(--el-bg-color-page);
  min-height: 100vh;
  display: flex;
  justify-content: center; /* 整体居中 */

  // 响应式内边距调整
  @media (max-width: 768px) {
    padding: 12px;
  }

  /* 限制最大宽度，解决大屏横向大量空白 */
  .layout-wrapper {
    width: 100%;
    max-width: 1200px;
  }

  /* 核心：让左右两列等高 */
  .equal-height-row {
    display: flex;
    align-items: stretch;
    flex-wrap: wrap;
  }

  .stretch-col {
    display: flex;
    flex-direction: column;
    margin-bottom: 20px; /* 移动端折叠时的间距 */
  }

  .info-card {
    flex: 1; /* 撑满 column 的高度 */
    border-radius: 12px; /* 稍微收敛一点圆角，更显商务精致 */
    transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
    background: var(--el-bg-color);
    border: none;

    &:hover {
      box-shadow: var(--el-box-shadow-light);
    }

    :deep(.el-card__body) {
      padding: 24px;
      height: 100%;
      box-sizing: border-box;
      display: flex;
      flex-direction: column;
    }
  }

  .custom-divider {
    margin: 16px 0;
  }

  // 左侧用户卡片样式
  .user-card {
    .user-avatar-wrapper {
      display: flex;
      flex-direction: column;
      align-items: center;
      padding: 0 0 16px;

      .user-avatar {
        transition: transform 0.3s ease;
        border: 4px solid var(--el-color-primary-light-9);
        &:hover {
          transform: rotate(5deg) scale(1.05);
        }
      }

      .user-name {
        margin: 16px 0 8px;
        font-size: 20px;
        font-weight: 600;
        color: var(--el-text-color-primary);
      }

      .user-signature {
        font-size: 13px;
        color: var(--el-text-color-secondary);
        text-align: center;
        line-height: 1.5;
        max-width: 90%;
      }
    }

    .user-roles {
      display: flex;
      flex-wrap: wrap;
      justify-content: center;
      gap: 8px;
      margin-bottom: 8px;

      .role-tag {
        border-radius: 4px;
      }
    }

    .user-detail-list {
      .detail-item {
        display: flex;
        align-items: center;
        margin-bottom: 12px;
        font-size: 14px;
        padding: 8px 12px;
        border-radius: 8px;
        background: var(--el-fill-color-light);

        .el-icon {
          margin-right: 12px;
          font-size: 16px;
          color: var(--el-color-primary);
        }

        .label {
          color: var(--el-text-color-regular);
          width: 75px;
        }

        .value {
          color: var(--el-text-color-primary);
          flex: 1;
          font-weight: 500;
        }
      }
    }
  }

  // 右侧详情卡片样式
  .right-card {
    :deep(.el-card__body) {
      padding: 20px 32px;
    }
  }

  .profile-tabs {
    flex: 1;
    display: flex;
    flex-direction: column;

    :deep(.el-tabs__header) {
      margin-bottom: 24px;
    }

    :deep(.el-tabs__nav-wrap::after) {
      height: 1px;
      background-color: var(--el-border-color-lighter);
    }

    :deep(.el-tabs__item) {
      font-size: 15px;
      padding: 0 24px;
      height: 48px;
      line-height: 48px;
    }

    /* 核心：固定内容区域最小高度，防止切换Tab时外层卡片高度跳动 */
    .tab-content-wrapper {
      min-height: 320px;
    }

    .info-descriptions {
      :deep(.el-descriptions__label) {
        font-weight: 500;
        color: var(--el-text-color-regular);
        width: 100px;
        background-color: var(--el-fill-color-light);
      }
      :deep(.el-descriptions__content) {
        color: var(--el-text-color-primary);
      }
    }

    .tab-footer {
      display: flex;
      justify-content: flex-end;
      margin-top: 24px;
    }
  }

  // 安全设置列表样式
  .security-list {
    .security-item {
      display: flex;
      justify-content: space-between;
      align-items: center;
      padding: 12px 8px;

      .s-info {
        display: flex;
        flex-direction: column;
        gap: 8px;

        .s-title {
          font-weight: 500;
          font-size: 15px;
          color: var(--el-text-color-primary);
        }

        .s-desc {
          font-size: 13px;
          color: var(--el-text-color-secondary);
        }
      }
    }
  }
}

// 全局响应式断点优化
@media (max-width: 992px) {
  .user-info-container {
    .equal-height-row {
      display: block; /* 窄屏下取消等高，允许自然换行堆叠 */
    }
    .right-card :deep(.el-card__body) {
      padding: 20px;
    }
  }
}
</style>
