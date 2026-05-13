<template>
  <div class="main-container">
    <div class="main-breadcrumb">
      <el-breadcrumb separator="/">
        <el-breadcrumb-item>管理端</el-breadcrumb-item>
        <el-breadcrumb-item>坐席工作台配置</el-breadcrumb-item>
      </el-breadcrumb>
    </div>

    <div class="main-area">
      <div class="main-content main-pd32 custom-scrollbar layout-page">
        <section class="config-card">
          <div class="section-header">
            <span class="section-title">布局配置</span>
            <div class="actions">
              <el-select v-model="activeLayoutCode" style="width: 260px" placeholder="选择布局">
                <el-option
                  v-for="item in layouts"
                  :key="item.code"
                  :label="`${item.workflowCode} / ${item.stageCode}`"
                  :value="item.code"
                />
              </el-select>
              <el-button @click="createLayout">新增布局</el-button>
              <el-button type="primary" @click="saveLayout">保存布局</el-button>
              <el-button
                v-if="editableLayout"
                @click="previewLayout"
              >
                预览工作台
              </el-button>
            </div>
          </div>

          <template v-if="editableLayout">
            <el-form label-width="100px" class="layout-form">
              <el-form-item label="布局编码">
                <el-input v-model="editableLayout.code" />
              </el-form-item>
              <el-form-item label="布局名称">
                <el-input v-model="editableLayout.name" />
              </el-form-item>
              <el-form-item label="工作流">
                <el-select v-model="editableLayout.workflowCode" style="width: 100%">
                  <el-option
                    v-for="workflow in workflows"
                    :key="workflow.code"
                    :label="workflow.name"
                    :value="workflow.code"
                  />
                </el-select>
              </el-form-item>
              <el-form-item label="阶段编码">
                <el-input v-model="editableLayout.stageCode" />
              </el-form-item>
              <el-form-item label="启用状态">
                <el-switch v-model="editableLayout.enabled" />
              </el-form-item>
            </el-form>

            <div class="sub-header">
              <span class="section-subtitle">区域编排</span>
              <el-button size="small" @click="addRegion">新增区域</el-button>
            </div>

            <div class="region-list">
              <div v-for="(region, regionIndex) in editableLayout.regions" :key="regionIndex" class="region-card">
                <div class="region-header">
                  <div class="region-fields">
                    <el-select v-model="region.code" style="width: 120px">
                      <el-option label="上方" value="top" />
                      <el-option label="左侧" value="left" />
                      <el-option label="中间" value="center" />
                      <el-option label="右侧" value="right" />
                      <el-option label="下方" value="bottom" />
                    </el-select>
                    <el-input v-model="region.width" placeholder="宽度，如 320px" style="width: 160px" />
                  </div>
                  <div class="actions">
                    <el-button size="small" @click="addWidget(regionIndex)">新增组件</el-button>
                    <el-button size="small" text type="danger" @click="removeRegion(regionIndex)">删除区域</el-button>
                  </div>
                </div>

                <el-table :data="region.widgets" border>
                  <el-table-column label="顺序" width="90">
                    <template #default="{ row }">
                      <el-input-number v-model="row.order" :min="1" :step="1" />
                    </template>
                  </el-table-column>
                  <el-table-column label="组件类型" width="180">
                    <template #default="{ row }">
                      <el-select v-model="row.type" style="width: 100%">
                        <el-option
                          v-for="type in widgetTypes"
                          :key="type"
                          :label="type"
                          :value="type"
                        />
                      </el-select>
                    </template>
                  </el-table-column>
                  <el-table-column label="组件编码" width="180">
                    <template #default="{ row }">
                      <el-input v-model="row.code" />
                    </template>
                  </el-table-column>
                  <el-table-column label="标题">
                    <template #default="{ row }">
                      <el-input v-model="row.title" />
                    </template>
                  </el-table-column>
                  <el-table-column label="显示" width="90">
                    <template #default="{ row }">
                      <el-switch v-model="row.visible" />
                    </template>
                  </el-table-column>
                  <el-table-column label="操作" width="90">
                    <template #default="{ $index }">
                      <el-button size="small" text type="danger" @click="removeWidget(regionIndex, $index)">
                        删除
                      </el-button>
                    </template>
                  </el-table-column>
                </el-table>
              </div>
            </div>
          </template>
        </section>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, watch } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import {
  getAgentWorkflows,
  getWorkbenchLayouts,
  updateWorkbenchLayout,
} from '@/api/workflow'
import type { AgentWorkflowTemplate, WorkbenchLayoutConfig, WorkbenchRegion } from '@/types'

