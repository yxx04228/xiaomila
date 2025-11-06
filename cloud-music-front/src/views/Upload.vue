<template>
  <div class="upload-page">
    <div class="upload-container">
      <h2>上传音乐</h2>
      <el-upload
        class="upload-demo"
        drag
        action="/api/music/upload"
        multiple
        :before-upload="beforeUpload"
      >
        <el-icon class="el-icon--upload"><upload-filled /></el-icon>
        <div class="el-upload__text">将音乐文件拖到此处，或<em>点击上传</em></div>
        <template #tip>
          <div class="el-upload__tip">支持 mp3、wav、flac等格式文件，且不超过 100MB</div>
        </template>
      </el-upload>
    </div>
  </div>
</template>

<script setup lang="ts">
import { UploadFilled } from '@element-plus/icons-vue'

const beforeUpload = (file: File) => {
  const isAudio = file.type.includes('audio/')
  const isLt100M = file.size / 1024 / 1024 < 100

  if (!isAudio) {
    ElMessage.error('只能上传音频文件!')
    return false
  }
  if (!isLt100M) {
    ElMessage.error('音频文件大小不能超过 100MB!')
    return false
  }
  return true
}
</script>

<style scoped>
.upload-page {
  padding: 20px;
}

.upload-container {
  background: white;
  padding: 40px;
  border-radius: 8px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
}

.upload-container h2 {
  margin-bottom: 30px;
  color: #333;
}
</style>
