<template>
  <div class="music-list">
    <div class="table-container">
      <el-table
        :data="musicList"
        v-loading="loading"
        style="width: 100%"
        :default-sort="{ prop: 'createTime', order: 'descending' }"
      >
        <el-table-column label="歌曲" min-width="200">
          <template #default="{ row }">
            <div class="song-info">
              <div class="song-title">{{ row.title || '未知歌曲' }}</div>
              <div class="song-artist">{{ row.singer || '未知歌手' }}</div>
            </div>
          </template>
        </el-table-column>

        <el-table-column prop="album" label="专辑" min-width="150">
          <template #default="{ row }">
            {{ row.album || '未知专辑' }}
          </template>
        </el-table-column>

        <el-table-column prop="duration" label="时长" width="100" />

        <el-table-column prop="fileSize" label="大小" width="100" />

        <el-table-column prop="playCount" label="播放次数" width="100">
          <template #default="{ row }">
            {{ row.playCount || 0 }}
          </template>
        </el-table-column>

        <el-table-column prop="fileType" label="格式" width="80" />

        <el-table-column label="操作" width="220" fixed="right">
          <template #default="{ row }">
            <el-button
              size="small"
              @click="handlePlayPause(row)"
              :type="isCurrentPlaying(row) ? 'success' : 'primary'"
              :loading="isCurrentPlaying(row) && audioLoading"
            >
              {{ getPlayButtonText(row) }}
            </el-button>
            <el-button
              size="small"
              @click="$emit('download', row)"
              :loading="downloadingId === row.id"
              :disabled="downloadingId === row.id"
            >
              {{ downloadingId === row.id ? '下载中' : '下载' }}
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <div class="pagination" v-if="pagination.total > 0">
      <el-pagination
        v-model:current-page="pagination.current"
        :page-size="pagination.pageSize"
        :total="pagination.total"
        layout="total, prev, pager, next, jumper"
        @current-change="$emit('page-change', $event)"
      />
    </div>

    <div v-if="!loading && musicList.length === 0" class="empty-state">
      <el-empty description="暂无音乐数据" />
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import type { Music } from '@/types/music'
import { useMusicStore } from '@/stores/music'
import { storeToRefs } from 'pinia'

interface Props {
  musicList: Music[]
  loading: boolean
  pagination: {
    current: number
    pageSize: number
    total: number
  }
}

const props = defineProps<Props>()
const emit = defineEmits<{
  play: [music: Music]
  download: [music: Music]
  pageChange: [page: number]
}>()

// 跟踪正在下载的歌曲ID
const downloadingId = ref<string>('')

// 获取音乐 store
const musicStore = useMusicStore()
const { currentMusic, isPlaying, audioLoading } = storeToRefs(musicStore)

// 检查是否是当前播放的歌曲
const isCurrentPlaying = (music: Music) => {
  return currentMusic.value?.id === music.id
}

// 获取播放按钮文本
const getPlayButtonText = (music: Music) => {
  if (isCurrentPlaying(music)) {
    return isPlaying.value ? '暂停' : '播放'
  }
  return '播放'
}

// 处理播放/暂停点击
const handlePlayPause = async (music: Music) => {
  if (isCurrentPlaying(music)) {
    // 如果是当前播放的歌曲，切换播放/暂停状态
    await musicStore.togglePlay()
  } else {
    // 如果不是当前播放的歌曲，播放新歌曲
    emit('play', music)
  }
}
</script>

<style scoped>
.music-list {
  width: 100%;
  height: 100%; /* 占满父容器 */
  background-color: #ffffff;
  border-radius: 12px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
  overflow: hidden;
  display: flex;
  flex-direction: column;
  min-height: 0; /* 允许收缩 */
}

/* 表格容器 - 关键修复 */
.table-container {
  flex: 1;
  min-height: 0;
  overflow: hidden;
  display: flex;
  flex-direction: column;
}

