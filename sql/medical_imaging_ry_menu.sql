-- 文件编码: UTF-8 | 客户端连接字符集: utf8mb4
-- 若依主库菜单与角色菜单（影像数据管理模块）
SET NAMES utf8mb4 COLLATE utf8mb4_unicode_ci;
USE `ry-cloud`;

-- 影像管理顶级菜单
INSERT INTO sys_menu VALUES (3100, '影像管理', 0, 7, 'imaging', NULL, '', '', 1, 0, 'M', '0', '0', '', 'monitor', 'admin', sysdate(), '', NULL, '医学影像数据管理目录');

-- 影像数据
INSERT INTO sys_menu VALUES (3101, '影像数据', 3100, 1, 'xray', 'medical/imaging/xray/index', '', '', 1, 0, 'C', '0', '0', 'imaging:xray:list', 'eye-open', 'admin', sysdate(), '', NULL, '胸部X光影像列表');
-- 影像上传
INSERT INTO sys_menu VALUES (3102, '影像上传', 3100, 2, 'upload', 'medical/imaging/upload/index', '', '', 1, 0, 'C', '0', '0', 'imaging:xray:upload', 'upload', 'admin', sysdate(), '', NULL, '影像批量上传');
-- 影像标注
INSERT INTO sys_menu VALUES (3103, '影像标注', 3100, 3, 'annotate', 'medical/imaging/annotate/index', '', '', 1, 0, 'C', '0', '0', 'imaging:xray:annotate', 'edit', 'admin', sysdate(), '', NULL, '影像疾病标注');
-- 序列管理
INSERT INTO sys_menu VALUES (3104, '序列管理', 3100, 4, 'sequence', 'medical/imaging/sequence/index', '', '', 1, 0, 'C', '0', '0', 'imaging:sequence:list', 'cascader', 'admin', sysdate(), '', NULL, '影像序列分组管理');

-- 影像数据 按钮权限
INSERT INTO sys_menu VALUES (3110, '影像查询', 3101, 1, '', '', '', '', 1, 0, 'F', '0', '0', 'imaging:xray:query', '#', 'admin', sysdate(), '', NULL, '');
INSERT INTO sys_menu VALUES (3111, '影像新增', 3101, 2, '', '', '', '', 1, 0, 'F', '0', '0', 'imaging:xray:add', '#', 'admin', sysdate(), '', NULL, '');
INSERT INTO sys_menu VALUES (3112, '影像修改', 3101, 3, '', '', '', '', 1, 0, 'F', '0', '0', 'imaging:xray:edit', '#', 'admin', sysdate(), '', NULL, '');
INSERT INTO sys_menu VALUES (3113, '影像删除', 3101, 4, '', '', '', '', 1, 0, 'F', '0', '0', 'imaging:xray:remove', '#', 'admin', sysdate(), '', NULL, '');
INSERT INTO sys_menu VALUES (3114, '影像导出', 3101, 5, '', '', '', '', 1, 0, 'F', '0', '0', 'imaging:xray:export', '#', 'admin', sysdate(), '', NULL, '');
INSERT INTO sys_menu VALUES (3115, '影像上传', 3101, 6, '', '', '', '', 1, 0, 'F', '0', '0', 'imaging:xray:upload', '#', 'admin', sysdate(), '', NULL, '');
INSERT INTO sys_menu VALUES (3116, '影像标注', 3101, 7, '', '', '', '', 1, 0, 'F', '0', '0', 'imaging:xray:annotate', '#', 'admin', sysdate(), '', NULL, '');

-- 序列管理 按钮权限
INSERT INTO sys_menu VALUES (3120, '序列查询', 3104, 1, '', '', '', '', 1, 0, 'F', '0', '0', 'imaging:sequence:query', '#', 'admin', sysdate(), '', NULL, '');
INSERT INTO sys_menu VALUES (3121, '序列新增', 3104, 2, '', '', '', '', 1, 0, 'F', '0', '0', 'imaging:sequence:add', '#', 'admin', sysdate(), '', NULL, '');
INSERT INTO sys_menu VALUES (3122, '序列修改', 3104, 3, '', '', '', '', 1, 0, 'F', '0', '0', 'imaging:sequence:edit', '#', 'admin', sysdate(), '', NULL, '');
INSERT INTO sys_menu VALUES (3123, '序列删除', 3104, 4, '', '', '', '', 1, 0, 'F', '0', '0', 'imaging:sequence:remove', '#', 'admin', sysdate(), '', NULL, '');
INSERT INTO sys_menu VALUES (3124, '序列归档', 3104, 5, '', '', '', '', 1, 0, 'F', '0', '0', 'imaging:sequence:archive', '#', 'admin', sysdate(), '', NULL, '');
INSERT INTO sys_menu VALUES (3125, '取消归档', 3104, 6, '', '', '', '', 1, 0, 'F', '0', '0', 'imaging:sequence:unarchive', '#', 'admin', sysdate(), '', NULL, '');

-- 角色菜单关联（普通管理员角色 role_id=2）
INSERT INTO sys_role_menu VALUES (2, 3100);
INSERT INTO sys_role_menu VALUES (2, 3101);
INSERT INTO sys_role_menu VALUES (2, 3102);
INSERT INTO sys_role_menu VALUES (2, 3103);
INSERT INTO sys_role_menu VALUES (2, 3104);
INSERT INTO sys_role_menu VALUES (2, 3110);
INSERT INTO sys_role_menu VALUES (2, 3111);
INSERT INTO sys_role_menu VALUES (2, 3112);
INSERT INTO sys_role_menu VALUES (2, 3113);
INSERT INTO sys_role_menu VALUES (2, 3114);
INSERT INTO sys_role_menu VALUES (2, 3115);
INSERT INTO sys_role_menu VALUES (2, 3116);
INSERT INTO sys_role_menu VALUES (2, 3120);
INSERT INTO sys_role_menu VALUES (2, 3121);
INSERT INTO sys_role_menu VALUES (2, 3122);
INSERT INTO sys_role_menu VALUES (2, 3123);
INSERT INTO sys_role_menu VALUES (2, 3124);
INSERT INTO sys_role_menu VALUES (2, 3125);
