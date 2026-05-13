# Design: 报障按钮重命名

## 变更对比

| 位置 | Before | After |
|------|--------|-------|
| CustomerPanel 底部按钮文案 | 生产故障报障 | 宏e站一键报障 |
| customer-chat/spec.md 报障按钮描述 | 生产故障报障 | 宏e站一键报障 |

## 影响范围

- **文件**：`frontend/src/components/agent/CustomerPanel.vue`（按钮文案）
- **文件**：`openspec/specs/agent-workstation/spec.md`（规格同步）

## 风险评估

- 风险极低，仅涉及 UI 文案修改，不影响任何业务逻辑和报障功能

## 回滚计划

将按钮文案改回"生产故障报障"即可。
