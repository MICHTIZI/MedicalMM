# -*- coding: utf-8 -*-
"""Fix corrupted UI strings in viewer/index.vue (ASCII-only source file)."""
from __future__ import annotations

import re
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
VUE = ROOT / "ruoyi-ui" / "src" / "views" / "medical" / "viewer" / "index.vue"


def u(*codes: int) -> str:
    return "".join(chr(c) for c in codes)


# (old, new): old substring must match disk; new uses u() so this file stays ASCII-only.
REPLACEMENTS: list[tuple[str, str]] = [
    ('@click="goPatientList">???????????</el-button>', '@click="goPatientList">' + u(0x8FD4, 0x56DE, 0x60A3, 0x8005, 0x5217, 0x8868) + "</el-button>"),
    ('<span class="tb-label">????</span>', '<span class="tb-label">' + u(0x60A3, 0x8005) + "</span>"),
    ('placeholder="???????????"', 'placeholder="' + u(0x641C, 0x7D22, 0x5E76, 0x9009, 0x62E9, 0x60A3, 0x8005) + '"'),
    ('@click="prevImage">?????</el-button>', '@click="prevImage">' + u(0x4E0A, 0x4E00, 0x5F20) + "</el-button>"),
    ('@click="nextImage">?????</el-button>', '@click="nextImage">' + u(0x4E0B, 0x4E00, 0x5F20) + "</el-button>"),
    ('@click="resetView">???????</el-button>', '@click="resetView">' + u(0x91CD, 0x7F6E, 0x89C6, 0x56FE) + "</el-button>"),
    ('@click="tool=\'zoom\'">????</el-button>', '@click="tool=\'zoom\'">' + u(0x7F29, 0x653E) + "</el-button>"),
    ('@click="tool=\'pan\'">???</el-button>', '@click="tool=\'pan\'">' + u(0x5E73, 0x79FB) + "</el-button>"),
    ('<div>???????????{{ Math.round(brightness * 100) }}%</div>', "<div>" + u(0x7A97, 0x4F4D, 0xFF08, 0x4EAE, 0x5EA6, 0xFF09) + "{{ Math.round(brightness * 100) }}%</div>"),
    ('<div>??????????{{ Math.round(contrast * 100) }}%</div>', "<div>" + u(0x7A97, 0x5BBD, 0xFF08, 0x5BF9, 0x6BD4, 0xFF09) + "{{ Math.round(contrast * 100) }}%</div>"),
    ('icon="el-icon-sunny">????????</el-button>', 'icon="el-icon-sunny">' + u(0x7A97, 0x5BBD, 0x7A97, 0x4F4D) + "</el-button>"),
    ('@click="fitToCanvas">????????</el-button>', '@click="fitToCanvas">' + u(0x94FA, 0x6EE1, 0x753B, 0x5E03) + "</el-button>"),
    ('@click="tool=\'ruler\'">???</el-button>', '@click="tool=\'ruler\'">' + u(0x6D4B, 0x8DDD) + "</el-button>"),
    ('@click="tool=\'rect\'">???????</el-button>', '@click="tool=\'rect\'">' + u(0x77E9, 0x5F62, 0x6807, 0x6CE8) + "</el-button>"),
    ('@click="tool=\'brush\'">????</el-button>', '@click="tool=\'brush\'">' + u(0x753B, 0x7B14) + "</el-button>"),
    ('@click="clearUserAnnotations">??????</el-button>', '@click="clearUserAnnotations">' + u(0x6E05, 0x9664, 0x6807, 0x6CE8) + "</el-button>"),
    ('@click="goImportRecord">??????</el-button>', '@click="goImportRecord">' + u(0x5BFC, 0x5165, 0x75C5, 0x5386) + "</el-button>"),
    ('@click="goImportLab">???????</el-button>', '@click="goImportLab">' + u(0x5BFC, 0x5165, 0x68C0, 0x9A8C) + "</el-button>"),
    ('@click="runAiAnalyze">AI ???????</el-button>', '@click="runAiAnalyze">AI ' + u(0x75C5, 0x7076, 0x5206, 0x6790) + "</el-button>"),
    ('@click="exportReport">???????</el-button>', '@click="exportReport">' + u(0x751F, 0x6210, 0x62A5, 0x544A) + "</el-button>"),
    ("{{ isFs ? '??????' : '???' }}", "{{ isFs ? '" + u(0x9000, 0x51FA, 0x5168, 0x5C4F) + "' : '" + u(0x5168, 0x5C4F) + "' }}"),
    ('class="stage-empty">???????????????</div>', 'class="stage-empty">' + u(0x8BF7, 0x9009, 0x62E9, 0x60A3, 0x8005, 0x5E76, 0x52A0, 0x8F7D, 0x80F8, 0x7247) + "</div>"),
    ("<!-- AI ??????????????? -->", "<!-- AI " + u(0x75C5, 0x7076, 0x6846, 0x4E0E, 0x624B, 0x5DE5, 0x6807, 0x6CE8, 0x5C42) + " -->"),
    ('title="???????????"', 'title="' + u(0x62D6, 0x62FD, 0x8C03, 0x6574, 0x5BBD, 0x5EA6) + '"'),
    ('<h3 class="rp-title">??????????</h3>', '<h3 class="rp-title">' + u(0x60A3, 0x8005, 0x57FA, 0x7840, 0x4FE1, 0x606F) + "</h3>"),
    (
        "<span class=\"rp-meta\">{{ genderText(detailSnapshot.gender) }} ?? {{ detailSnapshot.age != null ? detailSnapshot.age + '??' : '??' }}</span>",
        "<span class=\"rp-meta\">{{ genderText(detailSnapshot.gender) }} \u00b7 {{ detailSnapshot.age != null ? detailSnapshot.age + '\u5c81' : '\u2014' }}</span>",
    ),
    (
        '<div class="rp-row muted">?????????{{ detailSnapshot.attendingDoctor || \'??\' }}</div>',
        '<div class="rp-row muted">' + u(0x4E3B, 0x6CBB, 0x533B, 0x751F, 0xFF1A) + "{{ detailSnapshot.attendingDoctor || '\u2014' }}</div>",
    ),
    (
        '<div class="rp-row muted">????/??????{{ parseTime(detailSnapshot.createTime) }}</div>',
        '<div class="rp-row muted">' + u(0x5C31, 0x8BCA, 0x5EFA, 0x6863, 0xFF1A) + "{{ parseTime(detailSnapshot.createTime) }}</div>",
    ),
    (
        'modOk(d.hasImage)?\'el-icon-success\':\'el-icon-circle-plus-outline\'" />???</span>',
        'modOk(d.hasImage)?\'el-icon-success\':\'el-icon-circle-plus-outline\'" />' + u(0x80F8, 0x7247) + "</span>",
    ),
    (
        'modOk(d.hasMedicalRecord)?\'el-icon-success\':\'el-icon-circle-plus-outline\'" />????</span>',
        'modOk(d.hasMedicalRecord)?\'el-icon-success\':\'el-icon-circle-plus-outline\'" />' + u(0x75C5, 0x5386) + "</span>",
    ),
    (
        'modOk(d.hasLabResult)?\'el-icon-success\':\'el-icon-circle-plus-outline\'" />????</span>',
        'modOk(d.hasLabResult)?\'el-icon-success\':\'el-icon-circle-plus-outline\'" />' + u(0x68C0, 0x9A8C) + "</span>",
    ),
    ('description="????????????"', 'description="' + u(0x672A, 0x52A0, 0x8F7D, 0x60A3, 0x8005, 0x4FE1, 0x606F) + '"'),
    ('<h3 class="rp-title">AI ???????????</h3>', '<h3 class="rp-title">AI ' + u(0x75C5, 0x7076, 0x7ED3, 0x679C) + "</h3>"),
    ('<span>????????</span>', '<span>' + u(0x75C5, 0x7076, 0x6570, 0x91CF) + "</span>"),
]

