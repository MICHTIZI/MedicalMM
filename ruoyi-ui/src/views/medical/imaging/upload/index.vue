<template>
  <!-- 医学影像 - 批量上传 UTF-8 -->
  <div class="app-container">
    <el-card>
      <div slot="header"><span>影像批量上传</span></div>
      <el-upload
        ref="uploader"
        drag
        multiple
        action="#"
        :auto-upload="false"
        :file-list="fileList"
        :on-change="onChange"
        :on-remove="onRemove"
        accept=".png,.jpg,.jpeg,.dcm,.dicom"
      >
        <i class="el-icon-upload"></i>
        <div class="el-upload__text">将文件拖到此处，或<em>点击选择</em></div>
        <div class="el-upload__tip" slot="tip">支持 PNG / JPG / DICOM 格式，可批量拖拽</div>
      </el-upload>

      <el-progress v-if="uploading" :percentage="progress" style="margin-top:12px" />

      <el-button type="primary" :loading="uploading" style="margin-top:16px" @click="submitUpload" :disabled="!fileList.length" v-hasPermi="['imaging:xray:upload']">
        开始上传（{{ fileList.length }} 个文件）
      </el-button>

      <el-divider v-if="results.length">上传结果</el-divider>
      <el-table v-if="results.length" :data="results" size="small" border>
        <el-table-column label="缩略图" width="80" align="center">
          <template slot-scope="s">
            <el-image :src="getImageUrl(s.row.imagePath)" style="width:50px;height:50px" fit="cover" />
          </template>
        </el-table-column>
        <el-table-column label="文件名" prop="imageName" min-width="200" />
        <el-table-column label="尺寸" width="120">
          <template slot-scope="s">{{ s.row.width }}x{{ s.row.height }}</template>
        </el-table-column>
        <el-table-column label="ID" prop="id" width="80" />
      </el-table>
    </el-card>
  </div>
</template>

<script>
import request from '@/utils/request'
import { imageUrl } from '@/api/medical/imaging/xray'

export default {
  name: 'ImagingUpload',
  data() {
    return {
      fileList: [],
      uploading: false,
      progress: 0,
      results: []
    }
  },
  methods: {
    getImageUrl(path) {
      return imageUrl(path)
    },
    onChange(file, list) { this.fileList = list },
    onRemove(file, list) { this.fileList = list },
    submitUpload() {
      if (!this.fileList.length) return
      this.uploading = true
      this.progress = 0
      this.results = []

      const formData = new FormData()
      this.fileList.forEach(f => {
        formData.append('files', f.raw)
      })

      request({
        url: '/imaging/xray/upload',
        method: 'post',
        data: formData,
        headers: {
          repeatSubmit: false
        },
        timeout: 600000,
        onUploadProgress: e => {
          if (e.total > 0) this.progress = Math.round(e.loaded / e.total * 100)
        }
      }).then(res => {
        this.uploading = false
        if (res.code === 200) {
          this.$modal.msgSuccess(res.msg || '上传成功')
          this.results = res.data || []
          this.fileList = []
          this.$refs.uploader.clearFiles()
        } else {
          this.$modal.msgError(res.msg || '上传失败')
        }
      }).catch(() => { this.uploading = false })
    }
  }
}
</script>
