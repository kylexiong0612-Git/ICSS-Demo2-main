import { computed, ref } from 'vue'
import { defineStore } from 'pinia'
import {
  advanceTask,
  completeTask,
  createFaultTask,
  createTask,
  getTaskList,
  grabTask,
  updateTask as updateTaskApi,
} from '@/api/task'
import type { ServiceTask } from '@/types'

function normalizeTask(task: Partial<ServiceTask>): ServiceTask {
  return {
    id: task.id ?? '',
    customerId: task.customerId,
    agentId: task.agentId,
    requestSource: task.requestSource ?? 'Customer',
    status: task.status ?? 'Pending',
    priority: task.priority ?? 'Medium',
    category: task.category ?? '通用咨询',
    summary: task.summary ?? '',
    aiSuggestion: task.aiSuggestion ?? '',
    chatHistory: task.chatHistory ?? [],
    assignedTo: task.assignedTo,
    unreadCount: task.unreadCount ?? 0,
    level: task.level ?? task.currentStageOrder ?? 1,
    workflowCode: task.workflowCode ?? 'ops-service-flow',
    currentStageCode: task.currentStageCode ?? 'L1',
    currentStageOrder: task.currentStageOrder ?? task.level ?? 1,
    sourceTaskId: task.sourceTaskId,
    intentCode: task.intentCode,
    routeReason: task.routeReason,
    handoffConfidence: task.handoffConfidence,
    tags: task.tags ?? [],
    createdAt: task.createdAt ?? Date.now(),
    updatedAt: task.updatedAt ?? Date.now(),
  }
}

export const useTaskStore = defineStore('task', () => {
  const tasks = ref<ServiceTask[]>([])
  const isLoading = ref(false)

  function mergeTasks(incomingTasks: Partial<ServiceTask>[]) {
    for (const incoming of incomingTasks) {
      upsertTask(normalizeTask(incoming))
    }
  }

  function upsertTask(task: ServiceTask) {
    const idx = tasks.value.findIndex(item => item.id === task.id)
    if (idx === -1) {
      tasks.value.unshift(task)
      return
    }
    tasks.value[idx] = {
      ...tasks.value[idx],
      ...task,
      chatHistory: task.chatHistory?.length ? task.chatHistory : tasks.value[idx].chatHistory,
    }
  }

  async function loadTasks(params: {
    level?: number
    workflowCode?: string
    stageCode?: string
    customerId?: string
    status?: string
  }) {
    isLoading.value = true
    try {
      const data = await getTaskList(params)
      mergeTasks(data)
      return data.map(normalizeTask)
    } finally {
      isLoading.value = false
    }
  }

  async function refreshStageTasks(workflowCode: string, stageCode: string) {
    return loadTasks({ workflowCode, stageCode })
  }

  async function loadCustomerTasks(customerId: string) {
    return loadTasks({ customerId })
  }

  async function addTask(task: Partial<ServiceTask>) {
    const created = normalizeTask(await createTask(task))
    upsertTask(created)
    return created
  }

  async function saveTask(task: ServiceTask) {
    const saved = normalizeTask(await updateTaskApi(task))
    upsertTask(saved)
    return saved
  }

  async function assignTask(taskId: string, agentId = 'ME') {
    const saved = normalizeTask(await grabTask(taskId, agentId))
    upsertTask(saved)
    return saved
  }

  async function moveToNextStage(taskId: string) {
    const response = await advanceTask(taskId)
    if (response.currentTask) {
      upsertTask(normalizeTask(response.currentTask))
    }
    if (response.nextTask) {
      upsertTask(normalizeTask(response.nextTask))
    }
    return response
  }

  async function finishTask(taskId: string) {
    const saved = normalizeTask(await completeTask(taskId))
    upsertTask(saved)
    return saved
  }

  function updateTask(task: ServiceTask) {
    upsertTask(normalizeTask(task))
  }

  function getTasksByStage(workflowCode: string, stageCode: string) {
    return computed(() =>
      tasks.value.filter(
        task =>
          task.workflowCode === workflowCode &&
          task.currentStageCode === stageCode,
      ),
    )
  }

  function pendingByStage(workflowCode: string, stageCode: string) {
    return computed(() =>
      tasks.value.filter(
        task =>
          task.workflowCode === workflowCode &&
          task.currentStageCode === stageCode &&
          task.status === 'Pending',
      ),
    )
  }

  function processingByMe(workflowCode: string, stageCode: string) {
    return computed(() =>
      tasks.value.filter(
        task =>
          task.workflowCode === workflowCode &&
          task.currentStageCode === stageCode &&
          task.status === 'Processing' &&
          task.assignedTo === 'ME',
      ),
    )
  }

  async function submitFaultTask(payload: {
    relatedTaskId?: string
    faultType?: string
    description: string
    affectedScope?: string
    urgency: 'Low' | 'Medium' | 'High' | 'Urgent'
  }) {
    const created = normalizeTask(await createFaultTask(payload))
    upsertTask(created)
    return created
  }

  function markRead(taskId: string) {
    const task = tasks.value.find(item => item.id === taskId)
    if (!task) return
    upsertTask({ ...task, unreadCount: 0 })
  }

  return {
    tasks,
    isLoading,
    getTasksByStage,
    pendingByStage,
    processingByMe,
    loadTasks,
    refreshStageTasks,
    loadCustomerTasks,
    addTask,
    saveTask,
    assignTask,
    moveToNextStage,
    finishTask,
    submitFaultTask,
    updateTask,
    markRead,
  }
})
