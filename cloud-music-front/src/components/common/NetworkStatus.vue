<template>
  <div v-if="showError" class="network-error">
    <el-alert
      :title="errorTitle"
      :description="errorMessage"
      type="error"
      show-icon
      :closable="true"
      @close="showError = false"
    />
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'

const showError = ref(false)
const errorTitle = ref('网络连接失败')
const errorMessage = ref('')

const checkNetwork = async () => {
  try {
    // 使用专门的健康检查接口，这个接口不需要认证
    const response = await fetch('/api/newWork/health', {
      method: 'GET',
      headers: {
        'Cache-Control': 'no-cache',
      },
    })

    if (!response.ok) {
      throw new Error(`服务异常，状态码: ${response.status}`)
    }

    const data = await response.json()
    if (data.success !== true && data.data?.status !== 'ok') {
      throw new Error('服务响应异常')
    }

    showError.value = false
  } catch (error: any) {
    showError.value = true
    errorTitle.value = '服务连接失败'
    errorMessage.value = `无法连接到音乐服务。请检查：
1. 后端服务是否正常运行
2. 网络连接是否正常
3. 如问题持续，请联系管理员`

    console.error('服务健康检查失败:', error)
  }
}

// 监听在线/离线事件
const handleOnline = () => {
  console.log('网络已恢复，重新检查服务状态')
  showError.value = false
  checkNetwork()
}

const handleOffline = () => {
  console.log('网络已断开')
  showError.value = true
  errorTitle.value = '网络连接已断开'
  errorMessage.value = '请检查您的网络连接，连接恢复后页面将自动重试。'
}

onMounted(() => {
  checkNetwork()

  // 添加网络状态监听
  window.addEventListener('online', handleOnline)
  window.addEventListener('offline', handleOffline)
})

// 可选：定期检查网络状态
setInterval(() => {
  if (navigator.onLine && !showError.value) {
    checkNetwork()
  }
}, 600000) // 每十分钟检查一次
</script>

<style scoped>
.network-error {
  position: fixed;
  top: 70px;
  left: 50%;
  transform: translateX(-50%);
  z-index: 9999;
  width: 90%;
  max-width: 600px;
  animation: slideDown 0.3s ease-out;
}

@keyframes slideDown {
  from {
    transform: translateX(-50%) translateY(-100%);
    opacity: 0;
  }
  to {
    transform: translateX(-50%) translateY(0);
    opacity: 1;
  }
}
</style>
