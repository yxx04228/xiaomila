<template>
  <div class="home-page">
    <div class="page-header">
      <h1>发现音乐</h1>
      <div class="header-actions">
        <div class="search-area">
          <el-input
            v-model="searchTitle"
            placeholder="歌曲名称"
            clearable
            @clear="handleSearch"
            @keyup.enter="handleSearch"
            @input="handleInput"
            class="composite-search"
          >
            <template #prepend>
              <el-input
                v-model="searchSinger"
                placeholder="歌手名称"
                clearable
                @clear="handleSearch"
                @keyup.enter="handleSearch"
                @input="handleInput"
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
        <!-- 上传按钮 -->
        <el-button type="primary" @click="showUploadDialog" :icon="UploadFilled">
          上传音乐
        </el-button>
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
        @refresh="handleRefresh"
      />
    </div>

    <!-- 上传音乐对话框 -->
    <el-dialog
      v-model="uploadDialogVisible"
      title="上传音乐"
      width="800px"
      :before-close="handleUploadDialogClose"
      align-center
    >
      <div class="upload-container">
        <!-- 文件选择区域 -->
        <div class="file-select-area">
          <el-upload
            ref="uploadRef"
            :action="''"
            :multiple="true"
            :show-file-list="false"
            :file-list="pendingFiles"
            :before-upload="beforeUpload"
            :on-change="handleFileChange"
            :on-remove="handleFileRemove"
            :on-exceed="handleExceed"
            :limit="50"
            accept=".mp3,.wav,.flac,.aac,.m4a,.ogg,.wma"
            drag
            :auto-upload="false"
          >
            <div class="upload-area">
              <el-icon class="el-icon--upload"><upload-filled /></el-icon>
              <div class="el-upload__text">点击或拖拽文件到此处添加</div>
              <div class="el-upload__tip">
                支持 mp3、wav、flac、aac、m4a、ogg、wma 格式，单个文件不超过 100MB
              </div>
            </div>
          </el-upload>
        </div>

        <!-- Tab 页切换 -->
        <div class="upload-tabs">
          <el-tabs v-model="activeTab" type="card">
            <!-- 待上传文件 Tab -->
            <el-tab-pane label="待上传文件" name="pending">
              <div v-if="pendingFiles.length > 0" class="file-list-container">
                <div class="file-list-header">
                  <span class="file-list-title">待上传文件 ({{ pendingFiles.length }})</span>
                  <el-button size="small" text @click="clearPendingFiles" :disabled="isUploading">
                    清空列表
                  </el-button>
                </div>
                <div class="file-list-scroll">
                  <div
                    v-for="file in pendingFiles"
                    :key="file.uid"
                    class="file-item"
                    :class="{
                      uploading: file.status === 'uploading',
                      success: file.status === 'success',
                      error: file.status === 'error',
                    }"
                  >
                    <div class="file-info">
                      <div class="file-icon">
                        <el-icon><Document /></el-icon>
                      </div>
                      <div class="file-details">
                        <div class="file-name">{{ file.name }}</div>
                        <div class="file-meta">
                          <span class="file-size">{{ formatFileSize(file.size) }}</span>
                          <span class="file-type">{{ getFileType(file.name) }}</span>
                        </div>
                        <!-- 错误信息显示 -->
                        <div v-if="file.status === 'error' && file.errorMessage" class="file-error">
                          <el-icon><Warning /></el-icon>
                          <span class="error-text">{{ file.errorMessage }}</span>
                        </div>
                      </div>
                    </div>

                    <div class="file-status">
                      <template v-if="file.status === 'ready'">
                        <el-tag size="small" effect="plain">等待上传</el-tag>
                      </template>
                      <template v-else-if="file.status === 'uploading'">
                        <div class="upload-progress">
                          <el-progress
                            :percentage="file.percentage || 0"
                            :show-text="false"
                            :stroke-width="6"
                          />
                          <span class="progress-text"
                            >{{ (file.percentage || 0).toFixed(2) }}%</span
                          >
                        </div>
                      </template>
                      <template v-else-if="file.status === 'success'">
                        <el-tag size="small" type="success" effect="plain">
                          <el-icon><CircleCheck /></el-icon>
                          上传成功
                        </el-tag>
                      </template>
                      <template v-else-if="file.status === 'error'">
                        <el-tag size="small" type="danger" effect="plain">
                          <el-icon><CircleClose /></el-icon>
                          上传失败
                        </el-tag>
                      </template>
                    </div>

                    <div class="file-actions">
                      <el-button
                        v-if="file.status === 'ready' || file.status === 'error'"
                        text
                        size="small"
                        type="danger"
                        @click="handleRemovePendingFile(file)"
                      >
                        移除
                      </el-button>
                      <el-button
                        v-else-if="file.status === 'uploading'"
                        text
                        size="small"
                        @click="handleCancelUpload(file)"
                      >
                        取消
                      </el-button>
                    </div>
                  </div>
                </div>
              </div>
              <div v-else class="empty-tab">
                <el-empty description="暂无待上传文件" :image-size="100" />
              </div>
            </el-tab-pane>

            <!-- 已上传文件 Tab -->
            <el-tab-pane label="已上传文件" name="completed">
              <div v-if="completedFiles.length > 0" class="file-list-container">
                <div class="file-list-header">
                  <span class="file-list-title">已上传文件 ({{ completedFiles.length }})</span>
                  <el-button size="small" text @click="clearCompletedFiles"> 清空列表 </el-button>
                </div>
                <div class="file-list-scroll">
                  <div v-for="file in completedFiles" :key="file.uid" class="file-item success">
                    <div class="file-info">
                      <div class="file-icon">
                        <el-icon><Document /></el-icon>
                      </div>
                      <div class="file-details">
                        <div class="file-name">{{ file.name }}</div>
                        <div class="file-meta">
                          <span class="file-size">{{ formatFileSize(file.size) }}</span>
                          <span class="file-type">{{ getFileType(file.name) }}</span>
                        </div>
                      </div>
                    </div>

                    <div class="file-status">
                      <el-tag size="small" type="success" effect="plain">
                        <el-icon><CircleCheck /></el-icon>
                        上传成功
                      </el-tag>
                    </div>

                    <div class="file-actions">
                      <el-button
                        text
                        size="small"
                        type="danger"
                        @click="handleRemoveCompletedFile(file)"
                      >
                        移除
                      </el-button>
                    </div>
                  </div>
                </div>
              </div>
              <div v-else class="empty-tab">
                <el-empty description="暂无已上传文件" :image-size="100" />
              </div>
            </el-tab-pane>
          </el-tabs>
        </div>
      </div>

      <template #footer>
        <el-button @click="handleUploadDialogClose"> 取消 </el-button>
        <el-button
          type="primary"
          @click="handleStartUpload"
          :loading="isUploading"
          :disabled="pendingFiles.length === 0 || getReadyFilesCount === 0"
        >
          {{
            isUploading
              ? `上传中 (${uploadStats.success + uploadStats.failed}/${uploadStats.total})`
              : '开始上传'
          }}
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { onMounted, onUnmounted, ref, reactive, computed } from 'vue'
import {
  Search,
  Upload,
  UploadFilled,
  Loading,
  CircleCheck,
  CircleClose,
  Document,
  Warning,
} from '@element-plus/icons-vue'
import {
  ElMessage,
  ElMessageBox,
  type UploadInstance,
  type UploadRawFile,
  type UploadFile,
} from 'element-plus'
import MusicList from '@/components/music/MusicList.vue'
import { useMusicStore } from '@/stores/music'
import type { Music } from '@/types/music'
import { downloadMusicFile } from '@/utils/download'

