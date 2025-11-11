<template>
  <div class="music-player">
    <div v-if="currentMusic" class="player-container">
      <!-- 播放器内容 -->
      <div class="player-content">
        <!-- 歌曲信息 -->
        <div class="song-info">
          <div class="album-cover">
            <el-avatar
              :size="50"
              :src="getCoverUrl(currentMusic)"
              shape="square"
              @error="handleCoverError"
            >
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
              :icon="loopConfig.icon"
              text
              @click="toggleLoopMode"
              :type="loopConfig.color"
              :title="loopConfig.title"
              class="loop-button"
              :class="`loop-mode-${loopMode}`"
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
            <!-- 静音按钮 -->
            <el-button
              :icon="isMuted || volumeValue === 0 ? Mute : Microphone"
              text
              @click="toggleMute"
              :type="isMuted || volumeValue === 0 ? 'danger' : ''"
              :title="muteTitle"
            />
          </div>

          <!-- 进度条 -->
          <div class="progress-container" v-if="duration > 0">
            <span class="time-current">{{ formatTime(currentTime) }}</span>
            <el-slider
              v-model="sliderTime"
              :max="duration"
              :show-tooltip="false"
              @change="handleSliderChange"
              @input="handleSliderInput"
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
          <!-- 音量弹出框（修复显隐问题） -->
          <div class="volume-control-wrapper">
            <!-- 音量按钮 -->
            <el-button
              text
              :title="volumeTitle"
              class="volume-control-button"
              :type="isMuted || volumeValue === 0 ? 'danger' : ''"
              @click.stop.prevent="volumePopoverVisible = !volumePopoverVisible"
            >
              <component :is="volumeIcon" class="volume-icon" />
            </el-button>

            <!-- 原生弹出框（彻底避免组件冲突） -->
            <div class="native-volume-popover" v-show="volumePopoverVisible" @click.stop>
              <div class="volume-control">
                <el-slider
                  v-model="volumeValue"
                  vertical
                  height="120px"
                  :min="0"
                  :max="1"
                  :step="0.01"
                  :show-tooltip="false"
                  @input="handleVolumeInput"
                  class="volume-slider"
                />
                <div class="volume-percent">{{ Math.round(volumeValue * 100) }}%</div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
    <div v-else class="player-placeholder">
      <div class="elegant-placeholder">
        <div class="music-waves">
          <div class="wave"></div>
          <div class="wave"></div>
          <div class="wave"></div>
        </div>
        <div class="placeholder-info">
          <h3>等待播放</h3>
        </div>
      </div>
    </div>

    <!-- 音频元素始终存在 -->
    <audio
      ref="audioRef"
      @loadedmetadata="handleLoadedMetadata"
      @timeupdate="handleTimeUpdate"
      @ended="handleEnded"
      @error="handleError"
      @loadstart="handleLoadStart"
      @canplay="handleCanPlay"
      @play="handlePlay"
      @pause="handlePause"
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
import { ref, watch, onMounted, onUnmounted, nextTick, computed } from 'vue'
// 导入音量图标组件
import { VolumeMute, VolumeLow, VolumeMedium, VolumeHigh } from '@/components/icons/VolumeIcons'
import {
  Headset,
  ArrowLeft,
  ArrowRight,
  VideoPause,
  VideoPlay,
  Download,
  Loading,
  // 循环相关图标
  RefreshRight,
  Refresh,
  CircleClose,
  // 音量图标
  Microphone,
  Mute,
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
  musicList,
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
  getCoverUrl,
  handleRefresh,
} = musicStore

const audioRef = ref<HTMLAudioElement>()
const showErrorDialog = ref(false)
const initializationComplete = ref(false)
// 本地音量值（用于滑块双向绑定，避免直接修改store值的频繁触发）
const volumeValue = ref(volume.value)
const volumePopoverVisible = ref(false)
// 独立的进度条值，避免与音频当前时间直接绑定
const sliderTime = ref(0)
// 防止进度条事件重复触发
const isSeeking = ref(false)
// 防止单曲循环模式下的重复触发
const isHandlingEnded = ref(false)

// 获取循环模式图标和文本
const loopConfig = computed(() => {
  switch (loopMode.value) {
    case 'one':
      return {
        icon: RefreshRight, // 单曲循环图标
        title: '单曲循环',
        color: 'success',
      }
    case 'all':
      return {
        icon: Refresh, // 列表循环图标
        title: '列表循环',
        color: 'primary',
      }
    default:
      return {
        icon: CircleClose, // 不循环图标
        title: '不循环',
        color: 'info',
      }
  }
})

