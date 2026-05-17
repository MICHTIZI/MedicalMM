# -*- coding: utf-8 -*-
"""Rewrite AI analysis SQL and Vue with correct UTF-8 (source uses \\u escapes)."""
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]


def write_table_sql():
    lines = [
        "-- AI \u75c5\u7076\u5206\u6790\u7ed3\u679c\u6301\u4e45\u5316\uff08\u7ed1\u5b9a chest_xray\uff0c\u6bcf\u6b21\u5206\u6790\u65b0\u589e\u4e00\u6761\uff09",
        "-- UTF-8 utf8mb4\uff1b\u8bf7\u7528 mysql --default-character-set=utf8mb4 \u6267\u884c\u3002",
        "SET NAMES utf8mb4 COLLATE utf8mb4_unicode_ci;",
        "",
        "USE medical_db;",
        "",
        "CREATE TABLE IF NOT EXISTS ai_image_analysis_result (",
        "    analysis_id BIGINT AUTO_INCREMENT COMMENT '\u5206\u6790\u8bb0\u5f55ID' PRIMARY KEY,",
        "    image_id BIGINT NOT NULL COMMENT '\u80f8\u7247ID chest_xray.id',",
        "    patient_id BIGINT NULL COMMENT '\u60a3\u8005ID',",
        "    lesion_count INT DEFAULT 0 NULL COMMENT '\u75c5\u7076\u6570\u91cf',",
        "    infection_rate DECIMAL(8, 2) NULL COMMENT '\u611f\u67d3\u7387(%)',",
        "    total_infection_area DECIMAL(16, 2) NULL COMMENT '\u611f\u67d3\u533a\u57df\u9762\u79ef(px\u00b2)',",
        "    detect_diagnosis VARCHAR(512) NULL COMMENT 'YOLO\u68c0\u6d4b\u6458\u8981 diagnosis',",
        "    ai_result_path VARCHAR(512) NULL COMMENT 'AI\u6807\u6ce8\u56feMinIO\u8def\u5f84',",
        "    detect_result_json LONGTEXT NOT NULL COMMENT 'YOLO\u68c0\u6d4b\u56fa\u5b9a\u7ed3\u6784JSON',",
        "    diagnosis_report_raw LONGTEXT NULL COMMENT '\u62a5\u544a\u539f\u6587 diagnosis_report',",
        "    diagnosis_report_json LONGTEXT NULL COMMENT '\u62a5\u544a\u7ed3\u6784\u5316JSON',",
        "    report_create_time VARCHAR(32) NULL COMMENT '\u62a5\u544a\u751f\u6210\u65f6\u95f4',",
        "    create_by VARCHAR(64) DEFAULT '' NULL,",
        "    create_time DATETIME DEFAULT CURRENT_TIMESTAMP NULL,",
        "    is_deleted TINYINT DEFAULT 0 NOT NULL,",
        "    INDEX idx_image_id (image_id),",
        "    INDEX idx_patient_id (patient_id),",
        "    INDEX idx_create_time (create_time)",
        ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='AI\u75c5\u7076\u5206\u6790\u7ed3\u679c';",
        "",
    ]
    path = ROOT / "sql" / "ai_image_analysis_result_table.sql"
    path.write_text("\n".join(lines), encoding="utf-8")
    print("wrote", path)


