import request from '@/utils/request'
import type { HandoffAnalysis, Message, ServiceTask, TaskAdvanceResponse } from '@/types'

function serializeTask(task: Partial<ServiceTask>) {
  return {
    id: task.id,
    customerId: task.customerId,
    agentId: task.agentId,
    requestSource: task.requestSource,
    status: task.status,
    priority: task.priority,
    category: task.category,
    summary: task.summary,
    aiSuggestion: task.aiSuggestion,
    assignedTo: task.assignedTo,
    unreadCount: task.unreadCount,
    level: task.level,
    workflowCode: task.workflowCode,
    currentStageCode: task.currentStageCode,
    currentStageOrder: task.currentStageOrder,
    sourceTaskId: task.sourceTaskId,
    intentCode: task.intentCode,
    routeReason: task.routeReason,
    handoffConfidence: task.handoffConfidence,
    tags: task.tags,
    createdAt: task.createdAt,
    updatedAt: task.updatedAt,
  }
}

export function getTaskList(params: {
  level?: number
  workflowCode?: string
  stageCode?: string
  customerId?: string
  status?: string
}) {
  return request.get<unknown, ServiceTask[]>('/api/tasks', { params })
}

export function createTask(data: Partial<ServiceTask>) {
  return request.post<unknown, ServiceTask>('/api/tasks', serializeTask(data))
}

export function updateTask(task: ServiceTask) {
  return request.put<unknown, ServiceTask>(`/api/tasks/${task.id}`, serializeTask(task))
}

export function grabTask(taskId: string, agentId: string) {
  return request.put<unknown, ServiceTask>(`/api/tasks/${taskId}/grab`, { agentId })
}

export function escalateTask(taskId: string) {
  return request.put<unknown, TaskAdvanceResponse>(`/api/tasks/${taskId}/escalate`)
}

export function advanceTask(taskId: string) {
  return request.put<unknown, TaskAdvanceResponse>(`/api/tasks/${taskId}/advance`)
}

export function completeTask(taskId: string) {
  return request.put<unknown, ServiceTask>(`/api/tasks/${taskId}/complete`)
}

export function createFaultTask(payload: {
  relatedTaskId?: string
  faultType?: string
  description: string
  affectedScope?: string
  urgency: 'Low' | 'Medium' | 'High' | 'Urgent'
}) {
  return request.post<unknown, ServiceTask>('/api/tasks/fault', payload)
}

export function analyzeHandoff(payload: { customerId: string; messages: Message[] }) {
  return request.post<unknown, HandoffAnalysis>('/api/tasks/handoff-analyze', {
    customerId: payload.customerId,
    messages: payload.messages.map(message => ({
      id: message.id,
      role: message.role,
      content: message.content,
      type: message.type ?? 'text',
      metadata: message.metadata ?? null,
      timestamp: message.timestamp,
    })),
  })
}

export function createHandoffTask(payload: { customerId: string; messages: Message[] }) {
  return request.post<unknown, ServiceTask>('/api/tasks/handoff', {
    customerId: payload.customerId,
    messages: payload.messages.map(message => ({
      id: message.id,
      role: message.role,
      content: message.content,
      type: message.type ?? 'text',
      metadata: message.metadata ?? null,
      timestamp: message.timestamp,
    })),
  })
}
