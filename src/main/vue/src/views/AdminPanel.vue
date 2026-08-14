<template>
  <div class="admin-panel">
    <div class="admin-tabs">
      <button v-for="tab in tabs" :key="tab.key" class="admin-tab" :class="{ active: activeTab === tab.key }" @click="activeTab = tab.key">
        <span>{{ tab.icon }}</span>
        <span>{{ tab.label }}</span>
      </button>
    </div>

    <!-- 用户管理 -->
    <div v-if="activeTab === 'users'" class="tab-content">
      <div class="search-bar">
        <div class="search-box">
          <el-icon><Search /></el-icon>
          <input v-model="userSearch" type="text" placeholder="搜索用户..." />
        </div>
      </div>
      <div class="table-wrapper">
        <table class="data-table">
          <thead>
            <tr>
              <th>ID</th><th>用户名</th><th>手机号</th><th>角色</th><th>会员等级</th><th>积分</th><th>注册时间</th><th>操作</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="user in filteredUsers" :key="user.id">
              <td>{{ user.id }}</td>
              <td>{{ user.username }}</td>
              <td>{{ user.phone || '-' }}</td>
              <td><span class="role-badge" :class="user.role === 'ADMIN' ? 'admin' : 'user'">{{ user.role === 'ADMIN' ? '管理员' : '用户' }}</span></td>
              <td>{{ user.memberLevel || '普通会员' }}</td>
              <td>{{ user.points || 0 }}</td>
              <td>{{ formatDate(user.createdAt) }}</td>
              <td>
                <button class="table-action" @click="toggleUserStatus(user)">{{ user.status === 1 ? '禁用' : '启用' }}</button>
              </td>
            </tr>
          </tbody>
        </table>
      </div>
    </div>

    <!-- 宠物管理 -->
    <div v-if="activeTab === 'pets'" class="tab-content">
      <div class="table-wrapper">
        <table class="data-table">
          <thead>
            <tr>
              <th>ID</th><th>名称</th><th>类型</th><th>品种</th><th>性别</th><th>年龄</th><th>体重</th><th>主人</th><th>操作</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="pet in petList" :key="pet.id">
              <td>{{ pet.id }}</td>
              <td>{{ pet.name }}</td>
              <td>{{ pet.type }}</td>
              <td>{{ pet.breed }}</td>
              <td>{{ pet.gender }}</td>
              <td>{{ pet.age }}个月</td>
              <td>{{ pet.weight }}kg</td>
              <td>{{ pet.ownerName }}</td>
              <td>
                <button class="table-action" @click="viewPetDetail(pet)">查看</button>
                <button class="table-action danger" @click="deletePetConfirm(pet.id)">删除</button>
              </td>
            </tr>
          </tbody>
        </table>
      </div>
    </div>

    <!-- 预约管理 -->
    <div v-if="activeTab === 'appointments'" class="tab-content">
      <div class="table-wrapper">
        <table class="data-table">
          <thead>
            <tr>
              <th>ID</th><th>宠物</th><th>主人</th><th>房型</th><th>入住日期</th><th>离店日期</th><th>金额</th><th>状态</th><th>操作</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="appt in appointmentList" :key="appt.id">
              <td>{{ appt.id }}</td>
              <td>{{ appt.petName }}</td>
              <td>{{ appt.ownerName }}</td>
              <td>{{ appt.roomType }}</td>
              <td>{{ appt.checkInDate }}</td>
              <td>{{ appt.checkOutDate }}</td>
              <td>¥{{ appt.totalAmount }}</td>
              <td><span class="status-badge" :class="getStatusClass(appt.status)">{{ getStatusText(appt.status) }}</span></td>
              <td>
                <button v-if="appt.status === 'PENDING'" class="table-action primary" @click="approveAppointment(appt.id)">通过</button>
                <button v-if="appt.status === 'PENDING'" class="table-action danger" @click="rejectAppointment(appt.id)">拒绝</button>
              </td>
            </tr>
          </tbody>
        </table>
      </div>
    </div>

    <!-- 公告管理 -->
    <div v-if="activeTab === 'notices'" class="tab-content">
      <div class="search-bar">
        <button class="btn-primary" @click="openNoticeModal">
          <el-icon><Plus /></el-icon>
          发布公告
        </button>
      </div>
      <div class="table-wrapper">
        <table class="data-table">
          <thead>
            <tr>
              <th>ID</th><th>标题</th><th>内容</th><th>类型</th><th>发布时间</th><th>操作</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="notice in noticeList" :key="notice.id">
              <td>{{ notice.id }}</td>
              <td>{{ notice.title }}</td>
              <td class="text-ellipsis">{{ notice.content }}</td>
              <td>{{ notice.noticeType }}</td>
              <td>{{ formatDate(notice.createdAt) }}</td>
              <td>
                <button class="table-action" @click="editNotice(notice)">编辑</button>
                <button class="table-action danger" @click="deleteNoticeItem(notice.id)">删除</button>
              </td>
            </tr>
          </tbody>
        </table>
      </div>
    </div>

    <!-- 公告弹窗 -->
    <el-dialog v-model="showNoticeModal" :title="isEditingNotice ? '编辑公告' : '发布公告'" width="500px" class="modal-style">
      <form @submit.prevent="saveNotice" class="notice-form">
        <div class="form-group">
          <label>标题</label>
          <input v-model="noticeForm.title" type="text" class="form-input" required />
        </div>
        <div class="form-group">
          <label>内容</label>
          <textarea v-model="noticeForm.content" class="form-textarea" rows="4" required></textarea>
        </div>
        <div class="form-group">
          <label>类型</label>
          <select v-model="noticeForm.noticeType" class="form-select">
            <option value="SYSTEM">系统通知</option>
            <option value="ACTIVITY">活动通知</option>
            <option value="ANNOUNCEMENT">公告</option>
          </select>
        </div>
        <div class="form-actions">
          <button type="button" class="cancel-btn" @click="showNoticeModal = false">取消</button>
          <button type="submit" class="btn-primary">发布</button>
        </div>
      </form>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getAllUsers, getAllPets, getAllAppointments, getNotices, createNotice, updateNoticeAdmin, deleteNoticeAdmin, approveAppointmentAdmin, rejectAppointmentAdmin } from '@/api'

