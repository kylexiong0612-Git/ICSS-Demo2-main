## Why

坐席工作台的"生产故障报障"按钮当前仅为纯 UI 占位，点击无任何响应，坐席无法通过系统完成故障上报，导致需要转到系统外渠道（电话/IM）处理，影响响应效率和可追溯性。

## What Changes

- 为"生产故障报障"按钮添加点击交互，弹出报障工单填写表单
- 支持自动带入当前服务任务上下文（任务 ID、客户信息、AI 摘要）
- 支持坐席填写故障描述、故障类型、影响范围和紧急程度
- 提交后在 taskStore 中创建一条 `requestSource: 'Agent'` 的新任务，状态为 `Pending`，优先级按紧急程度映射
- 提交成功后在 ChatPanel 中插入一条系统通知消息（"已生成报障工单 #xxx"）
- 报障工单在 L2 坐席的任务公共池中可见，支持认领

## Capabilities

### New Capabilities
- `fault-report-dialog`: 报障表单弹窗，含故障类型、描述、影响范围、紧急程度字段，自动预填当前任务上下文
- `agent-created-task`: 坐席侧创建的报障工单在 taskStore 中的生命周期管理（创建、认领、完成）

### Modified Capabilities

（无现有 spec 需变更）

## Impact

- `src/components/agent/CustomerPanel.vue`：为报障按钮添加 `@click` 处理及弹窗组件
- `src/components/agent/FaultReportDialog.vue`：新增报障表单弹窗组件
- `src/stores/taskStore.ts`：新增 `createFaultTask(payload)` action
- `src/types/index.ts`：ServiceTask 的 `requestSource` 已有 `'Agent'` 值，无需扩展
- `src/components/agent/ChatPanel.vue`：支持接收父组件事件，插入系统通知消息
- 无外部 API 变更，无新依赖
