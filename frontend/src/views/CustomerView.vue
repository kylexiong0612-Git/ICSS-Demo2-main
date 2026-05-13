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
import { getBotResponse } from '@/api/ai'
import { createHandoffTask } from '@/api/task'
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
  [...taskStore.tasks]
    .filter(t => t.customerId === 'CUST-882' && t.status !== 'Completed')
    .sort((a, b) => b.updatedAt - a.updatedAt)[0] ?? null,
)

// 未读消息数：本地会话中时间戳晚于上次打开聊天的坐席消息数
const customerUnreadCount = computed(() => {
  return messages.value.filter(
    m => m.role === 'agent' && m.timestamp > chatStore.lastChatOpenedAt,
  ).length
})

// 持久化聊天历史
watch(messages, msgs => {
  chatStore.setHistory(msgs)
})

onMounted(async () => {
  await taskStore.loadCustomerTasks('CUST-882')
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
    if (activeTask.value && activeTask.value.status !== 'Completed') {
      const existingTaskMsg: Message = {
        id: crypto.randomUUID(),
        role: 'bot',
        content: `您当前已有人工服务单正在处理中，已进入${activeTask.value.category}专席，请耐心等待。`,
        timestamp: Date.now(),
        type: 'text',
      }
      messages.value.push(existingTaskMsg)
      chatStore.persistMessage('CUST-882', existingTaskMsg)
      return
    }

    isTransferring.value = true
    const transferMsg: Message = {
      id: crypto.randomUUID(),
      role: 'bot',
      content: '好的，正在为您识别服务意图并转接对应人工专席，请稍候...',
      timestamp: Date.now(),
      type: 'text',
    }
    messages.value.push(transferMsg)
    chatStore.persistMessage('CUST-882', transferMsg)
    try {
      const createdTask = await createHandoffTask({
        customerId: 'CUST-882',
        messages: messages.value,
      })
      const routedTask: ServiceTask = {
        ...createdTask,
        chatHistory: [...messages.value],
      }
      taskStore.updateTask(routedTask)

      const routedMsg: Message = {
        id: crypto.randomUUID(),
        role: 'bot',
        content: `已为您转接到${createdTask.category}专席，当前进入 ${createdTask.workflowCode} / ${createdTask.currentStageCode}。`,
        timestamp: Date.now(),
        type: 'system',
      }
      messages.value.push(routedMsg)
    } catch (error) {
      console.error('[CustomerView] 创建转人工任务失败', error)
      const failedMsg: Message = {
        id: crypto.randomUUID(),
        role: 'bot',
        content: '当前转人工暂时失败，已为您保留会话内容，请稍后再试。',
        timestamp: Date.now(),
        type: 'text',
      }
      messages.value.push(failedMsg)
      chatStore.persistMessage('CUST-882', failedMsg)
    } finally {
      isTransferring.value = false
    }
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
