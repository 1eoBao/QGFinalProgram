<template>
  <div class="profile-page">
    <div class="profile-card">
      <h2 class="page-title">个人设置</h2>

      <el-tabs v-model="activeTab">
        <el-tab-pane label="基本信息" name="info">
          <el-form :model="infoForm" label-width="80px" style="max-width: 500px">
            <el-form-item label="用户名">
              <el-input :value="userStore.userInfo?.username" disabled />
            </el-form-item>
            <el-form-item label="邮箱">
              <el-input :value="userStore.userInfo?.email" disabled />
            </el-form-item>
            <el-form-item label="手机号">
              <el-input :value="userStore.userInfo?.phone" disabled />
            </el-form-item>
            <el-form-item label="昵称">
              <el-input v-model="infoForm.nickname" placeholder="请输入昵称" />
            </el-form-item>
            <el-form-item label="头像">
              <div class="avatar-upload">
                <el-upload
                  class="avatar-uploader"
                  :show-file-list="false"
                  :before-upload="beforeAvatarUpload"
                  :http-request="handleAvatarUpload"
                >
                  <img v-if="infoForm.avatar" :src="infoForm.avatar" class="avatar-preview" />
                  <el-icon v-else class="avatar-uploader-icon"><Plus /></el-icon>
                </el-upload>
                <div class="avatar-tip">支持 jpg、png、gif 格式，大小不超过 2MB</div>
              </div>
            </el-form-item>
            <el-form-item label="座右铭">
              <el-input v-model="infoForm.motto" type="textarea" :rows="2" placeholder="一句话介绍自己" />
            </el-form-item>
            <el-form-item label="注册时间">
              <span class="info-text">{{ formatTime(userStore.userInfo?.createTime) }}</span>
            </el-form-item>
            <el-form-item label="最后修改">
              <span class="info-text">{{ formatTime(userStore.userInfo?.updateTime) }}</span>
            </el-form-item>
            <el-form-item>
              <el-button type="primary" :loading="savingInfo" @click="handleUpdateInfo">保存修改</el-button>
            </el-form-item>
          </el-form>
        </el-tab-pane>

        <el-tab-pane label="修改密码" name="password">
          <el-form ref="pwdFormRef" :model="pwdForm" :rules="pwdRules" label-width="80px" style="max-width: 500px">
            <el-form-item label="旧密码" prop="oldPassword">
              <el-input v-model="pwdForm.oldPassword" type="password" show-password />
            </el-form-item>
            <el-form-item label="新密码" prop="newPassword">
              <el-input v-model="pwdForm.newPassword" type="password" show-password />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" :loading="savingPwd" @click="handleChangePwd">修改密码</el-button>
            </el-form-item>
          </el-form>
        </el-tab-pane>
      </el-tabs>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import { useUserStore } from '../stores/user'
import { updateUserInfo, changePassword, uploadAvatar } from '../api/user'

const userStore = useUserStore()
const activeTab = ref('info')
const savingInfo = ref(false)
const savingPwd = ref(false)
const pwdFormRef = ref(null)

const infoForm = reactive({
  nickname: '',
  avatar: '',
  motto: ''
})

const pwdForm = reactive({
  oldPassword: '',
  newPassword: ''
})

const pwdRules = {
  oldPassword: [{ required: true, message: '请输入旧密码', trigger: 'blur' }],
  newPassword: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    { min: 6, max: 20, message: '密码长度6-20位', trigger: 'blur' }
  ]
}

onMounted(() => {
  if (userStore.userInfo) {
    infoForm.nickname = userStore.userInfo.nickname || ''
    infoForm.avatar = userStore.userInfo.avatar || ''
    infoForm.motto = userStore.userInfo.motto || ''
  }
})

function formatTime(t) {
  if (!t) return '暂无'
  return t.replace('T', ' ').substring(0, 16)
}

function beforeAvatarUpload(file) {
  const isImage = ['image/jpeg', 'image/png', 'image/gif'].includes(file.type)
  const isLt2M = file.size / 1024 / 1024 < 5

  if (!isImage) {
    ElMessage.error('只能上传 jpg/png/gif 格式的图片')
    return false
  }
  if (!isLt2M) {
    ElMessage.error('图片大小不能超过 5MB')
    return false
  }
  return true
}

async function handleAvatarUpload(options) {
  try {
    const res = await uploadAvatar(options.file)
    if (res.code === 200) {
      infoForm.avatar = res.data
      ElMessage.success('头像上传成功')
    } else {
      ElMessage.error(res.msg || '上传失败')
    }
  } catch (e) {
    ElMessage.error('上传失败，请稍后重试')
  }
}

async function handleUpdateInfo() {
  savingInfo.value = true
  try {
    const res = await updateUserInfo(infoForm)
    if (res.code === 200) {
      ElMessage.success('修改成功')
      userStore.fetchUserInfo()
    } else {
      ElMessage.error(res.msg || '修改失败')
    }
  } catch (e) {
    ElMessage.error('修改失败，请稍后重试')
  } finally {
    savingInfo.value = false
  }
}

async function handleChangePwd() {
  const valid = await pwdFormRef.value.validate().catch(() => false)
  if (!valid) return
  savingPwd.value = true
  try {
    const res = await changePassword(pwdForm)
    if (res.code === 200) {
      ElMessage.success('密码修改成功，请重新登录')
      userStore.clearToken()
      location.href = '/login'
    } else {
      ElMessage.error(res.msg || '修改失败')
    }
  } catch (e) {
    ElMessage.error('修改失败，请稍后重试')
  } finally {
    savingPwd.value = false
  }
}
</script>

<style scoped>
.profile-page {
  max-width: 700px;
  margin: 0 auto;
}

.profile-card {
  background: #fff;
  border-radius: 8px;
  padding: 32px;
}

.page-title {
  font-size: 22px;
  font-weight: 600;
  color: #303133;
  margin-bottom: 24px;
}

.avatar-upload {
  display: flex;
  align-items: center;
  gap: 16px;
}

.avatar-uploader {
  border: 1px dashed #d9d9d9;
  border-radius: 6px;
  cursor: pointer;
  position: relative;
  overflow: hidden;
  width: 100px;
  height: 100px;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: border-color 0.3s;
}

.avatar-uploader:hover {
  border-color: #409eff;
}

.avatar-uploader-icon {
  font-size: 28px;
  color: #8c939d;
}

.avatar-preview {
  width: 100px;
  height: 100px;
  object-fit: cover;
  display: block;
}

.avatar-tip {
  font-size: 12px;
  color: #909399;
}

.info-text {
  color: #606266;
}
</style>
