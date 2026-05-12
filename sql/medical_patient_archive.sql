/* 患者归档增量脚本：表字段 + 菜单权限
   编码 UTF-8 utf8mb4；执行时请加客户端参数 default-character-set=utf8mb4
   依赖：已执行 medical_patient_ry_menu_zh.sql（患者管理 parent_id=3200） */
USE `ry-cloud`;

-- 扩展 medical_patient（执行前请确认列尚未添加，否则会报错）
ALTER TABLE medical_patient
    ADD COLUMN is_archived TINYINT NOT NULL DEFAULT 0 COMMENT '0在办1已归档' AFTER remark,
    ADD COLUMN archive_time DATETIME NULL DEFAULT NULL COMMENT '归档时间' AFTER is_archived,
    ADD COLUMN archive_by VARCHAR(64) NULL DEFAULT NULL COMMENT '归档操作人' AFTER archive_time,
    ADD COLUMN archive_remark VARCHAR(500) NULL DEFAULT NULL COMMENT '归档备注' AFTER archive_by;

-- 菜单（3296 至 3298，与融合报告菜单 3291 至 3295 不冲突）
DELETE FROM sys_role_menu WHERE menu_id BETWEEN 3296 AND 3298;
DELETE FROM sys_menu WHERE menu_id BETWEEN 3296 AND 3298;

INSERT INTO sys_menu VALUES (3296, '患者归档', 3200, 5, 'patient-archive', 'medical/patient-archive/index', '', '', 1, 0, 'C', '0', '0', 'medical:patient:archiveList', 'lock', 'admin', sysdate(), '', NULL, '已归档患者查询与取消归档');
INSERT INTO sys_menu VALUES (3297, '执行归档', 3296, 1, '', '', '', '', 1, 0, 'F', '0', '0', 'medical:patient:archive', '#', 'admin', sysdate(), '', NULL, '');
INSERT INTO sys_menu VALUES (3298, '取消归档', 3296, 2, '', '', '', '', 1, 0, 'F', '0', '0', 'medical:patient:unarchive', '#', 'admin', sysdate(), '', NULL, '');

-- 管理员权限
INSERT INTO sys_role_menu VALUES (2, 3296);
INSERT INTO sys_role_menu VALUES (2, 3297);
INSERT INTO sys_role_menu VALUES (2, 3298);

-- 医生权限
INSERT INTO sys_role_menu VALUES (100, 3296);
INSERT INTO sys_role_menu VALUES (100, 3297);
INSERT INTO sys_role_menu VALUES (100, 3298);