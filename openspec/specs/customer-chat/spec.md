# 客户聊天规格 (Customer Chat Spec)

**版本**: v1.1
**最后更新**: 2026-04-16
**状态**: 已同步实现

---

## 1. 功能概述

客户侧为模拟手机 App，包含两个主界面：

- **HomeScreen**：服务入口，展示快捷服务、保单概览、AI 入口
- **ChatInterface**：与「宏小二」对话，并支持按意图转接人工专席

当前版本的转人工能力已从“创建通用人工任务”升级为“先识别服务意图，再路由到对应工作流与阶段”。

---

## 2. 路由

| 路由 | 组件 | 说明 |
|------|------|------|
| `/#/customer` | `CustomerView` | 客户端根视图 |

---

## 3. HomeScreen 规格

### 3.1 组件

- **文件**：`frontend/src/components/customer/HomeScreen.vue`
- **Props**：`unreadCount: number`
- **Emits**：`open-chat`

### 3.2 页面结构

| 区域 | 内容 |
|------|------|
| 顶部 Header | 用户问候语、搜索栏 |
| 快捷服务宫格 | 理赔申请 / 保单查询 / 续期缴费 / 全部服务 |
| 产品 Banner | 宣传横幅 |
| 我的保单 | 保单卡片列表 |
| 悬浮 AI 按钮 | 右下角 AI 入口，展示未读徽章 |

### 3.3 业务规则

- 未读消息数 `> 0` 时，悬浮按钮显示红色数字徽章
- 点击悬浮按钮或快捷服务均触发 `open-chat`

---

## 4. ChatInterface 规格

### 4.1 组件

- **文件**：`frontend/src/components/customer/ChatInterface.vue`
- **Props**：`messages: Message[]`、`isLoading: boolean`、`isTransferring: boolean`
- **Emits**：`close`、`send(text: string)`

### 4.2 消息展示

| role/type | 对齐 | 样式 |
|-----------|------|------|
| `user` | 右对齐 | 客户气泡 |
| `bot` | 左对齐 | 「宏小二」气泡 |
| `agent` | 左对齐 | 坐席气泡 |
| `type='system'` | 居中 | 灰色系统提示 |

### 4.3 交互状态

| 状态 | 表现 |
|------|------|
| `isLoading=true` | 展示 AI 回复中动画 |
| `isTransferring=true` | 展示“正在转接人工专席”提示 |
| 新消息到达 | 自动滚动到底部 |

---

## 5. CustomerView 核心流程

- **文件**：`frontend/src/views/CustomerView.vue`
- **当前演示客户**：`CUST-882`

### 5.1 普通 AI 对话

```text
用户发送消息
  -> 追加 user 消息
  -> 调用 getBotResponse(message, history)
  -> 追加 bot 消息
  -> 持久化到 chatStore 与后端
  -> 若当前存在活跃人工任务，则 unreadCount + 1
```

### 5.2 转人工触发条件

当用户消息包含以下任意关键词时，触发人工接入流程：

- `转人工`
- `转坐席`
- `人工`
- `客服`

### 5.3 转人工流程

```text
检测到转人工关键词
  -> 若当前客户已有未完成人工任务，则直接提示“已有人工服务单处理中”
  -> 否则展示“正在识别服务意图并转接对应人工专席”
  -> 调用 POST /api/tasks/handoff
  -> 后端执行：
       1. AI/规则分析 intentCode
       2. 根据 route rule 确定 targetWorkflowCode + entryStageCode
       3. 创建 ServiceTask
  -> 前端插入系统消息，展示已进入的 workflowCode / stageCode
```

### 5.4 转人工分析规则

后端转人工分析支持以下标准意图：

- `policy-service`
- `claims-service`
- `underwriting-service`
- `complaint-service`
- `channel-support`
- `system-fault`
- `general-service`

默认路由规则：

| intentCode | 目标工作流 | 入口阶段 |
|-----------|------------|----------|
| `system-fault` | `system-fault-flow` | `L1` |
| 其他标准意图 | `ops-service-flow` | `L1` |
| 未识别/低置信度 | `ops-service-flow` | `L1` |

### 5.5 回退策略

- AI 分析失败时，后端使用关键词规则回退分类
- 路由规则缺失或禁用时，回退到 `general-service -> ops-service-flow/L1`
- 工作流或入口阶段缺失时，回退到 `ops-service-flow/L1`
- 所有回退都会记录 `routeReason`

---

## 6. 消息持久化

### 6.1 chatStore

- **文件**：`frontend/src/stores/chatStore.ts`

| 字段 | 类型 | 说明 |
|------|------|------|
| `customerHistory` | `Message[]` | 客户侧聊天记录 |
| `lastChatOpenedAt` | `number` | 上次打开聊天时间 |

| 方法 | 说明 |
|------|------|
| `setHistory(messages)` | 覆盖聊天记录 |
| `appendMessage(msg)` | 追加单条消息 |
| `persistMessage(customerId, msg)` | 异步持久化到后端 |
| `loadHistoryFromBackend(customerId)` | 从后端加载并合并本地记录 |

### 6.2 未读消息规则

- 客户侧未读数统计 `role='agent'` 且 `timestamp > lastChatOpenedAt` 的消息
- 打开聊天窗口时更新 `lastChatOpenedAt`

---

## 7. 数据模型

### 7.1 Message

```typescript
type MessageRole = 'user' | 'bot' | 'agent' | 'system'

interface Message {
  id: string
  role: MessageRole
  content: string
  timestamp: number
  type?: 'text' | 'card' | 'system'
  metadata?: unknown
}
```

### 7.2 HandoffAnalysis

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

### 7.3 转人工创建后的任务关键字段

```typescript
interface ServiceTask {
  id: string
  customerId?: string
  category: string
  workflowCode: string
  currentStageCode: string
  currentStageOrder: number
  intentCode?: string
  routeReason?: string
  handoffConfidence?: number
}
```

---

## 8. API 定义

### 8.1 聊天记录

| 方法 | 路径 | 说明 |
|------|------|------|
| `GET` | `/api/chats/{customerId}` | 获取历史消息 |
| `POST` | `/api/chats/{customerId}/messages` | 保存单条消息 |

请求体示例：

```json
{
  "id": "uuid",
  "role": "user",
  "content": "我要转人工",
  "messageType": "text",
  "metadata": null,
  "createdAt": 1776300000000
}
```

### 8.2 转人工分析

| 方法 | 路径 | 说明 |
|------|------|------|
| `POST` | `/api/tasks/handoff-analyze` | 返回意图识别与路由建议 |
| `POST` | `/api/tasks/handoff` | 执行分析并直接创建人工任务 |

请求体示例：

```json
{
  "customerId": "CUST-882",
  "messages": [
    {
      "id": "msg-1",
      "role": "user",
      "content": "系统一直报错，我想转人工处理",
      "type": "text",
      "metadata": null,
      "timestamp": 1776300000000
    }
  ]
}
```

`/api/tasks/handoff` 响应关键字段：

```json
{
  "id": "TASK-1776394056340",
  "category": "系统报障",
  "workflowCode": "system-fault-flow",
  "currentStageCode": "L1",
  "intentCode": "system-fault",
  "routeReason": "route:rule-match",
  "handoffConfidence": 0.92
}
```

---

## 9. 错误处理

- AI 对话失败：前端结束 loading，保留已输入消息，不清空会话
- 消息持久化失败：静默降级，不阻断页面继续使用
- 转人工创建失败：展示“已保留会话内容，请稍后再试”
- 客户已存在活跃人工任务：不重复创建任务，仅提示等待处理

