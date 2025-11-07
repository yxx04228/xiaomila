<template>
  <div class="login-page">
    <div class="login-container">
      <div class="login-header">
        <div class="logo">🎵 云音乐</div>
        <h2>欢迎回来</h2>
        <p class="slogan">登录账号，发现更多好音乐</p>
      </div>
      <el-form
        :model="form"
        :rules="rules"
        ref="loginFormRef"
        label-width="0px"
        @keyup.enter="handleLogin"
        class="login-form"
      >
        <el-form-item prop="username">
          <el-input
            v-model="form.username"
            placeholder="请输入用户名"
            clearable
            class="login-input"
          >
            <template #prefix>
              <el-icon><User /></el-icon>
            </template>
          </el-input>
        </el-form-item>
        <el-form-item prop="password">
          <el-input
            v-model="form.password"
            type="password"
            placeholder="请输入密码"
            show-password
            clearable
            class="login-input"
          >
            <template #prefix>
              <el-icon><Lock /></el-icon>
            </template>
          </el-input>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleLogin" :loading="loading" class="login-btn">
            {{ loading ? '登录中...' : '登录' }}
          </el-button>
        </el-form-item>
        <el-form-item>
          <div class="login-footer">
            <span class="have-account">还没有账号？</span>
            <el-button link type="primary" @click="$router.push('/register')" class="register-link">
              去注册
            </el-button>
          </div>
        </el-form-item>
      </el-form>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage, type FormInstance, type FormRules } from 'element-plus'
import { User, Lock } from '@element-plus/icons-vue'
import { useUserStore } from '@/stores/user'

const router = useRouter()
const route = useRoute()
const loginFormRef = ref<FormInstance>()
const loading = ref(false)
const userStore = useUserStore()

// 表单数据
const form = reactive({
  username: '',
  password: '',
})

// 表单验证规则
const rules = reactive<FormRules>({
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 3, max: 20, message: '用户名长度在 3 到 20 个字符', trigger: 'blur' },
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, max: 20, message: '密码长度在 6 到 20 个字符', trigger: 'blur' },
  ],
})

// 登录处理
const handleLogin = async () => {
  if (!loginFormRef.value) return

  const valid = await loginFormRef.value.validate().catch(() => false)
  if (!valid) return

  loading.value = true

  try {
    const response = await userStore.userLogin({
      username: form.username,
      password: form.password,
    })

    if (response.success) {
      ElMessage.success(response.message || '登录成功')

      // 登录成功后跳转逻辑
      const redirect = route.query.redirect as string
      if (redirect) {
        // 跳转到之前访问的页面
        router.push(redirect)
      } else {
        // 跳转到首页
        router.push('/')
      }
    } else {
      ElMessage.error(response.message || '登录失败')
    }
  } catch (error: any) {
    console.error('登录失败:', error)
    // 错误信息已经在 userLogin 方法中显示，这里可以不用重复显示
  } finally {
    loading.value = false
  }
}

// 检查登录状态和 Token 过期重定向
const checkLoginStatus = () => {
  // 如果已登录，直接跳转到首页或目标页面
  if (userStore.isLoggedIn) {
    const redirect = route.query.redirect as string
    if (redirect) {
      router.push(redirect)
    } else {
      router.push('/')
    }
    return
  }

  // 检查是否有 Token 过期的重定向
  const tokenExpired = route.query.tokenExpired
  if (tokenExpired) {
    ElMessage.warning('登录状态已过期，请重新登录')

    // 清除可能的过期 token
    userStore.clearUserInfo()
  }

  // 检查是否有未登录访问的重定向
  const unauthorized = route.query.unauthorized
  if (unauthorized) {
    ElMessage.warning('请先登录以访问该页面')
  }
}

// 页面加载时检查登录状态
onMounted(() => {
  checkLoginStatus()
})

// 监听路由变化，处理登录状态
import { watch } from 'vue'
watch(
  () => route.query,
  (newQuery) => {
    if (newQuery.tokenExpired || newQuery.unauthorized) {
      checkLoginStatus()
    }
  }
)
</script>

<style scoped>
.login-page {
  width: 100%;
  height: 100%;
  display: flex;
  flex-direction: column;
  gap: 16px;
  min-height: 0;
}

/* 登录容器 - 简约卡片风格，降低阴影强度 */
.login-container {
  background: #ffffff;
  padding: 48px 36px;
  border-radius: 16px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.08);
  width: 100%;
  max-width: 400px;
  box-sizing: border-box;
  margin: 10px auto;
}

/* 登录头部 - 增加Logo和标语，提升品牌感 */
.login-header {
  text-align: center;
  margin-bottom: 36px;
}

.logo {
  font-size: 36px;
  margin-bottom: 12px;
  color: #6c7ae0;
}

.login-header h2 {
  color: #333333;
  font-size: 22px;
  font-weight: 600;
  margin-bottom: 8px;
}

.login-header .slogan {
  color: #999999;
  font-size: 14px;
  line-height: 1.5;
}

/* 表单样式 - 简化布局 */
.login-form {
  width: 100%;
}

/* 输入框样式 - 统一风格，增加图标间距 */
.login-input {
  height: 44px;
  border-radius: 8px;
  border: 1px solid #f0f0f0;
  transition: all 0.2s ease;
}

:deep(.login-input .el-input__prefix) {
  color: #999999;
  margin-right: 8px;
  display: flex;
  align-items: center;
}

/* 登录按钮 - 贴合整体配色，优化样式 */
.login-btn {
  width: 100%;
  height: 44px;
  border-radius: 8px;
  font-size: 16px;
  font-weight: 500;
  background: linear-gradient(135deg, #6c7ae0 0%, #7b5ea7 100%);
  border: none;
  transition: all 0.2s ease;
}

:deep(.login-btn:hover) {
  background: linear-gradient(135deg, #5d6bc0 0%, #6b5299 100%);
  transform: translateY(-1px);
}

:deep(.login-btn:active) {
  transform: translateY(0);
}

:deep(.login-btn.el-button--loading) {
  background: linear-gradient(135deg, #6c7ae0 0%, #7b5ea7 100%);
}

/* 底部注册链接 - 优化颜色和交互 */
.login-footer {
  text-align: center;
  margin-top: 16px;
  font-size: 14px;
  color: #999999;
}

.have-account {
  margin-right: 4px;
}

.register-link {
  font-size: 14px;
  transition: color 0.2s ease;
}

:deep(.register-link:hover) {
  color: #5d6bc0;
}

/* 错误提示样式 - 优化位置和颜色 */
:deep(.el-form-item__error) {
  font-size: 12px;
  padding-top: 4px;
  color: #ff4d4f;
}

/* 响应式适配 */
@media (max-width: 375px) {
  .login-container {
    padding: 36px 24px;
  }

  .login-header h2 {
    font-size: 20px;
  }

  .login-input,
  .login-btn {
    height: 40px;
  }
}
</style>
