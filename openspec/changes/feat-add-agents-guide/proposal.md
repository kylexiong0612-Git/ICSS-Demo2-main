# Proposal: 新增 Codex 项目级协作文档

**变更编号**: feat-add-agents-guide
**日期**: 2026-04-07

## 问题陈述

当前仓库存在面向 Claude 的入口文档 `CLAUDE.md`，但缺少一份面向 Codex 或通用 agent 的项目级协作说明，导致不同 agent 在进入仓库时难以快速对齐本项目的 OpenSpec 流程、技术边界和协作约定。

## 目标

- 在仓库根目录新增 `AGENTS.md`
- 明确 `openspec/project.md` 是最高优先级规则来源
- 为 Codex 提供与当前项目匹配的最小必要操作约定

## 非目标

- 不修改任何前后端业务代码
- 不替换或删除现有 `CLAUDE.md`
- 不改变 OpenSpec 的既有规范内容

## 预期价值

- 降低 Codex 进入仓库后的上下文建立成本
- 减少 agent 对 SDD 流程的误判和越界修改
- 保持 Claude 与 Codex 两套入口说明并存，互不干扰
