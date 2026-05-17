-- AI 病灶分析结果持久化（绑定 chest_xray，每次分析新增一条）
-- UTF-8 utf8mb4；请用 mysql --default-character-set=utf8mb4 执行。
SET NAMES utf8mb4 COLLATE utf8mb4_unicode_ci;

USE medical_db;

CREATE TABLE IF NOT EXISTS ai_image_analysis_result (
    analysis_id BIGINT AUTO_INCREMENT COMMENT '分析记录ID' PRIMARY KEY,
    image_id BIGINT NOT NULL COMMENT '胸片ID chest_xray.id',
    patient_id BIGINT NULL COMMENT '患者ID',
    lesion_count INT DEFAULT 0 NULL COMMENT '病灶数量',
    infection_rate DECIMAL(8, 2) NULL COMMENT '感染率(%)',
    total_infection_area DECIMAL(16, 2) NULL COMMENT '感染区域面积(px²)',
    detect_diagnosis VARCHAR(512) NULL COMMENT 'YOLO检测摘要 diagnosis',
    ai_result_path VARCHAR(512) NULL COMMENT 'AI标注图MinIO路径',
    detect_result_json LONGTEXT NOT NULL COMMENT 'YOLO检测固定结构JSON',
    diagnosis_report_raw LONGTEXT NULL COMMENT '报告原文 diagnosis_report',
    diagnosis_report_json LONGTEXT NULL COMMENT '报告结构化JSON',
    report_create_time VARCHAR(32) NULL COMMENT '报告生成时间',
    create_by VARCHAR(64) DEFAULT '' NULL,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP NULL,
    is_deleted TINYINT DEFAULT 0 NOT NULL,
    INDEX idx_image_id (image_id),
    INDEX idx_patient_id (patient_id),
    INDEX idx_create_time (create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='AI病灶分析结果';