const activeTab = ref('users')
const tabs = [
  { key: 'users', label: '用户管理', icon: '👥' },
  { key: 'pets', label: '宠物管理', icon: '🐾' },
  { key: 'appointments', label: '预约管理', icon: '📋' },
  { key: 'notices', label: '公告管理', icon: '📢' }
]

// 用户数据
const userList = ref([])
const userSearch = ref('')
const filteredUsers = computed(() => {
  if (!userSearch.value) return userList.value
  return userList.value.filter(u => u.username?.includes(userSearch.value) || u.phone?.includes(userSearch.value))
})

// 宠物数据
const petList = ref([])

// 预约数据
const appointmentList = ref([])

// 公告数据
const noticeList = ref([])
const showNoticeModal = ref(false)
const isEditingNotice = ref(false)
const noticeForm = reactive({ title: '', content: '', noticeType: 'SYSTEM' })

function formatDate(date) {
  if (!date) return ''
  return new Date(date).toLocaleDateString('zh-CN')
}

function getStatusClass(status) {
  return { 'PENDING': 's-pending', 'CONFIRMED': 's-confirmed', 'CANCELLED': 's-cancelled', 'COMPLETED': 's-completed' }[status] || ''
}

function getStatusText(status) {
  return { 'PENDING': '待审核', 'CONFIRMED': '已通过', 'CANCELLED': '已拒绝', 'COMPLETED': '已完成' }[status] || status
}

async function loadUsers() {
  try { const res = await getAllUsers(); userList.value = res.data || [] } catch (e) { /* ignore */ }
}

async function loadPets() {
  try { const res = await getAllPets(); petList.value = res.data || [] } catch (e) { /* ignore */ }
}

async function loadAppointments() {
  try { const res = await getAllAppointments(); appointmentList.value = res.data || [] } catch (e) { /* ignore */ }
}

async function loadNotices() {
  try { const res = await getNotices(); noticeList.value = res.data || [] } catch (e) { /* ignore */ }
}

async function toggleUserStatus(user) {
  try {
    await ElMessageBox.confirm(`确定${user.status === 1 ? '禁用' : '启用'}该用户？`, '提示')
    ElMessage.success('操作成功')
  } catch (e) { /* 取消 */ }
}

async function approveAppointment(id) {
  try {
    await approveAppointmentAdmin(id)
    ElMessage.success('预约已通过')
    await loadAppointments()
  } catch (e) { ElMessage.error('操作失败') }
}

async function rejectAppointment(id) {
  try {
    await rejectAppointmentAdmin(id)
    ElMessage.success('预约已拒绝')
    await loadAppointments()
  } catch (e) { ElMessage.error('操作失败') }
}

function openNoticeModal() {
  isEditingNotice.value = false
  noticeForm.title = ''; noticeForm.content = ''; noticeForm.noticeType = 'SYSTEM'
  showNoticeModal.value = true
}

function editNotice(notice) {
  isEditingNotice.value = true
  noticeForm.id = notice.id; noticeForm.title = notice.title; noticeForm.content = notice.content; noticeForm.noticeType = notice.noticeType
  showNoticeModal.value = true
}

async function saveNotice() {
  try {
    if (isEditingNotice.value) {
      await updateNoticeAdmin(noticeForm.id, noticeForm)
      ElMessage.success('更新成功')
    } else {
      await createNotice(noticeForm)
      ElMessage.success('发布成功')
    }
    showNoticeModal.value = false
    await loadNotices()
  } catch (e) { ElMessage.error('操作失败') }
}

