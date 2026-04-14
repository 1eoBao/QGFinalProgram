<template>
  <div class="note-list" v-loading="loading">
    <div class="toolbar">
      <el-input
        v-model="keyword"
        placeholder="搜索笔记标题..."
        clearable
        style="width: 300px"
        @keyup.enter="loadNotes"
        @clear="loadNotes"
      >
        <template #prefix>
          <el-icon><Search /></el-icon>
        </template>
      </el-input>
      <el-select v-model="permissionType" placeholder="按权限筛选" clearable style="width: 200px; margin-left: 12px" @change="loadNotes">
        <el-option :value="0" label="仅自己可见" />
        <el-option :value="1" label="部分好友可见（他人不可编辑）" />
        <el-option :value="2" label="部分好友可编辑" />
        <el-option :value="3" label="所有人可见（他人不可编辑）" />
      </el-select>
    </div>

    <div v-if="errorMsg" class="error-tip">
      <el-alert :title="errorMsg" type="error" show-icon :closable="false" />
      <el-button type="primary" size="small" style="margin-top: 12px" @click="loadNotes">重新加载</el-button>
    </div>

    <el-empty v-else-if="notes.length === 0 && !loading" description="暂无笔记，点击左上角新建" />

    <div v-else class="note-grid">
      <div v-for="note in notes" :key="note.id" class="note-card" @click="$router.push(`/note/${note.id}`)">
        <div class="note-card-header">
          <h3 class="note-title">{{ note.title || '无标题' }}</h3>
          <el-tag v-if="note.permissionType === 3" type="success" size="small">所有人可见</el-tag>
          <el-tag v-else-if="note.permissionType === 2" type="warning" size="small">好友可编辑</el-tag>
          <el-tag v-else-if="note.permissionType === 1" type="warning" size="small">好友可见</el-tag>
          <el-tag v-else type="info" size="small">仅自己</el-tag>
        </div>
        <p class="note-preview">{{ note.contentPreview || '暂无内容' }}</p>
        <div class="note-footer">
          <span class="note-time">{{ formatTime(note.lastEditTime) }}</span>
        </div>
      </div>
    </div>

    <div v-if="total > pageSize" class="pagination">
      <el-pagination
        v-model:current-page="pageNum"
        :page-size="pageSize"
        :total="total"
        layout="prev, pager, next"
        @current-change="loadNotes"
      />
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { getNoteList } from '../api/note'

const notes = ref([])
const keyword = ref('')
const permissionType = ref(null)
const pageNum = ref(1)
const pageSize = ref(12)
const total = ref(0)
const loading = ref(false)
const errorMsg = ref('')

async function loadNotes() {
  loading.value = true
  errorMsg.value = ''
  try {
    const res = await getNoteList({
      pageNum: pageNum.value,
      pageSize: pageSize.value,
      keyword: keyword.value || undefined,
      permissionType: permissionType.value !== null && permissionType.value !== undefined ? permissionType.value : undefined
    })
    if (res.code === 200) {
      notes.value = res.data.records || []
      total.value = res.data.total || 0
    } else {
      errorMsg.value = res.msg || '加载失败'
    }
  } catch (e) {
    errorMsg.value = '无法连接服务器，请确认后端已启动'
  } finally {
    loading.value = false
  }
}

function formatTime(t) {
  if (!t) return ''
  return t.replace('T', ' ').substring(0, 16)
}

onMounted(loadNotes)
</script>

<style scoped>
.note-list {
  max-width: 1200px;
  margin: 0 auto;
}

.toolbar {
  display: flex;
  align-items: center;
  margin-bottom: 20px;
}

.note-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
  gap: 16px;
}

.note-card {
  background: #fff;
  border-radius: 8px;
  padding: 20px;
  cursor: pointer;
  transition: box-shadow 0.2s, transform 0.2s;
  border: 1px solid #ebeef5;
}

.note-card:hover {
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.1);
  transform: translateY(-2px);
}

.note-card-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 10px;
}

.note-title {
  font-size: 16px;
  font-weight: 600;
  color: #303133;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  flex: 1;
  margin-right: 8px;
}

.note-preview {
  font-size: 13px;
  color: #909399;
  line-height: 1.6;
  display: -webkit-box;
  -webkit-line-clamp: 3;
  -webkit-box-orient: vertical;
  overflow: hidden;
  margin-bottom: 12px;
  min-height: 60px;
}

.note-footer {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.note-time {
  font-size: 12px;
  color: #c0c4cc;
  white-space: nowrap;
}

.error-tip {
  text-align: center;
  padding: 40px 0;
}

.pagination {
  display: flex;
  justify-content: center;
  margin-top: 24px;
}
</style>
