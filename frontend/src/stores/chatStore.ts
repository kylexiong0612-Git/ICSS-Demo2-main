import { defineStore } from 'pinia'
import { ref } from 'vue'
import type { Message } from '@/types'
import { getChatHistory } from '@/api/chat'
import request from '@/utils/request'

export const useChatStore = defineStore(
  'chat',
  () => {
    // 客户侧聊天历史（持久化，保留 Demo 会话内容）
    const customerHistory = ref<Message[]>([])
    // 客户上次打开聊天界面的时间戳，用于计算未读消息数
    const lastChatOpenedAt = ref(0)

    function setHistory(messages: Message[]) {
      customerHistory.value = messages
    }

    function appendMessage(msg: Message) {
      customerHistory.value.push(msg)
    }

    function clearHistory() {
      customerHistory.value = []
    }

    function markChatOpened() {
      lastChatOpenedAt.value = Date.now()
    }

    /**
     * 将单条消息持久化到后端；失败时静默降级，localStorage 缓存仍为 UI 数据源。
     */
    async function persistMessage(customerId: string, msg: Message): Promise<void> {
      try {
        await request.post(`/api/chats/${customerId}/messages`, {
          id: msg.id,
          role: msg.role,
          content: msg.content,
          messageType: msg.type ?? 'text',
          metadata: msg.metadata ?? null,
          createdAt: msg.timestamp,
        })
      } catch (err) {
        console.warn('[ChatStore] 消息持久化失败，使用本地缓存', err)
      }
    }

    /**
     * 从后端加载指定客户的聊天历史，并与本地未同步消息合并。
     */
    async function loadHistoryFromBackend(customerId: string): Promise<void> {
      try {
        const data = await getChatHistory(customerId)
        const backendIds = new Set(data.map((m: Message) => m.id))
        const unsynced = customerHistory.value.filter(m => !backendIds.has(m.id))
        customerHistory.value = [...data, ...unsynced]
      } catch (err) {
        console.warn('[ChatStore] 无法从后端加载历史，使用 localStorage 缓存', err)
      }
    }

    return {
      customerHistory,
      lastChatOpenedAt,
      setHistory,
      appendMessage,
      clearHistory,
      markChatOpened,
      persistMessage,
      loadHistoryFromBackend,
    }
  },
  {
    persist: true,
  },
)
