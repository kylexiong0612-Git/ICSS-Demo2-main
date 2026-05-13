<template>
  <div class="main-container">
    <div class="main-breadcrumb">
      <el-breadcrumb separator="/">
        <el-breadcrumb-item>坐席工作台</el-breadcrumb-item>
        <el-breadcrumb-item>{{ workflowName }}</el-breadcrumb-item>
        <el-breadcrumb-item>{{ stageName }}</el-breadcrumb-item>
      </el-breadcrumb>
    </div>

    <div v-loading="isBooting" class="main-area workstation-area">
      <div
        v-for="region in regions"
        :key="region.code"
        class="workbench-region"
        :class="`region-${region.code}`"
        :style="regionStyle(region)"
      >
        <TaskList
          v-if="hasTaskList(region)"
          :workflow-code="workflowCode"
          :stage-code="stageCode"
          :selected-task-id="selectedTask?.id"
          :show-public-pool="regionHasWidget(region, 'task-pool')"
          :show-personal-pool="regionHasWidget(region, 'my-task-list')"
          @select="handleSelectTask"
          @grab="handleGrabTask"
        />

        <ChatPanel
          v-if="regionHasWidget(region, 'chat-panel')"
          :task="selectedTask"
          :stage-label="stageName"
          :next-stage-label="nextStageName"
          @update-task="handleLocalTaskUpdate"
          @advance="handleAdvanceTask"
          @complete="handleCompleteTask"
          @grab="handleGrabTask"
        />

        <CustomerPanel
          v-if="customerSections(region).length > 0"
          :task="selectedTask"
          :visible-sections="customerSections(region)"
          :workflow-stages="workflowStages"
          @fault-reported="handleFaultReported"
        />
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed, ref, watch } from 'vue'
import { getChatHistory } from '@/api/chat'
import { getAgentWorkflow, getEffectiveWorkbenchLayout } from '@/api/workflow'
import TaskList from '@/components/agent/TaskList.vue'
import ChatPanel from '@/components/agent/ChatPanel.vue'
import CustomerPanel from '@/components/agent/CustomerPanel.vue'
import { useTaskStore } from '@/stores/taskStore'
import type {
  AgentWorkflowStage,
  AgentWorkflowTemplate,
  Message,
  ServiceTask,
  WorkbenchLayoutConfig,
  WorkbenchRegion,
} from '@/types'

const props = defineProps<{
  workflowCode: string
  stageCode: string
}>()

const taskStore = useTaskStore()
const isBooting = ref(false)
const workflow = ref<AgentWorkflowTemplate | null>(null)
const layout = ref<WorkbenchLayoutConfig | null>(null)
const selectedTask = ref<ServiceTask | null>(null)

const workflowStages = computed(() => workflow.value?.stages ?? [])
const workflowName = computed(() => workflow.value?.name ?? props.workflowCode)
const stageName = computed(
  () => workflowStages.value.find(item => item.code === props.stageCode)?.name ?? props.stageCode,
)
const currentStage = computed(() =>
  workflowStages.value.find(item => item.code === props.stageCode) ?? null,
)
const nextStage = computed(() =>
  workflowStages.value.find(
    item => item.stageOrder === (currentStage.value?.stageOrder ?? 0) + 1,
  ) ?? null,
)
const nextStageName = computed(() => nextStage.value?.name)

const fallbackRegions: WorkbenchRegion[] = [
  {
    code: 'left',
    width: '280px',
    widgets: [
      { code: 'task-pool', type: 'task-pool', visible: true, order: 1 },
      { code: 'my-task-list', type: 'my-task-list', visible: true, order: 2 },
    ],
  },
  {
    code: 'center',
    widgets: [{ code: 'chat-panel', type: 'chat-panel', visible: true, order: 1 }],
  },
  {
    code: 'right',
    width: '380px',
    widgets: [
      { code: 'ai-copilot', type: 'ai-copilot', visible: true, order: 1 },
      { code: 'customer-profile', type: 'customer-profile', visible: true, order: 2 },
      { code: 'policy-list', type: 'policy-list', visible: true, order: 3 },
      { code: 'history-records', type: 'history-records', visible: true, order: 4 },
      { code: 'fault-report-entry', type: 'fault-report-entry', visible: true, order: 5 },
    ],
  },
]

