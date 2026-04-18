<template>
  <div class="friends-page">
    <el-tabs v-model="activeTab">
      <el-tab-pane label="好友列表" name="list">
        <div class="tab-toolbar">
          <el-select v-model="groupId" placeholder="按分组筛选" clearable style="width: 180px" @change="loadFriends">
            <el-option v-for="g in groups" :key="g.id" :label="g.groupName" :value="g.id" />
          </el-select>
          <el-button type="primary" size="small" @click="showGroupDialog = true">新建分组</el-button>
          <el-button size="small" @click="showManageGroupDialog = true">管理分组</el-button>
        </div>
        <el-empty v-if="friends.length === 0" description="暂无好友" />
        <div class="friend-grid">
          <div v-for="f in friends" :key="f.friendId" class="friend-card">
            <el-avatar :size="48" :src="f.avatar || undefined">{{ (f.nickname || f.username || 'U').charAt(0) }}</el-avatar>
            <div class="friend-info">
              <div class="friend-name">{{ f.nickname || f.username }}</div>
              <div class="friend-group">{{ f.groupName || '默认分组' }}</div>
            </div>
            <el-dropdown trigger="click" @command="(cmd) => handleFriendCmd(cmd, f)">
              <el-button text size="small"><el-icon><MoreFilled /></el-icon></el-button>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item v-for="g in groups" :key="g.id" :command="'move-' + g.id">
                    移动到「{{ g.groupName }}」
                  </el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
          </div>
        </div>
      </el-tab-pane>

      <el-tab-pane label="添加好友" name="search">
        <div class="search-bar">
          <el-input v-model="searchKeyword" placeholder="输入邮箱或手机号搜索" @keyup.enter="handleSearch" style="width: 360px">
            <template #append>
              <el-button @click="handleSearch" :loading="searching">搜索</el-button>
            </template>
          </el-input>
        </div>
        <div v-if="searchResults.length > 0" class="search-results">
          <div v-for="u in searchResults" :key="u.id" class="search-item">
            <el-avatar :size="40">{{ (u.nickname || u.username).charAt(0) }}</el-avatar>
            <div class="search-info">
              <div class="search-name">{{ u.nickname || u.username }}</div>
              <div class="search-email">{{ u.email }}</div>
            </div>
            <el-button type="primary" size="small" @click="handleSendApply(u.id)">添加好友</el-button>
          </div>
        </div>
      </el-tab-pane>

      <el-tab-pane label="好友申请" name="apply">
        <el-empty v-if="applyList.length === 0" description="暂无好友申请" />
        <div class="apply-list">
          <div v-for="a in applyList" :key="a.id" class="apply-item">
            <el-avatar :size="40" :src="a.fromAvatar || undefined">{{ (a.fromUsername || 'U').charAt(0) }}</el-avatar>
            <div class="apply-info">
              <span class="apply-name">{{ a.fromUsername }}</span>
              <span class="apply-time">{{ formatTime(a.applyTime) }}</span>
            </div>
            <div v-if="a.status === 0" class="apply-actions">
              <el-button type="primary" size="small" @click="handleApply(a.id, 1)">同意</el-button>
              <el-button size="small" @click="handleApply(a.id, 2)">拒绝</el-button>
            </div>
            <el-tag v-else :type="a.status === 1 ? 'success' : 'danger'" size="small">
              {{ a.status === 1 ? '已同意' : '已拒绝' }}
            </el-tag>
          </div>
        </div>
      </el-tab-pane>
    </el-tabs>

    <el-dialog v-model="showGroupDialog" title="新建好友分组" width="400px">
      <el-input v-model="newGroupName" placeholder="分组名称" />
      <template #footer>
        <el-button @click="showGroupDialog = false">取消</el-button>
        <el-button type="primary" @click="handleCreateGroup">确定</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="showManageGroupDialog" title="管理好友分组" width="400px">
      <div class="group-list">
        <div v-for="g in groups" :key="g.id" class="group-item">
          <span>{{ g.groupName }}</span>
          <el-button v-if="g.groupName !== '默认'" type="danger" text size="small" @click="handleDeleteGroup(g.id)">删除</el-button>
          <el-tag v-else type="info" size="small">不可删除</el-tag>
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { searchUser, sendFriendApply, handleFriendApply, getFriendApplyList, getFriendList, getFriendGroups, createFriendGroup, deleteFriendGroup, moveFriendToGroup } from '../api/friend'

