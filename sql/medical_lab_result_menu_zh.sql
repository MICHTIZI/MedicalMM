-- 检验指标菜单（UTF-8，建议客户端连接字符集 utf8mb4）
USE `ry-cloud`;

DELETE FROM sys_role_menu WHERE menu_id BETWEEN 3280 AND 3285;
DELETE FROM sys_menu WHERE menu_id BETWEEN 3280 AND 3285;

INSERT INTO sys_menu VALUES (3280, '检验指标', 3200, 3, 'lab', 'medical/lab-result/index', '', '', 1, 0, 'C', '0', '0', 'medical:lab:list', 'skill', 'admin', sysdate(), '', NULL, '患者检验指标');

INSERT INTO sys_menu VALUES (3281, '检验查询', 3280, 1, '', '', '', '', 1, 0, 'F', '0', '0', 'medical:lab:query', '#', 'admin', sysdate(), '', NULL, '');
INSERT INTO sys_menu VALUES (3282, '检验新增', 3280, 2, '', '', '', '', 1, 0, 'F', '0', '0', 'medical:lab:add', '#', 'admin', sysdate(), '', NULL, '');
INSERT INTO sys_menu VALUES (3283, '检验修改', 3280, 3, '', '', '', '', 1, 0, 'F', '0', '0', 'medical:lab:edit', '#', 'admin', sysdate(), '', NULL, '');
INSERT INTO sys_menu VALUES (3284, '检验删除', 3280, 4, '', '', '', '', 1, 0, 'F', '0', '0', 'medical:lab:remove', '#', 'admin', sysdate(), '', NULL, '');
INSERT INTO sys_menu VALUES (3285, '检验导入TXT', 3280, 5, '', '', '', '', 1, 0, 'F', '0', '0', 'medical:lab:import', '#', 'admin', sysdate(), '', NULL, '');

-- 管理员权限
INSERT INTO sys_role_menu VALUES (2, 3280);
INSERT INTO sys_role_menu VALUES (2, 3281);
INSERT INTO sys_role_menu VALUES (2, 3282);
INSERT INTO sys_role_menu VALUES (2, 3283);
INSERT INTO sys_role_menu VALUES (2, 3284);
INSERT INTO sys_role_menu VALUES (2, 3285);

-- 医生权限
INSERT INTO sys_role_menu VALUES (100, 3280);
INSERT INTO sys_role_menu VALUES (100, 3281);
INSERT INTO sys_role_menu VALUES (100, 3282);
INSERT INTO sys_role_menu VALUES (100, 3283);
INSERT INTO sys_role_menu VALUES (100, 3284);
INSERT INTO sys_role_menu VALUES (100, 3285);