<template>
  <div class="app-container report-edit">
    <el-page-header content="辅助诊断报告" @back="$router.push('/physician/report')" />

    <el-row :gutter="16" class="mt16">
      <el-col :xs="24" :md="14">
        <el-card shadow="never" class="re-card">
          <div slot="header" class="re-card__head"><span>报告基本信息</span></div>
          <el-form :model="form" label-width="110px" size="small">
            <el-row :gutter="12">
              <el-col :span="12">
                <el-form-item label="患者姓名"><el-input v-model="form.patientName" placeholder="请填写" /></el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="患者信息"><el-input v-model="form.patientInfo" placeholder="例如: 男,55岁,心内科" /></el-form-item>
              </el-col>
            </el-row>
            <el-row :gutter="12">
              <el-col :span="12">
                <el-form-item label="关联病历 ID"><el-input v-model="form.recordId" placeholder="可选" /></el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="状态">
                  <el-select v-model="form.status" style="width:100%">
                    <el-option label="草稿" value="0" />
                    <el-option label="已审核" value="1" />
                    <el-option label="已归档" value="2" />
                  </el-select>
                </el-form-item>
              </el-col>
            </el-row>
            <el-form-item label="病历摘要">
              <el-input v-model="form.recordSummary" type="textarea" :rows="3" />
            </el-form-item>
          </el-form>
        </el-card>

        <el-card shadow="never" class="re-card">
          <div slot="header" class="re-card__head">
            <span>病历 AI 诊断结果</span>
            <el-button size="mini" icon="el-icon-refresh" @click="loadFromContext">从上下文同步</el-button>
          </div>
          <div v-if="form.aiTextResult">
            <div class="re-kv"><label>临床印象</label><el-input v-model="form.aiTextResult.clinical_impression" type="textarea" :rows="2" /></div>
            <div class="re-kv"><label>症状</label><el-input v-model="form.aiTextResult.symptoms" type="textarea" :rows="2" /></div>
            <div class="re-kv"><label>用药</label><el-input v-model="form.aiTextResult.medications" type="textarea" :rows="2" /></div>
            <div class="re-kv"><label>既往史</label><el-input v-model="form.aiTextResult.history" type="textarea" :rows="2" /></div>
            <div class="re-kv">
              <label>疑似诊断</label>
              <div>
                <el-tag v-for="(d, idx) in form.aiTextResult.differential" :key="'df'+idx" closable @close="form.aiTextResult.differential.splice(idx,1)" style="margin-right:6px;margin-bottom:4px;">{{ d }}</el-tag>
              </div>
            </div>
          </div>
          <el-alert v-else type="info" :closable="false" show-icon title="未携带病历 AI 结果" description='可先在「病历 AI 诊断」页点击"用于生成报告"带入' />
        </el-card>

        <el-card shadow="never" class="re-card">
          <div slot="header" class="re-card__head"><span>影像与精准分析结果</span></div>
          <el-form label-width="110px" size="small">
            <el-form-item label="MinIO 路径">
              <el-input v-model="form.imagePath" placeholder="可选" />
            </el-form-item>
            <el-form-item label="检测病灶">
              <div v-if="form.aiDetection && form.aiDetection.length">
                <el-tag v-for="(d, idx) in form.aiDetection" :key="'det'+idx" style="margin-right:6px;margin-bottom:4px;" type="info" size="mini">
                  {{ d.label }} ({{ (d.confidence * 100).toFixed(0) }}%)
                </el-tag>
              </div>
              <span v-else class="re-muted">—</span>
            </el-form-item>
            <el-form-item label="精准分析">
              <div v-if="segBoxes.length">
                <el-tag v-for="(d, idx) in segBoxes" :key="'seg'+idx" style="margin-right:6px;margin-bottom:4px;" type="warning" size="mini">
                  {{ d.label }} {{ d.x1 }},{{ d.y1 }} ~ {{ d.x2 }},{{ d.y2 }}
                </el-tag>
              </div>
              <span v-else class="re-muted">—</span>
            </el-form-item>
          </el-form>
        </el-card>

        <el-card shadow="never" class="re-card">
          <div slot="header" class="re-card__head"><span>AI 诊断结论 / 建议</span></div>
          <el-form label-width="110px" size="small">
            <el-form-item label="诊断结论">
              <el-input v-model="form.conclusion" type="textarea" :rows="3" />
            </el-form-item>
            <el-form-item label="诊疗建议">
              <el-input v-model="form.suggestion" type="textarea" :rows="3" />
            </el-form-item>
            <el-form-item label="医生意见">
              <el-input v-model="form.doctorOpinion" type="textarea" :rows="3" placeholder="医生补充意见" />
            </el-form-item>
            <el-form-item label="医生签名">
              <el-input v-model="form.doctorSignature" placeholder="签名" style="width:280px" />
            </el-form-item>
          </el-form>
        </el-card>

        <div class="re-actions">
          <el-button type="primary" icon="el-icon-check" @click="handleSave">保存</el-button>
          <el-button icon="el-icon-finished" @click="handleSaveStatus('1')">保存并审核</el-button>
          <el-button icon="el-icon-folder-opened" @click="handleSaveStatus('2')">保存并归档</el-button>
          <el-button type="success" icon="el-icon-printer" @click="handlePdf">预览 / 导出 PDF</el-button>
        </div>
      </el-col>

      <el-col :xs="24" :md="10">
        <el-card shadow="never" class="re-card re-preview">
          <div slot="header" class="re-card__head"><span>报告预览</span></div>
          <div class="rp">
            <h3 class="rp__title">医疗辅助诊断报告</h3>
            <div class="rp__sub">编号：{{ form.id || '(未保存)' }} · 状态：{{ statusLabel(form.status) }}</div>
            <div class="rp__section">
              <div class="rp__h2">患者信息</div>
              <div class="rp__kv"><span>姓名</span><b>{{ form.patientName || '-' }}</b></div>
              <div class="rp__kv"><span>信息</span><b>{{ form.patientInfo || '-' }}</b></div>
              <div class="rp__kv"><span>病历</span><b>{{ form.recordId || '-' }}</b></div>
            </div>
            <div class="rp__section">
              <div class="rp__h2">病历摘要</div>
              <div class="rp__text">{{ form.recordSummary || (form.aiTextResult && form.aiTextResult.summary) || '—' }}</div>
            </div>
            <div class="rp__section" v-if="form.aiTextResult && form.aiTextResult.clinical_impression">
              <div class="rp__h2">病历 AI 临床印象</div>
              <div class="rp__text">{{ form.aiTextResult.clinical_impression }}</div>
            </div>
            <div class="rp__section" v-if="form.imagePath">
              <div class="rp__h2">影像信息</div>
              <div class="rp__text">{{ form.imagePath }}</div>
            </div>
            <div class="rp__section">
              <div class="rp__h2">AI 诊断结论</div>
              <div class="rp__text">{{ form.conclusion || '—' }}</div>
            </div>
            <div class="rp__section">
              <div class="rp__h2">诊疗建议</div>
              <div class="rp__text">{{ form.suggestion || '—' }}</div>
            </div>
            <div class="rp__section" v-if="form.doctorOpinion">
              <div class="rp__h2">医生意见</div>
              <div class="rp__text">{{ form.doctorOpinion }}</div>
            </div>
            <div class="rp__sign">医生签名：{{ form.doctorSignature || '___________' }}</div>
          </div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script>