const musicStore = useMusicStore()
const searchTitle = ref('')
const searchSinger = ref('')
const searchTimer = ref<number>()

// 上传相关变量
const uploadDialogVisible = ref(false)
const uploadRef = ref<UploadInstance>()
const activeTab = ref('pending') // 当前激活的tab

// 文件列表分离
const pendingFiles = ref<UploadFile[]>([]) // 待上传文件
const completedFiles = ref<UploadFile[]>([]) // 已上传成功文件

// 上传配置
const uploadAction = '/api/music/upload'
const uploadHeaders = reactive({
  Authorization: `Bearer ${localStorage.getItem('token') || ''}`,
})

// 上传统计
const uploadStats = reactive({
  total: 0,
  ready: 0,
  uploading: 0,
  success: 0,
  failed: 0,
})

// 计算属性
const isUploading = computed(() => uploadStats.uploading > 0)
const getReadyFilesCount = computed(() => {
  return pendingFiles.value.filter((f) => f.status === 'ready').length
})

const loadMusicList = () => {
  musicStore.fetchMusicList({
    title: searchTitle.value,
    singer: searchSinger.value,
    nCurrent: musicStore.pagination.current,
    nSize: musicStore.pagination.pageSize,
  })
}

const handleSearch = () => {
  if (searchTimer.value) {
    clearTimeout(searchTimer.value)
  }
  musicStore.pagination.current = 1
  loadMusicList()
}

