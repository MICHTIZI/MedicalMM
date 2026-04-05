<template>
  <!-- 医学影像 - 单张标注编辑 UTF-8 -->
  <div class="app-container">
    <el-card v-loading="loading">
      <div slot="header" class="clearfix">
        <span>影像标注 #{{ imageId }}</span>
        <el-button style="float:right;padding:3px 0" type="text" @click="goBack">返回</el-button>
      </div>
      <template v-if="detail">
        <el-row :gutter="20">
          <el-col :span="12">
            <div class="img-preview">
              <el-image :src="getImageUrl(detail.imagePath)" fit="contain" style="max-width:100%;max-height:500px" :preview-src-list="[getImageUrl(detail.imagePath)]" />
            </div>
            <p class="img-info">{{ detail.imageName }} ({{ detail.width }}x{{ detail.height }})</p>
          </el-col>
          <el-col :span="12">
            <h4>选择疾病标签（可多选）</h4>
            <el-checkbox-group v-model="selectedDiseases">
              <el-checkbox v-for="d in diseaseOptions" :key="d" :label="d" style="display:block;margin:6px 0">{{ d }}</el-checkbox>
            </el-checkbox-group>
            <el-divider />
            <el-button type="primary" @click="saveAnnotation" v-hasPermi="['imaging:xray:annotate']">保存标注</el-button>
            <el-button @click="clearAnnotation">清空标注</el-button>
          </el-col>
        </el-row>
      </template>
    </el-card>
  </div>
</template>

<script>
import { getXray, annotateXray, getDiseaseLabels, imageUrl } from '@/api/medical/imaging/xray'

export default {
  name: 'ImagingAnnotateEdit',
  data() {
    return {
      loading: true,
      imageId: undefined,
      detail: null,
      diseaseOptions: [],
      selectedDiseases: []
    }
  },
  created() {
    this.imageId = this.$route.query.id ? parseInt(this.$route.query.id, 10) : undefined
    this.loadLabels()
    if (this.imageId) this.loadDetail()
    else this.loading = false
  },
  methods: {
    getImageUrl(path) {
      return imageUrl(path)
    },
    goBack() { this.$router.push('/imaging/annotate') },
    loadLabels() {
      getDiseaseLabels().then(res => {
        this.diseaseOptions = res.data || []
      })
    },
    loadDetail() {
      this.loading = true
      getXray(this.imageId).then(res => {
        this.detail = res.data
        if (this.detail && this.detail.diseases) {
          this.selectedDiseases = this.detail.diseases.split('|').filter(s => s)
        }
        this.loading = false
      }).catch(() => { this.loading = false })
    },
    saveAnnotation() {
      const diseases = this.selectedDiseases.join('|')
      annotateXray(this.imageId, { diseases }).then(() => {
        this.$modal.msgSuccess('标注已保存')
        this.loadDetail()
      })
    },
    clearAnnotation() {
      this.selectedDiseases = []
      annotateXray(this.imageId, { diseases: '' }).then(() => {
        this.$modal.msgSuccess('标注已清空')
        this.loadDetail()
      })
    }
  }
}
</script>

<style scoped>
.img-preview { text-align: center; padding: 12px; border: 1px solid #ebeef5; border-radius: 4px; }
.img-info { text-align: center; color: #909399; font-size: 13px; margin-top: 8px; }
</style>
