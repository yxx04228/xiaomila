<template>
  <AppLayout />
  <NetworkStatus />
</template>

<script setup lang="ts">
import AppLayout from '@/components/layout/AppLayout.vue'
import NetworkStatus from '@/components/common/NetworkStatus.vue'
</script>

<style>
* {
  margin: 0;
  padding: 0;
  box-sizing: border-box;
}

body {
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif;
  background-color: #f5f7fa;
  color: #333;
}

#app {
  min-height: 100vh;
}
</style>
