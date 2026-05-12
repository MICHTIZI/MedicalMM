# -*- coding: utf-8 -*-
"""Generate UTF-8 SQL files (ASCII-only source; avoids editor/shell encoding issues)."""
from pathlib import Path

HERE = Path(__file__).resolve().parent

MERGE_ZH_HEADER = """-- =============================================================================
-- \u533b\u7597\u6a21\u5757\uff1a\u5c06\u300c\u5f71\u50cf\u7ba1\u7406\u300d\u300c\u60a3\u8005\u7ba1\u7406\u300d\u300cAI\u5f71\u50cf\u5206\u6790\u300d\u4e0b\u7684\u9875\u9762\u7ea7\u83dc\u5355\u5408\u5e76\u5230\u300c\u5de5\u4f5c\u53f0\u300d
-- \u4f9d\u8d56\uff1a\u5df2\u6267\u884c medical_imaging_ry_menu.sql\u3001medical_patient_ry_menu_zh.sql\u3001
--       medical_record_ry_menu_zh.sql\u3001medical_lab_result_menu_zh.sql\u3001
--       medical_ai_image_ry_menu_zh.sql\uff08menu_id \u4e0e\u4e0b\u8ff0\u4e00\u81f4\uff09
-- \u8bf4\u660e\uff1a
--   1. \u5b50\u83dc\u5355 path \u6539\u4e3a\u4ee5 / \u5f00\u5934\u7684\u5e94\u7528\u5185\u7edd\u5bf9\u8def\u5f84\uff0c\u4fdd\u8bc1\u4e0e\u73b0\u6709 router.push\u3001
--      \u4fa7\u8fb9\u680f path.resolve \u53ca Vue Router \u5d4c\u5957\u8def\u7531\u884c\u4e3a\u4e00\u81f4\uff08\u4ecd\u4e3a /patient/list \u7b49\uff09\u3002
--   2. \u539f\u4e09\u4e2a\u7236\u76ee\u5f55\u4ec5\u9690\u85cf\uff08visible=1\uff09\uff0c\u4fbf\u4e8e\u56de\u6eda\uff1b\u82e5\u5e0c\u671b\u83dc\u5355\u7ba1\u7406\u91cc\u4e5f\u4e0d\u51fa\u73b0\uff0c
--      \u53ef\u81ea\u884c\u6539\u4e3a status=1 \u6216\u5220\u9664\uff08\u9700\u540c\u6b65 sys_role_menu\uff09\u3002
--   3. \u5176\u4ed6\u89d2\u8272\u9664 2\u3001100 \u5916\u82e5\u53ea\u7ed1\u4e86\u65e7\u7236\u76ee\u5f55\u672a\u7ed1\u5b50\u83dc\u5355\uff0c\u9700\u5728\u300c\u89d2\u8272-\u83dc\u5355\u300d\u4e2d\u8865\u52fe 3300 \u6216\u5404\u5b50\u9875\u3002
-- =============================================================================
"""

MERGE_ZH_BODY = """USE `ry-cloud`;

-- \u5de5\u4f5c\u53f0\u9876\u5c42\u76ee\u5f55\uff08\u4e0e\u5f71\u50cf\u7ba1\u7406\u539f order_num=7 \u540c\u7ea7\uff0c\u53ef\u6309\u9700\u6539 order_num\uff09
INSERT INTO sys_menu VALUES (3300, '\u5de5\u4f5c\u53f0', 0, 7, 'workbench', NULL, '', '', 1, 0, 'M', '0', '0', '', 'dashboard', 'admin', sysdate(), '', NULL, '\u4e34\u5e8a\u5de5\u4f5c\u53f0\u7edf\u4e00\u5165\u53e3');

-- \u4e00\u7ea7\u9875\u9762\u6302\u5230\u5de5\u4f5c\u53f0\uff1bpath \u4f7f\u7528\u7edd\u5bf9\u8def\u5f84\uff0c\u907f\u514d\u6302\u5728 workbench \u4e0b\u53d8\u6210 /workbench/patient/list
UPDATE sys_menu SET parent_id = 3300, path = '/imaging/xray', order_num = 1 WHERE menu_id = 3101;
UPDATE sys_menu SET parent_id = 3300, path = '/imaging/upload', order_num = 2 WHERE menu_id = 3102;
UPDATE sys_menu SET parent_id = 3300, path = '/imaging/annotate', order_num = 3 WHERE menu_id = 3103;
UPDATE sys_menu SET parent_id = 3300, path = '/imaging/sequence', order_num = 4 WHERE menu_id = 3104;
UPDATE sys_menu SET parent_id = 3300, path = '/patient/list', order_num = 5 WHERE menu_id = 3201;
UPDATE sys_menu SET parent_id = 3300, path = '/patient/record', order_num = 6 WHERE menu_id = 3220;
UPDATE sys_menu SET parent_id = 3300, path = '/patient/lab', order_num = 7 WHERE menu_id = 3280;
UPDATE sys_menu SET parent_id = 3300, path = '/ai-image/analysis', order_num = 8 WHERE menu_id = 3241;

-- \u9690\u85cf\u539f\u4e09\u4e2a\u7236\u7ea7\u76ee\u5f55\uff08\u4fa7\u8fb9\u680f\u4e0d\u518d\u5c55\u793a\uff1b\u5b50\u83dc\u5355\u6743\u9650\u4ecd\u901a\u8fc7\u539f menu_id \u7ed1\u5b9a\uff09
UPDATE sys_menu SET visible = '1' WHERE menu_id IN (3100, 3200, 3240);

-- \u89d2\u8272\u83dc\u5355\uff1a\u6388\u4e88\u5de5\u4f5c\u53f0\u76ee\u5f55\uff1b\u53bb\u6389\u65e7\u7236\u76ee\u5f55\u7ed1\u5b9a\uff08\u5b50\u83dc\u5355\u539f\u7ed1\u5b9a\u4fdd\u7559\uff09
INSERT IGNORE INTO sys_role_menu VALUES (2, 3300);
INSERT IGNORE INTO sys_role_menu VALUES (100, 3300);
DELETE FROM sys_role_menu WHERE menu_id IN (3100, 3200, 3240);
"""

ROLLBACK_ZH_HEADER = """-- =============================================================================
-- \u56de\u6eda medical_workbench_menu_merge.sql\uff08\u6267\u884c\u524d\u8bf7\u786e\u8ba4\u5f53\u524d\u5e93\u4ecd\u4e3a\u5408\u5e76\u540e\u7ed3\u6784\uff09
-- =============================================================================
"""

ROLLBACK_BODY = """USE `ry-cloud`;

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
"""


def main():
    # UTF-8 output with Chinese comments + menu strings (decoded from escapes in source)
    merge_zh = MERGE_ZH_HEADER + MERGE_ZH_BODY
    (HERE / "medical_workbench_menu_merge.sql").write_text(merge_zh, encoding="utf-8", newline="\n")

    rb_zh = ROLLBACK_ZH_HEADER + ROLLBACK_BODY
    (HERE / "medical_workbench_menu_merge_rollback.sql").write_text(rb_zh, encoding="utf-8", newline="\n")

    print("OK: medical_workbench_menu_merge.sql")
    print("OK: medical_workbench_menu_merge_rollback.sql")


if __name__ == "__main__":
    main()