const handleInput = () => {
  if (searchTimer.value) {
    clearTimeout(searchTimer.value)
  }
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

const handleRefresh = () => {
  loadMusicList()
}

// 上传相关方法
const showUploadDialog = () => {
  uploadDialogVisible.value = true
  activeTab.value = 'pending'
  resetUploadStats()
}

const handleUploadDialogClose = () => {
  // 关闭对话框时刷新页面
  handleRefresh()
  uploadDialogVisible.value = false
  resetUpload()
}

const resetUpload = () => {
  pendingFiles.value = []
  completedFiles.value = []
  resetUploadStats()
}

const resetUploadStats = () => {
  uploadStats.total = 0
  uploadStats.ready = 0
  uploadStats.uploading = 0
  uploadStats.success = 0
  uploadStats.failed = 0
}

const updateUploadStats = () => {
  const stats = {
    total: pendingFiles.value.length,
    ready: pendingFiles.value.filter((f) => f.status === 'ready').length,
    uploading: pendingFiles.value.filter((f) => f.status === 'uploading').length,
    success: pendingFiles.value.filter((f) => f.status === 'success').length,
    failed: pendingFiles.value.filter((f) => f.status === 'error').length,
  }

  Object.assign(uploadStats, stats)
}

const beforeUpload = (file: UploadRawFile) => {
  // 检查文件类型
  const allowedTypes = [
    'audio/mpeg', // mp3
    'audio/wav',
    'audio/x-wav',
    'audio/flac',
    'audio/x-flac',
    'audio/aac',
    'audio/x-m4a',
    'audio/ogg',
    'audio/wma',
  ]

  const fileExtension = file.name.split('.').pop()?.toLowerCase()
  const allowedExtensions = ['mp3', 'wav', 'flac', 'aac', 'm4a', 'ogg', 'wma']

  if (!allowedTypes.includes(file.type) && !allowedExtensions.includes(fileExtension || '')) {
    const errorFile: UploadFile = {
      uid: Date.now().toString(),
      name: file.name,
      size: file.size,
      raw: file,
      status: 'error',
      errorMessage: '不支持的文件格式',
    }
    pendingFiles.value.push(errorFile)
    updateUploadStats()
    return false
  }

  // 检查文件大小 (100MB)
  const isLt100M = file.size / 1024 / 1024 < 100
  if (!isLt100M) {
    const errorFile: UploadFile = {
      uid: Date.now().toString(),
      name: file.name,
      size: file.size,
      raw: file,
      status: 'error',
      errorMessage: '文件大小不能超过 100MB',
    }
    pendingFiles.value.push(errorFile)
    updateUploadStats()
    return false
  }

  // 检查是否重复（在待上传列表中）
  const isDuplicate = pendingFiles.value.some((f) => f.name === file.name && f.size === file.size)
  if (isDuplicate) {
    const errorFile: UploadFile = {
      uid: Date.now().toString(),
      name: file.name,
      size: file.size,
      raw: file,
      status: 'error',
      errorMessage: '文件已存在',
    }
    pendingFiles.value.push(errorFile)
    updateUploadStats()
    return false
  }

  return true
}

const handleFileChange = (file: UploadFile) => {
  // 手动管理文件列表
  if (file.status === 'ready') {
    // 检查是否已存在相同文件
    const existingFile = pendingFiles.value.find(
      (f) => f.name === file.name && f.size === file.size
    )

    if (!existingFile) {
      // 添加文件到待上传列表
      pendingFiles.value.push({
        ...file,
        status: 'ready',
        percentage: 0,
        errorMessage: '',
      })
      updateUploadStats()
    }
  }
}

const handleFileRemove = (file: UploadFile) => {
  const index = pendingFiles.value.findIndex((f) => f.uid === file.uid)
  if (index > -1) {
    pendingFiles.value.splice(index, 1)
    updateUploadStats()
  }
}

// 清空待上传文件
const clearPendingFiles = () => {
  if (pendingFiles.value.length === 0) return

  ElMessageBox.confirm('确定要清空所有待上传文件吗？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning',
  }).then(() => {
    pendingFiles.value = []
    resetUploadStats()
  })
}

// 清空已上传文件
const clearCompletedFiles = () => {
  if (completedFiles.value.length === 0) return

  ElMessageBox.confirm('确定要清空所有已上传文件记录吗？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning',
  }).then(() => {
    completedFiles.value = []
  })
}

