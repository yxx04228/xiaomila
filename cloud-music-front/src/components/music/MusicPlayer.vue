<template>
  <div class="music-player" v-if="currentMusic">
    <div class="player-content">
      <!-- 歌曲信息 -->
      <div class="song-info">
        <div class="album-cover">
          <el-avatar :size="50" :src="getAlbumCover(currentMusic)" shape="square">
            <el-icon><Headset /></el-icon>
          </el-avatar>
        </div>
        <div class="song-details">
          <div class="song-title">{{ currentMusic.title }}</div>
          <div class="song-artist">{{ currentMusic.singer }}</div>
          <div class="loading-status" v-if="audioLoading">
            <el-icon class="is-loading"><Loading /></el-icon>
            <span>加载中...</span>
          </div>
        </div>
      </div>

      <!-- 播放控制 -->
      <div class="playback-controls">
        <div class="control-buttons">
          <el-button
            :icon="Refresh"
            text
            @click="toggleLoopMode"
            :type="loopMode !== 'none' ? 'primary' : ''"
            :title="getLoopModeText()"
          />
          <el-button :icon="ArrowLeft" text @click="playPrevious" title="上一首" />
          <el-button
            :icon="isPlaying ? VideoPause : VideoPlay"
            circle
            type="primary"
            size="large"
            @click="handlePlayButtonClick"
            :title="isPlaying ? '暂停' : '播放'"
            :loading="audioLoading"
            :disabled="!currentMusic && musicList.length === 0"
          />
          <el-button :icon="ArrowRight" text @click="playNext" title="下一首" />
          <el-button
            :icon="Microphone"
            text
            @click="toggleMute"
            :type="isMuted ? 'danger' : ''"
            :title="isMuted ? '取消静音' : '静音'"
          />
        </div>

        <!-- 进度条 -->
        <div class="progress-container" v-if="duration > 0">
          <span class="time-current">{{ formatTime(currentTime) }}</span>
          <el-slider
            v-model="currentTime"
            :max="duration"
            :show-tooltip="false"
            @change="setCurrentTime"
            class="progress-slider"
          />
          <span class="time-total">{{ formatTime(duration) }}</span>
        </div>
        <div class="progress-container" v-else>
          <span class="time-current">0:00</span>
          <el-slider :value="0" disabled class="progress-slider" />
          <span class="time-total">{{ currentMusic.duration || '0:00' }}</span>
        </div>
      </div>

      <!-- 其他控制 -->
      <div class="extra-controls">
        <el-button :icon="Download" text @click="handleDownload" title="下载" />
        <!-- 使用麦克风图标替代音量图标 -->
        <el-popover placement="top" width="60" trigger="click">
          <template #reference>
            <el-button :icon="Microphone" text title="音量" />
          </template>
          <el-slider
            v-model="volume"
            vertical
            height="80px"
            :show-tooltip="false"
            @input="setVolume"
          />
        </el-popover>
      </div>
    </div>

    <!-- 音频元素 -->
    <audio
      ref="audioRef"
      @loadedmetadata="handleLoadedMetadata"
      @timeupdate="handleTimeUpdate"
      @ended="handleEnded"
      @error="handleError"
      @loadstart="handleLoadStart"
      @canplay="handleCanPlay"
      preload="none"
      controls
      style="display: none"
    />

    <!-- 错误提示 -->
    <el-dialog v-model="showErrorDialog" title="播放错误" width="400px" center>
      <p>无法播放音频文件，可能的原因：</p>
      <ul style="text-align: left; margin: 10px 0">
        <li>音频文件不存在</li>
        <li>文件格式不支持</li>
        <li>网络连接问题</li>
      </ul>
      <template #footer>
        <el-button @click="showErrorDialog = false">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, watch, onMounted, onUnmounted, computed } from 'vue'
import {
  Headset,
  ArrowLeft,
  ArrowRight,
  VideoPause,
  VideoPlay,
  Refresh,
  Microphone,
  Download,
  Loading,
} from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { useMusicStore } from '@/stores/music'
import { storeToRefs } from 'pinia'
import { downloadMusicFile } from '@/utils/download'

const musicStore = useMusicStore()
const {
  currentMusic,
  isPlaying,
  currentTime,
  duration,
  volume,
  loopMode,
  isMuted,
  audioLoading,
  audioElementReady,
} = storeToRefs(musicStore)

const {
  initAudioElement,
  togglePlay,
  setCurrentTime,
  setVolume,
  toggleLoopMode,
  toggleMute,
  playNext,
  playPrevious,
  formatTime,
  cleanupBlobUrl,
} = musicStore

const audioRef = ref<HTMLAudioElement>()
const showErrorDialog = ref(false)
const playerReady = ref(false) // 新增：播放器就绪状态

// 获取循环模式文本
const getLoopModeText = () => {
  switch (loopMode.value) {
    case 'one':
      return '单曲循环'
    case 'all':
      return '列表循环'
    default:
      return '不循环'
  }
}

// 获取专辑封面（模拟）
const getAlbumCover = (music: any) => {
  return ''
}

// 音频事件处理
const handleLoadStart = () => {
  console.log('开始加载音频...')
}

const handleLoadedMetadata = () => {
  console.log('音频元数据加载完成')
  if (audioRef.value) {
    duration.value = audioRef.value.duration
    console.log('音频时长:', duration.value)
  }
}

const handleCanPlay = () => {
  console.log('音频可以播放了')
}

