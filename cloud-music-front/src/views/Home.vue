<template>
  <div class="home-page">
    <div class="page-header">
      <h1>发现音乐</h1>
      <div class="search-area">
        <el-input
          v-model="searchTitle"
          placeholder="搜索歌曲..."
          clearable
          @clear="handleSearch"
          @keyup.enter="handleSearch"
          @input="handleInput"
        >
          <template #append>
            <el-button @click="handleSearch">
              <el-icon><Search /></el-icon>
            </el-button>
          </template>
        </el-input>
      </div>
    </div>

    <!-- 自动播放提示 -->
    <div
      v-if="musicStore.autoPlayEnabled && musicStore.musicList.length > 0"
      class="auto-play-tips"
    >
      <el-alert
        title="自动播放提示"
        description="已自动加载第一首歌曲，点击播放按钮开始聆听"
        type="info"
        show-icon
        :closable="true"
        @close="musicStore.disableAutoPlay()"
      />
    </div>

    <MusicList
      :music-list="musicStore.musicList"
      :loading="musicStore.loading"
      :pagination="musicStore.pagination"
      @play="handlePlay"
      @download="handleDownload"
      @page-change="handlePageChange"
    />
  </div>
</template>

<script setup lang="ts">
import { onMounted, onUnmounted, ref } from 'vue'
import { Search } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import MusicList from '@/components/music/MusicList.vue'
import { useMusicStore } from '@/stores/music'
import type { Music } from '@/types/music'
import { downloadMusicFile } from '@/utils/download'

const musicStore = useMusicStore()
const searchTitle = ref('')
const searchTimer = ref<number>()

const loadMusicList = () => {
  musicStore.fetchMusicList({
    title: searchTitle.value,
    nCurrent: musicStore.pagination.current,
    nSize: musicStore.pagination.pageSize,
  })
}

const handleSearch = () => {
  // 清除之前的定时器
  if (searchTimer.value) {
    clearTimeout(searchTimer.value)
  }
  musicStore.pagination.current = 1
  loadMusicList()
}

// 输入时防抖搜索
const handleInput = () => {
  // 清除之前的定时器
  if (searchTimer.value) {
    clearTimeout(searchTimer.value)
  }
  // 设置新的定时器，500ms后自动搜索
  searchTimer.value = setTimeout(() => {
    musicStore.pagination.current = 1
    loadMusicList()
  }, 500)
}

const handlePlay = async (music: Music) => {
  try {
    await musicStore.playMusic(music)
  } catch (error: any) {
    ElMessage.error(`播放失败: ${error.message || '请稍后重试'}`)
  }
}

const handleDownload = async (music: Music) => {
  await downloadMusicFile(music)
}

const handlePageChange = (page: number) => {
  musicStore.pagination.current = page
  loadMusicList()
}

onMounted(() => {
  // 启用自动播放
  musicStore.enableAutoPlay()
  // 加载音乐列表（会自动触发播放第一首）
  loadMusicList()
})

onUnmounted(() => {
  // 清理搜索定时器
  if (searchTimer.value) {
    clearTimeout(searchTimer.value)
  }
  // 禁用自动播放（避免在其他页面也自动播放）
  musicStore.disableAutoPlay()
  // 清理blob URL
  musicStore.cleanupBlobUrl()
})
</script>

<style scoped>
.home-page {
  padding: 20px 0;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 30px;
}

.page-header h1 {
  color: #333;
  margin: 0;
}

.search-area {
  width: 300px;
}

.auto-play-tips {
  margin-bottom: 20px;
}
</style>
