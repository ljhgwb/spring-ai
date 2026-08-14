<template>
  <div class="health-management">
    <!-- 宠物选择 -->
    <div class="pet-select-bar">
      <select v-model="selectedPetId" class="pet-select" @change="loadRecords">
        <option value="">请选择宠物</option>
        <option v-for="pet in petList" :key="pet.id" :value="pet.id">{{ pet.name }} ({{ pet.type }})</option>
      </select>
      <button class="btn-primary" @click="openAddModal" :disabled="!selectedPetId">
        <el-icon><Plus /></el-icon>
        添加记录
      </button>
    </div>

    <!-- 选项卡 -->
    <div class="tabs-container">
      <div class="tabs">
        <button v-for="tab in tabs" :key="tab.key" class="tab-btn" :class="{ active: activeTab === tab.key }" @click="activeTab = tab.key">
          <span class="tab-icon">{{ tab.icon }}</span>
          <span>{{ tab.label }}</span>
        </button>
      </div>
    </div>

    <!-- 记录列表 -->
    <div v-if="filteredRecords.length === 0" class="empty-state">
      <span class="empty-icon">📋</span>
      <p>暂无记录</p>
    </div>

    <div v-for="record in filteredRecords" :key="record.id" class="record-card card">
      <div class="record-header">
        <span class="record-type-badge" :class="'type-' + record.recordType?.toLowerCase()">
          {{ getTypeLabel(record.recordType) }}
        </span>
        <span class="record-date">{{ formatDate(record.createdAt) }}</span>
      </div>
      <div class="record-body">
        <p v-if="record.description">{{ record.description }}</p>
        <div class="record-metrics">
          <span v-if="record.weight">体重: {{ record.weight }} kg<template v-if="record.weightDate"> ({{ formatDate(record.weightDate) }})</template></span>
          <span v-if="record.temperature">体温: {{ record.temperature }}℃</span>
          <span v-if="record.vaccineName">疫苗: {{ record.vaccineName }}<template v-if="record.vaccineDate"> ({{ formatDate(record.vaccineDate) }})</template></span>
          <span v-if="record.dietType">饮食: {{ getDietLabel(record.dietType) }}</span>
          <span v-if="record.dentalResult">口腔: {{ getDentalLabel(record.dentalResult) }}<template v-if="record.dentalDate"> ({{ formatDate(record.dentalDate) }})</template></span>
          <span v-if="record.dentalCleanDate">洗牙日期: {{ formatDate(record.dentalCleanDate) }}</span>
        </div>
      </div>
      <div class="record-actions">
        <button class="action-btn edit" @click="editRecord(record)"><el-icon><Edit /></el-icon></button>
        <button class="action-btn delete" @click="handleDelete(record.id)"><el-icon><Delete /></el-icon></button>
      </div>
    </div>

    <!-- 添加/编辑弹窗 -->
    <el-dialog v-model="showModal" :title="isEditing ? '编辑记录' : '添加记录'" width="500px" class="modal-style">
      <form @submit.prevent="saveRecord" class="record-form">
        <div class="form-group">
          <label>记录类型</label>
          <select v-model="form.recordType" class="form-select" @change="onTypeChange">
            <option value="HEALTH">健康检查</option>
            <option value="DIET">饮食记录</option>
            <option value="VACCINE">疫苗记录</option>
            <option value="GROWTH">成长记录</option>
          </select>
        </div>
        <div class="form-group">
          <label>描述</label>
          <textarea v-model="form.description" class="form-textarea" rows="3" placeholder="请输入描述..."></textarea>
        </div>
        <div class="form-row" v-if="form.recordType === 'HEALTH' || form.recordType === 'GROWTH'">
          <div class="form-group">
            <label>体重（kg）</label>
            <input v-model.number="form.weight" type="number" step="0.1" class="form-input" />
          </div>
          <div class="form-group">
            <label>体重记录日期</label>
            <input v-model="form.weightDate" type="date" class="form-input" />
          </div>
        </div>
        <div class="form-row" v-if="form.recordType === 'HEALTH' || form.recordType === 'GROWTH'">
          <div class="form-group">
            <label>体温（℃）</label>
            <input v-model.number="form.temperature" type="number" step="0.1" class="form-input" />
          </div>
        </div>
        <div class="form-row" v-if="form.recordType === 'VACCINE'">
          <div class="form-group">
            <label>疫苗名称</label>
            <input v-model="form.vaccineName" type="text" class="form-input" placeholder="请输入疫苗名称" />
          </div>
          <div class="form-group">
            <label>接种日期</label>
            <input v-model="form.vaccineDate" type="date" class="form-input" />
          </div>
        </div>
        <div class="form-row" v-if="form.recordType === 'HEALTH'">
          <div class="form-group">
            <label>口腔检查结果</label>
            <select v-model="form.dentalResult" class="form-select">
              <option value="">请选择</option>
              <option value="normal">正常</option>
              <option value="mild">轻微牙结石</option>
              <option value="cleaning">需要洗牙</option>
              <option value="problem">牙齿问题</option>
            </select>
          </div>
          <div class="form-group">
            <label>口腔检查日期</label>
            <input v-model="form.dentalDate" type="date" class="form-input" />
          </div>
        </div>
        <div class="form-group" v-if="form.recordType === 'HEALTH' && form.dentalResult === 'cleaning'">
          <label>洗牙日期</label>
          <input v-model="form.dentalCleanDate" type="date" class="form-input" />
        </div>
        <div class="form-group" v-if="form.recordType === 'DIET'">
          <label>饮食类型</label>
          <select v-model="form.dietType" class="form-select">
            <option value="">请选择</option>
            <option value="DRY">干粮</option>
            <option value="WET">湿粮</option>
            <option value="RAW">生鲜</option>
            <option value="HOMEMADE">自制</option>
            <option value="OTHER">其他</option>
          </select>
        </div>
        <div class="form-actions">
          <button type="button" class="cancel-btn" @click="showModal = false">取消</button>
          <button type="submit" class="btn-primary">保存</button>
        </div>
      </form>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getMyPets, getHealthRecords, addHealthRecord, updateHealthRecord, deleteHealthRecord } from '@/api'

