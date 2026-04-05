<template>
  <!-- 电子病历 - 已归档列表 UTF-8 -->
  <div class="app-container">
    <el-alert title="当前为已归档记录，仅可查看详情，管理员可取消归档" type="info" show-icon :closable="false" class="mb8" />
    <el-form :model="queryParams" ref="queryForm" size="small" :inline="true" label-width="100px">
      <el-form-item label="关键词" prop="keyword">
        <el-input v-model="queryParams.keyword" placeholder="标题/内容" clearable @keyup.enter.native="handleQuery" />
      </el-form-item>
      <el-form-item label="疾病" prop="disease">
        <el-input v-model="queryParams.disease" placeholder="疾病关键词" clearable />
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="el-icon-search" size="mini" @click="handleQuery">查询</el-button>
      </el-form-item>
    </el-form>
    <el-table v-loading="loading" :data="recordList">
      <el-table-column label="ID" prop="id" width="70" />
      <el-table-column label="标题" prop="title" min-width="140" show-overflow-tooltip />
      <el-table-column label="疾病" prop="disease" width="120" />
      <el-table-column label="归档时间" prop="archivedTime" width="170">
        <template slot-scope="s">{{ parseTime(s.row.archivedTime) }}</template>
      </el-table-column>
      <el-table-column label="操作" width="200" align="center">
        <template slot-scope="scope">
          <el-button size="mini" type="text" @click="goDetail(scope.row.id)" v-hasPermi="['emr:record:query']">详情</el-button>
          <el-button size="mini" type="text" @click="handleUnarchive(scope.row)" v-hasPermi="['emr:record:unarchive']">取消归档</el-button>
        </template>
      </el-table-column>
    </el-table>
    <pagination v-show="total>0" :total="total" :page.sync="queryParams.pageNum" :limit.sync="queryParams.pageSize" @pagination="getList" />
  </div>
</template>

<script>
import { listRecord, unarchiveRecord } from '@/api/medical/emr/record'

export default {
  name: 'EmrArchive',
  data() {
    return {
      loading: true,
      recordList: [],
      total: 0,
      queryParams: {
        pageNum: 1,
        pageSize: 10,
        archiveStatus: '1',
        keyword: undefined,
        disease: undefined
      }
    }
  },
  created() { this.getList() },
  methods: {
    getList() {
      this.loading = true
      listRecord(this.queryParams).then(res => {
        this.recordList = res.rows
        this.total = res.total
        this.loading = false
      })
    },
    handleQuery() { this.queryParams.pageNum = 1; this.getList() },
    goDetail(id) {
      this.$router.push({ path: '/emr/record-detail/index', query: { id } })
    },
    handleUnarchive(row) {
      this.$modal.confirm('确定取消归档该病历？').then(() => unarchiveRecord(row.id)).then(() => {
        this.$modal.msgSuccess('操作成功')
        this.getList()
      }).catch(() => {})
    }
  }
}
</script>
