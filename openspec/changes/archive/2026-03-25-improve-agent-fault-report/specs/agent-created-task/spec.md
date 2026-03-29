## ADDED Requirements

### Requirement: 提交报障后在 taskStore 创建报障工单
系统 SHALL 在坐席提交报障表单后，调用 `taskStore.createFaultTask()` 创建一条新的 `ServiceTask`，其中 `requestSource` SHALL 为 `'Agent'`，`level` SHALL 为 `2`，`status` SHALL 为 `'Pending'`，`priority` SHALL 按表单紧急程度映射（低→Low, 中→Medium, 高→High, 紧急→Urgent）。

#### Scenario: 成功创建报障工单
- **WHEN** 坐席提交有效的报障表单
- **THEN** taskStore 中 SHALL 新增一条 ServiceTask，其 requestSource 为 'Agent'，level 为 2，status 为 'Pending'，priority 与所选紧急程度对应

#### Scenario: 报障工单出现在 L2 公共任务池
- **WHEN** L2 坐席（/#/agent/2）访问任务公共池
- **THEN** 新创建的报障工单 SHALL 出现在列表中，可被认领

### Requirement: 提交后 ChatPanel 插入系统通知消息
系统 SHALL 在报障工单创建成功后，在当前活跃任务的 ChatPanel 消息列表底部插入一条系统通知，内容 SHALL 包含新建工单的 ID，格式为"已生成报障工单 #<taskId>"。

#### Scenario: 查看系统通知
- **WHEN** 坐席成功提交报障
- **THEN** ChatPanel 消息列表中 SHALL 出现一条 sender 类型为 'system'（或等效展示方式）的通知消息，显示"已生成报障工单 #<taskId>"

### Requirement: 报障工单 ID 格式
系统 SHALL 为报障工单分配唯一 ID，格式 SHALL 为 `FAULT-<timestamp后6位>`，以区别于普通服务任务（TASK-xxx）。

#### Scenario: 报障工单 ID 格式校验
- **WHEN** 创建报障工单
- **THEN** 工单 ID SHALL 匹配正则 `/^FAULT-\d{6}$/`