// 移除待上传文件
const handleRemovePendingFile = (file: UploadFile) => {
  ElMessageBox.confirm(`确定要移除文件 ${file.name} 吗？`, '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning',
  }).then(() => {
    handleFileRemove(file)
  })
}

// 移除已上传文件
const handleRemoveCompletedFile = (file: UploadFile) => {
  ElMessageBox.confirm(`确定要移除文件 ${file.name} 的记录吗？`, '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning',
  }).then(() => {
    const index = completedFiles.value.findIndex((f) => f.uid === file.uid)
    if (index > -1) {
      completedFiles.value.splice(index, 1)
    }
  })
}

const handleExceed = () => {
  ElMessage.warning('最多只能上传 50 个文件')
}

const formatFileSize = (bytes: number) => {
  if (bytes === 0) return '0 B'
  const k = 1024
  const sizes = ['B', 'KB', 'MB', 'GB']
  const i = Math.floor(Math.log(bytes) / Math.log(k))
  return parseFloat((bytes / Math.pow(k, i)).toFixed(2)) + ' ' + sizes[i]
}

const getFileType = (filename: string) => {
  const ext = filename.split('.').pop()?.toUpperCase()
  return ext || '未知'
}

// 手动上传文件
const handleStartUpload = async () => {
  if (pendingFiles.value.length === 0) {
    ElMessage.warning('请先选择要上传的文件')
    return
  }

  const readyFiles = pendingFiles.value.filter((f) => f.status === 'ready')
  if (readyFiles.length === 0) {
    ElMessage.warning('没有可上传的文件')
    return
  }

  // 重置统计（支持重复点击）
  uploadStats.total = readyFiles.length
  uploadStats.ready = readyFiles.length
  uploadStats.uploading = 0
  uploadStats.success = 0
  uploadStats.failed = 0

  // 开始上传
  for (const file of readyFiles) {
    await uploadSingleFile(file)
  }

  // 上传完成后不自动关闭弹窗，等待用户手动取消
}

const uploadSingleFile = async (file: UploadFile): Promise<void> => {
  return new Promise((resolve) => {
    file.status = 'uploading'
    file.percentage = 0
    file.errorMessage = ''
    updateUploadStats()

    // 模拟上传进度
    const progressInterval = setInterval(() => {
      if (file.percentage < 90) {
        file.percentage += Math.random() * 20
      }
    }, 200)

    // 创建 FormData
    const formData = new FormData()
    formData.append('file', file.raw!)

    // 实际的上传请求
    fetch(uploadAction, {
      method: 'POST',
      headers: uploadHeaders,
      body: formData,
    })
      .then(async (response) => {
        clearInterval(progressInterval)
        file.percentage = 100

        const result = await response.json()
        if (response.ok && result.success) {
          file.status = 'success'
          // 将成功文件移动到已上传列表
          setTimeout(() => {
            const index = pendingFiles.value.findIndex((f) => f.uid === file.uid)
            if (index > -1) {
              const completedFile = { ...pendingFiles.value[index] }
              completedFiles.value.push(completedFile)
              pendingFiles.value.splice(index, 1)
            }
          }, 500)
        } else {
          // 从后端返回的错误信息
          const errorMessage = result.message || result.error || '上传失败'
          file.status = 'error'
          file.errorMessage = errorMessage
        }
      })
      .catch((error) => {
        clearInterval(progressInterval)
        file.status = 'error'
        file.errorMessage = error.message || '网络错误，请检查网络连接'
        console.error('上传失败:', error)
      })
      .finally(() => {
        updateUploadStats()
        resolve()
      })
  })
}

const handleCancelUpload = (file: UploadFile) => {
  file.status = 'error'
  file.errorMessage = '上传已取消'
  updateUploadStats()
}

onMounted(() => {
  musicStore.enableAutoPlay()
  loadMusicList()
})

onUnmounted(() => {
  if (searchTimer.value) {
    clearTimeout(searchTimer.value)
  }
  musicStore.disableAutoPlay()
  musicStore.cleanupBlobUrl()
})
</script>

<style scoped>
.home-page {
  width: 100%;
  height: 100%;
  display: flex;
  flex-direction: column;
  gap: 16px;
  min-height: 0;
}

.page-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 8px;
  padding-bottom: 12px;
  border-bottom: 1px solid #e2e8f0;
  flex-shrink: 0;
}

