<template>
  <div class="app-container">
    <el-form :model="queryParams" ref="queryForm" size="small" :inline="true" v-show="showSearch" :label-width="labelWidth">
      <el-form-item :label="L.patientName" prop="patientName">
        <el-input v-model="queryParams.patientName" :placeholder="L.fuzzyPlaceholder" clearable @keyup.enter.native="handleQuery" style="width: 200px" />
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="el-icon-search" size="mini" @click="handleQuery">{{ L.search }}</el-button>
        <el-button icon="el-icon-refresh" size="mini" @click="resetQuery">{{ L.reset }}</el-button>
      </el-form-item>
    </el-form>

    <el-row :gutter="10" class="mb8">
      <right-toolbar :showSearch.sync="showSearch" @queryTable="getList" />
    </el-row>

    <el-alert
      :title="L.readonlyTip"
      type="info"
      :closable="false"
      show-icon
      class="mb8"
    />

    <el-table v-loading="loading" :data="dataList">
      <el-table-column :label="L.reportId" prop="reportId" width="80" align="center" />
      <el-table-column :label="L.patient" prop="patientName" min-width="100" show-overflow-tooltip />
      <el-table-column :label="L.image" prop="imageName" min-width="110" show-overflow-tooltip />
      <el-table-column :label="L.confirmGrade" min-width="130" show-overflow-tooltip>
        <template slot-scope="scope">{{ rowSummary(scope.row).confirmGrade }}</template>
      </el-table-column>
      <el-table-column :label="L.severityGrade" width="88" align="center">
        <template slot-scope="scope">{{ rowSummary(scope.row).severityGrade }}</template>
      </el-table-column>
      <el-table-column :label="L.fusionScore" width="88" align="center">
        <template slot-scope="scope">{{ rowSummary(scope.row).fusionTotalScore }}</template>
      </el-table-column>
      <el-table-column :label="L.pathogen" min-width="100" show-overflow-tooltip>
        <template slot-scope="scope">{{ rowSummary(scope.row).pathogenInference }}</template>
      </el-table-column>
      <el-table-column :label="L.signature" prop="doctorSignature" width="90" show-overflow-tooltip />
      <el-table-column :label="L.createdAt" prop="createTime" width="168" align="center">
        <template slot-scope="scope">{{ parseTime(scope.row.createTime) }}</template>
      </el-table-column>
      <el-table-column :label="L.actions" align="center" width="88" fixed="right">
        <template slot-scope="scope">
          <el-button size="mini" type="text" icon="el-icon-view" @click="handleView(scope.row)" v-hasPermi="['fusion:report:query']">{{ L.view }}</el-button>
        </template>
      </el-table-column>
    </el-table>

    <pagination v-show="total > 0" :total="total" :page.sync="queryParams.pageNum" :limit.sync="queryParams.pageSize" @pagination="getList" />

    <el-dialog :title="L.detailTitle" :visible.sync="viewOpen" width="960px" append-to-body destroy-on-close @closed="onViewClosed">
      <template v-if="detail">
        <el-descriptions :column="2" border size="small" class="mb12">
          <el-descriptions-item :label="L.reportId">{{ detail.reportId }}</el-descriptions-item>
          <el-descriptions-item :label="L.patientIdLabel">{{ detail.patientId }}</el-descriptions-item>
          <el-descriptions-item :label="L.imageIdLabel">{{ detail.imageId }}</el-descriptions-item>
          <el-descriptions-item :label="L.imageName">{{ detail.imageName || L.dash }}</el-descriptions-item>
          <el-descriptions-item :label="L.recordId">{{ detail.medicalRecordId || L.dash }}</el-descriptions-item>
          <el-descriptions-item :label="L.labId">{{ detail.labResultId || L.dash }}</el-descriptions-item>
          <el-descriptions-item :label="L.doctorSignature">{{ detail.doctorSignature || L.dash }}</el-descriptions-item>
          <el-descriptions-item :label="L.doctorAdvice">{{ detail.doctorAdvice || L.dash }}</el-descriptions-item>
        </el-descriptions>

        <el-alert v-if="!fusionPayload && detail.fusionResponseJson" type="warning" :closable="false" show-icon class="mb12"
          title="无法解析为新版融合结构，请查看底部原始 JSON；旧版字段已尝试回退展示。" />

        <el-collapse v-model="detailCollapse">
          <el-collapse-item :title="L.fusionResult" name="fusion">
            <el-collapse v-model="fusionCollapse">
              <el-collapse-item :title="L.modalScore" name="ms">
                <el-descriptions v-if="modalScore" :column="1" border size="small">
                  <el-descriptions-item :label="L.imageScore">{{ fusionPick(modalScore, 'image_score', 'imageScore') }}/10</el-descriptions-item>
                  <el-descriptions-item :label="L.caseScore">{{ fusionPick(modalScore, 'case_score', 'caseScore') }}/10</el-descriptions-item>
                  <el-descriptions-item :label="L.labScore">{{ fusionPick(modalScore, 'lab_score', 'labScore') }}/10</el-descriptions-item>
                  <el-descriptions-item :label="L.fusionTotal">{{ fusionPick(modalScore, 'fusion_total_score', 'fusionTotalScore') }}</el-descriptions-item>
                  <el-descriptions-item :label="L.confidence">{{ fusionPick(modalScore, 'confidence_coefficient', 'confidenceCoefficient') }}</el-descriptions-item>
                </el-descriptions>
                <el-descriptions v-else-if="legacyScores" :column="1" border size="small">
                  <el-descriptions-item v-if="legacyScores.image" :label="L.imageMod">{{ legacyScores.image.score }}/10</el-descriptions-item>
                  <el-descriptions-item v-if="legacyScores.case" :label="L.caseMod">{{ legacyScores.case.score }}/10</el-descriptions-item>
                  <el-descriptions-item v-if="legacyScores.lab" :label="L.labMod">{{ legacyScores.lab.score }}/10</el-descriptions-item>
                </el-descriptions>
                <span v-else>{{ L.none }}</span>
              </el-collapse-item>
              <el-collapse-item :title="L.diagnosisBlock" name="dg">
                <template v-if="fusionDiagnosisResult">
                  <p><strong>{{ L.confirmGradeLabel }}</strong>{{ fusionPick(fusionDiagnosisResult, 'confirm_grade', 'confirmGrade') }}</p>
                  <p><strong>{{ L.severityLabel }}</strong>{{ fusionPick(fusionDiagnosisResult, 'severity_grade', 'severityGrade') }}</p>
                  <p><strong>{{ L.pathogenLabel }}</strong>{{ fusionPick(fusionDiagnosisResult, 'pathogen_inference', 'pathogenInference') }}</p>
                </template>
                <template v-else-if="legacyDiagnosis">
                  <p>{{ L.legacyGrade }}：{{ legacyDiagnosis.grade_cn || legacyDiagnosis.gradeCn || legacyDiagnosis.grade || L.dash }}</p>
                  <p>{{ L.legacySeverity }}：{{ legacyDiagnosis.severity_cn || legacyDiagnosis.severityCn || legacyDiagnosis.severity || L.dash }}</p>
                  <p>{{ L.legacyAction }}：{{ legacyDiagnosis.action || L.dash }}</p>
                </template>
                <span v-else>{{ L.none }}</span>
              </el-collapse-item>
              <el-collapse-item :title="L.conflictWarn" name="warn">
                <el-table v-if="conflictWarnings.length" :data="conflictWarnings" size="mini" border>
                  <el-table-column :label="L.ruleCol" width="72" align="center">
                    <template slot-scope="scope">{{ scope.row.rule_id || scope.row.ruleId }}</template>
                  </el-table-column>
                  <el-table-column :label="L.levelCol" width="72" align="center">
                    <template slot-scope="scope">
                      <el-tag size="mini" :type="warningLevelTag(scope.row.warning_level || scope.row.warningLevel)">
                        {{ scope.row.warning_level || scope.row.warningLevel }}
                      </el-tag>
                    </template>
                  </el-table-column>
                  <el-table-column :label="L.warnContent" min-width="280" show-overflow-tooltip>
                    <template slot-scope="scope">{{ scope.row.warning_content || scope.row.warningContent }}</template>
                  </el-table-column>
                </el-table>
                <div v-else-if="legacyConsistency">
                  <p><strong>{{ L.levelCol }}：</strong>{{ legacyConsistency.level || L.dash }}</p>
                  <ul v-if="legacyConflictRules.length" class="fusion-ul">
                    <li v-for="(cr, idx) in legacyConflictRules" :key="idx">
                      <strong>{{ cr.rule_name || cr.ruleName }}</strong>：{{ cr.report_text || cr.reportText }}
                    </li>
                  </ul>
                </div>
                <span v-else>{{ L.noWarn }}</span>
              </el-collapse-item>
              <el-collapse-item :title="L.fusionAdvice" name="advice">
                <div v-if="fusionAdviceSections.length" class="advice-wrap">
                  <div v-for="(sec, si) in fusionAdviceSections" :key="si" class="advice-sec">
                    <h4 class="advice-title">{{ sec.title }}</h4>
                    <div v-for="(item, ii) in sec.items" :key="ii" class="advice-item">
                      <p v-if="item.label"><strong>{{ item.label }}：</strong>{{ item.text }}</p>
                      <p v-else>{{ item.text }}</p>
                    </div>
                  </div>
                </div>
                <ol v-else-if="legacySuggestions.length" class="fusion-ul">
                  <li v-for="(s, i) in legacySuggestions" :key="i">
                    <el-tag size="mini" :type="s.priority === 'P0' ? 'danger' : (s.priority === 'P1' ? 'warning' : 'info')">{{ s.priority }}</el-tag>
                    {{ s.category }} — {{ s.content }}
                  </li>
                </ol>
                <span v-else>{{ L.none }}</span>
              </el-collapse-item>
            </el-collapse>
          </el-collapse-item>

          <el-collapse-item :title="L.imageInput" name="image">
            <p v-if="imageSummary" class="image-summary">
              {{ L.lesionCount }}：{{ imageSummary.lesionCount != null ? imageSummary.lesionCount : L.dash }}
              ；{{ L.infectionRate }}：{{ imageSummary.infectionRate != null ? imageSummary.infectionRate + '%' : L.dash }}
              ；{{ L.detectSummary }}：{{ imageSummary.diagnosis || L.dash }}
              ；{{ L.hasDiagReport }}：{{ imageSummary.hasReport ? L.yes : L.no }}
            </p>
            <pre class="mono-pre">{{ prettyJson(detail.imageResultJson) }}</pre>
          </el-collapse-item>

          <el-collapse-item :title="L.caseSnapshot" name="case">
            <pre class="mono-pre">{{ detail.caseText || L.dash }}</pre>
          </el-collapse-item>

          <el-collapse-item :title="L.labSnapshot" name="lab">
            <pre class="mono-pre">{{ prettyJson(detail.labDataJson) }}</pre>
          </el-collapse-item>

          <el-collapse-item :title="L.rawFusionJson" name="raw">
            <pre class="mono-pre">{{ prettyJson(detail.fusionResponseJson) }}</pre>
          </el-collapse-item>
        </el-collapse>
      </template>
      <div slot="footer">
        <el-button @click="viewOpen = false">{{ L.close }}</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import { listFusionReport, getFusionReport } from '@/api/medical/fusionReport'
