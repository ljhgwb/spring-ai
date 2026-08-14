<template>
  <div class="foster-appointment">
    <!-- 预约区 -->
    <h2 class="section-title">预约寄养</h2>
    <div class="appointment-card card">
      <div class="card-header">
        <span class="card-icon">🐾</span>
        <h3>选择宠物</h3>
        <button class="add-pet-btn" @click="showAddPetModal = true">+ 添加宠物</button>
      </div>
      <div class="pet-selector">
        <div v-for="pet in petList" :key="pet.id" class="pet-option" :class="{ selected: form.petId === pet.id }" @click="form.petId = pet.id">
          <div class="option-avatar" :style="{ background: getPetBg(pet.type) }">
            <span class="avatar-emoji">{{ getPetEmoji(pet.type) }}</span>
          </div>
          <div class="option-info">
            <span class="option-name">{{ pet.name }}</span>
            <span class="option-type">{{ pet.type }} · {{ pet.breed }}</span>
          </div>
          <el-icon v-if="form.petId === pet.id" class="check-icon"><Check /></el-icon>
        </div>
        <div v-if="petList.length === 0" class="empty-pet">
          <span class="empty-icon">🐾</span>
          <p>暂无宠物，请先添加</p>
        </div>
      </div>

      <div class="card-header">
        <span class="card-icon">📅</span>
        <h3>选择时间</h3>
      </div>
      <div class="form-row date-row">
        <div class="form-group">
          <label>入住日期</label>
          <input v-model="form.checkInDate" type="date" class="form-input" />
        </div>
        <div class="form-group">
          <label>离店日期</label>
          <input v-model="form.checkOutDate" type="date" class="form-input" />
        </div>
      </div>

      <div class="card-header">
        <span class="card-icon">🏠</span>
        <h3>选择房型</h3>
      </div>
      <div class="room-selector">
        <div v-for="room in rooms" :key="room.type" class="room-card" :class="{ selected: form.roomType === room.type }" @click="selectRoom(room)">
          <div class="room-header">
            <span class="room-icon">{{ room.icon }}</span>
            <span class="room-type">{{ room.type }}</span>
          </div>
          <p class="room-desc">{{ room.desc }}</p>
          <p class="room-price">¥{{ room.price }}/天</p>
          <el-icon v-if="form.roomType === room.type" class="room-check"><Check /></el-icon>
        </div>
      </div>

      <div class="card-header">
        <span class="card-icon">📝</span>
        <h3>特殊需求</h3>
      </div>
      <textarea v-model="form.specialRequirements" class="form-textarea" placeholder="请填写特殊需求（选填）..." rows="3"></textarea>

      <div class="price-summary" v-if="form.roomType && form.checkInDate && form.checkOutDate">
        <div class="price-row">
          <span>房型单价</span>
          <span>¥{{ selectedRoom?.price || 0 }}/天</span>
        </div>
        <div class="price-row">
          <span>寄养天数</span>
          <span>{{ fosterDays }} 天</span>
        </div>
        <div class="price-row total">
          <span>预估总价</span>
          <span class="total-price">¥{{ totalPrice }}</span>
        </div>
      </div>

      <button class="btn-primary submit-btn" @click="submitAppointment">
        <el-icon><Calendar /></el-icon>
        提交预约
      </button>
    </div>

    <!-- 我的预约 -->
    <h2 class="section-title" style="margin-top: 32px;">我的预约</h2>
    <div v-if="appointmentList.length === 0" class="empty-state">
      <span class="empty-icon">📋</span>
      <p>暂无预约记录</p>
    </div>
    <div v-for="appt in appointmentList" :key="appt.id" class="appointment-item card">
      <div class="appt-header">
        <span class="appt-status" :class="getStatusClass(appt.status)">{{ getStatusText(appt.status) }}</span>
        <span class="appt-date">{{ appt.checkInDate }} ~ {{ appt.checkOutDate }}</span>
      </div>
      <div class="appt-body">
        <div class="appt-info">
          <span class="info-label">房型</span><span>{{ appt.roomType }}</span>
        </div>
        <div class="appt-info">
          <span class="info-label">天数</span><span>{{ appt.duration }} 天</span>
        </div>
        <div class="appt-info">
          <span class="info-label">金额</span><span class="amount">¥{{ appt.totalAmount }}</span>
        </div>
      </div>
      <div class="appt-footer" v-if="appt.status === 'PENDING'">
        <button class="cancel-btn" @click="cancelAppt(appt.id)">取消预约</button>
      </div>
    </div>

    <!-- 添加宠物弹窗 -->
    <el-dialog title="添加宠物" v-model="showAddPetModal" width="480px">
      <el-form :model="petForm" label-width="80px">
        <el-form-item label="宠物名称">
          <el-input v-model="petForm.name" placeholder="请输入宠物名称" />
        </el-form-item>
        <el-form-item label="宠物类型">
          <el-select v-model="petForm.type">
            <el-option label="狗狗" value="狗狗" />
            <el-option label="猫咪" value="猫咪" />
            <el-option label="兔子" value="兔子" />
            <el-option label="仓鼠" value="仓鼠" />
            <el-option label="鹦鹉" value="鹦鹉" />
            <el-option label="金鱼" value="金鱼" />
            <el-option label="乌龟" value="乌龟" />
            <el-option label="蜥蜴" value="蜥蜴" />
          </el-select>
        </el-form-item>
        <el-form-item label="品种">
          <el-input v-model="petForm.breed" placeholder="请输入品种" />
        </el-form-item>
        <el-form-item label="性别">
          <el-select v-model="petForm.gender">
            <el-option label="公" value="公" />
            <el-option label="母" value="母" />
          </el-select>
        </el-form-item>
        <el-form-item label="年龄（月）">
          <el-input v-model.number="petForm.age" type="number" placeholder="请输入年龄" />
        </el-form-item>
        <el-form-item label="体重（kg）">
          <el-input v-model.number="petForm.weight" type="number" step="0.1" placeholder="请输入体重" />
        </el-form-item>
        <el-form-item label="性格标签">
          <el-input v-model="petForm.tags" placeholder="多个标签用逗号分隔" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showAddPetModal = false">取消</el-button>
        <el-button type="primary" @click="addPet">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElDialog, ElForm, ElFormItem, ElInput, ElSelect, ElOption, ElButton } from 'element-plus'