import { getReport, saveReport, REPORT_STATUS_LABEL } from '@/api/physician/report'
import { getContext, patchContext } from '@/api/physician/diagnosisContext'
import { exportReportPdf } from '@/utils/reportPdf'

function emptyForm() {
  return {
    id: '',
    patientName: '',
    patientInfo: '',
    recordId: '',
    recordSummary: '',
    aiTextResult: null,
    imagePath: '',
    aiDetection: [],
    aiSegment: null,
    conclusion: '',
    suggestion: '',
    doctorOpinion: '',
    doctorSignature: '',
    status: '0',
    createTime: '',
    updateTime: ''
  }
}

export default {
  name: 'PhysicianReportEdit',
  data() {
    return { form: emptyForm() }
  },
  computed: {
    segBoxes() {
      const seg = this.form.aiSegment
      if (!seg) return []
      return seg.detections || seg.data || []
    }
  },
  async created() {
    const id = this.$route.params && this.$route.params.id
    if (id) {
      const res = await getReport(id)
      if (res.data) {
        this.form = { ...emptyForm(), ...res.data }
      }
    } else {
      this.loadFromContext()
    }
  },
  methods: {
    loadFromContext() {
      const ctx = getContext()
      const patch = {}
      if (ctx.emr) {
        patch.recordId = ctx.emr.recordId || this.form.recordId
        patch.recordSummary = (ctx.emr.aiResult && ctx.emr.aiResult.summary) || this.form.recordSummary
        patch.aiTextResult = ctx.emr.aiResult ? { ...ctx.emr.aiResult } : this.form.aiTextResult
      }
      if (ctx.image) {
        patch.imagePath = ctx.image.path || this.form.imagePath
        patch.aiDetection = Array.isArray(ctx.image.detection) ? [...ctx.image.detection] : this.form.aiDetection
        patch.aiSegment = ctx.image.segment ? { ...ctx.image.segment } : this.form.aiSegment
        if (ctx.image.segment && ctx.image.segment.conclusion && !this.form.conclusion) {
          patch.conclusion = ctx.image.segment.conclusion
        }
        if (ctx.image.segment && ctx.image.segment.suggestion && !this.form.suggestion) {
          patch.suggestion = ctx.image.segment.suggestion
        }
      }
      if (ctx.patient) {
        patch.patientName = ctx.patient.name || this.form.patientName
        patch.patientInfo = ctx.patient.info || this.form.patientInfo
      }
      // 若结论为空，依据病历/影像 AI 自动生成一段
      if (!this.form.conclusion && !patch.conclusion) {
        const parts = []
        if (patch.aiTextResult && patch.aiTextResult.clinical_impression) parts.push(patch.aiTextResult.clinical_impression)
        if (patch.aiSegment && patch.aiSegment.conclusion) parts.push(patch.aiSegment.conclusion)
        patch.conclusion = parts.join('；')
      }
      this.form = { ...this.form, ...patch }
    },
    async handleSave(silent) {
      const res = await saveReport(this.form)
      this.form = { ...this.form, ...res.data }
      if (!silent) this.$message.success('保存成功')
      if (!this.$route.params.id) {
        this.$router.replace('/physician/report/edit/' + this.form.id)
      }
    },
    async handleSaveStatus(status) {
      this.form.status = status
      await this.handleSave(true)
      this.$message.success('已保存并更新状态')
    },
    async handlePdf() {
      // 未保存时先保存再导出
      if (!this.form.id) await this.handleSave(true)
      exportReportPdf(this.form)
    },
    statusLabel(s) { return REPORT_STATUS_LABEL[s] || '-' }
  }
}
</script>

