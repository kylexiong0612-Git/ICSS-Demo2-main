<template>
  <div class="task-list-panel">
    <!-- 标签页切换 -->
    <div class="tab-bar">
      <div v-if="showPublicPool !== false && showPersonalPool !== false" class="tab-switcher">
        <button
          class="tab-btn"
          :class="{ active: activeTab === 'public' }"
          @click="activeTab = 'public'"
        >
          任务公共池 ({{ publicCount }})
        </button>
        <button
          class="tab-btn"
          :class="{ active: activeTab === 'personal' }"
          @click="activeTab = 'personal'"
        >
          个人工作池 ({{ personalCount }})
        </button>
      </div>
      <div v-else class="single-tab">
        <span>{{ showPublicPool === false ? '个人工作池' : '任务公共池' }}</span>
        <b>{{ showPublicPool === false ? personalCount : publicCount }}</b>
      </div>
      <div class="tab-extra">
        <el-icon size="13" class="extra-icon"><Clock /></el-icon>
        <span>{{ workflowCode }} / {{ stageCode }}</span>
      </div>
    </div>

    <!-- 搜索栏 -->
    <div class="search-bar">
      <el-input
        v-model="searchKey"
        size="small"
        placeholder="搜索任务ID/客户名..."
        :prefix-icon="Search"
      />
      <el-button size="small" :icon="Refresh" circle @click="searchKey = ''" />
    </div>

    <!-- 列表头 -->
    <div class="list-header">
      <span>任务列表</span>
      <el-button text size="small" :icon="Filter" type="primary">筛选</el-button>
    </div>

    <!-- 任务列表 -->
    <div class="task-scroll custom-scrollbar">
      <div
        v-for="task in filteredTasks"
        :key="task.id"
        class="task-item"
        :class="{ selected: selectedTaskId === task.id }"
        @click="$emit('select', task)"
      >
        <el-badge
          v-if="task.unreadCount && task.unreadCount > 0"
          :value="task.unreadCount"
          class="unread-badge"
        />
        <div class="task-meta">
          <span class="task-id">{{ task.id }}</span>
          <el-tag
            size="small"
            :type="task.requestSource === 'Agent' ? 'warning' : 'primary'"
            effect="plain"
          >
            {{ task.requestSource === 'Agent' ? '渠道' : '客户' }}
          </el-tag>
          <el-tag
            size="small"
            :type="task.priority === 'High' || task.priority === 'Urgent' ? 'danger' : 'info'"
            effect="plain"
          >
            {{ task.priority }}
          </el-tag>
        </div>
        <p class="task-summary">{{ task.summary }}</p>
        <div class="task-footer">
          <span class="task-time">
            {{ new Date(task.createdAt).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' }) }}
          </span>
          <el-button
            v-if="activeTab === 'public' && showPublicPool !== false"
            size="small"
            type="primary"
            @click.stop="$emit('grab', task.id)"
          >
            领取
          </el-button>
        </div>
      </div>

      <div v-if="filteredTasks.length === 0" class="empty-tip">
        暂无待处理任务
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'
import { Search, Refresh, Filter, Clock } from '@element-plus/icons-vue'
import { useTaskStore } from '@/stores/taskStore'
import type { ServiceTask } from '@/types'

const props = defineProps<{
  workflowCode: string
  stageCode: string
  selectedTaskId?: string
  showPublicPool?: boolean
  showPersonalPool?: boolean
}>()

defineEmits<{
  select: [task: ServiceTask]
  grab: [taskId: string]
}>()

const taskStore = useTaskStore()
const activeTab = ref<'public' | 'personal'>(props.showPublicPool === false ? 'personal' : 'public')
const searchKey = ref('')

const publicCount = computed(
  () =>
    taskStore.tasks.filter(
      t =>
        t.workflowCode === props.workflowCode &&
        t.currentStageCode === props.stageCode &&
        t.status === 'Pending',
    ).length,
)
const personalCount = computed(
  () =>
    taskStore.tasks.filter(
      t =>
        t.workflowCode === props.workflowCode &&
        t.currentStageCode === props.stageCode &&
        t.status === 'Processing' &&
        t.assignedTo === 'ME',
    ).length,
)

const filteredTasks = computed(() => {
  let list = taskStore.tasks.filter(
    t =>
      t.workflowCode === props.workflowCode &&
      t.currentStageCode === props.stageCode &&
      (activeTab.value === 'public'
        ? t.status === 'Pending'
        : t.status === 'Processing' && t.assignedTo === 'ME'),
  )
  if (searchKey.value.trim()) {
    const key = searchKey.value.trim().toLowerCase()
    list = list.filter(
      t => t.id.toLowerCase().includes(key) || t.summary.toLowerCase().includes(key),
    )
  }
  return list
})
</script>

<style lang="scss" scoped>
.task-list-panel {
  width: 240px;
  flex-shrink: 0;
  display: flex;
  flex-direction: column;
  background-color: #fff;
  border-right: 1px solid var(--border-color);
  height: 100%;
  overflow: hidden;
}

.tab-bar {
  padding: 8px 12px;
  border-bottom: 1px solid var(--blod-border-color);
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.tab-switcher {
  display: flex;
  background-color: var(--gray-color);
  border-radius: 6px;
  padding: 2px;
  gap: 2px;
}

.single-tab {
  display: flex;
  align-items: center;
  justify-content: space-between;
  font-size: 12px;
  font-weight: 700;
  color: var(--text-color);
}

.tab-btn {
  flex: 1;
  padding: 4px 8px;
  border-radius: 4px;
  font-size: 11px;
  font-weight: 700;
  color: var(--label-desc-color);
  transition: all 0.15s;
  white-space: nowrap;

  &:hover {
    color: var(--title-color);
  }

  &.active {
    background-color: #fff;
    color: var(--primary-color);
    box-shadow: 0 1px 3px rgba(0, 0, 0, 0.08);
  }
}

.tab-extra {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 11px;
  color: var(--label-desc-color);

  b {
    color: var(--text-color);
  }
}

.extra-icon {
  color: var(--label-desc-color);
}

.search-bar {
  padding: 8px 12px;
  display: flex;
  align-items: center;
  gap: 6px;
  border-bottom: 1px solid var(--blod-border-color);
}

.list-header {
  padding: 8px 12px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  font-size: 12px;
  font-weight: 700;
  color: var(--text-color);
  border-bottom: 1px solid var(--blod-border-color);
}

.task-scroll {
  flex: 1;
  overflow-y: auto;
}

.task-item {
  position: relative;
  padding: 12px;
  border-bottom: 1px solid var(--blod-border-color);
  cursor: pointer;
  transition: background-color 0.15s;

  &:hover {
    background-color: var(--gray-color);
  }

  &.selected {
    background-color: rgba(0, 167, 88, 0.04);
    border-left: 2px solid var(--primary-color);
  }
}

.unread-badge {
  position: absolute;
  top: 10px;
  right: 10px;
}

.task-meta {
  display: flex;
  align-items: center;
  gap: 4px;
  margin-bottom: 6px;
}

.task-id {
  font-size: 10px;
  font-weight: 700;
  color: var(--desc-color);
}

.task-summary {
  font-size: 12px;
  color: var(--text-color);
  line-height: 1.5;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
  margin-bottom: 8px;
}

.task-footer {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.task-time {
  font-size: 10px;
  color: var(--label-desc-color);
}

.empty-tip {
  padding: 24px;
  text-align: center;
  font-size: 12px;
  color: var(--desc-color);
}
</style>