const router = useRouter()
const widgetTypes = [
  'task-pool',
  'my-task-list',
  'chat-panel',
  'customer-profile',
  'policy-list',
  'ai-copilot',
  'history-records',
  'fault-report-entry',
  'stage-timeline',
  'knowledge-card',
]

const workflows = ref<AgentWorkflowTemplate[]>([])
const layouts = ref<WorkbenchLayoutConfig[]>([])
const activeLayoutCode = ref('')
const editableLayout = ref<WorkbenchLayoutConfig | null>(null)

watch(activeLayoutCode, code => {
  const selected = layouts.value.find(item => item.code === code)
  editableLayout.value = selected ? structuredClone(selected) : null
})

load()

async function load() {
  const [workflowData, layoutData] = await Promise.all([
    getAgentWorkflows(),
    getWorkbenchLayouts(),
  ])
  workflows.value = workflowData
  layouts.value = layoutData
  if (!activeLayoutCode.value && layoutData.length > 0) {
    activeLayoutCode.value = layoutData[0].code
  } else if (activeLayoutCode.value) {
    const selected = layoutData.find(item => item.code === activeLayoutCode.value)
    editableLayout.value = selected ? structuredClone(selected) : null
  }
}

function createLayout() {
  const workflowCode = workflows.value[0]?.code ?? 'ops-service-flow'
  const stageCode = workflows.value[0]?.stages?.[0]?.code ?? 'L1'
  const layout: WorkbenchLayoutConfig = {
    code: `${workflowCode}-${stageCode}-${Date.now()}`,
    name: '新工作台布局',
    workflowCode,
    stageCode,
    enabled: true,
    regions: [
      {
        code: 'left',
        width: '280px',
        widgets: [{ code: 'task-pool', type: 'task-pool', title: '任务公共池', visible: true, order: 1 }],
      },
      {
        code: 'center',
        widgets: [{ code: 'chat-panel', type: 'chat-panel', title: '会话记录', visible: true, order: 1 }],
      },
    ],
  }
  layouts.value = [layout, ...layouts.value]
  activeLayoutCode.value = layout.code
}

function addRegion() {
  editableLayout.value?.regions.push({
    code: 'right',
    width: '360px',
    widgets: [],
  } as WorkbenchRegion)
}

function removeRegion(index: number) {
  editableLayout.value?.regions.splice(index, 1)
}

function addWidget(regionIndex: number) {
  editableLayout.value?.regions[regionIndex]?.widgets.push({
    code: `widget-${Date.now()}`,
    type: 'ai-copilot',
    title: '新组件',
    visible: true,
    order: editableLayout.value.regions[regionIndex].widgets.length + 1,
  })
}

function removeWidget(regionIndex: number, widgetIndex: number) {
  editableLayout.value?.regions[regionIndex]?.widgets.splice(widgetIndex, 1)
}

async function saveLayout() {
  if (!editableLayout.value) return
  const payload = structuredClone(editableLayout.value)
  payload.regions.forEach(region => {
    region.widgets = [...region.widgets].sort((a, b) => a.order - b.order)
  })
  await updateWorkbenchLayout(payload)
  ElMessage.success('工作台布局已保存')
  await load()
}

function previewLayout() {
  if (!editableLayout.value) return
  router.push(`/agent/stage/${editableLayout.value.workflowCode}/${editableLayout.value.stageCode}`)
}
</script>

<style lang="scss" scoped>
.layout-page {
  display: flex;
  flex-direction: column;
}

.config-card {
  background-color: #fff;
  border: 1px solid var(--blod-border-color);
  border-radius: 12px;
  padding: 16px;
}

.section-header,
.sub-header,
.region-header,
.actions,
.region-fields {
  display: flex;
  align-items: center;
  gap: 8px;
}

.section-header,
.sub-header,
.region-header {
  justify-content: space-between;
}

.section-header,
.sub-header {
  margin-bottom: 16px;
}

.section-title,
.section-subtitle {
  font-size: 15px;
  font-weight: 700;
  color: var(--text-color);
}

.layout-form {
  margin-bottom: 16px;
}

.region-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.region-card {
  border: 1px solid var(--blod-border-color);
  border-radius: 12px;
  padding: 12px;
}

.region-header {
  margin-bottom: 12px;
}
</style>
