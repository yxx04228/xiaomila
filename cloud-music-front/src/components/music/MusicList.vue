<template>
  <div class="music-list">
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
          <el-button size="small" @click="$emit('play', row)" type="primary"> 播放 </el-button>
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

interface Props {
  musicList: Music[]
  loading: boolean
  pagination: {
    current: number
    pageSize: number
    total: number
  }
}

defineProps<Props>()
defineEmits<{
  play: [music: Music]
  download: [music: Music]
  pageChange: [page: number]
}>()

// 跟踪正在下载的歌曲ID
const downloadingId = ref<string>('')
</script>

<style scoped>
.music-list {
  background: white;
  border-radius: 8px;
  padding: 20px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
}

.song-info {
  line-height: 1.4;
}

.song-title {
  font-weight: 500;
  color: #333;
}

.song-artist {
  font-size: 12px;
  color: #666;
}

.pagination {
  margin-top: 20px;
  display: flex;
  justify-content: center;
}

.empty-state {
  padding: 40px 0;
}
</style>
