<template>
  <div class="chat-interface">
    <!-- 聊天顶部 -->
    <header class="chat-header">
      <button class="back-btn" @click="$emit('close')">
        <el-icon size="24" color="#fff"><ArrowLeft /></el-icon>
      </button>
      <div class="chat-title-group">
        <h2 class="chat-title">宏小二智能服务</h2>
        <div class="online-indicator">
          <div class="online-dot" />
          <span>在线服务中</span>
        </div>
      </div>
      <button class="more-btn">
        <el-icon size="24" color="#fff"><MoreFilled /></el-icon>
      </button>
    </header>

    <!-- 消息区域 -->
    <div ref="scrollRef" class="chat-messages custom-scrollbar">
      <TransitionGroup name="msg" tag="div" class="messages-inner">
        <div
          v-for="msg in messages"
          :key="msg.id"
          class="msg-wrapper"
          :class="msg.role === 'user' ? 'msg-user' : 'msg-other'"
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
                <svg width="18" height="18" viewBox="0 0 1024 1024" fill="currentColor" xmlns="http://www.w3.org/2000/svg">
                  <path d="M22.8 496.7H76v217.2H22.8V496.7z m922 0H998v217.2h-53.2V496.7z"/>
                  <path d="M310.3 589.3c0 27.6 22.4 50 50 50s50-22.4 50-50c0-17.9-9.5-34.4-25-43.3-15.5-8.9-34.6-8.9-50 0-15.5 8.9-25 25.4-25 43.3z m0 0M604.1 589.3c0 17.9 9.5 34.4 25 43.3 15.5 8.9 34.6 8.9 50 0 15.5-8.9 25-25.5 25-43.3 0-17.9-9.5-34.4-25-43.3-15.5-8.9-34.6-8.9-50 0-15.4 8.9-25 25.4-25 43.3z m0 0"/>
                  <path d="M854.1 299.7H533.8V133.4c14.1-8.9 23.4-24.5 23.4-42.4 0-27.7-22.4-50-50-50-27.7 0-50 22.4-50 50 0 17.9 9.4 33.4 23.4 42.4v166.3H166.7c-24.7 0-44.7 20-44.7 44.7V913c0 24.7 20 44.7 44.7 44.7h687.2c24.7 0 44.7-20 44.7-44.7V344.4c0.2-24.7-20-44.7-44.5-44.7z m-8.5 604.8H175.2V352.9h670.2v551.5h0.2z"/>
                </svg>
              </template>
              <template v-else-if="senderInfo(msg).emoji">{{ senderInfo(msg).emoji }}</template>
              <el-icon v-else size="16"><component :is="senderInfo(msg).icon" /></el-icon>
            </div>
            <div class="msg-body">
              <span v-if="msg.role !== 'user'" class="msg-sender-name">
                {{ senderInfo(msg).name }}
              </span>
              <div
                class="msg-bubble"
                :class="msg.role === 'user' ? 'bubble-user' : 'bubble-other'"
              >
                {{ msg.content }}
              </div>
            </div>
          </template>
        </div>
      </TransitionGroup>

      <!-- Loading 气泡 -->
      <div v-if="isLoading" class="msg-wrapper msg-other">
        <div class="msg-avatar avatar-bot">
          <svg width="18" height="18" viewBox="0 0 1024 1024" fill="currentColor" xmlns="http://www.w3.org/2000/svg">
            <path d="M22.8 496.7H76v217.2H22.8V496.7z m922 0H998v217.2h-53.2V496.7z"/>
            <path d="M310.3 589.3c0 27.6 22.4 50 50 50s50-22.4 50-50c0-17.9-9.5-34.4-25-43.3-15.5-8.9-34.6-8.9-50 0-15.5 8.9-25 25.4-25 43.3z m0 0M604.1 589.3c0 17.9 9.5 34.4 25 43.3 15.5 8.9 34.6 8.9 50 0 15.5-8.9 25-25.5 25-43.3 0-17.9-9.5-34.4-25-43.3-15.5-8.9-34.6-8.9-50 0-15.4 8.9-25 25.4-25 43.3z m0 0"/>
            <path d="M854.1 299.7H533.8V133.4c14.1-8.9 23.4-24.5 23.4-42.4 0-27.7-22.4-50-50-50-27.7 0-50 22.4-50 50 0 17.9 9.4 33.4 23.4 42.4v166.3H166.7c-24.7 0-44.7 20-44.7 44.7V913c0 24.7 20 44.7 44.7 44.7h687.2c24.7 0 44.7-20 44.7-44.7V344.4c0.2-24.7-20-44.7-44.5-44.7z m-8.5 604.8H175.2V352.9h670.2v551.5h0.2z"/>
          </svg>
        </div>
        <div class="loading-bubble">
          <div class="dot" />
          <div class="dot dot-2" />
          <div class="dot dot-3" />
        </div>
      </div>
    </div>

    <!-- 快捷操作 -->
    <div class="quick-actions no-scrollbar">
      <button
        v-for="action in quickActions"
        :key="action"
        class="quick-btn"
        @click="$emit('send', action)"
      >
        {{ action }}
      </button>
    </div>

    <!-- 输入区 -->
    <div class="input-area">
      <div class="input-wrap" :class="{ focused: isInputFocused }">
        <el-icon size="20" class="input-icon"><Plus /></el-icon>
        <input
          v-model="inputText"
          class="chat-input"
          placeholder="请输入您的问题..."
          @focus="isInputFocused = true"
          @blur="isInputFocused = false"
          @keydown.enter="handleSend"
        />
        <button
          class="send-btn"
          :class="{ active: inputText.trim() }"
          :disabled="!inputText.trim() || isLoading"
          @click="handleSend"
        >
          <el-icon size="18"><Promotion /></el-icon>
        </button>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, watch, nextTick } from 'vue'
