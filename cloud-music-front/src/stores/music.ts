import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
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
  const currentBlobUrl = ref<string>('')
  const audioLoading = ref(false)
  const autoPlayEnabled = ref(false)
  const audioElementReady = ref(false)
  const pendingMusicLoad = ref<Music | null>(null) // 待加载的音乐

  // 明确的保存搜索条件
  const currentSearchParams = ref<MusicQueryParams>({
    title: '',
    singer: '',
    nCurrent: 1,
    nSize: 10,
  })

  // 封面URL缓存
  const coverUrlCache = ref<Map<string, string>>(new Map())

  const pagination = ref({
    current: 1,
    pageSize: 10,
    total: 0,
  })
  const loading = ref(false)

  // 获取封面URL的计算属性
  const getCoverUrl = computed(() => (music: Music) => {
    if (!music || !music.id) {
      return ''
    }

    // 检查缓存中是否已有封面URL
    const cachedUrl = coverUrlCache.value.get(music.id)
    if (cachedUrl) {
      return cachedUrl
    }

    // 生成封面URL
    const coverUrl = `/api/music/cover?id=${music.id}&t=${Date.now()}`
    coverUrlCache.value.set(music.id, coverUrl)
    return coverUrl
  })

  const fetchMusicList = async (params: MusicQueryParams) => {
    loading.value = true
    try {
      // 保存当前的搜索条件
      currentSearchParams.value = { ...params }

      const response = await musicApi.getMusicList(params)

      if (response.success) {
        musicList.value = response.data.records || []
        pagination.value.total = response.data.total || 0
        pagination.value.current = response.data.current || 1
        console.log('获取音乐列表成功:', musicList.value)

        // 如果启用了自动播放且有音乐数据，设置待加载的音乐（不立即加载）
        if (autoPlayEnabled.value && musicList.value.length > 0 && !currentMusic.value) {
          pendingMusicLoad.value = musicList.value[0]
          console.log('设置待加载音乐:', pendingMusicLoad.value.title)

          // 如果音频元素已经就绪，立即加载
          if (audioElementReady.value) {
            console.log('音频元素已就绪，立即加载待加载音乐')
            await loadMusic(pendingMusicLoad.value, false)
            pendingMusicLoad.value = null
          }
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

  // 初始化音频元素
  const initAudioElement = (audio: HTMLAudioElement) => {
    audioElement.value = audio
    console.log('🎵 初始化音频元素...')

    // 设置基本属性
    audio.volume = volume.value
    audio.muted = isMuted.value
    audio.playbackRate = playbackRate.value
    audio.loop = loopMode.value === 'one'

    // 添加事件监听器
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

    audio.addEventListener('canplaythrough', () => {
      console.log('音频可以完整播放了')
    })

    // 标记音频元素为就绪状态
    audioElementReady.value = true
    console.log('✅ 音频元素初始化完成')

    // 如果有待加载的音乐，现在加载
    if (pendingMusicLoad.value) {
      console.log('加载待播放的音乐:', pendingMusicLoad.value.title)
      loadMusic(pendingMusicLoad.value, false)
        .then(() => {
          console.log('待播放音乐加载完成')
          pendingMusicLoad.value = null
        })
        .catch((error) => {
          console.error('待播放音乐加载失败:', error)
          pendingMusicLoad.value = null
        })
    }
  }

  // 加载音乐
  const loadMusic = async (music: Music, autoPlay = false) => {
    try {
      audioLoading.value = true
      console.log('开始加载音乐:', music.title)

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
      if (!audioElement.value) {
        throw new Error('音频元素未初始化')
      }

      audioElement.value.src = blobUrl
      console.log('音频源设置完成')

      // 等待音频元素加载
      await new Promise((resolve, reject) => {
        const onCanPlay = () => {
          cleanup()
          resolve(true)
        }

        const onError = () => {
          cleanup()
          reject(new Error('音频加载失败'))
        }

        const cleanup = () => {
          audioElement.value?.removeEventListener('canplay', onCanPlay)
          audioElement.value?.removeEventListener('error', onError)
        }

        audioElement.value?.addEventListener('canplay', onCanPlay)
        audioElement.value?.addEventListener('error', onError)

        // 设置超时
        setTimeout(() => {
          cleanup()
          resolve(true) // 即使超时也继续
        }, 10000)
      })

      console.log('音乐加载完成:', music.title)

      // 如果要求自动播放，尝试播放
      if (autoPlay) {
        try {
          await audioElement.value!.play()
          isPlaying.value = true
          console.log('自动播放成功:', music.title)
        } catch (playError) {
          console.warn('自动播放被阻止:', playError)
          isPlaying.value = false
          // 不抛出错误，因为自动播放被阻止是正常现象
        }
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
  const playMusic = async (music: Music, retryCount = 0) => {
    const maxRetries = 3

    // 检查播放器是否就绪
    if (!audioElementReady.value) {
      if (retryCount < maxRetries) {
        console.warn(`播放器未就绪，第 ${retryCount + 1} 次重试...`)
        await new Promise((resolve) => setTimeout(resolve, 500))
        return playMusic(music, retryCount + 1)
      } else {
        throw new Error('播放器初始化超时，请刷新页面重试')
      }
    }

    // 如果已经是当前音乐且已加载，直接播放
    if (currentMusic.value?.id === music.id && currentBlobUrl.value) {
      // if (isPlaying.value) {
      //   pauseMusic()
      // } else {
      //   await resumeMusic()
      // }
      await resumeMusic()
    } else {
      // 新歌曲，加载并播放
      await loadMusic(music, true)
    }
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

    // 如果设置音量大于0，自动取消静音
    if (value > 0) {
      isMuted.value = false
    } else {
      isMuted.value = true
    }

    if (audioElement.value) {
      audioElement.value.volume = value
      audioElement.value.muted = isMuted.value
    }
  }

  // 切换静音
  const toggleMute = () => {
    isMuted.value = !isMuted.value
    if (audioElement.value) {
      if (audioElement.value.volume == 0) {
        audioElement.value.volume = 0.3
      }
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
        audioElement.value.play().catch((error) => {
          console.warn('循环播放失败:', error)
        })
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
  const playNext = async () => {
    if (!currentMusic.value || musicList.value.length === 0) return

    const currentIndex = musicList.value.findIndex((m) => m.id === currentMusic.value?.id)

    // 如果当前歌曲是列表中的最后一首
    if (currentIndex === musicList.value.length - 1) {
      // 检查是否还有下一页
      if (pagination.value.current * pagination.value.pageSize < pagination.value.total) {
        // 有下一页，加载下一页并播放第一首
        const nextPage = pagination.value.current + 1
        await fetchMusicList({
          title: currentSearchParams.value.title,
          singer: currentSearchParams.value.singer,
          nCurrent: nextPage,
          nSize: pagination.value.pageSize,
        })

        // 等待列表更新后播放第一首
        if (musicList.value.length > 0) {
          await playMusic(musicList.value[0])
        }
      } else {
        // 列表循环：回到第一页的第一首
        await fetchMusicList({
          title: currentSearchParams.value.title,
          singer: currentSearchParams.value.singer,
          nCurrent: 1,
          nSize: pagination.value.pageSize,
        })

        if (musicList.value.length > 0) {
          await playMusic(musicList.value[0])
        }
      }
    } else {
      // 播放当前页的下一首
      const nextIndex = currentIndex + 1
      await playMusic(musicList.value[nextIndex])
    }
  }

  // 播放上一首
  const playPrevious = async () => {
    if (!currentMusic.value || musicList.value.length === 0) return

    const currentIndex = musicList.value.findIndex((m) => m.id === currentMusic.value?.id)

    // 如果当前歌曲是列表中的第一首
    if (currentIndex === 0) {
      // 检查是否还有上一页
      if (pagination.value.current > 1) {
        // 有上一页，加载上一页并播放最后一首
        const prevPage = pagination.value.current - 1
        await fetchMusicList({
          title: currentSearchParams.value.title,
          singer: currentSearchParams.value.singer,
          nCurrent: prevPage,
          nSize: pagination.value.pageSize,
        })

        // 等待列表更新后播放最后一首
        if (musicList.value.length > 0) {
          const lastIndex = musicList.value.length - 1
          await playMusic(musicList.value[lastIndex])
        }
      } else {
        // 列表循环：跳到最后一页的最后一首
        const lastPage = Math.ceil(pagination.value.total / pagination.value.pageSize)
        await fetchMusicList({
          title: currentSearchParams.value.title,
          singer: currentSearchParams.value.singer,
          nCurrent: lastPage,
          nSize: pagination.value.pageSize,
        })

        if (musicList.value.length > 0) {
          const lastIndex = musicList.value.length - 1
          await playMusic(musicList.value[lastIndex])
        }
      }
    } else {
      // 播放当前页的上一首
      const prevIndex = currentIndex - 1
      await playMusic(musicList.value[prevIndex])
    }
  }

  // 刷新当前列表
  const handleRefresh = async () => {
    await fetchMusicList({
      title: currentSearchParams.value.title,
      singer: currentSearchParams.value.singer,
      nCurrent: pagination.value.current,
      nSize: pagination.value.pageSize,
    })
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

  // 清理封面缓存
  const cleanupCoverCache = () => {
    coverUrlCache.value.clear()
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

  // 删除音乐
  const deleteMusic = async (id: string) => {
    try {
      const response = await musicApi.deleteMusic(id)

      if (response.success) {
        // 从列表中移除
        const index = musicList.value.findIndex((m) => m.id === id)
        if (index !== -1) {
          musicList.value.splice(index, 1)
        }

        // 如果删除的是当前播放的歌曲，停止播放
        if (currentMusic.value?.id === id) {
          stopPlayback()
        }

        // 从封面缓存中移除
        coverUrlCache.value.delete(id)

        // 更新分页总数
        pagination.value.total = Math.max(0, pagination.value.total - 1)

        console.log('删除音乐成功:', id)
        return true
      } else {
        throw new Error(response.message || '删除失败')
      }
    } catch (error) {
      console.error('删除音乐失败:', error)
      throw error
    }
  }

  // 停止播放（新增方法）
  const stopPlayback = () => {
    if (audioElement.value) {
      audioElement.value.pause()
      audioElement.value.currentTime = 0
    }
    isPlaying.value = false
    currentTime.value = 0
    currentMusic.value = null

    // 清理blob URL
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
    pendingMusicLoad,

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
    cleanupCoverCache,
    enableAutoPlay,
    disableAutoPlay,
    loadMusic,
    deleteMusic,
    stopPlayback,
    getCoverUrl,
    handleRefresh,
  }
})
