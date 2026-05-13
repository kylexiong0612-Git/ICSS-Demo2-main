# 产品需求文档 (PRD)
# ICSS — 一体化渠道智能服务平台

**版本**: v1.2
**日期**: 2026-04-17
**状态**: 已同步当前实现

---

## 1. 产品概述

ICSS（Integrated Omnichannel Customer Service System）是一体化渠道智能服务平台，覆盖客户自助咨询、人工坐席处理、运营监控与配置管理。

当前版本的核心升级点：

- 客户转人工支持意图识别与自动路由
- 坐席链路支持按工作流模板配置不同阶段
- 坐席工作台支持按工作流阶段加载差异化布局
- 管理端支持维护工作流、路由规则和工作台布局

---

## 2. 用户角色与入口

| 角色 | 路由入口 | 核心职责 |
|------|---------|---------|
| 终端客户 | `/#/customer` | 与宏小二对话、发起转人工 |
| 一线/二线兼容坐席 | `/#/agent/1`、`/#/agent/2` | 兼容旧入口，映射默认运营工作流 |
| 阶段化坐席 | `/#/agent/stage/:workflowCode/:stageCode` | 在指定工作流阶段处理任务 |
| 运营管理员 | `/#/admin` | 查看运营监控大屏 |
| 配置管理员 | `/#/admin/agent-workflows`、`/#/admin/workbench-config` | 配置工作流、路由规则、工作台布局 |

顶部演示导航当前默认暴露的阶段入口：

- `运营-一线坐席` -> `/#/agent/stage/ops-service-flow/L1`
- `运营-二线专家` -> `/#/agent/stage/ops-service-flow/L2`
- `报障-一线受理` -> `/#/agent/stage/system-fault-flow/L1`
- `报障-二线定位` -> `/#/agent/stage/system-fault-flow/L2`
- `报障-三线技术支持` -> `/#/agent/stage/system-fault-flow/L3`

---

## 3. 功能模块总览

```text
ICSS 平台
  -> 客户端
     -> HomeScreen
     -> ChatInterface
  -> 坐席端
     -> TaskList
     -> ChatPanel
     -> CustomerPanel
     -> FaultReportDialog
  -> 管理端
     -> AdminDashboardView
     -> AdminWorkflowConfigView
     -> AdminWorkbenchConfigView
```

---

## 4. 客户端需求

### 4.1 AI 对话

- 客户可与「宏小二」进行文本对话
- AI 服务通过后端代理调用 GLM-4-Flash
- 聊天记录同时保存在本地 `chatStore` 与后端

### 4.2 转人工

#### 触发方式

命中以下关键词之一时触发：

- `转人工`
- `转坐席`
- `人工`
- `客服`

#### 处理流程

```text
客户发送转人工请求
  -> 后端分析 intentCode
  -> 根据路由规则映射 workflowCode + stageCode
  -> 创建 ServiceTask
  -> 客户端提示已进入对应专席
```

#### 业务目标

- 把系统报障类需求送入 `system-fault-flow`
- 把运营、理赔、核保、投诉等需求送入 `ops-service-flow`
- 避免所有转人工请求都进入统一公共池

#### 兜底策略

- AI 失败时使用关键词规则回退
- 路由缺失时回退 `ops-service-flow/L1`
- 客户已有未完成任务时不重复建单

---

## 5. 坐席端需求

### 5.1 可配置工作流

系统支持按模板定义不同类型的坐席链路。

当前默认模板：

| 工作流 | 适用场景 | 阶段 |
|--------|---------|------|
| `ops-service-flow` | 运营、理赔、核保、投诉、渠道支持 | `运营-一线坐席 -> 运营-二线专家` |
| `system-fault-flow` | 系统报障 | `报障-一线受理 -> 报障-二线定位 -> 报障-三线技术支持` |

### 5.2 可配置工作台

坐席工作台不再固定为单一三栏布局，而是按 `workflowCode + stageCode` 加载布局配置：

- 支持区域：`top / left / center / right / bottom`
- 支持组件：任务池、我的任务、聊天区、客户画像、保单、AI Copilot、历史记录、报障入口、阶段轨迹、知识卡片
- 某阶段未配置时，回退到默认三栏布局
- 顶部导航优先提供运营链路与报障链路各关键阶段的直达入口

### 5.3 任务处理

坐席可执行：

- 领取任务
- 发送消息
- 推进至下一阶段
- 完成任务
- 发起系统报障

任务推进规则：

```text
Processing
  -> advance
  -> 若有下一阶段：当前任务 Escalated，新建下一阶段 Pending 任务
  -> 若无下一阶段：当前任务 Completed
```

### 5.4 报障工单

