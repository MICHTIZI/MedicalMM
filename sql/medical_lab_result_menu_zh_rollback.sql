-- 回滚 medical_lab_result_menu_zh.sql：删除检验指标菜单（3280～3285）及角色绑定
USE `ry-cloud`;

DELETE FROM sys_role_menu WHERE menu_id IN (3280, 3281, 3282, 3283, 3284, 3285);
DELETE FROM sys_menu WHERE menu_id IN (3280, 3281, 3282, 3283, 3284, 3285);