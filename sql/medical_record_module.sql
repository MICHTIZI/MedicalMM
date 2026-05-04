-- =============================================================================
-- 结构化病历管理业务表：medical_record
-- 依赖：medical_patient、chest_xray
-- =============================================================================

SET NAMES utf8mb4 COLLATE utf8mb4_unicode_ci;
USE medical_db;

CREATE TABLE IF NOT EXISTS medical_record (
    record_id BIGINT NOT NULL AUTO_INCREMENT COMMENT '病历ID',
    patient_id BIGINT NOT NULL COMMENT '患者ID，关联 medical_patient.patient_id',
    chief_complaint TEXT COMMENT '主诉',
    present_history TEXT COMMENT '现病史',
    past_history TEXT COMMENT '既往史',
    physical_exam TEXT COMMENT '体格检查',
    initial_diagnosis TEXT COMMENT '初步诊断',
    operate_doctor_id BIGINT NOT NULL COMMENT '操作医生ID，关联 sys_user.user_id',
    operate_doctor VARCHAR(30) NOT NULL COMMENT '操作医生姓名，来源 sys_user.nick_name',
    image_id BIGINT DEFAULT NULL COMMENT '胸片ID，关联 chest_xray.id',
    image_path VARCHAR(500) DEFAULT NULL COMMENT '胸片MinIO对象路径或文件名',
    ai_result_path VARCHAR(500) DEFAULT NULL COMMENT 'AI结果MinIO对象路径或文件名',
    create_by VARCHAR(64) DEFAULT '' COMMENT '创建者',
    create_time DATETIME DEFAULT NULL COMMENT '创建时间',
    update_by VARCHAR(64) DEFAULT '' COMMENT '更新者',
    update_time DATETIME DEFAULT NULL COMMENT '更新时间',
    remark VARCHAR(500) DEFAULT NULL COMMENT '备注',
    PRIMARY KEY (record_id),
    KEY idx_medical_record_patient (patient_id),
    KEY idx_medical_record_doctor (operate_doctor_id),
    UNIQUE KEY uk_medical_record_image (image_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='结构化病历表';

