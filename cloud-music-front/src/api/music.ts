import request from '@/utils/request'
import type { MusicListResponse, MusicQueryParams, UpdateMusicParams } from '@/types/music'

export const musicApi = {
  // 音乐列表查询
  getMusicList(params: MusicQueryParams) {
    return request.get<MusicListResponse>('/music/getPageList', { params })
  },

  // 上传音乐
  uploadMusic(formData: FormData) {
    return request.post('/music/upload', formData, {
      headers: { 'Content-Type': 'multipart/form-data' },
    })
  },

  // 下载音乐
  downloadMusic(id: string) {
    return request.get(`/music/download`, {
      params: { id },
      responseType: 'blob', // 重要：指定响应类型为 blob
    })
  },

  // 播放音乐 - 返回音频blob（用于高级播放控制）
  playMusic(id: string) {
    return request.get(`/music/play/`, {
      params: { id },
      responseType: 'blob',
      headers: {
        Accept: 'audio/*', // 明确接受音频类型
      },
    })
  },

  // 更新音乐信息
  updateMusic(music: UpdateMusicParams) {
    return request.put('/music/update', music)
  },

  // 删除音乐
  deleteMusic(id: string) {
    return request.delete('/music/delete', {
      params: { id },
    })
  },
}
