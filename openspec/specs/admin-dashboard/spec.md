# 管理端规格 (Admin Dashboard and Config Spec)

**版本**: v1.1
**最后更新**: 2026-04-16
**状态**: 已同步实现

---

## 1. 功能概述

管理端当前包含两类能力：

1. 运营监控大屏
2. 坐席配置中心

其中运营大屏仍以静态 Mock 数据为主；坐席配置中心已支持维护工作流、路由规则和工作台布局。

---

## 2. 路由

| 路由 | 视图 | 说明 |
|------|------|------|
| `/#/admin` | `AdminDashboardView` | 运营监控大屏 |
| `/#/admin/agent-workflows` | `AdminWorkflowConfigView` | 工作流与转人工路由配置 |
| `/#/admin/workbench-config` | `AdminWorkbenchConfigView` | 工作台布局配置 |

---

## 3. 运营监控大屏

### 3.1 页面结构

```text
AdminDashboardView
  -> StatsGrid
  -> TrendChart
  -> CategoryChart
  -> IssueList
```

### 3.2 当前状态

- 所有统计图表仍为静态 Mock
- 页面新增跳转按钮，可进入：
  - 工作流配置页
  - 工作台配置页

---

## 4. 工作流配置页

- **文件**：`frontend/src/views/AdminWorkflowConfigView.vue`

### 4.1 工作流模板维护

可维护字段：

| 字段 | 说明 |
|------|------|
| `code` | 工作流编码 |
| `name` | 工作流名称 |
| `description` | 工作流描述 |
| `enabled` | 是否启用 |
| `categoryScope` | 覆盖的服务类别 |

### 4.2 阶段配置

每个阶段可维护：

| 字段 | 说明 |
|------|------|
| `stageOrder` | 顺序号 |
| `code` | 阶段编码 |
| `name` | 阶段名称 |
| `roleLabel` | 角色标签 |
| `allowedActions` | 支持动作集合 |

支持操作：

- 新增工作流
- 新增阶段
- 删除阶段
- 保存工作流
- 预览某阶段工作台

### 4.3 转人工路由规则维护

支持维护：

| 字段 | 说明 |
|------|------|
| `intentCode` | 标准意图编码 |
| `intentName` | 意图名称 |
| `targetWorkflowCode` | 目标工作流 |
| `entryStageCode` | 入口阶段 |
| `priorityStrategy` | 优先级策略 |
| `enabled` | 是否启用 |

---

## 5. 工作台配置页

- **文件**：`frontend/src/views/AdminWorkbenchConfigView.vue`

### 5.1 布局基础信息

| 字段 | 说明 |
|------|------|
| `code` | 布局编码 |
| `name` | 布局名称 |
| `workflowCode` | 关联工作流 |
| `stageCode` | 关联阶段 |
| `enabled` | 是否启用 |

### 5.2 区域编排

每个布局由多个 `region` 组成：

- `top`
- `left`
- `center`
- `right`
- `bottom`

每个区域支持：

- 设置宽度
- 新增组件
- 删除区域
- 调整组件顺序
- 控制组件显隐

### 5.3 组件类型

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

### 5.4 预览能力

- 可直接跳转到 `/#/agent/stage/:workflowCode/:stageCode`
- 用于验证配置后的工作台效果

---

## 6. API 定义

### 6.1 工作流与路由

| 方法 | 路径 | 说明 |
|------|------|------|
| `GET` | `/api/agent-workflows` | 查询全部工作流 |
| `GET` | `/api/agent-workflows/{code}` | 查询单个工作流 |
| `POST` | `/api/agent-workflows` | 新增工作流 |
| `PUT` | `/api/agent-workflows/{code}` | 更新工作流 |
| `GET` | `/api/agent-workflow-routes` | 查询全部路由规则 |
| `PUT` | `/api/agent-workflow-routes/{intentCode}` | 更新路由规则 |

### 6.2 工作台布局

| 方法 | 路径 | 说明 |
|------|------|------|
| `GET` | `/api/workbench-layouts` | 查询全部布局 |
| `GET` | `/api/workbench-layouts/effective` | 查询生效布局 |
| `POST` | `/api/workbench-layouts` | 新增布局 |
| `PUT` | `/api/workbench-layouts/{code}` | 更新布局 |

---

## 7. 错误处理

- 工作流或布局保存失败：前端保留当前编辑内容，不清空表单
- 某阶段无启用布局：`effective` 接口回退到 `ops-service-flow/L1`
- 路由规则编辑不完整：以前端表单保存结果为准，后端按 upsert 覆盖

