-- =============================================================================
-- 回滚 medical_workbench_menu_merge.sql（执行前请确认当前库仍为合并后结构）
-- =============================================================================
USE `ry-cloud`;

UPDATE sys_menu SET parent_id = 3100, path = 'xray', order_num = 1 WHERE menu_id = 3101;
UPDATE sys_menu SET parent_id = 3100, path = 'upload', order_num = 2 WHERE menu_id = 3102;
UPDATE sys_menu SET parent_id = 3100, path = 'annotate', order_num = 3 WHERE menu_id = 3103;
UPDATE sys_menu SET parent_id = 3100, path = 'sequence', order_num = 4 WHERE menu_id = 3104;

UPDATE sys_menu SET parent_id = 3200, path = 'list', order_num = 1 WHERE menu_id = 3201;
UPDATE sys_menu SET parent_id = 3200, path = 'record', order_num = 2 WHERE menu_id = 3220;
UPDATE sys_menu SET parent_id = 3200, path = 'lab', order_num = 3 WHERE menu_id = 3280;

UPDATE sys_menu SET parent_id = 3240, path = 'analysis', order_num = 1 WHERE menu_id = 3241;

UPDATE sys_menu SET visible = '0' WHERE menu_id IN (3100, 3200, 3240);

DELETE FROM sys_role_menu WHERE menu_id = 3300;
DELETE FROM sys_menu WHERE menu_id = 3300;

INSERT IGNORE INTO sys_role_menu VALUES (2, 3100);
INSERT IGNORE INTO sys_role_menu VALUES (2, 3200);
INSERT IGNORE INTO sys_role_menu VALUES (2, 3240);
INSERT IGNORE INTO sys_role_menu VALUES (100, 3100);
INSERT IGNORE INTO sys_role_menu VALUES (100, 3200);
INSERT IGNORE INTO sys_role_menu VALUES (100, 3240);
