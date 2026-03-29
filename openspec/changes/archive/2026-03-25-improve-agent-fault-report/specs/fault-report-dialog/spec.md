## ADDED Requirements

### Requirement: 坐席可通过弹窗填写报障信息
系统 SHALL 在坐席点击"生产故障报障"按钮时弹出报障表单对话框，表单中 SHALL 自动预填当前活跃任务的 ID、客户姓名和 AI 摘要（aiSuggestion）作为只读参考信息。

#### Scenario: 打开报障弹窗
- **WHEN** 坐席在 CustomerPanel 点击"生产故障报障"按钮
- **THEN** 弹出 FaultReportDialog，显示当前任务 ID 和客户姓名（只读），以及待填写的故障类型、故障描述、影响范围、紧急程度字段

#### Scenario: 无活跃任务时打开报障弹窗
- **WHEN** 坐席在无活跃任务（currentTask 为 null）时点击报障按钮
- **THEN** 表单中任务 ID 和客户信息显示为"—"，其余字段正常可填写

### Requirement: 报障表单字段校验
系统 SHALL 要求"故障描述"和"紧急程度"为必填字段，其余字段为可选；表单提交前 SHALL 对必填字段进行非空校验。

#### Scenario: 提交时必填字段为空
- **WHEN** 坐席点击"提交报障"但"故障描述"为空
- **THEN** 表单 SHALL 在对应字段下方显示校验错误提示，且不触发提交

#### Scenario: 所有必填字段已填写
- **WHEN** 坐席填写了"故障描述"和"紧急程度"后点击"提交报障"
- **THEN** 表单 SHALL 触发提交逻辑，弹窗关闭

### Requirement: 报障表单支持取消操作
系统 SHALL 在弹窗中提供"取消"按钮，点击后关闭弹窗且不创建任何工单。

#### Scenario: 取消报障
- **WHEN** 坐席点击弹窗中的"取消"按钮
- **THEN** 弹窗关闭，taskStore 中 SHALL NOT 新增任何任务
