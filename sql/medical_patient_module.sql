-- =============================================================================
-- Patient management business table: medical_patient
-- Doctors are sys_user records. No standalone doctor table is created.
-- medical_patient.attending_doctor_id references sys_user.user_id.
-- =============================================================================

SET NAMES utf8mb4 COLLATE utf8mb4_unicode_ci;
USE medical_db;

CREATE TABLE IF NOT EXISTS medical_patient (
    patient_id BIGINT NOT NULL AUTO_INCREMENT COMMENT 'Patient ID',
    patient_name VARCHAR(50) NOT NULL COMMENT 'Patient name',
    gender CHAR(1) DEFAULT '2' COMMENT 'Gender: 0 male, 1 female, 2 unknown',
    age INT DEFAULT NULL COMMENT 'Age',
    phone VARCHAR(20) DEFAULT NULL COMMENT 'Phone',
    address VARCHAR(255) DEFAULT NULL COMMENT 'Address',
    attending_doctor_id BIGINT NOT NULL COMMENT 'Attending doctor ID, references sys_user.user_id',
    attending_doctor VARCHAR(30) NOT NULL COMMENT 'Attending doctor name, copied from sys_user.nick_name',
    create_by VARCHAR(64) DEFAULT '' COMMENT 'Created by',
    create_time DATETIME DEFAULT NULL COMMENT 'Created time',
    update_by VARCHAR(64) DEFAULT '' COMMENT 'Updated by',
    update_time DATETIME DEFAULT NULL COMMENT 'Updated time',
    remark VARCHAR(500) DEFAULT NULL COMMENT 'Remark',
    PRIMARY KEY (patient_id),
    KEY idx_medical_patient_doctor (attending_doctor_id),
    KEY idx_medical_patient_name (patient_name),
    KEY idx_medical_patient_phone (phone)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Medical patient table';
