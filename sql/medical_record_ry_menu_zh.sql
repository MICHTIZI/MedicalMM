USE `ry-cloud`;

DELETE FROM sys_role_menu WHERE menu_id BETWEEN 3220 AND 3235;
DELETE FROM sys_menu WHERE menu_id BETWEEN 3220 AND 3235;

INSERT INTO sys_menu VALUES (3220, '结构化病历', 3200, 2, 'record', 'medical/record/index', '', '', 1, 0, 'C', '0', '0', 'medical:record:list', 'form', 'admin', sysdate(), '', NULL, '结构化病历列表');

INSERT INTO sys_menu VALUES (3230, '病历查询', 3220, 1, '', '', '', '', 1, 0, 'F', '0', '0', 'medical:record:query', '#', 'admin', sysdate(), '', NULL, '');
INSERT INTO sys_menu VALUES (3231, '病历新增', 3220, 2, '', '', '', '', 1, 0, 'F', '0', '0', 'medical:record:add', '#', 'admin', sysdate(), '', NULL, '');
INSERT INTO sys_menu VALUES (3232, '病历修改', 3220, 3, '', '', '', '', 1, 0, 'F', '0', '0', 'medical:record:edit', '#', 'admin', sysdate(), '', NULL, '');
INSERT INTO sys_menu VALUES (3233, '病历删除', 3220, 4, '', '', '', '', 1, 0, 'F', '0', '0', 'medical:record:remove', '#', 'admin', sysdate(), '', NULL, '');
INSERT INTO sys_menu VALUES (3234, '胸片选择', 3220, 5, '', '', '', '', 1, 0, 'F', '0', '0', 'medical:record:image', '#', 'admin', sysdate(), '', NULL, '');
INSERT INTO sys_menu VALUES (3235, '病历导出', 3220, 6, '', '', '', '', 1, 0, 'F', '0', '0', 'medical:record:export', '#', 'admin', sysdate(), '', NULL, '');

-- 管理员权限
INSERT INTO sys_role_menu VALUES (2, 3220);
INSERT INTO sys_role_menu VALUES (2, 3230);
INSERT INTO sys_role_menu VALUES (2, 3231);
INSERT INTO sys_role_menu VALUES (2, 3232);
INSERT INTO sys_role_menu VALUES (2, 3233);
INSERT INTO sys_role_menu VALUES (2, 3234);
INSERT INTO sys_role_menu VALUES (2, 3235);

-- 医生权限
INSERT INTO sys_role_menu VALUES (100, 3220);
INSERT INTO sys_role_menu VALUES (100, 3230);
INSERT INTO sys_role_menu VALUES (100, 3231);
INSERT INTO sys_role_menu VALUES (100, 3232);
INSERT INTO sys_role_menu VALUES (100, 3233);
INSERT INTO sys_role_menu VALUES (100, 3234);
INSERT INTO sys_role_menu VALUES (100, 3235);
