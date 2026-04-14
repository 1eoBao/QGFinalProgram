<template>
  <div class="history-page">
    <h2 class="page-title">浏览历史</h2>
    <el-empty v-if="history.length === 0 && !loading" description="暂无浏览记录" />
    <div class="history-list">
      <div v-for="h in history" :key="h.noteId" class="history-item" @click="$router.push(`/note/${h.noteId}`)">
        <el-icon class="history-icon"><Document /></el-icon>
        <div class="history-info">
          <div class="history-title">{{ h.title || '无标题笔记' }}</div>
          <div class="history-time">{{ formatTime(h.viewTime) }}</div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { getNoteHistory } from '../api/note'

const history = ref([])
const loading = ref(false)

async function loadHistory() {
  loading.value = true
  try {
    const res = await getNoteHistory()
    if (res.code === 200) {
      history.value = res.data
    }
  } finally {
    loading.value = false
  }
}

function formatTime(t) {
  if (!t) return ''
  return t.replace('T', ' ').substring(0, 16)
}

onMounted(loadHistory)
</script>

<style scoped>
.history-page {
  max-width: 700px;
  margin: 0 auto;
}

.page-title {
  font-size: 22px;
  font-weight: 600;
  color: #303133;
  margin-bottom: 20px;
}

.history-list {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.history-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 14px 16px;
  background: #fff;
  border-radius: 8px;
  cursor: pointer;
  transition: box-shadow 0.2s;
}

.history-item:hover {
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
}

.history-icon {
  color: #667eea;
  font-size: 20px;
}

.history-info {
  flex: 1;
}

.history-title {
  font-size: 14px;
  font-weight: 500;
  color: #303133;
}

.history-time {
  font-size: 12px;
  color: #909399;
  margin-top: 2px;
}
</style>
