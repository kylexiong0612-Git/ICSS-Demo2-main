<template>
  <div class="main-container">
    <div class="main-breadcrumb">
      <el-breadcrumb separator="/">
        <el-breadcrumb-item>坐席工作台</el-breadcrumb-item>
        <el-breadcrumb-item>{{ level === 1 ? '一线坐席' : '二线坐席' }}</el-breadcrumb-item>
      </el-breadcrumb>
    </div>

    <div class="main-area workstation-area">
      <TaskList
        :level="level"
        :selected-task-id="selectedTask?.id"
        @select="handleSelectTask"
        @grab="taskStore.grabTask($event)"
      />
      <ChatPanel
        :task="selectedTask"
        :level="level"
        @update-task="taskStore.updateTask($event)"
        @escalate="taskStore.escalateTask($event)"
        @grab="taskStore.grabTask($event)"
      />
      <CustomerPanel :task="selectedTask" @fault-reported="handleFaultReported" />
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, watch } from 'vue'
import TaskList from '@/components/agent/TaskList.vue'
import ChatPanel from '@/components/agent/ChatPanel.vue'
import CustomerPanel from '@/components/agent/CustomerPanel.vue'
import { useTaskStore } from '@/stores/taskStore'
import type { ServiceTask, Message } from '@/types'

const props = defineProps<{ level: 1 | 2 }>()

const taskStore = useTaskStore()
const selectedTask = ref<ServiceTask | null>(null)

// 当 store 中任务更新时，同步 selectedTask
watch(
  () => taskStore.tasks,
  tasks => {
    if (selectedTask.value) {
      const updated = tasks.find(t => t.id === selectedTask.value!.id)
      if (updated) selectedTask.value = { ...updated }
    }
  },
  { deep: true },
)

// 切换 level 时清空选中
watch(() => props.level, () => {
  selectedTask.value = null
})

function handleSelectTask(task: ServiceTask) {
  selectedTask.value = { ...task }
  taskStore.markRead(task.id)
}

function handleFaultReported(faultTaskId: string) {
  if (!selectedTask.value) return
  const systemMsg: Message = {
    id: crypto.randomUUID(),
    role: 'bot',
    content: `已生成报障工单 #${faultTaskId}`,
    timestamp: Date.now(),
    type: 'system',
  }
  taskStore.updateTask({
    ...selectedTask.value,
    chatHistory: [...selectedTask.value.chatHistory, systemMsg],
    updatedAt: Date.now(),
  })
}
</script>

<style lang="scss" scoped>
.workstation-area {
  display: flex;
  gap: 0;
  padding: 0;
  background-color: var(--bg-color);
}
</style>