const handleTimeUpdate = () => {
  if (audioRef.value) {
    currentTime.value = audioRef.value.currentTime
  }
}

const handleEnded = () => {
  console.log('音频播放结束')
}

const handleError = (error: any) => {
  console.error('音频播放错误:', error)
  showErrorDialog.value = true

  const audioElement = audioRef.value
  if (audioElement?.error) {
    switch (audioElement.error.code) {
      case MediaError.MEDIA_ERR_ABORTED:
        ElMessage.error('音频加载被中止')
        break
      case MediaError.MEDIA_ERR_NETWORK:
        ElMessage.error('网络错误，无法加载音频')
        break
      case MediaError.MEDIA_ERR_DECODE:
        ElMessage.error('音频解码错误，文件可能已损坏')
        break
      case MediaError.MEDIA_ERR_SRC_NOT_SUPPORTED:
        ElMessage.error('音频格式不支持或文件不存在')
        break
      default:
        ElMessage.error('未知播放错误')
    }
  }
}

// 下载当前歌曲
const handleDownload = async () => {
  if (currentMusic.value) {
    await downloadMusicFile(currentMusic.value)
  }
}

// 播放按钮点击处理
const handlePlayButtonClick = async () => {
  if (!playerReady.value || !audioElementReady.value) {
    ElMessage.warning('播放器正在初始化，请稍候...')
    return
  }

  try {
    await togglePlay()
  } catch (error: any) {
    ElMessage.error(`播放失败: ${error.message || '请稍后重试'}`)
  }
}

// 初始化音频元素
onMounted(() => {
  if (audioRef.value) {
    initAudioElement(audioRef.value)
    console.log('音频元素已初始化')
  } else {
    console.error('音频元素引用为空')
  }
})

// 监听播放状态变化
watch(isPlaying, async (playing) => {
  if (!audioRef.value || !currentMusic.value) return

  try {
    if (playing) {
      console.log('开始播放音频...')
      // 播放状态由store中的playMusic方法控制
    } else {
      audioRef.value.pause()
      console.log('音频已暂停')
    }
  } catch (error: any) {
    console.error('播放控制失败:', error)
    ElMessage.error(`播放失败: ${error.message}`)
    musicStore.isPlaying = false
  }
})

// 监听音量变化
watch(volume, (newVolume) => {
  setVolume(newVolume)
})

// 组件卸载时清理
onUnmounted(() => {
  if (audioRef.value) {
    audioRef.value.pause()
  }
  cleanupBlobUrl()
})
</script>

<style scoped>
/* 就绪状态样式 */
.ready-status {
  margin-top: 4px;
}

.ready-text {
  font-size: 12px;
  color: #52c41a;
}

.loading-status {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 12px;
  color: #1890ff;
  margin-top: 4px;
}

.loading-status .el-icon {
  animation: rotating 2s linear infinite;
}

@keyframes rotating {
  from {
    transform: rotate(0deg);
  }
  to {
    transform: rotate(360deg);
  }
}

.music-player {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  background: white;
  border-top: 1px solid #e4e7ed;
  padding: 10px 20px;
  box-shadow: 0 -2px 20px rgba(0, 0, 0, 0.1);
  z-index: 1000;
}

.player-content {
  display: flex;
  justify-content: space-between;
  align-items: center;
  max-width: 1200px;
  margin: 0 auto;
  gap: 20px;
}

.song-info {
  display: flex;
  align-items: center;
  gap: 12px;
  flex: 1;
  min-width: 200px;
}

.song-details {
  flex: 1;
}

.song-title {
  font-weight: 500;
  color: #333;
  margin-bottom: 2px;
  font-size: 14px;
}

.song-artist {
  font-size: 12px;
  color: #666;
}

.playback-controls {
  flex: 2;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
  min-width: 400px;
}

.control-buttons {
  display: flex;
  align-items: center;
  gap: 8px;
}

.progress-container {
  display: flex;
  align-items: center;
  gap: 12px;
  width: 100%;
  max-width: 500px;
}

.time-current,
.time-total {
  font-size: 12px;
  color: #666;
  min-width: 40px;
}

.progress-slider {
  flex: 1;
}

.extra-controls {
  display: flex;
  align-items: center;
  gap: 8px;
  flex: 1;
  justify-content: flex-end;
  min-width: 120px;
}

/* 播放列表样式 */
.playlist-content {
  padding: 0 10px;
}

.playlist-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
  padding-bottom: 12px;
  border-bottom: 1px solid #f0f0f0;
}

.playlist-header h4 {
  margin: 0;
  color: #333;
}

.playlist-items {
  max-height: 400px;
  overflow-y: auto;
}

.playlist-item {
  display: flex;
  align-items: center;
  padding: 8px 12px;
  border-radius: 6px;
  cursor: pointer;
  transition: background-color 0.2s;
  gap: 12px;
}

.playlist-item:hover {
  background-color: #f5f5f5;
}

.playlist-item.active {
  background-color: #e6f7ff;
  color: #1890ff;
}

.item-index {
  width: 24px;
  text-align: center;
  font-size: 12px;
  color: #999;
}

.item-info {
  flex: 1;
}

.item-title {
  font-size: 14px;
  margin-bottom: 2px;
}

.item-artist {
  font-size: 12px;
  color: #666;
}

.item-duration {
  font-size: 12px;
  color: #999;
}

/* 隐藏音频元素 */
audio {
  display: none;
}
</style>
