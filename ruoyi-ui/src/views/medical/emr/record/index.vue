<template>
  <!-- 电子病历 - 病历列表 UTF-8 -->
  <div class="app-container">
    <el-form :model="queryParams" ref="queryForm" size="small" :inline="true" v-show="showSearch" label-width="100px">
      <el-form-item label="关键词" prop="keyword">
        <el-input v-model="queryParams.keyword" placeholder="标题/内容/疾病" clearable @keyup.enter.native="handleQuery" />
      </el-form-item>
      <el-form-item label="疾病" prop="disease">
        <el-input v-model="queryParams.disease" placeholder="疾病关键词" clearable />
      </el-form-item>
      <el-form-item label="归档" prop="archiveStatus">
        <el-select v-model="queryParams.archiveStatus" placeholder="全部" clearable style="width:120px">
          <el-option label="未归档" value="0" />
          <el-option label="已归档" value="1" />
        </el-select>
      </el-form-item>
      <el-form-item label="病历类型" prop="recordType">
        <el-select v-model="queryParams.recordType" placeholder="全部" clearable style="width:140px">
          <el-option v-for="o in recordTypeOptions" :key="o.value" :label="o.label" :value="o.value" />
        </el-select>
      </el-form-item>
      <el-form-item label="时间范围">
        <el-date-picker v-model="dateRange" style="width:240px" value-format="yyyy-MM-dd HH:mm:ss" type="datetimerange"
          range-separator="至" start-placeholder="开始时间" end-placeholder="结束时间" :default-time="['00:00:00','23:59:59']" />
      </el-form-item>
      <el-form-item label="实体类型1">
        <el-select v-model="queryParams.e1Type" placeholder="全部" clearable style="width:120px">
          <el-option v-for="o in labelTypeOptions" :key="o.value" :label="o.label" :value="o.value" />
        </el-select>
      </el-form-item>
      <el-form-item label="实体关键词1">
        <el-input v-model="queryParams.e1Keyword" placeholder="关键词" clearable style="width:120px" />
      </el-form-item>
      <el-form-item label="实体类型2">
        <el-select v-model="queryParams.e2Type" placeholder="全部" clearable style="width:120px">
          <el-option v-for="o in labelTypeOptions" :key="o.value" :label="o.label" :value="o.value" />
        </el-select>
      </el-form-item>
      <el-form-item label="实体关键词2">
        <el-input v-model="queryParams.e2Keyword" placeholder="关键词" clearable style="width:120px" />
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="el-icon-search" size="mini" @click="handleQuery">查询</el-button>
        <el-button icon="el-icon-refresh" size="mini" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5">
        <el-button type="primary" plain icon="el-icon-plus" size="mini" @click="handleAdd" v-hasPermi="['emr:record:add']">新增</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="success" plain icon="el-icon-edit" size="mini" :disabled="single" @click="handleUpdate" v-hasPermi="['emr:record:edit']">修改</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="danger" plain icon="el-icon-delete" size="mini" :disabled="multiple" @click="handleDelete" v-hasPermi="['emr:record:remove']">删除</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="warning" plain icon="el-icon-download" size="mini" @click="handleExport" v-hasPermi="['emr:record:export']">导出</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="info" plain icon="el-icon-upload2" size="mini" @click="$refs.fileInput.click()" v-hasPermi="['emr:record:import']">导入</el-button>
        <input ref="fileInput" type="file" accept=".xlsx,.xls" style="display:none" @change="onImportFile" />
      </el-col>
      <el-col :span="1.5">
        <el-button type="default" plain icon="el-icon-folder" size="mini" :disabled="single" @click="handleArchive" v-hasPermi="['emr:record:archive']">归档</el-button>
      </el-col>
      <right-toolbar :showSearch.sync="showSearch" @queryTable="getList" />
    </el-row>

    <el-table v-loading="loading" :data="recordList" @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="55" align="center" />
      <el-table-column label="ID" align="center" prop="id" width="70" />
      <el-table-column label="标题" align="center" prop="title" min-width="120" show-overflow-tooltip />
      <el-table-column label="类型" align="center" prop="recordType" width="100" :formatter="fmtRecordType" />
      <el-table-column label="疾病" align="center" prop="disease" width="100" show-overflow-tooltip />
      <el-table-column label="已归档" align="center" prop="archiveStatus" width="90">
        <template slot-scope="s">{{ s.row.archiveStatus === '1' ? '是' : '否' }}</template>
      </el-table-column>
      <el-table-column label="创建时间" align="center" prop="createTime" width="160">
        <template slot-scope="s"><span>{{ parseTime(s.row.createTime) }}</span></template>
      </el-table-column>
      <el-table-column label="操作" align="center" class-name="small-padding fixed-width" width="200">
        <template slot-scope="scope">
          <el-button size="mini" type="text" icon="el-icon-view" @click="goDetail(scope.row.id)" v-hasPermi="['emr:record:query']">详情</el-button>
          <el-button size="mini" type="text" icon="el-icon-edit" @click="handleUpdateRow(scope.row)" v-if="scope.row.archiveStatus !== '1'" v-hasPermi="['emr:record:edit']">修改</el-button>
          <el-button size="mini" type="text" icon="el-icon-delete" @click="handleDeleteRow(scope.row)" v-if="scope.row.archiveStatus !== '1'" v-hasPermi="['emr:record:remove']">删除</el-button>
        </template>
      </el-table-column>
    </el-table>
    <pagination v-show="total>0" :total="total" :page.sync="queryParams.pageNum" :limit.sync="queryParams.pageSize" @pagination="getList" />
  </div>