.page-header h1 {
  font-size: 24px;
  font-weight: 600;
  color: #2d3748;
  margin: 0;
}

.header-actions {
  display: flex;
  align-items: center;
  gap: 16px;
}

.search-area {
  flex: 1;
  max-width: 500px;
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

/* 音乐列表容器 */
.music-list-container {
  flex: 1;
  min-height: 0;
  overflow: hidden;
  display: flex;
  flex-direction: column;
}

/* 上传相关样式 */
.upload-container {
  padding: 8px 0;
}

.file-select-area {
  margin-bottom: 20px;
}

.upload-area {
  padding: 10px 0;
  text-align: center;
}

.el-icon--upload {
  font-size: 48px;
  color: #c0c4cc;
  margin-bottom: 16px;
}

.el-upload__text {
  font-size: 14px;
  color: #606266;
  margin-bottom: 8px;
}

.el-upload__text em {
  color: #409eff;
  font-style: normal;
}

.el-upload__tip {
  font-size: 12px;
  color: #909399;
  margin-top: 8px;
}

/* Tab 页样式 */
.upload-tabs {
  margin-top: 20px;
}

:deep(.upload-tabs .el-tabs__header) {
  margin-bottom: 0;
}

:deep(.upload-tabs .el-tabs__content) {
  padding: 0;
}

.empty-tab {
  padding: 40px 0;
  text-align: center;
}

/* 文件列表容器 */
.file-list-container {
  border: 1px solid #e4e7ed;
  border-radius: 8px;
  overflow: hidden;
  margin-top: 0;
}

.file-list-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 12px 16px;
  background: #f5f7fa;
  border-bottom: 1px solid #e4e7ed;
}

.file-list-title {
  font-size: 14px;
  font-weight: 600;
  color: #303133;
}

/* 可滚动的文件列表 */
.file-list-scroll {
  max-height: 300px;
  overflow-y: auto;
  padding: 8px;
}

.file-list-scroll::-webkit-scrollbar {
  width: 6px;
}

.file-list-scroll::-webkit-scrollbar-thumb {
  background-color: #c0c4cc;
  border-radius: 3px;
}

.file-list-scroll::-webkit-scrollbar-track {
  background-color: #f5f7fa;
}

/* 文件项样式 */
.file-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px;
  margin-bottom: 8px;
  background: #ffffff;
  border: 1px solid #e4e7ed;
  border-radius: 6px;
  transition: all 0.3s ease;
}

.file-item:hover {
  border-color: #409eff;
  box-shadow: 0 2px 8px rgba(64, 158, 255, 0.1);
}

.file-item.uploading {
  border-left: 3px solid #e6a23c;
}

.file-item.success {
  border-left: 3px solid #67c23a;
}

.file-item.error {
  border-left: 3px solid #f56c6c;
}

.file-info {
  display: flex;
  align-items: center;
  gap: 12px;
  flex: 1;
  min-width: 0;
}

.file-icon {
  color: #409eff;
  font-size: 20px;
}

.file-details {
  flex: 1;
  min-width: 0;
}

.file-name {
  font-size: 14px;
  font-weight: 500;
  color: #303133;
  margin-bottom: 4px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.file-meta {
  display: flex;
  gap: 12px;
  font-size: 12px;
  color: #909399;
  margin-bottom: 4px;
}

/* 错误信息样式 */
.file-error {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 12px;
  color: #f56c6c;
  margin-top: 4px;
}

.file-error .el-icon {
  font-size: 14px;
}

.error-text {
  flex: 1;
  word-break: break-all;
}

.file-status {
  flex-shrink: 0;
  width: 120px;
}

.upload-progress {
  display: flex;
  align-items: center;
  gap: 8px;
}

.upload-progress .el-progress {
  flex: 1;
}

.progress-text {
  font-size: 12px;
  color: #e6a23c;
  font-weight: 500;
  min-width: 35px;
}

.file-actions {
  flex-shrink: 0;
}

/* 响应式调整 */
@media (max-width: 768px) {
  .home-page {
    gap: 12px;
  }

  .page-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 12px;
    padding-bottom: 8px;
  }

  .header-actions {
    width: 100%;
    flex-direction: column;
    gap: 12px;
  }

  .search-area {
    width: 100%;
    max-width: none;
  }

  .upload-area {
    padding: 20px 0;
  }

  .file-item {
    flex-direction: column;
    align-items: flex-start;
    gap: 8px;
  }

  .file-status {
    width: 100%;
  }

  .file-actions {
    align-self: flex-end;
  }
}
</style>
