# 项目上下文 (Project Context)

## 项目用途

**ICSS Demo 1** — 一体化渠道智能服务平台（Integrated Omnichannel Customer Service System）

面向保险公司渠道服务中心的全渠道服务演示原型，实现以下核心场景：

- **客户侧**：模拟手机 App，提供 AI 智能客服（宏小二）、保单查询、服务申请
- **坐席侧**：L1/L2 双层坐席工作台，含任务队列、实时聊天、客户 360° 视图、AI 辅助建议
- **管理侧**：运营监控大屏，含实时指标（等待人数、在线坐席、首次解决率、机器人分流率）
- **AI 驱动**：接入智谱 GLM-4-Flash，实现对话意图识别、任务分析、回复生成

支持的服务类型：理赔咨询、核保咨询、保单变更、渠道支持、投诉建议。

---

## 技术栈

| 层级 | 技术 | 版本 |
|------|------|------|
| 前端框架 | Vue | 3.5.13 |
| 语言 | TypeScript | ~5.8.2 |
| 构建工具 | Vite | 6.2.0 |
| PC UI 组件库 | Element Plus | 2.8.0 |
| PC 图标 | @element-plus/icons-vue | 2.3.1 |
| 移动端 UI 组件库 | Vant | 4.8.5 |
| 样式预处理 | Sass | 1.77.0 |
| 状态管理 | Pinia | 2.2.0 |
| 状态持久化 | pinia-plugin-persistedstate | 3.2.0 |
| 路由 | Vue Router | 4.3.0 |
| HTTP 客户端 | Axios | 1.8.4 |
| 图表 | ECharts | 5.5.0 |
| 图表（Vue 封装） | vue-echarts | 7.0.0 |
| 移动端适配 | amfe-flexible | 2.2.1 |
| px→rem 转换 | postcss-pxtorem | 6.1.0 |
| 自动导入（API） | unplugin-auto-import | 0.18.0 |
| 自动导入（组件） | unplugin-vue-components | 0.27.0 |

**当前激活的 AI 服务**：智谱 AI（GLM-4-Flash），接口地址 `https://open.bigmodel.cn/api/paas/v4/chat/completions`，密钥通过 `ZHIPU_API_KEY` 环境变量注入（服务端注入，不暴露到前端 bundle）。

---

## 代码规范

### 通用规范（本项目 Vue 3 / TypeScript）

- **命名**：组件文件使用大驼峰（`AgentWorkstationView.vue`），变量/函数使用小驼峰（`serviceTask`），常量使用全大写下划线（`MAX_RETRY_COUNT`）
- **组件结构**：使用 `<script setup lang="ts">` Composition API，顺序为 import → defineProps/defineEmits → 响应式状态 → 计算属性 → 方法 → 生命周期
- **TypeScript**：所有 props 和状态必须明确类型，接口定义统一在 `src/types/index.ts`
- **样式**：使用 `<style scoped lang="scss">`，SCSS 变量统一在 `src/assets/styles/variable.scss`，避免内联样式
- **API 调用**：统一封装在 `src/api/` 目录，HTTP 实例封装在 `src/utils/request.ts`，不在组件内直接发起 HTTP 请求
- **状态管理**：业务状态使用 Pinia store（`src/stores/`），组件间通信优先 props/emit
- **缩进**：2 个空格

### 公司通用规范（参见 dev-standards/ 目录）

- **PC 端**（Vue 项目）：Element Plus，Sass scoped 样式，接口定义在 `src/api/`，axios 封装在 `src/utils/request.ts`
- **移动端**（Vue 项目）：Vue 3 Composition API（script setup），Vant 4，Pinia 状态管理，px-to-rem 移动端适配（根字体 37.5px，设计稿 375px）

---

## 领域知识

### 保险业务

- **客户层级**：Normal < Silver < Gold < VIP，影响服务优先级和响应时效
- **保单状态**：Active（有效）/ Lapsed（失效）/ Pending（待生效）
- **任务优先级**：Low < Medium < High < Urgent
- **任务状态**：Pending → Processing → (Suspended | Escalated | Completed)
- **请求来源**：Customer（客户直接发起）/ Agent（渠道代理人发起）
- **服务等级**：L1 坐席处理常规咨询，L2 坐席处理升级复杂案件

### 核心数据模型

```typescript
ServiceTask { id, category, priority, status, level(1|2), requestSource,
              chatHistory, aiSuggestion, assignedTo }
Customer    { id, name, level, policies[], agent }
Policy      { id, name, status, premium, startDate }
Message     { id, role('user'|'bot'|'agent'), content, type, timestamp }
DashboardStats { waitingCount, onlineAgents, avgHandlingTime, fcr, botDeflectionRate }
```

### AI 助手角色设定

- 客服 Bot 人设为「宏小二」，需保持专业、简洁的保险服务话术
- 系统提示词包含：公司背景、服务范围、禁止话题（如竞品对比、投资建议）
- JSON 模式用于任务分析（类别识别、优先级评估、处理建议）

---

## 约束条件

### 技术约束

- 项目为**演示原型**，所有客户数据为 Mock，不连接真实后端数据库
- 初始演示数据硬编码在组件内（TASK-001、TASK-002、CUST-882）
- 开发服务器监听 `0.0.0.0:3000`，可通过 `DISABLE_HMR=true` 关闭热更新
- 路径别名 `@/` 指向 `src/` 目录

### 安全约束

- API 密钥通过环境变量注入，禁止硬编码在源码中
- `.env` 文件已在 `.gitignore` 中排除，使用 `.env.example` 作为模板
- `metadata.json` 声明了摄像头和麦克风权限（camera、microphone），新增媒体功能需更新此文件

### 功能约束

- 多视图切换（Customer / Agent L1 / Agent L2 / Admin）通过 Vue Router（hash 模式）实现，路由路径为：`/customer`、`/agent/1`、`/agent/2`、`/admin`
- 当前 AI 服务仅支持文本对话，不支持多模态输入
- 图表（ECharts）数据目前为静态 Mock，未接入实时数据流

### 开发约束

- 本项目技术栈为 **Vue 3 + TypeScript**，spec/ 目录中的规范同样适用于此项目
- 新增视图组件应放在 `src/views/`，新增功能组件应放在 `src/components/`
- 新增 API 调用应放在 `src/api/`，新增业务 store 应放在 `src/stores/`
- 类型定义统一维护在 `src/types/index.ts`，避免在组件文件内定义业务类型
