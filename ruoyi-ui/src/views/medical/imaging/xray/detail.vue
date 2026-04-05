<template>
  <!-- 医学影像 - 影像详情 UTF-8 -->
  <div class="app-container">
    <el-card v-loading="loading">
      <div slot="header" class="clearfix">
        <span>影像详情 #{{ imageId }}</span>
        <el-button style="float:right;padding:3px 0" type="text" @click="goBack">返回</el-button>
      </div>
      <template v-if="detail">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="文件名">{{ detail.imageName }}</el-descriptions-item>
          <el-descriptions-item label="尺寸">{{ detail.width }} x {{ detail.height }}</el-descriptions-item>
          <el-descriptions-item label="标签">{{ detail.labelStr === '1' ? '异常' : '正常' }}</el-descriptions-item>
          <el-descriptions-item label="疾病">{{ detail.diseases || '无' }}</el-descriptions-item>
          <el-descriptions-item label="路径">{{ detail.imagePath }}</el-descriptions-item>
          <el-descriptions-item label="入库时间">{{ parseTime(detail.createTime) }}</el-descriptions-item>
        </el-descriptions>
        <el-divider>高清大图</el-divider>
        <div class="img-preview">
          <el-image :src="getImageUrl(detail.imagePath)" fit="contain" style="max-width:100%;max-height:600px" :preview-src-list="[getImageUrl(detail.imagePath)]" />
        </div>
      </template>
    </el-card>
  </div>
</template>

<script>
import { getXray, imageUrl } from '@/api/medical/imaging/xray'

export default {
  name: 'ImagingXrayDetail',
  data() {
    return { loading: true, imageId: undefined, detail: null }
  },
  created() {
    this.imageId = this.$route.query.id ? parseInt(this.$route.query.id, 10) : undefined
    if (this.imageId) this.load()
    else this.loading = false
  },
  methods: {
    getImageUrl(path) {
      return imageUrl(path)
    },
    goBack() { this.$router.push('/imaging/xray') },
    load() {
      this.loading = true
      getXray(this.imageId).then(res => {
        this.detail = res.data
        this.loading = false
      }).catch(() => { this.loading = false })
    }
  }
}
</script>

<style scoped>
.img-preview { text-align: center; padding: 12px; }
</style>
