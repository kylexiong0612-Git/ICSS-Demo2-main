<template>
  <div class="trend-chart">
    <v-chart class="chart" :option="option" autoresize />
  </div>
</template>

<script setup lang="ts">
import { use } from 'echarts/core'
import { LineChart } from 'echarts/charts'
import {
  GridComponent,
  TooltipComponent,
  LegendComponent,
} from 'echarts/components'
import { CanvasRenderer } from 'echarts/renderers'
import VChart from 'vue-echarts'
import { computed } from 'vue'

use([LineChart, GridComponent, TooltipComponent, LegendComponent, CanvasRenderer])

const MOCK_STATS = [
  { name: '09:00', count: 45 },
  { name: '10:00', count: 82 },
  { name: '11:00', count: 120 },
  { name: '12:00', count: 95 },
  { name: '13:00', count: 110 },
  { name: '14:00', count: 154 },
  { name: '15:00', count: 130 },
]

const option = computed(() => ({
  grid: {
    top: 10,
    right: 10,
    bottom: 24,
    left: 36,
    containLabel: false,
  },
  xAxis: {
    type: 'category',
    data: MOCK_STATS.map(d => d.name),
    axisLine: { show: false },
    axisTick: { show: false },
    axisLabel: { fontSize: 10, color: '#94a3b8' },
  },
  yAxis: {
    type: 'value',
    axisLine: { show: false },
    axisTick: { show: false },
    axisLabel: { fontSize: 10, color: '#94a3b8' },
    splitLine: { lineStyle: { color: '#f1f5f9', type: 'dashed' } },
  },
  tooltip: {
    trigger: 'axis',
    borderRadius: 8,
    borderWidth: 0,
    backgroundColor: '#fff',
    shadowBlur: 12,
    shadowColor: 'rgba(0,0,0,0.1)',
    textStyle: { color: '#282b3e', fontSize: 12 },
  },
  series: [
    {
      name: '任务量',
      type: 'line',
      data: MOCK_STATS.map(d => d.count),
      smooth: true,
      lineStyle: { color: '#00a758', width: 3 },
      itemStyle: { color: '#00a758', borderColor: '#fff', borderWidth: 2 },
      symbol: 'circle',
      symbolSize: 8,
      areaStyle: {
        color: {
          type: 'linear',
          x: 0,
          y: 0,
          x2: 0,
          y2: 1,
          colorStops: [
            { offset: 0, color: 'rgba(0,167,88,0.15)' },
            { offset: 1, color: 'rgba(0,167,88,0)' },
          ],
        },
      },
    },
  ],
}))
</script>

<style lang="scss" scoped>
.trend-chart {
  height: 200px;
}

.chart {
  width: 100%;
  height: 100%;
}
</style>
