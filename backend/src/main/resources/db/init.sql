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
