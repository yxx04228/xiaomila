<template>
  <div class="music-list">
    <div class="table-container" v-if="pagination.total > 0">
      <el-table
        :data="musicList"
        v-loading="loading"
        style="width: 100%"
        :default-sort="{ prop: 'createTime', order: 'descending' }"
      >
        <el-table-column label="歌曲" min-width="100">
          <template #default="{ row }">
            <div class="song-info">
              <div class="song-title">{{ row.title || '未知歌曲' }}</div>
              <div class="song-artist">{{ row.singer || '未知歌手' }}</div>
            </div>
          </template>
        </el-table-column>

        <el-table-column prop="album" label="专辑" min-width="100">
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

        <el-table-column label="操作" width="300" fixed="right">
          <template #default="{ row }">
            <el-button
              size="small"
              @click="handlePlayPause(row)"
              :type="isCurrentPlaying(row) ? 'success' : 'primary'"
              :loading="isCurrentPlaying(row) && audioLoading"
            >
              {{ getPlayButtonText(row) }}
            </el-button>
            <el-button size="small" @click="handleEdit(row)" :loading="editingId === row.id">
              修改
            </el-button>
            <el-button
              size="small"
              @click="handleDelete(row)"
              type="danger"
              :loading="deletingId === row.id"
            >
              删除
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
      <div class="illustration-empty">
        <div class="music-scene">
          <div class="record-player">
            <div class="record"></div>
            <div class="tone-arm"></div>
          </div>
        </div>
        <div class="illustration-text">
          <h3>音乐库等待中</h3>
          <p>添加一些音乐让播放器活跃起来</p>
        </div>
        <div class="illustration-actions">
          <el-button type="primary" @click="$emit('upload')">
            <el-icon><UploadFilled /></el-icon>
            上传音乐
          </el-button>
        </div>
      </div>
    </div>

    <!-- 修改歌曲信息对话框 -->
    <el-dialog
      v-model="editDialogVisible"
      title="修改歌曲信息"
      width="500px"
      :before-close="handleEditDialogClose"
    >
      <el-form :model="editForm" :rules="editFormRules" ref="editFormRef" label-width="80px">
        <el-form-item label="歌曲名称" prop="title">
          <el-input
            v-model="editForm.title"
            placeholder="请输入歌曲名称"
            maxlength="100"
            show-word-limit
          />
        </el-form-item>

        <el-form-item label="歌手" prop="singer">
          <el-input
            v-model="editForm.singer"
            placeholder="请输入歌手名称"
            maxlength="50"
            show-word-limit
          />
        </el-form-item>

        <el-form-item label="专辑" prop="album">
          <el-input
            v-model="editForm.album"
            placeholder="请输入专辑名称"
            maxlength="100"
            show-word-limit
          />
        </el-form-item>
      </el-form>

      <template #footer>
        <el-button @click="handleEditDialogClose">取消</el-button>
        <el-button type="primary" @click="handleEditSubmit" :loading="editingId !== ''">
          确认修改
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue'
import type { Music } from '@/types/music'
import { useMusicStore } from '@/stores/music'
import { storeToRefs } from 'pinia'
import { UploadFilled } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox, type FormInstance, type FormRules } from 'element-plus'
import { musicApi } from '@/api/music'

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
  refresh: [] // 刷新列表事件
}>()

// 跟踪正在下载的歌曲ID
const downloadingId = ref<string>('')
const editingId = ref<string>('')
const deletingId = ref<string>('')
const editDialogVisible = ref(false)
const editFormRef = ref<FormInstance>()

// 编辑表单数据
const editForm = reactive({
  id: '',
  title: '',
  singer: '',
  album: '',
})

// 表单验证规则
const editFormRules: FormRules = {
  title: [
    { required: true, message: '请输入歌曲名称', trigger: 'blur' },
    { min: 1, max: 100, message: '歌曲名称长度在 1 到 100 个字符', trigger: 'blur' },
  ],
  singer: [
    { required: true, message: '请输入歌手名称', trigger: 'blur' },
    { min: 1, max: 50, message: '歌手名称长度在 1 到 50 个字符', trigger: 'blur' },
  ],
  album: [
    { required: false, message: '请输入专辑名称', trigger: 'blur' },
    { max: 100, message: '专辑名称长度不能超过 100 个字符', trigger: 'blur' },
  ],
}

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

