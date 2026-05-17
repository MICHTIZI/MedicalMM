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

/** 页面文案 */
const L = {
  formPatientName: '患者姓名',
  placeholderName: '姓名模糊查询',
  btnSearch: '搜索',
  btnReset: '重置',
  alertTitle: '以下为已归档患者，仅可查看信息与取消归档；编辑数据请先在「患者列表」中取消归档。',
  colPatientId: '患者ID',
  colName: '姓名',
  colDoctor: '主治医生',
  colArchiveTime: '归档时间',
  colArchiveBy: '归档人',
  colArchiveRemark: '归档备注',
  colDiagStatus: '诊断状态',
  colAction: '操作',
  btnUnarchive: '取消归档',
  dash: '—',
  diag0: '未开始',
  diag1: 'AI 诊断中',
  diag2: '诊断完成',
  diag3: '待审核',
  diag4: '已生成报告',
  confirmUnarchive: '确认取消归档？取消后患者将回到「患者列表」并可继续录入与修改。',
  msgUnarchivedOk: '已取消归档'
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
