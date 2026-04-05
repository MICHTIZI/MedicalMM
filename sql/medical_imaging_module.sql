-- =============================================================================
-- 文件编码: UTF-8 | 客户端连接字符集: utf8mb4
-- medical_db: 影像数据管理模块 DDL（chest_xray 表已存在，仅建辅助表与索引）
-- =============================================================================

SET NAMES utf8mb4 COLLATE utf8mb4_unicode_ci;
USE medical_db;

-- chest_xray 查询索引（若已存在可忽略报错后手工跳过）
CREATE INDEX idx_chest_xray_diseases ON chest_xray (diseases(100));
CREATE INDEX idx_chest_xray_name ON chest_xray (image_name(100));
CREATE INDEX idx_chest_xray_time ON chest_xray (create_time);

-- 影像序列表（同一患者/检查的影像分组）
CREATE TABLE IF NOT EXISTS imaging_sequence (
  id           BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
  seq_name     VARCHAR(200) NOT NULL COMMENT '序列名称',
  patient_info VARCHAR(200) DEFAULT '' COMMENT '患者信息',
  description  VARCHAR(500) DEFAULT '' COMMENT '检查描述',
  archive_status CHAR(1)    DEFAULT '0' COMMENT '归档状态 0=正常 1=已归档',
  sort_order   INT          DEFAULT 0 COMMENT '排序号',
  create_time  DATETIME     DEFAULT NULL COMMENT '创建时间',
  update_time  DATETIME     DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (id),
  KEY idx_seq_archive (archive_status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='影像序列表';

-- 序列-影像关联表
CREATE TABLE IF NOT EXISTS sequence_image (
  id         BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
  seq_id     BIGINT NOT NULL COMMENT '序列ID',
  image_id   BIGINT NOT NULL COMMENT '影像ID(chest_xray.id)',
  sort_order INT    DEFAULT 0 COMMENT '序列内排序',
  PRIMARY KEY (id),
  KEY idx_si_seq (seq_id),
  KEY idx_si_img (image_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='序列-影像关联表';
