<template>
  <div class="app-container image-segment">
    <el-card shadow="never" class="is-card">
      <div slot="header" class="is-card__head">
        <span>精准诊断输入</span>
        <div>
          <el-button size="mini" icon="el-icon-download" @click="loadFromContext">从检测结果带入</el-button>
          <el-button size="mini" icon="el-icon-refresh-left" @click="reset">重置</el-button>
        </div>
      </div>
      <el-form :inline="false" label-width="110px" size="small">
        <el-form-item label="MinIO 路径">
          <el-input v-model="imagePath" placeholder="自动带入影像检测页的 minio_image_path" clearable>
            <el-button slot="append" icon="el-icon-aim" @click="goDetect">前往检测</el-button>
          </el-input>
        </el-form-item>
        <el-form-item label="病灶边界框">
          <el-table :data="boxes" size="mini" empty-text="暂无边界框">
            <el-table-column label="#" type="index" width="50" />
            <el-table-column label="标签" prop="label">
              <template slot-scope="s">
                <el-input v-model="s.row.label" size="mini" />
              </template>
            </el-table-column>
            <el-table-column label="置信度" width="110">
              <template slot-scope="s">
                <el-input-number v-model="s.row.confidence" size="mini" :min="0" :max="1" :step="0.05" :precision="2" controls-position="right" />
              </template>
            </el-table-column>
            <el-table-column label="x1/y1">
              <template slot-scope="s">
                <el-input-number v-model="s.row.x1" size="mini" :min="0" controls-position="right" />
                <el-input-number v-model="s.row.y1" size="mini" :min="0" controls-position="right" />
              </template>
            </el-table-column>
            <el-table-column label="x2/y2">
              <template slot-scope="s">
                <el-input-number v-model="s.row.x2" size="mini" :min="0" controls-position="right" />
                <el-input-number v-model="s.row.y2" size="mini" :min="0" controls-position="right" />
              </template>
            </el-table-column>
            <el-table-column label="操作" width="70" align="center">
              <template slot-scope="s">
                <el-button type="text" size="mini" @click="boxes.splice(s.$index, 1)">删除</el-button>
              </template>
            </el-table-column>
          </el-table>
          <el-button size="mini" icon="el-icon-plus" style="margin-top:6px" @click="addBox">新增边界框</el-button>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" icon="el-icon-cpu" :loading="aiLoading" @click="handleSegment">AI 精准分析</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-row :gutter="16">
      <el-col :xs="24" :md="12">
        <el-card shadow="never" class="is-card">
          <div slot="header" class="is-card__head"><span>原始影像 + 边界框</span></div>
          <div v-if="!imagePath" class="is-empty">
            <i class="el-icon-picture-outline" />
            <p>请先指定 MinIO 路径。</p>
          </div>
          <div v-else class="is-preview">
            <div class="is-preview__canvas" :style="{ width: canvasW + 'px', height: canvasH + 'px' }">
              <img :src="previewUrl" class="is-preview__img" @load="onImgLoad" @error="onImgError" />
              <svg
                v-if="imgLoaded"
                class="is-preview__svg"
                :viewBox="`0 0 ${imgNatW} ${imgNatH}`"
                preserveAspectRatio="xMidYMid meet"
              >
                <g v-for="(d, idx) in displayBoxes" :key="'bx' + idx">
                  <rect
                    :x="d.x1" :y="d.y1"
                    :width="Math.max(1, d.x2 - d.x1)" :height="Math.max(1, d.y2 - d.y1)"
                    :stroke="colorOf(idx)" stroke-width="2" fill="none"
                  />
                  <text :x="d.x1" :y="Math.max(d.y1 - 4, 12)" :fill="colorOf(idx)" font-size="14" font-weight="600">
                    {{ d.label }}
                  </text>
                </g>
              </svg>
            </div>
          </div>
        </el-card>
      </el-col>

      <el-col :xs="24" :md="12">
        <el-card shadow="never" class="is-card">
          <div slot="header" class="is-card__head"><span>分割 / 精准分析结果</span></div>
          <div v-if="!segResult" class="is-empty">
            <i class="el-icon-cpu" />
            <p>请在上方配置影像与边界框后点击「AI 精准分析」。</p>
          </div>
          <div v-else>
            <!-- 分割影像（着色渲染） -->
            <div class="is-preview">
              <div class="is-preview__canvas" :style="{ width: canvasW + 'px', height: canvasH + 'px' }">
                <img :src="previewUrl" class="is-preview__img is-preview__img--grey" />
                <svg
                  class="is-preview__svg"
                  :viewBox="`0 0 ${imgNatW} ${imgNatH}`"
                  preserveAspectRatio="xMidYMid meet"
                >
                  <g v-for="(d, idx) in segBoxes" :key="'sx' + idx">
                    <rect
                      :x="d.x1" :y="d.y1"
                      :width="Math.max(1, d.x2 - d.x1)" :height="Math.max(1, d.y2 - d.y1)"
                      :fill="colorOf(idx)" fill-opacity="0.25"
                      :stroke="colorOf(idx)" stroke-width="2"
                    />
                    <text :x="d.x1" :y="Math.max(d.y1 - 4, 12)" :fill="colorOf(idx)" font-size="14" font-weight="600">
                      {{ d.label }} ({{ (d.confidence * 100).toFixed(0) }}%)
                    </text>
                  </g>
                </svg>
              </div>
            </div>

            <!-- 异常区域详情 -->
            <div class="is-section">
              <div class="is-section__title"><i class="el-icon-aim" /> 异常区域详情</div>
              <div v-for="(d, idx) in segBoxes" :key="'det' + idx" class="is-abn-row">
                <div class="is-abn-row__dot" :style="{ background: colorOf(idx) }" />
                <div class="is-abn-row__body">
                  <div class="is-abn-row__title">{{ d.label }}</div>
                  <div class="is-abn-row__meta">
                    位置：({{ d.x1 }},{{ d.y1 }}) ~ ({{ d.x2 }},{{ d.y2 }}) ·
                    尺寸：{{ Math.max(0, d.x2 - d.x1) }} × {{ Math.max(0, d.y2 - d.y1) }} ·
                    置信度：{{ (d.confidence * 100).toFixed(0) }}%
                  </div>
                </div>
              </div>
            </div>

            <!-- AI 结论 -->
            <div class="is-section">
              <div class="is-section__title"><i class="el-icon-s-flag" /> AI 诊断结论（可编辑）</div>
              <el-input v-model="segConclusion" type="textarea" :rows="3" placeholder="AI 生成的病灶性质判断与疑似疾病" />
            </div>

            <div class="is-section">
              <div class="is-section__title"><i class="el-icon-s-order" /> 诊断建议（可编辑）</div>
              <el-input v-model="segSuggestion" type="textarea" :rows="3" placeholder="AI 给出的诊断建议 / 进一步检查" />
            </div>

            <div class="is-actions">
              <el-button type="success" icon="el-icon-check" @click="saveContext">保存到诊断上下文</el-button>
              <el-button type="primary" icon="el-icon-s-promotion" @click="toReport">用于生成报告</el-button>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script>
