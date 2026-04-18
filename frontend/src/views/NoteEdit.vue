<template>
  <div class="note-edit">
    <div class="edit-container">
      <h2 class="page-title">{{ isEdit ? '编辑笔记' : '新建笔记' }}</h2>
      <el-form ref="formRef" :model="form" :rules="rules" label-position="top">
        <el-form-item label="标题" prop="title">
          <el-input v-model="form.title" placeholder="请输入笔记标题" maxlength="100" show-word-limit />
        </el-form-item>
        <el-form-item label="内容" prop="content">
          <el-input v-model="form.content" type="textarea" placeholder="开始记录你的想法..." :rows="16" />
        </el-form-item>
        <el-form-item v-if="!isEdit || isOwner" label="权限">
          <el-radio-group v-model="form.permissionType">
            <el-radio :value="0">仅自己可见</el-radio>
            <el-radio :value="1">部分好友可见（他人不可编辑）</el-radio>
            <el-radio :value="2">部分好友可编辑</el-radio>
            <el-radio :value="3">所有人可见（他人不可编辑）</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item v-if="(form.permissionType === 1 || form.permissionType === 2) && (!isEdit || isOwner)" label="选择好友">
          <el-select v-model="form.targetUserIds" multiple placeholder="选择可见好友" style="width: 100%">
            <el-option v-for="f in friendList" :key="f.friendId" :label="f.nickname || f.username" :value="f.friendId" />
          </el-select>
        </el-form-item>
        <el-form-item v-if="isEdit && !isOwner" label="当前权限">
          <el-tag v-if="form.permissionType === 3" type="success">所有人可见</el-tag>
          <el-tag v-else-if="form.permissionType === 2" type="warning">好友可编辑</el-tag>
          <el-tag v-else-if="form.permissionType === 1" type="warning">好友可见</el-tag>
          <el-tag v-else type="info">仅自己可见</el-tag>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :loading="saving" @click="handleSave">{{ isEdit ? '保存修改' : '创建笔记' }}</el-button>
          <el-button @click="$router.back()">取消</el-button>
        </el-form-item>
      </el-form>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { createNote, getNoteDetail, updateNote, updateNotePermission } from '../api/note'
import { getFriendList } from '../api/friend'

const route = useRoute()
const router = useRouter()
const formRef = ref(null)
const saving = ref(false)
const friendList = ref([])
const isOwner = ref(true)

const isEdit = computed(() => route.name === 'NoteEdit')

const form = reactive({
  title: '',
  content: '',
  permissionType: 0,
  targetUserIds: []
})

const rules = {
  title: [{ required: true, message: '请输入标题', trigger: 'blur' }]
}

async function loadNote() {
  if (!isEdit.value) return
  try {
    const res = await getNoteDetail(route.params.id)
    if (res.code === 200) {
      form.title = res.data.title
      form.content = res.data.content
      form.permissionType = res.data.permissionType
      form.targetUserIds = res.data.targetUserIds || []
      isOwner.value = res.data.isOwner === true
    } else {
      ElMessage.error(res.msg || '加载笔记失败')
      router.back()
    }
  } catch (e) {
    ElMessage.error('加载笔记失败')
    router.back()
  }
}

async function loadFriends() {
  const res = await getFriendList()
  if (res.code === 200) {
    friendList.value = res.data
  }
}

async function handleSave() {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return
  saving.value = true
  try {
    if (isEdit.value) {
      const res = await updateNote(route.params.id, { title: form.title, content: form.content })
      if (res.code === 200) {
        if (isOwner.value) {
          if (form.permissionType === 1 || form.permissionType === 2) {
            await updateNotePermission(route.params.id, {
              permissionType: form.permissionType,
              targetUserIds: form.targetUserIds
            })
          } else {
            await updateNotePermission(route.params.id, { permissionType: form.permissionType })
          }
        }
        ElMessage.success('保存成功')
        router.push('/notes')
      } else {
        ElMessage.error(res.msg || '保存失败')
      }
    } else {
      const res = await createNote({ title: form.title, content: form.content })
      if (res.code === 200) {
        if (form.permissionType !== 0) {
          await updateNotePermission(res.data, {
            permissionType: form.permissionType,
            targetUserIds: form.targetUserIds
          })
        }
        ElMessage.success('创建成功')
        router.push('/notes')
      } else {
        ElMessage.error(res.msg || '创建失败')
      }
    }
  } catch (e) {
    ElMessage.error('操作失败')
  } finally {
    saving.value = false
  }
}

onMounted(() => {
  loadFriends()
  loadNote()
})
</script>

<style scoped>
.note-edit {
  max-width: 800px;
  margin: 0 auto;
}

.edit-container {
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
</style>
