<template>
  <div class="customer-panel">
    <template v-if="task">
      <!-- AI Copilot -->
      <div class="copilot-section">
        <div class="section-title">
          <div class="copilot-icon">
            <el-icon size="12" color="#fff"><Lightning /></el-icon>
          </div>
          <span>AI 辅助决策 (Copilot)</span>
        </div>

        <div class="copilot-grid">
          <div class="copilot-card">
            <p class="copilot-label">意图摘要</p>
            <p class="copilot-text">{{ task.summary }}</p>
          </div>
          <div class="copilot-card">
            <p class="copilot-label">处理建议</p>
            <p class="copilot-text">{{ task.aiSuggestion }}</p>
          </div>
        </div>

        <div class="script-block">
          <p class="copilot-label">建议话术</p>
          <p class="script-text">
            "您好张先生，经我司精算团队评估，您保单因体检血压指标略高于标准范围，按规则需做费率调整，加费幅度为标准费率的15%，以保障保单长期有效。如有疑问可为您进一步说明精算依据。"
          </p>
        </div>

        <div class="tags-row">
          <el-tag v-for="tag in taskTags" :key="tag" size="small" type="success" effect="plain">
            #{{ tag }}
          </el-tag>
        </div>
      </div>

      <!-- Customer 360 -->
      <div class="customer-360 custom-scrollbar">
        <div class="customer-360-header">
          <div class="header-left">
            <el-icon size="12" color="#00a758"><User /></el-icon>
            <span>客户全景视图 (360°)</span>
          </div>
          <el-button text size="small" type="primary">
            详情 <el-icon><ArrowRight /></el-icon>
          </el-button>
        </div>

        <template v-if="customer">
        <!-- 客户基本信息 -->
        <div class="customer-card">
          <el-avatar :size="32" :src="`https://picsum.photos/seed/${customer.id}/100/100`" />
          <div class="customer-info">
            <div class="customer-name-row">
              <span class="customer-name">{{ customer.name }}</span>
              <el-tag size="small" type="warning" effect="plain">{{ customer.level }}</el-tag>
            </div>
            <span class="customer-phone">{{ customer.phone }}</span>
          </div>
        </div>

        <!-- 持有保单 -->
        <div class="info-section">
          <p class="info-section-label">持有保单 ({{ customer.policies.length }})</p>
          <div class="policy-grid">
            <div
              v-for="policy in customer.policies"
              :key="policy.id"
              class="policy-card"
            >
              <p class="policy-name">{{ policy.name }}</p>
              <p class="policy-id">{{ policy.id }}</p>
              <div class="policy-footer">
                <span class="policy-premium">¥{{ policy.premium.toLocaleString() }}</span>
                <el-tag size="small" type="success" effect="plain">有效</el-tag>
              </div>
            </div>
          </div>
        </div>

        <!-- 理赔 & 保全记录 -->
        <div class="info-section">
          <div class="record-grid-header">
            <p class="info-section-label">既往理赔记录</p>
            <p class="info-section-label">近期保全记录</p>
          </div>
          <div class="record-grid">
            <template v-for="(claim, i) in customer.claims" :key="claim.id">
              <div class="record-card">
                <div class="record-row">
                  <el-icon size="10" color="#3b82f6"><Wallet /></el-icon>
                  <span class="record-type">{{ claim.type }}</span>
                </div>
                <p class="record-date">{{ claim.date }}</p>
                <div class="record-footer">
                  <span class="record-amount">¥{{ claim.amount.toLocaleString() }}</span>
                  <el-tag size="small" type="success" effect="plain">{{ claim.status }}</el-tag>
                </div>
              </div>
              <div v-if="customer.policyServices[i]" class="record-card">
                <div class="record-row">
                  <el-icon size="10" color="#f59e0b"><Document /></el-icon>
                  <span class="record-type">{{ customer.policyServices[i].type }}</span>
                </div>
                <p class="record-date">{{ customer.policyServices[i].date }}</p>
                <el-tag size="small" type="success" effect="plain">{{ customer.policyServices[i].status }}</el-tag>
              </div>
              <div v-else />
            </template>
          </div>
        </div>

        <!-- 代理人信息 -->
        <div class="agent-card">
          <div class="agent-avatar">
            <el-icon size="14" color="#ffd700"><User /></el-icon>
          </div>
          <div class="agent-info">
            <p class="agent-name">{{ customer.agent.name }}</p>
            <p class="agent-sub">{{ customer.agent.branch }} · 工号 {{ customer.agent.code }}</p>
          </div>
          <el-button size="small" type="info" plain>联系</el-button>
        </div>

        <!-- 历史服务记录 & 故障报障 -->
        <div class="bottom-grid">
          <div class="history-col">
            <div class="history-header">
              <el-icon size="12" color="#8e90a2"><Clock /></el-icon>
              <p class="info-section-label">历史服务记录 ({{ customer.serviceHistory.length }})</p>
            </div>
            <div
              v-for="item in customer.serviceHistory.slice(0, 2)"
              :key="item.id"
              class="history-card"
            >
              <div class="history-meta">
                <span class="history-type">{{ item.type }}</span>
                <span class="history-date">{{ item.date }}</span>
              </div>
              <p class="history-summary">{{ item.summary }}</p>
              <el-tag
                :type="item.status === '已关闭' ? 'info' : 'success'"
                size="small"
                effect="plain"
              >
                {{ item.status }}
              </el-tag>
            </div>
          </div>

          <button class="fault-btn" @click="openFaultDialog">
            <el-icon size="32" color="#ec6453"><WarnTriangleFilled /></el-icon>
            <span>生产故障报障</span>
          </button>
        </div>
        </template>
        <div v-else class="customer-empty">
          <p>该任务暂无关联客户信息</p>
        </div>
      </div>
    </template>

    <div v-else class="panel-empty">
      <p>选择任务后显示客户信息</p>
    </div>

    <FaultReportDialog
      v-model="dialogVisible"
      :task="task"
      :customer-name="customer?.name ?? ''"
      @submitted="handleFaultSubmitted"
    />
  </div>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'
