<template>
  <div class="customer-page">
    <div class="phone-frame">
      <!-- 手机顶部状态栏 -->
      <div class="phone-status-bar">
        <span class="status-time">9:41</span>
        <div class="status-icons">
          <el-icon size="12"><Signal /></el-icon>
          <el-icon size="12"><Wifi /></el-icon>
          <el-icon size="12"><Battery /></el-icon>
        </div>
      </div>

      <div class="phone-content">
        <HomeScreen v-if="!isChatOpen" :unread-count="customerUnreadCount" @open-chat="openChat" />
        <ChatInterface
          v-else
          :messages="messages"
          :is-loading="isLoading"
          :is-transferring="isTransferring"
          @close="isChatOpen = false"
          @send="handleSend"
        />
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, watch, nextTick, onMounted } from 'vue'
import HomeScreen from '@/components/customer/HomeScreen.vue'
import ChatInterface from '@/components/customer/ChatInterface.vue'
import { getBotResponse, preProcessTask } from '@/api/ai'
import { useTaskStore } from '@/stores/taskStore'
import { useChatStore } from '@/stores/chatStore'
import type { Message, ServiceTask } from '@/types'

const taskStore = useTaskStore()
const chatStore = useChatStore()

const isChatOpen = ref(false)
const messages = ref<Message[]>([])
const isLoading = ref(false)
const isTransferring = ref(false)

// 当前客户对应的活跃任务（customerId = CUST-882）
const activeTask = computed(() =>
  taskStore.tasks.find(
    t => t.customerId === 'CUST-882' && t.status !== 'Completed',
  ),
)

// 未读消息数：task chatHistory 中时间戳晚于上次打开聊天的坐席消息数（computed 自动响应）
const customerUnreadCount = computed(() => {
  const history = activeTask.value?.chatHistory
  if (!history) return 0
  return history.filter(
    m => m.role === 'agent' && m.timestamp > chatStore.lastChatOpenedAt,
  ).length
})

// 当坐席回复时，同步到 messages
watch(
  () => activeTask.value?.chatHistory,
  history => {
    if (!history) return
    const localIds = new Set(messages.value.map(m => m.id))
    const newMsgs = history.filter(m => !localIds.has(m.id))
    if (newMsgs.length > 0) {
      messages.value = [...messages.value, ...newMsgs]
    }
  },
  { deep: true },
)

// 持久化聊天历史
watch(messages, msgs => {
  chatStore.setHistory(msgs)
})

onMounted(async () => {
  // 优先从后端加载历史（与 localStorage 缓存合并）
  await chatStore.loadHistoryFromBackend('CUST-882')

  if (chatStore.customerHistory.length > 0) {
    messages.value = [...chatStore.customerHistory]
  } else {
    // 初始欢迎消息
    const welcomeMsg: Message = {
      id: crypto.randomUUID(),
      role: 'bot',
      content: '您好！我是宏小二，您的专属保险助理。请问有什么可以帮您？',
      timestamp: Date.now(),
      type: 'text',
    }
    messages.value = [welcomeMsg]
    chatStore.persistMessage('CUST-882', welcomeMsg)
  }

  // 合并坐席在组件卸载期间发送的消息（导航离开再返回时 watch 不会重放历史变更）
  const history = activeTask.value?.chatHistory
  if (history) {
    const localIds = new Set(messages.value.map(m => m.id))
    const newMsgs = history.filter(m => !localIds.has(m.id))
    if (newMsgs.length > 0) {
      messages.value = [...messages.value, ...newMsgs]
    }
  }
})

function openChat() {
  isChatOpen.value = true
  chatStore.markChatOpened()
}

async function handleSend(text: string) {
  if (!text.trim() || isLoading.value) return

  const userMsg: Message = {
    id: crypto.randomUUID(),
    role: 'user',
    content: text.trim(),
    timestamp: Date.now(),
    type: 'text',
  }
  messages.value.push(userMsg)
  chatStore.persistMessage('CUST-882', userMsg)
  await nextTick()

  // 检测转人工
  const transferKeywords = ['转人工', '转坐席', '人工', '客服']
  const wantsTransfer = transferKeywords.some(kw => text.includes(kw))

  if (wantsTransfer && !isTransferring.value) {
    isTransferring.value = true
    const transferMsg: Message = {
      id: crypto.randomUUID(),
      role: 'bot',
      content: '好的，正在为您转接人工坐席，请稍候...',
      timestamp: Date.now(),
      type: 'text',
    }
    messages.value.push(transferMsg)
    chatStore.persistMessage('CUST-882', transferMsg)

    // AI 预处理生成任务摘要
    const analysis = await preProcessTask(messages.value)
    const newTask: ServiceTask = {
      id: `TASK-${Date.now()}`,
      customerId: 'CUST-882',
      requestSource: 'Customer',
      status: 'Pending',
      priority: 'Medium',
      category: analysis.tags?.[0] || '通用咨询',
      summary: analysis.summary || '客户请求转人工服务',
      aiSuggestion: analysis.suggestion || '请人工接待',
      chatHistory: [...messages.value],
      level: 1,
      createdAt: Date.now(),
      updatedAt: Date.now(),
    }
    taskStore.addTask(newTask)
    return
  }

  // 普通 AI 对话
  isLoading.value = true
  try {
    const reply = await getBotResponse(text, messages.value)
    const botMsg: Message = {
      id: crypto.randomUUID(),
      role: 'bot',
      content: reply,
      timestamp: Date.now(),
      type: 'text',
    }
    messages.value.push(botMsg)
    chatStore.persistMessage('CUST-882', botMsg)

    // 同步到活跃任务的聊天记录（客户是发送方，坐席是接收方，unreadCount 递增）
    if (activeTask.value) {
      taskStore.updateTask({
        ...activeTask.value,
        chatHistory: [...messages.value],
        unreadCount: (activeTask.value.unreadCount || 0) + 1,
        updatedAt: Date.now(),
      })
    }
  } finally {
    isLoading.value = false
  }
}
</script>

<style lang="scss" scoped>
.customer-page {
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  background-color: #e8ecf0;
  overflow-y: auto;
  padding: 16px;
}

.phone-frame {
  width: 375px;
  height: 667px;
  background-color: #fff;
  border-radius: 40px;
  border: 8px solid #1a1a1a;
  box-shadow:
    0 0 0 2px #333,
    0 32px 64px rgba(0, 0, 0, 0.3);
  display: flex;
  flex-direction: column;
  overflow: hidden;
  position: relative;
  flex-shrink: 0;
}

.phone-status-bar {
  flex-shrink: 0;
  height: 44px;
  background-color: var(--primary-color);
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 20px;
  padding-top: env(safe-area-inset-top);
}

.status-time {
  font-size: 12px;
  font-weight: 600;
  color: #fff;
}

.status-icons {
  display: flex;
  align-items: center;
  gap: 4px;
  color: #fff;
}

.phone-content {
  flex: 1;
  overflow: hidden;
  display: flex;
  flex-direction: column;
}
</style>
