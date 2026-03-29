<template>
  <div class="stats-grid">
    <div v-for="(stat, i) in stats" :key="i" class="stat-card">
      <div class="stat-icon" :style="{ background: stat.bg }">
        <el-icon size="18" :color="stat.color">
          <component :is="stat.icon" />
        </el-icon>
      </div>
      <div class="stat-body">
        <p class="stat-label">{{ stat.label }}</p>
        <div class="stat-value-row">
          <span class="stat-value">{{ stat.value }}</span>
          <span class="stat-trend" :class="stat.trendType">
            <el-icon v-if="stat.trendType === 'up'" size="10"><ArrowUpBold /></el-icon>
            <el-icon v-else-if="stat.trendType === 'down'" size="10"><ArrowDownBold /></el-icon>
            {{ stat.trend }}
          </span>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import {
  Clock,
  User,
  Lightning,
  CircleCheck,
  ArrowUpBold,
  ArrowDownBold,
} from '@element-plus/icons-vue'

const stats = [
  {
    label: '待分配任务',
    value: '12',
    icon: Clock,
    color: '#3b82f6',
    bg: '#eff6ff',
    trend: '+2',
    trendType: 'up',
  },
  {
    label: '在线坐席',
    value: '48',
    icon: User,
    color: '#10b981',
    bg: '#ecfdf5',
    trend: '稳定',
    trendType: 'neutral',
  },
  {
    label: '平均处理时长',
    value: '4.2m',
    icon: Lightning,
    color: '#f59e0b',
    bg: '#fffbeb',
    trend: '-12%',
    trendType: 'down',
  },
  {
    label: '一次解决率',
    value: '84%',
    icon: CircleCheck,
    color: '#00a758',
    bg: '#ecfdf5',
    trend: '+5%',
    trendType: 'up',
  },
]
</script>

<style lang="scss" scoped>
.stats-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 12px;
}

.stat-card {
  background-color: #fff;
  border-radius: 12px;
  border: 1px solid var(--blod-border-color);
  padding: 16px;
  display: flex;
  align-items: center;
  gap: 12px;
}

.stat-icon {
  width: 40px;
  height: 40px;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.stat-body {
  min-width: 0;
  flex: 1;
}

.stat-label {
  font-size: 11px;
  font-weight: 700;
  color: var(--desc-color);
  text-transform: uppercase;
  letter-spacing: 0.05em;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.stat-value-row {
  display: flex;
  align-items: baseline;
  gap: 8px;
  margin-top: 4px;
}

.stat-value {
  font-size: 22px;
  font-weight: 700;
  color: var(--text-color);
}

.stat-trend {
  display: flex;
  align-items: center;
  gap: 2px;
  font-size: 11px;
  font-weight: 700;
  padding: 2px 6px;
  border-radius: 20px;

  &.up {
    background-color: #d1fae5;
    color: #10b981;
  }

  &.down {
    background-color: #fee2e2;
    color: #ef4444;
  }

  &.neutral {
    background-color: var(--gray-color);
    color: var(--label-desc-color);
  }
}
</style>
