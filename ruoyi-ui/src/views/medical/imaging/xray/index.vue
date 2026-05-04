<template>
  <!-- 医学影像 - 影像数据列表 UTF-8 -->
  <div class="app-container">
    <el-form :model="queryParams" ref="queryForm" size="small" :inline="true" v-show="showSearch" label-width="100px">
      <el-form-item label="关键词" prop="keyword">
        <el-input v-model="queryParams.keyword" placeholder="文件名/疾病" clearable @keyup.enter.native="handleQuery" />
      </el-form-item>
      <el-form-item label="疾病" prop="diseases">
        <el-input v-model="queryParams.diseases" placeholder="疾病名称" clearable />
      </el-form-item>
      <el-form-item label="患者姓名" prop="patientName">
        <el-input v-model="queryParams.patientName" placeholder="请输入患者姓名" clearable @keyup.enter.native="handleQuery" />
      </el-form-item>
      <el-form-item label="标签" prop="labelStr">
        <el-select v-model="queryParams.labelStr" placeholder="全部" clearable style="width:100px">
          <el-option label="正常(0)" value="0" />
          <el-option label="异常(1)" value="1" />
        </el-select>
      </el-form-item>
      <el-form-item label="时间范围">
        <el-date-picker v-model="dateRange" style="width:240px" value-format="yyyy-MM-dd HH:mm:ss" type="datetimerange"
          range-separator="至" start-placeholder="开始" end-placeholder="结束" :default-time="['00:00:00','23:59:59']" />
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="el-icon-search" size="mini" @click="handleQuery">查询</el-button>
        <el-button icon="el-icon-refresh" size="mini" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5">
        <el-button type="primary" plain icon="el-icon-plus" size="mini" @click="handleAdd" v-hasPermi="['imaging:xray:add']">新增</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="danger" plain icon="el-icon-delete" size="mini" :disabled="multiple" @click="handleDelete" v-hasPermi="['imaging:xray:remove']">删除</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="warning" plain icon="el-icon-download" size="mini" @click="handleExport" v-hasPermi="['imaging:xray:export']">导出</el-button>
      </el-col>
      <el-col :span="1.5">
        <router-link to="/imaging/upload"><el-button type="success" plain icon="el-icon-upload2" size="mini" v-hasPermi="['imaging:xray:upload']">批量上传</el-button></router-link>
      </el-col>
      <right-toolbar :showSearch.sync="showSearch" @queryTable="getList" />
    </el-row>

    <el-table v-loading="loading" :data="list" @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="55" align="center" />
      <el-table-column label="ID" prop="id" width="70" align="center" />
      <el-table-column label="缩略图" width="80" align="center">
        <template slot-scope="s">
          <el-image :src="getImageUrl(s.row.imagePath)" style="width:50px;height:50px" fit="cover" :preview-src-list="[getImageUrl(s.row.imagePath)]" />
        </template>
      </el-table-column>
      <el-table-column label="文件名" prop="imageName" min-width="160" show-overflow-tooltip />
      <el-table-column label="患者姓名" prop="patientName" width="120" show-overflow-tooltip />
      <el-table-column label="疾病" prop="diseases" min-width="140" show-overflow-tooltip />
      <el-table-column label="标签" prop="labelStr" width="80" align="center">
        <template slot-scope="s">
          <el-tag :type="s.row.labelStr === '1' ? 'danger' : 'success'" size="mini">{{ s.row.labelStr === '1' ? '异常' : '正常' }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="尺寸" width="100" align="center">
        <template slot-scope="s">{{ s.row.width }}x{{ s.row.height }}</template>
      </el-table-column>
      <el-table-column label="入库时间" prop="createTime" width="160" align="center">
        <template slot-scope="s">{{ parseTime(s.row.createTime) }}</template>
      </el-table-column>
      <el-table-column label="操作" width="200" align="center">
        <template slot-scope="scope">
          <el-button size="mini" type="text" icon="el-icon-view" @click="goDetail(scope.row.id)" v-hasPermi="['imaging:xray:query']">详情</el-button>
          <el-button size="mini" type="text" icon="el-icon-edit" @click="goAnnotate(scope.row.id)" v-hasPermi="['imaging:xray:annotate']">标注</el-button>
          <el-button size="mini" type="text" icon="el-icon-delete" @click="handleDeleteRow(scope.row)" v-hasPermi="['imaging:xray:remove']">删除</el-button>
        </template>
      </el-table-column>
    </el-table>
    <pagination v-show="total>0" :total="total" :page.sync="queryParams.pageNum" :limit.sync="queryParams.pageSize" @pagination="getList" />
  </div>
</template>

<script>
import { listXray, delXray, imageUrl } from '@/api/medical/imaging/xray'

export default {
  name: 'ImagingXray',
  data() {
    return {
      loading: true,
      showSearch: true,
      ids: [],
      multiple: true,
      total: 0,
      list: [],
      dateRange: [],
      queryParams: {
        pageNum: 1,
        pageSize: 10,
        keyword: undefined,
        diseases: undefined,
        patientName: undefined,
        labelStr: undefined
      }
    }
  },
  created() { this.getList() },
  methods: {
    getImageUrl(path) {
      return imageUrl(path)
    },
    getList() {
      this.loading = true
      const q = { ...this.queryParams }
      if (this.dateRange && this.dateRange.length === 2) {
        q.beginTime = this.dateRange[0]
        q.endTime = this.dateRange[1]
      }
      listXray(q).then(res => {
        this.list = res.rows
        this.total = res.total
        this.loading = false
      })
    },
    handleQuery() { this.queryParams.pageNum = 1; this.getList() },
    resetQuery() { this.dateRange = []; this.resetForm('queryForm'); this.handleQuery() },
    handleSelectionChange(sel) {
      this.ids = sel.map(i => i.id)
      this.multiple = !sel.length
    },
    handleAdd() {
      this.$router.push('/imaging/upload')
    },
    goDetail(id) {
      this.$router.push({ path: '/imaging/xray-detail/index', query: { id } })
    },
    goAnnotate(id) {
      this.$router.push({ path: '/imaging/annotate-edit/index', query: { id } })
    },
    handleDelete() {
      this.$modal.confirm('是否删除选中的影像？').then(() => delXray(this.ids.join(','))).then(() => {
        this.getList(); this.$modal.msgSuccess('操作成功')
      }).catch(() => {})
    },
    handleDeleteRow(row) {
      this.$modal.confirm('是否删除该影像？').then(() => delXray(row.id)).then(() => {
        this.getList(); this.$modal.msgSuccess('操作成功')
      }).catch(() => {})
    },
    handleExport() {
      const q = { ...this.queryParams }
      if (this.dateRange && this.dateRange.length === 2) {
        q.beginTime = this.dateRange[0]
        q.endTime = this.dateRange[1]
      }
      this.download('imaging/xray/export', q, `医学影像_${Date.now()}.xlsx`)
    }
  }
}
</script>
