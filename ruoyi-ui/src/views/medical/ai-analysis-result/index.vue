<template>
  <div class="app-container">
    <el-form :model="queryParams" ref="queryForm" size="small" :inline="true" v-show="showSearch" label-width="90px">
      <el-form-item label="患者姓名" prop="patientName">
        <el-input v-model="queryParams.patientName" placeholder="模糊查询" clearable @keyup.enter.native="handleQuery" />
      </el-form-item>
      <el-form-item label="胸片ID" prop="imageId">
        <el-input v-model="queryParams.imageId" placeholder="影像ID" clearable @keyup.enter.native="handleQuery" />
      </el-form-item>
      <el-form-item label="图片名" prop="imageName">
        <el-input v-model="queryParams.imageName" placeholder="模糊查询" clearable @keyup.enter.native="handleQuery" />
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="el-icon-search" size="mini" @click="handleQuery">搜索</el-button>
        <el-button icon="el-icon-refresh" size="mini" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5">
        <el-button type="danger" plain icon="el-icon-delete" size="mini" :disabled="multiple" @click="handleDelete()" v-hasPermi="['ai:analysis:result:remove']">删除</el-button>
      </el-col>
      <right-toolbar :showSearch.sync="showSearch" @queryTable="getList" />
    </el-row>

    <el-alert
      title="分析记录仅在阅片器或 AI 分析列表执行「AI 病灶分析」后自动新增，本页不支持手工新增或修改。"
      type="info"
      :closable="false"
      show-icon
      class="mb8"
    />

    <el-table v-loading="loading" :data="dataList" @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="55" align="center" />
      <el-table-column label="记录ID" prop="analysisId" width="90" align="center" />
      <el-table-column label="胸片ID" prop="imageId" width="90" align="center" />
      <el-table-column label="患者" prop="patientName" min-width="100" show-overflow-tooltip />
      <el-table-column label="图片名" prop="imageName" min-width="140" show-overflow-tooltip />
      <el-table-column label="病灶数" prop="lesionCount" width="80" align="center" />
      <el-table-column label="感染率" width="90" align="center">
        <template slot-scope="scope">{{ formatRate(scope.row.infectionRate) }}</template>
      </el-table-column>
      <el-table-column label="检测摘要" prop="detectDiagnosis" min-width="180" show-overflow-tooltip />
      <el-table-column label="报告时间" prop="reportCreateTime" width="160" align="center" />
      <el-table-column label="入库时间" prop="createTime" width="168" align="center">
        <template slot-scope="scope">{{ parseTime(scope.row.createTime) }}</template>
      </el-table-column>
      <el-table-column label="操作" align="center" width="140" class-name="small-padding fixed-width">
        <template slot-scope="scope">
          <el-button size="mini" type="text" icon="el-icon-view" @click="handleView(scope.row)" v-hasPermi="['ai:analysis:result:query']">详情</el-button>
          <el-button size="mini" type="text" icon="el-icon-delete" @click="handleDelete(scope.row)" v-hasPermi="['ai:analysis:result:remove']">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <pagination v-show="total > 0" :total="total" :page.sync="queryParams.pageNum" :limit.sync="queryParams.pageSize" @pagination="getList" />

    <el-dialog title="分析结果详情" :visible.sync="viewOpen" width="920px" append-to-body destroy-on-close>
      <template v-if="detail">
        <el-descriptions :column="2" border size="small" class="mb12">
          <el-descriptions-item label="记录ID">{{ detail.analysisId }}</el-descriptions-item>
          <el-descriptions-item label="胸片ID">{{ detail.imageId }}</el-descriptions-item>
          <el-descriptions-item label="患者">{{ detail.patientName || dash }}</el-descriptions-item>
          <el-descriptions-item label="图片名">{{ detail.imageName || dash }}</el-descriptions-item>
          <el-descriptions-item label="病灶数量">{{ detail.lesionCount != null ? detail.lesionCount : dash }}</el-descriptions-item>
          <el-descriptions-item label="感染率">{{ formatRate(detail.infectionRate) }}</el-descriptions-item>
          <el-descriptions-item label="感染面积(px²)">{{ detail.totalInfectionArea != null ? detail.totalInfectionArea : dash }}</el-descriptions-item>
          <el-descriptions-item label="报告生成时间">{{ detail.reportCreateTime || dash }}</el-descriptions-item>
          <el-descriptions-item label="检测摘要" :span="2">{{ detail.detectDiagnosis || dash }}</el-descriptions-item>
        </el-descriptions>

        <h4 class="detail-section-title">一、病灶检测（YOLO）</h4>
        <pre class="mono-pre">{{ prettyJson(detail.detectResultJson) }}</pre>
        <el-divider />
        <h4 class="detail-section-title">二、AI 影像诊断报告（结构化）</h4>
        <template v-if="reportStruct">
          <el-card shadow="never" class="struct-card">
            <div class="struct-block">
              <h5>诊断结论</h5>
              <p v-if="reportStruct.conclusion && reportStruct.conclusion.mainDiagnosis"><b>主要诊断：</b>{{ reportStruct.conclusion.mainDiagnosis }}</p>
              <p v-if="reportStruct.conclusion && reportStruct.conclusion.confidence"><b>诊断置信度：</b>{{ reportStruct.conclusion.confidence }}</p>
              <p v-if="reportStruct.conclusion && reportStruct.conclusion.differentialDiagnosis"><b>鉴别诊断：</b>{{ reportStruct.conclusion.differentialDiagnosis }}</p>
            </div>
            <div class="struct-block" v-if="hasList(reportStruct.imagingAnalysis && reportStruct.imagingAnalysis.lesionFeatures)">
              <h5>影像学分析 · 病灶特征</h5>
              <ul><li v-for="(t, i) in reportStruct.imagingAnalysis.lesionFeatures" :key="'lf'+i">{{ t }}</li></ul>
            </div>
            <div class="struct-block" v-if="hasList(reportStruct.imagingAnalysis && reportStruct.imagingAnalysis.overallLungAssessment)">
              <h5>影像学分析 · 整体肺部评估</h5>
              <ul><li v-for="(t, i) in reportStruct.imagingAnalysis.overallLungAssessment" :key="'ol'+i">{{ t }}</li></ul>
            </div>
            <div class="struct-block" v-if="hasList(reportStruct.imagingAnalysis && reportStruct.imagingAnalysis.otherFeatures)">
              <h5>影像学分析 · 其他特征</h5>
              <ul><li v-for="(t, i) in reportStruct.imagingAnalysis.otherFeatures" :key="'of'+i">{{ t }}</li></ul>
            </div>
            <div class="struct-block" v-if="hasList(reportStruct.clinicalSignificance && reportStruct.clinicalSignificance.lesionNatureAnalysis)">
              <h5>临床意义 · 病变性质分析</h5>
              <ul><li v-for="(t, i) in reportStruct.clinicalSignificance.lesionNatureAnalysis" :key="'cn'+i">{{ t }}</li></ul>
            </div>
            <div class="struct-block" v-if="riskEntries.length">
              <h5>临床意义 · 临床风险评估</h5>
              <p v-for="item in riskEntries" :key="item.key"><b>{{ item.key }}：</b>{{ item.value }}</p>
            </div>
            <div class="struct-block" v-if="reportStruct.severityAssessment && (reportStruct.severityAssessment.severityLevel || reportStruct.severityAssessment.basis)">
              <h5>严重程度评估</h5>
              <p v-if="reportStruct.severityAssessment.severityLevel"><b>分级：</b>{{ reportStruct.severityAssessment.severityLevel }}</p>
              <p v-if="reportStruct.severityAssessment.basis"><b>依据：</b>{{ reportStruct.severityAssessment.basis }}</p>
            </div>
            <div class="struct-block" v-if="reportStruct.summary">
              <h5>总结</h5>
              <p>{{ reportStruct.summary }}</p>
            </div>
          </el-card>
        </template>
        <el-empty v-else description="无结构化报告数据" :image-size="48" />
        <el-divider />
        <h4 class="detail-section-title">报告原文</h4>
        <pre class="mono-pre report-raw">{{ detail.diagnosisReportRaw || dash }}</pre>
      </template>
    </el-dialog>
  </div>
