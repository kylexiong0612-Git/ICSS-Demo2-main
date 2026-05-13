<template>
  <div class="chat-panel">
    <!-- 未选中状态 -->
    <div v-if="!task" class="chat-empty">
      <div class="empty-icon">
        <el-icon size="40" color="#8e90a2"><Headset /></el-icon>
      </div>
      <h3>请选择一个任务开始作业</h3>
      <p>您可以从左侧任务列表中选择待处理的任务，或在公共池中领取任务。</p>
    </div>

    <!-- 已选中任务 -->
    <template v-else>
      <!-- 聊天头 -->
      <div class="chat-header">
        <div class="chat-header-left">
          <el-icon size="14" color="#00a758"><ChatDotRound /></el-icon>
          <span class="chat-header-title">会话记录</span>
          <span class="chat-task-id">· {{ task.id }}</span>
        </div>
        <el-tag
          :type="task.status === 'Processing' ? 'success' : task.status === 'Escalated' ? 'warning' : 'info'"
          size="small"
          effect="light"
        >
          {{ statusLabel }}
        </el-tag>
      </div>

      <!-- 消息区域 -->
      <div ref="chatScrollRef" class="chat-messages custom-scrollbar">
        <div class="session-start-tag">
          <span>会话开始 · 2026-03-16 12:30</span>
        </div>

        <div
          v-for="msg in task.chatHistory"
          :key="msg.id"
          class="msg-row"
          :class="msg.role === 'user' ? 'msg-left' : 'msg-right'"
        >
          <!-- 系统消息 -->
          <div v-if="msg.type === 'system'" class="msg-system">
            <span>{{ msg.content }}</span>
          </div>

          <!-- 普通消息 -->
          <template v-else>
            <div
              class="msg-avatar"
              :class="senderInfo(msg).avatarClass"
            >
              <template v-if="msg.role === 'bot'">
                <svg width="16" height="16" viewBox="0 0 1024 1024" fill="currentColor" xmlns="http://www.w3.org/2000/svg">
                  <path d="M22.8 496.7H76v217.2H22.8V496.7z m922 0H998v217.2h-53.2V496.7z"/>
                  <path d="M310.3 589.3c0 27.6 22.4 50 50 50s50-22.4 50-50c0-17.9-9.5-34.4-25-43.3-15.5-8.9-34.6-8.9-50 0-15.5 8.9-25 25.4-25 43.3z m0 0M604.1 589.3c0 17.9 9.5 34.4 25 43.3 15.5 8.9 34.6 8.9 50 0 15.5-8.9 25-25.5 25-43.3 0-17.9-9.5-34.4-25-43.3-15.5-8.9-34.6-8.9-50 0-15.4 8.9-25 25.4-25 43.3z m0 0"/>
                  <path d="M854.1 299.7H533.8V133.4c14.1-8.9 23.4-24.5 23.4-42.4 0-27.7-22.4-50-50-50-27.7 0-50 22.4-50 50 0 17.9 9.4 33.4 23.4 42.4v166.3H166.7c-24.7 0-44.7 20-44.7 44.7V913c0 24.7 20 44.7 44.7 44.7h687.2c24.7 0 44.7-20 44.7-44.7V344.4c0.2-24.7-20-44.7-44.5-44.7z m-8.5 604.8H175.2V352.9h670.2v551.5h0.2z"/>
                </svg>
              </template>
              <template v-else-if="senderInfo(msg).emoji">{{ senderInfo(msg).emoji }}</template>
              <el-icon v-else size="14"><component :is="senderInfo(msg).icon" /></el-icon>
            </div>
            <div class="msg-body">
              <span class="msg-sender-name" :class="msg.role !== 'user' ? 'text-right' : ''">
                {{ senderInfo(msg).name }}
              </span>
              <div
                class="msg-bubble"
                :class="msg.role === 'user' ? 'bubble-user' : 'bubble-agent'"
              >
                {{ msg.content }}
              </div>
              <p class="msg-time" :class="msg.role !== 'user' ? 'text-right' : ''">
                {{ new Date(msg.timestamp).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' }) }}
              </p>
            </div>
          </template>
        </div>
      </div>

      <!-- 回复区域 -->
      <div class="reply-area">
        <div class="reply-toolbar">
          <el-button text size="small" :icon="Opportunity">快捷回复</el-button>
          <el-button text size="small" :icon="Lock">合规话术</el-button>
        </div>
        <div class="reply-input-wrap" :class="{ focused: isFocused }">
          <textarea
            v-model="reply"
            class="reply-textarea"
            placeholder="输入回复内容... (Ctrl+Enter 发送)"
            @focus="isFocused = true"
            @blur="isFocused = false"
            @keydown.ctrl.enter="handleSend"
            @keydown.meta.enter="handleSend"
          />
          <div class="reply-actions">
            <div class="reply-actions-left">
              <el-button text :icon="Document" size="small" />
              <el-button text :icon="Warning" size="small" />
            </div>
            <div class="reply-actions-right">
              <template v-if="task.status === 'Pending'">
                <el-button type="primary" size="small" @click="$emit('grab', task.id)">
                  领取任务
                </el-button>
              </template>
              <template v-else>
                <el-button
                  v-if="canAdvance"
                  size="small"
                  @click="handleAdvance"
                >
                  推进至 {{ nextStageLabel }}
                </el-button>
                <el-button
                  size="small"
                  @click="$emit('complete', task.id)"
                >
                  完成
                </el-button>
                <el-button
                  type="primary"
                  size="small"
                  :icon="Promotion"
                  :disabled="!reply.trim()"
                  @click="handleSend"
                >
                  发送
                </el-button>
              </template>
            </div>
          </div>
        </div>
      </div>
    </template>
  </div>
</template>

<script setup lang="ts">
import { computed, ref, watch, nextTick } from 'vue'
import {
  ChatDotRound,
  Headset,
  Opportunity,
  Lock,
  Document,
  Warning,
  Promotion,
  User,
  Service,
  StarFilled,
  BellFilled,
} from '@element-plus/icons-vue'
import type { ServiceTask, Message } from '@/types'
import { useChatStore } from '@/stores/chatStore'

const chatStore = useChatStore()

const props = defineProps<{
  task: ServiceTask | null
  stageLabel: string
  nextStageLabel?: string
}>()

const emit = defineEmits<{
  'update-task': [task: ServiceTask]
  advance: [taskId: string]
  complete: [taskId: string]
  grab: [taskId: string]
}>()

const reply = ref('')
const isFocused = ref(false)
const chatScrollRef = ref<HTMLDivElement | null>(null)
const statusLabel = computed(() => {
  if (!props.task) return '待领取'
  if (props.task.status === 'Processing') return '处理中'
  if (props.task.status === 'Completed') return '已完成'
  if (props.task.status === 'Escalated') return '已升级'
  return '待领取'
})
const canAdvance = computed(() =>
  Boolean(props.task && props.task.status === 'Processing' && props.nextStageLabel),
)

// 消息更新时自动滚到底部
watch(
  () => props.task?.chatHistory,
  async () => {
    await nextTick()
    if (chatScrollRef.value) {
      chatScrollRef.value.scrollTop = chatScrollRef.value.scrollHeight
    }
  },
  { deep: true },
)

function senderInfo(msg: Message) {
  if (msg.role === 'user') {
    return { name: '客户', icon: User, avatarClass: 'avatar-user', emoji: '' }
  }
  if (msg.role === 'bot') {
    return { name: '宏小二', icon: null, avatarClass: 'avatar-bot', emoji: '' }
  }
  if (msg.role === 'agent') {
    const stageOrder = (msg.metadata as { level?: number; stageOrder?: number })?.stageOrder
      ?? (msg.metadata as { level?: number })?.level
    if (stageOrder && stageOrder >= 2) {
      return { name: '资深专家', icon: StarFilled, avatarClass: 'avatar-expert', emoji: '' }
    }
    return { name: '人工客服', icon: Service, avatarClass: 'avatar-agent', emoji: '' }
  }
  return { name: '系统', icon: BellFilled, avatarClass: 'avatar-system', emoji: '' }
}

function handleSend() {
  if (!reply.value.trim() || !props.task) return
  const newMsg: Message = {
    id: crypto.randomUUID(),
    role: 'agent',
    content: reply.value.trim(),
    timestamp: Date.now(),
    type: 'text',
    metadata: { stageCode: props.task.currentStageCode, stageOrder: props.task.currentStageOrder },
  }
  emit('update-task', {
    ...props.task,
    chatHistory: [...props.task.chatHistory, newMsg],
    updatedAt: Date.now(),
  })
  if (props.task.customerId) {
    chatStore.persistMessage(props.task.customerId, newMsg)
  }
  reply.value = ''
}

function handleAdvance() {
  if (!props.task) return
  const systemMsg: Message = {
    id: crypto.randomUUID(),
    role: 'bot',
    content: `正在为您转接${props.nextStageLabel ?? '下一阶段专席'}，请稍后`,
    timestamp: Date.now(),
    type: 'system',
  }
  emit('update-task', {
    ...props.task,
    chatHistory: [...props.task.chatHistory, systemMsg],
    updatedAt: Date.now(),
  })
  emit('advance', props.task.id)
}
</script>

<style lang="scss" scoped>
.chat-panel {
  flex: 1;
  display: flex;
  flex-direction: column;
  background-color: #fff;
  border-right: 1px solid var(--border-color);
  min-width: 0;
  overflow: hidden;
}

.chat-empty {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 48px;
  text-align: center;

  .empty-icon {
    width: 80px;
    height: 80px;
    background-color: var(--gray-color);
    border-radius: 50%;
    display: flex;
    align-items: center;
    justify-content: center;
    margin-bottom: 20px;
  }

  h3 {
    font-size: 15px;
    font-weight: 700;
    color: var(--title-color);
    margin-bottom: 8px;
  }

  p {
    font-size: 12px;
    color: var(--label-desc-color);
    line-height: 1.6;
    max-width: 240px;
  }
}

.chat-header {
  flex-shrink: 0;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 10px 16px;
  border-bottom: 1px solid var(--blod-border-color);
}

.chat-header-left {
  display: flex;
  align-items: center;
  gap: 6px;
}

.chat-header-title {
  font-size: 12px;
  font-weight: 700;
  color: var(--text-color);
}

.chat-task-id {
  font-size: 11px;
  color: var(--label-desc-color);
}

.chat-messages {
  flex: 1;
  overflow-y: auto;
  padding: 12px 16px;
  background-color: rgba(247, 247, 247, 0.5);
  display: flex;
  flex-direction: column;
  gap: 12px;
  min-height: 0;
}

.session-start-tag {
  display: flex;
  justify-content: center;

  span {
    font-size: 10px;
    background-color: var(--border-color);
    color: var(--label-desc-color);
    padding: 3px 12px;
    border-radius: 20px;
    font-weight: 700;
    letter-spacing: 0.05em;
  }
}

.msg-row {
  display: flex;
  gap: 8px;

  &.msg-right {
    flex-direction: row-reverse;
  }
}

.msg-system {
  width: 100%;
  display: flex;
  justify-content: center;

  span {
    font-size: 10px;
    background-color: var(--border-color);
    color: var(--label-desc-color);
    padding: 3px 12px;
    border-radius: 20px;
    font-weight: 700;
  }
}

.msg-avatar {
  width: 28px;
  height: 28px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;

  &.avatar-user {
    background-color: var(--gray-color);
    color: var(--label-desc-color);
  }

  &.avatar-bot {
    background-color: #fff;
    color: var(--primary-color);
    border: 1.5px solid rgba(0, 167, 88, 0.2);
    box-shadow: 0 1px 4px rgba(0, 0, 0, 0.06);
  }

  &.avatar-agent {
    background-color: #3b82f6;
    color: #fff;
  }

  &.avatar-expert {
    background-color: var(--orange);
    color: #fff;
  }

  &.avatar-system {
    background-color: var(--border-color);
    color: var(--label-desc-color);
  }
}

.msg-body {
  display: flex;
  flex-direction: column;
  gap: 2px;
  max-width: 80%;
}

.msg-sender-name {
  font-size: 10px;
  font-weight: 700;
  color: var(--desc-color);
  padding: 0 4px;

  &.text-right {
    text-align: right;
  }
}

.msg-bubble {
  padding: 10px 12px;
  border-radius: 12px;
  font-size: 13px;
  line-height: 1.6;
  word-break: break-word;

  &.bubble-user {
    background-color: #fff;
    color: var(--text-color);
    border-radius: 12px 12px 12px 2px;
    border: 1px solid var(--border-color);
    box-shadow: 0 1px 3px rgba(0, 0, 0, 0.04);
  }

  &.bubble-agent {
    background-color: var(--primary-color);
    color: #fff;
    border-radius: 12px 12px 2px 12px;
  }
}

.msg-time {
  font-size: 10px;
  color: var(--desc-color);
  padding: 0 4px;

  &.text-right {
    text-align: right;
  }
}

.reply-area {
  flex-shrink: 0;
  padding: 10px 12px;
  background-color: #fff;
  border-top: 1px solid var(--border-color);
}

.reply-toolbar {
  display: flex;
  gap: 4px;
  margin-bottom: 6px;
}

.reply-input-wrap {
  background-color: var(--gray-color);
  border-radius: 8px;
  border: 1px solid var(--border-color);
  transition: border-color 0.2s;

  &.focused {
    border-color: rgba(0, 167, 88, 0.5);
  }
}

.reply-textarea {
  width: 100%;
  background: transparent;
  border: none;
  padding: 10px 12px;
  font-size: 13px;
  color: var(--text-color);
  min-height: 56px;
  resize: none;
  font-family: inherit;

  &::placeholder {
    color: var(--desc-color);
  }
}

.reply-actions {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 6px 8px;
  border-top: 1px solid var(--blod-border-color);
}

.reply-actions-left,
.reply-actions-right {
  display: flex;
  align-items: center;
  gap: 4px;
}
</style>
