<template>
  <div class="main-container">
    <div class="main-breadcrumb">
      <el-breadcrumb separator="/">
        <el-breadcrumb-item>管理端</el-breadcrumb-item>
        <el-breadcrumb-item>坐席工作流配置</el-breadcrumb-item>
      </el-breadcrumb>
    </div>

    <div class="main-area">
      <div class="main-content main-pd32 custom-scrollbar workflow-config-page">
        <section class="config-card">
          <div class="section-header">
            <span class="section-title">工作流模板</span>
            <div class="actions">
              <el-select v-model="activeWorkflowCode" style="width: 240px" placeholder="选择工作流">
                <el-option
                  v-for="item in workflows"
                  :key="item.code"
                  :label="item.name"
                  :value="item.code"
                />
              </el-select>
              <el-button @click="createWorkflow">新增工作流</el-button>
              <el-button type="primary" @click="saveWorkflow">保存工作流</el-button>
            </div>
          </div>

          <template v-if="editableWorkflow">
            <el-form label-width="100px" class="workflow-form">
              <el-form-item label="工作流编码">
                <el-input v-model="editableWorkflow.code" />
              </el-form-item>
              <el-form-item label="工作流名称">
                <el-input v-model="editableWorkflow.name" />
              </el-form-item>
              <el-form-item label="描述">
                <el-input v-model="editableWorkflow.description" type="textarea" :rows="2" />
              </el-form-item>
              <el-form-item label="启用状态">
                <el-switch v-model="editableWorkflow.enabled" />
              </el-form-item>
              <el-form-item label="分类范围">
                <el-select
                  v-model="editableWorkflow.categoryScope"
                  multiple
                  filterable
                  allow-create
                  default-first-option
                  style="width: 100%"
                />
              </el-form-item>
            </el-form>

            <div class="sub-header">
              <span class="section-subtitle">阶段配置</span>
              <el-button size="small" @click="addStage">新增阶段</el-button>
            </div>

            <el-table :data="editableWorkflow.stages" border>
              <el-table-column label="顺序" width="80">
                <template #default="{ row }">
                  <el-input-number v-model="row.stageOrder" :min="1" :step="1" />
                </template>
              </el-table-column>
              <el-table-column label="阶段编码" width="140">
                <template #default="{ row }">
                  <el-input v-model="row.code" />
                </template>
              </el-table-column>
              <el-table-column label="阶段名称" width="160">
                <template #default="{ row }">
                  <el-input v-model="row.name" />
                </template>
              </el-table-column>
              <el-table-column label="角色标签" width="160">
                <template #default="{ row }">
                  <el-input v-model="row.roleLabel" />
                </template>
              </el-table-column>
              <el-table-column label="动作集合">
                <template #default="{ row }">
                  <el-select
                    v-model="row.allowedActions"
                    multiple
                    filterable
                    allow-create
                    default-first-option
                    style="width: 100%"
                  />
                </template>
              </el-table-column>
              <el-table-column label="预览" width="120">
                <template #default="{ row }">
                  <el-button size="small" text type="primary" @click="previewStage(row.code)">
                    打开工作台
                  </el-button>
                </template>
              </el-table-column>
              <el-table-column label="操作" width="90">
                <template #default="{ $index }">
                  <el-button size="small" text type="danger" @click="removeStage($index)">
                    删除
                  </el-button>
                </template>
              </el-table-column>
            </el-table>
          </template>
        </section>

        <section class="config-card">
          <div class="section-header">
            <span class="section-title">转人工路由规则</span>
          </div>
          <el-table :data="routeRules" border>
            <el-table-column prop="intentCode" label="意图编码" width="180" />
            <el-table-column label="意图名称" width="140">
              <template #default="{ row }">
                <el-input v-model="row.intentName" />
              </template>
            </el-table-column>
            <el-table-column label="目标工作流" width="200">
              <template #default="{ row }">
                <el-select v-model="row.targetWorkflowCode" style="width: 100%">
                  <el-option
                    v-for="workflow in workflows"
                    :key="workflow.code"
                    :label="workflow.name"
                    :value="workflow.code"
                  />
                </el-select>
              </template>
            </el-table-column>
            <el-table-column label="入口阶段" width="160">
              <template #default="{ row }">
                <el-input v-model="row.entryStageCode" />
              </template>
            </el-table-column>
            <el-table-column label="优先级策略" width="180">
              <template #default="{ row }">
                <el-select v-model="row.priorityStrategy" style="width: 100%">
                  <el-option label="继承" value="inherit" />
                  <el-option label="强制中" value="force-medium" />
                  <el-option label="强制高" value="force-high" />
                  <el-option label="强制紧急" value="force-urgent" />
                </el-select>
              </template>
            </el-table-column>
            <el-table-column label="启用" width="90">
              <template #default="{ row }">
                <el-switch v-model="row.enabled" />
              </template>
            </el-table-column>
            <el-table-column label="操作" width="110">
              <template #default="{ row }">
                <el-button size="small" type="primary" @click="saveRouteRule(row)">保存</el-button>
              </template>
            </el-table-column>
          </el-table>
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
  getAgentWorkflowRoutes,
  getAgentWorkflows,
  updateAgentWorkflow,
  updateAgentWorkflowRoute,
} from '@/api/workflow'
import type { AgentWorkflowRouteRule, AgentWorkflowTemplate } from '@/types'