.music-list .el-table {
  --el-table-bg-color: #ffffff;
  --el-table-text-color: #2d3748;
  --el-table-header-text-color: #4a5568;
  --el-table-header-bg-color: #f7fafc;
  --el-table-border-color: #e2e8f0;
  --el-table-row-hover-bg-color: #fef7fb;
  border-radius: 12px;
  overflow: hidden;
  flex: 1; /* 关键：表格在容器内弹性伸缩 */
}

.music-list .el-table__header-wrapper th {
  font-weight: 500;
  font-size: 13px;
  padding: 10px 8px;
  position: sticky;
  top: 0;
  z-index: 10;
  background-color: #f7fafc !important; /* 表头固定背景 */
}

.music-list .el-table__body-wrapper {
  overflow-y: auto; /* 表格体滚动 */
  flex: 1;
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

.music-list .el-table__body-wrapper td {
  padding: 12px 8px;
  font-size: 12px;
  color: #4a5568;
}

/* 歌曲信息样式 */
.song-info {
  display: flex;
  flex-direction: column;
  gap: 3px;
}

.song-title {
  font-size: 13px;
  font-weight: 500;
  color: #2d3748;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.song-artist {
  font-size: 11px;
  color: #718096;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

/* 操作列按钮样式 */
.music-list .el-table-column--fixed-right {
  background-color: #ffffff;
  border-left: 1px solid #e2e8f0;
}

.music-list .el-button {
  margin-right: 6px;
  border-radius: 4px;
  padding: 3px 10px;
  font-size: 11px;
}

.music-list .el-button--primary {
  --el-button-bg-color: #4299e1;
  --el-button-hover-bg-color: #3182ce;
  --el-button-text-color: #ffffff;
}

.music-list .el-button--success {
  --el-button-bg-color: #48bb78;
  --el-button-hover-bg-color: #38a169;
  --el-button-text-color: #ffffff;
}

.music-list .el-button:disabled {
  --el-button-disabled-bg-color: #f0f2f5;
  --el-button-disabled-text-color: #9ca3af;
}

/* 分页样式 */
.pagination {
  padding: 12px 16px;
  display: flex;
  justify-content: flex-end;
  border-top: 1px solid #e2e8f0;
  flex-shrink: 0; /* 禁止分页收缩 */
}

.music-list .el-pagination {
  --el-pagination-text-color: #4a5568;
  --el-pagination-hover-text-color: #4299e1;
  --el-pagination-active-text-color: #4299e1;
  --el-pagination-active-border-color: #4299e1;
  font-size: 12px;
}

/* 空状态样式 */
.empty-state {
  padding: 48px 24px;
  text-align: center;
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  min-height: 200px; /* 确保空状态有最小高度 */
}

.music-list .el-empty {
  --el-empty-text-color: #718096;
}

/* 加载状态样式 */
.music-list .el-loading-mask {
  --el-loading-bg-color: rgba(255, 255, 255, 0.8);
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  z-index: 5;
}

/* 响应式调整 */
@media (max-width: 1200px) {
  .music-list .el-table-column--fixed-right {
    position: static;
    border-left: none;
  }
}

@media (max-width: 768px) {
  .music-list {
    border-radius: 8px;
  }

  .music-list .el-table__header-wrapper th,
  .music-list .el-table__body-wrapper td {
    padding: 8px 6px;
    font-size: 11px;
  }

  .song-title {
    font-size: 12px;
  }

  .music-list .el-button {
    margin-right: 4px;
    padding: 2px 8px;
    font-size: 10px;
  }

  .pagination {
    padding: 10px;
    justify-content: center;
  }

  .music-list .el-pagination {
    font-size: 11px;
  }
}

@media (max-width: 480px) {
  .music-list .el-table-column--fixed-right .el-button {
    display: block;
    width: 100%;
    margin-bottom: 4px;
  }

  .empty-state {
    padding: 32px 16px;
    min-height: 150px;
  }
}
</style>
