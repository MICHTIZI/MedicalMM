<template>
  <!-- 电子病历 - 病历详情 UTF-8 -->
  <div class="app-container">
    <el-card v-loading="loading">
      <div slot="header" class="clearfix">
        <span>病历详情 #{{ recordId }}</span>
        <el-button style="float:right;padding:3px 0" type="text" @click="goBack">返回</el-button>
      </div>
      <template v-if="detail">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="标题">{{ detail.record.title }}</el-descriptions-item>
          <el-descriptions-item label="类型">{{ fmtRecordType(detail.record.recordType) }}</el-descriptions-item>
          <el-descriptions-item label="疾病">{{ detail.record.disease }}</el-descriptions-item>
          <el-descriptions-item label="已归档">{{ detail.record.archiveStatus === '1' ? '是' : '否' }}</el-descriptions-item>
          <el-descriptions-item label="创建时间">{{ parseTime(detail.record.createTime) }}</el-descriptions-item>
        </el-descriptions>
        <el-divider>病历原文高亮展示</el-divider>
        <div class="highlight-box" v-html="detail.highlightedHtml"></div>
        <el-divider>病历原文</el-divider>
        <div class="html-box" v-html="detail.record.content"></div>
        <el-divider>实体列表</el-divider>
        <el-table :data="detail.entities" size="small" border>
          <el-table-column label="实体类型" prop="labelType" width="120" />
          <el-table-column label="实体文本" prop="entityText" min-width="160" />
          <el-table-column label="起始位置" prop="startPos" width="80" />
          <el-table-column label="结束位置" prop="endPos" width="80" />
        </el-table>
      </template>
    </el-card>
  </div>
</template>

<script>
import { getRecord } from '@/api/medical/emr/record'

export default {
  name: 'EmrRecordDetail',
  data() {
    return {
      loading: true,
      recordId: undefined,
      detail: null
    }
  },
  created() {
    this.recordId = this.$route.query.id ? parseInt(this.$route.query.id, 10) : undefined
    if (this.recordId) {
      this.load()
    } else {
      this.loading = false
    }
  },
  methods: {
    fmtRecordType(code) {
      const m = { admission: '入院记录', discharge: '出院记录', surgery: '手术记录', course: '病程记录' }
      return m[code] || code || '-'
    },
    goBack() {
      this.$router.push('/emr/record')
    },
    load() {
      this.loading = true
      getRecord(this.recordId).then(response => {
        this.detail = response.data
        this.loading = false
      }).catch(() => { this.loading = false })
    }
  }
}
</script>

<style scoped>
.highlight-box, .html-box {
  padding: 12px;
  border: 1px solid #ebeef5;
  border-radius: 4px;
  min-height: 80px;
  line-height: 1.6;
}
.highlight-box >>> mark.emr-entity { background: #fff3cd; }
</style>
