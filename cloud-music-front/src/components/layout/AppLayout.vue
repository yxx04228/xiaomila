<template>
  <div class="app-layout">
    <AppHeader />
    <main class="main-content">
      <router-view />
    </main>
    <MusicPlayer v-if="currentMusic" />
  </div>
</template>

<script setup lang="ts">
import AppHeader from './AppHeader.vue'
import MusicPlayer from '../music/MusicPlayer.vue'
import { useMusicStore } from '@/stores/music'
import { storeToRefs } from 'pinia' 

const musicStore = useMusicStore()
const { currentMusic } = storeToRefs(musicStore)
</script>

<style scoped>
.app-layout {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
}

.main-content {
  flex: 1;
  padding: 20px;
  max-width: 1200px;
  margin: 0 auto;
  width: 100%;
}
</style>
