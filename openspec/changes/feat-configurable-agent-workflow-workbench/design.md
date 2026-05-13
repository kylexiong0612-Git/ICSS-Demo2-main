# Design: 可配置坐席工作流与工作台编排

## 变更对比

| 位置 | Before | After |
|------|--------|-------|
| 坐席处理链路 | 固定 L1/L2 | 基于工作流模板定义任意顺序阶段 |
| 任务层级模型 | `level: 1 | 2` | `workflowCode + currentStageCode + stageOrder` |
| 升级逻辑 | 固定“一线升级二线” | 根据工作流模板流转到下一阶段 |
| 坐席工作台 | 固定三栏布局 | 基于页面配置渲染区域与组件 |
| 工作台差异化 | 通过 `level` 切换同一组件 | 按“工作流 + 阶段”加载对应布局 |
| 顶部演示导航 | 泛化命名，且报障仅单入口 | 使用业务化命名，并提供报障 `L1/L2/L3` 直达入口 |

## 方案说明

本次变更拆为两个相互配合的能力：

1. **坐席工作流配置**
2. **转人工意图识别与任务路由**
3. **坐席工作台配置**
4. **演示导航与阶段入口映射**

二者通过“工作流编码 + 阶段编码”关联。

---

## 一、坐席工作流配置

### 1.1 核心概念

#### AgentWorkflowTemplate

表示一条可配置的坐席处理链路，例如：

- `ops-service-flow`：运营坐席链路，包含 `L1 -> L2`
- `system-fault-flow`：系统报障坐席链路，包含 `L1 -> L2 -> L3`

#### AgentWorkflowStage

表示工作流中的一个处理阶段，定义：

- 阶段编码，如 `L1`、`L2`、`L3`
- 阶段名称，如“一线坐席”“二线专家”“三线技术支持”
- 顺序号，如 `1 / 2 / 3`
- 可执行动作，如领取、转派、升级、完成、挂起

### 1.2 数据模型调整

#### ServiceTask 新增字段

```typescript
interface ServiceTask {
  workflowCode: string
  currentStageCode: string
  currentStageOrder: number
  sourceTaskId?: string
}
```

#### ServiceTask 兼容策略

- 现有 `level` 字段在过渡期保留，用于兼容旧逻辑和旧路由
- 默认映射规则：
  - `level=1` -> `workflowCode='ops-service-flow'` + `currentStageCode='L1'`
  - `level=2` -> `workflowCode='ops-service-flow'` + `currentStageCode='L2'`
- 后续稳定后再评估是否完全移除 `level`

#### 新增配置实体

```typescript
interface AgentWorkflowTemplate {
  code: string
  name: string
  categoryScope: string[]
  enabled: boolean
  stages: AgentWorkflowStage[]
}

interface AgentWorkflowStage {
  code: string
  name: string
  order: number
  roleLabel: string
  description?: string
  allowedActions: Array<'grab' | 'transfer' | 'escalate' | 'complete' | 'suspend' | 'resume'>
}
```

### 1.3 任务分配与流转规则

#### 新任务创建

- 任务创建时根据任务类型或来源选择默认工作流
- 普通运营类任务默认进入 `ops-service-flow/L1`
- 系统报障类任务默认进入 `system-fault-flow/L1`

#### 升级/流转

不再写死“升级二线”，而是按工作流配置查找下一阶段：

```text
当前任务 Processing
  -> 执行 escalate / transfer-next
  -> 当前任务标记为 Escalated 或 Transferred
  -> 创建下一阶段 Pending 任务
  -> 新任务继承 category / summary / customerId / sourceTaskId
```

#### 报障链路示例

```text
system-fault-flow
  L1 一线受理
    -> L2 故障定位
      -> L3 技术支持
```

### 1.4 API 设计

#### 工作流配置接口

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/api/agent-workflows` | 查询工作流模板列表 |
| GET | `/api/agent-workflows/{code}` | 查询单个工作流模板 |
| POST | `/api/agent-workflows` | 新增工作流模板 |
| PUT | `/api/agent-workflows/{code}` | 更新工作流模板 |
| PUT | `/api/tasks/{id}/advance` | 将任务推进到下一个阶段 |

#### 任务列表接口调整

`GET /api/tasks` 查询维度从 `level` 扩展为：

- `workflowCode`
- `stageCode`
- `status`

过渡期允许继续接受 `level` 参数并映射到默认工作流。

### 1.5 流程图

```text
任务创建
  -> 选择工作流模板
  -> 进入首阶段公共池
  -> 坐席领取
  -> Processing
  -> 推进下一阶段 / 完成 / 挂起
