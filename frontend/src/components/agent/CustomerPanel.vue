<template>
  <div class="customer-panel">
    <template v-if="task">
      <div class="customer-360 custom-scrollbar">
        <section v-if="showSection('ai-copilot')" class="info-card copilot-card">
          <div class="section-title">
            <div class="copilot-icon">
              <el-icon size="12" color="#fff"><Lightning /></el-icon>
            </div>
            <span>AI Copilot</span>
          </div>
          <div class="copilot-grid">
            <div class="mini-card">
              <p class="mini-label">意图摘要</p>
              <p class="mini-text">{{ task.summary }}</p>
            </div>
            <div class="mini-card">
              <p class="mini-label">处理建议</p>
              <p class="mini-text">{{ task.aiSuggestion }}</p>
            </div>
          </div>
          <div class="tags-row">
            <el-tag v-for="tag in taskTags" :key="tag" size="small" type="success" effect="plain">
              #{{ tag }}
            </el-tag>
          </div>
        </section>

        <section v-if="showSection('stage-timeline')" class="info-card">
          <div class="section-title">
            <el-icon size="12" color="#00a758"><Connection /></el-icon>
            <span>阶段轨迹</span>
          </div>
          <div class="timeline-list">
            <div
              v-for="stage in workflowStages"
              :key="stage.code"
              class="timeline-item"
              :class="{
                active: stage.code === task.currentStageCode,
                done: stage.stageOrder < task.currentStageOrder,
              }"
            >
              <div class="timeline-dot">{{ stage.stageOrder }}</div>
              <div class="timeline-body">
                <p class="timeline-name">{{ stage.name }}</p>
                <p class="timeline-role">{{ stage.roleLabel }}</p>
              </div>
            </div>
          </div>
        </section>

        <section v-if="showSection('knowledge-card')" class="info-card">
          <div class="section-title">
            <el-icon size="12" color="#f59e0b"><Collection /></el-icon>
            <span>知识卡片</span>
          </div>
          <p class="knowledge-text">{{ knowledgeText }}</p>
        </section>

        <section v-if="showSection('customer-profile')" class="info-card">
          <div class="section-title">
            <el-icon size="12" color="#00a758"><User /></el-icon>
            <span>客户画像</span>
          </div>
          <template v-if="customer">
            <div class="customer-card">
              <el-avatar :size="36" :src="`https://picsum.photos/seed/${customer.id}/100/100`" />
              <div class="customer-info">
                <div class="customer-name-row">
                  <span class="customer-name">{{ customer.name }}</span>
                  <el-tag size="small" type="warning" effect="plain">{{ customer.level }}</el-tag>
                </div>
                <span class="customer-phone">{{ customer.phone }}</span>
              </div>
            </div>
            <div class="agent-card">
              <div class="agent-avatar">
                <el-icon size="14" color="#ffd700"><UserFilled /></el-icon>
              </div>
              <div class="agent-info">
                <p class="agent-name">{{ customer.agent.name }}</p>
                <p class="agent-sub">{{ customer.agent.branch }} · 工号 {{ customer.agent.code }}</p>
              </div>
            </div>
          </template>
          <div v-else class="empty-block">暂无客户信息</div>
        </section>

        <section v-if="showSection('policy-list')" class="info-card">
          <div class="section-title">
            <el-icon size="12" color="#00a758"><Tickets /></el-icon>
            <span>保单信息</span>
          </div>
          <template v-if="customer?.policies?.length">
            <div class="policy-grid">
              <div v-for="policy in customer.policies" :key="policy.id" class="policy-card">
                <p class="policy-name">{{ policy.name }}</p>
                <p class="policy-id">{{ policy.id }}</p>
                <div class="policy-footer">
                  <span class="policy-premium">¥{{ Number(policy.premium).toLocaleString() }}</span>
                  <el-tag size="small" type="success" effect="plain">{{ policy.status }}</el-tag>
                </div>
              </div>
            </div>
          </template>
          <div v-else class="empty-block">暂无保单信息</div>
        </section>

        <section v-if="showSection('history-records')" class="info-card">
          <div class="section-title">
            <el-icon size="12" color="#8e90a2"><Clock /></el-icon>
            <span>历史记录</span>
          </div>
          <template v-if="historyItems.length">
            <div v-for="item in historyItems" :key="item.id" class="history-card">
              <div class="history-meta">
                <span class="history-type">{{ item.type }}</span>
                <span class="history-date">{{ item.date }}</span>
              </div>
              <p class="history-summary">{{ item.summary }}</p>
              <el-tag size="small" effect="plain">{{ item.status }}</el-tag>
            </div>
          </template>
          <div v-else class="empty-block">暂无历史记录</div>
        </section>

        <section v-if="showSection('fault-report-entry')" class="info-card danger-card">
          <div class="section-title">
            <el-icon size="12" color="#ec6453"><WarnTriangleFilled /></el-icon>
            <span>报障入口</span>
          </div>
          <button class="fault-btn" @click="openFaultDialog">
            <el-icon size="24" color="#ec6453"><WarnTriangleFilled /></el-icon>
            <span>宏e站一键报障</span>
          </button>
        </section>
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
import { computed, ref, watch } from 'vue'
import {
  Clock,
  Collection,
  Connection,
  Lightning,
  Tickets,
  User,
  UserFilled,
  WarnTriangleFilled,
} from '@element-plus/icons-vue'
import { getCustomer } from '@/api/customer'
import FaultReportDialog from './FaultReportDialog.vue'
import type { AgentWorkflowStage, Customer, ServiceTask } from '@/types'