import { imageUrl } from '@/api/medical/imaging/xray'
import { aiSegmentLesion } from '@/api/physician/ai'
import { getContext, patchContext } from '@/api/physician/diagnosisContext'

const PALETTE = ['#e54d42', '#f39c12', '#165dff', '#18a058', '#9b51e0', '#00b8d4']

export default {
  name: 'PhysicianImageSegment',
  data() {
    return {
      imagePath: '',
      boxes: [],
      aiLoading: false,
      segResult: null,
      segConclusion: '',
      segSuggestion: '',
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
    },
    displayBoxes() { return this.boxes || [] },
    segBoxes() {
      if (!this.segResult) return []
      // 优先使用接口返回的 detections
      const det = this.segResult.detections || this.segResult.data || []
      return det
    }
  },
  created() {
    this.loadFromContext()
  },
  methods: {
    loadFromContext() {
      const ctx = getContext()
      if (ctx.image && ctx.image.path) this.imagePath = ctx.image.path
      if (ctx.image && Array.isArray(ctx.image.detection)) {
        this.boxes = ctx.image.detection.map(d => ({ ...d }))
      }
      if (ctx.image && ctx.image.segment) {
        this.segResult = ctx.image.segment
        this.segConclusion = ctx.image.segment.conclusion || ''
        this.segSuggestion = ctx.image.segment.suggestion || ''
      }
    },
    addBox() {
      this.boxes.push({ label: '可疑病灶', confidence: 0.6, x1: 0, y1: 0, x2: 100, y2: 100 })
    },
    reset() {
      this.boxes = []
      this.segResult = null
      this.segConclusion = ''
      this.segSuggestion = ''
    },
    goDetect() {
      this.$router.push({ path: '/physician/image-detect', query: { path: this.imagePath || undefined } })
    },
    handleSegment() {
      if (!this.imagePath) {
        this.$message.warning('请先指定 MinIO 影像路径')
        return
      }
      this.aiLoading = true
      const payload = {
        minio_image_path: this.imagePath,
        detections: this.boxes
      }
      aiSegmentLesion(payload).then(res => {
        // 兼容两种返回：res 本身或 res.data
        const body = res && res.data ? res.data : res
        this.segResult = body
        // 自动生成结论与建议文本
        const boxes = body.detections || body.data || []
        if (!this.segConclusion && boxes.length) {
          this.segConclusion = boxes.map(b => `${b.label}（置信度 ${(b.confidence * 100).toFixed(0)}%）`).join('；')
        }
        if (!this.segSuggestion) {
          this.segSuggestion = '结合病灶位置与形态，建议进一步完善临床检查并结合电子病历信息综合判断。'
        }
        this.$message.success(res.msg || '精准分析完成')
      }).catch(() => {}).finally(() => { this.aiLoading = false })
    },
    saveContext() {
      patchContext({
        image: {
          path: this.imagePath,
          detection: this.boxes,
          segment: {
            ...(this.segResult || {}),
            conclusion: this.segConclusion,
            suggestion: this.segSuggestion
          }
        }
      })
      this.$message.success('已保存，后续可在报告中复用')
    },
    toReport() {
      this.saveContext()
      this.$router.push('/physician/report/edit')
    },
    onImgLoad(e) {
      const img = e.target
      this.imgNatW = img.naturalWidth || 1
      this.imgNatH = img.naturalHeight || 1
      const scale = Math.min(1, 500 / this.imgNatW)
      this.canvasW = Math.floor(this.imgNatW * scale)
      this.canvasH = Math.floor(this.imgNatH * scale)
      this.imgLoaded = true
    },
    onImgError() { this.$message.error('影像加载失败'); this.imgLoaded = false },
    colorOf(i) { return PALETTE[i % PALETTE.length] }
  }
}
</script>