import {
  unwrapFusionPayload,
  pick,
  buildFusionAdviceSections,
  conflictWarningsFromPayload,
  extractFusionListSummary,
  warningLevelTag,
  imageResultSummary,
  parseJsonMaybe,
  legacyModalityScores,
  legacyDiagnosisOutput,
  legacyStructuredSuggestions,
  legacyConsistencyCheck
} from '@/utils/fusionReportParse'

const L = {
  patientName: '患者姓名',
  fuzzyPlaceholder: '模糊查询',
  search: '搜索',
  reset: '重置',
  readonlyTip: '融合分析报告仅在阅片器执行「辅助诊断（融合）」后自动写入或覆盖，本页仅支持查询与查看详情，不支持修改或删除。',
  reportId: '报告ID',
  patient: '患者',
  image: '影像',
  confirmGrade: '确诊分级',
  severityGrade: '严重程度',
  fusionScore: '融合总分',
  pathogen: '病原推断',
  signature: '签名',
  createdAt: '创建时间',
  actions: '操作',
  view: '详情',
  detailTitle: '融合分析报告详情',
  patientIdLabel: '患者ID',
  imageIdLabel: '胸片ID',
  imageName: '图片名',
  recordId: '病历ID',
  labId: '检验ID',
  dash: '—',
  fusionResult: '融合决策结果',
  modalScore: '模态评分',
  imageScore: '影像评分',
  caseScore: '病历评分',
  labScore: '检验评分',
  fusionTotal: '融合总分',
  confidence: '置信系数',
  imageMod: '影像',
  caseMod: '病历',
  labMod: '检验',
  diagnosisBlock: '诊断结果',
  confirmGradeLabel: '确诊分级：',
  severityLabel: '严重程度：',
  pathogenLabel: '病原体推断：',
  legacyGrade: '确诊度',
  legacySeverity: '严重程度',
  legacyAction: '处置',
  conflictWarn: '冲突预警',
  ruleCol: '规则',
  levelCol: '级别',
  warnContent: '预警内容',
  noWarn: '暂无预警',
  fusionAdvice: '融合诊疗建议',
  imageInput: '影像 AI 入参（YOLO + 胸片报告）',
  lesionCount: '病灶数',
  infectionRate: '感染率',
  detectSummary: '检测摘要',
  hasDiagReport: '含诊断报告',
  yes: '是',
  no: '否',
  caseSnapshot: '病历文本快照',
  labSnapshot: '检验数据快照',
  rawFusionJson: '原始融合 JSON',
  close: '关 闭',
  none: '暂无',
  doctorSignature: '医生签名',
  doctorAdvice: '医生建议'
}

