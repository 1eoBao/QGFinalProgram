<template>
  <div class="login-container">
    <div class="login-card">
      <h2 class="title">智能笔记</h2>
      <p class="subtitle">登录你的账号</p>
      <el-form ref="formRef" :model="form" :rules="rules" label-position="top" @submit.prevent="handleLogin">
        <el-form-item label="邮箱/手机号" prop="account">
          <el-input v-model="form.account" placeholder="请输入邮箱或手机号" prefix-icon="User" />
        </el-form-item>
        <el-form-item label="密码" prop="password">
          <el-input v-model="form.password" type="password" placeholder="请输入密码" prefix-icon="Lock" show-password />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :loading="loading" style="width: 100%" native-type="submit">登 录</el-button>
        </el-form-item>
      </el-form>
      <div class="footer-link">
        还没有账号？<router-link to="/register">立即注册</router-link>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { login } from '../api/user'
import { useUserStore } from '../stores/user'

const router = useRouter()
const userStore = useUserStore()
const formRef = ref(null)
const loading = ref(false)

const form = reactive({ account: '', password: '' })

const rules = {
  account: [{ required: true, message: '请输入邮箱或手机号', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }]
}

async function handleLogin() {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return
  loading.value = true
  try {
    const res = await login(form)
    if (res.code === 200) {
      userStore.setToken(res.data)
      ElMessage.success('登录成功')
      router.push('/')
    } else {
      ElMessage.error(res.msg || '登录失败')
    }
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.login-container {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

.login-card {
  width: 400px;
  padding: 40px;
  background: #fff;
  border-radius: 12px;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.15);
}

.title {
  text-align: center;
  font-size: 28px;
  color: #303133;
  margin-bottom: 4px;
}

.subtitle {
  text-align: center;
  color: #909399;
  margin-bottom: 32px;
  font-size: 14px;
}

.footer-link {
  text-align: center;
  font-size: 14px;
  color: #909399;
}

.footer-link a {
  color: #667eea;
  font-weight: 500;
}
</style>
