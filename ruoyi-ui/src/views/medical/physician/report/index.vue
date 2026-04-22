<template>
  <div class="app-container report-list">
    <el-form :inline="true" size="small">
      <el-form-item label="患者姓名">
        <el-input v-model="queryParams.patientName" placeholder="姓名关键词" clearable style="width:160px" @keyup.enter.native="handleQuery" />
      </el-form-item>
      <el-form-item label="状态">
        <el-select v-model="queryParams.status" placeholder="全部" clearable style="width:140px">
          <el-option v-for="s in statusOptions" :key="s.value" :label="s.label" :value="s.value" />
        </el-select>
      </el-form-item>
      <el-form-item label="创建时间">
        <el-date-picker
          v-model="dateRange"
          type="daterange"
          value-format="yyyy-MM-dd"
          range-separator="至"
          start-placeholder="起"
          end-placeholder="止"
          style="width:240px"
        />
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="el-icon-search" @click="handleQuery">查询</el-button>
        <el-button icon="el-icon-refresh" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5">
        <el-button type="primary" plain icon="el-icon-plus" size="mini" @click="handleAdd">新建报告</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="danger" plain icon="el-icon-delete" size="mini" :disabled="!selectedIds.length" @click="handleBatchDelete">删除</el-button>
      </el-col>
    </el-row>

    <el-table v-loading="loading" :data="list" @selection-change="onSelChange">
      <el-table-column type="selection" width="55" align="center" />
      <el-table-column label="报告ID" prop="id" width="160" />
      <el-table-column label="患者" prop="patientName" min-width="120" show-overflow-tooltip />
      <el-table-column label="关联病历" prop="recordId" width="110" />
      <el-table-column label="影像路径" prop="imagePath" min-width="180" show-overflow-tooltip />
      <el-table-column label="状态" prop="status" width="110" align="center">
        <template slot-scope="s">
          <el-tag :type="statusTag(s.row.status)" size="mini">{{ statusLabel(s.row.status) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="创建时间" prop="createTime" width="170" align="center" />
      <el-table-column label="更新时间" prop="updateTime" width="170" align="center" />
      <el-table-column label="操作" width="280" align="center">
        <template slot-scope="s">
          <el-button type="text" size="mini" icon="el-icon-edit" @click="handleEdit(s.row)">编辑</el-button>
          <el-button
            v-if="s.row.status === '0'"
            type="text" size="mini" icon="el-icon-finished"
            @click="handleStatus(s.row, '1')"
          >审核</el-button>
          <el-button
            v-if="s.row.status === '1'"
            type="text" size="mini" icon="el-icon-folder-opened"
            @click="handleStatus(s.row, '2')"
          >归档</el-button>
          <el-button type="text" size="mini" icon="el-icon-printer" @click="handleExportPdf(s.row)">PDF</el-button>
          <el-button type="text" size="mini" icon="el-icon-delete" @click="handleDelete(s.row)">删除</el-button>
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
import { listReport, delReport, updateReportStatus, REPORT_STATUS_LABEL } from '@/api/physician/report'
import { exportReportPdf } from '@/utils/reportPdf'

export default {
  name: 'PhysicianReport',
  data() {
    return {
      loading: false,
      list: [],
      total: 0,
      selectedIds: [],
      dateRange: [],
      queryParams: { pageNum: 1, pageSize: 10, patientName: '', status: '' },
      statusOptions: [
        { value: '0', label: '草稿' },
        { value: '1', label: '已审核' },
        { value: '2', label: '已归档' }
      ]
    }
  },
  created() { this.getList() },
  methods: {
    async getList() {
      this.loading = true
      const q = { ...this.queryParams }
      if (this.dateRange && this.dateRange.length === 2) {
        q.startDate = this.dateRange[0]
        q.endDate = this.dateRange[1]
      }
      const res = await listReport(q)
      this.list = res.rows || []
      this.total = res.total || 0
      this.loading = false
    },
    handleQuery() { this.queryParams.pageNum = 1; this.getList() },
    resetQuery() {
      this.queryParams = { pageNum: 1, pageSize: 10, patientName: '', status: '' }
      this.dateRange = []
      this.getList()
    },
    onSelChange(sel) { this.selectedIds = sel.map(r => r.id) },
    handleAdd() { this.$router.push('/physician/report/edit') },
    handleEdit(row) { this.$router.push('/physician/report/edit/' + row.id) },
    async handleStatus(row, status) {
      await updateReportStatus(row.id, status)
      this.$message.success('状态已更新')
      this.getList()
    },
    async handleDelete(row) {
      await this.$confirm(`确认删除报告 ${row.patientName || row.id} ?`, '提示', { type: 'warning' })
      await delReport(row.id)
      this.$message.success('删除成功')
      this.getList()
    },
    async handleBatchDelete() {
      if (!this.selectedIds.length) return
      await this.$confirm(`确认删除选中的 ${this.selectedIds.length} 条报告 ?`, '提示', { type: 'warning' })
      for (const id of this.selectedIds) await delReport(id)
      this.$message.success('删除成功')
      this.getList()
    },
    handleExportPdf(row) { exportReportPdf(row) },
    statusLabel(s) { return REPORT_STATUS_LABEL[s] || '-' },
    statusTag(s) { return { '0': 'info', '1': 'success', '2': 'warning' }[s] || 'info' }
  }
}
</script>

<style lang="scss" scoped>
.report-list { background: #fff; }
</style>
