<template>
  <header class="app-header">
    <div class="header-content">
      <div class="logo">
        <router-link to="/">🎵 云音乐</router-link>
      </div>
      <nav class="nav">
        <router-link to="/" class="nav-link">发现音乐</router-link>
      </nav>
      <div class="user-area">
        <template v-if="userStore.isLoggedIn">
          <el-dropdown>
            <span class="user-dropdown-trigger">
              <el-avatar :size="32" class="user-avatar">
                {{ userStore.userInfo?.nickname?.charAt(0) }}
              </el-avatar>
              <span class="username">{{ userStore.userInfo?.nickname }}</span>
              <el-icon><arrow-down /></el-icon>
            </span>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item @click="goToProfile">
                  <el-icon><User /></el-icon>
                  个人中心
                </el-dropdown-item>
                <el-dropdown-item @click="goToMyMusic">
                  <el-icon><VideoPlay /></el-icon>
                  我的音乐
                </el-dropdown-item>
                <el-dropdown-item divided @click="handleLogout">
                  <el-icon><SwitchButton /></el-icon>
                  退出登录
                </el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </template>
        <template v-else>
          <router-link to="/login" class="login-btn">登录</router-link>
          <router-link to="/register" class="register-btn">注册</router-link>
        </template>
      </div>
    </div>
  </header>
</template>

<script setup lang="ts">
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { User, VideoPlay, SwitchButton, ArrowDown } from '@element-plus/icons-vue'
import { useUserStore } from '@/stores/user'

const router = useRouter()
const userStore = useUserStore()

// 跳转到个人中心
const goToProfile = () => {
  router.push('/profile')
}

// 跳转到我的音乐
const goToMyMusic = () => {
  router.push('/my-music')
}

// 处理退出登录
const handleLogout = async () => {
  try {
    await ElMessageBox.confirm('确定要退出登录吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning',
    })

    userStore.userLogout()
    ElMessage.success('退出成功')
    // 退出后跳转到登录页
    router.push('/login')
  } catch (error) {
    // 用户取消退出
    console.log('取消退出登录')
  }
}
</script>

<style scoped>
/* 基础头部样式 - 简化渐变、降低阴影强度 */
.app-header {
  background: linear-gradient(135deg, #6c7ae0 0%, #7b5ea7 100%);
  box-shadow: 0 1px 6px rgba(0, 0, 0, 0.08);
  position: sticky;
  top: 0;
  z-index: 1000;
}

/* 内容容器 - 保持简洁布局 */
.header-content {
  margin: 0 auto;
  padding: 0 38px;
  height: 60px;
  display: flex;
  align-items: center;
  justify-content: space-between;
}

/* Logo样式 - 简化字体效果 */
.logo a {
  font-size: 22px;
  font-weight: 600;
  color: #fff;
  text-decoration: none;
  transition: color 0.2s ease;
}

.logo a:hover {
  color: #f0e6ff;
}

/* 导航链接 - 简化交互效果 */
.nav-link {
  color: #fff;
  text-decoration: none;
  padding: 6px 14px;
  border-radius: 16px;
  transition: background-color 0.2s ease;
  font-weight: 400;
}

.nav-link:hover {
  background: rgba(255, 255, 255, 0.08);
  color: #fff;
}

.nav-link.router-link-active {
  background: rgba(255, 255, 255, 0.15);
  color: #fff;
}

/* 用户区域 - 精简间距和样式 */
.user-area {
  display: flex;
  align-items: center;
  gap: 8px; /* 缩小按钮间距，更紧凑 */
}

/* 登录/注册按钮 - 核心优化：贴合渐变背景，弱化突兀感 */
.login-btn,
.register-btn {
  padding: 5px 16px;
  border-radius: 18px; /* 适度圆角，更柔和 */
  text-decoration: none;
  font-weight: 400;
  transition: all 0.2s ease;
  font-size: 14px;
  border: none; /* 移除边框，减少割裂感 */
}

/* 登录按钮：半透明白色背景，与头部渐变融合 */
.login-btn {
  color: #fff;
  background: rgba(255, 255, 255, 0.12); /* 半透明白色，不突兀 */
}

.login-btn:hover {
  background: rgba(255, 255, 255, 0.2); /* hover时加深透明度，保持融合 */
  color: #fff;
}

/* 注册按钮：低饱和度紫色，与头部渐变同色系 */
.register-btn {
  background: rgba(255, 255, 255, 0.9); /* 接近白色但带点通透感 */
  color: #6c7ae0; /* 与头部渐变主色一致 */
}

.register-btn:hover {
  background: #fff; /* hover时略微提亮，保持简洁 */
  color: #5d6bc0; /* 主色加深，提升反馈 */
}

/* 用户下拉触发器 - 简化交互效果 */
.user-dropdown-trigger {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 3px 10px;
  border-radius: 16px;
  cursor: pointer;
  transition: background-color 0.2s ease;
  color: #fff;
  font-size: 14px;
}

.user-dropdown-trigger:hover {
  background: rgba(255, 255, 255, 0.08);
}

/* 用户头像 - 简化渐变 */
.user-avatar {
  background: linear-gradient(45deg, #8a94f0, #a78bfa);
  font-weight: 500;
  color: #fff;
}

/* 用户名 - 保持简洁 */
.username {
  font-weight: 400;
  max-width: 100px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

/* 下拉菜单 - 简约风格优化 */
:deep(.el-dropdown-menu) {
  border: 1px solid #f0f0f0;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
  padding: 6px 0;
}

:deep(.el-dropdown-menu__item) {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 8px 14px;
  color: #333;
  transition: background-color 0.2s ease;
  font-size: 14px;
}

:deep(.el-dropdown-menu__item:hover) {
  background: #f5f4ff;
  color: #6c7ae0;
}

:deep(.el-dropdown-menu__item--divided) {
  border-top: 1px solid #f0f0f0;
}

/* 响应式设计 - 保持简洁适配 */
@media (max-width: 768px) {
  .header-content {
    padding: 0 12px;
  }

  .logo a {
    font-size: 19px;
  }

  .username {
    display: none;
  }

  .user-area .login-btn,
  .user-area .register-btn {
    padding: 4px 14px;
    font-size: 13px;
  }
}
</style>