import {
  ArrowLeft,
  MoreFilled,
  Plus,
  Promotion,
  User,
  Service,
  StarFilled,
  BellFilled,
} from '@element-plus/icons-vue'
import type { Message } from '@/types'

const props = defineProps<{
  messages: Message[]
  isLoading: boolean
  isTransferring: boolean
}>()

const emit = defineEmits<{
  close: []
  send: [text: string]
}>()

const inputText = ref('')
const isInputFocused = ref(false)
const scrollRef = ref<HTMLDivElement | null>(null)

const quickActions = ['理赔指引', '保单查询', '转人工', '投诉建议']

// 新消息时滚动到底部
watch(
  () => [props.messages.length, props.isLoading],
  async () => {
    await nextTick()
    if (scrollRef.value) {
      scrollRef.value.scrollTop = scrollRef.value.scrollHeight
    }
  },
  { immediate: true },
)

function senderInfo(msg: Message) {
  if (msg.role === 'user') return { name: '我', icon: User, avatarClass: 'avatar-user', emoji: '' }
  if (msg.role === 'bot') return { name: '宏小二', icon: null, avatarClass: 'avatar-bot', emoji: '' }
  if (msg.role === 'agent') {
    if ((msg.metadata as { level?: number })?.level === 2)
      return { name: '资深专家', icon: StarFilled, avatarClass: 'avatar-expert', emoji: '' }
    return { name: '人工客服', icon: Service, avatarClass: 'avatar-agent', emoji: '' }
  }
  return { name: '系统', icon: BellFilled, avatarClass: 'avatar-system', emoji: '' }
}

function handleSend() {
  if (!inputText.value.trim() || props.isLoading) return
  emit('send', inputText.value.trim())
  inputText.value = ''
}
</script>

<style lang="scss" scoped>
.chat-interface {
  flex: 1;
  display: flex;
  flex-direction: column;
  background-color: #fff;
  overflow: hidden;
}

.chat-header {
  flex-shrink: 0;
  background-color: var(--primary-color);
  padding-top: 44px;
  padding-bottom: 14px;
  padding-left: 14px;
  padding-right: 14px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  color: #fff;
}

.back-btn,
.more-btn {
  padding: 4px;
  border-radius: 50%;
  transition: background-color 0.15s;

  &:hover {
    background-color: rgba(255, 255, 255, 0.1);
  }
}

.chat-title-group {
  display: flex;
  flex-direction: column;
  align-items: center;
}

.chat-title {
  font-size: 16px;
  font-weight: 700;
}

.online-indicator {
  display: flex;
  align-items: center;
  gap: 4px;
  margin-top: 2px;
}

.online-dot {
  width: 6px;
  height: 6px;
  border-radius: 50%;
  background-color: #ffd700;
  animation: pulse 1.5s infinite;
}

@keyframes pulse {
  0%, 100% { opacity: 1; }
  50% { opacity: 0.4; }
}

.online-indicator span {
  font-size: 11px;
  opacity: 0.8;
}

// 消息区域
.chat-messages {
  flex: 1;
  overflow-y: auto;
  padding: 16px;
  background-color: var(--gray-color);
  min-height: 0;
}