const regions = computed(() => {
  const source = Array.isArray(layout.value?.regions) ? layout.value!.regions : fallbackRegions
  return [...source]
    .map(region => ({
      ...region,
      widgets: [...region.widgets].filter(widget => widget.visible).sort((a, b) => a.order - b.order),
    }))
    .sort((left, right) => regionOrder(left.code) - regionOrder(right.code))
})

watch(
  () => [props.workflowCode, props.stageCode],
  async () => {
    await loadStageData()
  },
  { immediate: true },
)

watch(
  () => taskStore.tasks,
  tasks => {
    if (!selectedTask.value) return
    const updated = tasks.find(task => task.id === selectedTask.value?.id)
    if (updated) {
      selectedTask.value = {
        ...updated,
        chatHistory: selectedTask.value.chatHistory,
      }
    }
  },
  { deep: true },
)

async function loadStageData() {
  isBooting.value = true
  try {
    const [workflowData, layoutData] = await Promise.all([
      getAgentWorkflow(props.workflowCode),
      getEffectiveWorkbenchLayout(props.workflowCode, props.stageCode),
      taskStore.refreshStageTasks(props.workflowCode, props.stageCode),
    ])
    workflow.value = workflowData
    layout.value = layoutData
    selectedTask.value = null
  } finally {
    isBooting.value = false
  }
}

function regionOrder(code: WorkbenchRegion['code']) {
  return { top: 0, left: 1, center: 2, right: 3, bottom: 4 }[code] ?? 99
}

function regionStyle(region: WorkbenchRegion) {
  if (region.code === 'left' || region.code === 'right') {
    return { width: region.width ?? '320px' }
  }
  if (region.code === 'top' || region.code === 'bottom') {
    return { width: '100%' }
  }
  return { flex: 1 }
}

function regionHasWidget(region: WorkbenchRegion, widgetType: string) {
  return region.widgets.some(widget => widget.type === widgetType)
}

function hasTaskList(region: WorkbenchRegion) {
  return regionHasWidget(region, 'task-pool') || regionHasWidget(region, 'my-task-list')
}

function customerSections(region: WorkbenchRegion) {
  return region.widgets
    .map(widget => widget.type)
    .filter(type =>
      [
        'ai-copilot',
        'customer-profile',
        'policy-list',
        'history-records',
        'fault-report-entry',
        'stage-timeline',
        'knowledge-card',
      ].includes(type),
    )
}

async function handleSelectTask(task: ServiceTask) {
  taskStore.markRead(task.id)
  const history = task.customerId ? await getChatHistory(task.customerId) : []
  selectedTask.value = {
    ...task,
    chatHistory: history,
  }
}

async function handleGrabTask(taskId: string) {
  await taskStore.assignTask(taskId)
  await taskStore.refreshStageTasks(props.workflowCode, props.stageCode)
  if (selectedTask.value?.id === taskId) {
    const latest = taskStore.tasks.find(task => task.id === taskId)
    if (latest) {
      selectedTask.value = { ...latest, chatHistory: selectedTask.value.chatHistory }
    }
  }
}

function handleLocalTaskUpdate(task: ServiceTask) {
  taskStore.updateTask(task)
  selectedTask.value = task
}

async function handleAdvanceTask(taskId: string) {
  await taskStore.moveToNextStage(taskId)
  await taskStore.refreshStageTasks(props.workflowCode, props.stageCode)
  selectedTask.value = null
}

async function handleCompleteTask(taskId: string) {
  await taskStore.finishTask(taskId)
  await taskStore.refreshStageTasks(props.workflowCode, props.stageCode)
  if (selectedTask.value?.id === taskId) {
    selectedTask.value = null
  }
}

function handleFaultReported(faultTaskId: string) {
  if (!selectedTask.value) return
  const systemMsg: Message = {
    id: crypto.randomUUID(),
    role: 'bot',
    content: `已生成系统报障工单 #${faultTaskId}`,
    timestamp: Date.now(),
    type: 'system',
  }
  const updated = {
    ...selectedTask.value,
    chatHistory: [...selectedTask.value.chatHistory, systemMsg],
    updatedAt: Date.now(),
  }
  taskStore.updateTask(updated)
  selectedTask.value = updated
}
</script>

<style lang="scss" scoped>
.workstation-area {
  display: flex;
  gap: 12px;
  padding: 0;
  background-color: var(--bg-color);
}

.workbench-region {
  min-width: 0;
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.region-center {
  flex: 1;
}
</style>