import { getMyPets, getMyAppointments, createAppointment, cancelAppointment, addPet as addPetApi } from '@/api'

const petList = ref([])
const appointmentList = ref([])
const selectedRoom = ref(null)
const showAddPetModal = ref(false)

const rooms = [
  { type: '标准间', icon: '🏠', price: 99, desc: '舒适温馨，基础护理' },
  { type: '豪华间', icon: '🏡', price: 199, desc: '宽敞明亮，高级护理' },
  { type: 'VIP套房', icon: '🏰', price: 299, desc: '尊享空间，全方位护理' }
]

const form = reactive({
  petId: null,
  checkInDate: '',
  checkOutDate: '',
  roomType: '',
  specialRequirements: ''
})

const petForm = reactive({
  name: '',
  type: '狗狗',
  breed: '',
  gender: '公',
  age: '',
  weight: '',
  tags: ''
})

const fosterDays = computed(() => {
  if (!form.checkInDate || !form.checkOutDate) return 0
  const d1 = new Date(form.checkInDate)
  const d2 = new Date(form.checkOutDate)
  return Math.max(0, Math.ceil((d2 - d1) / (1000 * 60 * 60 * 24)))
})

const totalPrice = computed(() => {
  return (selectedRoom.value?.price || 0) * fosterDays.value
})

function selectRoom(room) {
  form.roomType = room.type
  selectedRoom.value = room
}

function getPetEmoji(type) {
  const emojis = {
    '狗狗': '🐶',
    '猫咪': '🐱',
    '兔子': '🐰',
    '仓鼠': '🐹',
    '鹦鹉': '🦜',
    '金鱼': '🐠',
    '乌龟': '🐢',
    '蜥蜴': '🦎'
  }
  return emojis[type] || '🐾'
}

