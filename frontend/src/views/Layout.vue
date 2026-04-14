<template>
  <el-container class="layout-container">
    <el-aside width="220px" class="aside">
      <div class="logo" @click="$router.push('/')">
        <el-icon size="24"><Notebook /></el-icon>
        <span>智能笔记</span>
      </div>
      <el-menu :default-active="route.path" router class="aside-menu">
        <el-menu-item index="/notes">
          <el-icon><Document /></el-icon>
          <span>我的笔记</span>
        </el-menu-item>
        <el-menu-item index="/shared">
          <el-icon><Share /></el-icon>
          <span>共享笔记</span>
        </el-menu-item>
        <el-menu-item index="/history">
          <el-icon><Clock /></el-icon>
          <span>浏览历史</span>
        </el-menu-item>
        <el-menu-item index="/friends">
          <el-icon><User /></el-icon>
          <span>好友管理</span>
        </el-menu-item>
        <el-menu-item index="/profile">
          <el-icon><Setting /></el-icon>
          <span>个人设置</span>
        </el-menu-item>
      </el-menu>
    </el-aside>
    <el-container>
      <el-header class="header">
        <div class="header-left">
          <el-button type="primary" @click="$router.push('/note/create')">
            <el-icon><Plus /></el-icon>新建笔记
          </el-button>
        </div>
        <div class="header-right">
          <el-dropdown @command="handleCommand">
            <span class="user-info">
              <el-avatar :size="32" :src="userStore.userInfo?.avatar || undefined">
                {{ userStore.userInfo?.nickname?.charAt(0) || 'U' }}
              </el-avatar>
              <span class="username">{{ userStore.userInfo?.nickname || '用户' }}</span>
            </span>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="profile">个人设置</el-dropdown-item>
                <el-dropdown-item command="logout" divided>退出登录</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </el-header>
      <el-main class="main">
        <router-view />
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup>
import { onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useUserStore } from '../stores/user'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

onMounted(() => {
  userStore.fetchUserInfo()
})

function handleCommand(cmd) {
  if (cmd === 'logout') {
    userStore.clearToken()
    router.push('/login')
  } else if (cmd === 'profile') {
    router.push('/profile')
  }
}
</script>

<style scoped>
.layout-container {
  height: 100vh;
}

.aside {
  background: #fff;
  border-right: 1px solid #e4e7ed;
  overflow-y: auto;
}

.logo {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 20px 24px;
  font-size: 18px;
  font-weight: 600;
  color: #667eea;
  cursor: pointer;
}

.aside-menu {
  border-right: none;
}

.header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  background: #fff;
  border-bottom: 1px solid #e4e7ed;
  padding: 0 24px;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
}

.username {
  font-size: 14px;
  color: #303133;
}

.main {
  background: #f5f7fa;
  overflow-y: auto;
}
</style>