def write_menu_sql():
    lines = [
        "-- \u56fe\u50cf\u5206\u6790\u7ba1\u7406\u83dc\u5355\uff1a\u6302\u5728\u300cAI\u5f71\u50cf\u5206\u6790\u300d(menu_id=3240) \u4e0b\u3002",
        "-- UTF-8 utf8mb4\uff1b\u8bf7\u7528 mysql --default-character-set=utf8mb4 \u6267\u884c\u3002",
        "-- \u4f9d\u8d56\uff1a\u5df2\u6267\u884c medical_ai_image_ry_menu_zh.sql \u3002",
        "USE `ry-cloud`;",
        "",
        "DELETE FROM sys_role_menu WHERE menu_id BETWEEN 3245 AND 3249;",
        "DELETE FROM sys_menu WHERE menu_id BETWEEN 3245 AND 3249;",
        "",
        "INSERT INTO sys_menu VALUES (3245, '\u56fe\u50cf\u5206\u6790\u7ba1\u7406', 3240, 2, 'analysis-result', 'medical/ai-analysis-result/index', '', '', 1, 0, 'C', '0', '0', 'ai:analysis:result:list', 'documentation', 'admin', sysdate(), '', NULL, 'AI\u75c5\u7076\u5206\u6790\u7ed3\u679c\u7ba1\u7406');",
        "INSERT INTO sys_menu VALUES (3246, '\u5206\u6790\u7ed3\u679c\u67e5\u8be2', 3245, 1, '', '', '', '', 1, 0, 'F', '0', '0', 'ai:analysis:result:query', '#', 'admin', sysdate(), '', NULL, '');",
        "INSERT INTO sys_menu VALUES (3247, '\u5206\u6790\u7ed3\u679c\u5220\u9664', 3245, 2, '', '', '', '', 1, 0, 'F', '0', '0', 'ai:analysis:result:remove', '#', 'admin', sysdate(), '', NULL, '');",
        "",
        "INSERT INTO sys_role_menu VALUES (2, 3245);",
        "INSERT INTO sys_role_menu VALUES (2, 3246);",
        "INSERT INTO sys_role_menu VALUES (2, 3247);",
        "",
        "INSERT INTO sys_role_menu VALUES (100, 3245);",
        "INSERT INTO sys_role_menu VALUES (100, 3246);",
        "INSERT INTO sys_role_menu VALUES (100, 3247);",
        "",
    ]
    path = ROOT / "sql" / "ai_image_analysis_result_menu_zh.sql"
    path.write_text("\n".join(lines), encoding="utf-8")
    print("wrote", path)