// 获取音量图标
const volumeIcon = computed(() => {
  if (isMuted.value || volumeValue.value === 0) {
    return VolumeMute
  } else if (volumeValue.value < 0.3) {
    return VolumeLow
  } else if (volumeValue.value < 0.7) {
    return VolumeMedium
  } else {
    return VolumeHigh
  }
})

// 获取音量按钮标题
const volumeTitle = computed(() => {
  if (isMuted.value) {
    return '取消静音'
  }
  return `音量: ${Math.round(volume.value * 100)}%`
})

// 获取静音提示文本
const muteTitle = computed(() => {
  return isMuted.value ? '取消静音' : '静音'
})

// 封面加载错误处理
const handleCoverError = () => {
  console.log('封面加载失败，使用默认图标')
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

// 播放状态同步事件处理
const handlePlay = () => {
  console.log('音频开始播放（可能是外部触发的）')
  // 同步播放状态到 store
  isPlaying.value = true
  musicStore.isPlaying = true
}

const handlePause = () => {
  console.log('音频暂停（可能是外部触发的）')
  // 同步暂停状态到 store
  isPlaying.value = false
  musicStore.isPlaying = false
}

const handleTimeUpdate = () => {
  if (audioRef.value && !isSeeking.value) {
    currentTime.value = audioRef.value.currentTime
    // 只有当用户没有在拖动时才更新滑块值
    sliderTime.value = currentTime.value
  }
}

const handleEnded = async () => {
  console.log('音频播放结束，当前循环模式:', loopMode.value)

  if (isHandlingEnded.value) {
    console.log('正在处理结束事件，跳过重复处理')
    return
  }

  isHandlingEnded.value = true

  try {
    if (loopMode.value === 'one') {
      // 单曲循环模式：重置到开头并播放
      console.log('单曲循环模式，重新开始播放')
      if (audioRef.value) {
        // 先暂停，避免状态冲突
        audioRef.value.pause()
        // 重置播放位置
        audioRef.value.currentTime = 0
        currentTime.value = 0
        sliderTime.value = 0

        // 短暂延迟后重新播放，确保音频状态稳定
        setTimeout(() => {
          if (audioRef.value && currentMusic.value) {
            audioRef.value.play().catch((error) => {
              console.error('单曲循环重新播放失败:', error)
              // 如果播放失败，重置播放状态
              isPlaying.value = false
            })
          }
        }, 100)
      }
    } else if (loopMode.value === 'all') {
      // 列表循环模式：播放下一首
      console.log('列表循环模式，播放下一首')
      await playNext()
    } else {
      // 不循环模式：停止播放，重置状态
      console.log('不循环模式，停止播放')
      if (audioRef.value) {
        audioRef.value.pause()
        // 重置到开头
        audioRef.value.currentTime = 0
        currentTime.value = 0
        sliderTime.value = 0
      }
      isPlaying.value = false
    }
  } catch (error) {
    console.error('处理播放结束事件失败:', error)
    ElMessage.error('播放控制出错')
  } finally {
    // 短暂延迟后重置处理状态
    setTimeout(() => {
      isHandlingEnded.value = false
    }, 200)
  }
}

const handleError = (error: any) => {
  console.error('音频播放错误:', error)

  // 如果是单曲循环模式且正在处理结束事件，忽略某些错误
  if (loopMode.value === 'one' && isHandlingEnded.value) {
    console.log('单曲循环处理过程中的错误，忽略')
    return
  }

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

// 进度条输入事件（拖动过程中）
const handleSliderInput = (value: number) => {
  isSeeking.value = true
  sliderTime.value = value
}

// 进度条改变事件（拖动结束或点击）
const handleSliderChange = async (value: number) => {
  // 防止在单曲循环结束时设置时间
  if (isHandlingEnded.value) {
    console.log('正在处理播放结束，跳过进度设置')
    return
  }

  try {
    isSeeking.value = false

    // 检查是否接近结尾（避免在单曲循环模式下设置到最后一秒）
    const safeValue = value >= duration.value - 0.5 ? Math.max(0, duration.value - 1) : value

    await setCurrentTime(safeValue)
    // 确保滑块值与实际时间同步
    if (audioRef.value) {
      const actualTime = audioRef.value.currentTime
      sliderTime.value = actualTime
      currentTime.value = actualTime
    }
  } catch (error) {
    console.error('设置播放进度失败:', error)
    // 出错时恢复滑块到实际位置
    if (audioRef.value) {
      const actualTime = audioRef.value.currentTime
      sliderTime.value = actualTime
      currentTime.value = actualTime
    }
    isSeeking.value = false
  }
}

// 下载当前歌曲
const handleDownload = async () => {
  await downloadMusicFile(currentMusic.value)
  await handleRefresh() // 刷新当前列表
  const updatedMusic = musicStore.musicList.find((music) => music.id === currentMusic.value?.id)
  if (updatedMusic && currentMusic.value) {
    currentMusic.value.downloadCount = updatedMusic.downloadCount
  }
}

// 播放按钮点击处理
const handlePlayButtonClick = async () => {
  if (!initializationComplete.value) {
    ElMessage.warning('播放器正在初始化，请稍候...')
    return
  }

  // 防止在单曲循环处理过程中操作
  if (isHandlingEnded.value) {
    ElMessage.warning('播放器正在处理，请稍候...')
    return
  }

  try {
    await togglePlay()
  } catch (error: any) {
    ElMessage.error(`播放失败: ${error.message || '请稍后重试'}`)
  }
}

// 处理音量滑块输入
const handleVolumeInput = (newValue: number) => {
  volumeValue.value = newValue
  // 同步到store，并取消静音状态
  if (isMuted.value) {
    toggleMute() // 调节音量时自动取消静音
  }
  setVolume(newValue)
}

// 全局点击事件处理函数
const handleClickOutside = (e: MouseEvent) => {
  const volumeWrapper = document.querySelector('.volume-control-wrapper')
  if (volumeWrapper && !volumeWrapper.contains(e.target as Node)) {
    volumePopoverVisible.value = false
  }
}

// 初始化音频元素
onMounted(async () => {
  console.log('MusicPlayer 组件挂载')

  // 注册全局点击事件监听器
  document.addEventListener('click', handleClickOutside)

  // 等待下一个tick确保DOM渲染完成
  await nextTick()

  if (audioRef.value) {
    console.log('找到音频元素，开始初始化...')
    initAudioElement(audioRef.value)

    // 监听音频元素就绪状态
    const checkReadyState = () => {
      if (audioElementReady.value) {
        initializationComplete.value = true
        console.log('🎵 音频播放器初始化完成，已就绪！')
        ElMessage.success('播放器已就绪')
      } else {
        console.log('等待播放器就绪...')
        setTimeout(checkReadyState, 100)
      }
    }

    checkReadyState()
  } else {
    console.error('❌ 音频元素引用为空')
    ElMessage.error('播放器初始化失败')
  }
})

// 组件卸载时清理
onUnmounted(() => {
  // 移除全局事件监听器
  document.removeEventListener('click', handleClickOutside)

  if (audioRef.value) {
    audioRef.value.pause()
  }
  cleanupBlobUrl()
  volumePopoverVisible.value = false // 卸载时关闭弹出框
  console.log('MusicPlayer 组件卸载')
})

// 监听播放状态变化
watch(isPlaying, async (playing) => {
  if (!audioRef.value || !currentMusic.value) return

  // 防止在单曲循环处理过程中操作
  if (isHandlingEnded.value) {
    return
  }

  try {
    if (playing) {
      console.log('开始播放音频...')
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

// 监听store中的音量变化，同步到本地音量值
watch(volume, (newVal) => {
  volumeValue.value = newVal
})

// 监听静音状态变化，同步滑块样式
watch(isMuted, (muted) => {
  if (muted) {
    volumeValue.value = 0 // 静音时滑块显示0
  } else {
    volumeValue.value = volume.value == 0 ? 0.3 : volume.value // 取消静音时恢复原音量
  }
})

// 监听当前时间变化，同步到滑块（确保在非拖动状态下）
watch(currentTime, (newTime) => {
  if (!isSeeking.value && !isHandlingEnded.value) {
    sliderTime.value = newTime
  }
})
</script>

<style scoped>
.music-player {
  width: 100%;
  background: linear-gradient(135deg, #f5f7fa 0%, #e4eaf5 100%);
  color: #333;
  box-sizing: border-box;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  text-align: center;
  padding: 0 38px !important;
}

.player-placeholder {
  text-align: center;
  display: flex;
  height: 25%;
  align-items: center;
  justify-content: center;
}
.elegant-placeholder {
  height: 100%;
  text-align: center;
}
.music-waves {
  display: flex;
  justify-content: center;
  align-items: flex-end;
  gap: 4px;
  height: 30px;
}
.wave {
  width: 5px;
  background: #4299e1;
  border-radius: 2px;
  animation: wave 1.2s ease-in-out infinite;
}
.wave:nth-child(1) {
  height: 15px;
  animation-delay: 0s;
}
.wave:nth-child(2) {
  height: 25px;
  animation-delay: 0.3s;
}
.wave:nth-child(3) {
  height: 20px;
  animation-delay: 0.4s;
}
@keyframes wave {
  0%,
  100% {
    transform: scaleY(1);
  }
  50% {
    transform: scaleY(0.5);
  }
}
.placeholder-info h3 {
  font-size: 15px;
  font-weight: 600;
  color: #2d3748;
}

.player-container {
  display: flex;
  align-items: center;
  height: 100%;
  min-height: 70px;
  width: 100%;
}

.player-content {
  display: flex;
  align-items: center;
  gap: 20px;
  height: 100%;
  min-height: 70px;
  width: 100%;
  margin: 17px auto 3px;
  padding: 0 20px;
  box-sizing: border-box;
}

/* 歌曲信息样式 */
.song-info {
  display: flex;
  align-items: center;
  flex: 0 0 auto;
  min-width: 200px;
  justify-content: flex-start;
}

.album-cover {
  border-radius: 8px;
  overflow: hidden;
  box-shadow: 0 2px 8px rgba(4, 4, 4, 0);
  flex-shrink: 0;
}

.song-details {
  flex: 1;
  overflow: hidden;
  text-align: left;
  margin-left: 12px;
  min-width: 120px;
}

.song-title {
  font-size: 16px;
  font-weight: 600;
  color: #2d3748;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  margin-bottom: 4px;
  text-align: left;
}

.song-artist {
  font-size: 14px;
  color: #718096;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  text-align: left;
}

.loading-status {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 12px;
  color: #4299e1;
  margin-top: 4px;
  justify-content: flex-start;
}

/* 播放控制样式 */
.playback-controls {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
  flex: 1;
  margin: 0 40px;
}

.control-buttons {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 20px;
  width: 100%;
}

.loop-button {
  position: relative;
}
.loop-button::after {
  content: '';
  position: absolute;
  bottom: 2px;
  left: 50%;
  transform: translateX(-50%);
  width: 4px;
  height: 4px;
  border-radius: 50%;
  transition: all 0.3s ease;
}
/* 不同循环模式的状态点颜色 */
.loop-mode-none::after {
  background-color: #909399;
}
.loop-mode-one::after {
  background-color: #10d361;
}
.loop-mode-all::after {
  background-color: #409eff;
}

.control-buttons .el-button--circle.el-button--primary {
  --el-button-size: 44px;
  --el-button-text-color: #fff;
  --el-button-bg-color: #4299e1;
  --el-button-hover-bg-color: #3182ce;
  box-shadow: 0 4px 12px rgba(66, 153, 225, 0.3);
}

.control-buttons .el-icon {
  font-size: 20px;
}

.control-buttons .el-button--text {
  padding: 8px;
}

/* 进度条样式 */
.progress-container {
  display: flex;
  align-items: center;
  gap: 12px;
  width: 100%;
  padding: 0 10px;
}

.time-current,
.time-total {
  font-size: 12px;
  color: #718096;
  width: 45px;
  text-align: center;
  flex-shrink: 0;
}

/* 进度条滑块样式 */
.progress-slider {
  flex: 1;
  cursor: pointer;
  position: relative;
  min-width: 300px;
}
:deep(.progress-slider) {
  --el-slider-rail-height: 3px;
  --el-slider-track-height: 3px;
  --el-slider-thumb-size: 10px;
}
:deep(.progress-slider .el-slider__runway) {
  height: 3px;
  margin: 15px 0;
  background-color: #e2e8f0;
  border-radius: 2px;
  cursor: pointer;
  position: relative;
}
:deep(.progress-slider .el-slider__runway::before) {
  content: '';
  position: absolute;
  top: -9px;
  left: 0;
  right: 0;
  bottom: -10px;
  z-index: 1;
  cursor: pointer;
}
:deep(.progress-slider .el-slider__bar) {
  height: 3px;
  background-color: #4299e1;
  border-radius: 2px;
}
:deep(.progress-slider .el-slider__button-wrapper) {
  width: 24px;
  height: 24px;
  top: -11px;
  transform: translateX(-50%);
  cursor: pointer;
  z-index: 10;
  display: flex;
  align-items: center;
  justify-content: center;
}
:deep(.progress-slider .el-slider__button) {
  width: 8px;
  height: 8px;
  border: 1px solid #fff;
  background-color: #4299e1;
  box-shadow: 0 1px 2px rgba(66, 153, 225, 0.4);
  transition: all 0.2s ease;
}
:deep(.progress-slider .el-slider__button-wrapper:hover .el-slider__button) {
  transform: scale(1.3);
  background-color: #3182ce;
}

/* 额外控制样式 */
.extra-controls {
  display: flex;
  align-items: center;
  justify-content: flex-end;
  gap: 12px;
  flex: 0 0 auto;
  min-width: 120px;
}

.extra-controls .el-button--text {
  --el-button-text-color: #4a5568;
  --el-button-hover-text-color: #4299e1;
  padding: 8px;
}

.extra-controls .el-icon {
  font-size: 18px;
}

/* 音量控制容器 */
.volume-control-wrapper {
  position: relative;
  display: inline-block;
}

/* 音量按钮 */
.volume-control-button {
  padding: 8px;
  border: none;
  background: transparent;
  cursor: pointer;
  border-radius: 4px;
  transition: background-color 0.3s ease;
}

.volume-control-button:hover {
  background-color: rgba(0, 0, 0, 0.04);
}

.volume-icon {
  font-size: 15px;
}

/* 原生弹出框 */
.native-volume-popover {
  position: absolute;
  bottom: 100%;
  left: 50%;
  transform: translateX(-50%);
  margin-bottom: 8px;
  background: #ffffff;
  border-radius: 8px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
  border: 1px solid #e4e7ed;
  padding: 16px 8px;
  z-index: 2000;
  min-width: 60px;
}

/* 音量控制区域 */
.volume-control {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 12px;
}

:deep(.volume-slider) {
  width: 3px !important;
}

:deep(.volume-slider .el-slider__runway) {
  width: 3px;
  background-color: #e4e7ed;
  border-radius: 3px;
  margin: 0 auto;
}

:deep(.volume-slider .el-slider__runway.vertical) {
  margin: 0 auto;
}

:deep(.volume-slider .el-slider__bar) {
  width: 3px;
  background-color: #409eff;
  border-radius: 2px;
  transition: background-color 0.3s ease;
}

:deep(.volume-slider .el-slider__bar:hover) {
  background-color: #3375b9;
}

:deep(.volume-slider .el-slider__button) {
  width: 10px;
  height: 10px;
  border: 2px solid #409eff;
  background-color: #ffffff;
  transition: all 0.3s ease;
  margin-left: -3px;
}

:deep(.volume-slider .el-slider__button:hover) {
  transform: scale(1.1);
  border-color: #3375b9;
}

:deep(.volume-slider .el-slider__button:active) {
  transform: scale(1.2);
}

/* 音量百分比显示 */
.volume-percent {
  font-size: 12px;
  color: #909399;
  font-weight: 500;
  min-width: 40px;
  text-align: center;
  padding: 2px 6px;
  background: #f5f7fa;
  border-radius: 4px;
}

/* 动画效果 */
.native-volume-popover {
  animation: slideDown 0.2s ease-out;
}

@keyframes slideDown {
  from {
    opacity: 0;
    transform: translateX(-50%) translateY(10px);
  }
  to {
    opacity: 1;
    transform: translateX(-50%) translateY(0);
  }
}

/* 响应式调整 */
@media (max-width: 1024px) {
  .player-content {
    gap: 16px;
    padding: 0 16px;
  }

  .song-info {
    min-width: 180px;
  }

  .playback-controls {
    margin: 0 30px;
  }

  .control-buttons {
    gap: 16px;
  }

  .progress-slider {
    min-width: 250px;
  }
}

@media (max-width: 768px) {
  .player-content {
    flex-wrap: wrap;
    justify-content: space-between;
    gap: 12px;
    padding: 0 12px;
  }

  .song-info {
    order: 1;
    flex: 1;
    min-width: auto;
  }

  .playback-controls {
    order: 3;
    flex: 1 0 100%;
    margin: 8px 0 0 0;
  }

  .extra-controls {
    order: 2;
    flex: 0 0 auto;
  }

  .progress-slider {
    min-width: auto;
  }

  .song-title {
    font-size: 14px;
  }

  .song-artist {
    font-size: 12px;
  }

  .control-buttons {
    gap: 12px;
  }

  .progress-container {
    gap: 8px;
  }
}

@media (max-width: 480px) {
  .player-content {
    gap: 8px;
    padding: 0 8px;
  }

  .song-info {
    flex-direction: column;
    align-items: flex-start;
    gap: 4px;
  }

  .album-cover {
    display: none;
  }

  .playback-controls {
    margin: 8px 0 0 0;
  }

  .progress-container {
    gap: 6px;
  }

  .time-current,
  .time-total {
    font-size: 11px;
    width: 36px;
  }

  .control-buttons .el-button--circle.el-button--primary {
    --el-button-size: 40px;
  }
}
</style>
