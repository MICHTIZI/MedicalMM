<template>
  <div class="app-container">
    <el-form :model="queryParams" ref="queryForm" size="small" :inline="true" v-show="showSearch" label-width="90px">
      <el-form-item label="患者姓名" prop="patientName">
        <el-input v-model="queryParams.patientName" placeholder="请输入患者姓名" clearable @keyup.enter.native="handleQuery" />
      </el-form-item>
      <el-form-item label="图片名" prop="imageName">
        <el-input v-model="queryParams.imageName" placeholder="请输入图片名" clearable @keyup.enter.native="handleQuery" />
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="el-icon-search" size="mini" @click="handleQuery">查询</el-button>
        <el-button icon="el-icon-refresh" size="mini" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <el-row :gutter="10" class="mb8">
      <right-toolbar :showSearch.sync="showSearch" @queryTable="getList" />
    </el-row>

    <el-table v-loading="loading" :data="imageList">
      <el-table-column label="ID" prop="id" width="70" align="center" />
      <el-table-column label="原图" width="90" align="center">
        <template slot-scope="scope">
          <el-image :src="getImageUrl(scope.row.imagePath)" style="width:50px;height:50px" fit="cover" :preview-src-list="[getImageUrl(scope.row.imagePath)]" />
        </template>
      </el-table-column>
      <el-table-column label="患者姓名" prop="patientName" width="120" show-overflow-tooltip />
      <el-table-column label="图片名" prop="imageName" min-width="160" show-overflow-tooltip />
      <el-table-column label="病灶数量" prop="lesionCount" width="100" align="center">
        <template slot-scope="scope">{{ scope.row.lesionCount || 0 }}</template>
      </el-table-column>
      <el-table-column label="AI诊断" prop="diagnosis" min-width="220" show-overflow-tooltip />
      <el-table-column label="创建时间" prop="createTime" width="160" align="center">
        <template slot-scope="scope">{{ parseTime(scope.row.createTime) }}</template>
      </el-table-column>
      <el-table-column label="操作" width="240" align="center">
        <template slot-scope="scope">
          <el-button size="mini" type="text" icon="el-icon-cpu" @click="handleAnalyze(scope.row)" v-hasPermi="['ai:image:analyze']">AI分析</el-button>
          <el-button size="mini" type="text" icon="el-icon-picture" @click="handleCompare(scope.row)" v-hasPermi="['ai:image:view']">查看双图</el-button>
          <el-button size="mini" type="text" icon="el-icon-document" @click="handleRecord(scope.row)" v-hasPermi="['ai:image:record']">查看病历</el-button>
        </template>
      </el-table-column>
    </el-table>
    <pagination v-show="total > 0" :total="total" :page.sync="queryParams.pageNum" :limit.sync="queryParams.pageSize" @pagination="getList" />

    <el-dialog title="双图对比" :visible.sync="compareOpen" width="90%" custom-class="ai-compare-dialog" append-to-body>
      <el-row :gutter="16" class="compare-row">
        <el-col :span="12">
          <div class="compare-title">原图</div>
          <el-image v-if="current.imagePath" :src="getImageUrl(current.imagePath)" class="compare-image" fit="contain" :preview-src-list="[getImageUrl(current.imagePath)]" />
        </el-col>
        <el-col :span="12">
          <div class="compare-title">AI标注图</div>
          <el-image :src="getImageUrl(getAiResultPath(current))" class="compare-image" fit="contain" :preview-src-list="[getImageUrl(getAiResultPath(current))]" />
        </el-col>
      </el-row>
    </el-dialog>

    <el-dialog title="自动生成病历" :visible.sync="recordOpen" width="760px" append-to-body>
      <el-descriptions :column="1" border v-if="record">
        <el-descriptions-item label="患者姓名">{{ record.patientName || current.patientName }}</el-descriptions-item>
        <el-descriptions-item label="主诉">{{ record.chiefComplaint }}</el-descriptions-item>
        <el-descriptions-item label="现病史">{{ record.presentHistory }}</el-descriptions-item>
        <el-descriptions-item label="既往史">{{ record.pastHistory }}</el-descriptions-item>
        <el-descriptions-item label="体格检查">{{ record.physicalExam }}</el-descriptions-item>
        <el-descriptions-item label="初步诊断">{{ record.initialDiagnosis }}</el-descriptions-item>
      </el-descriptions>
      <el-empty v-else description="暂无自动生成病历，请先执行AI分析" />
    </el-dialog>
  </div>
</template>

<script>
import { listAiImage, analyzeAiImage, getAiImageRecord, aiImageUrl, aiResultPathFromImage } from '@/api/medical/aiImage'

export default {
  name: 'AiImageAnalysis',
  data() {
    return {
      loading: true,
      showSearch: true,
      total: 0,
      imageList: [],
      compareOpen: false,
      recordOpen: false,
      current: {},
      record: null,
      queryParams: {
        pageNum: 1,
        pageSize: 10,
        patientName: undefined,
        imageName: undefined
      }
    }
  },
  created() {
    this.getList()
  },
  methods: {
    getImageUrl(path) {
      return aiImageUrl(path)
    },
    getAiResultPath(row) {
      return row.aiResultPath || aiResultPathFromImage(row.imagePath)
    },
    getList() {
      this.loading = true
      listAiImage(this.queryParams).then(res => {
        this.imageList = res.rows || []
        this.total = res.total || 0
        this.loading = false
      })
    },
    handleQuery() {
      this.queryParams.pageNum = 1
      this.getList()
    },
    resetQuery() {
      this.resetForm('queryForm')
      this.handleQuery()
    },
    handleAnalyze(row) {
      this.$modal.confirm('确认对该胸片执行AI分析？').then(() => {
        return analyzeAiImage(row.id)
      }).then(res => {
        this.$modal.msgSuccess(res.msg || '检测成功')
        this.getList()
      }).catch(() => {})
    },
    handleCompare(row) {
      this.current = row
      this.compareOpen = true
    },
    handleRecord(row) {
      this.current = row
      getAiImageRecord(row.id).then(res => {
        this.record = res.data || null
        this.recordOpen = true
      })
    }
  }
}
</script>

<style scoped>
.compare-title {
  margin-bottom: 8px;
  color: #1e3a5f;
  font-weight: 600;
  text-align: center;
}

.compare-row {
  min-height: 76vh;
}

.compare-image {
  width: 100%;
  height: 76vh;
  background: #f7fbfc;
  border: 1px solid rgba(30, 58, 95, 0.08);
  border-radius: 12px;
}

::v-deep .ai-compare-dialog {
  margin-top: 5vh !important;
}

::v-deep .ai-compare-dialog .el-dialog__body {
  padding: 16px 20px 22px;
}
</style>
