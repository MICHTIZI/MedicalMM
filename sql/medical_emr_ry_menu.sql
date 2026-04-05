-- 文件编码: UTF-8 | 数据库字符集: utf8mb4 菜单名称全部正常中文
-- 业务模块菜单，同 sys_menu 结构，请把 USE 后的库名修改为你实际的 ruoyi-cloud 库名
SET NAMES utf8mb4 COLLATE utf8mb4_unicode_ci;
USE `ry-cloud`;

-- 电子病历一级菜单
INSERT INTO sys_menu VALUES (3050, '电子病历', 0, 6, 'emr', NULL, '', '', 1, 0, 'M', '0', '0', '', 'documentation', 'admin', sysdate(), '', NULL, '电子病历目录');
-- 病历记录
INSERT INTO sys_menu VALUES (3051, '病历记录', 3050, 1, 'record', 'medical/emr/record/index', '', '', 1, 0, 'C', '0', '0', 'emr:record:list', 'form', 'admin', sysdate(), '', NULL, '病历记录列表');
-- 病历模板
INSERT INTO sys_menu VALUES (3052, '病历模板', 3050, 2, 'template', 'medical/emr/template/index', '', '', 1, 0, 'C', '0', '0', 'emr:template:list', 'education', 'admin', sysdate(), '', NULL, '病历模板管理');
-- 归档管理
INSERT INTO sys_menu VALUES (3053, '归档管理', 3050, 3, 'archive', 'medical/emr/archive/index', '', '', 1, 0, 'C', '0', '0', 'emr:archive:list', 'lock', 'admin', sysdate(), '', NULL, '归档记录管理');

-- 病历记录 按钮权限
INSERT INTO sys_menu VALUES (3060, '病历查询', 3051, 1, '', '', '', '', 1, 0, 'F', '0', '0', 'emr:record:query', '#', 'admin', sysdate(), '', NULL, '');
INSERT INTO sys_menu VALUES (3061, '病历新增', 3051, 2, '', '', '', '', 1, 0, 'F', '0', '0', 'emr:record:add', '#', 'admin', sysdate(), '', NULL, '');
INSERT INTO sys_menu VALUES (3062, '病历修改', 3051, 3, '', '', '', '', 1, 0, 'F', '0', '0', 'emr:record:edit', '#', 'admin', sysdate(), '', NULL, '');
INSERT INTO sys_menu VALUES (3063, '病历删除', 3051, 4, '', '', '', '', 1, 0, 'F', '0', '0', 'emr:record:remove', '#', 'admin', sysdate(), '', NULL, '');
INSERT INTO sys_menu VALUES (3064, '病历导出', 3051, 5, '', '', '', '', 1, 0, 'F', '0', '0', 'emr:record:export', '#', 'admin', sysdate(), '', NULL, '');
INSERT INTO sys_menu VALUES (3065, '病历导入', 3051, 6, '', '', '', '', 1, 0, 'F', '0', '0', 'emr:record:import', '#', 'admin', sysdate(), '', NULL, '');
INSERT INTO sys_menu VALUES (3066, '病历归档', 3051, 7, '', '', '', '', 1, 0, 'F', '0', '0', 'emr:record:archive', '#', 'admin', sysdate(), '', NULL, '');
INSERT INTO sys_menu VALUES (3067, '取消归档', 3051, 8, '', '', '', '', 1, 0, 'F', '0', '0', 'emr:record:unarchive', '#', 'admin', sysdate(), '', NULL, '');

-- 病历模板 按钮权限
INSERT INTO sys_menu VALUES (3070, '模板查询', 3052, 1, '', '', '', '', 1, 0, 'F', '0', '0', 'emr:template:query', '#', 'admin', sysdate(), '', NULL, '');
INSERT INTO sys_menu VALUES (3071, '模板新增', 3052, 2, '', '', '', '', 1, 0, 'F', '0', '0', 'emr:template:add', '#', 'admin', sysdate(), '', NULL, '');
INSERT INTO sys_menu VALUES (3072, '模板修改', 3052, 3, '', '', '', '', 1, 0, 'F', '0', '0', 'emr:template:edit', '#', 'admin', sysdate(), '', NULL, '');
INSERT INTO sys_menu VALUES (3073, '模板删除', 3052, 4, '', '', '', '', 1, 0, 'F', '0', '0', 'emr:template:remove', '#', 'admin', sysdate(), '', NULL, '');
INSERT INTO sys_menu VALUES (3074, '模板导出', 3052, 5, '', '', '', '', 1, 0, 'F', '0', '0', 'emr:template:export', '#', 'admin', sysdate(), '', NULL, '');
INSERT INTO sys_menu VALUES (3075, '模板导入', 3052, 6, '', '', '', '', 1, 0, 'F', '0', '0', 'emr:template:import', '#', 'admin', sysdate(), '', NULL, '');

-- 赋予普通管理员角色菜单权限
INSERT INTO sys_role_menu VALUES (2, 3050);
INSERT INTO sys_role_menu VALUES (2, 3051);
INSERT INTO sys_role_menu VALUES (2, 3052);
INSERT INTO sys_role_menu VALUES (2, 3053);
INSERT INTO sys_role_menu VALUES (2, 3060);
INSERT INTO sys_role_menu VALUES (2, 3061);
INSERT INTO sys_role_menu VALUES (2, 3062);
INSERT INTO sys_role_menu VALUES (2, 3063);
INSERT INTO sys_role_menu VALUES (2, 3064);
INSERT INTO sys_role_menu VALUES (2, 3065);
INSERT INTO sys_role_menu VALUES (2, 3066);
INSERT INTO sys_role_menu VALUES (2, 3067);
INSERT INTO sys_role_menu VALUES (2, 3070);
INSERT INTO sys_role_menu VALUES (2, 3071);
INSERT INTO sys_role_menu VALUES (2, 3072);
INSERT INTO sys_role_menu VALUES (2, 3073);
INSERT INTO sys_role_menu VALUES (2, 3074);
INSERT INTO sys_role_menu VALUES (2, 3075);