def write_vue():
    d = "motion"
    d = chr(100) + chr(105) + chr(118)
    T = []
    a = T.append
    a("<template>")
    a(f'  <{d} class="app-container">')
    a('    <el-form :model="queryParams" ref="queryForm" size="small" :inline="true" v-show="showSearch" label-width="90px">')
    a('      <el-form-item label="\u60a3\u8005\u59d3\u540d" prop="patientName">')
    a('        <el-input v-model="queryParams.patientName" placeholder="\u6a21\u7cca\u67e5\u8be2" clearable @keyup.enter.native="handleQuery" />')
    a('      </el-form-item>')
    a('      <el-form-item label="\u80f8\u7247ID" prop="imageId">')
    a('        <el-input v-model="queryParams.imageId" placeholder="\u5f71\u50cfID" clearable @keyup.enter.native="handleQuery" />')
    a('      </el-form-item>')
    a('      <el-form-item label="\u56fe\u7247\u540d" prop="imageName">')
    a('        <el-input v-model="queryParams.imageName" placeholder="\u6a21\u7cca\u67e5\u8be2" clearable @keyup.enter.native="handleQuery" />')
    a('      </el-form-item>')
    a('      <el-form-item>')
    a('        <el-button type="primary" icon="el-icon-search" size="mini" @click="handleQuery">\u641c\u7d22</el-button>')
    a('        <el-button icon="el-icon-refresh" size="mini" @click="resetQuery">\u91cd\u7f6e</el-button>')
    a('      </el-form-item>')
    a('    </el-form>')
    a('')
    a('    <el-row :gutter="10" class="mb8">')
    a('      <el-col :span="1.5">')
    a('        <el-button type="danger" plain icon="el-icon-delete" size="mini" :disabled="multiple" @click="handleDelete()" v-hasPermi="[\'ai:analysis:result:remove\']">\u5220\u9664</el-button>')
    a('      </el-col>')
    a('      <right-toolbar :showSearch.sync="showSearch" @queryTable="getList" />')
    a('    </el-row>')
    a('')
    a('    <el-alert')
    a('      title="\u5206\u6790\u8bb0\u5f55\u4ec5\u5728\u9605\u7247\u5668\u6216 AI \u5206\u6790\u5217\u8868\u6267\u884c\u300cAI \u75c5\u7076\u5206\u6790\u300d\u540e\u81ea\u52a8\u65b0\u589e\uff0c\u672c\u9875\u4e0d\u652f\u6301\u624b\u5de5\u65b0\u589e\u6216\u4fee\u6539\u3002"')
    a('      type="info"')
    a('      :closable="false"')
    a('      show-icon')
    a('      class="mb8"')
    a('    />')
    a('')
    a('    <el-table v-loading="loading" :data="dataList" @selection-change="handleSelectionChange">')
    a('      <el-table-column type="selection" width="55" align="center" />')
    a('      <el-table-column label="\u8bb0\u5f55ID" prop="analysisId" width="90" align="center" />')
    a('      <el-table-column label="\u80f8\u7247ID" prop="imageId" width="90" align="center" />')
    a('      <el-table-column label="\u60a3\u8005" prop="patientName" min-width="100" show-overflow-tooltip />')
    a('      <el-table-column label="\u56fe\u7247\u540d" prop="imageName" min-width="140" show-overflow-tooltip />')
    a('      <el-table-column label="\u75c5\u7076\u6570" prop="lesionCount" width="80" align="center" />')
    a('      <el-table-column label="\u611f\u67d3\u7387" width="90" align="center">')
    a('        <template slot-scope="scope">{{ formatRate(scope.row.infectionRate) }}</template>')
    a('      </el-table-column>')
    a('      <el-table-column label="\u68c0\u6d4b\u6458\u8981" prop="detectDiagnosis" min-width="180" show-overflow-tooltip />')
    a('      <el-table-column label="\u62a5\u544a\u65f6\u95f4" prop="reportCreateTime" width="160" align="center" />')
    a('      <el-table-column label="\u5165\u5e93\u65f6\u95f4" prop="createTime" width="168" align="center">')
    a('        <template slot-scope="scope">{{ parseTime(scope.row.createTime) }}</template>')
    a('      </el-table-column>')
    a('      <el-table-column label="\u64cd\u4f5c" align="center" width="140" class-name="small-padding fixed-width">')
    a('        <template slot-scope="scope">')
    a('          <el-button size="mini" type="text" icon="el-icon-view" @click="handleView(scope.row)" v-hasPermi="[\'ai:analysis:result:query\']">\u8be6\u60c5</el-button>')
    a('          <el-button size="mini" type="text" icon="el-icon-delete" @click="handleDelete(scope.row)" v-hasPermi="[\'ai:analysis:result:remove\']">\u5220\u9664</el-button>')
    a('        </template>')
    a('      </el-table-column>')
    a('    </el-table>')
    a('')
    a('    <pagination v-show="total > 0" :total="total" :page.sync="queryParams.pageNum" :limit.sync="queryParams.pageSize" @pagination="getList" />')
    a('')
    a('    <el-dialog title="\u5206\u6790\u7ed3\u679c\u8be6\u60c5" :visible.sync="viewOpen" width="920px" append-to-body destroy-on-close>')
    a('      <template v-if="detail">')
    a('        <el-descriptions :column="2" border size="small" class="mb12">')
    a('          <el-descriptions-item label="\u8bb0\u5f55ID">{{ detail.analysisId }}</el-descriptions-item>')
    a('          <el-descriptions-item label="\u80f8\u7247ID">{{ detail.imageId }}</el-descriptions-item>')
    a('          <el-descriptions-item label="\u60a3\u8005">{{ detail.patientName || dash }}</el-descriptions-item>')
    a('          <el-descriptions-item label="\u56fe\u7247\u540d">{{ detail.imageName || dash }}</el-descriptions-item>')
    a('          <el-descriptions-item label="\u75c5\u7076\u6570\u91cf">{{ detail.lesionCount != null ? detail.lesionCount : dash }}</el-descriptions-item>')
    a('          <el-descriptions-item label="\u611f\u67d3\u7387">{{ formatRate(detail.infectionRate) }}</el-descriptions-item>')
    a('          <el-descriptions-item label="\u611f\u67d3\u9762\u79ef(px\u00b2)">{{ detail.totalInfectionArea != null ? detail.totalInfectionArea : dash }}</el-descriptions-item>')
    a('          <el-descriptions-item label="\u62a5\u544a\u751f\u6210\u65f6\u95f4">{{ detail.reportCreateTime || dash }}</el-descriptions-item>')
    a('          <el-descriptions-item label="\u68c0\u6d4b\u6458\u8981" :span="2">{{ detail.detectDiagnosis || dash }}</el-descriptions-item>')
    a('        </el-descriptions>')
    a('')
    a('        <h4 class="detail-section-title">\u4e00\u3001\u75c5\u7076\u68c0\u6d4b\uff08YOLO\uff09</h4>')
    a('        <pre class="mono-pre">{{ prettyJson(detail.detectResultJson) }}</pre>')
    a('        <el-divider />')
    a('        <h4 class="detail-section-title">\u4e8c\u3001AI \u5f71\u50cf\u8bca\u65ad\u62a5\u544a\uff08\u7ed3\u6784\u5316\uff09</h4>')
    a('        <template v-if="reportStruct">')
    a('          <el-card shadow="never" class="struct-card">')
    a(f'            <{d} class="struct-block">')
    a('              <h5>\u8bca\u65ad\u7ed3\u8bba</h5>')
    a('              <p v-if="reportStruct.conclusion && reportStruct.conclusion.mainDiagnosis"><b>\u4e3b\u8981\u8bca\u65ad\uff1a</b>{{ reportStruct.conclusion.mainDiagnosis }}</p>')
    a('              <p v-if="reportStruct.conclusion && reportStruct.conclusion.confidence"><b>\u8bca\u65ad\u7f6e\u4fe1\u5ea6\uff1a</b>{{ reportStruct.conclusion.confidence }}</p>')
    a('              <p v-if="reportStruct.conclusion && reportStruct.conclusion.differentialDiagnosis"><b>\u9274\u522b\u8bca\u65ad\uff1a</b>{{ reportStruct.conclusion.differentialDiagnosis }}</p>')
    a(f'            </{d}>')
    a(f'            <{d} class="struct-block" v-if="hasList(reportStruct.imagingAnalysis && reportStruct.imagingAnalysis.lesionFeatures)">')
    a('              <h5>\u5f71\u50cf\u5b66\u5206\u6790 \u00b7 \u75c5\u7076\u7279\u5f81</h5>')
    a('              <ul><li v-for="(t, i) in reportStruct.imagingAnalysis.lesionFeatures" :key="\'lf\'+i">{{ t }}</li></ul>')
    a(f'            </{d}>')
    a(f'            <{d} class="struct-block" v-if="hasList(reportStruct.imagingAnalysis && reportStruct.imagingAnalysis.overallLungAssessment)">')
    a('              <h5>\u5f71\u50cf\u5b66\u5206\u6790 \u00b7 \u6574\u4f53\u80ba\u90e8\u8bc4\u4f30</h5>')
    a('              <ul><li v-for="(t, i) in reportStruct.imagingAnalysis.overallLungAssessment" :key="\'ol\'+i">{{ t }}</li></ul>')
    a(f'            </{d}>')
    a(f'            <{d} class="struct-block" v-if="hasList(reportStruct.imagingAnalysis && reportStruct.imagingAnalysis.otherFeatures)">')
    a('              <h5>\u5f71\u50cf\u5b66\u5206\u6790 \u00b7 \u5176\u4ed6\u7279\u5f81</h5>')
    a('              <ul><li v-for="(t, i) in reportStruct.imagingAnalysis.otherFeatures" :key="\'of\'+i">{{ t }}</li></ul>')
    a(f'            </{d}>')
    a(f'            <{d} class="struct-block" v-if="hasList(reportStruct.clinicalSignificance && reportStruct.clinicalSignificance.lesionNatureAnalysis)">')
    a('              <h5>\u4e34\u5e8a\u610f\u4e49 \u00b7 \u75c5\u53d8\u6027\u8d28\u5206\u6790</h5>')
    a('              <ul><li v-for="(t, i) in reportStruct.clinicalSignificance.lesionNatureAnalysis" :key="\'cn\'+i">{{ t }}</li></ul>')
    a(f'            </{d}>')
    a(f'            <{d} class="struct-block" v-if="riskEntries.length">')
    a('              <h5>\u4e34\u5e8a\u610f\u4e49 \u00b7 \u4e34\u5e8a\u98ce\u9669\u8bc4\u4f30</h5>')
    a('              <p v-for="item in riskEntries" :key="item.key"><b>{{ item.key }}\uff1a</b>{{ item.value }}</p>')
    a(f'            </{d}>')
    a(f'            <{d} class="struct-block" v-if="reportStruct.severityAssessment && (reportStruct.severityAssessment.severityLevel || reportStruct.severityAssessment.basis)">')
    a('              <h5>\u4e25\u91cd\u7a0b\u5ea6\u8bc4\u4f30</h5>')
    a('              <p v-if="reportStruct.severityAssessment.severityLevel"><b>\u5206\u7ea7\uff1a</b>{{ reportStruct.severityAssessment.severityLevel }}</p>')
    a('              <p v-if="reportStruct.severityAssessment.basis"><b>\u4f9d\u636e\uff1a</b>{{ reportStruct.severityAssessment.basis }}</p>')
    a(f'            </{d}>')
    a(f'            <{d} class="struct-block" v-if="reportStruct.summary">')
    a('              <h5>\u603b\u7ed3</h5>')
    a('              <p>{{ reportStruct.summary }}</p>')
    a(f'            </{d}>')
    a('          </el-card>')
    a('        </template>')
    a('        <el-empty v-else description="\u65e0\u7ed3\u6784\u5316\u62a5\u544a\u6570\u636e" :image-size="48" />')
    a('        <el-divider />')
    a('        <h4 class="detail-section-title">\u62a5\u544a\u539f\u6587</h4>')
    a('        <pre class="mono-pre report-raw">{{ detail.diagnosisReportRaw || dash }}</pre>')
    a('      </template>')
    a('    </el-dialog>')
    a(f'  </{d}>')
    a('</template>')
    a('')
    a('<script>')
    a("import { listAiAnalysisResult, getAiAnalysisResult, delAiAnalysisResult } from '@/api/medical/aiAnalysisResult'")
    a('')
    a('export default {')
    a("  name: 'AiAnalysisResultManage',")
    a('  data() {')
    a('    return {')
    a('      dash: "\u2014",')
    a('      loading: true,')
    a('      showSearch: true,')
    a('      total: 0,')
    a('      dataList: [],')
    a('      ids: [],')
    a('      multiple: true,')
    a('      queryParams: { pageNum: 1, pageSize: 10, patientName: undefined, imageId: undefined, imageName: undefined },')
    a('      viewOpen: false,')
    a('      detail: null,')
    a('      reportStruct: null')
    a('    }')
    a('  },')
    a('  computed: {')
    a('    riskEntries() {')
    a('      const map = this.reportStruct && this.reportStruct.clinicalSignificance && this.reportStruct.clinicalSignificance.riskAssessment')
    a("      if (!map || typeof map !== 'object') return []")
    a('      return Object.keys(map).map(k => ({ key: k, value: map[k] }))')
    a('    }')
    a('  },')
    a('  created() { this.getList() },')
    a('  methods: {')
    a('    getList() {')
    a('      this.loading = true')
    a('      listAiAnalysisResult(this.queryParams).then(res => {')
    a('        this.dataList = res.rows || []')
    a('        this.total = res.total || 0')
    a('        this.loading = false')
    a('      }).catch(() => { this.loading = false })')
    a('    },')
    a('    handleQuery() { this.queryParams.pageNum = 1; this.getList() },')
    a("    resetQuery() { this.resetForm('queryForm'); this.handleQuery() },")
    a('    handleSelectionChange(selection) {')
    a('      this.ids = selection.map(item => item.analysisId)')
    a('      this.multiple = !selection.length')
    a('    },')
    a('    handleView(row) {')
    a('      getAiAnalysisResult(row.analysisId).then(res => {')
    a('        this.detail = res.data || res')
    a('        this.reportStruct = null')
    a('        if (this.detail && this.detail.diagnosisReportJson) {')
    a('          try { this.reportStruct = JSON.parse(this.detail.diagnosisReportJson) } catch (e) { this.reportStruct = null }')
    a('        }')
    a('        this.viewOpen = true')
    a('      })')
    a('    },')
    a('    handleDelete(row) {')
    a('      const analysisIds = row && row.analysisId ? [row.analysisId] : this.ids')
    a('      if (!analysisIds.length) return')
    a('      this.$modal.confirm("\u662f\u5426\u786e\u8ba4\u5220\u9664\u9009\u4e2d\u7684\u5206\u6790\u8bb0\u5f55\uff1f\u5220\u9664\u540e\u4e0d\u53ef\u6062\u590d\u3002").then(() => {')
    a('        return delAiAnalysisResult(analysisIds.join(","))')
    a('      }).then(() => {')
    a('        this.getList()')
    a('        this.$modal.msgSuccess("\u5220\u9664\u6210\u529f")')
    a('      }).catch(() => {})')
    a('    },')
    a("    formatRate(val) { if (val == null || val === '') return this.dash; const n = Number(val); return Number.isNaN(n) ? val : n + '%' },")
    a('    prettyJson(str) {')
    a('      if (!str) return this.dash')
    a('      try { return JSON.stringify(JSON.parse(str), null, 2) } catch (e) { return str }')
    a('    },')
    a('    hasList(arr) { return Array.isArray(arr) && arr.length > 0 }')
    a('  }')
    a('}')
    a('</script>')
    a('')
    a('<style scoped>')
    a('.mb12 { margin-bottom: 12px; }')
    a('.mb8 { margin-bottom: 8px; }')
    a('.detail-section-title { margin: 12px 0 8px; font-size: 14px; font-weight: 600; color: #303133; }')
    a('.mono-pre { margin: 0; padding: 10px; background: #fafafa; border: 1px solid #ebeef5; border-radius: 6px; font-size: 12px; line-height: 1.55; white-space: pre-wrap; word-break: break-word; max-height: 240px; overflow: auto; }')
    a('.report-raw { max-height: 320px; }')
    a('.struct-card { background: #fcfcfc; }')
    a('.struct-block { margin-bottom: 12px; }')
    a('.struct-block h5 { margin: 0 0 6px; font-size: 13px; color: #606266; }')
    a('.struct-block p, .struct-block li { margin: 4px 0; font-size: 13px; line-height: 1.55; color: #303133; }')
    a('.struct-block ul { margin: 0; padding-left: 18px; }')
    a('</style>')
    a('')

    path = ROOT / "ruoyi-ui/src/views/medical/ai-analysis-result/index.vue"
    path.parent.mkdir(parents=True, exist_ok=True)
    path.write_text("\n".join(T), encoding="utf-8")
    print("wrote", path)


if __name__ == "__main__":
    write_table_sql()
    write_menu_sql()
    write_vue()