</template>

<script>
import { listAiAnalysisResult, getAiAnalysisResult, delAiAnalysisResult } from '@/api/medical/aiAnalysisResult'

export default {
  name: 'AiAnalysisResultManage',
  data() {
    return {
      dash: "—",
      loading: true,
      showSearch: true,
      total: 0,
      dataList: [],
      ids: [],
      multiple: true,
      queryParams: { pageNum: 1, pageSize: 10, patientName: undefined, imageId: undefined, imageName: undefined },
      viewOpen: false,
      detail: null,
      reportStruct: null
    }
  },
  computed: {
    riskEntries() {
      const map = this.reportStruct && this.reportStruct.clinicalSignificance && this.reportStruct.clinicalSignificance.riskAssessment
      if (!map || typeof map !== 'object') return []
      return Object.keys(map).map(k => ({ key: k, value: map[k] }))
    }
  },
  created() { this.getList() },
  methods: {
    getList() {
      this.loading = true
      listAiAnalysisResult(this.queryParams).then(res => {
        this.dataList = res.rows || []
        this.total = res.total || 0
        this.loading = false
      }).catch(() => { this.loading = false })
    },
    handleQuery() { this.queryParams.pageNum = 1; this.getList() },
    resetQuery() { this.resetForm('queryForm'); this.handleQuery() },
    handleSelectionChange(selection) {
      this.ids = selection.map(item => item.analysisId)
      this.multiple = !selection.length
    },
    handleView(row) {
      getAiAnalysisResult(row.analysisId).then(res => {
        this.detail = res.data || res
        this.reportStruct = null
        if (this.detail && this.detail.diagnosisReportJson) {
          try { this.reportStruct = JSON.parse(this.detail.diagnosisReportJson) } catch (e) { this.reportStruct = null }
        }
        this.viewOpen = true
      })
    },
    handleDelete(row) {
      const analysisIds = row && row.analysisId ? [row.analysisId] : this.ids
      if (!analysisIds.length) return
      this.$modal.confirm("是否确认删除选中的分析记录？删除后不可恢复。").then(() => {
        return delAiAnalysisResult(analysisIds.join(","))
      }).then(() => {
        this.getList()
        this.$modal.msgSuccess("删除成功")
      }).catch(() => {})
    },
    formatRate(val) { if (val == null || val === '') return this.dash; const n = Number(val); return Number.isNaN(n) ? val : n + '%' },
    prettyJson(str) {
      if (!str) return this.dash
      try { return JSON.stringify(JSON.parse(str), null, 2) } catch (e) { return str }
    },
    hasList(arr) { return Array.isArray(arr) && arr.length > 0 }
  }
}
</script>

<style scoped>
.mb12 { margin-bottom: 12px; }
.mb8 { margin-bottom: 8px; }
.detail-section-title { margin: 12px 0 8px; font-size: 14px; font-weight: 600; color: #303133; }
.mono-pre { margin: 0; padding: 10px; background: #fafafa; border: 1px solid #ebeef5; border-radius: 6px; font-size: 12px; line-height: 1.55; white-space: pre-wrap; word-break: break-word; max-height: 240px; overflow: auto; }
.report-raw { max-height: 320px; }
.struct-card { background: #fcfcfc; }
.struct-block { margin-bottom: 12px; }
.struct-block h5 { margin: 0 0 6px; font-size: 13px; color: #606266; }
.struct-block p, .struct-block li { margin: 4px 0; font-size: 13px; line-height: 1.55; color: #303133; }
.struct-block ul { margin: 0; padding-left: 18px; }
</style>