# Narrow infection label (only that line context)
REPLACEMENTS.append((
    '<div v-if="aiResult.infection_rate != null || aiResult.infectionRate != null" class="rp-kv"><span>?????</span>',
    '<div v-if="aiResult.infection_rate != null || aiResult.infectionRate != null" class="rp-kv"><span>' + u(0x611F, 0x67D3, 0x7387) + "</span>",
))
REPLACEMENTS.append((
    '<span>??????????(px?)</span>',
    "<span>" + u(0x611F, 0x67D3, 0x533A, 0x57DF, 0x9762, 0x79EF) + "(px\u00b2)</span>",
))
REPLACEMENTS.append(('<span>??????</span><b class="sev">', "<span>" + u(0x4E25, 0x91CD, 0x7A0B, 0x5EA6) + "</span><b class=\"sev\">"))
REPLACEMENTS.append((
    'aiResult.pneumoniaType" class="rp-kv"><span>????????</span>',
    'aiResult.pneumoniaType" class="rp-kv"><span>' + u(0x80BA, 0x708E, 0x7C7B, 0x578B) + "</span>",
))
REPLACEMENTS.append(('<div v-if="aiResult.diagnosis" class="rp-block"><span class="lbl">??????</span>', '<div v-if="aiResult.diagnosis" class="rp-block"><span class="lbl">' + u(0x8BCA, 0x65AD, 0x610F, 0x89C1) + "</span>"))
REPLACEMENTS.append((
    'aiResult.treatmentSuggestion" class="rp-block"><span class="lbl">???????</span>',
    'aiResult.treatmentSuggestion" class="rp-block"><span class="lbl">' + u(0x8BCA, 0x7597, 0x5EFA, 0x8BAE) + "</span>",
))
REPLACEMENTS.append((
    'aiResult.furtherExamination" class="rp-block"><span class="lbl">????????</span>',
    'aiResult.furtherExamination" class="rp-block"><span class="lbl">' + u(0x8FDB, 0x4E00, 0x6B65, 0x68C0, 0x67E5) + "</span>",
))
REPLACEMENTS.append(('<el-collapse-item title="????????" name="1">', '<el-collapse-item title="' + u(0x9010, 0x75C5, 0x7076, 0x8BE6, 0x60C5) + '" name="1">'))
REPLACEMENTS.append(('<div class="li-h">???? {{ i + 1 }}</div>', '<div class="li-h">' + u(0x75C5, 0x7076) + " {{ i + 1 }}</div>"))
REPLACEMENTS.append(('<div class="rp-kv sm"><span>????</span><b>{{ lv.full_position', '<div class="rp-kv sm"><span>' + u(0x4F4D, 0x7F6E) + "</span><b>{{ lv.full_position"))
REPLACEMENTS.append(('<div class="rp-kv sm"><span>?????</span><b>{{ lv.confidence', '<div class="rp-kv sm"><span>' + u(0x7F6E, 0x4FE1, 0x5EA6) + "</span><b>{{ lv.confidence"))
REPLACEMENTS.append(("<span>???(px?)</span>", "<span>" + u(0x9762, 0x79EF) + "(px\u00b2)</span>"))
REPLACEMENTS.append(('<div class="rp-kv sm"><span>????</span><b>{{ lv.width', '<div class="rp-kv sm"><span>' + u(0x6846, 0x5C3A, 0x5BF8) + "</span><b>{{ lv.width"))