const petList = ref([])
const records = ref([])
const selectedPetId = ref('')
const activeTab = ref('ALL')
const showModal = ref(false)
const isEditing = ref(false)
const editingId = ref(null)

const tabs = [
  { key: 'ALL', label: '全部', icon: '📋' },
  { key: 'HEALTH', label: '健康检查', icon: '❤️' },
  { key: 'DIET', label: '饮食记录', icon: '🍖' },
  { key: 'VACCINE', label: '疫苗记录', icon: '💉' },
  { key: 'GROWTH', label: '成长记录', icon: '📈' }
]

const form = reactive({
  recordType: 'HEALTH',
  description: '',
  weight: null,
  weightDate: '',
  temperature: null,
  vaccineName: '',
  vaccineDate: '',
  dentalResult: '',
  dentalDate: '',
  dentalCleanDate: '',
  dietType: ''
})

const filteredRecords = computed(() => {
  if (activeTab.value === 'ALL') return records.value
  return records.value.filter(r => r.recordType === activeTab.value)
})

function getTypeLabel(type) {
  return { 'HEALTH': '健康检查', 'DIET': '饮食记录', 'VACCINE': '疫苗记录', 'GROWTH': '成长记录' }[type] || type
}

function getDietLabel(type) {
  return { 'DRY': '干粮', 'WET': '湿粮', 'RAW': '生鲜', 'HOMEMADE': '自制', 'OTHER': '其他' }[type] || type
}

function getDentalLabel(type) {
  return { 'normal': '正常', 'mild': '轻微牙结石', 'cleaning': '需要洗牙', 'problem': '牙齿问题' }[type] || type
}

function formatDate(date) {
  if (!date) return ''
  return new Date(date).toLocaleDateString('zh-CN')
}

function onTypeChange() {
  form.weight = null; form.weightDate = ''; form.temperature = null; form.vaccineName = ''; form.vaccineDate = ''; form.dentalResult = ''; form.dentalDate = ''; form.dentalCleanDate = ''; form.dietType = ''
}

async function loadPets() {
  try {
    const res = await getMyPets()
    petList.value = res.data || []
  } catch (e) { /* ignore */ }
}