</template>

<script>
import request from '@/utils/request'
import { listRecord, delRecord, archiveRecord } from '@/api/medical/emr/record'

export default {
  name: 'EmrRecord',
  data() {
    return {
      loading: true,
      showSearch: true,
      ids: [],
      single: true,
      multiple: true,
      total: 0,
      recordList: [],
      dateRange: [],
      queryParams: {
        pageNum: 1,
        pageSize: 10,
        keyword: undefined,
        disease: undefined,
        archiveStatus: undefined,
        recordType: undefined,
        e1Type: undefined,
        e1Keyword: undefined,
        e2Type: undefined,
        e2Keyword: undefined
      },
      recordTypeOptions: [
        { value: 'admission', label: '入院记录' },
        { value: 'discharge', label: '出院记录' },
        { value: 'surgery', label: '手术记录' },
        { value: 'course', label: '病程记录' }
      ],
      labelTypeOptions: [
        { value: 'DISEASE', label: '疾病' },
        { value: 'DRUG', label: '药物' },
        { value: 'SURGERY', label: '手术' },
        { value: 'ANATOMY', label: '解剖部位' },
        { value: 'IMAGING', label: '影像' },
        { value: 'LAB', label: '检验' }
      ]
    }
  },
  created() {
    this.getList()
  },
  methods: {
    fmtRecordType(row) {
      const m = { admission: '入院记录', discharge: '出院记录', surgery: '手术记录', course: '病程记录' }
      return m[row.recordType] || row.recordType || '-'
    },
    getList() {
      this.loading = true
      const q = { ...this.queryParams }
      if (this.dateRange && this.dateRange.length === 2) {
        q.beginTime = this.dateRange[0]
        q.endTime = this.dateRange[1]
      }
      listRecord(q).then(res => {
        this.recordList = res.rows
        this.total = res.total
        this.loading = false
      })
    },
    handleQuery() {
      this.queryParams.pageNum = 1
      this.getList()
    },
    resetQuery() {
      this.dateRange = []
      this.resetForm('queryForm')
      this.handleQuery()
    },
    handleSelectionChange(selection) {
      this.ids = selection.map(i => i.id)
      this.single = selection.length !== 1
      this.multiple = !selection.length
    },
    handleAdd() {
      this.$router.push({ path: '/emr/record-edit/index' })
    },
    handleUpdate() {
      const id = this.ids[0]
      this.$router.push({ path: '/emr/record-edit/index', query: { id } })
    },
    handleUpdateRow(row) {
      this.$router.push({ path: '/emr/record-edit/index', query: { id: row.id } })
    },
    goDetail(id) {
      this.$router.push({ path: '/emr/record-detail/index', query: { id } })
    },
    handleDelete() {
      const ids = this.ids
      this.$modal.confirm('是否删除选中的病历记录？').then(() => {
        let chain = Promise.resolve()
        ids.forEach(id => { chain = chain.then(() => delRecord(id)) })
        return chain
      }).then(() => {
        this.getList()
        this.$modal.msgSuccess('删除成功')
      }).catch(() => {})
    },
    handleDeleteRow(row) {
      this.$modal.confirm('是否删除该病历记录？').then(() => delRecord(row.id)).then(() => {
        this.getList()
        this.$modal.msgSuccess('删除成功')
      }).catch(() => {})
    },
    handleArchive() {
      const id = this.ids[0]
      this.$modal.confirm('归档后将只读，确定要归档吗？').then(() => archiveRecord(id)).then(() => {
        this.getList()
        this.$modal.msgSuccess('操作成功')
      }).catch(() => {})
    },
    handleExport() {
      const q = { ...this.queryParams }
      if (this.dateRange && this.dateRange.length === 2) {
        q.beginTime = this.dateRange[0]
        q.endTime = this.dateRange[1]
      }
      this.download('emr/record/export', q, `电子病历_${new Date().getTime()}.xlsx`)
    },
    onImportFile(e) {
      const file = e.target.files[0]
      if (!file) return
      const form = new FormData()
      form.append('file', file)
      request({
        url: '/emr/record/importData',
        method: 'post',
        data: form
      }).then(data => {
        if (data && data.code === 200) {
          this.$modal.msgSuccess(data.msg || '导入成功')
          this.getList()
        } else {
          this.$modal.msgError((data && data.msg) || '导入失败')
        }
      }).catch(() => {}).finally(() => { e.target.value = '' })
    }
  }
}
</script>
