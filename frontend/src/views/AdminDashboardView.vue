<template>
  <div class="main-container">
    <div class="main-breadcrumb">
      <el-breadcrumb separator="/">
        <el-breadcrumb-item>管理端</el-breadcrumb-item>
        <el-breadcrumb-item>运营监控中心</el-breadcrumb-item>
      </el-breadcrumb>
      <div class="breadcrumb-actions">
        <el-select v-model="dateRange" size="small" style="width: 100px">
          <el-option label="今日" value="today" />
          <el-option label="昨日" value="yesterday" />
          <el-option label="近7日" value="week" />
        </el-select>
        <el-button size="small" type="primary" :icon="Download">导出报表</el-button>
        <el-button size="small" :icon="Setting">流程配置</el-button>
      </div>
    </div>

    <el-divider style="margin: 4px 0 16px; border-color: #dfe3ea" />

    <div class="main-area">
      <div class="main-content main-pd32 custom-scrollbar">
        <StatsGrid />

        <div class="charts-row">
          <div class="chart-card">
            <div class="chart-header">
              <span class="chart-title">任务量趋势</span>
            </div>
            <TrendChart />
          </div>
          <div class="chart-card">
            <div class="chart-header">
              <span class="chart-title">咨询类型分布</span>
            </div>
            <CategoryChart />
          </div>
        </div>

        <div class="issues-section">
          <div class="section-header">
            <span class="section-title">系统问题追踪</span>
            <el-button size="small" text type="primary">查看全部</el-button>
          </div>
          <IssueList />
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { Download, Setting } from '@element-plus/icons-vue'
import StatsGrid from '@/components/admin/StatsGrid.vue'
import TrendChart from '@/components/admin/TrendChart.vue'
import CategoryChart from '@/components/admin/CategoryChart.vue'
import IssueList from '@/components/admin/IssueList.vue'

const dateRange = ref('today')
</script>

<style lang="scss" scoped>
.main-breadcrumb {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding-right: 16px;
}

.breadcrumb-actions {
  display: flex;
  align-items: center;
  gap: 8px;
}

.charts-row {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 16px;
  margin-top: 16px;
}

.chart-card {
  background-color: #fff;
  border-radius: 8px;
  border: 1px solid var(--blod-border-color);
  padding: 16px;
}

.chart-header {
  margin-bottom: 12px;
}

.chart-title {
  font-size: 14px;
  font-weight: 600;
  color: var(--text-color);
}

.issues-section {
  margin-top: 16px;
  background-color: #fff;
  border-radius: 8px;
  border: 1px solid var(--blod-border-color);
  padding: 16px;
}

.section-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 12px;
}

.section-title {
  font-size: 14px;
  font-weight: 600;
  color: var(--text-color);
}
</style>