function getPetBg(type) {
  const bgs = {
    '狗狗': 'linear-gradient(135deg, #FFF5EE, #FFE4D6)',
    '猫咪': 'linear-gradient(135deg, #F5F0EB, #E8EDE5)',
    '兔子': 'linear-gradient(135deg, #FFF0F5, #FFE4EE)',
    '仓鼠': 'linear-gradient(135deg, #FFF8E1, #FFF0C8)',
    '鹦鹉': 'linear-gradient(135deg, #E8F5E9, #C8E6C9)',
    '金鱼': 'linear-gradient(135deg, #E3F2FD, #BBDEFB)',
    '乌龟': 'linear-gradient(135deg, #E8F5E9, #C5E1A5)',
    '蜥蜴': 'linear-gradient(135deg, #EFEBE9, #D7CCC8)'
  }
  return bgs[type] || 'linear-gradient(135deg, #F5F5F5, #EEEEEE)'
}

function getStatusClass(status) {
  return { 'PENDING': 'status-pending', 'CONFIRMED': 'status-confirmed', 'CANCELLED': 'status-cancelled', 'COMPLETED': 'status-completed' }[status] || ''
}

function getStatusText(status) {
  return { 'PENDING': '待确认', 'CONFIRMED': '已确认', 'CANCELLED': '已取消', 'COMPLETED': '已完成' }[status] || status
}

async function loadData() {
  try {
    const res = await getMyPets()
    petList.value = res.data || []
  } catch (e) { /* ignore */ }
  try {
    const res = await getMyAppointments()
    appointmentList.value = res.data || []
  } catch (e) { /* ignore */ }
}

async function submitAppointment() {
  if (!form.petId || !form.checkInDate || !form.checkOutDate || !form.roomType) {
    ElMessage.warning('请填写完整信息')
    return
  }
  if (fosterDays.value <= 0) {
    ElMessage.warning('离店日期必须晚于入住日期')
    return
  }
  try {
    await createAppointment(form)
    ElMessage.success('预约提交成功')
    form.petId = null; form.checkInDate = ''; form.checkOutDate = ''; form.roomType = ''; form.specialRequirements = ''
    selectedRoom.value = null
    await loadData()
  } catch (e) {
    ElMessage.error('预约失败')
  }
}

async function cancelAppt(id) {
  try {
    await cancelAppointment(id)
    ElMessage.success('取消成功')
    await loadData()
  } catch (e) {
    ElMessage.error('取消失败')
  }
}

async function addPet() {
  if (!petForm.name || !petForm.type) {
    ElMessage.warning('请填写宠物名称和类型')
    return
  }
  try {
    await addPetApi(petForm)
    ElMessage.success('添加成功')
    showAddPetModal.value = false
    petForm.name = ''; petForm.type = '狗狗'; petForm.breed = ''; petForm.gender = '公'; petForm.age = ''; petForm.weight = ''; petForm.tags = ''
    await loadData()
  } catch (e) {
    ElMessage.error('添加失败')
  }
}

onMounted(loadData)
</script>

<style lang="scss" scoped>
.foster-appointment {
  animation: fadeInUp 0.5s ease;
}

.appointment-card {
  padding: 28px;
}

