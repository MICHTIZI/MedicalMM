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
      <el-col :span="1.5">
        <el-button type="danger" plain icon="el-icon-delete" size="mini" :disabled="multiple" @click="handleDelete()" v-hasPermi="['fusion:report:remove']">{{ L.delete }}</el-button>
      </el-col>
      <right-toolbar :showSearch.sync="showSearch" @queryTable="getList" />
    </el-row>

    <el-table v-loading="loading" :data="dataList" @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="55" align="center" />
      <el-table-column :label="L.reportId" prop="reportId" width="90" align="center" />
      <el-table-column :label="L.patient" prop="patientName" min-width="110" show-overflow-tooltip />
      <el-table-column :label="L.image" prop="imageName" min-width="120" show-overflow-tooltip />
      <el-table-column :label="L.recordId" prop="medicalRecordId" width="90" align="center" />
      <el-table-column :label="L.labId" prop="labResultId" width="90" align="center" />
      <el-table-column :label="L.signature" prop="doctorSignature" width="100" show-overflow-tooltip />
      <el-table-column :label="L.createdAt" prop="createTime" width="168" align="center">
        <template slot-scope="scope">{{ parseTime(scope.row.createTime) }}</template>
      </el-table-column>
      <el-table-column :label="L.actions" align="center" width="200" class-name="small-padding fixed-width">
        <template slot-scope="scope">
          <el-button size="mini" type="text" icon="el-icon-view" @click="handleView(scope.row)" v-hasPermi="['fusion:report:query']">{{ L.view }}</el-button>
          <el-button size="mini" type="text" icon="el-icon-edit" @click="handleEdit(scope.row)" v-hasPermi="['fusion:report:edit']">{{ L.edit }}</el-button>
          <el-button size="mini" type="text" icon="el-icon-delete" @click="handleDelete(scope.row)" v-hasPermi="['fusion:report:remove']">{{ L.delete }}</el-button>
        </template>
      </el-table-column>
    </el-table>

    <pagination v-show="total > 0" :total="total" :page.sync="queryParams.pageNum" :limit.sync="queryParams.pageSize" @pagination="getList" />

    <el-dialog :title="L.detailTitle" :visible.sync="viewOpen" width="900px" append-to-body destroy-on-close>
      <el-descriptions v-if="detail" :column="1" border size="small">
        <el-descriptions-item :label="L.reportId">{{ detail.reportId }}</el-descriptions-item>
        <el-descriptions-item :label="L.patientIdLabel">{{ detail.patientId }}</el-descriptions-item>
        <el-descriptions-item :label="L.imageIdLabel">{{ detail.imageId }}</el-descriptions-item>
        <el-descriptions-item :label="L.recordId">{{ detail.medicalRecordId || L.dash }}</el-descriptions-item>
        <el-descriptions-item :label="L.labId">{{ detail.labResultId || L.dash }}</el-descriptions-item>
        <el-descriptions-item :label="L.doctorSignature">{{ detail.doctorSignature || L.dash }}</el-descriptions-item>
        <el-descriptions-item :label="L.doctorAdvice">{{ detail.doctorAdvice || L.dash }}</el-descriptions-item>
      </el-descriptions>
      <el-divider />
      <div class="mono-block"><strong>{{ L.caseSnapshot }}</strong><pre>{{ detail && detail.caseText }}</pre></div>
      <el-divider />
      <div class="mono-block"><strong>{{ L.fusionJson }}</strong><pre>{{ prettyJson(detail && detail.fusionResponseJson) }}</pre></div>
    </el-dialog>

    <el-dialog :title="L.editTitle" :visible.sync="editOpen" width="520px" append-to-body destroy-on-close @close="resetEdit">
      <el-form ref="editForm" :model="editForm" :label-width="labelWidth">
        <el-form-item :label="L.doctorSignature">
          <el-input v-model="editForm.doctorSignature" maxlength="128" show-word-limit />
        </el-form-item>
        <el-form-item :label="L.doctorAdvice">
          <el-input v-model="editForm.doctorAdvice" type="textarea" :rows="4" maxlength="2000" show-word-limit />
        </el-form-item>
        <el-form-item :label="L.remark">
          <el-input v-model="editForm.remark" maxlength="500" show-word-limit />
        </el-form-item>
      </el-form>
      <div slot="footer">
        <el-button type="primary" @click="submitEdit">{{ L.save }}</el-button>
        <el-button @click="editOpen = false">{{ L.cancel }}</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import { listFusionReport, getFusionReport, updateFusionReport, delFusionReport } from '@/api/medical/fusionReport'