.messages-inner {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.msg-wrapper {
  display: flex;
  gap: 8px;

  &.msg-user {
    flex-direction: row-reverse;
  }
}

.msg-system {
  width: 100%;
  display: flex;
  justify-content: center;

  span {
    font-size: 11px;
    background-color: var(--border-color);
    color: var(--label-desc-color);
    padding: 4px 14px;
    border-radius: 20px;
    font-weight: 700;
    text-transform: uppercase;
    letter-spacing: 0.05em;
  }
}

.msg-avatar {
  width: 32px;
  height: 32px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  box-shadow: 0 2px 6px rgba(0, 0, 0, 0.1);

  &.avatar-user {
    background-color: var(--primary-color);
    color: #fff;
  }

  &.avatar-bot {
    background-color: #fff;
    color: var(--primary-color);
    border: 1.5px solid rgba(0, 167, 88, 0.2);
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
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
  gap: 4px;
  max-width: 85%;
}

.msg-sender-name {
  font-size: 11px;
  font-weight: 700;
  color: var(--desc-color);
  margin-left: 4px;
}

.msg-bubble {
  padding: 12px;
  border-radius: 16px;
  font-size: 14px;
  line-height: 1.5;
  word-break: break-word;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.06);

  &.bubble-user {
    background-color: var(--primary-color);
    color: #fff;
    border-radius: 16px 16px 2px 16px;
  }

  &.bubble-other {
    background-color: #fff;
    color: var(--text-color);
    border-radius: 16px 16px 16px 2px;
    border: 1px solid var(--border-color);
  }
}

// Loading 气泡
.loading-bubble {
  display: flex;
  align-items: center;
  gap: 4px;
  background-color: #fff;
  padding: 14px;
  border-radius: 16px 16px 16px 2px;
  border: 1px solid var(--border-color);
}

.dot {
  width: 6px;
  height: 6px;
  border-radius: 50%;
  background-color: rgba(0, 167, 88, 0.4);
  animation: bounce 1s infinite;

  &.dot-2 {
    animation-delay: 0.2s;
  }

  &.dot-3 {
    animation-delay: 0.4s;
  }
}

@keyframes bounce {
  0%, 60%, 100% { transform: translateY(0); }
  30% { transform: translateY(-6px); }
}

// 快捷操作
.quick-actions {
  flex-shrink: 0;
  padding: 8px 14px;
  background-color: var(--search-bg);
  display: flex;
  gap: 8px;
  overflow-x: auto;
  border-top: 1px solid var(--blod-border-color);
}

.quick-btn {
  flex-shrink: 0;
  padding: 6px 14px;
  background-color: #fff;
  border: 1px solid var(--border-color);
  border-radius: 20px;
  font-size: 12px;
  color: var(--title-color);
  cursor: pointer;
  transition: all 0.15s;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.04);

  &:hover {
    border-color: var(--primary-color);
    color: var(--primary-color);
  }
}

// 输入区
.input-area {
  flex-shrink: 0;
  padding: 14px;
  background-color: #fff;
  border-top: 1px solid var(--border-color);
  padding-bottom: calc(14px + env(safe-area-inset-bottom));
}

.input-wrap {
  display: flex;
  align-items: center;
  gap: 10px;
  background-color: var(--gray-color);
  border-radius: 24px;
  padding: 8px 14px;
  transition: all 0.2s;

  &.focused {
    background-color: #fff;
    box-shadow: 0 0 0 2px rgba(0, 167, 88, 0.2);
  }
}

.input-icon {
  color: var(--desc-color);
  flex-shrink: 0;
  cursor: pointer;

  &:hover {
    color: var(--primary-color);
  }
}

.chat-input {
  flex: 1;
  background: transparent;
  border: none;
  font-size: 14px;
  color: var(--text-color);
  padding: 4px 0;

  &::placeholder {
    color: var(--desc-color);
  }
}

.send-btn {
  width: 36px;
  height: 36px;
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: var(--desc-color);
  flex-shrink: 0;
  transition: all 0.15s;

  &.active {
    background-color: var(--primary-color);
    color: #fff;
    box-shadow: 0 2px 8px rgba(0, 167, 88, 0.3);
  }

  &:disabled {
    opacity: 0.5;
    cursor: not-allowed;
  }
}

// 消息进场动画
.msg-enter-active {
  transition: opacity 0.2s ease, transform 0.2s ease;
}

.msg-enter-from {
  opacity: 0;
  transform: translateY(8px) scale(0.96);
}
</style>