// 处理修改点击
const handleEdit = (music: Music) => {
  // 填充表单数据
  editForm.id = music.id
  editForm.title = music.title || ''
  editForm.singer = music.singer || ''
  editForm.album = music.album || ''

  editDialogVisible.value = true
}

// 处理修改对话框关闭
const handleEditDialogClose = () => {
  editDialogVisible.value = false
  if (editFormRef.value) {
    editFormRef.value.clearValidate()
  }
  // 重置表单
  Object.assign(editForm, {
    id: '',
    title: '',
    singer: '',
    album: '',
  })
}

// 处理修改提交
const handleEditSubmit = async () => {
  if (!editFormRef.value) return

  // 表单验证
  try {
    await editFormRef.value.validate()
  } catch (error) {
    ElMessage.warning('请完善表单信息')
    return
  }

  editingId.value = editForm.id

  try {
    // 调用更新接口
    const response = await musicApi.updateMusic(editForm)

    if (response.success) {
      ElMessage.success('修改成功')
      editDialogVisible.value = false
      // 触发刷新列表事件
      emit('refresh')
    } else {
      throw new Error(response.message || '更新失败')
    }
  } catch (error: any) {
    console.error('修改歌曲信息失败:', error)
    ElMessage.error(`修改失败: ${error.message || '请稍后重试'}`)
  } finally {
    editingId.value = ''
  }
}

// 处理删除点击
const handleDelete = async (music: Music) => {
  try {
    // 确认删除对话框
    await ElMessageBox.confirm(`确定要删除歌曲 "${music.title}" 吗？此操作不可恢复。`, '删除确认', {
      confirmButtonText: '确定删除',
      cancelButtonText: '取消',
      type: 'warning',
      confirmButtonClass: 'el-button--danger',
    })

    deletingId.value = music.id

    try {
      // 调用store的删除方法
      await musicStore.deleteMusic(music.id)
      ElMessage.success('删除成功')
      // 触发刷新事件，父组件可以重新获取数据
      emit('refresh')
    } catch (error: any) {
      console.error('删除歌曲失败:', error)
      ElMessage.error(`删除失败: ${error.message || '请稍后重试'}`)
    } finally {
      deletingId.value = ''
    }
  } catch (error) {
    // 用户取消删除
    console.log('用户取消删除')
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
  margin-right: 4px;
  margin-bottom: 2px;
  border-radius: 3px;
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
.illustration-empty {
  text-align: center;
  padding: 60px 24px;
}
.music-scene {
  margin-bottom: 32px;
  height: 120px;
  display: flex;
  align-items: center;
  justify-content: center;
}
.record-player {
  position: relative;
  width: 80px;
  height: 80px;
}
.record {
  width: 60px;
  height: 60px;
  border: 8px solid #e2e8f0;
  border-radius: 50%;
  position: absolute;
  top: 10px;
  left: 10px;
  animation: spin 4s linear infinite;
}
.record::before {
  content: '';
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  width: 12px;
  height: 12px;
  background: #cbd5e1;
  border-radius: 50%;
}
.tone-arm {
  position: absolute;
  top: 0;
  right: 0;
  width: 30px;
  height: 3px;
  background: #cbd5e1;
  transform-origin: left center;
  transform: rotate(-30deg);
  animation: toneArm 4s ease-in-out infinite;
}
@keyframes spin {
  from {
    transform: rotate(0deg);
  }
  to {
    transform: rotate(360deg);
  }
}
@keyframes toneArm {
  0%,
  100% {
    transform: rotate(-30deg);
  }
  50% {
    transform: rotate(-20deg);
  }
}
.illustration-text h3 {
  font-size: 18px;
  font-weight: 600;
  color: #374151;
  margin-bottom: 8px;
}
.illustration-text p {
  font-size: 14px;
  color: #6b7280;
  margin-bottom: 24px;
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
    margin-right: 2px;
    padding: 2px 6px;
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