import {
  Lightning,
  User,
  ArrowRight,
  Wallet,
  Document,
  Clock,
  WarnTriangleFilled,
} from '@element-plus/icons-vue'
import type { ServiceTask } from '@/types'
import FaultReportDialog from './FaultReportDialog.vue'

const props = defineProps<{ task: ServiceTask | null }>()

const emit = defineEmits<{
  faultReported: [taskId: string]
}>()

const dialogVisible = ref(false)

function openFaultDialog() {
  dialogVisible.value = true
}

function handleFaultSubmitted(taskId: string) {
  emit('faultReported', taskId)
}

interface MockCustomer {
  id: string
  name: string
  phone: string
  level: string
  policies: { id: string; name: string; status: string; premium: number; startDate: string }[]
  agent: { id: string; name: string; code: string; phone: string; branch: string }
  claims: { id: string; type: string; date: string; status: string; amount: number }[]
  policyServices: { id: string; type: string; date: string; status: string }[]
  serviceHistory: { id: string; date: string; type: string; summary: string; status: string }[]
}

const MOCK_CUSTOMERS: Record<string, MockCustomer> = {
  'CUST-882': {
    id: 'CUST-882',
    name: '张伟',
    phone: '138****8829',
    level: 'Gold',
    policies: [
      { id: 'POL-101', name: '宏掌门终身寿险', status: 'Active', premium: 12000, startDate: '2023-05-12' },
      { id: 'POL-102', name: '宏掌门意外伤害保险', status: 'Active', premium: 500, startDate: '2024-01-01' },
    ],
    agent: { id: 'AG-99', name: '李芳', code: '880012', phone: '139****1122', branch: '上海分公司' },
    claims: [
      { id: 'CLM-2024-001', type: '意外伤害理赔', date: '2024-08-15', status: '已结案', amount: 8500 },
      { id: 'CLM-2023-007', type: '住院医疗理赔', date: '2023-11-20', status: '已结案', amount: 23600 },
    ],
    policyServices: [
      { id: 'SRV-2025-003', type: '受益人变更', date: '2025-03-01', status: '已完成' },
      { id: 'SRV-2024-012', type: '地址信息更新', date: '2024-12-10', status: '已完成' },
    ],
    serviceHistory: [
      { id: 'SVC-2026-015', date: '2026-02-28', type: '保全服务', summary: '客户申请变更受益人信息，已完成身份核验并提交变更申请，次日生效。', status: '已完成' },
      { id: 'SVC-2025-098', date: '2025-11-15', type: '理赔咨询', summary: '就意外伤害险理赔流程进行咨询，提供完整材料清单及线上提交指引，客户满意。', status: '已完成' },
    ],
  },
}

// 根据当前任务的 customerId 动态查找客户数据
const customer = computed(() => {
  if (!props.task?.customerId) return null
  return MOCK_CUSTOMERS[props.task.customerId] ?? null
})

// 标签从任务 category 和优先级派生
const taskTags = computed(() => {
  if (!props.task) return []
  const tags = [props.task.category]
  if (props.task.priority === 'High' || props.task.priority === 'Urgent') tags.push('高优')
  return tags
})
</script>

<style lang="scss" scoped>
.customer-panel {
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
  background-color: #fff;
  overflow: hidden;
}

.panel-empty,
.customer-empty {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 13px;
  color: var(--desc-color);
}

// AI Copilot
.copilot-section {
  flex-shrink: 0;
  padding: 12px;
  border-bottom: 1px solid var(--blod-border-color);
  background-color: rgba(0, 167, 88, 0.03);
}

.section-title {
  display: flex;
  align-items: center;
  gap: 6px;
  margin-bottom: 8px;
  font-size: 12px;
  font-weight: 700;
  color: var(--text-color);
}

.copilot-icon {
  width: 20px;
  height: 20px;
  border-radius: 4px;
  background-color: var(--primary-color);
  display: flex;
  align-items: center;
  justify-content: center;
}

.copilot-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 8px;
  margin-bottom: 8px;
}

