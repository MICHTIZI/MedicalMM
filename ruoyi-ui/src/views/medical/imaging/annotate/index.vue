<template>
  <!-- 医学影像 - 标注入口列表 UTF-8 -->
  <div class="app-container">
    <el-form :model="queryParams" ref="queryForm" size="small" :inline="true" label-width="80px">
      <el-form-item label="关键词">
        <el-input v-model="queryParams.keyword" placeholder="文件名/疾病" clearable @keyup.enter.native="handleQuery" />
      </el-form-item>
      <el-form-item label="标签">
        <el-select v-model="queryParams.labelStr" placeholder="全部" clearable style="width:100px">
          <el-option label="正常(0)" value="0" />
          <el-option label="异常(1)" value="1" />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="el-icon-search" size="mini" @click="handleQuery">查询</el-button>
      </el-form-item>
    </el-form>

    <el-table v-loading="loading" :data="list" size="small">
      <el-table-column label="ID" prop="id" width="70" />
      <el-table-column label="缩略图" width="80" align="center">
        <template slot-scope="s">
          <el-image :src="getImageUrl(s.row.imagePath)" style="width:50px;height:50px" fit="cover" />
        </template>
      </el-table-column>
      <el-table-column label="文件名" prop="imageName" min-width="160" show-overflow-tooltip />
      <el-table-column label="当前标注" prop="diseases" min-width="160" show-overflow-tooltip>
        <template slot-scope="s">{{ s.row.diseases || '未标注' }}</template>
      </el-table-column>
      <el-table-column label="标签" prop="labelStr" width="80" align="center">
        <template slot-scope="s">
          <el-tag :type="s.row.labelStr === '1' ? 'danger' : 'info'" size="mini">{{ s.row.labelStr === '1' ? '异常' : '正常' }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="120" align="center">
        <template slot-scope="scope">
          <el-button size="mini" type="text" icon="el-icon-edit" @click="goAnnotate(scope.row.id)" v-hasPermi="['imaging:xray:annotate']">标注</el-button>
        </template>
      </el-table-column>
    </el-table>
    <pagination v-show="total>0" :total="total" :page.sync="queryParams.pageNum" :limit.sync="queryParams.pageSize" @pagination="getList" />
  </div>
</template>

<script>
import { listXray, imageUrl } from '@/api/medical/imaging/xray'

export default {
  name: 'ImagingAnnotate',
  data() {
    return {
      loading: true,
      list: [],
      total: 0,
      queryParams: { pageNum: 1, pageSize: 10, keyword: undefined, labelStr: undefined }
    }
  },
  created() { this.getList() },
  methods: {
    getImageUrl(path) {
      return imageUrl(path)
    },
    getList() {
      this.loading = true
      listXray(this.queryParams).then(res => {
        this.list = res.rows
        this.total = res.total
        this.loading = false
      })
    },
    handleQuery() { this.queryParams.pageNum = 1; this.getList() },
    goAnnotate(id) {
      this.$router.push({ path: '/imaging/annotate-edit/index', query: { id } })
    }
  }
}
</script>
