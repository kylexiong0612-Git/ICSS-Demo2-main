<template>
  <div class="issue-list">
    <div v-for="(bug, i) in issues" :key="i" class="issue-item">
      <div class="issue-left">
        <el-tag
          :type="priorityType(bug.priority)"
          size="small"
          class="priority-tag"
        >
          {{ bug.priority }}
        </el-tag>
        <span class="issue-desc">{{ bug.desc }}</span>
      </div>
      <el-tag
        :type="statusType(bug.status)"
        size="small"
        effect="light"
      >
        {{ bug.status }}
      </el-tag>
    </div>
  </div>
</template>

<script setup lang="ts">
const issues = [
  { id: 'BUG-102', desc: '理赔上传附件接口超时', user: '坐席-李芳', status: '处理中', priority: 'P1' },
  { id: 'BUG-105', desc: '保单详情页数据脱敏异常', user: '坐席-王强', status: '待确认', priority: 'P0' },
  { id: 'BUG-108', desc: '宏小二意图识别率下降', user: '系统自动', status: '已修复', priority: 'P2' },
]

function priorityType(priority: string): 'danger' | 'warning' | 'info' {
  if (priority === 'P0') return 'danger'
  if (priority === 'P1') return 'warning'
  return 'info'
}

function statusType(status: string): 'success' | 'warning' {
  return status === '已修复' ? 'success' : 'warning'
}
</script>

<style lang="scss" scoped>
.issue-list {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.issue-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  background-color: var(--gray-color);
  border-radius: 8px;
  padding: 10px 12px;
  transition: background-color 0.2s;

  &:hover {
    background-color: var(--blod-border-color);
  }
}

.issue-left {
  display: flex;
  align-items: center;
  gap: 8px;
  min-width: 0;
}

.priority-tag {
  flex-shrink: 0;
}

.issue-desc {
  font-size: 13px;
  font-weight: 600;
  color: var(--text-color);
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}
</style>
