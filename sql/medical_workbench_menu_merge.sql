-- =============================================================================
-- 医疗模块：将「影像管理」「患者管理」「AI影像分析」下的页面级菜单合并到「工作台」
-- 依赖：已执行 medical_imaging_ry_menu.sql、medical_patient_ry_menu_zh.sql、
--       medical_record_ry_menu_zh.sql、medical_lab_result_menu_zh.sql、
--       medical_ai_image_ry_menu_zh.sql（menu_id 与下述一致）
-- 说明：
--   1. 子菜单 path 改为以 / 开头的应用内绝对路径，保证与现有 router.push、
--      侧边栏 path.resolve 及 Vue Router 嵌套路由行为一致（仍为 /patient/list 等）。
--   2. 原三个父目录仅隐藏（visible=1），便于回滚；若希望菜单管理里也不出现，
--      可自行改为 status=1 或删除（需同步 sys_role_menu）。
--   3. 其他角色除 2、100 外若只绑了旧父目录未绑子菜单，需在「角色-菜单」中补勾 3300 或各子页。
-- =============================================================================
USE `ry-cloud`;

-- 工作台顶层目录（与影像管理原 order_num=7 同级，可按需改 order_num）
INSERT INTO sys_menu VALUES (3300, '工作台', 0, 7, 'workbench', NULL, '', '', 1, 0, 'M', '0', '0', '', 'dashboard', 'admin', sysdate(), '', NULL, '临床工作台统一入口');

-- 一级页面挂到工作台；path 使用绝对路径，避免挂在 workbench 下变成 /workbench/patient/list
UPDATE sys_menu SET parent_id = 3300, path = '/imaging/xray', order_num = 1 WHERE menu_id = 3101;
UPDATE sys_menu SET parent_id = 3300, path = '/imaging/upload', order_num = 2 WHERE menu_id = 3102;
UPDATE sys_menu SET parent_id = 3300, path = '/imaging/annotate', order_num = 3 WHERE menu_id = 3103;
UPDATE sys_menu SET parent_id = 3300, path = '/imaging/sequence', order_num = 4 WHERE menu_id = 3104;
UPDATE sys_menu SET parent_id = 3300, path = '/patient/list', order_num = 5 WHERE menu_id = 3201;
UPDATE sys_menu SET parent_id = 3300, path = '/patient/record', order_num = 6 WHERE menu_id = 3220;
UPDATE sys_menu SET parent_id = 3300, path = '/patient/lab', order_num = 7 WHERE menu_id = 3280;
UPDATE sys_menu SET parent_id = 3300, path = '/ai-image/analysis', order_num = 8 WHERE menu_id = 3241;

-- 隐藏原三个父级目录（侧边栏不再展示；子菜单权限仍通过原 menu_id 绑定）
UPDATE sys_menu SET visible = '1' WHERE menu_id IN (3100, 3200, 3240);

-- 角色菜单：授予工作台目录；去掉旧父目录绑定（子菜单原绑定保留）
INSERT IGNORE INTO sys_role_menu VALUES (2, 3300);
INSERT IGNORE INTO sys_role_menu VALUES (100, 3300);
DELETE FROM sys_role_menu WHERE menu_id IN (3100, 3200, 3240);
