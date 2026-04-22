-- 医生工作台菜单（menu_id=2100）
-- ==============================
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
VALUES (2100, '医生工作台', 0, 11, 'physician', NULL, '', 1, 0, 'M', '0', '0', '', 'user', 'admin', sysdate(), '', NULL, '医生专属目录');

INSERT INTO sys_menu VALUES (2101, '工作台', 2100, 1, 'workbench', 'medical/physician/workbench/index', '', 'PhysicianWorkbench', 1, 0, 'C', '0', '0', 'doctor:workbench:view', 'dashboard', 'admin', sysdate(), '', NULL, '医生工作台首页');
INSERT INTO sys_menu VALUES (2110, '病历AI诊断', 2100, 2, 'emr-ai', 'medical/physician/emr-ai/index', '', 'PhysicianEmrAi', 1, 0, 'C', '0', '0', 'doctor:emr:ai', 'education', 'admin', sysdate(), '', NULL, '电子病历关键信息AI提取');
INSERT INTO sys_menu VALUES (2120, '影像检测', 2100, 3, 'image-detect', 'medical/physician/image-detect/index', '', 'PhysicianImageDetect', 1, 0, 'C', '0', '0', 'doctor:image:detect', 'eye-open', 'admin', sysdate(), '', NULL, '基于MinIO影像病灶检测');
INSERT INTO sys_menu VALUES (2121, '病灶精准诊断', 2100, 4, 'image-segment', 'medical/physician/image-segment/index', '', 'PhysicianImageSegment', 1, 0, 'C', '0', '0', 'doctor:image:segment', 'cascader', 'admin', sysdate(), '', NULL, '病灶精准分割与诊断');
INSERT INTO sys_menu VALUES (2130, '诊断报告', 2100, 5, 'report', 'medical/physician/report/index', '', 'PhysicianReport', 1, 0, 'C', '0', '0', 'doctor:report:list', 'documentation', 'admin', sysdate(), '', NULL, '诊断报告列表');
INSERT INTO sys_menu VALUES (2131, '报告编辑', 2100, 51, 'report/edit/:id?', 'medical/physician/report/edit', '', 'PhysicianReportEdit', 1, 1, 'C', '1', '0', 'doctor:report:edit', '#', 'admin', sysdate(), '', NULL, '诊断报告编辑');
INSERT INTO sys_menu VALUES (2140, '个人中心', 2100, 6, 'profile', 'system/user/profile/index', '', 'PhysicianProfile', 1, 0, 'C', '0', '0', 'doctor:profile:view', 'user', 'admin', sysdate(), '', NULL, '医生个人信息与安全设置');

-- ==============================
-- 角色 100 绑定菜单（仅医生端菜单，无重复权限）
-- ==============================
INSERT INTO sys_role_menu VALUES (100, 2100);
INSERT INTO sys_role_menu VALUES (100, 2101);
INSERT INTO sys_role_menu VALUES (100, 2110);
INSERT INTO sys_role_menu VALUES (100, 2120);
INSERT INTO sys_role_menu VALUES (100, 2121);
INSERT INTO sys_role_menu VALUES (100, 2130);
INSERT INTO sys_role_menu VALUES (100, 2131);
INSERT INTO sys_role_menu VALUES (100, 2140);
