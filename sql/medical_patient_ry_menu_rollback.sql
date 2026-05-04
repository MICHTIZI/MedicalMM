-- =============================================================================
-- 患者管理菜单回滚脚本
-- 用途：撤销已执行的英文菜单 medical_patient_ry_menu.sql
-- 注意：只删除 menu_id 3200-3215 及其角色绑定，不删除业务表 medical_patient
-- =============================================================================

SET NAMES utf8mb4 COLLATE utf8mb4_unicode_ci;
USE `ry-cloud`;

DELETE FROM sys_role_menu WHERE menu_id IN (3200, 3201, 3210, 3211, 3212, 3213, 3214, 3215);
DELETE FROM sys_menu WHERE menu_id IN (3215, 3214, 3213, 3212, 3211, 3210, 3201, 3200);