```

---

## 二、转人工意图识别与任务路由

### 2.1 问题定义

当前客户侧在识别到“转人工”关键词后，只会创建一条通用人工任务，无法判断该任务应进入哪条坐席链路。

这会导致：

- 运营咨询、理赔咨询、投诉建议、系统报障等任务全部进入同一类坐席池
- 坐席链路虽然可配置，但转人工入口没有与配置链路真正打通
- 系统无法根据对话内容自动决定首个进入的工作流和阶段

### 2.2 设计原则

- 先识别“是否需要人工”
- 再识别“属于哪类人工服务意图”
- 最后根据路由规则匹配目标工作流和首阶段

整体采用“AI 识别 + 规则映射 + 默认回退”的方式，避免纯模型判断带来的不稳定性。

### 2.3 意图识别模型

在现有 `preProcessTask(history)` 的基础上扩展转人工分析结果：

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

建议首版支持的 `intentCode`：

- `policy-service`
- `claims-service`
- `underwriting-service`
- `complaint-service`
- `channel-support`
- `system-fault`
- `general-service`

### 2.4 路由规则模型

新增意图到工作流的映射配置：

```typescript
interface AgentWorkflowRouteRule {
  intentCode: string
  targetWorkflowCode: string
  entryStageCode: string
  priority?: 'inherit' | 'force-medium' | 'force-high' | 'force-urgent'
  enabled: boolean
}
```

默认映射建议：

| intentCode | targetWorkflowCode | entryStageCode |
|-----------|--------------------|----------------|
| `policy-service` | `ops-service-flow` | `L1` |
| `claims-service` | `ops-service-flow` | `L1` |
| `underwriting-service` | `ops-service-flow` | `L1` |
| `complaint-service` | `ops-service-flow` | `L1` |
| `channel-support` | `ops-service-flow` | `L1` |
| `system-fault` | `system-fault-flow` | `L1` |
| `general-service` | `ops-service-flow` | `L1` |

### 2.5 客户侧转人工流程

```text
客户发送消息
  -> 命中“转人工”触发条件
  -> 调用 handoff analysis
  -> 识别 intentCode / confidence
  -> 根据 route rule 确定 targetWorkflowCode + entryStageCode
  -> 创建 ServiceTask
  -> 任务进入对应工作流首阶段公共池
  -> 客户看到“已为您转接到对应专席”的系统提示
```

### 2.6 路由决策规则

- 当 `confidence >= 阈值` 且存在启用中的路由规则时，按识别结果路由
- 当识别结果存在，但没有命中有效路由规则时，回退到 `ops-service-flow/L1`
- 当 AI 识别失败或置信度不足时，回退到 `general-service -> ops-service-flow/L1`
- 当识别为 `system-fault` 时，优先进入 `system-fault-flow/L1`
- 后续坐席升级或流转仍遵循该工作流的阶段定义，不再重新分类

### 2.7 API 设计

可选实现方案一：扩展现有任务预处理接口返回

可选实现方案二：新增专用接口

| 方法 | 路径 | 说明 |
|------|------|------|
| POST | `/api/tasks/handoff-analyze` | 分析转人工意图并返回路由建议 |
| POST | `/api/tasks/handoff` | 根据分析结果创建人工任务 |

若延用现有 `preProcessTask`，则需要返回：

- `intentCode`
- `targetWorkflowCode`
- `targetStageCode`
- `confidence`

### 2.8 ServiceTask 创建规则补充

客户转人工创建任务时：

- `requestSource='Customer'`
- `workflowCode` 由路由规则决定
- `currentStageCode` 使用目标工作流首阶段
- `currentStageOrder` 取首阶段顺序号
- `category` 优先取 `intentName` 或标准分类映射
- `summary/suggestion/tags` 由 AI 分析结果生成

### 2.9 失败与回退策略

- AI 超时或失败：创建默认运营链路任务，并记录 `routeReason='fallback:ai-error'`
- 工作流不存在或被禁用：回退到 `ops-service-flow/L1`
- 首阶段未配置：回退到工作流最小顺序阶段；仍失败则回退默认链路
- 任何回退都应在任务元数据中记录，便于后续审计与调优

---

## 三、坐席工作台配置

### 2.1 核心思路

将当前固定三栏工作台抽象为“页面区域 + 可投放组件 + 组件参数”的配置模型。

第一阶段采用**配置驱动编排**，而不是完整低代码拖拽引擎：

- 支持区域开关
- 支持区域内组件排序
- 支持组件显隐
- 支持组件参数配置
- 支持按工作流阶段切换布局

### 2.2 页面模型

```typescript
interface WorkbenchLayoutConfig {
  code: string
  name: string
  workflowCode: string
  stageCode: string
  regions: WorkbenchRegion[]
  enabled: boolean
}

interface WorkbenchRegion {
  code: 'left' | 'center' | 'right' | 'top' | 'bottom'
  width?: string
  widgets: WorkbenchWidget[]
}

