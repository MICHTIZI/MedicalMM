<template>
  <div class="pacs-root" ref="pacsRoot">
    <header class="pacs-toolbar">
      <div class="tb-left">
        <el-button type="text" class="tb-txt" icon="el-icon-back" @click="goPatientList">返回患者列表</el-button>
        <el-divider direction="vertical" class="tb-div" />
        <span class="tb-label">患者</span>
        <el-select
          v-model="selectedPatientId"
          filterable
          remote
          clearable
          reserve-keyword
          placeholder="选患者"
          :remote-method="remotePatientSearch"
          :loading="patientSearchLoading"
          size="small"
          class="tb-patient-select"
          @change="onPatientChange"
        >
          <el-option v-for="p in patientHits" :key="p.patientId" :label="formatPatientOption(p)" :value="p.patientId" />
        </el-select>
      </div>

      <div class="tb-center">
        <el-button size="mini" class="tb-btn" icon="el-icon-full-screen" @click="fitToCanvas">铺满画布</el-button>
        <el-button-group class="tb-toggle-group">
          <el-button size="mini" class="tb-btn tb-toggle" :class="{ 'tb-toggle--active': imageViewMode === 'original' }" :type="imageViewMode === 'original' ? 'primary' : ''" :disabled="!currentXray" @click="setImageView('original')">原图</el-button>
          <el-button size="mini" class="tb-btn tb-toggle" :class="{ 'tb-toggle--active': imageViewMode === 'annotated' }" :type="imageViewMode === 'annotated' ? 'primary' : ''" :disabled="!currentXray || !hasAnnotatedView" @click="setImageView('annotated')">标注图</el-button>
        </el-button-group>
        <el-button size="mini" class="tb-btn tb-draw" :class="{ 'tb-draw--active': drawTool === 'rect' }" :type="drawTool === 'rect' ? 'primary' : ''" icon="el-icon-crop" :disabled="!currentXray || imageViewMode !== 'original'" @click="setDrawTool('rect')">矩形标注</el-button>
        <el-button size="mini" class="tb-btn tb-draw" :class="{ 'tb-draw--active': drawTool === 'brush' }" :type="drawTool === 'brush' ? 'primary' : ''" icon="el-icon-edit" :disabled="!currentXray || imageViewMode !== 'original'" @click="setDrawTool('brush')">画笔</el-button>
        <el-button size="mini" class="tb-btn" icon="el-icon-delete" :disabled="!currentXray || imageViewMode !== 'original'" @click="clearBrushTracks">清除画笔</el-button>
        <el-button size="mini" class="tb-btn" icon="el-icon-delete-solid" :disabled="!currentXray || imageViewMode !== 'original'" @click="clearRectMarks">清除矩形</el-button>
        <el-button size="mini" class="tb-btn" type="warning" plain icon="el-icon-upload2" :disabled="!currentXray || overlayUploading" :loading="overlayUploading" title="需先在原图上绘制矩形" @click="commitRectOverlay">上传标注</el-button>
      </div>

      <div class="tb-right">
        <el-button size="mini" class="tb-btn" icon="el-icon-upload" @click="goImportData">导入数据</el-button>
        <el-button size="mini" class="tb-btn cta" type="danger" plain icon="el-icon-cpu" :loading="aiLoading" :disabled="!currentXray" @click="runAiAnalyze">AI 病灶分析</el-button>
        <el-button size="mini" class="tb-btn cta" type="warning" plain icon="el-icon-document-copy" :disabled="!currentXray" :loading="fusionPanelLoading" @click="openFusionReportDialog">生成辅助诊断报告</el-button>
        <el-button size="mini" class="tb-btn" :icon="isFs ? 'el-icon-copy-document' : 'el-icon-full-screen'" @click="toggleFullscreen">{{ isFs ? '退出全屏' : '全屏' }}</el-button>
      </div>
    </header>

    <div class="pacs-main">
      <section class="pacs-left" :style="{ width: leftFrac * 100 + '%' }">
        <div
          class="pacs-stage"
          ref="stage"
          @wheel.prevent="onStageWheel"
          @mousedown="onStageMouseDown"
          @mousemove="onStageMouseMove"
          @mouseup="onStageMouseUp"
          @mouseleave="onStageMouseUp"
        >
          <div v-if="!currentXray" class="stage-empty">请选择患者并加载胸片</div>
          <template v-else>
            <div class="stage-inner" ref="stageInner">
              <div class="img-stack" :style="stackStyle">
                <img
                  ref="imgEl"
                  class="pacs-img"
                  :src="displayImageSrc"
                  draggable="false"
                  @load="onImgLoad"
                  @error="onImgError"
                >
                <!-- AI 病灶框与手工标注层（仅原图模式叠加矢量框；标注图为 MinIO 整图） -->
                <div v-if="natW && natH && imageViewMode === 'original'" class="lesion-layer">
                  <div
                    v-for="(lesion, idx) in displayLesions"
                    :key="'ai-'+idx"
                    class="lesion-box ai-lesion"
                    :style="lesionStyle(lesion)"
                    @mouseenter="hoverLesion = idx"
                    @mouseleave="hoverLesion = null"
                  />
                  <div
                    v-for="(r, idx) in userRects"
                    :key="'ur-'+idx"
                    class="lesion-box user-lesion"
                    :style="normRectStyle(r)"
                  />
                  <svg v-if="rectPreview" class="ruler-svg" :viewBox="`0 0 ${natW} ${natH}`" preserveAspectRatio="none">
                    <rect :x="rectPreview.x" :y="rectPreview.y" :width="rectPreview.w" :height="rectPreview.h" fill="none" stroke="#ff5252" :stroke-width="rulerStrokeWidth" stroke-dasharray="8" />
                  </svg>
                </div>
                <canvas v-show="imageViewMode === 'original' && drawTool === 'brush'" ref="brushCanvas" class="brush-canvas" :width="natW || 800" :height="natH || 800" />
              </div>
              <div v-show="hoverLesion != null && lesionTooltip" class="lesion-tip" :style="tipStyle">{{ lesionTooltip }}</div>
            </div>
            <div v-if="currentXray && xrayList.length" class="stage-nav">
              <button
                type="button"
                class="stage-nav-btn"
                title="上一张"
                :disabled="!canPrevImg"
                @click.stop="prevImage"
              >
                <i class="el-icon-arrow-left" />
              </button>
              <button
                type="button"
                class="stage-nav-btn"
                title="下一张"
                :disabled="!canNextImg"
                @click.stop="nextImage"
              >
                <i class="el-icon-arrow-right" />
              </button>
            </div>
          </template>
        </div>
      </section>

      <div class="pacs-splitter" title="拖拽调整宽度" @mousedown.prevent="startSplitDrag" />

      <aside class="pacs-right" :style="{ width: (1 - leftFrac) * 100 + '%' }">
        <div class="rp-scroll">
          <div class="rp-card">
            <h3 class="rp-title">患者基础信息</h3>
            <template v-if="detailSnapshot">
              <div class="rp-row big">{{ detailSnapshot.patientName }}
                <span class="rp-meta">{{ genderText(detailSnapshot.gender) }} · {{ detailSnapshot.age != null ? detailSnapshot.age + '岁' : '—' }}</span>
              </div>
              <div class="rp-row muted">主治医生：{{ detailSnapshot.attendingDoctor || '—' }}</div>
              <div class="rp-row muted">就诊建档：{{ parseTime(detailSnapshot.createTime) }}</div>
              <div class="rp-tags">
                <el-tag size="mini" effect="dark" :type="diagTag(diagStatus)" :color="diagStatus === 4 ? '#722ED1' : undefined">{{ diagLabel(diagStatus) }}</el-tag>
                <span class="rp-mod" :class="modOk(d.hasImage)"><i :class="modOk(d.hasImage)?'el-icon-success':'el-icon-circle-plus-outline'" />胸片</span>
                <span class="rp-mod" :class="modOk(d.hasMedicalRecord)"><i :class="modOk(d.hasMedicalRecord)?'el-icon-success':'el-icon-circle-plus-outline'" />病历</span>
                <span class="rp-mod" :class="modOk(d.hasLabResult)"><i :class="modOk(d.hasLabResult)?'el-icon-success':'el-icon-circle-plus-outline'" />检验</span>
              </div>
            </template>
            <el-empty v-else description="未加载患者信息" :image-size="48" />
          </div>

          <div class="rp-card">
            <h3 class="rp-title">AI 病灶结果</h3>
            <template v-if="aiResult">
              <div class="rp-kv"><span>病灶数量</span><b>{{ aiResult.lesionCount != null ? aiResult.lesionCount : (aiResult.lesion_list || aiResult.lesionList || []).length }}</b></div>
              <div v-if="aiResult.infection_rate != null || aiResult.infectionRate != null" class="rp-kv"><span>感染率</span><b>{{ fmtPct(aiResult.infection_rate != null ? aiResult.infection_rate : aiResult.infectionRate) }}</b></div>
              <div v-if="aiResult.total_infection_area != null || aiResult.totalInfectionArea != null" class="rp-kv"><span>感染区域面积(px²)</span><b>{{ fmtNum(aiResult.total_infection_area != null ? aiResult.total_infection_area : aiResult.totalInfectionArea) }}</b></div>
              <div v-if="aiResult.severity" class="rp-kv"><span>严重程度</span><b class="sev">{{ aiResult.severity }}</b></div>
              <div v-if="aiResult.pneumonia_type || aiResult.pneumoniaType" class="rp-kv"><span>肺炎类型</span><b>{{ aiResult.pneumonia_type || aiResult.pneumoniaType }}</b></div>
              <div v-if="aiResult.diagnosis" class="rp-block"><span class="lbl">诊断意见</span><p>{{ aiResult.diagnosis }}</p></div>
              <div v-if="aiResult.treatment_suggestion || aiResult.treatmentSuggestion" class="rp-block"><span class="lbl">诊疗建议</span><p>{{ aiResult.treatment_suggestion || aiResult.treatmentSuggestion }}</p></div>
              <div v-if="aiResult.further_examination || aiResult.furtherExamination" class="rp-block"><span class="lbl">进一步检查</span><p>{{ aiResult.further_examination || aiResult.furtherExamination }}</p></div>
              <el-collapse v-if="lesionListNorm.length" class="rp-collapse">
                <el-collapse-item title="逐病灶详情" name="1">
                  <div v-for="(lv, i) in lesionListNorm" :key="i" class="lesion-item">
                    <div class="li-h">病灶 {{ i + 1 }}</div>
                    <div class="rp-kv sm"><span>位置</span><b>{{ lv.full_position || lv.fullPosition || [lv.position, lv.lobe].filter(Boolean).join(' ') || '—' }}</b></div>
                    <div class="rp-kv sm"><span>置信度</span><b>{{ lv.confidence != null ? (Number(lv.confidence) * 100).toFixed(1) + '%' : '—' }}</b></div>
                    <div class="rp-kv sm"><span>面积(px²)</span><b>{{ lv.area != null ? fmtNum(lv.area) : '—' }}</b></div>
                    <div class="rp-kv sm"><span>框尺寸</span><b>{{ lv.width != null && lv.height != null ? fmtNum(lv.width)+'×'+fmtNum(lv.height) : '—' }}</b></div>
                  </div>
                </el-collapse-item>
              </el-collapse>
            </template>
            <div v-else class="rp-placeholder">执行「AI 病灶分析」后展示结构化结果与画框</div>
          </div>

          <div class="rp-card">
            <h3 class="rp-title">电子病历（只读）</h3>
            <pre v-if="recordPlainText" class="rp-pre">{{ recordPlainText }}</pre>
            <div v-else class="rp-placeholder">暂无绑定病历；可点击「导入数据」进入流水录入</div>
          </div>

          <div class="rp-card rp-card-last">
            <h3 class="rp-title">检验对照</h3>
            <div v-if="labCompareRows.length" class="lab-table">
              <div v-for="row in labCompareRows" :key="row.key" class="lab-row" :class="'lvl-'+row.level">
                <span class="lab-name">{{ row.label }}</span>
                <span class="lab-val">{{ row.value }} {{ row.unit }}</span>
                <span class="lab-ref">参考 {{ row.refText }}</span>
              </div>
            </div>
            <pre v-else-if="labRemarkText" class="rp-pre sm">{{ labRemarkText }}</pre>
            <div v-else class="rp-placeholder">暂无检验数据；可点击「导入数据」进入流水录入</div>
          </div>
        </div>
      </aside>
    </div>

    <el-dialog
      title="辅助诊断信息"
      :visible.sync="fusionDialogVisible"
      width="900px"
      append-to-body
      :z-index="5000"
      @closed="onFusionDialogClosed"
    >
      <div v-loading="fusionPanelLoading">
        <template v-if="fusionEnvelope && !fusionPanelLoading">
          <el-collapse v-model="fusionCollapseNames">
            <el-collapse-item title="单模态评分" name="ms">
              <el-descriptions v-if="modalityScores" :column="1" border size="small">
                <el-descriptions-item v-if="modalityScores.image" label="影像 ImgS">{{ modalityScores.image.score }}/10</el-descriptions-item>
                <el-descriptions-item v-if="modalityScores.case" label="病例 CaseS">{{ modalityScores.case.score }}/10</el-descriptions-item>
                <el-descriptions-item v-if="modalityScores.lab" label="检验 LabS">{{ modalityScores.lab.score }}/10</el-descriptions-item>
              </el-descriptions>
              <span v-else>暂无</span>
            </el-collapse-item>
            <el-collapse-item title="一致性校验" name="cc">
              <div v-if="consistencyCheck">
                <p><strong>级别：</strong>{{ consistencyCheck.level || '—' }}</p>
                <ul v-if="consistencyRules.length" style="margin:8px 0 0 18px;padding:0;">
                  <li v-for="(cr, idx) in consistencyRules" :key="idx" style="margin-bottom:6px;">
                    <strong>{{ cr.rule_name || cr.ruleName }}</strong>：{{ cr.report_text || cr.reportText }}
                  </li>
                </ul>
              </div>
              <span v-else>暂无</span>
            </el-collapse-item>
            <el-collapse-item title="综合评分" name="fc">
              <template v-if="fusionCalc">
                <p>原始分 {{ fusionCalcRawScore }}，置信系数 {{ fusionCalcConfidenceFactor }}，最终分 {{ fusionCalcFinalScore }}</p>
                <p v-if="fusionCalcWeights">权重 ImgS={{ fusionCalcWeights.ImgS }}，CaseS={{ fusionCalcWeights.CaseS }}，LabS={{ fusionCalcWeights.LabS }}</p>
              </template>
              <span v-else>暂无</span>
            </el-collapse-item>
            <el-collapse-item title="诊断输出" name="dg">
              <template v-if="diagnosisOutput">
                <p>确诊度：{{ diagnosisGradeCn }}（{{ diagnosisGrade }}）</p>
                <p>严重程度：{{ diagnosisSeverityCn }}（{{ diagnosisSeverity }}）</p>
                <p>处置：{{ diagnosisAction }}</p>
              </template>
              <span v-else>暂无</span>
            </el-collapse-item>
            <el-collapse-item title="结构化处置建议" name="sg">
              <ol v-if="structuredSuggestions.length" style="margin:8px 0 0 18px;padding:0;">
                <li v-for="(s, i) in structuredSuggestions" :key="i" style="margin-bottom:8px;">
                  <el-tag size="mini" :type="s.priority === 'P0' ? 'danger' : (s.priority === 'P1' ? 'warning' : 'info')">{{ s.priority }}</el-tag>
                  {{ s.category }} — {{ s.content }}
                </li>
              </ol>
              <span v-else>暂无</span>
            </el-collapse-item>
          </el-collapse>
          <el-divider />
          <el-form label-width="96px" size="small">
            <el-form-item label="医生签名" required>
              <el-input v-model="doctorSignature" maxlength="64" show-word-limit placeholder="签名" />
            </el-form-item>
            <el-form-item label="医生建议">
              <el-input v-model="doctorAdvice" type="textarea" :rows="3" maxlength="2000" show-word-limit placeholder="选填" />
            </el-form-item>
          </el-form>
        </template>
      </div>
      <span slot="footer">
        <el-button @click="fusionDialogVisible = false">关 闭</el-button>
        <el-button type="primary" :loading="fusionExportLoading" :disabled="!fusionEnvelope || fusionPanelLoading" @click="confirmExportFusionWord">导出 Word 报告</el-button>
      </span>
    </el-dialog>
  </div>
