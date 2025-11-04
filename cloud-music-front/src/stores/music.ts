import { defineStore } from 'pinia'
import { ref } from 'vue'
import type { Music, MusicQueryParams } from '@/types/music'
import { musicApi } from '@/api/music'

export const useMusicStore = defineStore('music', () => {
  const musicList = ref<Music[]>([])
  const currentMusic = ref<Music | null>(null)
  const isPlaying = ref(false)
  const currentTime = ref(0)
  const duration = ref(0)
  const volume = ref(0.7)
  const playbackRate = ref(1.0)
  const isMuted = ref(false)
  const loopMode = ref<'none' | 'one' | 'all'>('none')
  const playHistory = ref<Music[]>([])
  const audioElement = ref<HTMLAudioElement | null>(null)
  const currentBlobUrl = ref<string>('') // 新增：当前blob URL
  const audioLoading = ref(false) // 新增：音频加载状态
  const autoPlayEnabled = ref(false) // 新增：自动播放标志
  const audioElementReady = ref(false) // 新增：音频元素就绪状态

  const pagination = ref({
    current: 1,
    pageSize: 10,
    total: 0,
  })
  const loading = ref(false)

  const fetchMusicList = async (params: MusicQueryParams) => {
    loading.value = true
    try {
      const response = await musicApi.getMusicList(params)

      if (response.success) {
        musicList.value = response.data.records || []
        pagination.value.total = response.data.total || 0
        pagination.value.current = response.data.current || 1
        console.log('获取音乐列表成功:', musicList.value)

        // 如果启用了自动播放且有音乐数据，预加载第一首（但不立即播放）
        if (autoPlayEnabled.value && musicList.value.length > 0 && !currentMusic.value) {
          await preloadFirstSong()
        }
      } else {
        console.error('接口返回失败:', response.message)
        musicList.value = []
      }
    } catch (error) {
      console.error('获取音乐列表失败:', error)
      musicList.value = []
    } finally {
      loading.value = false
    }
  }

  // 预加载第一首歌曲（不播放）
  const preloadFirstSong = async () => {
    if (musicList.value.length === 0) {
      console.log('没有可播放的音乐')
      return
    }

    const firstSong = musicList.value[0]
    console.log('预加载第一首歌曲:', firstSong.title)

    try {
      await loadMusic(firstSong, false) // 只加载，不播放
      console.log('预加载成功')
    } catch (error) {
      console.error('预加载失败:', error)
    }
  }

  // 启用自动播放
  const enableAutoPlay = () => {
    autoPlayEnabled.value = true
    console.log('启用自动播放')
  }

  // 禁用自动播放
  const disableAutoPlay = () => {
    autoPlayEnabled.value = false
    console.log('禁用自动播放')
  }

  // 加载音乐（不自动播放）
  const loadMusic = async (music: Music, autoPlay = false) => {
    try {
      audioLoading.value = true

      // 释放之前的blob URL
      if (currentBlobUrl.value) {
        URL.revokeObjectURL(currentBlobUrl.value)
        currentBlobUrl.value = ''
      }

      // 获取音频blob
      const response = await musicApi.playMusic(music.id)
      const blobUrl = URL.createObjectURL(response)
      currentBlobUrl.value = blobUrl

      // 设置当前音乐
      currentMusic.value = music

      // 设置音频源
      if (audioElement.value) {
        audioElement.value.src = blobUrl

        // 如果要求自动播放且音频元素已就绪，尝试播放
        if (autoPlay && audioElementReady.value) {
          try {
            await audioElement.value.play()
            isPlaying.value = true
            console.log('自动播放成功:', music.title)
          } catch (playError) {
            console.warn('自动播放被阻止，需要用户交互:', playError)
            isPlaying.value = false
          }
        } else {
          // 只加载，不播放
          isPlaying.value = false
          console.log('音乐加载完成，等待播放:', music.title)
        }
      } else {
        console.warn('音频元素未初始化，无法设置音乐源')
        throw new Error('音频播放器未就绪')
      }

      // 添加到播放历史
      if (!playHistory.value.some((item) => item.id === music.id)) {
        playHistory.value.unshift(music)
        if (playHistory.value.length > 50) {
          playHistory.value = playHistory.value.slice(0, 50)
        }
      }
    } catch (error) {
      console.error('加载音乐失败:', error)
      isPlaying.value = false
      throw error
    } finally {
      audioLoading.value = false
    }
  }

  // 播放音乐
  const playMusic = async (music: Music) => {
    // 如果已经是当前音乐且已加载，直接播放
    if (currentMusic.value?.id === music.id && currentBlobUrl.value && audioElement.value) {
      if (isPlaying.value) {
        pauseMusic()
      } else {
        await resumeMusic()
      }
    } else {
      // 新歌曲，加载并播放
      await loadMusic(music, true)
    }
  }

  // 初始化音频元素
  const initAudioElement = (audio: HTMLAudioElement) => {
    audioElement.value = audio
    audioElementReady.value = true // 标记音频元素已就绪
    console.log('音频元素初始化完成')

    audio.addEventListener('loadedmetadata', () => {
      duration.value = audio.duration
      console.log('音频元数据加载完成，时长:', duration.value)
    })

    audio.addEventListener('timeupdate', () => {
      currentTime.value = audio.currentTime
    })

    audio.addEventListener('ended', () => {
      handlePlayEnd()
    })

    audio.addEventListener('error', (e) => {
      console.error('音频播放错误:', e)
      isPlaying.value = false
      audioLoading.value = false
    })

    audio.addEventListener('loadstart', () => {
      audioLoading.value = true
      console.log('开始加载音频...')
    })

    audio.addEventListener('canplay', () => {
      audioLoading.value = false
      console.log('音频可以播放了')
    })
  }

  // 暂停音乐
  const pauseMusic = () => {
    if (audioElement.value) {
      audioElement.value.pause()
      isPlaying.value = false
      console.log('音乐已暂停')
    }
  }

  // 恢复播放
  const resumeMusic = async () => {
    if (audioElement.value && currentBlobUrl.value) {
      try {
        await audioElement.value.play()
        isPlaying.value = true
        console.log('音乐恢复播放')
      } catch (error) {
        console.error('播放失败:', error)
        isPlaying.value = false
        throw error
      }
    } else {
      console.warn('音频元素或音乐源未就绪，无法播放')
      throw new Error('播放器未就绪')
    }
  }

  // 切换播放状态
  const togglePlay = async () => {
    if (!currentMusic.value || !audioElementReady.value) {
      console.warn('当前无音乐或播放器未就绪')
      return
    }

    if (isPlaying.value) {
      pauseMusic()
    } else {
      await resumeMusic()
    }
  }

  // 设置播放时间
  const setCurrentTime = (time: number) => {
    if (audioElement.value) {
      audioElement.value.currentTime = time
      currentTime.value = time
    }
  }

  // 设置音量
  const setVolume = (value: number) => {
    volume.value = value
    if (audioElement.value) {
      audioElement.value.volume = value
    }
  }

  // 切换静音
  const toggleMute = () => {
    isMuted.value = !isMuted.value
    if (audioElement.value) {
      audioElement.value.muted = isMuted.value
    }
  }

  // 设置播放速度
  const setPlaybackRate = (rate: number) => {
    playbackRate.value = rate
    if (audioElement.value) {
      audioElement.value.playbackRate = rate
    }
  }

  // 切换循环模式
  const toggleLoopMode = () => {
    const modes: ('none' | 'one' | 'all')[] = ['none', 'one', 'all']
    const currentIndex = modes.indexOf(loopMode.value)
    loopMode.value = modes[(currentIndex + 1) % modes.length]

    if (audioElement.value) {
      audioElement.value.loop = loopMode.value === 'one'
    }
  }

  // 播放结束处理
  const handlePlayEnd = () => {
    if (loopMode.value === 'one') {
      // 单曲循环，重新播放
      if (audioElement.value) {
        audioElement.value.currentTime = 0
        audioElement.value.play()
      }
    } else if (loopMode.value === 'all' && musicList.value.length > 0) {
      // 列表循环，播放下一首
      playNext()
    } else {
      // 不循环，停止播放
      isPlaying.value = false
      currentTime.value = 0
    }
  }

  // 播放下一首
  const playNext = () => {
    if (!currentMusic.value || musicList.value.length === 0) return

    const currentIndex = musicList.value.findIndex((m) => m.id === currentMusic.value?.id)
    const nextIndex = (currentIndex + 1) % musicList.value.length
    playMusic(musicList.value[nextIndex])
  }

  // 播放上一首
  const playPrevious = () => {
    if (!currentMusic.value || musicList.value.length === 0) return

    const currentIndex = musicList.value.findIndex((m) => m.id === currentMusic.value?.id)
    const prevIndex = currentIndex > 0 ? currentIndex - 1 : musicList.value.length - 1
    playMusic(musicList.value[prevIndex])
  }

  // 格式化时间（秒 -> 分:秒）
  const formatTime = (seconds: number): string => {
    const mins = Math.floor(seconds / 60)
    const secs = Math.floor(seconds % 60)
    return `${mins}:${secs.toString().padStart(2, '0')}`
  }

  // 清理blob URL
  const cleanupBlobUrl = () => {
    if (currentBlobUrl.value) {
      URL.revokeObjectURL(currentBlobUrl.value)
      currentBlobUrl.value = ''
    }
  }

  return {
    // 状态
    musicList,
    currentMusic,
    isPlaying,
    currentTime,
    duration,
    volume,
    playbackRate,
    isMuted,
    loopMode,
    playHistory,
    pagination,
    loading,
    audioLoading,
    currentBlobUrl,
    autoPlayEnabled,
    audioElementReady,

    // 方法
    fetchMusicList,
    initAudioElement,
    playMusic,
    pauseMusic,
    resumeMusic,
    togglePlay,
    setCurrentTime,
    setVolume,
    toggleMute,
    setPlaybackRate,
    toggleLoopMode,
    playNext,
    playPrevious,
    formatTime,
    cleanupBlobUrl,
    enableAutoPlay,
    disableAutoPlay,
    preloadFirstSong,
    loadMusic,
  }
})