<style lang="scss" scoped>
.report-edit {
  .re-card { border: 1px solid #e8ecf2; border-radius: 6px; margin-bottom: 16px; }
  .re-card__head { display: flex; align-items: center; justify-content: space-between; font-weight: 600; color: #333; }
  .re-kv { display: grid; grid-template-columns: 88px 1fr; gap: 10px; margin-bottom: 10px; align-items: flex-start; label { color: #666; font-size: 13px; padding-top: 6px; } }
  .re-muted { color: #999; }
  .re-actions { text-align: right; margin-bottom: 16px; }
  .mt16 { margin-top: 16px; }

  .re-preview ::v-deep .el-card__body { padding: 20px; max-height: 80vh; overflow: auto; }
  .rp__title { text-align: center; font-size: 18px; margin: 0 0 4px; color: #333; }
  .rp__sub { text-align: center; color: #999; font-size: 12px; margin-bottom: 14px; }
  .rp__section { margin-bottom: 14px; }
  .rp__h2 { color: #165dff; font-weight: 600; border-left: 3px solid #165dff; padding-left: 6px; font-size: 13px; margin-bottom: 6px; }
  .rp__kv { display: flex; justify-content: space-between; font-size: 13px; padding: 2px 0; color: #333; span { color: #666; } }
  .rp__text { font-size: 13px; line-height: 1.7; white-space: pre-wrap; color: #333; }
  .rp__sign { margin-top: 24px; text-align: right; color: #666; font-size: 13px; }
}
</style>
