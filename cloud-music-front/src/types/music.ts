export interface Music {
  id: string
  createBy?: string | null
  createTime?: string
  updateBy?: string | null
  updateTime?: string
  deleted?: number
  title: string
  singer: string
  album?: string | null
  duration: string
  filePath: string
  fileSize: string
  fileType: string
  playCount: number
  coverUrl?: string // 新增封面URL字段
}

export interface MusicListResponse {
  success: boolean
  message: string
  data: {
    size: number
    total: number
    current: number
    pages: number
    records: Music[]
    countId?: string | null
    maxLimit?: string | null
    optimizeCountSql?: boolean
    orders?: any[]
    searchCount?: boolean
  }
}

export interface MusicQueryParams {
  title?: string
  nCurrent: number
  nSize: number
}

// 播放器状态类型
export interface PlayerState {
  currentMusic: Music | null
  isPlaying: boolean
  currentTime: number
  duration: number
  volume: number
  playbackRate: number
  isMuted: boolean
  loopMode: 'none' | 'one' | 'all'
}
