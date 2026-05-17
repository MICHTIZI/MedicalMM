-- 图像分析管理菜单：挂在「AI影像分析」(menu_id=3240) 下。
-- UTF-8 utf8mb4；请用 mysql --default-character-set=utf8mb4 执行。
-- 依赖：已执行 medical_ai_image_ry_menu_zh.sql 。
USE `ry-cloud`;

DELETE FROM sys_role_menu WHERE menu_id BETWEEN 3245 AND 3249;
DELETE FROM sys_menu WHERE menu_id BETWEEN 3245 AND 3249;

INSERT INTO sys_menu VALUES (3245, '图像分析管理', 3240, 2, 'analysis-result', 'medical/ai-analysis-result/index', '', '', 1, 0, 'C', '0', '0', 'ai:analysis:result:list', 'documentation', 'admin', sysdate(), '', NULL, 'AI病灶分析结果管理');
INSERT INTO sys_menu VALUES (3246, '分析结果查询', 3245, 1, '', '', '', '', 1, 0, 'F', '0', '0', 'ai:analysis:result:query', '#', 'admin', sysdate(), '', NULL, '');
INSERT INTO sys_menu VALUES (3247, '分析结果删除', 3245, 2, '', '', '', '', 1, 0, 'F', '0', '0', 'ai:analysis:result:remove', '#', 'admin', sysdate(), '', NULL, '');

INSERT INTO sys_role_menu VALUES (2, 3245);
INSERT INTO sys_role_menu VALUES (2, 3246);
INSERT INTO sys_role_menu VALUES (2, 3247);

INSERT INTO sys_role_menu VALUES (100, 3245);
INSERT INTO sys_role_menu VALUES (100, 3246);
INSERT INTO sys_role_menu VALUES (100, 3247);
