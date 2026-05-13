# 报障功能规格 (Fault Report Spec)

**版本**: v1.1
**最后更新**: 2026-04-17
**状态**: 已同步实现

---

## 1. 功能概述

坐席在处理任务时，可通过 CustomerPanel 底部的“宏e站一键报障”按钮打开报障表单，填写故障信息后提交，系统自动创建一条报障链路任务，并在 ChatPanel 中插入系统通知消息。

---

## 2. FaultReportDialog 表单规格

- **文件**：`frontend/src/components/agent/FaultReportDialog.vue`
- **Props**：`modelValue: boolean`（v-model）, `task: ServiceTask | null`, `customerName: string`
- **Emits**：`update:modelValue`, `submitted(taskId: string)`

### 2.1 表单字段

| 字段 | 类型 | 必填 | 预填规则 |
|------|------|------|---------|
| 任务编号 | 文本（只读）| — | 自动填入 task.id |
| 客户姓名 | 文本（只读）| — | 自动填入 customerName |
| 故障类型 | 下拉 | ❌ | 无预填 |
| 故障描述 | 多行文本 | ✅ | 无预填 |
| 影响范围 | 单行文本 | ❌ | 无预填 |
| 紧急程度 | 下拉 | ✅ | 无预填 |

**故障类型枚举**：系统宕机 / 数据异常 / 接口故障 / 性能问题 / 其他

**紧急程度枚举**：低（Low）/ 中（Medium）/ 高（High）/ 紧急（Urgent）

### 2.2 无活跃任务时

当 `task` 为 null 时，任务编号和客户姓名显示为"—"，其余字段正常可填写。

---

## 3. 表单校验规则

| 规则 | 字段 | 错误提示 |
|------|------|---------|
| 非空校验 | 故障描述 | 字段下方显示校验错误 |
| 非空校验 | 紧急程度 | 字段下方显示校验错误 |

- 校验失败时：不触发提交，弹窗保持打开
- 校验通过时：触发提交逻辑，弹窗关闭

---

## 4. 报障工单创建规格

### 4.1 工单 ID 格式

- **格式**：`FAULT-<timestamp后6位>`
- **示例**：`FAULT-823041`
- **校验正则**：`/^FAULT-\d{6}$/`

### 4.2 工单固定属性

| 字段 | 值 |
|------|----|
| requestSource | `'Agent'` |
| workflowCode | `system-fault-flow` |
| currentStageCode | `L1` |
| currentStageOrder | `1` |
| level | `1`（兼容字段，映射报障一线受理）|
| status | `'Pending'` |

### 4.3 优先级映射

| 表单紧急程度 | ServiceTask.priority |
|------------|----------------------|
| 低 | Low |
| 中 | Medium |
| 高 | High |
| 紧急 | Urgent |

### 4.4 创建方法

调用 `taskStore.submitFaultTask(payload)`，payload 结构：

```typescript
interface FaultTaskPayload {
  sourceTaskId: string    // 关联的源任务 ID
  customerName: string
  faultType: string
  description: string     // 必填
  affectedScope?: string
  urgency: 'Low' | 'Medium' | 'High' | 'Urgent'  // 必填
}
```

---

## 5. 业务场景

### Scenario: 打开报障弹窗
- **WHEN** 坐席在 CustomerPanel 点击“宏e站一键报障”按钮
- **THEN** 弹出 FaultReportDialog，显示当前任务 ID 和客户姓名（只读），以及待填写的故障类型、故障描述、影响范围、紧急程度字段

### Scenario: 无活跃任务时打开报障弹窗
- **WHEN** 坐席在无活跃任务（currentTask 为 null）时点击报障按钮
- **THEN** 表单中任务 ID 和客户信息显示为"—"，其余字段正常可填写

### Scenario: 提交时必填字段为空
- **WHEN** 坐席点击"提交报障"但"故障描述"为空
- **THEN** 表单 SHALL 在对应字段下方显示校验错误提示，且不触发提交

### Scenario: 成功提交报障
- **WHEN** 坐席填写了"故障描述"和"紧急程度"后点击"提交报障"
- **THEN** 弹窗关闭，taskStore 中新增一条 ServiceTask（requestSource='Agent', workflowCode='system-fault-flow', currentStageCode='L1', status='Pending'）

### Scenario: 报障工单出现在报障一线公共池
- **WHEN** 坐席访问 `/#/agent/stage/system-fault-flow/L1`
- **THEN** 新创建的报障工单 SHALL 出现在列表中，可被认领

### Scenario: ChatPanel 插入系统通知
- **WHEN** 坐席成功提交报障
- **THEN** ChatPanel 消息列表底部 SHALL 出现一条 role='system' 的通知消息，内容为"已生成报障工单 #<taskId>"

### Scenario: 取消报障
- **WHEN** 坐席点击弹窗中的"取消"按钮
- **THEN** 弹窗关闭，taskStore 中 SHALL NOT 新增任何任务
