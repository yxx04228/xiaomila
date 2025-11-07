// router/index.js
import { createRouter, createWebHistory } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { ElMessage } from 'element-plus'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/',
      name: 'home',
      component: () => import('@/views/Home.vue'),
      meta: {
        requiresAuth: true, // 需要登录
      },
    },
    {
      path: '/login',
      name: 'login',
      component: () => import('@/views/Login.vue'),
      meta: {
        requiresAuth: false, // 不需要登录
      },
    },
    {
      path: '/register',
      name: 'register',
      component: () => import('@/views/Register.vue'),
      meta: {
        requiresAuth: false, // 不需要登录
      },
    },
    // 可以添加更多需要权限的路由
    {
      path: '/profile',
      name: 'profile',
      component: () => import('@/views/Home.vue'),
      meta: {
        requiresAuth: true,
      },
    },
  ],
})

// 路由守卫
router.beforeEach((to, from, next) => {
  const userStore = useUserStore()

  // 检查路由是否需要认证
  if (to.meta.requiresAuth) {
    if (userStore.isLoggedIn) {
      // 已登录，允许访问
      next()
    } else {
      // 未登录，跳转到登录页
      ElMessage.warning('请先登录')
      next('/login')
    }
  } else {
    // 不需要认证的路由
    if (to.path === '/login' && userStore.isLoggedIn) {
      // 已登录用户访问登录页，跳转到首页
      next('/')
    } else {
      next()
    }
  }
})

// 全局错误处理
router.onError((error) => {
  console.error('路由错误:', error)
  ElMessage.error('页面加载失败')
})

export default router
