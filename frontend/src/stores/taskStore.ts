import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import type { ServiceTask } from '@/types'

const INITIAL_TASKS: ServiceTask[] = [
  {
    id: 'TASK-001',
    customerId: 'CUST-882',
    agentId: 'AG-880012',
    requestSource: 'Agent',
    status: 'Pending',
    priority: 'High',
    category: '核保咨询',
    summary: '渠道代理人李芳请求协助：客户张伟的高额保单核保进度查询及加费项解释。',
    aiSuggestion: '建议核实客户体检报告中的异常项，并根据精算规则提供加费说明话术。',
    chatHistory: [],
    level: 1,
    createdAt: Date.now() - 3600000,
    updatedAt: Date.now() - 3600000,
  },
  {
    id: 'TASK-002',
    customerId: 'CUST-441',
    agentId: 'AG-880045',
    requestSource: 'Agent',
    status: 'Pending',
    priority: 'Medium',
    category: '渠道支持',
    summary: '渠道代理人王强咨询：新产品"尊享人生"在江苏地区的销售限额及佣金政策。',
    aiSuggestion: '提供最新的产品区域销售政策文档，并确认其所属机构的佣金系数。',
    chatHistory: [],
    level: 1,
    createdAt: Date.now() - 7200000,
    updatedAt: Date.now() - 7200000,
  },
]

export const useTaskStore = defineStore('task', () => {
  const tasks = ref<ServiceTask[]>(INITIAL_TASKS)

  // --- Getters ---

  const pendingByLevel = (level: 1 | 2) =>
    computed(() => tasks.value.filter(t => t.level === level && t.status === 'Pending'))

  const processingByMe = (level: 1 | 2) =>
    computed(() =>
      tasks.value.filter(
        t => t.level === level && t.status === 'Processing' && t.assignedTo === 'ME',
      ),
    )

  // --- Actions ---

  function addTask(task: ServiceTask) {
    tasks.value.unshift(task)
  }

  function updateTask(updated: ServiceTask) {
    const idx = tasks.value.findIndex(t => t.id === updated.id)
    if (idx !== -1) {
      tasks.value[idx] = { ...updated }
    }
  }

  function grabTask(taskId: string) {
    const task = tasks.value.find(t => t.id === taskId)
    if (!task) return
    updateTask({
      ...task,
      status: 'Processing',
      assignedTo: 'ME',
      updatedAt: Date.now(),
    })
  }

  function escalateTask(taskId: string) {
    const task = tasks.value.find(t => t.id === taskId)
    if (!task) return
    updateTask({
      ...task,
      status: 'Pending',
      level: 2,
      assignedTo: undefined,
      updatedAt: Date.now(),
    })
  }

  function markRead(taskId: string) {
    const task = tasks.value.find(t => t.id === taskId)
    if (!task) return
    updateTask({ ...task, unreadCount: 0 })
  }

  function createFaultTask(payload: {
    relatedTaskId?: string
    faultType?: string
    description: string
    affectedScope?: string
    urgency: 'Low' | 'Medium' | 'High' | 'Urgent'
  }): string {
    const ts = Date.now()
    const id = `FAULT-${String(ts).slice(-6)}`
    const summaryParts = [
      payload.faultType ? `[${payload.faultType}]` : '',
      payload.description,
      payload.affectedScope ? `（影响范围：${payload.affectedScope}）` : '',
      payload.relatedTaskId ? `关联任务：${payload.relatedTaskId}` : '',
    ].filter(Boolean)
    const newTask: ServiceTask = {
      id,
      requestSource: 'Agent',
      status: 'Pending',
      priority: payload.urgency,
      category: '生产故障',
      summary: summaryParts.join(' '),
      aiSuggestion: '',
      chatHistory: [],
      level: 2,
      createdAt: ts,
      updatedAt: ts,
    }
    tasks.value.unshift(newTask)
    return id
  }

  return {
    tasks,
    pendingByLevel,
    processingByMe,
    addTask,
    updateTask,
    grabTask,
    escalateTask,
    markRead,
    createFaultTask,
  }
})
