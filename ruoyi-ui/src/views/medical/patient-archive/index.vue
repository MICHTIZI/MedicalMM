<template>
  <div class="app-container">
    <el-form :model="queryParams" ref="queryForm" size="small" :inline="true" label-width="88px">
      <el-form-item :label="L.formPatientName" prop="patientName">
        <el-input v-model="queryParams.patientName" :placeholder="L.placeholderName" clearable @keyup.enter.native="handleQuery" style="width: 220px" />
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="el-icon-search" size="mini" @click="handleQuery">{{ L.btnSearch }}</el-button>
        <el-button icon="el-icon-refresh" size="mini" @click="resetQuery">{{ L.btnReset }}</el-button>
      </el-form-item>
    </el-form>

    <el-alert :title="L.alertTitle" type="info" :closable="false" class="mb8" />

    <el-table v-loading="loading" :data="tableList" border size="small">
      <el-table-column :label="L.colPatientId" prop="patientId" width="88" align="center" />
      <el-table-column :label="L.colName" prop="patientName" min-width="100" show-overflow-tooltip />
      <el-table-column :label="L.colDoctor" prop="attendingDoctor" min-width="100" show-overflow-tooltip />
      <el-table-column :label="L.colArchiveTime" prop="archiveTime" width="160" align="center">
        <template slot-scope="scope">{{ scope.row.archiveTime ? parseTime(scope.row.archiveTime) : L.dash }}</template>
      </el-table-column>
      <el-table-column :label="L.colArchiveBy" prop="archiveBy" width="100" show-overflow-tooltip />
      <el-table-column :label="L.colArchiveRemark" prop="archiveRemark" min-width="140" show-overflow-tooltip />
      <el-table-column :label="L.colDiagStatus" prop="diagnosisStatus" width="110" align="center">
        <template slot-scope="scope">{{ diagLabel(scope.row.diagnosisStatus) }}</template>
      </el-table-column>
      <el-table-column :label="L.colAction" width="120" align="center" fixed="right">
        <template slot-scope="scope">
          <el-button
            v-hasPermi="['medical:patient:unarchive']"
            size="mini"
            type="text"
            @click="handleUnarchive(scope.row)"
          >{{ L.btnUnarchive }}</el-button>
        </template>
      </el-table-column>
    </el-table>

    <pagination
      v-show="total > 0"
      :total="total"
      :page.sync="queryParams.pageNum"
      :limit.sync="queryParams.pageSize"
      @pagination="getList"
    />
  </div>
</template>

<script>
import { listPatientCards, unarchivePatient } from '@/api/medical/patient'

/** All UI strings as \\u escapes (UTF-8 safe in repo / editor) */
const L = {
  formPatientName: '\u60a3\u8005\u59d3\u540d',
  placeholderName: '\u59d3\u540d\u6a21\u7cca\u67e5\u8be2',
  btnSearch: '\u641c\u7d22',
  btnReset: '\u91cd\u7f6e',
  alertTitle: '\u4ee5\u4e0b\u4e3a\u5df2\u5f52\u6863\u60a3\u8005\uff0c\u4ec5\u53ef\u67e5\u770b\u4fe1\u606f\u4e0e\u53d6\u6d88\u5f52\u6863\uff1b\u7f16\u8f91\u6570\u636e\u8bf7\u5148\u5728\u300c\u60a3\u8005\u5217\u8868\u300d\u4e2d\u53d6\u6d88\u5f52\u6863\u3002',
  colPatientId: '\u60a3\u8005ID',
  colName: '\u59d3\u540d',
  colDoctor: '\u4e3b\u6cbb\u533b\u751f',
  colArchiveTime: '\u5f52\u6863\u65f6\u95f4',
  colArchiveBy: '\u5f52\u6863\u4eba',
  colArchiveRemark: '\u5f52\u6863\u5907\u6ce8',
  colDiagStatus: '\u8bca\u65ad\u72b6\u6001',
  colAction: '\u64cd\u4f5c',
  btnUnarchive: '\u53d6\u6d88\u5f52\u6863',
  dash: '\u2014',
  diag0: '\u672a\u5f00\u59cb',
  diag1: 'AI \u8bca\u65ad\u4e2d',
  diag2: '\u8bca\u65ad\u5b8c\u6210',
  diag3: '\u5f85\u5ba1\u6838',
  diag4: '\u5df2\u751f\u6210\u62a5\u544a',
  confirmUnarchive: '\u786e\u8ba4\u53d6\u6d88\u5f52\u6863\uff1f\u53d6\u6d88\u540e\u60a3\u8005\u5c06\u56de\u5230\u300c\u60a3\u8005\u5217\u8868\u300d\u5e76\u53ef\u7ee7\u7eed\u5f55\u5165\u4e0e\u4fee\u6539\u3002',
  msgUnarchivedOk: '\u5df2\u53d6\u6d88\u5f52\u6863'
}

export default {
  name: 'PatientArchive',
  data() {
    return {
      L,
      loading: false,
      tableList: [],
      total: 0,
      queryParams: {
        pageNum: 1,
        pageSize: 10,
        patientName: undefined,
        archiveScope: 'archived'
      }
    }
  },
  created() {
    this.getList()
  },
  methods: {
    diagLabel(status) {
      const s = Number(status || 0)
      const map = {
        0: L.diag0,
        1: L.diag1,
        2: L.diag2,
        3: L.diag3,
        4: L.diag4
      }
      return map[s] || L.dash
    },
    getList() {
      this.loading = true
      listPatientCards(this.queryParams).then(res => {
        this.tableList = res.rows || []
        this.total = res.total || 0
      }).finally(() => { this.loading = false })
    },
    handleQuery() {
      this.queryParams.pageNum = 1
      this.getList()
    },
    resetQuery() {
      this.queryParams.patientName = undefined
      this.queryParams.pageNum = 1
      this.getList()
    },
    handleUnarchive(row) {
      const pid = row.patientId
      this.$modal.confirm(L.confirmUnarchive).then(() => {
        return unarchivePatient(pid)
      }).then(() => {
        this.$modal.msgSuccess(L.msgUnarchivedOk)
        this.getList()
      }).catch(() => {})
    }
  }
}
</script>

<style scoped>
.mb8 {
  margin-bottom: 8px;
}
</style>
