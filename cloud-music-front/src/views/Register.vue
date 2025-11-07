<template>
  <div class="register-page">
    <div class="register-container">
      <div class="register-header">
        <div class="logo">🎵 云音乐</div>
        <h2>创建账号</h2>
        <p class="slogan">加入我们，开启音乐之旅</p>
      </div>
      <el-form
        :model="form"
        :rules="rules"
        ref="registerFormRef"
        label-width="0px"
        @keyup.enter="handleRegister"
        class="register-form"
      >
        <el-form-item prop="username">
          <el-input
            v-model="form.username"
            placeholder="请输入用户名"
            clearable
            class="register-input"
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
            placeholder="请输入密码（6-20位）"
            show-password
            clearable
            class="register-input"
          >
            <template #prefix>
              <el-icon><Lock /></el-icon>
            </template>
          </el-input>
        </el-form-item>
        <el-form-item prop="nickname">
          <el-input
            v-model="form.nickname"
            placeholder="请输入昵称"
            clearable
            class="register-input"
          >
            <template #prefix>
              <el-icon><UserFilled /></el-icon>
            </template>
          </el-input>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleRegister" :loading="loading" class="register-btn">
            {{ loading ? '注册中...' : '注册' }}
          </el-button>
        </el-form-item>
        <el-form-item>
          <div class="register-footer">
            <span class="have-account">已有账号？</span>
            <el-button link type="primary" @click="$router.push('/login')" class="login-link">
              立即登录
            </el-button>
          </div>
        </el-form-item>
      </el-form>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, type FormInstance, type FormRules } from 'element-plus'
import { User, Lock, UserFilled } from '@element-plus/icons-vue'
import { useUserStore } from '@/stores/user'

const router = useRouter()
const registerFormRef = ref<FormInstance>()
const loading = ref(false)
const userStore = useUserStore()

// 表单数据
const form = reactive({
  username: '',
  password: '',
  nickname: '',
})

// 表单验证规则（与登录页保持一致的校验逻辑）
const rules = reactive<FormRules>({
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 3, max: 20, message: '用户名长度在 3 到 20 个字符', trigger: 'blur' },
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, max: 20, message: '密码长度在 6 到 20 个字符', trigger: 'blur' },
  ],
  nickname: [
    { required: true, message: '请输入昵称', trigger: 'blur' },
    { min: 2, max: 16, message: '昵称长度在 2 到 16 个字符', trigger: 'blur' },
  ],
})

// 注册处理
const handleRegister = async () => {
  if (!registerFormRef.value) return

  // 表单验证
  const valid = await registerFormRef.value.validate().catch(() => false)
  if (!valid) return

  loading.value = true

  try {
    // 后续替换为真实注册接口逻辑
    // const response = await userStore.userRegister(form)
    // if (response.success) {
    //   ElMessage.success(response.message || '注册成功')
    //   router.push('/login')
    // } else {
    //   ElMessage.error(response.message || '注册失败')
    // }

    // 临时模拟注册成功
    ElMessage.success('注册成功，即将跳转到登录页')
    setTimeout(() => {
      router.push('/login')
    }, 1500)
  } catch (error: any) {
    console.error('注册失败:', error)
    ElMessage.error(error.message || '注册失败，请稍后重试')
  } finally {
    loading.value = false
  }
}

// 检查是否已登录，如果已登录则跳转到首页
const checkLoginStatus = () => {
  if (userStore.isLoggedIn) {
    router.push('/')
  }
}

checkLoginStatus()
</script>

<style scoped>
/* 页面背景 - 与登录页完全一致的渐变 */
.register-page {
  width: 100%;
  height: 100%;
  display: flex;
  flex-direction: column;
  gap: 16px;
  min-height: 0;
}

/* 注册容器 - 与登录页统一的卡片样式 */
.register-container {
  background: #ffffff;
  padding: 48px 36px;
  border-radius: 16px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.08);
  width: 100%;
  max-width: 400px;
  box-sizing: border-box;
  margin: 10px auto;
}

/* 注册头部 - 与登录页结构一致 */
.register-header {
  text-align: center;
  margin-bottom: 36px;
}

.logo {
  font-size: 36px;
  margin-bottom: 12px;
  color: #6c7ae0;
}

.register-header h2 {
  color: #333333;
  font-size: 22px;
  font-weight: 600;
  margin-bottom: 8px;
}

.register-header .slogan {
  color: #999999;
  font-size: 14px;
  line-height: 1.5;
}

/* 表单样式 - 统一布局 */
.register-form {
  width: 100%;
}

/* 输入框样式 - 与登录页完全一致 */
.register-input {
  height: 44px;
  border-radius: 8px;
  border: 1px solid #f0f0f0;
  transition: all 0.2s ease;
}

:deep(.register-input .el-input__prefix) {
  color: #999999;
  margin-right: 8px;
  display: flex;
  align-items: center;
}

/* 注册按钮 - 与登录页按钮风格统一 */
.register-btn {
  width: 100%;
  height: 44px;
  border-radius: 8px;
  font-size: 16px;
  font-weight: 500;
  background: linear-gradient(135deg, #6c7ae0 0%, #7b5ea7 100%);
  border: none;
  transition: all 0.2s ease;
}

:deep(.register-btn:hover) {
  background: linear-gradient(135deg, #5d6bc0 0%, #6b5299 100%);
  transform: translateY(-1px);
}

:deep(.register-btn:active) {
  transform: translateY(0);
}

:deep(.register-btn.el-button--loading) {
  background: linear-gradient(135deg, #6c7ae0 0%, #7b5ea7 100%);
}

/* 底部登录链接 - 统一风格 */
.register-footer {
  text-align: center;
  margin-top: 16px;
  font-size: 14px;
  color: #999999;
}

.have-account {
  margin-right: 4px;
}

.login-link {
  font-size: 14px;
  transition: color 0.2s ease;
  padding: 0;
}

:deep(.login-link:hover) {
  color: #5d6bc0;
  background: transparent;
}

/* 错误提示样式 - 与登录页一致 */
:deep(.el-form-item__error) {
  font-size: 12px;
  padding-top: 4px;
  color: #ff4d4f;
}

/* 响应式适配 - 统一断点和样式 */
@media (max-width: 375px) {
  .register-container {
    padding: 36px 24px;
  }

  .register-header h2 {
    font-size: 20px;
  }

  .register-input,
  .register-btn {
    height: 40px;
  }
}
</style>