.card-header {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 16px;
  margin-top: 8px;

  h3 { font-size: 16px; color: #5D4E6D; }
  .card-icon { font-size: 22px; }
  
  .add-pet-btn {
    margin-left: auto;
    padding: 6px 14px;
    font-size: 13px;
    border-radius: 20px;
    background: linear-gradient(135deg, #FFB6C1, #FF69B4);
    color: white;
    border: none;
    cursor: pointer;
    transition: all 0.2s;
    
    &:hover {
      transform: translateY(-1px);
      box-shadow: 0 2px 8px rgba(255, 105, 180, 0.3);
    }
  }
}

.pet-selector {
  display: flex;
  gap: 12px;
  flex-wrap: wrap;
  margin-bottom: 8px;
  
  .empty-pet {
    flex: 1;
    min-width: 100%;
    text-align: center;
    padding: 30px;
    background: #fafafa;
    border-radius: 14px;
    border: 2px dashed #ddd;
    
    .empty-icon {
      font-size: 36px;
      display: block;
      margin-bottom: 8px;
    }
    
    p {
      color: #999;
      font-size: 14px;
    }
  }
}

.pet-option {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 12px 16px;
  border: 2px solid rgba(255, 182, 193, 0.2);
  border-radius: 14px;
  cursor: pointer;
  transition: all 0.25s;
  flex: 1;
  min-width: 200px;

  &.selected {
    border-color: #FFB6C1;
    background: #FFE4E9;
  }
  &:hover { border-color: #FFB6C1; }
}

.option-avatar {
  width: 44px;
  height: 44px;
  border-radius: 50%;
  border: 2px solid #FFE4E9;
  display: flex;
  align-items: center;
  justify-content: center;
  
  .avatar-emoji {
    font-size: 24px;
  }
}

.option-name { font-size: 14px; font-weight: 600; color: #5D4E6D; }
.option-type { font-size: 12px; color: #9B8FB5; display: block; }

.check-icon { color: #FF91A4; font-size: 20px; }

.form-row {
  display: flex;
  gap: 16px;
  margin-bottom: 8px;
}

.form-group {
  flex: 1;
  label { display: block; font-size: 13px; color: #9B8FB5; margin-bottom: 6px; }
}

.form-input {
  width: 100%;
  padding: 10px 14px;
  border: 1px solid rgba(255, 182, 193, 0.2);
  border-radius: 12px;
  font-size: 14px;
  color: #5D4E6D;
  outline: none;
  background: #FFF8F5;
  &:focus { border-color: #FFB6C1; }
}

.room-selector {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 12px;
  margin-bottom: 8px;

  @media (max-width: 640px) { grid-template-columns: 1fr; }
}

.room-card {
  padding: 16px;
  border: 2px solid rgba(255, 182, 193, 0.2);
  border-radius: 14px;
  cursor: pointer;
  text-align: center;
  transition: all 0.25s;
  position: relative;

  &.selected {
    border-color: #FFB6C1;
    background: #FFE4E9;
  }
  &:hover { border-color: #FFB6C1; transform: translateY(-2px); }
}

.room-icon { font-size: 32px; display: block; margin-bottom: 8px; }
.room-type { font-size: 15px; font-weight: 600; color: #5D4E6D; }
.room-desc { font-size: 12px; color: #9B8FB5; margin: 4px 0; }
.room-price { font-size: 18px; font-weight: 700; color: #FF91A4; }

.room-check { position: absolute; top: 8px; right: 8px; color: #FF91A4; font-size: 18px; }

.form-textarea {
  width: 100%;
  padding: 12px;
  border: 1px solid rgba(255, 182, 193, 0.2);
  border-radius: 12px;
  font-size: 14px;
  color: #5D4E6D;
  outline: none;
  resize: vertical;
  background: #FFF8F5;
  &:focus { border-color: #FFB6C1; }
}

.price-summary {
  margin: 16px 0;
  padding: 16px;
  background: #FFF8F5;
  border-radius: 14px;
}

.price-row {
  display: flex;
  justify-content: space-between;
  padding: 6px 0;
  font-size: 14px;
  color: #9B8FB5;

  &.total { border-top: 1px dashed rgba(255, 182, 193, 0.3); margin-top: 8px; padding-top: 12px; }
}

.total-price {
  font-size: 22px;
  font-weight: 700;
  color: #FF91A4;
}

.submit-btn {
  width: 100%;
  margin-top: 16px;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  padding: 14px;
  font-size: 16px;
}

// 预约列表
.appointment-item {
  margin-bottom: 16px;
}

.appt-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}

.appt-status {
  font-size: 13px;
  padding: 4px 12px;
  border-radius: 12px;
  font-weight: 500;
}

.status-pending { background: #FFF3E0; color: #F57C00; }
.status-confirmed { background: #E8F5E9; color: #388E3C; }
.status-cancelled { background: #FFEBEE; color: #D32F2F; }
.status-completed { background: #E3F2FD; color: #1976D2; }

.appt-date { font-size: 13px; color: #9B8FB5; }

.appt-body {
  display: flex;
  gap: 24px;
  margin-bottom: 12px;
}

.appt-info {
  .info-label { font-size: 12px; color: #C4B8D4; margin-right: 6px; }
  span:last-child { font-size: 14px; color: #5D4E6D; }
  .amount { color: #FF91A4; font-weight: 600; }
}

.appt-footer {
  text-align: right;
}

.cancel-btn {
  padding: 6px 16px;
  border: 1px solid rgba(255, 182, 193, 0.3);
  border-radius: 20px;
  background: white;
  color: #E57373;
  cursor: pointer;
  font-size: 13px;
  transition: all 0.25s;
  &:hover { background: #FFEBEE; }
}
</style>