</template>

<script>
import { saveAs } from 'file-saver'
import { listPatientCards, getPatientDiagnosisDetail } from '@/api/medical/patient'
import { listAiImage, analyzeAiImage, fusionAnalyze, exportFusionReport, aiImageUrl, uploadAiImageUserOverlay } from '@/api/medical/aiImage'
import { getFusionReportByImage, saveFusionAnalyze, updateFusionDoctor } from '@/api/medical/fusionReport'
import { listMedicalRecord } from '@/api/medical/record'
import { listLab } from '@/api/medical/labResult'
import { buildLabCompareRows, buildFusionLabPayload } from '@/utils/viewerLabReference'
import { blobValidate } from '@/utils/ruoyi'
import { checkPermi } from '@/utils/permission'

const AI_CACHE_PREFIX = 'pacs_ai_result_'

export default {
  name: 'MedicalPacsViewer',
  data() {
    return {
      patientHits: [],
      patientSearchLoading: false,
      selectedPatientId: undefined,
      xrayList: [],
      currentIndex: 0,
      leftFrac: 0.65,
      splitDrag: false,
      drawTool: null,
      scale: 1,
      tx: 0,
      ty: 0,
      brightness: 1,
      contrast: 1,
      natW: 0,
      natH: 0,
      panning: false,
      panStart: null,
      aiResult: null,
      aiLoading: false,
      hoverLesion: null,
      detailSnapshot: null,
      diagnosisSnapshot: null,
      recordRows: [],
      labRows: [],
      userRects: [],
      rectDrag: null,
      brushDrawing: false,
      brushLast: null,
      isFs: false,
      imgError: false,
      imageViewMode: 'original',
      annotatedImageKey: 0,
      overlayUploading: false,
      fusionDialogVisible: false,
      fusionPanelLoading: false,
      fusionExportLoading: false,
      fusionEnvelope: null,
      fusionReportId: null,
      cachedImageResultObject: null,
      cachedCaseText: '',
      cachedLabDataObject: null,
      doctorSignature: '',
      doctorAdvice: '',
      fusionCollapseNames: ['ms', 'cc', 'fc', 'dg', 'sg']
    }
  },
  computed: {
    currentXray() {
      return this.xrayList[this.currentIndex] || null
    },
    resolvedAiResultPath() {
      const row = this.currentXray
      if (!row) return ''
      if (row.aiResultPath) return row.aiResultPath
      if (this.aiResult) {
        const p = this.aiResult.aiResultPath || this.aiResult.ai_result_path
        if (p) return p
      }
      return ''
    },
    hasAnnotatedView() {
      return !!this.resolvedAiResultPath
    },
    displayImageSrc() {
      if (!this.currentXray || !this.currentXray.imagePath) return ''
      if (this.imageViewMode === 'annotated' && this.hasAnnotatedView) {
        const base = aiImageUrl(this.resolvedAiResultPath)
        if (!base) return aiImageUrl(this.currentXray.imagePath)
        const sep = base.includes('?') ? '&' : '?'
        return base + sep + '_t=' + this.annotatedImageKey
      }
      return aiImageUrl(this.currentXray.imagePath)
    },
    canPrevImg() {
      return this.currentIndex > 0
    },
    canNextImg() {
      return this.currentIndex < this.xrayList.length - 1
    },
    stackStyle() {
      const f = []
      f.push(`translate(${this.tx}px, ${this.ty}px)`)
      f.push(`scale(${this.scale})`)
      return {
        transform: f.join(' '),
        transformOrigin: 'center center',
        filter: `brightness(${this.brightness}) contrast(${this.contrast})`
      }
    },
    lesionListNorm() {
      if (!this.aiResult) return []
      const list = this.aiResult.lesion_list || this.aiResult.lesionList || []
      return Array.isArray(list) ? list : []
    },
    displayLesions() {
      return this.lesionListNorm.filter(l => l.x1 != null && l.y1 != null && l.x2 != null && l.y2 != null)
    },
    lesionTooltip() {
      const i = this.hoverLesion
      if (i == null) return ''
      const l = this.displayLesions[i]
      if (!l) return ''
      const pos = l.full_position || l.fullPosition || [l.position, l.lobe].filter(Boolean).join(' ')
      const conf = l.confidence != null ? `置信度 ${(Number(l.confidence) * 100).toFixed(1)}%` : ''
      const inf = this.aiResult && (this.aiResult.infection_rate != null || this.aiResult.infectionRate != null)
        ? `感染占比 ${this.fmtPct(this.aiResult.infection_rate != null ? this.aiResult.infection_rate : this.aiResult.infectionRate)}`
        : ''
      return [pos, conf, inf].filter(Boolean).join(' · ')
    },
    tipStyle() {
      return { left: '12px', bottom: '12px' }
    },
    d() {
      return this.diagnosisSnapshot || {}
    },
    diagStatus() {
      return this.d.diagnosisStatus != null ? Number(this.d.diagnosisStatus) : 0
    },
    linkedRecord() {
      if (!this.currentXray || !this.recordRows.length) return null
      const id = this.currentXray.id
      const byId = this.recordRows.find(r => r.imageId === id)
      if (byId) return byId
      const stem = this.pathStem(this.currentXray.imagePath)
      return this.recordRows.find(r => this.pathStem(r.imagePath) === stem && stem) || this.recordRows[0]
    },
    recordPlainText() {
      const r = this.linkedRecord
      if (!r) return ''
      const parts = [
        ['主诉', r.chiefComplaint],
        ['现病史', r.presentHistory],
        ['既往史', r.pastHistory],
        ['体格检查', r.physicalExam],
        ['初步诊断', r.initialDiagnosis],
        ['备注', r.remark]
      ]
      return parts
        .filter(([, v]) => v)
        .map(([k, v]) => `【${k}】\n${v}`)
        .join('\n\n')
    },
    latestLab() {
      if (!this.labRows.length) return null
      return [...this.labRows].sort((a, b) => new Date(b.testDate || 0) - new Date(a.testDate || 0))[0]
    },
    labCompareRows() {
      return buildLabCompareRows(this.latestLab)
    },
    labRemarkText() {
      const lab = this.latestLab
      if (!lab || !lab.remark) return ''
      return lab.remark
    },
    fusionCore() {
      const e = this.fusionEnvelope
      if (!e || e.data == null || typeof e.data !== 'object') return null
      return e.data
    },
    modalityScores() {
      const c = this.fusionCore
      return c ? (c.modality_scores || c.modalityScores) : null
    },
    consistencyCheck() {
      const c = this.fusionCore
      return c ? (c.consistency_check || c.consistencyCheck) : null
    },
    consistencyRules() {
      const cc = this.consistencyCheck
      if (!cc) return []
      const r = cc.conflict_rules || cc.conflictRules
      return Array.isArray(r) ? r : []
    },
    fusionCalc() {
      const c = this.fusionCore
      return c ? (c.fusion_calculation || c.fusionCalculation) : null
    },
    fusionCalcWeights() {
      const f = this.fusionCalc
      return f && f.weights ? f.weights : null
    },
    fusionCalcRawScore() {
      const f = this.fusionCalc
      if (!f) return '—'
      const v = f.raw_score != null ? f.raw_score : f.rawScore
      return v != null ? v : '—'
    },
    fusionCalcConfidenceFactor() {
      const f = this.fusionCalc
      if (!f) return '—'
      const v = f.confidence_factor != null ? f.confidence_factor : f.confidenceFactor
      return v != null ? v : '—'
    },
    fusionCalcFinalScore() {
      const f = this.fusionCalc
      if (!f) return '—'
      const v = f.final_score != null ? f.final_score : f.finalScore
      return v != null ? v : '—'
    },
    diagnosisOutput() {
      const c = this.fusionCore
      return c ? (c.diagnosis_output || c.diagnosisOutput) : null
    },
    diagnosisGradeCn() {
      const d = this.diagnosisOutput
      return d ? (d.grade_cn || d.gradeCn || '—') : '—'
    },
    diagnosisGrade() {
      const d = this.diagnosisOutput
      return d ? (d.grade || '—') : '—'
    },
    diagnosisSeverityCn() {
      const d = this.diagnosisOutput
      return d ? (d.severity_cn || d.severityCn || '—') : '—'
    },
    diagnosisSeverity() {
      const d = this.diagnosisOutput
      return d ? (d.severity || '—') : '—'
    },
    diagnosisAction() {
      const d = this.diagnosisOutput
      return d ? (d.action || '—') : '—'
    },
    structuredSuggestions() {
      const c = this.fusionCore
      const s = c && (c.structured_suggestions || c.structuredSuggestions)
      return Array.isArray(s) ? s : []
    },
    rectPreview() {
      if (!this.rectDrag || !this.natW) return null
      const x1 = Math.min(this.rectDrag.x0, this.rectDrag.x1) * this.natW
      const y1 = Math.min(this.rectDrag.y0, this.rectDrag.y1) * this.natH
      const x2 = Math.max(this.rectDrag.x0, this.rectDrag.x1) * this.natW
      const y2 = Math.max(this.rectDrag.y0, this.rectDrag.y1) * this.natH
      return { x: x1, y: y1, w: x2 - x1, h: y2 - y1 }
    },
    rulerStrokeWidth() {
      return Math.max(2, (this.natW || 0) * 0.003)
    }
  },
  watch: {
    '$route.query.patientId'(v) {
      if (v && String(v) !== String(this.selectedPatientId)) {
        this.selectedPatientId = Number(v) || v
        this.onPatientChange()
      }
    },
    imageViewMode(m) {
      if (m === 'annotated') {
        this.drawTool = null
      }
    },
    currentIndex() {
      this.loadAiForCurrentImage()
      this.resetViewSoft()
      this.imageViewMode = 'original'
      this.drawTool = null
    }
  },
  created() {
    this.remotePatientSearch('')
    const q = this.$route.query.patientId
    if (q) {
      this.selectedPatientId = Number(q) || q
      this.$nextTick(() => this.onPatientChange())
    }
  },
  mounted() {
    document.addEventListener('mousemove', this.onSplitMouseMove)
    document.addEventListener('mouseup', this.onSplitMouseUp)
    document.addEventListener('fullscreenchange', this.onFsChange)
    this.$nextTick(() => this.tryEnterFullscreen())
  },
  beforeDestroy() {
    document.removeEventListener('mousemove', this.onSplitMouseMove)
    document.removeEventListener('mouseup', this.onSplitMouseUp)
    document.removeEventListener('fullscreenchange', this.onFsChange)
  },
  methods: {
    fmtPct(v) {
      if (v == null) return '—'
      const n = Number(v)
      return (n <= 1 ? n * 100 : n).toFixed(1) + '%'
    },
    fmtNum(v) {
      if (v == null) return '—'
      const n = Number(v)
      return Number.isInteger(n) ? String(n) : n.toFixed(2)
    },
    pathStem(p) {
      if (!p) return ''
      const s = String(p).replace(/\\/g, '/')
      const base = s.substring(s.lastIndexOf('/') + 1)
      return base.replace(/\.[^.]+$/, '')
    },
    formatPatientOption(p) {
      return `${p.patientName}（ID ${p.patientId}）`
    },
    genderText(g) {
      const m = { 0: '男', 1: '女', 2: '未知' }
      return m[g] != null ? m[g] : '—'
    },
    modOk(v) {
      return v === 1 || v === true
    },
    diagLabel(s) {
      const map = { 0: '未开始', 1: 'AI诊断中', 2: '诊断完成', 3: '待审核', 4: '已生成报告' }
      return map[s] || '—'
    },
    diagTag(s) {
      const map = { 0: 'info', 1: 'primary', 2: 'success', 3: 'warning', 4: '' }
      return map[s] || 'info'
    },
    remotePatientSearch(query) {
      this.patientSearchLoading = true
      listPatientCards({ pageNum: 1, pageSize: 50, patientName: query || undefined })
        .then(res => {
          this.patientHits = res.rows || []
        })
        .finally(() => { this.patientSearchLoading = false })
    },
    onPatientChange() {
      this.xrayList = []
      this.currentIndex = 0
      this.aiResult = null
      this.detailSnapshot = null
      this.diagnosisSnapshot = null
      this.recordRows = []
      this.labRows = []
      this.imageViewMode = 'original'
      this.annotatedImageKey = 0
      this.drawTool = null
      if (!this.selectedPatientId) return
      const pid = this.selectedPatientId
      getPatientDiagnosisDetail(pid).then(res => {
        const d = res.data || {}
        this.detailSnapshot = d.patientSnapshot || null
        this.diagnosisSnapshot = d.diagnosisSnapshot || null
      }).catch(() => {})
      listAiImage({ patientId: pid, pageNum: 1, pageSize: 200 }).then(res => {
        this.xrayList = res.rows || []
        this.currentIndex = 0
        const qImg = this.$route.query.imageId
        if (qImg && this.xrayList.length) {
          const idx = this.xrayList.findIndex(r => String(r.id) === String(qImg))
          if (idx >= 0) this.currentIndex = idx
        }
        this.$nextTick(() => this.loadAiForCurrentImage())
      })
      listMedicalRecord({ patientId: pid, pageNum: 1, pageSize: 200 }).then(res => {
        this.recordRows = res.rows || []
      })
      listLab({ patientId: pid, pageNum: 1, pageSize: 100 }).then(res => {
        this.labRows = res.rows || []
      })
    },
    loadAiForCurrentImage() {
      this.aiResult = null
      const row = this.currentXray
      if (!row || !row.id) return
      try {
        const raw = sessionStorage.getItem(AI_CACHE_PREFIX + row.id)
        if (raw) this.aiResult = JSON.parse(raw)
      } catch (e) {}
    },
    persistAi(rowId, payload) {
      try {
        sessionStorage.setItem(AI_CACHE_PREFIX + rowId, JSON.stringify(payload))
      } catch (e) {}
    },
    prevImage() {
      if (this.canPrevImg) this.currentIndex--
    },
    nextImage() {
      if (this.canNextImg) this.currentIndex++
    },
    resetViewSoft() {
      this.scale = 1
      this.tx = 0
      this.ty = 0
    },
    fitToCanvas() {
      const stage = this.$refs.stage
      const img = this.$refs.imgEl
      if (!stage || !img || !this.natW || !this.natH) return
      const sw = stage.clientWidth
      const sh = stage.clientHeight
      const s = Math.min(sw / this.natW, sh / this.natH) * 0.98
      this.scale = s
      this.tx = 0
      this.ty = 0
    },
    onImgLoad() {
      const img = this.$refs.imgEl
      if (!img) return
      this.natW = img.naturalWidth || img.width
      this.natH = img.naturalHeight || img.height
      this.imgError = false
      this.$nextTick(() => this.initBrushCanvas())
      this.fitToCanvas()
    },
    onImgError() {
      this.imgError = true
    },
    initBrushCanvas() {
      const cvs = this.$refs.brushCanvas
      if (!cvs || !this.natW) return
      cvs.width = this.natW
      cvs.height = this.natH
      const ctx = cvs.getContext('2d')
      ctx.clearRect(0, 0, cvs.width, cvs.height)
    },
    lesionStyle(lesion) {
      const x1 = Number(lesion.x1)
      const y1 = Number(lesion.y1)
      const x2 = Number(lesion.x2)
      const y2 = Number(lesion.y2)
      const l = (x1 / this.natW) * 100
      const t = (y1 / this.natH) * 100
      const w = ((x2 - x1) / this.natW) * 100
      const h = ((y2 - y1) / this.natH) * 100
      return {
        left: l + '%',
        top: t + '%',
        width: w + '%',
        height: h + '%'
      }
    },
    normRectStyle(r) {
      return {
        left: r.x * 100 + '%',
        top: r.y * 100 + '%',
        width: r.w * 100 + '%',
        height: r.h * 100 + '%'
      }
    },
    onStageWheel(e) {
      const delta = e.deltaY > 0 ? -0.08 : 0.08
      const next = Math.min(8, Math.max(0.2, this.scale + delta))
      this.scale = next
    },
    clientToNorm(clientX, clientY) {
      const img = this.$refs.imgEl
      if (!img || !this.natW) return null
      const r = img.getBoundingClientRect()
      const px = (clientX - r.left) / r.width
      const py = (clientY - r.top) / r.height
      if (px < 0 || py < 0 || px > 1 || py > 1) return null
      return { x: px, y: py }
    },
    onStageMouseDown(e) {
      if (e.button !== 0) return
      const norm = this.clientToNorm(e.clientX, e.clientY)
      if (!norm) return
      if (this.imageViewMode === 'annotated') {
        this.panning = true
        this.panStart = { x: e.clientX, y: e.clientY, tx: this.tx, ty: this.ty }
        return
      }
      if (this.drawTool === 'rect') {
        this.rectDrag = { x0: norm.x, y0: norm.y, x1: norm.x, y1: norm.y }
      } else if (this.drawTool === 'brush') {
        this.brushDrawing = true
        this.brushLast = norm
        const cvs = this.$refs.brushCanvas
        const ctx = cvs && cvs.getContext('2d')
        if (ctx) {
          ctx.strokeStyle = '#ff5252'
          ctx.lineWidth = Math.max(2, this.natW * 0.004)
          ctx.lineCap = 'round'
          ctx.beginPath()
          ctx.moveTo(norm.x * this.natW, norm.y * this.natH)
        }
      } else {
        this.panning = true
        this.panStart = { x: e.clientX, y: e.clientY, tx: this.tx, ty: this.ty }
      }
    },
    onStageMouseMove(e) {
      if (this.panning && this.panStart) {
        this.tx = this.panStart.tx + (e.clientX - this.panStart.x)
        this.ty = this.panStart.ty + (e.clientY - this.panStart.y)
      }
      const norm = this.clientToNorm(e.clientX, e.clientY)
      if (this.rectDrag && norm) {
        this.rectDrag.x1 = norm.x
        this.rectDrag.y1 = norm.y
      }
      if (this.brushDrawing && norm && this.brushLast) {
        const cvs = this.$refs.brushCanvas
        const ctx = cvs && cvs.getContext('2d')
        if (ctx) {
          ctx.lineTo(norm.x * this.natW, norm.y * this.natH)
          ctx.stroke()
        }
        this.brushLast = norm
      }
    },
    onStageMouseUp() {
      if (this.panning) this.panning = false
      if (this.rectDrag && this.natW) {
        const x = Math.min(this.rectDrag.x0, this.rectDrag.x1)
        const y = Math.min(this.rectDrag.y0, this.rectDrag.y1)
        const w = Math.abs(this.rectDrag.x1 - this.rectDrag.x0)
        const h = Math.abs(this.rectDrag.y1 - this.rectDrag.y0)
        if (w > 0.01 && h > 0.01) this.userRects.push({ x, y, w, h })
        this.rectDrag = null
      }
      this.brushDrawing = false
    },
    clearBrushTracks() {
      this.initBrushCanvas()
      this.$message.success('已清除画笔痕迹')
    },
    clearRectMarks() {
      this.userRects = []
      this.rectDrag = null
      this.$message.success('已清除矩形标注')
    },
    startSplitDrag() {
      this.splitDrag = true
    },
    onSplitMouseMove(e) {
      if (!this.splitDrag) return
      const root = this.$refs.pacsRoot
      if (!root) return
      const rect = root.getBoundingClientRect()
      const x = e.clientX - rect.left
      let frac = x / rect.width
      frac = Math.min(0.78, Math.max(0.38, frac))
      this.leftFrac = frac
    },
    onSplitMouseUp() {
      this.splitDrag = false
    },
    setImageView(mode) {
      if (mode === 'annotated' && !this.hasAnnotatedView) {
        this.$message.warning('暂无标注图，请先执行 AI 病灶分析或上传矩形标注')
        return
      }
      if (!this.currentXray) return
      this.imageViewMode = mode
    },
    setDrawTool(mode) {
      if (!this.currentXray || this.imageViewMode !== 'original') return
      if (this.drawTool === mode) {
        this.drawTool = null
      } else {
        this.drawTool = mode
      }
    },
    goPatientList() {
      this.$router.push('/patient/list').catch(() => {})
    },
    goImportData() {
      const pid = this.selectedPatientId
      if (!pid) {
        this.$message.warning('请先选择患者')
        return
      }
      this.$router.push({ path: '/assisted-diagnosis/flow', query: { patientId: String(pid), step: '1' } }).catch(() => {})
    },
    loadRemoteImageForCanvas(url) {
      return fetch(url)
        .then(r => {
          if (!r.ok) throw new Error('原图加载失败')
          return r.blob()
        })
        .then(blob => createImageBitmap(blob))
    },
    async commitRectOverlay() {
      if (!this.currentXray) {
        this.$message.warning('请先加载影像')
        return
      }
      if (!this.userRects.length) {
        this.$message.warning('请先用矩形标注工具在图像上框选至少一个区域后再上传')
        return
      }
      if (!this.natW || !this.natH) {
        this.$message.warning('请等待影像加载完成后再上传')
        return
      }
      if (!checkPermi(['ai:image:analyze'])) {
        this.$modal.msgError('无上传权限，请联系管理员分配「AI分析」操作权限')
        return
      }
      const id = this.currentXray.id
      const pid = this.selectedPatientId
      this.overlayUploading = true
      try {
        const url = aiImageUrl(this.currentXray.imagePath)
        const bmp = await this.loadRemoteImageForCanvas(url)
        const cvs = document.createElement('canvas')
        cvs.width = this.natW
        cvs.height = this.natH
        const ctx = cvs.getContext('2d')
        ctx.drawImage(bmp, 0, 0, this.natW, this.natH)
        try { bmp.close() } catch (e) { /* noop */ }
        const lw = Math.max(2, this.natW * 0.004)
        ctx.lineWidth = lw
        ctx.strokeStyle = '#00e676'
        this.userRects.forEach(r => {
          ctx.strokeRect(r.x * this.natW, r.y * this.natH, r.w * this.natW, r.h * this.natH)
        })
        const full = String(this.currentXray.imagePath || '')
        const extMatch = full.match(/\.(jpe?g|png)$/i)
        const ext = extMatch ? extMatch[0].toLowerCase() : '.jpg'
        const mime = ext === '.png' ? 'image/png' : 'image/jpeg'
        const normPath = full.replace(/\\/g, '/')
        const fname = normPath.substring(normPath.lastIndexOf('/') + 1) || ('overlay' + ext)
        const blob = await new Promise((resolve, reject) => {
          cvs.toBlob(b => (b ? resolve(b) : reject(new Error('画布导出失败'))), mime, mime === 'image/jpeg' ? 0.92 : undefined)
        })
        const res = await uploadAiImageUserOverlay(id, blob, fname)
        if (res.code !== 200) {
          this.$modal.msgError(res.msg || '上传失败')
          return
        }
        this.$modal.msgSuccess('矩形标注图已写入 MinIO，已覆盖原 AI 标注图路径')
        this.annotatedImageKey++
        await Promise.all([
          listAiImage({ patientId: pid, pageNum: 1, pageSize: 200 }).then(r2 => {
            this.xrayList = r2.rows || []
            const idx = this.xrayList.findIndex(r => r.id === id)
            if (idx >= 0) this.currentIndex = idx
          }),
          listMedicalRecord({ patientId: pid, pageNum: 1, pageSize: 200 }).then(r3 => {
            this.recordRows = r3.rows || []
          })
        ])
        this.imageViewMode = 'annotated'
      } catch (e) {
        this.$modal.msgError((e && e.message) || '合成或上传失败')
      } finally {
        this.overlayUploading = false
      }
    },
    runAiAnalyze() {
      if (!this.currentXray) return
      const curId = this.currentXray.id
      const pid = this.selectedPatientId
      this.aiLoading = true
      analyzeAiImage(curId)
        .then(res => {
          let payload = res && res.data != null ? res.data : res
          if (payload && payload.data != null && payload.lesionList == null && payload.lesion_list == null) {
            payload = payload.data
          }
          this.aiResult = payload
          this.persistAi(curId, payload)
          this.annotatedImageKey++
          this.imageViewMode = 'annotated'
          this.$message.success((res && res.msg) || '检测成功')
          if (!pid) return null
          return listAiImage({ patientId: pid, pageNum: 1, pageSize: 200 })
        })
        .then(listRes => {
          if (listRes && listRes.rows) {
            this.xrayList = listRes.rows
            const idx = this.xrayList.findIndex(r => r.id === curId)
            if (idx >= 0) this.currentIndex = idx
          }
          this.loadAiForCurrentImage()
        })
        .catch(() => {})
        .finally(() => { this.aiLoading = false })
    },
    buildImageResultForFusion() {
      const ar = this.aiResult
      if (!ar) return null
      const row = this.currentXray
      const list = ar.lesion_list || ar.lesionList || []
      let listCopy
      try {
        listCopy = JSON.parse(JSON.stringify(list))
      } catch (e) {
        listCopy = list
      }
      const imgPath = row && row.imagePath ? String(row.imagePath) : ''
      const fn = imgPath ? imgPath.replace(/\\/g, '/').split('/').pop() : ''
      return {
        code: ar.code != null ? ar.code : 200,
        msg: ar.msg || 'OK',
        lesion_count: ar.lesion_count != null ? ar.lesion_count : (ar.lesionCount != null ? ar.lesionCount : listCopy.length),
        lesion_list: listCopy,
        original: ar.original || fn || '',
        ai_result: ar.ai_result || ar.aiResult || this.resolvedAiResultPath || '',
        diagnosis: ar.diagnosis || '',
        total_infection_area: ar.total_infection_area != null ? ar.total_infection_area : ar.totalInfectionArea,
        infection_rate: ar.infection_rate != null ? ar.infection_rate : ar.infectionRate,
        severity: ar.severity,
        pneumonia_type: ar.pneumonia_type || ar.pneumoniaType,
        treatment_suggestion: ar.treatment_suggestion || ar.treatmentSuggestion,
        further_examination: ar.further_examination || ar.furtherExamination
      }
    },
    /**
     * 浏览器全屏下仅渲染全屏元素子树，挂到 body 的 el-dialog 不可见；打开融合弹窗前先退出全屏。
     */
    async exitFullscreenIfNeeded() {
      const fsEl = document.fullscreenElement || document.webkitFullscreenElement || document.mozFullScreenElement || document.msFullscreenElement
      if (!fsEl) return
      try {
        const p = document.exitFullscreen
          ? document.exitFullscreen()
          : (document.webkitExitFullscreen ? document.webkitExitFullscreen() : null)
        if (p && typeof p.then === 'function') {
          await p
        }
      } catch (e) {
        /* 部分浏览器拒绝或已退出 */
      }
      this.isFs = !!(document.fullscreenElement || document.webkitFullscreenElement)
    },
    async openFusionReportDialog() {
      if (!checkPermi(['ai:image:record'])) {
        this.$modal.msgError('无权限：需要 ai:image:record')
        return
      }
      if (!this.currentXray) {
        this.$modal.msgWarning('请先选择并加载一张胸片')
        return
      }
      await this.exitFullscreenIfNeeded()
      await this.$nextTick()
      this.fusionDialogVisible = true
      this.fusionPanelLoading = true
      this.fusionEnvelope = null
      this.fusionReportId = null
      this.cachedImageResultObject = null
      this.cachedCaseText = ''
      this.cachedLabDataObject = null
      this.doctorAdvice = ''
      this.doctorSignature = this.$store.getters.nickName || this.$store.getters.name || ''
      this.fusionCollapseNames = ['ms', 'cc', 'fc', 'dg', 'sg']
      try {
        const ajax = await getFusionReportByImage(this.currentXray.id)
        const row = ajax && ajax.data
        if (row && row.reportId != null) {
          this.fusionReportId = row.reportId
          this.applyFusionReportRow(row)
          this.fusionPanelLoading = false
          return
        }
      } catch (e) {
        /* no cached row, continue */
      }
      if (!this.aiResult) {
        this.fusionPanelLoading = false
        this.fusionDialogVisible = false
        this.$modal.msgWarning('请先执行「AI 病灶分析」')
        return
      }
      const caseText = this.recordPlainText || ''
      if (!String(caseText).trim()) {
        this.fusionPanelLoading = false
        this.fusionDialogVisible = false
        this.$modal.msgWarning('请先导入或绑定电子病历（右侧需有病历文本）')
        return
      }
      const imageResult = this.buildImageResultForFusion()
      if (!imageResult) {
        this.fusionPanelLoading = false
        this.fusionDialogVisible = false
        return
      }
      const labData = buildFusionLabPayload(this.latestLab) || {}
      try {
        const ajax = await fusionAnalyze(this.currentXray.id, {
          imageResult,
          caseText,
          labData
        })
        const py = ajax && ajax.data
        if (!py) {
          this.$modal.msgError('融合服务返回为空')
          this.fusionDialogVisible = false
          return
        }
        const ic = py.code
        const ok = ic === 200 || ic === '200' || (typeof ic === 'number' && Number(ic) === 200)
        if (!ok) {
          this.$modal.msgError(py.msg || '融合分析失败')
          this.fusionDialogVisible = false
          return
        }
        this.fusionEnvelope = py
        const rec = this.linkedRecord
        const lab = this.latestLab
        const saveAjax = await saveFusionAnalyze(this.currentXray.id, {
          fusionResponse: py,
          imageResult,
          caseText,
          labData,
          medicalRecordId: rec && rec.recordId != null ? rec.recordId : undefined,
          labResultId: lab && lab.id != null ? lab.id : undefined
        })
        const saved = saveAjax && saveAjax.data
        if (saved && saved.reportId != null) {
          this.fusionReportId = saved.reportId
        }
      } catch (e) {
        this.fusionDialogVisible = false
        const msg = (e && e.message) || (typeof e === 'string' ? e : '') || '融合分析请求失败'
        this.$modal.msgError(msg)
      } finally {
        this.fusionPanelLoading = false
      }
    },
    applyFusionReportRow(row) {
      try {
        const fr = row.fusionResponseJson
        this.fusionEnvelope = typeof fr === 'string' ? JSON.parse(fr) : fr
      } catch (e) {
        this.$modal.msgError('已保存的融合报告 JSON 无法解析')
        this.fusionDialogVisible = false
        return
      }
      try {
        const ir = row.imageResultJson
        this.cachedImageResultObject = ir ? (typeof ir === 'string' ? JSON.parse(ir) : ir) : null
      } catch (e) {
        this.cachedImageResultObject = null
      }
      this.cachedCaseText = row.caseText || ''
      try {
        const ld = row.labDataJson
        this.cachedLabDataObject = ld ? (typeof ld === 'string' ? JSON.parse(ld) : ld) : {}
      } catch (e) {
        this.cachedLabDataObject = {}
      }
      this.doctorSignature = row.doctorSignature || this.$store.getters.nickName || this.$store.getters.name || ''
      this.doctorAdvice = row.doctorAdvice || ''
    },
    async confirmExportFusionWord() {
      const sig = (this.doctorSignature || '').trim()
      if (!sig) {
        this.$modal.msgWarning('请填写医生签名')
        return
      }
      if (!this.fusionEnvelope || !this.currentXray) return
      const imageResult = this.buildImageResultForFusion() || this.cachedImageResultObject
      if (!imageResult) {
        this.$modal.msgWarning('缺少 AI 影像结果（请重新执行 AI 分析或从已保存报告打开）')
        return
      }
      const liveLab = buildFusionLabPayload(this.latestLab) || {}
      const labData = Object.keys(liveLab).length ? liveLab : (this.cachedLabDataObject || {})
      const caseText = (this.recordPlainText || '').trim() || (this.cachedCaseText || '')
      this.fusionExportLoading = true
      try {
        if (this.fusionReportId) {
          await updateFusionDoctor({
            reportId: this.fusionReportId,
            doctorSignature: sig,
            doctorAdvice: this.doctorAdvice || ''
          })
        }
        const data = await exportFusionReport(this.currentXray.id, {
          doctorSignature: sig,
          doctorAdvice: this.doctorAdvice || '',
          fusionResponse: this.fusionEnvelope,
          imageResult,
          caseText,
          labData
        })
        if (!blobValidate(data)) {
          let err = '导出失败'
          try {
            const t = await data.text()
            const o = JSON.parse(t)
            err = o.msg || err
          } catch (e) { /* ignore */ }
          this.$modal.msgError(err)
          return
        }
        const patientName = this.currentXray.patientName || '患者'
        const filename = `多模态辅助诊断报告_${patientName}_${new Date().getTime()}.docx`
        saveAs(new Blob([data], { type: 'application/vnd.openxmlformats-officedocument.wordprocessingml.document' }), filename)
        this.fusionDialogVisible = false
        this.$router.push({ path: '/patient/list' }).catch(() => {})
      } catch (e) {
        this.$modal.msgError((e && e.message) || '导出失败')
      } finally {
        this.fusionExportLoading = false
      }
    },
    onFusionDialogClosed() {
      this.fusionEnvelope = null
      this.fusionExportLoading = false
      this.fusionReportId = null
      this.cachedImageResultObject = null
      this.cachedCaseText = ''
      this.cachedLabDataObject = null
    },
    toggleFullscreen() {
      const el = this.$refs.pacsRoot
      if (!document.fullscreenElement) {
        el.requestFullscreen && el.requestFullscreen()
      } else {
        document.exitFullscreen && document.exitFullscreen()
      }
    },
    tryEnterFullscreen() {
      const el = this.$refs.pacsRoot
      if (!el || typeof el.requestFullscreen !== 'function') return
      if (document.fullscreenElement) return
      const p = el.requestFullscreen()
      if (p && typeof p.catch === 'function') {
        p.catch(() => { /* 浏览器可能因无用户手势拒绝，静默忽略 */ })
      }
    },
    onFsChange() {
      this.isFs = !!document.fullscreenElement
    }
  }
}
</script>

