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

/** 页面文案 */
const L = {
  patientName: '患者姓名',
  fuzzyPlaceholder: '模糊查询',
  search: '搜索',
  reset: '重置',
  delete: '删除',
  reportId: '报告ID',
  patient: '患者',
  image: '影像',
  recordId: '病历ID',
  labId: '检验ID',
  signature: '签名',
  createdAt: '创建时间',
  actions: '操作',
  view: '详情',
  edit: '修改',
  detailTitle: '融合报告详情',
  patientIdLabel: '患者ID',
  imageIdLabel: '影像ID',
  dash: '—',
  caseSnapshot: '病历文本快照',
  fusionJson: '融合分析返回 JSON',
  editTitle: '修改融合报告',
  doctorSignature: '医生签名',
  doctorAdvice: '医生建议',
  remark: '备注',
  save: '保存',
  cancel: '取消',
  saveOk: '保存成功',
  deleteConfirm: '是否确认删除选中的融合报告？',
  deleteOk: '删除成功'
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
