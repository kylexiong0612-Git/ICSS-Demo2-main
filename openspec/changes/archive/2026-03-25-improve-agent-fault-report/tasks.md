## 1. 类型与 Store 扩展

- [x] 1.1 在 `src/types/index.ts` 确认 `ServiceTask.requestSource` 已包含 `'Agent'`（只读确认，无需修改）
- [x] 1.2 在 `src/stores/taskStore.ts` 新增 `createFaultTask(payload)` action，生成 `FAULT-<timestamp后6位>` 格式 ID，level 设为 2，status 为 Pending，requestSource 为 Agent

## 2. 报障弹窗组件

- [x] 2.1 创建 `src/components/agent/FaultReportDialog.vue`，包含 Element Plus Dialog 容器
- [x] 2.2 添加只读预填字段：当前任务 ID、客户姓名
- [x] 2.3 添加必填字段：故障描述（textarea）、紧急程度（select: 低/中/高/紧急）
- [x] 2.4 添加可选字段：故障类型（select）、影响范围（input）
- [x] 2.5 实现表单校验：故障描述和紧急程度非空校验
- [x] 2.6 实现"提交报障"按钮：调用 taskStore.createFaultTask()，emit `submitted` 事件携带新工单 ID
- [x] 2.7 实现"取消"按钮：关闭弹窗，不触发任何 store 操作

## 3. CustomerPanel 集成

- [x] 3.1 在 `src/components/agent/CustomerPanel.vue` 中引入并注册 FaultReportDialog
- [x] 3.2 为"生产故障报障"按钮添加 `@click="openFaultDialog"` 处理函数
- [x] 3.3 在 FaultReportDialog submitted 事件回调中，emit `faultReported` 事件携带工单 ID 向父组件传递

## 4. AgentWorkstationView 事件桥接

- [x] 4.1 在 `src/views/AgentWorkstationView.vue` 监听 CustomerPanel 的 `faultReported` 事件
- [x] 4.2 通过 ChatPanel 的 ref 或 emit，向 ChatPanel 插入系统通知消息（格式："已生成报障工单 #<taskId>"）

## 5. ChatPanel 系统消息支持

- [x] 5.1 在 `src/components/agent/ChatPanel.vue` 添加接收系统通知消息的方法（`addSystemMessage(text: string)`），通过 `defineExpose` 暴露或改用 prop/event 方式
- [x] 5.2 在消息渲染逻辑中为系统消息类型添加对应样式（居中灰色小字或类似视觉区分）

## 6. 验证

- [x] 6.1 验证 L1 坐席点击报障按钮可正常弹窗并提交，ChatPanel 出现系统通知
- [x] 6.2 验证 L2 坐席（/#/agent/2）公共任务池中出现新报障工单，且 ID 格式为 FAULT-xxxxxx
- [x] 6.3 验证无活跃任务时报障弹窗正常打开，预填信息显示为"—"
- [x] 6.4 验证必填字段为空时提交被阻止并显示校验提示
