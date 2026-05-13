# 坐席工作台规格 (Agent Workstation Spec)

**版本**: v1.2
**最后更新**: 2026-04-17
**状态**: 已同步实现

---

## 1. 功能概述

坐席工作台已从固定 `L1/L2` 双栏逻辑升级为“工作流 + 阶段 + 布局配置”驱动：

- 工作台按 `workflowCode + stageCode` 加载
- 任务流转按工作流阶段定义推进
- 页面布局按工作流阶段动态渲染
- 仍保留 `/agent/:level` 兼容入口，映射到默认运营链路

---

## 2. 路由

| 路由 | 说明 |
|------|------|
| `/#/agent/1` | 兼容入口，映射到 `ops-service-flow/L1` |
| `/#/agent/2` | 兼容入口，映射到 `ops-service-flow/L2` |
| `/#/agent/stage/:workflowCode/:stageCode` | 标准入口，按工作流阶段打开工作台 |

### 2.1 顶部演示导航

应用顶部导航当前提供以下快捷入口：

| 导航文案 | 目标路由 |
|---------|----------|
| `运营-一线坐席` | `/#/agent/stage/ops-service-flow/L1` |
| `运营-二线专家` | `/#/agent/stage/ops-service-flow/L2` |
| `报障-一线受理` | `/#/agent/stage/system-fault-flow/L1` |
| `报障-二线定位` | `/#/agent/stage/system-fault-flow/L2` |
| `报障-三线技术支持` | `/#/agent/stage/system-fault-flow/L3` |

说明：

- 顶部导航优先使用标准阶段路由
- `/#/agent/1` 与 `/#/agent/2` 仅作为兼容入口保留

---

## 3. AgentWorkstationView

- **文件**：`frontend/src/views/AgentWorkstationView.vue`
- **Props**：`workflowCode: string`、`stageCode: string`

### 3.1 启动流程

```text
进入工作台
  -> 获取工作流定义 GET /api/agent-workflows/{code}
  -> 获取有效布局 GET /api/workbench-layouts/effective
  -> 获取当前阶段任务 GET /api/tasks?workflowCode=...&stageCode=...
  -> 按 regions 渲染页面
```

### 3.2 默认回退布局

若当前阶段未配置可用布局，前端使用默认三栏布局：

- 左栏：`task-pool` + `my-task-list`
- 中栏：`chat-panel`
- 右栏：`ai-copilot`、`customer-profile`、`policy-list`、`history-records`、`fault-report-entry`

### 3.3 区域模型

```typescript
interface WorkbenchRegion {
  code: 'left' | 'center' | 'right' | 'top' | 'bottom'
  width?: string
  widgets: WorkbenchWidget[]
}
```

区域渲染顺序：

1. `top`
2. `left`
3. `center`
4. `right`
5. `bottom`

---

## 4. Widget Registry

首版工作台组件类型：

- `task-pool`
- `my-task-list`
- `chat-panel`
- `customer-profile`
- `policy-list`
- `ai-copilot`
- `history-records`
- `fault-report-entry`
- `stage-timeline`
- `knowledge-card`

其中：

- `task-pool` 与 `my-task-list` 复用 `TaskList`
- `chat-panel` 渲染 `ChatPanel`
- 其余右侧信息类组件统一由 `CustomerPanel` 根据 `visibleSections` 组合渲染

---

## 5. TaskList 规格

- **文件**：`frontend/src/components/agent/TaskList.vue`
- **Props**：
  - `workflowCode: string`
  - `stageCode: string`
  - `selectedTaskId?: string`
  - `showPublicPool?: boolean`
  - `showPersonalPool?: boolean`
- **Emits**：`select(task)`、`grab(taskId)`

### 5.1 查询与筛选规则

- 公共池：`status='Pending'`
- 个人池：`status='Processing' && assignedTo='ME'`
- 两类池都必须匹配 `workflowCode + currentStageCode`

### 5.2 搜索规则

- 支持按任务 ID 或摘要进行本地过滤
- 顶部展示当前工作流与阶段编码

---

## 6. ChatPanel 规格

