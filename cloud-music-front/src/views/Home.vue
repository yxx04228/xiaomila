<template>
  <div class="home-page">
    <div class="page-header">
      <h1>发现音乐</h1>
      <div class="search-area">
        <el-input
          v-model="searchTitle"
          placeholder="歌曲名称"
          clearable
          @clear="handleSearch"
          @keyup.enter="handleSearch"
          class="composite-search"
        >
          <template #prepend>
            <el-input
              v-model="searchSinger"
              placeholder="歌手名称"
              clearable
              @clear="handleSearch"
              @keyup.enter="handleSearch"
              style="width: 150px; border: none; --el-input-border-color: transparent"
            />
          </template>
          <template #append>
            <el-button @click="handleSearch">
              <el-icon><Search /></el-icon>
            </el-button>
          </template>
        </el-input>
      </div>
    </div>

    <!-- 音乐列表容器 -->
    <div class="music-list-container">
      <MusicList
        :music-list="musicStore.musicList"
        :loading="musicStore.loading"
        :pagination="musicStore.pagination"
        @play="handlePlay"
        @download="handleDownload"
        @page-change="handlePageChange"
      />
    </div>
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
const searchSinger = ref('')
const searchTimer = ref<number>()

const loadMusicList = () => {
  musicStore.fetchMusicList({
    title: searchTitle.value,
    singer: searchSinger.value,
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
    console.log('开始播放音乐:', music.title)
    await musicStore.playMusic(music)
    console.log('播放成功:', music.title)
  } catch (error: any) {
    console.error('播放失败:', error)

    if (error.message.includes('初始化超时')) {
      ElMessage.error('播放器初始化失败，请刷新页面重试')
    } else {
      ElMessage.error(`播放失败: ${error.message || '未知错误'}`)
    }
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
  width: 100%;
  height: 100%; /* 继承父容器高度 */
  display: flex;
  flex-direction: column;
  gap: 16px;
  min-height: 0; /* 关键：允许收缩 */
}

.page-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 8px;
  padding-bottom: 12px;
  border-bottom: 1px solid #e2e8f0;
  flex-shrink: 0; /* 禁止头部收缩 */
}

.page-header h1 {
  font-size: 24px;
  font-weight: 600;
  color: #2d3748;
  margin: 0;
}

.composite-search {
  max-width: 500px;
}
:deep(.composite-search .el-input-group__prepend) {
  background-color: #fff;
  border-right: 1px solid #dcdfe6;
  padding: 0;
}
:deep(.composite-search .el-input-group__prepend .el-input) {
  --el-input-bg-color: transparent;
}
:deep(.composite-search .el-input-group__prepend .el-input__wrapper) {
  box-shadow: none;
  border: none;
}

/* 音乐列表容器 - 关键修复 */
.music-list-container {
  flex: 1;
  min-height: 0; /* 关键：允许在flex容器中滚动 */
  overflow: hidden; /* 隐藏自身滚动，让内部组件处理 */
  display: flex;
  flex-direction: column;
}

/* 响应式调整 */
@media (max-width: 768px) {
  .home-page {
    gap: 12px;
  }

  .page-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 10px;
    padding-bottom: 8px;
  }

  .search-area {
    width: 100%;
  }
}
</style>
