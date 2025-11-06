import { musicApi } from '@/api/music'
import { ElMessage, ElLoading } from 'element-plus'

/**
 * 下载音乐文件
 * @param music 音乐信息
 * @param onProgress 进度回调（可选）
 */
export const downloadMusicFile = async (music: any, onProgress?: (progress: number) => void) => {
  let downloadSuccess = false

  try {
    // 显示加载提示
    const loadingInstance = ElLoading.service({
      lock: true,
      text: `正在下载 ${music.title}...`,
      background: 'rgba(0, 0, 0, 0.7)',
    })

    // 调用下载接口
    const response = await musicApi.downloadMusic(music.id)

    // 创建 Blob 对象
    const blob = new Blob([response], {
      type: getMimeType(music.fileType),
    })

    // 创建下载链接
    const url = window.URL.createObjectURL(blob)
    const link = document.createElement('a')
    link.href = url

    // 设置下载文件名
    const fileName = getDownloadFileName(music)
    link.download = fileName

    // 触发下载
    document.body.appendChild(link)
    link.click()

    // 清理
    document.body.removeChild(link)
    window.URL.revokeObjectURL(url)

    // 标记下载成功
    downloadSuccess = true

    loadingInstance.close()

    ElMessage.success(`"${music.title}" 下载成功`)

    // 下载成功后，增加下载次数
    if (downloadSuccess) {
      try {
        const newPlayCount = (music.playCount || 0) + 1
        await musicApi.updateMusic({
          id: music.id,
          playCount: newPlayCount,
        })
        console.log(`"${music.title}" 下载次数已更新: ${newPlayCount}`)
      } catch (updateError) {
        console.warn('更新下载次数失败:', updateError)
        // 不阻塞主流程，只是记录警告
      }
    }

    return true
  } catch (error) {
    console.error('下载失败:', error)
    ElMessage.error(`下载失败: ${error.message || '请稍后重试'}`)
    return false
  }
}

/**
 * 根据文件类型获取 MIME 类型
 */
const getMimeType = (fileType: string): string => {
  const mimeTypes: { [key: string]: string } = {
    mp3: 'audio/mpeg',
    wav: 'audio/wav',
    flac: 'audio/flac',
    ogg: 'audio/ogg',
    m4a: 'audio/mp4',
  }
  return mimeTypes[fileType.toLowerCase()] || 'application/octet-stream'
}

/**
 * 生成下载文件名
 */
const getDownloadFileName = (music: any): string => {
  const safeTitle = music.title.replace(/[<>:"/\\|?*]/g, '')
  const safeSinger = music.singer.replace(/[<>:"/\\|?*]/g, '')
  return `${safeSinger}-${safeTitle}.${music.fileType}`
}
