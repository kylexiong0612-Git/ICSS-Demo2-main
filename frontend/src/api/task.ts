import request from '@/utils/request'
import type { ServiceTask } from '@/types'

export function getTaskList(params: { level: 1 | 2; status?: string }) {
  return request.get<ServiceTask[]>('/api/tasks', { params })
}

export function createTask(data: Partial<ServiceTask>) {
  return request.post<ServiceTask>('/api/tasks', data)
}

export function updateTask(task: ServiceTask) {
  return request.put<ServiceTask>(`/api/tasks/${task.id}`, task)
}

export function grabTask(taskId: string, agentId: string) {
  return request.put<ServiceTask>(`/api/tasks/${taskId}/grab`, { agentId })
}

export function escalateTask(taskId: string) {
  return request.put<ServiceTask>(`/api/tasks/${taskId}/escalate`)
}

export function completeTask(taskId: string) {
  return request.put<ServiceTask>(`/api/tasks/${taskId}/complete`)
}

export function createFaultTask(payload: {
  relatedTaskId?: string
  faultType?: string
  description: string
  affectedScope?: string
  urgency: 'Low' | 'Medium' | 'High' | 'Urgent'
}) {
  return request.post<ServiceTask>('/api/tasks/fault', payload)
}
