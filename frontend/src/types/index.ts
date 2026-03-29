export type MessageRole = 'user' | 'bot' | 'agent'

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
  level: 1 | 2
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