const activeTab = ref('list')
const friends = ref([])
const groups = ref([])
const groupId = ref(null)
const searchKeyword = ref('')
const searching = ref(false)
const searchResults = ref([])
const applyList = ref([])
const showGroupDialog = ref(false)
const showManageGroupDialog = ref(false)
const newGroupName = ref('')

async function loadFriends() {
  const res = await getFriendList(groupId.value || undefined)
  if (res.code === 200) {
    friends.value = res.data
  }
}

async function loadGroups() {
  const res = await getFriendGroups()
  if (res.code === 200) {
    groups.value = res.data
  }
}

async function loadApplies() {
  const res = await getFriendApplyList()
  if (res.code === 200) {
    applyList.value = res.data
  }
}

async function handleSearch() {
  if (!searchKeyword.value.trim()) return
  searching.value = true
  try {
    const res = await searchUser(searchKeyword.value.trim())
    if (res.code === 200) {
      searchResults.value = res.data
    }
  } finally {
    searching.value = false
  }
}

async function handleSendApply(toUserId) {
  const res = await sendFriendApply(toUserId)
  if (res.code === 200) {
    ElMessage.success('好友申请已发送')
  }
}

async function handleApply(id, status) {
  const res = await handleFriendApply(id, { status })
  if (res.code === 200) {
    ElMessage.success(status === 1 ? '已同意' : '已拒绝')
    loadApplies()
    loadFriends()
  }
}

async function handleCreateGroup() {
  if (!newGroupName.value.trim()) return
  const res = await createFriendGroup({ groupName: newGroupName.value.trim() })
  if (res.code === 200) {
    ElMessage.success('分组创建成功')
    showGroupDialog.value = false
    newGroupName.value = ''
    loadGroups()
  }
}

async function handleDeleteGroup(id) {
  try {
    const res = await deleteFriendGroup(id)
    if (res.code === 200) {
      ElMessage.success('分组删除成功')
      loadGroups()
      loadFriends()
    } else {
      ElMessage.error(res.msg || '删除失败')
    }
  } catch (e) {
    ElMessage.error('删除失败')
  }
}

async function handleFriendCmd(cmd, friend) {
  if (cmd.startsWith('move-')) {
    const gId = parseInt(cmd.replace('move-', ''))
    const res = await moveFriendToGroup(friend.friendId, gId)
    if (res.code === 200) {
      ElMessage.success('已移动分组')
      loadFriends()
    }
  }
}

function formatTime(t) {
  if (!t) return ''
  return t.replace('T', ' ').substring(0, 16)
}

onMounted(() => {
  loadFriends()
  loadGroups()
  loadApplies()
})
</script>

<style scoped>
.friends-page {
  max-width: 900px;
  margin: 0 auto;
  background: #fff;
  border-radius: 8px;
  padding: 24px;
}

.tab-toolbar {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 16px;
}

.friend-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(240px, 1fr));
  gap: 12px;
}

.friend-card {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px;
  border: 1px solid #ebeef5;
  border-radius: 8px;
  transition: box-shadow 0.2s;
}

.friend-card:hover {
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
}

.friend-info {
  flex: 1;
  min-width: 0;
}

.friend-name {
  font-size: 14px;
  font-weight: 500;
  color: #303133;
}

.friend-group {
  font-size: 12px;
  color: #909399;
  margin-top: 2px;
}

.search-bar {
  margin-bottom: 20px;
}

.search-results {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.search-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px;
  border: 1px solid #ebeef5;
  border-radius: 8px;
}

.search-info {
  flex: 1;
}

.search-name {
  font-size: 14px;
  font-weight: 500;
  color: #303133;
}

.search-email {
  font-size: 12px;
  color: #909399;
}

.apply-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.apply-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px;
  border: 1px solid #ebeef5;
  border-radius: 8px;
}

.apply-info {
  flex: 1;
}

.apply-name {
  font-size: 14px;
  font-weight: 500;
  color: #303133;
}

.apply-time {
  font-size: 12px;
  color: #909399;
  margin-left: 8px;
}

.apply-actions {
  display: flex;
  gap: 8px;
}

.group-list {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.group-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 8px 12px;
  border: 1px solid #ebeef5;
  border-radius: 6px;
}
</style>
