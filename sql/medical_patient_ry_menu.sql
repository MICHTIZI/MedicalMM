-- =============================================================================
-- 患者管理中文菜单与权限
-- 请在 RuoYi 主库执行；医生角色 role_id=100，普通管理员角色 role_id=2
-- 如果已执行英文菜单版本，请先执行 medical_patient_ry_menu_rollback.sql
-- =============================================================================

SET NAMES utf8mb4 COLLATE utf8mb4_unicode_ci;
USE `ry-cloud`;

INSERT INTO sys_menu VALUES (3200, '患者管理', 0, 8, 'patient', NULL, '', '', 1, 0, 'M', '0', '0', '', 'peoples', 'admin', sysdate(), '', NULL, '患者管理目录');
INSERT INTO sys_menu VALUES (3201, '患者列表', 3200, 1, 'list', 'medical/patient/index', '', '', 1, 0, 'C', '0', '0', 'medical:patient:list', 'user', 'admin', sysdate(), '', NULL, '患者列表');

INSERT INTO sys_menu VALUES (3210, '患者查询', 3201, 1, '', '', '', '', 1, 0, 'F', '0', '0', 'medical:patient:query', '#', 'admin', sysdate(), '', NULL, '');
INSERT INTO sys_menu VALUES (3211, '患者新增', 3201, 2, '', '', '', '', 1, 0, 'F', '0', '0', 'medical:patient:add', '#', 'admin', sysdate(), '', NULL, '');
INSERT INTO sys_menu VALUES (3212, '患者修改', 3201, 3, '', '', '', '', 1, 0, 'F', '0', '0', 'medical:patient:edit', '#', 'admin', sysdate(), '', NULL, '');
INSERT INTO sys_menu VALUES (3213, '患者删除', 3201, 4, '', '', '', '', 1, 0, 'F', '0', '0', 'medical:patient:remove', '#', 'admin', sysdate(), '', NULL, '');
INSERT INTO sys_menu VALUES (3214, '分配医生', 3201, 5, '', '', '', '', 1, 0, 'F', '0', '0', 'medical:patient:assign', '#', 'admin', sysdate(), '', NULL, '');
INSERT INTO sys_menu VALUES (3215, '患者导出', 3201, 6, '', '', '', '', 1, 0, 'F', '0', '0', 'medical:patient:export', '#', 'admin', sysdate(), '', NULL, '');

INSERT INTO sys_role_menu VALUES (2, 3200);
INSERT INTO sys_role_menu VALUES (2, 3201);
INSERT INTO sys_role_menu VALUES (2, 3210);
INSERT INTO sys_role_menu VALUES (2, 3211);
INSERT INTO sys_role_menu VALUES (2, 3212);
INSERT INTO sys_role_menu VALUES (2, 3213);
INSERT INTO sys_role_menu VALUES (2, 3214);
INSERT INTO sys_role_menu VALUES (2, 3215);

INSERT INTO sys_role_menu VALUES (100, 3200);
INSERT INTO sys_role_menu VALUES (100, 3201);
INSERT INTO sys_role_menu VALUES (100, 3210);
INSERT INTO sys_role_menu VALUES (100, 3211);
INSERT INTO sys_role_menu VALUES (100, 3212);
INSERT INTO sys_role_menu VALUES (100, 3213);
INSERT INTO sys_role_menu VALUES (100, 3215);
-- =============================================================================
-- Patient management menu and permissions.
-- Run in the RuoYi main database. Doctor role_id=100, common admin role_id=2.
-- =============================================================================

SET NAMES utf8mb4 COLLATE utf8mb4_unicode_ci;
USE `ry-cloud`;

INSERT INTO sys_menu VALUES (3200, 'Patient Management', 0, 8, 'patient', NULL, '', '', 1, 0, 'M', '0', '0', '', 'peoples', 'admin', sysdate(), '', NULL, 'Patient management directory');
INSERT INTO sys_menu VALUES (3201, 'Patient List', 3200, 1, 'list', 'medical/patient/index', '', '', 1, 0, 'C', '0', '0', 'medical:patient:list', 'user', 'admin', sysdate(), '', NULL, 'Patient list');

INSERT INTO sys_menu VALUES (3210, 'Patient Query', 3201, 1, '', '', '', '', 1, 0, 'F', '0', '0', 'medical:patient:query', '#', 'admin', sysdate(), '', NULL, '');
INSERT INTO sys_menu VALUES (3211, 'Patient Add', 3201, 2, '', '', '', '', 1, 0, 'F', '0', '0', 'medical:patient:add', '#', 'admin', sysdate(), '', NULL, '');
INSERT INTO sys_menu VALUES (3212, 'Patient Edit', 3201, 3, '', '', '', '', 1, 0, 'F', '0', '0', 'medical:patient:edit', '#', 'admin', sysdate(), '', NULL, '');
INSERT INTO sys_menu VALUES (3213, 'Patient Delete', 3201, 4, '', '', '', '', 1, 0, 'F', '0', '0', 'medical:patient:remove', '#', 'admin', sysdate(), '', NULL, '');
INSERT INTO sys_menu VALUES (3214, 'Assign Doctor', 3201, 5, '', '', '', '', 1, 0, 'F', '0', '0', 'medical:patient:assign', '#', 'admin', sysdate(), '', NULL, '');
INSERT INTO sys_menu VALUES (3215, 'Patient Export', 3201, 6, '', '', '', '', 1, 0, 'F', '0', '0', 'medical:patient:export', '#', 'admin', sysdate(), '', NULL, '');

INSERT INTO sys_role_menu VALUES (2, 3200);
INSERT INTO sys_role_menu VALUES (2, 3201);
INSERT INTO sys_role_menu VALUES (2, 3210);
INSERT INTO sys_role_menu VALUES (2, 3211);
INSERT INTO sys_role_menu VALUES (2, 3212);
INSERT INTO sys_role_menu VALUES (2, 3213);
INSERT INTO sys_role_menu VALUES (2, 3214);
INSERT INTO sys_role_menu VALUES (2, 3215);

INSERT INTO sys_role_menu VALUES (100, 3200);
INSERT INTO sys_role_menu VALUES (100, 3201);
INSERT INTO sys_role_menu VALUES (100, 3210);
INSERT INTO sys_role_menu VALUES (100, 3211);
INSERT INTO sys_role_menu VALUES (100, 3212);
INSERT INTO sys_role_menu VALUES (100, 3213);
INSERT INTO sys_role_menu VALUES (100, 3215);