def main() -> None:
    text = VUE.read_text(encoding="utf-8", errors="replace")

    for old, new in REPLACEMENTS:
        if old not in text:
            print("SKIP (not found):", old[:60].replace("\n", " "))
        else:
            text = text.replace(old, new, 1)

    # Remaining template placeholders (long unique strings)
    longs = [
        (
            '<div v-else class="rp-placeholder">?????AI ????????????????????????</div>',
            '<div v-else class="rp-placeholder">' + u(0x6267, 0x884C, 0x300C, 0x0041, 0x0049, 0x0020, 0x75C5, 0x7076, 0x5206, 0x6790, 0x300D, 0x540E, 0x5C55, 0x793A, 0x7ED3, 0x6784, 0x5316, 0x7ED3, 0x679C, 0x4E0E, 0x753B, 0x6846) + "</div>",
        ),
        (
            '<h3 class="rp-title">??????????????</h3>',
            "<h3 class=\"rp-title\">" + u(0x7535, 0x5B50, 0x75C5, 0x5386, 0xFF08, 0x53EA, 0x8BFB, 0xFF09) + "</h3>",
        ),
        (
            '<div v-else class="rp-placeholder">???????????????????????????</div>',
            "<div v-else class=\"rp-placeholder\">" + u(0x6682, 0x65E0, 0x7ED1, 0x5B9A, 0x75C5, 0x5386, 0xFF1B, 0x53EF, 0x70B9, 0x51FB, 0x300C, 0x5BFC, 0x5165, 0x75C5, 0x5386, 0x300D, 0x5F55, 0x5165) + "</div>",
        ),
        (
            '<h3 class="rp-title">?????????</h3>',
            "<h3 class=\"rp-title\">" + u(0x68C0, 0x9A8C, 0x5BF9, 0x7167) + "</h3>",
        ),
        ('<span class="lab-ref">???? {{ row.refText }}</span>', '<span class="lab-ref">' + u(0x53C2, 0x8003) + " {{ row.refText }}</span>"),
        (
            '<div v-else class="rp-placeholder">??????????????????????????</div>',
            "<div v-else class=\"rp-placeholder\">" + u(0x6682, 0x65E0, 0x68C0, 0x9A8C, 0x6570, 0x636E, 0xFF1B, 0x53EF, 0x70B9, 0x51FB, 0x300C, 0x5BFC, 0x5165, 0x68C0, 0x9A8C, 0x300D) + "</div>",
        ),
    ]
    for old, new in longs:
        if old in text:
            text = text.replace(old, new, 1)
        else:
            print("SKIP long:", old[:50])

    # Script: lesionTooltip
    text = text.replace(
        "const conf = l.confidence != null ? `????? ${(Number(l.confidence) * 100).toFixed(1)}%` : ''",
        "const conf = l.confidence != null ? `\u7f6e\u4fe1\u5ea6 ${(Number(l.confidence) * 100).toFixed(1)}%` : ''",
    )
    text = text.replace(
        "`?????? ${this.fmtPct(this.aiResult.infection_rate != null ? this.aiResult.infection_rate : this.aiResult.infectionRate)}`",
        "`\u611f\u67d3\u5360\u6bd4 ${this.fmtPct(this.aiResult.infection_rate != null ? this.aiResult.infection_rate : this.aiResult.infectionRate)}`",
    )
    text = text.replace(".join(' ?? ')", ".join(' \u00b7 ')")

    # recordPlainText
    text = text.replace("['????', r.chiefComplaint]", "['\u4e3b\u8bc9', r.chiefComplaint]")
    text = text.replace("['????', r.presentHistory]", "['\u73b0\u75c5\u53f2', r.presentHistory]")
    text = text.replace("['?????', r.pastHistory]", "['\u65e2\u5f80\u53f2', r.pastHistory]")
    text = text.replace("['?????', r.physicalExam]", "['\u4f53\u683c\u68c0\u67e5', r.physicalExam]")
    text = text.replace("['???????', r.initialDiagnosis]", "['\u521d\u6b65\u8bca\u65ad', r.initialDiagnosis]")
    text = text.replace("['???', r.remark]", "['\u5907\u6ce8', r.remark]")
    text = text.replace(".map(([k, v]) => `??${k}??\\n${v}`)", ".map(([k, v]) => `\u3010${k}\u3011\\n${v}`)")

    text = text.replace("if (v == null) return '??'", "if (v == null) return '\u2014'", 2)
    text = text.replace(
        "fmtNum(lv.width)+'??'+fmtNum(lv.height)",
        "fmtNum(lv.width)+'\u00d7'+fmtNum(lv.height)",
    )
    text = text.replace(
        "[lv.position, lv.lobe].filter(Boolean).join(' ') || '??'",
        "[lv.position, lv.lobe].filter(Boolean).join(' ') || '\u2014'",
    )
    text = text.replace("return `${p.patientName}??ID ${p.patientId}??`", "return `${p.patientName}\uff08ID ${p.patientId}\uff09`")
    text = text.replace("const m = { 0: '??', 1: '?', 2: '???' }", "const m = { 0: '\u7537', 1: '\u5973', 2: '\u672a\u77e5' }")
    text = text.replace("return m[g] != null ? m[g] : '??'", "return m[g] != null ? m[g] : '\u2014'")

    text = re.sub(
        r"const map = \{ 0: '[^']*', 1: '[^']*', 2: '[^']*', 3: '[^']*', 4: '[^']*' \}",
        "const map = { 0: '\u672a\u5f00\u59cb', 1: 'AI\u8bca\u65ad\u4e2d', 2: '\u8bca\u65ad\u5b8c\u6210', 3: '\u5f85\u5ba1\u6838', 4: '\u5df2\u751f\u6210\u62a5\u544a' }",
        text,
        count=1,
    )
    text = text.replace("return map[s] || '??'", "return map[s] || '\u2014'")

    # Garbled message.warning (any bad bytes between quotes)
    text = re.sub(
        r"this\.\$message\.warning\('[^']*'\)",
        "this.$message.warning('\u8bf7\u5148\u9009\u62e9\u60a3\u8005')",
        text,
    )

    VUE.write_text(text, encoding="utf-8", newline="\n")
    VUE.read_text(encoding="utf-8")
    print("OK", VUE)


if __name__ == "__main__":
    main()
