<template>
  <AppLayout />
  <NetworkStatus />
</template>

<script setup lang="ts">
import { onMounted } from 'vue'
import AppLayout from '@/components/layout/AppLayout.vue'
import NetworkStatus from '@/components/common/NetworkStatus.vue'
import { useMusicStore } from '@/stores/music'

const musicStore = useMusicStore()

onMounted(() => {
  // 可以在这里添加播放器状态监控
  console.log('应用初始化完成，播放器状态:', musicStore.audioElementReady)
})
</script>

<style>
/* 全局样式重置 */
html,
body,
#app {
  margin: 0;
  padding: 0;
  height: 100vh;
  min-height: 100vh;
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, 'Helvetica Neue', Arial,
    sans-serif;
  -webkit-font-smoothing: antialiased;
  -moz-osx-font-smoothing: grayscale;
  overflow: hidden; /* 禁止全局滚动 */
}

/* 网络状态提示 */
.network-status {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  z-index: 101;
  margin: 0 !important;
  border-radius: 0 !important;
}

/* 修复可能的全局样式冲突 */
* {
  box-sizing: border-box;
}
</style>
