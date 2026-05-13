export type MessageRole = 'user' | 'bot' | 'agent' | 'system'

export interface Message {
  id: string
  role: MessageRole
  content: string
  timestamp: number
  type?: 'text' | 'card' | 'system'
  metadata?: unknown
}

export interface Customer {
  id: string
  name: string
  phone: string
  idNumber: string
  level: 'Normal' | 'Silver' | 'Gold' | 'VIP'
  policies: Policy[]
  agent: Agent
}

export interface Policy {
  id: string
  name: string
  status: 'Active' | 'Lapsed' | 'Pending'
  premium: number
  startDate: string
}

export interface Agent {
  id: string
  name: string
  code: string
  phone: string
  branch: string
}

export interface ServiceTask {
  id: string
  customerId?: string
  agentId?: string
  requestSource: 'Customer' | 'Agent'
  status: 'Pending' | 'Processing' | 'Suspended' | 'Completed' | 'Escalated'
  priority: 'Low' | 'Medium' | 'High' | 'Urgent'
  category: string
  summary: string
  aiSuggestion: string
  chatHistory: Message[]
  assignedTo?: string
  unreadCount?: number
  level: number
  workflowCode: string
  currentStageCode: string
  currentStageOrder: number
  sourceTaskId?: string
  intentCode?: string
  routeReason?: string
  handoffConfidence?: number
  tags?: string[]
  createdAt: number
  updatedAt: number
}

export interface DashboardStats {
  waitingCount: number
  onlineAgents: number
  avgHandlingTime: number
  fcr: number // First Contact Resolution
  botDeflectionRate: number
}

export interface AgentWorkflowStage {
  id?: number
  workflowCode: string
  code: string
  name: string
  stageOrder: number
  roleLabel: string
  description?: string
  allowedActions: string[]
}

export interface AgentWorkflowTemplate {
  code: string
  name: string
  description?: string
  categoryScope: string[]
  enabled: boolean
  stages: AgentWorkflowStage[]
}

export interface AgentWorkflowRouteRule {
  id?: number
  intentCode: string
  intentName: string
  targetWorkflowCode: string
  entryStageCode: string
  priorityStrategy: 'inherit' | 'force-medium' | 'force-high' | 'force-urgent'
  enabled: boolean
}

export interface WorkbenchWidget {
  code: string
  type: string
  title?: string
  visible: boolean
  order: number
  props?: Record<string, unknown>
}

export interface WorkbenchRegion {
  code: 'left' | 'center' | 'right' | 'top' | 'bottom'
  width?: string
  widgets: WorkbenchWidget[]
}

export interface WorkbenchLayoutConfig {
  code: string
  name: string
  workflowCode: string
  stageCode: string
  regions: WorkbenchRegion[]
  enabled: boolean
}

export interface HandoffAnalysis {
  handoffNeeded: boolean
  intentCode: string
  intentName: string
  summary: string
  suggestion: string
  tags: string[]
  targetWorkflowCode?: string
  targetStageCode?: string
  confidence: number
  routeReason?: string
}

export interface TaskAdvanceResponse {
  currentTask: ServiceTask
  nextTask: ServiceTask | null
}
