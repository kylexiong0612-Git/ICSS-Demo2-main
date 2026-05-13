# Design: 新增 Codex 项目级协作文档

## 变更对比

| 位置 | Before | After |
|------|--------|-------|
| 仓库根目录 | 无 `AGENTS.md` | 新增 `AGENTS.md` |
| Codex 入口说明 | 依赖会话内指令和人工口头说明 | 增加仓库内可读的项目级约定文档 |

## 方案说明

在仓库根目录新增 `AGENTS.md`，内容聚焦以下方面：

- 项目身份与目录约定：说明 `frontend`、`backend`、`openspec` 的角色
- 最高优先级规则：要求先读取 `openspec/project.md`
- 工作流约定：明确 `Analyze -> Propose -> Approve -> Implement -> Sync`
- 修改红线：未提案不改业务代码，未批准不实施
- 仓库速查：补充启动命令、默认端口、关键技术栈
- 协作约定：默认中文、先说明再操作、存在风险先确认

## 影响范围

- **新增文件**：`AGENTS.md`

## 对规格与 PRD 的影响

- 本次为仓库协作文档补充，不涉及业务行为、数据模型或 API 变化
- 预计无需修改 `openspec/specs/` 和 `PRD.md`

## 风险评估

- 风险低，主要风险是与 `CLAUDE.md` 或 `openspec/project.md` 表述不一致
- 通过将 `openspec/project.md` 设为权威来源，并让 `AGENTS.md` 保持摘要化，可降低漂移风险

## 回滚计划

- 删除新增的 `AGENTS.md`
- 如需保留说明入口，则继续仅使用现有 `CLAUDE.md`