- **文件**：`frontend/src/components/agent/ChatPanel.vue`
- **Props**：`task`、`stageLabel`、`nextStageLabel?`
- **Emits**：`update-task`、`advance`、`complete`、`grab`

### 6.1 头部与消息

- 展示当前任务 ID、状态标签
- `agent` 消息根据 `metadata.stageOrder` 显示不同身份感知
- `type='system'` 消息使用系统提示样式

### 6.2 动作按钮规则

| 按钮 | 条件 | 动作 |
|------|------|------|
| 领取任务 | `status='Pending'` | 调用 `PUT /api/tasks/{id}/grab` |
| 推进至下一阶段 | `status='Processing'` 且存在 `nextStageLabel` | 调用 `PUT /api/tasks/{id}/advance` |
| 完成 | `status='Processing'` 或末阶段处理完成 | 调用 `PUT /api/tasks/{id}/complete` |
| 发送 | 输入非空 | 发送坐席消息并持久化 |

### 6.3 推进规则

```text
当前任务 Processing
  -> advance
  -> 后端查找 currentStageOrder 的下一阶段
  -> 原任务标记 Escalated
  -> 生成下一阶段 Pending 任务
  -> 若无下一阶段，则原任务直接 Completed
```

---

## 7. CustomerPanel 规格

- **文件**：`frontend/src/components/agent/CustomerPanel.vue`
- **Props**：`task`、`visibleSections?: string[]`、`workflowStages?: AgentWorkflowStage[]`
- **Emits**：`fault-reported(taskId)`

### 7.1 可组合区块

| 区块编码 | 内容 |
|----------|------|
| `ai-copilot` | 摘要、建议、标签 |
| `stage-timeline` | 当前工作流阶段轨迹 |
| `knowledge-card` | 按工作流给出知识提示 |
| `customer-profile` | 客户画像与代理人信息 |
| `policy-list` | 客户保单信息 |
| `history-records` | 历史服务记录 |
| `fault-report-entry` | 报障入口 |

### 7.2 区块显隐规则

- 未传入 `visibleSections` 时，全部可用区块默认显示
- 传入 `visibleSections` 时，仅渲染配置中声明的区块

### 7.3 报障入口

- 点击“宏e站一键报障”打开 `FaultReportDialog`
- 提交成功后触发 `fault-reported`
- 当前聊天窗口追加系统提示：“已生成系统报障工单 #<taskId>”

---

## 8. 数据模型

### 8.1 工作流定义

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
  workflowCode: string
  code: string
  name: string
  stageOrder: number
  roleLabel: string
  allowedActions: string[]
}
```

### 8.2 工作台布局

```typescript
interface WorkbenchLayoutConfig {
  code: string
  name: string
  workflowCode: string
  stageCode: string
  regions: WorkbenchRegion[]
  enabled: boolean
}
```

---

## 9. API 定义

### 9.1 工作台依赖接口

| 方法 | 路径 | 说明 |
|------|------|------|
| `GET` | `/api/agent-workflows/{code}` | 获取工作流定义 |
| `GET` | `/api/workbench-layouts/effective` | 获取生效布局 |
| `GET` | `/api/tasks` | 按工作流与阶段查询任务 |
| `PUT` | `/api/tasks/{id}/grab` | 领取任务 |
| `PUT` | `/api/tasks/{id}/advance` | 推进到下一阶段 |
| `PUT` | `/api/tasks/{id}/complete` | 完成任务 |

### 9.2 任务查询参数

| 参数 | 说明 |
|------|------|
| `workflowCode` | 工作流编码 |
| `stageCode` | 阶段编码 |
| `status` | 任务状态 |
| `customerId` | 客户 ID |
| `level` | 兼容旧接口的层级参数 |

---

## 10. 错误处理

- 工作台布局不存在：后端回退到 `ops-service-flow/L1` 的启用布局，前端仍可正常进入
- 工作流阶段不存在：后端回退到该工作流首阶段；仍失败时回退默认运营链路
- 任务推进无下一阶段：直接完成当前任务，不再创建新任务
- 客户信息加载失败：`CustomerPanel` 显示空态，不阻断任务处理