const props = defineProps<{
  task: ServiceTask | null
  visibleSections?: string[]
  workflowStages?: AgentWorkflowStage[]
}>()

const emit = defineEmits<{
  faultReported: [taskId: string]
}>()

const dialogVisible = ref(false)
const customer = ref<Customer | null>(null)

const STATIC_HISTORY: Record<string, Array<{ id: string; date: string; type: string; summary: string; status: string }>> = {
  'CUST-882': [
    {
      id: 'SVC-2026-015',
      date: '2026-02-28',
      type: '保全服务',
      summary: '客户申请变更受益人信息，已完成身份核验并提交变更申请。',
      status: '已完成',
    },
    {
      id: 'SVC-2025-098',
      date: '2025-11-15',
      type: '理赔咨询',
      summary: '就意外伤害险理赔流程进行咨询，已提供材料清单及指引。',
      status: '已完成',
    },
  ],
}

watch(
  () => props.task?.customerId,
  async customerId => {
    if (!customerId) {
      customer.value = null
      return
    }
    try {
      customer.value = await getCustomer(customerId)
    } catch (error) {
      console.warn('[CustomerPanel] 加载客户信息失败', error)
      customer.value = null
    }
  },
  { immediate: true },
)

const workflowStages = computed(() => props.workflowStages ?? [])
const historyItems = computed(() => {
  if (!props.task?.customerId) return []
  return STATIC_HISTORY[props.task.customerId] ?? []
})
const taskTags = computed(() => {
  if (!props.task) return []
  const tags = [...(props.task.tags ?? [])]
  if (!tags.length) tags.push(props.task.category)
  return tags
})
const knowledgeText = computed(() => {
  if (!props.task) return '暂无知识推荐'
  if (props.task.workflowCode === 'system-fault-flow') {
    return '优先核对报错时间、影响范围、复现路径和关联系统，再判断是否进入三线技术支持。'
  }
  return '优先结合客户等级、保单状态和历史服务记录，使用标准合规话术完成答复。'
})

function showSection(sectionCode: string) {
  if (!props.visibleSections?.length) return true
  return props.visibleSections.includes(sectionCode)
}

function openFaultDialog() {
  dialogVisible.value = true
}

function handleFaultSubmitted(taskId: string) {
  emit('faultReported', taskId)
}
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

.customer-360 {
  flex: 1;
  overflow-y: auto;
  padding: 12px;
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.panel-empty,
.empty-block {
  display: flex;
  align-items: center;
  justify-content: center;
  min-height: 88px;
  font-size: 13px;
  color: var(--desc-color);
}

.info-card {
  border: 1px solid var(--blod-border-color);
  border-radius: 12px;
  background-color: #fff;
  padding: 12px;
}

.danger-card {
  border-color: rgba(236, 100, 83, 0.25);
  background: linear-gradient(180deg, rgba(236, 100, 83, 0.04), rgba(236, 100, 83, 0.01));
}

.copilot-card {
  background: linear-gradient(180deg, rgba(0, 167, 88, 0.05), rgba(0, 167, 88, 0.01));
}

.section-title {
  display: flex;
  align-items: center;
  gap: 6px;
  margin-bottom: 10px;
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

.copilot-grid,
.policy-grid {
  display: grid;
  gap: 8px;
}

.copilot-grid {
  grid-template-columns: 1fr 1fr;
}

.mini-card,
.policy-card,
.history-card {
  background-color: #fff;
  border: 1px solid var(--blod-border-color);
  border-radius: 8px;
  padding: 10px;
}

.mini-label,
.timeline-role,
.policy-id,
.history-date,
.customer-phone,
.agent-sub {
  font-size: 11px;
  color: var(--label-desc-color);
}

.mini-text,
.knowledge-text,
.history-summary,
.policy-name {
  font-size: 12px;
  line-height: 1.6;
  color: var(--text-color);
}

.tags-row {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
  margin-top: 10px;
}

.timeline-list {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.timeline-item {
  display: flex;
  gap: 10px;
  opacity: 0.6;

  &.active {
    opacity: 1;
  }

  &.done .timeline-dot {
    background-color: var(--primary-color);
    color: #fff;
  }
}

.timeline-dot {
  width: 22px;
  height: 22px;
  border-radius: 50%;
  border: 1px solid var(--blod-border-color);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 11px;
  font-weight: 700;
}

.timeline-name,
.customer-name,
.agent-name,
.history-type {
  font-size: 13px;
  font-weight: 700;
  color: var(--text-color);
}

.customer-card,
.agent-card {
  display: flex;
  align-items: center;
  gap: 10px;
}

.customer-name-row {
  display: flex;
  align-items: center;
  gap: 6px;
  margin-bottom: 4px;
}

.customer-info,
.agent-info {
  min-width: 0;
}

.agent-avatar {
  width: 32px;
  height: 32px;
  border-radius: 50%;
  background-color: rgba(255, 215, 0, 0.12);
  display: flex;
  align-items: center;
  justify-content: center;
}

.policy-footer,
.history-meta {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 8px;
  margin-top: 8px;
}

.fault-btn {
  width: 100%;
  border: 1px dashed rgba(236, 100, 83, 0.45);
  border-radius: 12px;
  padding: 14px;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 10px;
  font-size: 14px;
  font-weight: 700;
  color: #ec6453;
  background-color: rgba(236, 100, 83, 0.04);
}
</style>