/** UI strings as Unicode escapes (UTF-8 safe in repo / tooling). */
const L = {
  patientName: '\u60a3\u8005\u59d3\u540d',
  fuzzyPlaceholder: '\u6a21\u7cca\u67e5\u8be2',
  search: '\u641c\u7d22',
  reset: '\u91cd\u7f6e',
  delete: '\u5220\u9664',
  reportId: '\u62a5\u544aID',
  patient: '\u60a3\u8005',
  image: '\u5f71\u50cf',
  recordId: '\u75c5\u5386ID',
  labId: '\u68c0\u9a8cID',
  signature: '\u7b7e\u540d',
  createdAt: '\u521b\u5efa\u65f6\u95f4',
  actions: '\u64cd\u4f5c',
  view: '\u8be6\u60c5',
  edit: '\u4fee\u6539',
  detailTitle: '\u878d\u5408\u62a5\u544a\u8be6\u60c5',
  patientIdLabel: '\u60a3\u8005ID',
  imageIdLabel: '\u5f71\u50cfID',
  dash: '\u2014',
  caseSnapshot: '\u75c5\u5386\u6587\u672c\u5feb\u7167',
  fusionJson: '\u878d\u5408\u8fd4\u56de JSON',
  editTitle: '\u4fee\u6539\u878d\u5408\u62a5\u544a',
  doctorSignature: '\u533b\u751f\u7b7e\u540d',
  doctorAdvice: '\u533b\u751f\u5efa\u8bae',
  remark: '\u5907\u6ce8',
  save: '\u4fdd\u5b58',
  cancel: '\u53d6\u6d88',
  saveOk: '\u4fdd\u5b58\u6210\u529f',
  deleteConfirm: '\u662f\u5426\u786e\u8ba4\u5220\u9664\u9009\u4e2d\u7684\u878d\u5408\u62a5\u544a\uff1f',
  deleteOk: '\u5220\u9664\u6210\u529f'
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
      ids: [],
      single: true,
      multiple: true,
      queryParams: {
        pageNum: 1,
        pageSize: 10,
        patientName: undefined
      },
      viewOpen: false,
      detail: null,
      editOpen: false,
      editForm: {
        reportId: null,
        doctorSignature: '',
        doctorAdvice: '',
        remark: ''
      }
    }
  },
  created() {
    this.getList()
  },
  methods: {
    getList() {
      this.loading = true
      listFusionReport(this.queryParams).then(res => {
        this.dataList = res.rows || []
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
    handleSelectionChange(selection) {
      this.ids = selection.map(item => item.reportId)
      this.single = selection.length !== 1
      this.multiple = !selection.length
    },
    handleView(row) {
      getFusionReport(row.reportId).then(res => {
        this.detail = res.data || null
        this.viewOpen = true
      })
    },
    prettyJson(raw) {
      if (!raw) return ''
      try {
        const o = typeof raw === 'string' ? JSON.parse(raw) : raw
        return JSON.stringify(o, null, 2)
      } catch (e) {
        return String(raw)
      }
    },
    handleEdit(row) {
      getFusionReport(row.reportId).then(res => {
        const d = res.data || {}
        this.editForm = {
          reportId: d.reportId,
          doctorSignature: d.doctorSignature || '',
          doctorAdvice: d.doctorAdvice || '',
          remark: d.remark || ''
        }
        this.editOpen = true
      })
    },
    resetEdit() {
      this.editForm = { reportId: null, doctorSignature: '', doctorAdvice: '', remark: '' }
    },
    submitEdit() {
      updateFusionReport({
        reportId: this.editForm.reportId,
        doctorSignature: this.editForm.doctorSignature,
        doctorAdvice: this.editForm.doctorAdvice,
        remark: this.editForm.remark
      }).then(() => {
        this.$modal.msgSuccess(this.L.saveOk)
        this.editOpen = false
        this.getList()
      })
    },
    handleDelete(row) {
      const idsArr = row && row.reportId != null ? [row.reportId] : this.ids
      if (!idsArr.length) return
      this.$modal.confirm(this.L.deleteConfirm).then(() => {
        return delFusionReport(idsArr.join(','))
      }).then(() => {
        this.getList()
        this.$modal.msgSuccess(this.L.deleteOk)
      }).catch(() => {})
    }
  }
}
</script>

<style scoped>
.mono-block pre {
  max-height: 360px;
  overflow: auto;
  background: #f5f7fa;
  padding: 10px;
  font-size: 12px;
  white-space: pre-wrap;
  word-break: break-all;
}
</style>
