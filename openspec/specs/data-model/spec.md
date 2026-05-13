# 数据模型规格 (Data Model Spec)

**版本**: v1.2
**最后更新**: 2026-04-17
**状态**: 已同步实现

---

## 1. 前端类型定义

> 源文件：`frontend/src/types/index.ts`

### 1.1 Message

```typescript
type MessageRole = 'user' | 'bot' | 'agent' | 'system'

interface Message {
  id: string
  role: MessageRole
  content: string
  timestamp: number
  type?: 'text' | 'card' | 'system'
  metadata?: unknown
}
```

### 1.2 ServiceTask

```typescript
interface ServiceTask {
  id: string
  customerId?: string
  agentId?: string
  requestSource: 'Customer' | 'Agent'
  status: 'Pending' | 'Processing' | 'Suspended' | 'Completed' | 'Escalated'
  priority: 'Low' | 'Medium' | 'High' | 'Urgent'
  category: string
  summary: string
  aiSuggestion: string
  chatHistory: Message[]
  assignedTo?: string
  unreadCount?: number
  level: number
  workflowCode: string
  currentStageCode: string
  currentStageOrder: number
  sourceTaskId?: string
  intentCode?: string
  routeReason?: string
  handoffConfidence?: number
  tags?: string[]
  createdAt: number
  updatedAt: number
}
```

### 1.3 工作流配置

```typescript
interface AgentWorkflowTemplate {
  code: string
  name: string
  description?: string
  categoryScope: string[]
  enabled: boolean
  stages: AgentWorkflowStage[]
}

interface AgentWorkflowStage {
  id?: number
  workflowCode: string
  code: string
  name: string
  stageOrder: number
  roleLabel: string
  description?: string
  allowedActions: string[]
}
```

### 1.4 路由规则与布局

```typescript
interface AgentWorkflowRouteRule {
  intentCode: string
  intentName: string
  targetWorkflowCode: string
  entryStageCode: string
  priorityStrategy: 'inherit' | 'force-medium' | 'force-high' | 'force-urgent'
  enabled: boolean
}

interface WorkbenchLayoutConfig {
  code: string
  name: string
  workflowCode: string
  stageCode: string
  regions: WorkbenchRegion[]
  enabled: boolean
}
```

### 1.5 HandoffAnalysis

```typescript
interface HandoffAnalysis {
  handoffNeeded: boolean
  intentCode: string
  intentName: string
  summary: string
  suggestion: string
  tags: string[]
  targetWorkflowCode?: string
  targetStageCode?: string
  confidence: number
  routeReason?: string
}
```

---

## 2. 数据库模式

> 源文件：`backend/src/main/resources/db/init.sql`

### 2.1 service_task

`service_task` 在原有任务表基础上扩展了工作流与路由字段：

```sql
service_task (
  id,
  customer_id,
  agent_id,
  request_source,
  status,
  priority,
  category,
  summary,
  ai_suggestion,
  assigned_to,
  unread_count,
  level,
  workflow_code,
  current_stage_code,
  current_stage_order,
  source_task_id,
  intent_code,
  route_reason,
  handoff_confidence,
  tags,
  created_at,
  updated_at
)
```

关键说明：

- `workflow_code`：任务所属工作流
- `current_stage_code`：当前阶段编码
- `current_stage_order`：当前阶段顺序
- `source_task_id`：上一阶段源任务
- `intent_code`：转人工意图编码
- `route_reason`：路由命中或回退原因
- `handoff_confidence`：转人工分析置信度

### 2.2 agent_workflow_template

```sql
agent_workflow_template (
  code varchar primary key,
  name varchar,
  description text,
  category_scope json,
  enabled tinyint,
  created_at bigint,
  updated_at bigint
)
```

### 2.3 agent_workflow_stage

```sql
agent_workflow_stage (
  id bigint primary key auto_increment,
  workflow_code varchar,
  code varchar,
  name varchar,
  stage_order int,
  role_label varchar,
  description text,
  allowed_actions json,
  created_at bigint,
  updated_at bigint
)
```

### 2.4 agent_workflow_route_rule

```sql
agent_workflow_route_rule (
  id bigint primary key auto_increment,
  intent_code varchar unique,
  intent_name varchar,
  target_workflow_code varchar,
  entry_stage_code varchar,
  priority_strategy varchar,
  enabled tinyint,
  created_at bigint,
  updated_at bigint
)
```

### 2.5 workbench_layout_config

```sql
workbench_layout_config (
  code varchar primary key,
  name varchar,
  workflow_code varchar,
  stage_code varchar,
  regions json,
  enabled tinyint,
  created_at bigint,
  updated_at bigint
)
```

### 2.6 chat_message

消息表继续承担客户与坐席会话持久化：

```sql
chat_message (
  id,
  customer_id,
  task_id,
  role,
  content,
  message_type,
  metadata,
  created_at
)
```

---

## 3. 字段映射关系

| 前端字段 | 后端字段 | 说明 |
|---------|---------|------|
| `ServiceTask.createdAt` | `service_task.created_at` | 前端为毫秒时间戳 |
| `ServiceTask.updatedAt` | `service_task.updated_at` | 前端为毫秒时间戳 |
| `ServiceTask.workflowCode` | `service_task.workflow_code` | 工作流编码 |
| `ServiceTask.currentStageCode` | `service_task.current_stage_code` | 阶段编码 |
| `ServiceTask.currentStageOrder` | `service_task.current_stage_order` | 阶段顺序 |
| `ServiceTask.intentCode` | `service_task.intent_code` | 人工接入意图 |
| `ServiceTask.routeReason` | `service_task.route_reason` | 路由原因 |
| `Message.type` | `chat_message.message_type` | 字段名不同 |

---

## 4. 初始配置数据

当前初始化脚本会写入以下默认配置：

### 4.1 工作流

- `ops-service-flow`
  - `L1` 运营-一线坐席
  - `L2` 运营-二线专家
- `system-fault-flow`
  - `L1` 报障-一线受理
  - `L2` 报障-二线定位
  - `L3` 报障-三线技术支持

### 4.2 路由规则

- `system-fault` -> `system-fault-flow/L1`
- `general-service` -> `ops-service-flow/L1`
- 其余标准服务意图默认进入 `ops-service-flow/L1`

### 4.3 默认工作台布局

- `ops-service-flow/L1`
- `ops-service-flow/L2`
- `system-fault-flow/L1`
- `system-fault-flow/L2`
- `system-fault-flow/L3`

---

## 5. API 返回模型

### 5.1 TaskAdvanceResponse

```typescript
interface TaskAdvanceResponse {
  currentTask: ServiceTask
  nextTask: ServiceTask | null
}
```

说明：

- `nextTask != null` 表示成功流转到下一阶段
- `nextTask = null` 表示当前任务已在末阶段完成

---

## 6. 错误处理

- 旧库表缺少新字段时，初始化脚本通过迁移 SQL 自动补列
- JSON 字段使用 MyBatis `TypeHandler` 解析，若解析失败应返回空结构而非中断主流程
- 对历史旧任务，如果缺少工作流字段，后端按 `level` 映射到默认运营链路