interface WorkbenchWidget {
  code: string
  type: string
  title?: string
  visible: boolean
  order: number
  props?: Record<string, unknown>
}
```

### 2.3 组件库范围

首版建议支持以下工作台组件：

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

### 2.4 页面配置示例

#### 运营链路 L1

```text
left: [task-pool, my-task-list]
center: [chat-panel]
right: [customer-profile, policy-list, ai-copilot]
```

#### 系统报障链路 L3

```text
left: [my-task-list, stage-timeline]
center: [chat-panel, knowledge-card]
right: [fault-report-entry, history-records, ai-copilot]
```

### 2.5 前端渲染策略

- 新增工作台配置加载入口，根据当前任务或当前路由解析 `workflowCode/stageCode`
- `AgentWorkstationView` 从固定布局改为配置驱动容器
- 各业务组件保留现有职责，但改为通过 widget registry 动态挂载

### 2.6 配置管理界面

新增管理端配置页面，至少包含：

- 工作流模板管理
- 阶段节点管理
- 工作台布局配置
- 布局预览与启停

建议新增路由：

- `/#/admin/agent-workflows`
- `/#/admin/workbench-config`

### 2.7 业务规则

- 每个工作流至少包含 1 个阶段
- 阶段顺序必须唯一且连续
- 每个“工作流 + 阶段”最多启用 1 套工作台布局
- `chat-panel` 为必备组件，禁止删除
- 若未命中布局配置，回退到默认三栏工作台

---

## 四、演示导航与阶段入口映射

### 4.1 问题定义

虽然工作流和工作台已经配置化，但顶部导航仍使用“ 一线坐席 / 二线坐席 / 报障专席 ”等泛化命名，且没有暴露报障链路 `L2 / L3` 入口。

这会带来两个问题：

- 页面入口名称与工作流阶段名称不一致，用户难以快速理解当前所在链路
- 报障链路的二线、三线阶段缺少一跳直达入口，演示与验证效率较低

### 4.2 设计原则

- 导航文案直接体现业务链路和阶段职责
- 导航入口直接映射到标准阶段路由 `/#/agent/stage/:workflowCode/:stageCode`
- 不新增新的页面组件，继续复用 `AgentWorkstationView`

### 4.3 导航映射方案

| 导航文案 | 路由 | 说明 |
|---------|------|------|
| `运营-一线坐席` | `/#/agent/stage/ops-service-flow/L1` | 运营链路一线入口 |
| `运营-二线专家` | `/#/agent/stage/ops-service-flow/L2` | 运营链路二线入口 |
| `报障-一线受理` | `/#/agent/stage/system-fault-flow/L1` | 报障链路一线入口 |
| `报障-二线定位` | `/#/agent/stage/system-fault-flow/L2` | 报障链路二线入口 |
| `报障-三线技术支持` | `/#/agent/stage/system-fault-flow/L3` | 报障链路三线入口 |

### 4.4 兼容策略

- 保留旧入口 `/#/agent/1` 与 `/#/agent/2`，继续兼容默认运营链路
- 顶部导航优先使用阶段路由，减少对旧 `level` 路由的依赖
- 新增报障 `L2 / L3` 入口时，不增加新的页面文件，仅增加导航入口

---

## 五、对现有规格的影响

预计会影响以下规格：

- `openspec/specs/agent-workstation/spec.md`
- `openspec/specs/task-lifecycle/spec.md`
- `openspec/specs/data-model/spec.md`
- `openspec/specs/admin-dashboard/spec.md`
- `openspec/specs/customer-chat/spec.md`

同时需补充：

- 坐席工作流配置相关业务规则
- 转人工意图识别与任务路由规则
- 工作台布局配置模型
- 新增 API 的 Request/Response
- 错误处理与回退策略

---

## 六、风险评估

- **模型迁移风险**：当前 `level` 深度绑定前后端逻辑，改为工作流配置后可能出现兼容问题
- **识别准确率风险**：转人工意图识别错误会导致任务进入错误坐席链路
- **页面复杂度风险**：工作台配置化后，渲染逻辑与状态管理会明显复杂
- **配置错误风险**：错误布局配置可能导致关键组件缺失，影响坐席操作
- **范围膨胀风险**：如果首版直接做成通用拖拽低代码，开发周期和稳定性风险都会升高

## 七、缓解策略

- 保留 `level` 到默认工作流的映射，采用兼容迁移
- 采用“AI 识别 + 路由规则 + 默认回退”三层机制，避免单点判断失败
- 为路由结果记录 `confidence` 和 `routeReason`，便于后续运营调优
- 先支持有限组件库和固定区域，不直接开放任意嵌套布局
- 对配置增加校验规则和默认回退布局
- 将“工作流配置”和“工作台配置”分阶段实现与联调

## 八、回滚计划

- 保留原有固定 `L1/L2` 路由与三栏布局实现
- 保留“转人工统一进入默认运营链路”的兜底逻辑
- 若配置化方案不稳定，可切回默认工作流和默认工作台
- 新增配置表与接口可停用，不影响旧任务基础处理流程
