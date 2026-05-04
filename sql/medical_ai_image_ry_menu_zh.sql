USE `ry-cloud`;

DELETE FROM sys_role_menu WHERE menu_id BETWEEN 3240 AND 3245;
DELETE FROM sys_menu WHERE menu_id BETWEEN 3240 AND 3245;

INSERT INTO sys_menu VALUES (3240, 'AI影像分析', 0, 9, 'ai-image', NULL, '', '', 1, 0, 'M', '0', '0', '', 'eye-open', 'admin', sysdate(), '', NULL, 'AI影像分析目录');
INSERT INTO sys_menu VALUES (3241, 'AI分析列表', 3240, 1, 'analysis', 'medical/ai-image/index', '', '', 1, 0, 'C', '0', '0', 'ai:image:list', 'monitor', 'admin', sysdate(), '', NULL, 'AI影像分析列表');
INSERT INTO sys_menu VALUES (3242, 'AI分析', 3241, 1, '', '', '', '', 1, 0, 'F', '0', '0', 'ai:image:analyze', '#', 'admin', sysdate(), '', NULL, '');
INSERT INTO sys_menu VALUES (3243, '查看双图', 3241, 2, '', '', '', '', 1, 0, 'F', '0', '0', 'ai:image:view', '#', 'admin', sysdate(), '', NULL, '');
INSERT INTO sys_menu VALUES (3244, '查看病历', 3241, 3, '', '', '', '', 1, 0, 'F', '0', '0', 'ai:image:record', '#', 'admin', sysdate(), '', NULL, '');

-- 管理员权限
INSERT INTO sys_role_menu VALUES (2, 3240);
INSERT INTO sys_role_menu VALUES (2, 3241);
INSERT INTO sys_role_menu VALUES (2, 3242);
INSERT INTO sys_role_menu VALUES (2, 3243);
INSERT INTO sys_role_menu VALUES (2, 3244);

-- 医生权限
INSERT INTO sys_role_menu VALUES (100, 3240);
INSERT INTO sys_role_menu VALUES (100, 3241);
INSERT INTO sys_role_menu VALUES (100, 3242);
INSERT INTO sys_role_menu VALUES (100, 3243);
INSERT INTO sys_role_menu VALUES (100, 3244);
