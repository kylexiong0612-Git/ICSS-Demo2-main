-- ============================================================
-- ICSS Database Initialization Script
-- ============================================================

CREATE DATABASE IF NOT EXISTS icss_db
  DEFAULT CHARACTER SET utf8mb4
  DEFAULT COLLATE utf8mb4_unicode_ci;

USE icss_db;

-- ------------------------------------------------------------
-- agent（渠道代理人）
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS agent (
  id         VARCHAR(50)  NOT NULL PRIMARY KEY,
  name       VARCHAR(100) NOT NULL,
  code       VARCHAR(50)  NOT NULL COMMENT '工号',
  phone      VARCHAR(20),
  branch     VARCHAR(100) COMMENT '所属机构',
  created_at BIGINT       NOT NULL DEFAULT (UNIX_TIMESTAMP(NOW(3)) * 1000)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ------------------------------------------------------------
-- customer（客户）
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS customer (
  id         VARCHAR(50)  NOT NULL PRIMARY KEY,
  name       VARCHAR(100) NOT NULL,
  phone      VARCHAR(20)  NOT NULL,
  id_number  VARCHAR(50)  COMMENT '身份证号（脱敏）',
  tier       VARCHAR(20)  NOT NULL DEFAULT '普通' COMMENT '普通 | 银卡 | 金卡 | 白金',
  agent_id   VARCHAR(50),
  created_at BIGINT       NOT NULL DEFAULT (UNIX_TIMESTAMP(NOW(3)) * 1000)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ------------------------------------------------------------
-- policy（保单）
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS policy (
  id          VARCHAR(50)    NOT NULL PRIMARY KEY,
  customer_id VARCHAR(50)    NOT NULL,
  name        VARCHAR(200)   NOT NULL COMMENT '险种名称',
  status      VARCHAR(20)    NOT NULL DEFAULT 'Active' COMMENT 'Active | Lapsed | Pending',
  premium     DECIMAL(12, 2) NOT NULL DEFAULT 0,
  start_date  DATE,
  end_date    DATE,
  created_at  BIGINT         NOT NULL DEFAULT (UNIX_TIMESTAMP(NOW(3)) * 1000),
  INDEX idx_customer_id (customer_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ------------------------------------------------------------
-- service_task（服务任务）
-- created_at / updated_at 存 epoch ms，与前端 number 类型对齐
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS service_task (
  id             VARCHAR(50)  NOT NULL PRIMARY KEY,
  customer_id    VARCHAR(50)  COMMENT '关联客户，坐席发起任务可为 NULL',
  agent_id       VARCHAR(50)  COMMENT '渠道代理人 ID',
  request_source VARCHAR(20)  NOT NULL DEFAULT 'Customer' COMMENT 'Customer | Agent',
  status         VARCHAR(20)  NOT NULL DEFAULT 'Pending'
                              COMMENT 'Pending | Processing | Suspended | Completed | Escalated',
  priority       VARCHAR(20)  NOT NULL DEFAULT 'Medium' COMMENT 'Low | Medium | High | Urgent',
  category       VARCHAR(100) NOT NULL DEFAULT '通用咨询',
  summary        TEXT,
  ai_suggestion  TEXT,
  assigned_to    VARCHAR(50)  COMMENT '坐席工号或 ID',
  unread_count   INT          NOT NULL DEFAULT 0,
  level          TINYINT      NOT NULL DEFAULT 1 COMMENT '1=一线 2=二线',
  tags           JSON         COMMENT '["理赔咨询","高优"]',
  created_at     BIGINT       NOT NULL DEFAULT (UNIX_TIMESTAMP(NOW(3)) * 1000),
  updated_at     BIGINT       NOT NULL DEFAULT (UNIX_TIMESTAMP(NOW(3)) * 1000),
  INDEX idx_level_status (level, status),
  INDEX idx_customer_id (customer_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

SET @schema_name = DATABASE();

SET @sql = IF(
  (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
   WHERE TABLE_SCHEMA = @schema_name AND TABLE_NAME = 'service_task' AND COLUMN_NAME = 'workflow_code') = 0,
  'ALTER TABLE service_task ADD COLUMN workflow_code VARCHAR(100) NOT NULL DEFAULT ''ops-service-flow'' AFTER level',
  'SELECT 1'
);
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @sql = IF(
  (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
   WHERE TABLE_SCHEMA = @schema_name AND TABLE_NAME = 'service_task' AND COLUMN_NAME = 'current_stage_code') = 0,
  'ALTER TABLE service_task ADD COLUMN current_stage_code VARCHAR(50) NOT NULL DEFAULT ''L1'' AFTER workflow_code',
  'SELECT 1'
);
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @sql = IF(
  (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
   WHERE TABLE_SCHEMA = @schema_name AND TABLE_NAME = 'service_task' AND COLUMN_NAME = 'current_stage_order') = 0,
  'ALTER TABLE service_task ADD COLUMN current_stage_order INT NOT NULL DEFAULT 1 AFTER current_stage_code',
  'SELECT 1'
);
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @sql = IF(
  (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
   WHERE TABLE_SCHEMA = @schema_name AND TABLE_NAME = 'service_task' AND COLUMN_NAME = 'source_task_id') = 0,
  'ALTER TABLE service_task ADD COLUMN source_task_id VARCHAR(50) NULL AFTER current_stage_order',
  'SELECT 1'
);
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @sql = IF(
  (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
   WHERE TABLE_SCHEMA = @schema_name AND TABLE_NAME = 'service_task' AND COLUMN_NAME = 'intent_code') = 0,
  'ALTER TABLE service_task ADD COLUMN intent_code VARCHAR(100) NULL AFTER source_task_id',
  'SELECT 1'
);
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @sql = IF(
  (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
   WHERE TABLE_SCHEMA = @schema_name AND TABLE_NAME = 'service_task' AND COLUMN_NAME = 'route_reason') = 0,
  'ALTER TABLE service_task ADD COLUMN route_reason VARCHAR(255) NULL AFTER intent_code',
  'SELECT 1'
);
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @sql = IF(
  (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
   WHERE TABLE_SCHEMA = @schema_name AND TABLE_NAME = 'service_task' AND COLUMN_NAME = 'handoff_confidence') = 0,
  'ALTER TABLE service_task ADD COLUMN handoff_confidence DECIMAL(5,2) NULL AFTER route_reason',
  'SELECT 1'
);
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @sql = IF(
  (SELECT COUNT(*) FROM INFORMATION_SCHEMA.STATISTICS
   WHERE TABLE_SCHEMA = @schema_name AND TABLE_NAME = 'service_task' AND INDEX_NAME = 'idx_workflow_stage_status') = 0,
  'CREATE INDEX idx_workflow_stage_status ON service_task (workflow_code, current_stage_code, status)',
  'SELECT 1'
);
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- ------------------------------------------------------------
-- agent_workflow_template（坐席工作流模板）
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS agent_workflow_template (
  code           VARCHAR(100) NOT NULL PRIMARY KEY,
  name           VARCHAR(100) NOT NULL,
  description    VARCHAR(255),
  category_scope JSON,
  enabled        TINYINT(1)   NOT NULL DEFAULT 1,
  created_at     BIGINT       NOT NULL DEFAULT (UNIX_TIMESTAMP(NOW(3)) * 1000),
  updated_at     BIGINT       NOT NULL DEFAULT (UNIX_TIMESTAMP(NOW(3)) * 1000)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ------------------------------------------------------------
-- agent_workflow_stage（坐席工作流阶段）
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS agent_workflow_stage (
  id              BIGINT       NOT NULL AUTO_INCREMENT PRIMARY KEY,
  workflow_code   VARCHAR(100) NOT NULL,
  code            VARCHAR(50)  NOT NULL,
  name            VARCHAR(100) NOT NULL,
  stage_order     INT          NOT NULL,
  role_label      VARCHAR(100) NOT NULL,
  description     VARCHAR(255),
  allowed_actions JSON,
  created_at      BIGINT       NOT NULL DEFAULT (UNIX_TIMESTAMP(NOW(3)) * 1000),
  updated_at      BIGINT       NOT NULL DEFAULT (UNIX_TIMESTAMP(NOW(3)) * 1000),
  UNIQUE KEY uk_workflow_stage_code (workflow_code, code),
  UNIQUE KEY uk_workflow_stage_order (workflow_code, stage_order)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ------------------------------------------------------------
-- agent_workflow_route_rule（转人工路由规则）
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS agent_workflow_route_rule (
  id                  BIGINT       NOT NULL AUTO_INCREMENT PRIMARY KEY,
  intent_code         VARCHAR(100) NOT NULL,
  intent_name         VARCHAR(100) NOT NULL,
  target_workflow_code VARCHAR(100) NOT NULL,
  entry_stage_code    VARCHAR(50)  NOT NULL,
  priority_strategy   VARCHAR(50)  NOT NULL DEFAULT 'inherit',
  enabled             TINYINT(1)   NOT NULL DEFAULT 1,
  created_at          BIGINT       NOT NULL DEFAULT (UNIX_TIMESTAMP(NOW(3)) * 1000),
  updated_at          BIGINT       NOT NULL DEFAULT (UNIX_TIMESTAMP(NOW(3)) * 1000),
  UNIQUE KEY uk_intent_code (intent_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ------------------------------------------------------------
-- workbench_layout_config（工作台布局配置）
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS workbench_layout_config (
  code          VARCHAR(100) NOT NULL PRIMARY KEY,
  name          VARCHAR(100) NOT NULL,
  workflow_code VARCHAR(100) NOT NULL,
  stage_code    VARCHAR(50)  NOT NULL,
  regions       JSON         NOT NULL,
  enabled       TINYINT(1)   NOT NULL DEFAULT 1,
  created_at    BIGINT       NOT NULL DEFAULT (UNIX_TIMESTAMP(NOW(3)) * 1000),
  updated_at    BIGINT       NOT NULL DEFAULT (UNIX_TIMESTAMP(NOW(3)) * 1000),
  UNIQUE KEY uk_layout_workflow_stage (workflow_code, stage_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ------------------------------------------------------------
-- chat_message（聊天记录）
-- customer_id 为 session key，与 chatStore.ts 保持一致
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS chat_message (
  id           VARCHAR(100) NOT NULL PRIMARY KEY COMMENT '前端生成的 UUID',
  customer_id  VARCHAR(50)  NOT NULL,
  task_id      VARCHAR(50)  COMMENT '可选：关联服务任务',
  role         VARCHAR(20)  NOT NULL COMMENT 'user | bot | agent',
  content      TEXT         NOT NULL,
  message_type VARCHAR(20)  NOT NULL DEFAULT 'text',
  metadata     JSON,
  created_at   BIGINT       NOT NULL DEFAULT (UNIX_TIMESTAMP(NOW(3)) * 1000),
  INDEX idx_customer_id (customer_id),
  INDEX idx_task_id (task_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================================
-- Seed Data
-- ============================================================

INSERT IGNORE INTO agent (id, name, code, phone, branch) VALUES
  ('AGENT-001', '王建国', 'A001', '13800000001', '上海分公司');

INSERT IGNORE INTO customer (id, name, phone, tier, agent_id) VALUES
  ('CUST-882', '李明', '13912345678', '金卡', 'AGENT-001');

INSERT IGNORE INTO policy (id, customer_id, name, status, premium, start_date, end_date) VALUES
  ('POL-001', 'CUST-882', '宏康终身寿险', 'Active', 12000.00, '2022-01-01', '2052-01-01'),
  ('POL-002', 'CUST-882', '宏惠医疗险', 'Active',  3600.00, '2023-06-01', '2024-06-01');

INSERT IGNORE INTO service_task (id, customer_id, request_source, status, priority, category, summary, ai_suggestion, level, tags, created_at, updated_at) VALUES
  ('TASK-001', 'CUST-882', 'Customer', 'Pending', 'High', '核保咨询',
   '客户咨询重疾险核保流程及所需材料', '建议提供标准核保材料清单，告知预计时效',
   1, '["核保","重疾险"]',
   UNIX_TIMESTAMP(NOW(3)) * 1000, UNIX_TIMESTAMP(NOW(3)) * 1000),
  ('TASK-002', NULL, 'Agent', 'Pending', 'Medium', '渠道支持',
   '代理人反映系统报表导出异常', '建议排查报表权限配置，联系技术支持',
   2, '["系统问题","渠道"]',
   UNIX_TIMESTAMP(NOW(3)) * 1000, UNIX_TIMESTAMP(NOW(3)) * 1000);

UPDATE service_task
SET workflow_code = CASE
    WHEN category IN ('系统报障', '生产故障', '系统问题') THEN 'system-fault-flow'
    ELSE 'ops-service-flow'
  END,
  current_stage_code = CASE
    WHEN level = 1 THEN 'L1'
    WHEN level = 2 THEN 'L2'
    ELSE CONCAT('L', level)
  END,
  current_stage_order = level
WHERE workflow_code IS NULL OR workflow_code = '';

INSERT IGNORE INTO agent_workflow_template (code, name, description, category_scope, enabled) VALUES
  ('ops-service-flow', '运营坐席链路', '适用于保单服务、理赔咨询、核保咨询、投诉建议等人工服务场景', '["policy-service","claims-service","underwriting-service","complaint-service","channel-support","general-service"]', 1),
  ('system-fault-flow', '系统报障坐席链路', '适用于系统报障、接口异常、登录故障等系统问题场景', '["system-fault"]', 1);

INSERT IGNORE INTO agent_workflow_stage (workflow_code, code, name, stage_order, role_label, description, allowed_actions) VALUES
  ('ops-service-flow', 'L1', '一线坐席', 1, '一线坐席', '负责受理常规运营咨询与客户服务请求', '["grab","advance","complete","suspend","resume"]'),
  ('ops-service-flow', 'L2', '二线专家', 2, '二线专家', '负责复杂案件升级与疑难问题处理', '["grab","complete","suspend","resume"]'),
  ('system-fault-flow', 'L1', '一线受理', 1, '一线受理', '负责初步受理故障与信息收集', '["grab","advance","complete","suspend","resume"]'),
  ('system-fault-flow', 'L2', '二线定位', 2, '二线定位', '负责故障复现、定位与排查', '["grab","advance","complete","suspend","resume"]'),
  ('system-fault-flow', 'L3', '三线技术支持', 3, '三线技术支持', '负责复杂系统问题与技术支持处理', '["grab","complete","suspend","resume"]');

INSERT IGNORE INTO agent_workflow_route_rule (intent_code, intent_name, target_workflow_code, entry_stage_code, priority_strategy, enabled) VALUES
  ('policy-service', '保单服务', 'ops-service-flow', 'L1', 'inherit', 1),
  ('claims-service', '理赔服务', 'ops-service-flow', 'L1', 'inherit', 1),
  ('underwriting-service', '核保服务', 'ops-service-flow', 'L1', 'inherit', 1),
  ('complaint-service', '投诉建议', 'ops-service-flow', 'L1', 'force-high', 1),
  ('channel-support', '渠道支持', 'ops-service-flow', 'L1', 'inherit', 1),
  ('system-fault', '系统报障', 'system-fault-flow', 'L1', 'force-urgent', 1),
  ('general-service', '通用服务', 'ops-service-flow', 'L1', 'inherit', 1);

INSERT IGNORE INTO workbench_layout_config (code, name, workflow_code, stage_code, regions, enabled) VALUES
  (
    'ops-service-flow-L1',
    '运营链路一线工作台',
    'ops-service-flow',
    'L1',
    '[
      {"code":"left","width":"280px","widgets":[
        {"code":"task-pool","type":"task-pool","title":"任务公共池","visible":true,"order":1},
        {"code":"my-task-list","type":"my-task-list","title":"个人工作池","visible":true,"order":2}
      ]},
      {"code":"center","widgets":[
        {"code":"chat-panel","type":"chat-panel","title":"会话记录","visible":true,"order":1}
      ]},
      {"code":"right","width":"380px","widgets":[
        {"code":"customer-profile","type":"customer-profile","title":"客户画像","visible":true,"order":1},
        {"code":"policy-list","type":"policy-list","title":"保单信息","visible":true,"order":2},
        {"code":"ai-copilot","type":"ai-copilot","title":"AI Copilot","visible":true,"order":3}
      ]}
    ]',
    1
  ),
  (
    'ops-service-flow-L2',
    '运营链路二线工作台',
    'ops-service-flow',
    'L2',
    '[
      {"code":"left","width":"280px","widgets":[
        {"code":"task-pool","type":"task-pool","title":"待处理升级任务","visible":true,"order":1},
        {"code":"my-task-list","type":"my-task-list","title":"个人工作池","visible":true,"order":2},
        {"code":"stage-timeline","type":"stage-timeline","title":"阶段轨迹","visible":true,"order":3}
      ]},
      {"code":"center","widgets":[
        {"code":"chat-panel","type":"chat-panel","title":"会话记录","visible":true,"order":1}
      ]},
      {"code":"right","width":"420px","widgets":[
        {"code":"customer-profile","type":"customer-profile","title":"客户画像","visible":true,"order":1},
        {"code":"history-records","type":"history-records","title":"历史记录","visible":true,"order":2},
        {"code":"ai-copilot","type":"ai-copilot","title":"AI Copilot","visible":true,"order":3}
      ]}
    ]',
    1
  ),
  (
    'system-fault-flow-L1',
    '系统报障一线工作台',
    'system-fault-flow',
    'L1',
    '[
      {"code":"left","width":"280px","widgets":[
        {"code":"task-pool","type":"task-pool","title":"报障受理池","visible":true,"order":1},
        {"code":"my-task-list","type":"my-task-list","title":"个人工作池","visible":true,"order":2}
      ]},
      {"code":"center","widgets":[
        {"code":"chat-panel","type":"chat-panel","title":"会话记录","visible":true,"order":1}
      ]},
      {"code":"right","width":"420px","widgets":[
        {"code":"fault-report-entry","type":"fault-report-entry","title":"报障入口","visible":true,"order":1},
        {"code":"history-records","type":"history-records","title":"历史记录","visible":true,"order":2},
        {"code":"ai-copilot","type":"ai-copilot","title":"AI Copilot","visible":true,"order":3}
      ]}
    ]',
    1
  ),
  (
    'system-fault-flow-L2',
    '系统报障二线工作台',
    'system-fault-flow',
    'L2',
    '[
      {"code":"left","width":"280px","widgets":[
        {"code":"my-task-list","type":"my-task-list","title":"待定位工单","visible":true,"order":1},
        {"code":"stage-timeline","type":"stage-timeline","title":"阶段轨迹","visible":true,"order":2}
      ]},
      {"code":"center","widgets":[
        {"code":"chat-panel","type":"chat-panel","title":"会话记录","visible":true,"order":1},
        {"code":"knowledge-card","type":"knowledge-card","title":"排障知识","visible":true,"order":2}
      ]},
      {"code":"right","width":"420px","widgets":[
        {"code":"history-records","type":"history-records","title":"历史记录","visible":true,"order":1},
        {"code":"ai-copilot","type":"ai-copilot","title":"AI Copilot","visible":true,"order":2}
      ]}
    ]',
    1
  ),
  (
    'system-fault-flow-L3',
    '系统报障三线工作台',
    'system-fault-flow',
    'L3',
    '[
      {"code":"left","width":"280px","widgets":[
        {"code":"my-task-list","type":"my-task-list","title":"技术支持任务","visible":true,"order":1},
        {"code":"stage-timeline","type":"stage-timeline","title":"阶段轨迹","visible":true,"order":2}
      ]},
      {"code":"center","widgets":[
        {"code":"chat-panel","type":"chat-panel","title":"会话记录","visible":true,"order":1},
        {"code":"knowledge-card","type":"knowledge-card","title":"排障知识","visible":true,"order":2}
      ]},
      {"code":"right","width":"420px","widgets":[
        {"code":"fault-report-entry","type":"fault-report-entry","title":"故障上下文","visible":true,"order":1},
        {"code":"history-records","type":"history-records","title":"历史记录","visible":true,"order":2},
        {"code":"ai-copilot","type":"ai-copilot","title":"AI Copilot","visible":true,"order":3}
      ]}
    ]',
    1
  );
