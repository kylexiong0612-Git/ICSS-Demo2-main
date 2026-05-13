import request from '@/utils/request'
import type { Message } from '@/types'

interface BackendChatMessage {
  id: string
  role: Message['role']
  content: string
  messageType?: string
  createdAt?: number
  metadata?: unknown
}

export async function getChatHistory(customerId: string): Promise<Message[]> {
  const records = await request.get<unknown, BackendChatMessage[]>(`/api/chats/${customerId}`)
  return records.map(record => ({
    id: record.id,
    role: record.role,
    content: record.content,
    type: (record.messageType as Message['type']) ?? 'text',
    timestamp: record.createdAt ?? Date.now(),
    metadata: record.metadata,
  }))
}
