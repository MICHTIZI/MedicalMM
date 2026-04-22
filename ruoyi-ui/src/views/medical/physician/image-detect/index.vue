<template>
  <div class="app-container image-detect">
    <el-row :gutter="16">
      <!-- 左侧：输入 -->
      <el-col :xs="24" :md="10">
        <el-card shadow="never" class="id-card">
          <div slot="header" class="id-card__head">
            <span>影像来源</span>
            <el-radio-group v-model="inputMode" size="mini">
              <el-radio-button label="path">MinIO 路径</el-radio-button>
              <el-radio-button label="select">从影像库选择</el-radio-button>
            </el-radio-group>
          </div>

          <div v-show="inputMode === 'path'">
            <el-form size="small" label-width="110px" @submit.native.prevent>
              <el-form-item label="影像路径">
                <el-input
                  v-model="imagePath"
                  placeholder="例如: medical-imaging/chest_224_hd/chest_00000_正常.png"
                  clearable
                />
              </el-form-item>
            </el-form>
          </div>

          <div v-show="inputMode === 'select'">
            <el-form :inline="true" size="mini" @submit.native.prevent>
              <el-form-item label="关键词">
                <el-input v-model="xrayQuery.keyword" placeholder="文件名/疾病" clearable @keyup.enter.native="queryXray" style="width:200px" />
              </el-form-item>
              <el-form-item>
                <el-button type="primary" icon="el-icon-search" @click="queryXray">查询</el-button>
              </el-form-item>
            </el-form>
            <el-table
              :data="xrayList"
              v-loading="xrayLoading"
              size="mini"
              height="320"
              highlight-current-row
              @current-change="onXraySelect"
              empty-text="请输入关键词查询影像"
            >
              <el-table-column label="ID" prop="id" width="70" />
              <el-table-column label="路径" prop="imagePath" show-overflow-tooltip />
              <el-table-column label="疾病" prop="disease" width="100" show-overflow-tooltip />
            </el-table>
            <div class="id-card__selected" v-if="imagePath">
              已选：<el-tag size="small">{{ imagePath }}</el-tag>
            </div>
          </div>

          <div class="id-card__actions">
            <el-button type="primary" icon="el-icon-aim" :loading="aiLoading" @click="handleDetect">AI 病灶检测</el-button>
            <el-button icon="el-icon-refresh-left" @click="resetForm">重置</el-button>
          </div>
        </el-card>

        <!-- 病灶列表卡片 -->
        <el-card shadow="never" class="id-card" v-if="detections && detections.length">
          <div slot="header" class="id-card__head"><span>病灶信息</span></div>
          <div class="id-legend">
            <div
              v-for="(d, idx) in detections"
              :key="'leg' + idx"
              class="id-legend__item"
              :class="{ active: activeIdx === idx }"
              @mouseenter="activeIdx = idx"
              @mouseleave="activeIdx = -1"
            >
              <span class="id-legend__dot" :style="{ background: colorOf(idx) }" />
              <div class="id-legend__body">
                <div class="id-legend__label">{{ d.label }}</div>
                <div class="id-legend__meta">
                  置信度
                  <el-tag size="mini" :type="confTagType(d.confidence)">{{ (d.confidence * 100).toFixed(0) }}%</el-tag>
                  <span class="id-legend__coords">({{ d.x1 }},{{ d.y1 }}) → ({{ d.x2 }},{{ d.y2 }})</span>
                </div>
              </div>
            </div>
          </div>
        </el-card>
      </el-col>

      <!-- 右侧：可视化 -->
      <el-col :xs="24" :md="14">
        <el-card shadow="never" class="id-card">
          <div slot="header" class="id-card__head">
            <span>影像预览与病灶标注</span>
            <div v-if="detections && detections.length">
              <el-button size="mini" type="primary" icon="el-icon-magic-stick" @click="toSegment">
                一键进入精准诊断
              </el-button>
            </div>
          </div>

          <div v-if="!imagePath" class="id-empty">
            <i class="el-icon-picture-outline" />
            <p>请先输入或选择一张 MinIO 影像。</p>
          </div>

          <div v-else class="id-preview" ref="previewWrap">
            <div class="id-preview__canvas" :style="{ width: canvasW + 'px', height: canvasH + 'px' }">
              <img
                :src="previewUrl"
                class="id-preview__img"
                ref="img"
                @load="onImageLoad"
                @error="onImageError"
              />
              <svg
                v-if="imgLoaded && detections && detections.length"
                class="id-preview__svg"
                :viewBox="`0 0 ${imgNatW} ${imgNatH}`"
                preserveAspectRatio="xMidYMid meet"
              >
                <g v-for="(d, idx) in detections" :key="'box' + idx">
                  <rect
                    :x="d.x1" :y="d.y1"
                    :width="Math.max(1, d.x2 - d.x1)" :height="Math.max(1, d.y2 - d.y1)"
                    :stroke="colorOf(idx)" :stroke-width="activeIdx === idx ? 4 : 2"
                    fill="none"
                  />
                  <text
                    :x="d.x1" :y="Math.max(d.y1 - 4, 12)" :fill="colorOf(idx)"
                    font-size="14" font-weight="600"
                  >{{ d.label }} ({{ (d.confidence * 100).toFixed(0) }}%)</text>
                </g>
              </svg>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script>