- 坐席可从 `CustomerPanel` 进入“宏e站一键报障”
- 报障任务统一进入 `system-fault-flow/L1`
- 后续可继续流转到 `报障-二线定位` 与 `报障-三线技术支持`

---

## 6. 管理端需求

### 6.1 运营大屏

- 继续展示 KPI 卡片、趋势图、类别分布和问题追踪
- 当前版本仍使用静态 Mock 数据

### 6.2 工作流配置

管理员可维护：

- 工作流编码、名称、描述、启用状态
- 分类范围
- 阶段顺序、编码、名称、角色标签、动作集合
- 转人工意图到工作流的路由规则

### 6.3 工作台配置

管理员可维护：

- 工作台布局编码、名称、工作流、阶段、启用状态
- 区域宽度与排序
- 组件类型、显隐、顺序
- 预览某工作流阶段的实际工作台效果

---

## 7. 任务生命周期

### 7.1 状态

- `Pending`
- `Processing`
- `Suspended`
- `Escalated`
- `Completed`

### 7.2 核心字段

```typescript
ServiceTask {
  id,
  requestSource,
  status,
  priority,
  category,
  workflowCode,
  currentStageCode,
  currentStageOrder,
  intentCode,
  routeReason,
  handoffConfidence
}
```

### 7.3 兼容策略

- `level` 字段仍保留
- `/agent/:level` 仍可使用
- `GET /api/tasks?level=...` 仍可使用

---

## 8. 数据模型

### 8.1 核心实体

- `Customer`
- `Policy`
- `Message`
- `ServiceTask`
- `AgentWorkflowTemplate`
- `AgentWorkflowStage`
- `AgentWorkflowRouteRule`
- `WorkbenchLayoutConfig`
- `HandoffAnalysis`

### 8.2 新增配置表

- `agent_workflow_template`
- `agent_workflow_stage`
- `agent_workflow_route_rule`
- `workbench_layout_config`

### 8.3 关键新增字段

`service_task` 新增：

- `workflow_code`
- `current_stage_code`
- `current_stage_order`
- `source_task_id`
- `intent_code`
- `route_reason`
- `handoff_confidence`

---

## 9. API 汇总

### 9.1 任务接口

| 方法 | 路径 | 说明 |
|------|------|------|
| `GET` | `/api/tasks` | 支持 `level/workflowCode/stageCode/customerId/status` 查询 |
| `POST` | `/api/tasks` | 创建普通任务 |
| `PUT` | `/api/tasks/{id}/grab` | 领取任务 |
| `PUT` | `/api/tasks/{id}/advance` | 推进到下一阶段 |
| `PUT` | `/api/tasks/{id}/escalate` | 兼容旧入口 |
| `PUT` | `/api/tasks/{id}/complete` | 完成任务 |
| `POST` | `/api/tasks/fault` | 创建报障工单 |
| `POST` | `/api/tasks/handoff-analyze` | 转人工分析 |
| `POST` | `/api/tasks/handoff` | 创建转人工任务 |

### 9.2 配置接口

| 方法 | 路径 | 说明 |
|------|------|------|
| `GET/POST/PUT` | `/api/agent-workflows...` | 工作流模板管理 |
| `GET/PUT` | `/api/agent-workflow-routes...` | 路由规则管理 |
| `GET/POST/PUT` | `/api/workbench-layouts...` | 工作台布局管理 |
| `GET` | `/api/workbench-layouts/effective` | 获取阶段生效布局 |

### 9.3 消息接口

| 方法 | 路径 | 说明 |
|------|------|------|
| `GET` | `/api/chats/{customerId}` | 获取聊天历史 |
| `POST` | `/api/chats/{customerId}/messages` | 保存消息 |

---

## 10. 非功能性需求

### 10.1 安全性

- AI 密钥仅通过服务端环境变量注入
- 前端不暴露任何模型密钥

### 10.2 可用性

- 聊天消息持久化失败时不阻断主流程
- 转人工分析失败时允许回退到默认链路继续建单
- 布局配置缺失时允许回退默认工作台

### 10.3 性能

- 任务列表在前端本地筛选
- 工作台阶段数据按进入页面时加载

---

## 11. 当前限制

- 运营大屏数据仍为静态 Mock
- 客户历史记录部分仍为演示数据
- 当前仅支持文本对话
- `level` 仍处于兼容保留阶段，后续可逐步收敛到纯工作流模型

---

## 12. 后续方向

- 增加更多业务工作流模板
- 支持更细粒度的坐席权限与身份
- 支持更丰富的工作台组件参数化
- 为大屏接入实时数据源
- 逐步移除旧 `level` 模型的硬兼容
