<template>
  <div class="category-chart">
    <div class="chart-wrap">
      <v-chart class="chart" :option="option" autoresize />
    </div>
    <div class="legend-list">
      <div v-for="(item, i) in CATEGORY_DATA" :key="i" class="legend-item">
        <div class="legend-dot" :style="{ backgroundColor: COLORS[i] }" />
        <span class="legend-name">{{ item.name }}</span>
        <span class="legend-value">{{ item.value }}</span>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { use } from 'echarts/core'
import { PieChart } from 'echarts/charts'
import { TooltipComponent, LegendComponent } from 'echarts/components'
import { CanvasRenderer } from 'echarts/renderers'
import VChart from 'vue-echarts'
import { computed } from 'vue'

use([PieChart, TooltipComponent, LegendComponent, CanvasRenderer])

const CATEGORY_DATA = [
  { name: '理赔咨询', value: 400 },
  { name: '保单变更', value: 300 },
  { name: '投诉建议', value: 100 },
  { name: '产品咨询', value: 200 },
]

const COLORS = ['#008542', '#FFD700', '#FF4444', '#4488FF']

const option = computed(() => ({
  tooltip: {
    trigger: 'item',
    borderRadius: 8,
    borderWidth: 0,
    backgroundColor: '#fff',
    shadowBlur: 12,
    shadowColor: 'rgba(0,0,0,0.1)',
    textStyle: { color: '#282b3e', fontSize: 12 },
    formatter: '{b}: {c} ({d}%)',
  },
  series: [
    {
      type: 'pie',
      radius: ['50%', '75%'],
      center: ['50%', '50%'],
      padAngle: 3,
      data: CATEGORY_DATA.map((d, i) => ({
        value: d.value,
        name: d.name,
        itemStyle: { color: COLORS[i] },
      })),
      label: { show: false },
    },
  ],
}))
</script>

<style lang="scss" scoped>
.category-chart {
  display: flex;
  align-items: center;
  gap: 16px;
}

.chart-wrap {
  width: 110px;
  height: 110px;
  flex-shrink: 0;
}

.chart {
  width: 100%;
  height: 100%;
}

.legend-list {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.legend-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.legend-dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  flex-shrink: 0;
  margin-right: 6px;
}

.legend-name {
  font-size: 12px;
  color: var(--title-color);
  flex: 1;
}

.legend-value {
  font-size: 12px;
  font-weight: 700;
  color: var(--text-color);
}
</style>