<style scoped>
.pacs-root {
  position: fixed;
  inset: 0;
  z-index: 3000;
  display: flex;
  flex-direction: column;
  background: #e8eaed;
  font-family: 'Segoe UI', 'PingFang SC', 'Microsoft YaHei', sans-serif;
}
.pacs-toolbar {
  min-height: 48px;
  flex-shrink: 0;
  display: flex;
  align-items: center;
  justify-content: space-between;
  flex-wrap: wrap;
  row-gap: 6px;
  padding: 6px 10px;
  background: linear-gradient(180deg, #003366 0%, #004085 100%);
  color: #fff;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.2);
}
.tb-left,
.tb-center,
.tb-right {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 4px 6px;
}
.tb-left {
  flex-shrink: 0;
  max-width: min(42%, 320px);
}
.tb-center {
  justify-content: center;
  flex: 1;
  min-width: 0;
  padding: 0 6px;
}
.tb-right {
  justify-content: flex-end;
  flex-shrink: 0;
  max-width: 100%;
}
.tb-txt {
  color: #fff !important;
}
.tb-btn {
  background: rgba(255, 255, 255, 0.12) !important;
  border-color: rgba(255, 255, 255, 0.25) !important;
  color: #fff !important;
}
.tb-btn:hover {
  background: rgba(255, 255, 255, 0.22) !important;
}
.tb-btn.cta {
  font-weight: 600;
}
.tb-toggle-group {
  margin: 0 2px;
}
.tb-toggle.tb-toggle--active,
.tb-draw.tb-draw--active {
  box-shadow: 0 0 0 1px rgba(121, 187, 255, 0.9) inset !important;
  font-weight: 600;
}
.tb-label {
  font-size: 12px;
  opacity: 0.85;
  margin-right: 4px;
}
.tb-patient-select {
  width: 132px;
  max-width: min(28vw, 180px);
  flex-shrink: 0;
}
.tb-div {
  background: rgba(255, 255, 255, 0.25);
  margin: 0 6px;
}
.pacs-main {
  flex: 1;
  min-height: 0;
  display: flex;
  flex-direction: row;
  position: relative;
}
.pacs-left {
  min-width: 38%;
  position: relative;
  background: #000;
}
.pacs-stage {
  position: absolute;
  inset: 0;
  overflow: hidden;
  cursor: crosshair;
}
.stage-empty {
  color: #9e9e9e;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 15px;
}
.stage-inner {
  position: relative;
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
}
.stage-nav {
  position: absolute;
  left: 0;
  right: 0;
  bottom: 14px;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 56px;
  z-index: 12;
  pointer-events: none;
}
.stage-nav-btn {
  pointer-events: auto;
  width: 42px;
  height: 42px;
  border-radius: 50%;
  border: 1px solid rgba(255, 255, 255, 0.35);
  background: rgba(0, 0, 0, 0.35);
  color: #fff;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 18px;
  line-height: 1;
  transition: background 0.15s ease, transform 0.12s ease;
}
.stage-nav-btn:hover:not(:disabled) {
  background: rgba(0, 0, 0, 0.52);
  transform: scale(1.06);
}
.stage-nav-btn:disabled {
  opacity: 0.22;
  cursor: not-allowed;
  transform: none;
}
.img-stack {
  position: relative;
  display: inline-block;
  transition: filter 0.15s ease;
}
.pacs-img {
  display: block;
  max-width: none;
  user-select: none;
  vertical-align: top;
}
.lesion-layer {
  position: absolute;
  left: 0;
  top: 0;
  width: 100%;
  height: 100%;
  pointer-events: none;
}
.lesion-box {
  position: absolute;
  pointer-events: auto;
  box-sizing: border-box;
  border: 2px solid #ff1744;
  background: rgba(255, 23, 68, 0.12);
  cursor: help;
}
.lesion-box.user-lesion {
  border-color: #00e676;
  background: rgba(0, 230, 118, 0.1);
}
.ruler-svg {
  position: absolute;
  left: 0;
  top: 0;
  width: 100%;
  height: 100%;
  pointer-events: none;
}
.brush-canvas {
  position: absolute;
  left: 0;
  top: 0;
  width: 100%;
  height: 100%;
  pointer-events: none;
}
.lesion-tip {
  position: absolute;
  z-index: 5;
  max-width: 70%;
  padding: 8px 10px;
  background: rgba(0, 0, 0, 0.75);
  color: #fff;
  font-size: 12px;
  border-radius: 6px;
  pointer-events: none;
  line-height: 1.45;
}
.pacs-splitter {
  width: 6px;
  cursor: col-resize;
  background: #cfd4dc;
  flex-shrink: 0;
  border-left: 1px solid #b0b7c3;
  border-right: 1px solid #b0b7c3;
}
.pacs-splitter:hover {
  background: #004085;
}
.pacs-right {
  min-width: 28%;
  background: #eceff3;
  display: flex;
  flex-direction: column;
}
.rp-scroll {
  flex: 1;
  overflow: auto;
  padding: 12px;
  display: flex;
  flex-direction: column;
  gap: 12px;
}
.rp-card {
  background: #fff;
  border-radius: 10px;
  border: 1px solid #d9dee6;
  box-shadow: 0 1px 4px rgba(0, 64, 133, 0.06);
  padding: 12px 14px;
}
.rp-card-last {
  margin-bottom: 8px;
}
.rp-title {
  margin: 0 0 10px;
  font-size: 14px;
  font-weight: 700;
  color: #004085;
  border-bottom: 1px solid #ebeef5;
  padding-bottom: 8px;
}
.rp-row {
  font-size: 13px;
  margin-bottom: 6px;
  color: #303133;
}
.rp-row.big {
  font-size: 17px;
  font-weight: 700;
}
.rp-meta {
  font-size: 13px;
  font-weight: 400;
  color: #606266;
  margin-left: 8px;
}
.rp-row.muted {
  color: #909399;
  font-size: 12px;
}
.rp-tags {
  margin-top: 10px;
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 8px;
}
.rp-mod {
  font-size: 12px;
  color: #909399;
}
.rp-mod.ok {
  color: #2e7d32;
}
.rp-mod i {
  margin-right: 2px;
}
.rp-kv {
  display: flex;
  justify-content: space-between;
  gap: 10px;
  font-size: 13px;
  margin-bottom: 6px;
}
.rp-kv span {
  color: #909399;
}
.rp-kv.sm {
  font-size: 12px;
}
.rp-block {
  margin-top: 8px;
  font-size: 13px;
}
.rp-block .lbl {
  display: block;
  color: #004085;
  font-weight: 600;
  margin-bottom: 4px;
  font-size: 12px;
}
.rp-block p {
  margin: 0;
  line-height: 1.55;
  color: #303133;
}
.sev {
  color: #c62828;
}
.rp-placeholder {
  font-size: 12px;
  color: #a0a4aa;
  padding: 10px 0;
  text-align: center;
}
.rp-pre {
  margin: 0;
  white-space: pre-wrap;
  word-break: break-word;
  font-size: 12px;
  line-height: 1.55;
  color: #424242;
  max-height: 280px;
  overflow: auto;
  background: #fafafa;
  border-radius: 6px;
  padding: 10px;
  border: 1px solid #eee;
}
.rp-pre.sm {
  max-height: 160px;
}
.lab-table {
  display: flex;
  flex-direction: column;
  gap: 6px;
}
.lab-row {
  display: grid;
  grid-template-columns: 1fr auto;
  grid-template-rows: auto auto;
  font-size: 12px;
  padding: 6px 8px;
  border-radius: 6px;
  background: #f5f7fa;
  border: 1px solid #e4e7ed;
}
.lab-name {
  font-weight: 600;
  color: #303133;
}
.lab-val {
  text-align: right;
  font-weight: 600;
}
.lab-ref {
  grid-column: 1 / -1;
  font-size: 11px;
  color: #909399;
}
.lab-row.lvl-none .lab-val {
  color: #2e7d32;
}
.lab-row.lvl-mild .lab-val {
  color: #f9a825;
  font-weight: 700;
}
.lab-row.lvl-severe .lab-val {
  color: #c62828;
  font-weight: 800;
}
.wl-pop {
  padding: 4px 0;
  font-size: 12px;
  color: #606266;
}
.lesion-item {
  border-top: 1px dashed #ebeef5;
  padding-top: 8px;
  margin-top: 8px;
}
.li-h {
  font-weight: 600;
  color: #004085;
  margin-bottom: 6px;
  font-size: 12px;
}
::v-deep .tb-patient-select .el-input__inner {
  background: rgba(255, 255, 255, 0.95);
  height: 30px;
  line-height: 30px;
  font-size: 12px;
  padding-left: 8px;
  padding-right: 26px;
}
</style>
