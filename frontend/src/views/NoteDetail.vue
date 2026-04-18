<template>
  <div class="note-detail" v-loading="loading">
    <div v-if="errorMsg" class="error-state">
      <el-alert :title="errorMsg" type="error" show-icon :closable="false" />
      <div class="error-actions">
        <el-button type="primary" @click="loadNote">重新加载</el-button>
        <el-button @click="$router.back()">返回列表</el-button>
      </div>
    </div>
    <div v-else-if="note" class="detail-content">
      <div class="detail-header">
        <el-button text @click="$router.back()">
          <el-icon><ArrowLeft /></el-icon>返回
        </el-button>
        <div class="header-actions">
          <el-button v-if="note.canEdit" type="primary" text @click="$router.push(`/note/${note.id}/edit`)">
            <el-icon><Edit /></el-icon>编辑
          </el-button>
          <el-button v-if="note.isOwner" type="danger" text @click="handleDelete">
            <el-icon><Delete /></el-icon>删除
          </el-button>
        </div>
      </div>

      <h1 class="detail-title">{{ note.title || '无标题' }}</h1>

      <div class="detail-meta">
        <el-tag v-if="note.permissionType === 3" type="success" size="small">所有人可见</el-tag>
        <el-tag v-else-if="note.permissionType === 2" type="warning" size="small">好友可编辑</el-tag>
        <el-tag v-else-if="note.permissionType === 1" type="warning" size="small">好友可见</el-tag>
        <el-tag v-else type="info" size="small">仅自己</el-tag>
        <span class="meta-time">最后编辑：{{ formatTime(note.lastEditTime) }}</span>
        <div class="meta-tags">
          <el-tag v-for="tag in note.tags" :key="tag.id" size="small" effect="plain">{{ tag.name }}</el-tag>
        </div>
      </div>

      <el-divider />

      <div class="detail-body" v-html="formatContent(note.content)"></div>

      <el-divider />

      <div class="ai-section">
        <div class="ai-header">
          <h3><el-icon><MagicStick /></el-icon> AI 智能分析</h3>
          <el-button type="primary" size="small" :loading="aiLoading" @click="handleAnalyze">
            {{ note.aiAnalysis ? '重新分析' : '开始分析' }}
          </el-button>
        </div>

        <div v-if="note.aiAnalysis" class="ai-result">
          <div class="ai-block">
            <h4>摘要</h4>
            <p>{{ note.aiAnalysis.summary }}</p>
          </div>
          <div class="ai-block">
            <h4>要点</h4>
            <ul>
              <li v-for="(point, idx) in parseJson(note.aiAnalysis.keyPoints)" :key="idx">{{ point }}</li>
            </ul>
          </div>
          <div class="ai-block">
            <h4>建议标签</h4>
            <div class="ai-tags">
              <el-tag v-for="(tag, idx) in parseJson(note.aiAnalysis.suggestedTags)" :key="idx" type="success" effect="plain">{{ tag }}</el-tag>
            </div>
          </div>
        </div>
        <el-empty v-else description="暂无分析结果，点击上方按钮开始分析" :image-size="60" />
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, watch, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getPublicNote, deleteNote } from '../api/note'
import { analyzeNote } from '../api/ai'

const route = useRoute()
const router = useRouter()
const note = ref(null)
const loading = ref(false)
const aiLoading = ref(false)
const errorMsg = ref('')

async function loadNote() {
  loading.value = true
  errorMsg.value = ''
  note.value = null
  try {
    const res = await getPublicNote(route.params.id)
    if (res.code === 200) {
      note.value = res.data
    } else if (res.code === 401) {
      errorMsg.value = '请先登录后查看此笔记'
    } else {
      errorMsg.value = res.msg || '加载笔记失败'
    }
  } catch (e) {
    if (e.response?.status === 401) {
      errorMsg.value = '请先登录后查看此笔记'
    } else {
      errorMsg.value = '无法连接服务器，请确认后端已启动'
    }
  } finally {
    loading.value = false
  }
}

async function handleAnalyze() {
  aiLoading.value = true
  try {
    const res = await analyzeNote(route.params.id)
    if (res.code === 200) {
      note.value.aiAnalysis = res.data
      ElMessage.success('AI分析完成')
    } else {
      ElMessage.error(res.msg || 'AI分析失败')
    }
  } catch (e) {
    ElMessage.error('AI分析请求失败')
  } finally {
    aiLoading.value = false
  }
}

async function handleDelete() {
  try {
    await ElMessageBox.confirm('确定删除该笔记吗？', '提示', { type: 'warning' })
  } catch {
    return
  }
  try {
    const res = await deleteNote(route.params.id)
    if (res.code === 200) {
      ElMessage.success('删除成功')
      router.push('/notes')
    } else {
      ElMessage.error(res.msg || '删除失败')
    }
  } catch (e) {
    ElMessage.error('删除请求失败')
  }
}

function formatTime(t) {
  if (!t) return ''
  return t.replace('T', ' ').substring(0, 16)
}

function formatContent(content) {
  if (!content) return ''
  return content.replace(/\n/g, '<br>')
}

function parseJson(str) {
  if (!str) return []
  try {
    return typeof str === 'string' ? JSON.parse(str) : str
  } catch {
    return []
  }
}

onMounted(loadNote)

watch(() => route.params.id, (newId) => {
  if (newId) loadNote()
})
</script>

<style scoped>
.note-detail {
  max-width: 800px;
  margin: 0 auto;
  background: #fff;
  border-radius: 8px;
  padding: 32px;
  min-height: 500px;
}

.detail-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}

.detail-title {
  font-size: 28px;
  font-weight: 700;
  color: #303133;
  margin-bottom: 12px;
}

.detail-meta {
  display: flex;
  align-items: center;
  gap: 12px;
  flex-wrap: wrap;
}

.meta-time {
  font-size: 13px;
  color: #909399;
}

.detail-body {
  font-size: 15px;
  line-height: 1.8;
  color: #303133;
  min-height: 100px;
}

.ai-section {
  margin-top: 8px;
}

.ai-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 16px;
}

.ai-header h3 {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 16px;
  color: #667eea;
}

.ai-result {
  background: #f5f7fa;
  border-radius: 8px;
  padding: 20px;
}

.ai-block {
  margin-bottom: 16px;
}

.ai-block:last-child {
  margin-bottom: 0;
}

.ai-block h4 {
  font-size: 14px;
  color: #606266;
  margin-bottom: 8px;
}

.ai-block p {
  font-size: 14px;
  color: #303133;
  line-height: 1.6;
}

.ai-block ul {
  padding-left: 20px;
}

.ai-block li {
  font-size: 14px;
  color: #303133;
  line-height: 1.8;
}

.ai-tags {
  display: flex;
  gap: 8px;
}

.error-state {
  text-align: center;
  padding: 60px 20px;
}

.error-actions {
  margin-top: 16px;
  display: flex;
  justify-content: center;
  gap: 12px;
}
</style>