export default {
  name: 'FusionAnalysisReport',
  data() {
    return {
      L,
      labelWidth: '100px',
      loading: true,
      showSearch: true,
      total: 0,
      dataList: [],
      queryParams: {
        pageNum: 1,
        pageSize: 10,
        patientName: undefined
      },
      viewOpen: false,
      detail: null,
      fusionPayload: null,
      detailCollapse: ['fusion', 'image', 'case', 'lab'],
      fusionCollapse: ['ms', 'dg', 'warn', 'advice']
    }
  },
  computed: {
    modalScore() {
      const p = this.fusionPayload
      return p ? (p.modal_score || p.modalScore) : null
    },
    legacyScores() {
      return legacyModalityScores(this.fusionPayload)
    },
    fusionDiagnosisResult() {
      const p = this.fusionPayload
      return p ? (p.diagnosis_result || p.diagnosisResult) : null
    },
    legacyDiagnosis() {
      return legacyDiagnosisOutput(this.fusionPayload)
    },
    conflictWarnings() {
      return conflictWarningsFromPayload(this.fusionPayload)
    },
    legacyConsistency() {
      return legacyConsistencyCheck(this.fusionPayload)
    },
    legacyConflictRules() {
      const cc = this.legacyConsistency
      if (!cc) return []
      const r = cc.conflict_rules || cc.conflictRules
      return Array.isArray(r) ? r : []
    },
    fusionAdviceSections() {
      return buildFusionAdviceSections(this.fusionPayload)
    },
    legacySuggestions() {
      return legacyStructuredSuggestions(this.fusionPayload)
    },
    imageSummary() {
      return this.detail ? imageResultSummary(this.detail.imageResultJson) : null
    }
  },
  created() {
    this.getList()
  },
  methods: {
    warningLevelTag,
    rowSummary(row) {
      return row._summary || extractFusionListSummary(row.fusionResponseJson)
    },
    fusionPick(obj, snakeKey, camelKey) {
      const v = pick(obj, snakeKey, camelKey)
      return v !== '' ? v : this.L.dash
    },
    getList() {
      this.loading = true
      listFusionReport(this.queryParams).then(res => {
        const rows = res.rows || []
        this.dataList = rows.map(row => ({
          ...row,
          _summary: extractFusionListSummary(row.fusionResponseJson)
        }))
        this.total = res.total || 0
        this.loading = false
      }).catch(() => { this.loading = false })
    },
    handleQuery() {
      this.queryParams.pageNum = 1
      this.getList()
    },
    resetQuery() {
      this.resetForm('queryForm')
      this.handleQuery()
    },
    handleView(row) {
      getFusionReport(row.reportId).then(res => {
        this.detail = res.data || null
        this.fusionPayload = unwrapFusionPayload(this.detail && this.detail.fusionResponseJson)
        this.viewOpen = true
      })
    },
    onViewClosed() {
      this.detail = null
      this.fusionPayload = null
    },
    prettyJson(raw) {
      const o = parseJsonMaybe(raw)
      if (!o) return this.L.dash
      return JSON.stringify(o, null, 2)
    }
  }
}
</script>

<style scoped>
.mb12 { margin-bottom: 12px; }
.image-summary { font-size: 13px; color: #606266; margin-bottom: 8px; }
.mono-pre {
  max-height: 280px;
  overflow: auto;
  background: #f5f7fa;
  padding: 10px;
  font-size: 12px;
  white-space: pre-wrap;
  word-break: break-word;
  border-radius: 6px;
  border: 1px solid #ebeef5;
}
.advice-wrap { max-height: 360px; overflow: auto; }
.advice-title { margin: 10px 0 6px; font-size: 13px; font-weight: 600; color: #303133; }
.advice-item { margin-bottom: 6px; font-size: 12px; line-height: 1.55; color: #606266; }
.fusion-ul { margin: 6px 0; padding-left: 20px; font-size: 12px; color: #606266; }
</style>