<style lang="scss" scoped>
.image-segment {
  .is-card { border: 1px solid #e8ecf2; border-radius: 6px; margin-bottom: 16px; }
  .is-card__head { display: flex; align-items: center; justify-content: space-between; font-weight: 600; color: #333; }
  .is-empty {
    color: #999; text-align: center; padding: 60px 0;
    i { font-size: 42px; color: #c0c4cc; display: block; margin-bottom: 12px; }
  }
  .is-preview { display: flex; justify-content: center; padding: 10px; background: #fafafa; border-radius: 4px; }
  .is-preview__canvas { position: relative; }
  .is-preview__img { display: block; width: 100%; height: 100%; object-fit: contain; }
  .is-preview__img--grey { filter: grayscale(0.4) brightness(0.95); }
  .is-preview__svg { position: absolute; left: 0; top: 0; width: 100%; height: 100%; pointer-events: none; }
  .is-section { padding: 10px 0; border-bottom: 1px dashed #e8ecf2; }
  .is-section__title { font-weight: 600; color: #165dff; font-size: 14px; margin-bottom: 8px; i { margin-right: 4px; } }
  .is-abn-row { display: flex; align-items: flex-start; margin-bottom: 6px; }
  .is-abn-row__dot { width: 10px; height: 10px; border-radius: 2px; margin: 6px 10px 0 0; }
  .is-abn-row__title { font-weight: 600; color: #333; font-size: 13px; }
  .is-abn-row__meta { color: #666; font-size: 12px; margin-top: 4px; }
  .is-actions { text-align: right; margin-top: 10px; }
}
</style>
