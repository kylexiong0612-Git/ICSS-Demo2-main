# 任务生命周期规格 (Task Lifecycle Spec)

**版本**: v1.1
**最后更新**: 2026-04-16
**状态**: 已同步实现

---

## 1. 任务状态机

### 1.1 状态定义

| 状态 | 含义 |
|------|------|
| `Pending` | 待分配，位于当前阶段公共池 |
| `Processing` | 已领取，正在由当前阶段坐席处理 |
| `Suspended` | 已挂起 |
| `Escalated` | 已从当前阶段流转到下一阶段 |
| `Completed` | 当前链路处理完成 |

### 1.2 基础流转

```text
新任务创建
  -> Pending
  -> grab
  -> Processing
  -> advance -> Escalated + 下一阶段新任务
  -> complete -> Completed
```

### 1.3 状态转换规则

| 操作 | 前置状态 | 后置状态 | 说明 |
|------|---------|---------|------|
| `grabTask` | `Pending` | `Processing` | 坐席领取当前阶段任务 |
| `advanceTask` | `Processing` | `Escalated` | 当前任务结束并生成下一阶段任务 |
| `completeTask` | `Processing` | `Completed` | 末阶段完成，或无需继续流转 |
| `suspendTask` | `Processing` | `Suspended` | 预留 |
| `resumeTask` | `Suspended` | `Processing` | 预留 |

---

## 2. 工作流与阶段模型

任务不再只依赖 `level` 表达处理层级，而是以工作流和阶段为主：

```typescript
interface ServiceTask {
  workflowCode: string
  currentStageCode: string
  currentStageOrder: number
  level: number
}
```

### 2.1 默认工作流

| 工作流 | 说明 | 默认阶段链路 |
|--------|------|--------------|
| `ops-service-flow` | 运营服务链路 | `L1 -> L2` |
| `system-fault-flow` | 系统报障链路 | `L1 -> L2 -> L3` |

### 2.2 兼容策略

- `level` 字段保留，用于兼容旧查询和旧路由
- 兼容入口映射：
  - `level=1` -> `ops-service-flow/L1`
  - `level=2` -> `ops-service-flow/L2`

---

## 3. 转人工识别与任务路由

### 3.1 触发场景

客户侧消息命中转人工关键词后，调用 `POST /api/tasks/handoff` 创建人工任务。

### 3.2 识别流程

```text
客户消息 -> HandoffAnalysisService
  -> 优先调用 GLM-4-Flash 返回 intentCode / summary / suggestion / tags / confidence
  -> AI 失败时按关键词回退分类
  -> 读取 agent_workflow_route_rule
  -> 确定 targetWorkflowCode + entryStageCode
  -> 创建首阶段 Pending 任务
```

### 3.3 支持的标准意图

- `policy-service`
- `claims-service`
- `underwriting-service`
- `complaint-service`
- `channel-support`
- `system-fault`
- `general-service`

### 3.4 路由规则

| intentCode | 默认目标工作流 | 默认入口阶段 |
|-----------|----------------|--------------|
| `system-fault` | `system-fault-flow` | `L1` |
| `policy-service` | `ops-service-flow` | `L1` |
| `claims-service` | `ops-service-flow` | `L1` |
| `underwriting-service` | `ops-service-flow` | `L1` |
| `complaint-service` | `ops-service-flow` | `L1` |
| `channel-support` | `ops-service-flow` | `L1` |
| `general-service` | `ops-service-flow` | `L1` |

### 3.5 回退规则

- AI 失败：`routeReason='fallback:ai-error'`
- 路由规则缺失或禁用：回退 `general-service`
- 入口阶段缺失：回退工作流首阶段
- 工作流不可用：回退 `ops-service-flow/L1`

---

## 4. 任务创建规则

### 4.1 客户转人工

| 字段 | 规则 |
|------|------|
| `requestSource` | `Customer` |
| `status` | `Pending` |
| `workflowCode` | 由路由规则决定 |
| `currentStageCode` | 由入口阶段决定 |
| `category` | 使用 `intentName` |
| `summary` | AI/回退摘要 |
| `aiSuggestion` | AI/回退建议 |
| `intentCode` | 标准意图编码 |
| `routeReason` | 路由命中或回退原因 |
| `handoffConfidence` | AI/回退置信度 |

### 4.2 阶段推进

```text
advance(taskId)
  -> 查当前任务
  -> 查 workflowCode + currentStageOrder 的下一阶段
  -> 若存在：
       当前任务 status=Escalated
       新建下一阶段 Pending 任务
       继承 customerId / summary / tags / intentCode
  -> 若不存在：
       当前任务 status=Completed
```

### 4.3 报障工单

| 字段 | 规则 |
|------|------|
| `id` | `FAULT-<8位随机码>` |
| `requestSource` | `Agent` |
| `workflowCode` | `system-fault-flow` |
| `currentStageCode` | `L1` |
| `intentCode` | `system-fault` |
| `routeReason` | `agent:fault-report` |

---

## 5. 前端 taskStore 能力

- **文件**：`frontend/src/stores/taskStore.ts`

| 方法 | 说明 |
|------|------|
| `loadTasks(filters)` | 按条件加载任务 |
| `refreshStageTasks(workflowCode, stageCode)` | 刷新某阶段任务 |
| `assignTask(taskId)` | 领取任务 |
| `moveToNextStage(taskId)` | 推进到下一阶段 |
| `finishTask(taskId)` | 完成任务 |
| `submitFaultTask(payload)` | 创建报障工单 |
| `loadCustomerTasks(customerId)` | 加载某客户活跃任务 |

---

## 6. API 定义

### 6.1 任务查询与流转

| 方法 | 路径 | 说明 |
|------|------|------|
| `GET` | `/api/tasks` | 获取任务列表 |
| `POST` | `/api/tasks` | 创建普通任务 |
| `PUT` | `/api/tasks/{id}` | 更新任务 |
| `PUT` | `/api/tasks/{id}/grab` | 领取任务 |
| `PUT` | `/api/tasks/{id}/advance` | 推进至下一阶段 |
| `PUT` | `/api/tasks/{id}/escalate` | 兼容旧入口，等价于 `advance` |
| `PUT` | `/api/tasks/{id}/complete` | 完成任务 |
| `POST` | `/api/tasks/fault` | 创建报障工单 |

### 6.2 转人工

| 方法 | 路径 | 说明 |
|------|------|------|
| `POST` | `/api/tasks/handoff-analyze` | 返回 `HandoffAnalysis` |
| `POST` | `/api/tasks/handoff` | 创建转人工任务 |

### 6.3 查询参数

| 参数 | 说明 |
|------|------|
| `level` | 兼容旧层级查询 |
| `workflowCode` | 按工作流过滤 |
| `stageCode` | 按阶段过滤 |
| `customerId` | 按客户过滤 |
| `status` | 按任务状态过滤 |

---

## 7. 错误处理

- 任务不存在：接口返回空或 `null`，前端应刷新列表并退出当前选中态
- 任务推进无下一阶段：后端直接完成当前任务
- 报障入口阶段解析失败：回退到 `system-fault-flow/L1`
- 转人工分析失败：允许创建默认运营链路任务，避免阻塞客户接入