.copilot-card {
  background-color: #fff;
  border: 1px solid rgba(0, 167, 88, 0.15);
  border-radius: 8px;
  padding: 10px;
  min-height: 72px;
}

.copilot-label {
  font-size: 10px;
  font-weight: 700;
  color: var(--primary-color);
  text-transform: uppercase;
  letter-spacing: 0.05em;
  margin-bottom: 4px;
}

.copilot-text {
  font-size: 12px;
  color: var(--text-color);
  line-height: 1.5;
}

.script-block {
  background-color: rgba(0, 167, 88, 0.05);
  border-left: 2px solid var(--primary-color);
  border-radius: 4px;
  padding: 10px 12px;
  margin-bottom: 8px;
}

.script-text {
  font-size: 12px;
  color: var(--text-color);
  line-height: 1.6;
  font-style: italic;
}

.tags-row {
  display: flex;
  flex-wrap: wrap;
  gap: 4px;
}

// Customer 360
.customer-360 {
  flex: 1;
  overflow-y: auto;
  padding: 8px;
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.customer-360-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 12px;
  font-weight: 700;
  color: var(--text-color);
}

.customer-card {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 8px;
  background-color: var(--gray-color);
  border-radius: 12px;
  border: 1px solid var(--blod-border-color);
}

.customer-info {
  flex: 1;
  min-width: 0;
}

.customer-name-row {
  display: flex;
  align-items: center;
  gap: 6px;
  margin-bottom: 2px;
}

.customer-name {
  font-size: 14px;
  font-weight: 700;
  color: var(--text-color);
}

.customer-phone {
  font-size: 11px;
  color: var(--label-desc-color);
}

// Info sections
.info-section {
  flex-shrink: 0;
}

.info-section-label {
  font-size: 10px;
  font-weight: 700;
  color: var(--label-desc-color);
  text-transform: uppercase;
  letter-spacing: 0.05em;
  margin-bottom: 4px;
}

.policy-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 4px;
}

.policy-card {
  padding: 6px;
  background-color: #fff;
  border: 1px solid var(--border-color);
  border-radius: 8px;
  cursor: pointer;
  transition: border-color 0.15s;

  &:hover {
    border-color: rgba(0, 167, 88, 0.4);
  }
}

.policy-name {
  font-size: 11px;
  font-weight: 700;
  color: var(--text-color);
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  margin-bottom: 2px;
}

.policy-id {
  font-size: 10px;
  color: var(--label-desc-color);
  margin-bottom: 4px;
}

.policy-footer {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.policy-premium {
  font-size: 12px;
  font-weight: 700;
  color: var(--text-color);
}

.record-grid-header {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 4px;
  margin-bottom: 4px;
}

.record-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 4px;
}

.record-card {
  padding: 6px;
  background-color: #fff;
  border: 1px solid var(--border-color);
  border-radius: 8px;
}

.record-row {
  display: flex;
  align-items: center;
  gap: 4px;
  margin-bottom: 2px;
}

.record-type {
  font-size: 11px;
  font-weight: 700;
  color: var(--text-color);
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.record-date {
  font-size: 10px;
  color: var(--label-desc-color);
  margin-bottom: 4px;
}

.record-footer {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.record-amount {
  font-size: 11px;
  font-weight: 700;
  color: var(--text-color);
}

// Agent card
.agent-card {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 8px;
  background-color: var(--text-color);
  border-radius: 12px;
  color: #fff;
  position: relative;
  overflow: hidden;
}

.agent-avatar {
  width: 28px;
  height: 28px;
  border-radius: 50%;
  background-color: rgba(255, 255, 255, 0.1);
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.agent-info {
  flex: 1;
  min-width: 0;
}

.agent-name {
  font-size: 12px;
  font-weight: 700;
  line-height: 1.2;
}

.agent-sub {
  font-size: 10px;
  opacity: 0.6;
  margin-top: 2px;
}

// Bottom section
.bottom-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 6px;
  flex: 1;
}

.history-col {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.history-header {
  display: flex;
  align-items: center;
  gap: 6px;
}

.history-card {
  padding: 6px;
  background-color: #fff;
  border: 1px solid var(--border-color);
  border-radius: 8px;
}

.history-meta {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 2px;
}

.history-type {
  font-size: 10px;
  font-weight: 700;
  color: var(--primary-color);
}

.history-date {
  font-size: 10px;
  color: var(--label-desc-color);
}

.history-summary {
  font-size: 11px;
  color: var(--text-color);
  line-height: 1.4;
  display: -webkit-box;
  -webkit-line-clamp: 3;
  -webkit-box-orient: vertical;
  overflow: hidden;
  margin-bottom: 4px;
}

.fault-btn {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 8px;
  background-color: rgba(236, 100, 83, 0.1);
  border: 1px solid rgba(236, 100, 83, 0.2);
  border-radius: 12px;
  color: var(--danger-color);
  font-size: 12px;
  font-weight: 700;
  cursor: pointer;
  transition: background-color 0.15s;
  width: 100%;

  &:hover {
    background-color: rgba(236, 100, 83, 0.2);
  }
}
</style>
