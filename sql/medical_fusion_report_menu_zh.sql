-- 融合分析报告菜单：挂在「患者管理」(menu_id=3200) 下。
-- UTF-8 utf8mb4；请用 mysql --default-character-set=utf8mb4 执行。
-- 依赖：已执行 medical_patient_ry_menu_zh.sql 。
USE `ry-cloud`;

DELETE FROM sys_role_menu WHERE menu_id BETWEEN 3291 AND 3295;
DELETE FROM sys_menu WHERE menu_id BETWEEN 3291 AND 3295;

INSERT INTO sys_menu VALUES (3291, '融合分析报告', 3200, 4, 'fusion-report', 'medical/fusion-report/index', '', '', 1, 0, 'C', '0', '0', 'fusion:report:list', 'documentation', 'admin', sysdate(), '', NULL, '多模态融合分析报告管理');
INSERT INTO sys_menu VALUES (3292, '报告查询', 3291, 1, '', '', '', '', 1, 0, 'F', '0', '0', 'fusion:report:query', '#', 'admin', sysdate(), '', NULL, '');
INSERT INTO sys_menu VALUES (3293, '报告新增', 3291, 2, '', '', '', '', 1, 0, 'F', '0', '0', 'fusion:report:add', '#', 'admin', sysdate(), '', NULL, '');
INSERT INTO sys_menu VALUES (3294, '报告修改', 3291, 3, '', '', '', '', 1, 0, 'F', '0', '0', 'fusion:report:edit', '#', 'admin', sysdate(), '', NULL, '');
INSERT INTO sys_menu VALUES (3295, '报告删除', 3291, 4, '', '', '', '', 1, 0, 'F', '0', '0', 'fusion:report:remove', '#', 'admin', sysdate(), '', NULL, '');

-- Admin
INSERT INTO sys_role_menu VALUES (2, 3291);
INSERT INTO sys_role_menu VALUES (2, 3292);
INSERT INTO sys_role_menu VALUES (2, 3293);
INSERT INTO sys_role_menu VALUES (2, 3294);
INSERT INTO sys_role_menu VALUES (2, 3295);

-- Doctor
INSERT INTO sys_role_menu VALUES (100, 3291);
INSERT INTO sys_role_menu VALUES (100, 3292);
INSERT INTO sys_role_menu VALUES (100, 3293);
INSERT INTO sys_role_menu VALUES (100, 3294);
INSERT INTO sys_role_menu VALUES (100, 3295);
