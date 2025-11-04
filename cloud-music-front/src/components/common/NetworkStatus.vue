<template>
  <div v-if="showError" class="network-error">
    <el-alert
      title="网络连接失败"
      :description="errorMessage"
      type="error"
      show-icon
      :closable="false"
    />
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'

const showError = ref(false)
const errorMessage = ref('')

const checkNetwork = async () => {
  try {
    const response = await fetch('http://192.168.0.125:8081/music/getPageList?nCurrent=1&nSize=1')
    if (!response.ok) {
      throw new Error(`HTTP error! status: ${response.status}`)
    }
    showError.value = false
  } catch (error) {
    showError.value = true
    errorMessage.value = `无法连接到后端服务器 (http://192.168.0.125:8081)。请检查：
    1. 后端服务是否启动
    2. 网络连接是否正常
    3. 防火墙设置`
    console.error('网络检查失败:', error)
  }
}

onMounted(() => {
  checkNetwork()
})
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
}
</style>
