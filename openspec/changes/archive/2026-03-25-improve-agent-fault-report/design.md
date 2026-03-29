## Context

当前坐席工作台右侧 `CustomerPanel.vue` 底部有"生产故障报障"按钮，但仅为静态 UI，无任何交互逻辑。系统已有完整的任务生命周期（`ServiceTask`）、Pinia 任务存储（`taskStore`）和双层坐席路由（L1/L2），具备承载报障工单的基础设施。

## Goals / Non-Goals

**Goals:**
- 点击报障按钮弹出填写表单，自动预填当前任务上下文
- 提交后在 taskStore 中生成一条 Agent 侧报障工单（`requestSource: 'Agent'`）
- 工单在 L2 公共池可见并可被认领
- 提交后 ChatPanel 插入一条系统提示消息作为操作凭证

**Non-Goals:**
- 不接入真实故障管理系统（如 Jira/ServiceNow）
- 不实现报障工单的状态流转通知（邮件/短信）
- 不修改管理员看板（AdminDashboardView）的统计逻辑

## Decisions

### 1. 弹窗组件独立为 `FaultReportDialog.vue`

而非内联在 CustomerPanel，原因：复用性（未来 ChatPanel 也可触发）、职责单一、避免 CustomerPanel 过重。

### 2. 工单通过 `taskStore.createFaultTask()` 创建，不走 AI 预处理

坐席自行填写结构化信息，无需 `preProcessTask()` 的 JSON-mode 调用，减少延迟并避免不必要的 API 消耗。

### 3. 系统通知消息通过事件向上传递至 AgentWorkstationView，再传给 ChatPanel

CustomerPanel emit `faultReported` 事件 → AgentWorkstationView 处理 → 调用 ChatPanel 的 `addSystemMessage()` 方法（通过 ref）。
选择原因：避免 CustomerPanel 直接依赖 ChatPanel，保持三面板解耦。

### 4. 紧急程度映射到 `ServiceTask.priority`

| 表单选项 | priority 值 |
|---------|-------------|
| 低      | Low         |
| 中      | Medium      |
| 高      | High        |
| 紧急    | Urgent      |

## Risks / Trade-offs

- **Pinia 仅内存存储** → 刷新页面报障工单丢失。Mitigation：Demo 场景可接受，生产环境需持久化。
- **L2 公共池过滤逻辑** → 当前 TaskList 按 `level` 过滤任务，报障工单设 `level: 2` 即可出现在 L2 池。需确认 TaskList 筛选逻辑无额外限制。
