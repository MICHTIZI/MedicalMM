-- =============================================================================
-- 文件编码: UTF-8 | 数据库字符集: utf8mb4 全部 DEFAULT CHARSET=utf8mb4 一致
-- medical_db: 电子病历模块扩展 + 模板表 包括 emr_record、emr_entity 修改
-- 本权限请执行 medical_emr_ry_menu.sql 业务 DDL 不要重复执行
-- =============================================================================

SET NAMES utf8mb4 COLLATE utf8mb4_unicode_ci;
USE medical_db;

-- 扩展电子病历表字段
ALTER TABLE emr_record ADD COLUMN title VARCHAR(200) DEFAULT '' COMMENT '病历标题' AFTER id;
ALTER TABLE emr_record ADD COLUMN structured_json LONGTEXT COMMENT '结构化信息JSON' AFTER content;
ALTER TABLE emr_record ADD COLUMN archive_status CHAR(1) DEFAULT '0' COMMENT '归档状态 0未归档 1已归档' AFTER structured_json;
ALTER TABLE emr_record ADD COLUMN archived_time DATETIME DEFAULT NULL COMMENT '归档时间' AFTER archive_status;
ALTER TABLE emr_record ADD COLUMN disease VARCHAR(200) DEFAULT '' COMMENT '诊断/疾病关键词' AFTER archived_time;
ALTER TABLE emr_record ADD COLUMN template_id BIGINT DEFAULT NULL COMMENT '来源模板ID' AFTER disease;
ALTER TABLE emr_record ADD COLUMN record_type VARCHAR(32) DEFAULT '' COMMENT '病历类型 admission/discharge/surgery/course' AFTER template_id;

-- 创建查询索引
CREATE INDEX idx_emr_record_create_time ON emr_record (create_time);
CREATE INDEX idx_emr_record_archive ON emr_record (archive_status, create_time);
CREATE INDEX idx_emr_record_disease ON emr_record (disease);
CREATE INDEX idx_emr_record_type ON emr_record (record_type);

CREATE INDEX idx_emr_entity_record ON emr_entity (record_id);
CREATE INDEX idx_emr_entity_label ON emr_entity (label_type);
CREATE INDEX idx_emr_entity_text ON emr_entity (entity_text(100));

-- 电子病历模板表
CREATE TABLE IF NOT EXISTS emr_template (
                                            id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
                                            template_name VARCHAR(100) NOT NULL COMMENT '模板名称',
                                            template_type VARCHAR(32) NOT NULL COMMENT '模板类型 admission/discharge/surgery/course',
                                            template_content LONGTEXT NOT NULL COMMENT '模板内容',
                                            remark VARCHAR(500) DEFAULT NULL COMMENT '备注',
                                            create_time DATETIME DEFAULT NULL COMMENT '创建时间',
                                            update_time DATETIME DEFAULT NULL COMMENT '更新时间',
                                            PRIMARY KEY (id),
                                            KEY idx_emr_template_type (template_type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='电子病历模板表';
