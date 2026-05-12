-- Fusion analysis report (medical_db). UTF-8.
SET NAMES utf8mb4 COLLATE utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS fusion_analysis_report (
    report_id BIGINT AUTO_INCREMENT COMMENT '报告ID' PRIMARY KEY,
    patient_id BIGINT NOT NULL COMMENT '患者ID',
    image_id BIGINT NOT NULL COMMENT '胸部影像 chest_xray.id',
    medical_record_id BIGINT NULL COMMENT '关联病历 medical_record.record_id',
    lab_result_id BIGINT NULL COMMENT '关联检验 medical_lab_results.id',
    fusion_response_json LONGTEXT NOT NULL COMMENT '融合服务完整返回 JSON',
    image_result_json LONGTEXT NOT NULL COMMENT '影像AI入参快照 JSON',
    case_text LONGTEXT NOT NULL COMMENT '病历文本入参快照',
    lab_data_json LONGTEXT NULL COMMENT '检验入参快照 JSON',
    doctor_signature VARCHAR(128) NULL COMMENT '医生签名',
    doctor_advice TEXT NULL COMMENT '医生建议',
    create_by VARCHAR(64) DEFAULT '' NULL,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP NULL,
    update_by VARCHAR(64) DEFAULT '' NULL,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP NULL,
    remark VARCHAR(500) NULL,
    is_deleted TINYINT DEFAULT 0 NOT NULL,
    UNIQUE KEY uk_patient_image (patient_id, image_id),
    INDEX idx_patient_id (patient_id),
    INDEX idx_image_id (image_id),
    INDEX idx_create_time (create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='多模态融合分析报告';