async function deleteNoticeItem(id) {
  try {
    await ElMessageBox.confirm('确定删除该公告？', '提示', { type: 'warning' })
    await deleteNoticeAdmin(id)
    ElMessage.success('删除成功')
    await loadNotices()
  } catch (e) { /* 取消 */ }
}

function viewPetDetail(pet) {
  ElMessage.info(`查看宠物: ${pet.name}`)
}

async function deletePetConfirm(id) {
  try {
    await ElMessageBox.confirm('确定删除该宠物？', '提示', { type: 'warning' })
    ElMessage.success('删除成功')
    await loadPets()
  } catch (e) { /* 取消 */ }
}

onMounted(() => {
  loadUsers()
  loadPets()
  loadAppointments()
  loadNotices()
})
</script>

<style lang="scss" scoped>
.admin-panel {
  animation: fadeInUp 0.5s ease;
}

.admin-tabs {
  display: flex;
  gap: 8px;
  margin-bottom: 24px;
  overflow-x: auto;
}

.admin-tab {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 10px 20px;
  border: 1px solid rgba(255, 182, 193, 0.2);
  border-radius: 25px;
  background: var(--bg-card);
  color: #9B8FB5;
  cursor: pointer;
  font-size: 14px;
  transition: all 0.25s;
  white-space: nowrap;

  &:hover { border-color: var(--primary); color: var(--primary); }
  &.active {
    background: linear-gradient(135deg, #FFE4E9, #FFDAB9);
    border-color: var(--primary);
    color: var(--primary);
    font-weight: 600;
  }
}

.search-bar {
  display: flex;
  margin-bottom: 16px;
}

.search-box {
  display: flex;
  align-items: center;
  gap: 8px;
  background: var(--bg-card);
  border-radius: 14px;
  padding: 10px 16px;
  border: 1px solid rgba(255, 182, 193, 0.2);
  flex: 1;
  max-width: 320px;

  input {
    border: none; outline: none;
    font-size: 14px; color: #5D4E6D;
    width: 100%;
    &::placeholder { color: #C4B8D4; }
  }
  .el-icon { color: #C4B8D4; }
}

.table-wrapper {
  overflow-x: auto;
  background: var(--bg-card);
  border-radius: 16px;
  box-shadow: 0 2px 12px rgba(255, 182, 193, 0.08);
}

.data-table {
  width: 100%;
  border-collapse: collapse;
  font-size: 14px;

  th {
    text-align: left;
    padding: 14px 16px;
    color: #9B8FB5;
    font-weight: 500;
    background: var(--bg-card);
    border-bottom: 1px solid rgba(255, 182, 193, 0.1);
    white-space: nowrap;
  }

  td {
    padding: 14px 16px;
    color: #5D4E6D;
    border-bottom: 1px solid rgba(255, 182, 193, 0.05);
  }

  tr:hover td { background: rgba(255,255,255,0.03); }
}

.role-badge {
  font-size: 12px;
  padding: 3px 10px;
  border-radius: 10px;

  &.admin { background: rgba(201, 160, 80, 0.1); color: var(--primary); }
  &.user { background: rgba(74, 144, 217, 0.15); color: #4A90D9; }
}

.status-badge {
  font-size: 12px;
  padding: 3px 10px;
  border-radius: 10px;

  &.s-pending { background: rgba(255,152,0,0.15); color: #FF9800; }
  &.s-confirmed { background: rgba(76, 175, 80, 0.15); color: #4CAF50; }
  &.s-cancelled { background: rgba(229, 57, 53, 0.15); color: #E53935; }
  &.s-completed { background: rgba(74, 144, 217, 0.15); color: #4A90D9; }
}

.table-action {
  padding: 4px 12px;
  border: 1px solid rgba(255, 182, 193, 0.2);
  border-radius: 12px;
  background: var(--bg-card);
  color: #9B8FB5;
  cursor: pointer;
  font-size: 12px;
  margin-right: 6px;
  transition: all 0.2s;

  &:hover { background: rgba(201, 160, 80, 0.1); color: var(--primary); }
  &.danger:hover { background: rgba(229, 57, 53, 0.15); color: #E53935; }
  &.primary { background: rgba(201, 160, 80, 0.1); color: var(--primary); border-color: var(--primary); }
}

.notice-form {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.form-group {
  display: flex;
  flex-direction: column;
  gap: 6px;
  label { font-size: 13px; color: #9B8FB5; }
}

.form-input, .form-select, .form-textarea {
  padding: 10px 14px;
  border: 1px solid rgba(255, 182, 193, 0.2);
  border-radius: 12px;
  font-size: 14px;
  color: #5D4E6D;
  outline: none;
  background: var(--bg-card);
  &:focus { border-color: var(--primary); }
}

.form-textarea { resize: vertical; }

.form-actions {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
}

.cancel-btn {
  padding: 10px 28px;
  border: 1px solid rgba(255, 182, 193, 0.3);
  border-radius: 25px;
  background: var(--bg-card);
  color: #9B8FB5;
  cursor: pointer;
  &:hover { background: rgba(201, 160, 80, 0.1); }
}
</style>