import { listXray, imageUrl } from '@/api/medical/imaging/xray'
import { aiDetectLesion } from '@/api/physician/ai'
import { patchContext } from '@/api/physician/diagnosisContext'

const PALETTE = ['#165dff', '#18a058', '#f39c12', '#e54d42', '#9b51e0', '#00b8d4', '#2bb0ed', '#c0392b']

export default {
  name: 'PhysicianImageDetect',
  data() {
    return {
      inputMode: 'path',
      imagePath: '',
      xrayQuery: { keyword: '', pageNum: 1, pageSize: 20 },
      xrayList: [],
      xrayLoading: false,
      aiLoading: false,
      detections: [],
      activeIdx: -1,
      imgLoaded: false,
      imgNatW: 1,
      imgNatH: 1,
      canvasW: 500,
      canvasH: 500
    }
  },
  computed: {
    previewUrl() {
      if (!this.imagePath) return ''
      return imageUrl(this.imagePath)
    }
  },
  watch: {
    imagePath() {
      this.imgLoaded = false
    }
  },
  mounted() {
    // 若从检测页返回或跳转来，带上次数据
    const q = this.$route.query || {}
    if (q.path) this.imagePath = q.path
  },
  methods: {
    queryXray() {
      this.xrayLoading = true
      listXray(this.xrayQuery).then(res => {
        this.xrayList = res.rows || []
      }).finally(() => { this.xrayLoading = false })
    },
    onXraySelect(row) {
      if (!row) return
      this.imagePath = row.imagePath
    },
    handleDetect() {
      if (!this.imagePath) {
        this.$message.warning('请先输入或选择影像路径')
        return
      }
      this.aiLoading = true
      aiDetectLesion(this.imagePath).then(res => {
        const arr = Array.isArray(res.data) ? res.data : []
        this.detections = arr.map(d => ({
          label: d.label || '未知',
          confidence: Number(d.confidence) || 0,
          x1: Number(d.x1) || 0,
          y1: Number(d.y1) || 0,
          x2: Number(d.x2) || 0,
          y2: Number(d.y2) || 0
        }))
        if (!this.detections.length) {
          this.$message.info('未检测到可疑病灶')
        } else {
          this.$message.success(res.msg || `检出 ${this.detections.length} 处可疑病灶`)
        }
        // 保存上下文
        patchContext({
          image: {
            path: this.imagePath,
            detection: this.detections,
            segment: null
          }
        })
      }).catch(() => {}).finally(() => { this.aiLoading = false })
    },
    resetForm() {
      this.imagePath = ''
      this.detections = []
      this.imgLoaded = false
    },
    onImageLoad(e) {
      const img = e.target
      this.imgNatW = img.naturalWidth || 1
      this.imgNatH = img.naturalHeight || 1
      // 依据容器宽度自适应
      const wrap = this.$refs.previewWrap
      const maxW = wrap ? wrap.clientWidth - 20 : 500
      const scale = Math.min(1, maxW / this.imgNatW)
      this.canvasW = Math.floor(this.imgNatW * scale)
      this.canvasH = Math.floor(this.imgNatH * scale)
      this.imgLoaded = true
    },
    onImageError() {
      this.$message.error('影像加载失败，请检查 MinIO 路径或后端代理')
      this.imgLoaded = false
    },
    colorOf(idx) { return PALETTE[idx % PALETTE.length] },
    confTagType(c) {
      if (c >= 0.8) return 'danger'
      if (c >= 0.6) return 'warning'
      return 'info'
    },
    toSegment() {
      patchContext({
        image: {
          path: this.imagePath,
          detection: this.detections
        }
      })
      this.$router.push('/physician/image-segment')
    }
  }
}
</script>

<style lang="scss" scoped>
.image-detect {
  .id-card { border: 1px solid #e8ecf2; border-radius: 6px; margin-bottom: 16px; }
  .id-card__head { display: flex; align-items: center; justify-content: space-between; font-weight: 600; color: #333; }
  .id-card__actions { margin-top: 12px; display: flex; gap: 8px; }
  .id-card__selected { margin-top: 8px; font-size: 13px; color: #666; }

  .id-empty {
    color: #999; text-align: center; padding: 80px 0;
    i { font-size: 50px; color: #c0c4cc; display: block; margin-bottom: 12px; }
  }

  .id-preview {
    display: flex; justify-content: center; align-items: center; padding: 10px;
    background: #fafafa; border-radius: 4px; min-height: 400px;
  }
  .id-preview__canvas { position: relative; }
  .id-preview__img { display: block; width: 100%; height: 100%; object-fit: contain; }
  .id-preview__svg { position: absolute; left: 0; top: 0; width: 100%; height: 100%; pointer-events: none; }

  .id-legend__item {
    display: flex; align-items: flex-start; padding: 8px 10px;
    border: 1px solid transparent; border-radius: 4px; cursor: pointer; margin-bottom: 6px;
    transition: all 0.2s;
    &.active, &:hover { border-color: #165dff; background: #f5f9ff; }
  }
  .id-legend__dot { width: 10px; height: 10px; border-radius: 2px; margin: 6px 10px 0 0; flex-shrink: 0; }
  .id-legend__body { flex: 1; }
  .id-legend__label { font-weight: 600; color: #333; font-size: 13px; }
  .id-legend__meta { color: #666; font-size: 12px; margin-top: 4px; display: flex; align-items: center; gap: 8px; }
  .id-legend__coords { color: #999; }
}
</style>