async function loadRecords() {
  if (!selectedPetId.value) { records.value = []; return }
  try {
    const res = await getHealthRecords(selectedPetId.value)
    records.value = res.data || []
  } catch (e) { records.value = [] }
}

function openAddModal() {
  isEditing.value = false; editingId.value = null
  Object.assign(form, { recordType: 'HEALTH', description: '', weight: null, weightDate: '', temperature: null, vaccineName: '', vaccineDate: '', dentalResult: '', dentalDate: '', dentalCleanDate: '', dietType: '' })
  showModal.value = true
}

function editRecord(record) {
  isEditing.value = true; editingId.value = record.id
  Object.assign(form, {
    recordType: record.recordType, description: record.description || '',
    weight: record.weight, weightDate: record.weightDate || '',
    temperature: record.temperature,
    vaccineName: record.vaccineName || '', vaccineDate: record.vaccineDate || '',
    dentalResult: record.dentalResult || '', dentalDate: record.dentalDate || '',
    dentalCleanDate: record.dentalCleanDate || '',
    dietType: record.dietType || ''
  })
  showModal.value = true
}

async function saveRecord() {
  try {
    const data = { ...form, petId: selectedPetId.value }
    if (isEditing.value) {
      await updateHealthRecord(editingId.value, data)
      ElMessage.success('更新成功')
    } else {
      await addHealthRecord(data)
      ElMessage.success('添加成功')
    }
    showModal.value = false
    await loadRecords()
  } catch (e) { ElMessage.error('操作失败') }
}

async function handleDelete(id) {
  try {
    await ElMessageBox.confirm('确定删除该记录？', '提示', { type: 'warning' })
    await deleteHealthRecord(id)
    ElMessage.success('删除成功')
    await loadRecords()
  } catch (e) { /* 取消 */ }
}

onMounted(async () => {
  await loadPets()
})
</script>

<style lang="scss" scoped>
.health-management {
  animation: fadeInUp 0.5s ease;
}

.pet-select-bar {
  display: flex;
  gap: 16px;
  margin-bottom: 24px;
  align-items: center;
}

.pet-select {
  flex: 1;
  max-width: 300px;
  padding: 10px 16px;
  border: 1px solid rgba(255, 182, 193, 0.2);
  border-radius: 14px;
  font-size: 14px;
  color: #5D4E6D;
  background: var(--bg-card);
  outline: none;
  &:focus { border-color: var(--primary); }
}

.tabs-container {
  margin-bottom: 24px;
  overflow-x: auto;
}

.tabs {
  display: flex;
  gap: 8px;
}

.tab-btn {
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

.tab-icon { font-size: 16px; }

.record-card {
  margin-bottom: 16px;
  position: relative;
}

.record-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}

.record-type-badge {
  font-size: 12px;
  padding: 4px 12px;
  border-radius: 10px;
  font-weight: 500;
}

.type-health, .type-growth { background: rgba(76, 175, 80, 0.15); color: #4CAF50; }
.type-diet { background: rgba(255,152,0,0.15); color: #FF9800; }
.type-vaccine { background: rgba(74, 144, 217, 0.15); color: #4A90D9; }

.record-date { font-size: 13px; color: #C4B8D4; }

.record-body p { font-size: 14px; color: #5D4E6D; margin-bottom: 8px; }

.record-metrics {
  display: flex;
  gap: 16px;
  flex-wrap: wrap;
  font-size: 13px;
  color: #9B8FB5;
}

.record-actions {
  position: absolute;
  top: 16px;
  right: 16px;
  display: flex;
  gap: 8px;
  opacity: 0;
  transition: opacity 0.25s;
}

.record-card:hover .record-actions { opacity: 1; }

.action-btn {
  width: 32px; height: 32px;
  border-radius: 50%;
  border: none;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.25s;

  &.edit { background: rgba(201, 160, 80, 0.1); color: var(--primary); &:hover { background: rgba(201, 160, 80, 0.3); color: white; } }
  &.delete { background: rgba(229, 57, 53, 0.1); color: #E53935; &:hover { background: #EF5350; color: white; } }
}

// 表单
.record-form {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.form-group {
  display: flex;
  flex-direction: column;
  gap: 6px;
  flex: 1;
  label { font-size: 13px; color: #9B8FB5; }
}

.form-row { display: flex; gap: 12px; }

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