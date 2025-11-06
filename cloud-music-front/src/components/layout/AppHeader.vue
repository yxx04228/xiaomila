<template>
  <header class="app-header">
    <div class="header-content">
      <div class="logo">
        <router-link to="/">🎵 云音乐</router-link>
      </div>
      <nav class="nav">
        <router-link to="/" class="nav-link">发现音乐</router-link>
        <router-link to="/" class="nav-link">我的歌单</router-link>
      </nav>
      <div class="user-area">
        <template v-if="userStore.isLoggedIn">
          <span class="username">欢迎，{{ userStore.userInfo?.nickname }}</span>
          <button @click="handleLogout" class="logout-btn">退出</button>
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
import { useUserStore } from '@/stores/user' // 修复为 stores

const userStore = useUserStore()

const handleLogout = () => {
  userStore.logout()
}
</script>

<style scoped>
.app-header {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  padding: 0 20px;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.1);
}

.header-content {
  max-width: 1200px;
  margin: 0 auto;
  display: flex;
  align-items: center;
  justify-content: space-between;
  height: 60px;
}

.logo a {
  font-size: 24px;
  font-weight: bold;
  color: white;
  text-decoration: none;
}

.nav {
  display: flex;
  gap: 30px;
}

.nav-link {
  color: white;
  text-decoration: none;
  padding: 8px 16px;
  border-radius: 20px;
  transition: background-color 0.3s;
}

.nav-link:hover,
.nav-link.router-link-active {
  background-color: rgba(255, 255, 255, 0.2);
}

.user-area {
  display: flex;
  align-items: center;
  gap: 15px;
}

.username {
  font-size: 14px;
}

.login-btn,
.register-btn,
.logout-btn {
  padding: 8px 16px;
  border: none;
  border-radius: 20px;
  cursor: pointer;
  text-decoration: none;
  font-size: 14px;
  transition: all 0.3s;
}

.login-btn {
  background: transparent;
  color: white;
  border: 1px solid white;
}

.register-btn {
  background: white;
  color: #667eea;
}

.logout-btn {
  background: rgba(255, 255, 255, 0.1);
  color: white;
}

.logout-btn:hover {
  background: rgba(255, 255, 255, 0.2);
}
</style>
