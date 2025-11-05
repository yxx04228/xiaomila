<template>
  <div class="app-layout">
    <AppHeader />
    <main class="main-content">
      <router-view />
    </main>
    <MusicPlayer />
  </div>
</template>

<script setup lang="ts">
import AppHeader from './AppHeader.vue'
import MusicPlayer from '../music/MusicPlayer.vue'
</script>

<style scoped>
.app-layout {
  display: flex;
  flex-direction: column;
  height: 100vh; /* 关键：直接使用视口高度 */
  background-color: #f8f9fa;
  position: relative; /* 为播放器定位提供上下文 */
}

.main-content {
  flex: 1;
  width: 100%;
  max-width: 1400px;
  margin: 0 auto;
  padding: 20px 24px;
  box-sizing: border-box;
  overflow-y: auto; /* 只允许主内容区滚动 */
  min-height: 0; /* 关键：允许收缩 */

  /* 滚动条美化 */
  &::-webkit-scrollbar {
    width: 6px;
    height: 6px;
  }
  &::-webkit-scrollbar-thumb {
    background-color: #cbd5e0;
    border-radius: 3px;
  }
  &::-webkit-scrollbar-track {
    background-color: #f8f9fa;
  }
}

/* 播放器样式调整 - 关键修复 */
:deep(.music-player) {
  position: fixed; /* 改为 fixed 定位 */
  bottom: 0;
  left: 0;
  right: 0;
  z-index: 1000; /* 提高层级 */
  background: white;
  box-shadow: 0 -2px 20px rgba(0, 0, 0, 0.1);
  border-radius: 16px 16px 0 0;
  padding: 12px 20px;
  max-height: 90px;
  flex-shrink: 0;
}

/* 为内容区域添加底部内边距，避免被播放器遮挡 */
:deep(.main-content) {
  padding-bottom: 100px; /* 为播放器留出空间 */
}

/* 响应式调整 */
@media (max-width: 768px) {
  .main-content {
    padding: 16px;
    padding-bottom: 80px; /* 移动端调整 */
  }

  :deep(.music-player) {
    padding: 8px 16px;
    max-height: 80px;
    border-radius: 12px 12px 0 0;
  }
}

@media (max-width: 480px) {
  .main-content {
    padding: 12px 8px;
    padding-bottom: 70px;
  }

  :deep(.music-player) {
    padding: 6px 12px;
    max-height: 70px;
    border-radius: 8px 8px 0 0;
  }
}
</style>