const router = useRouter()
const workflows = ref<AgentWorkflowTemplate[]>([])
const routeRules = ref<AgentWorkflowRouteRule[]>([])
const activeWorkflowCode = ref('')
const editableWorkflow = ref<AgentWorkflowTemplate | null>(null)

watch(activeWorkflowCode, code => {
  const selected = workflows.value.find(item => item.code === code)
  editableWorkflow.value = selected ? structuredClone(selected) : null
})

load()

async function load() {
  const [workflowData, routeData] = await Promise.all([
    getAgentWorkflows(),
    getAgentWorkflowRoutes(),
  ])
  workflows.value = workflowData
  routeRules.value = routeData
  if (!activeWorkflowCode.value && workflowData.length > 0) {
    activeWorkflowCode.value = workflowData[0].code
  } else if (activeWorkflowCode.value) {
    const selected = workflowData.find(item => item.code === activeWorkflowCode.value)
    editableWorkflow.value = selected ? structuredClone(selected) : null
  }
}

function createWorkflow() {
  const workflow: AgentWorkflowTemplate = {
    code: `custom-flow-${Date.now()}`,
    name: '新工作流',
    description: '',
    categoryScope: ['general-service'],
    enabled: true,
    stages: [
      {
        workflowCode: '',
        code: 'L1',
        name: '一线坐席',
        stageOrder: 1,
        roleLabel: '一线坐席',
        allowedActions: ['grab', 'advance', 'complete'],
      },
    ],
  }
  workflows.value = [workflow, ...workflows.value]
  activeWorkflowCode.value = workflow.code
}

function addStage() {
  if (!editableWorkflow.value) return
  editableWorkflow.value.stages.push({
    workflowCode: editableWorkflow.value.code,
    code: `L${editableWorkflow.value.stages.length + 1}`,
    name: `阶段${editableWorkflow.value.stages.length + 1}`,
    stageOrder: editableWorkflow.value.stages.length + 1,
    roleLabel: '待命名角色',
    allowedActions: ['grab', 'complete'],
  })
}

function removeStage(index: number) {
  editableWorkflow.value?.stages.splice(index, 1)
}

async function saveWorkflow() {
  if (!editableWorkflow.value) return
  const payload = structuredClone(editableWorkflow.value)
  payload.stages = [...payload.stages].sort((a, b) => a.stageOrder - b.stageOrder)
  await updateAgentWorkflow(payload)
  ElMessage.success('工作流已保存')
  await load()
}

async function saveRouteRule(rule: AgentWorkflowRouteRule) {
  await updateAgentWorkflowRoute(rule)
  ElMessage.success(`已保存路由规则：${rule.intentName}`)
}

function previewStage(stageCode: string) {
  if (!editableWorkflow.value) return
  router.push(`/agent/stage/${editableWorkflow.value.code}/${stageCode}`)
}
</script>

<style lang="scss" scoped>
.workflow-config-page {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.config-card {
  background-color: #fff;
  border: 1px solid var(--blod-border-color);
  border-radius: 12px;
  padding: 16px;
}

.section-header,
.sub-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  margin-bottom: 16px;
}

.section-title,
.section-subtitle {
  font-size: 15px;
  font-weight: 700;
  color: var(--text-color);
}

.actions {
  display: flex;
  align-items: center;
  gap: 8px;
}

.workflow-form {
  margin-bottom: 16px;
}
</style>
