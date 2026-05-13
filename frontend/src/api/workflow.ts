import request from '@/utils/request'
import type {
  AgentWorkflowRouteRule,
  AgentWorkflowTemplate,
  WorkbenchLayoutConfig,
} from '@/types'

export function getAgentWorkflows() {
  return request.get<unknown, AgentWorkflowTemplate[]>('/api/agent-workflows')
}

export function getAgentWorkflow(code: string) {
  return request.get<unknown, AgentWorkflowTemplate>(`/api/agent-workflows/${code}`)
}

export function createAgentWorkflow(template: AgentWorkflowTemplate) {
  return request.post<unknown, AgentWorkflowTemplate>('/api/agent-workflows', template)
}

export function updateAgentWorkflow(template: AgentWorkflowTemplate) {
  return request.put<unknown, AgentWorkflowTemplate>(`/api/agent-workflows/${template.code}`, template)
}

export function getAgentWorkflowRoutes() {
  return request.get<unknown, AgentWorkflowRouteRule[]>('/api/agent-workflow-routes')
}

export function updateAgentWorkflowRoute(rule: AgentWorkflowRouteRule) {
  return request.put<unknown, AgentWorkflowRouteRule>(
    `/api/agent-workflow-routes/${rule.intentCode}`,
    rule,
  )
}

export function getWorkbenchLayouts() {
  return request.get<unknown, WorkbenchLayoutConfig[]>('/api/workbench-layouts')
}

export function getEffectiveWorkbenchLayout(workflowCode: string, stageCode: string) {
  return request.get<unknown, WorkbenchLayoutConfig>('/api/workbench-layouts/effective', {
    params: { workflowCode, stageCode },
  })
}

export function createWorkbenchLayout(layout: WorkbenchLayoutConfig) {
  return request.post<unknown, WorkbenchLayoutConfig>('/api/workbench-layouts', layout)
}

export function updateWorkbenchLayout(layout: WorkbenchLayoutConfig) {
  return request.put<unknown, WorkbenchLayoutConfig>(`/api/workbench-layouts/${layout.code}`, layout)
}
