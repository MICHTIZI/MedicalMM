<template>
  <div class="af-root app-container">
    <div class="af-toolbar">
      <el-button type="text" icon="el-icon-back" class="af-back" @click="backToPatients">返回患者列表</el-button>
      <div class="af-toolbar-center">
        <span class="af-title">流水式辅助诊断 · 三步录入</span>
        <el-tag v-if="patientName" type="info" effect="dark" size="small">{{ patientName }}（ID {{ patientId }}）</el-tag>
      </div>
      <span />
    </div>

    <el-card shadow="never" class="af-steps-card">
      <el-steps :active="stepIndex" finish-status="success" align-center>
        <el-step title="影像上传" description="PNG / JPG / DICOM" />
        <el-step title="结构化病历" description="主诉、病史与胸片关联" />
        <el-step title="检验指标" description="生命体征与实验室数据" />
      </el-steps>
    </el-card>

    <el-card v-show="stepIndex === 0" shadow="never" class="af-panel">
      <div class="af-panel-head">
        <h2>第 1 / 3 步 — 影像上传</h2>
        <p class="af-muted">支持拖拽或点击上传 PNG / JPG / DICOM；切换患者后会清空待上传列表并重新加载该患者已有影像。</p>
      </div>
      <el-alert v-if="imageRows.length" :title="'该患者已有影像 ' + imageRows.length + ' 条，可直接进入下一步或继续上传。'" type="success" show-icon class="af-mb" />
      <el-form ref="uploadFormRef" :model="uploadForm" :rules="uploadRules" label-width="88px" size="small" class="af-form">
        <el-form-item label="患者" prop="patientId">
          <el-input :value="patientLabel" disabled style="max-width: 360px" />
        </el-form-item>
      </el-form>
      <el-upload
        ref="uploader"
        drag
        multiple
        action="#"
        :auto-upload="false"
        :file-list="fileList"
        :on-change="onFileChange"
        :on-remove="onFileRemove"
        accept=".png,.jpg,.jpeg,.dcm,.dicom"
        class="af-upload"
      >
        <i class="el-icon-upload" />
        <div class="el-upload__text">将文件拖到此处，或<em>点击上传</em></div>
        <div slot="tip" class="el-upload__tip">支持 PNG / JPG / DICOM</div>
      </el-upload>
      <el-progress v-if="uploading" :percentage="uploadProgress" class="af-mt" />
      <div class="af-actions">
        <el-button type="primary" :loading="uploading" :disabled="!fileList.length" v-hasPermi="['imaging:xray:upload']" @click="submitUpload">开始上传（{{ fileList.length }}）</el-button>
      </div>
      <el-table v-if="uploadResults.length" :data="uploadResults" size="small" border class="af-mt">
        <el-table-column label="缩略图" width="86" align="center">
          <template slot-scope="s">
            <el-image :src="thumbUrl(s.row.imagePath)" style="width:52px;height:52px" fit="cover" />
          </template>
        </el-table-column>
        <el-table-column label="文件名" prop="imageName" min-width="160" />
        <el-table-column label="ID" prop="id" width="72" />
      </el-table>
      <div class="af-footer-btns">
        <el-button type="primary" :disabled="!canLeaveStep1" @click="goStep(2)">已有影像，进入下一步</el-button>
      </div>
    </el-card>

    <el-card v-show="stepIndex === 1" shadow="never" class="af-panel">
      <div class="af-panel-head">
        <h2>第 2 / 3 步 — 结构化病历</h2>
        <p class="af-muted">{{ recordForm.recordId ? '已加载历史病历，修改后保存将更新记录。' : '请填写病历并关联胸片。' }}</p>
      </div>
      <el-form ref="recordFormRef" :model="recordForm" :rules="recordRules" label-width="110px" size="small" class="af-form">
        <el-form-item label="患者">
          <el-input :value="patientLabel" disabled style="max-width: 360px" />
        </el-form-item>
        <el-row :gutter="8" class="af-mb">
          <el-button type="info" plain icon="el-icon-upload2" size="small" @click="$refs.afTxtInput.click()">导入病历 TXT</el-button>
          <input ref="afTxtInput" type="file" accept=".txt,text/plain" style="display:none" @change="handleTxtImport">
          <el-button type="primary" plain icon="el-icon-picture-outline" size="small" @click="openXrayPicker">选择胸片</el-button>
        </el-row>
        <el-form-item label="主诉" prop="chiefComplaint">
          <el-input v-model="recordForm.chiefComplaint" type="textarea" :rows="3" placeholder="主诉" />
        </el-form-item>
        <el-form-item label="现病史" prop="presentHistory">
          <el-input v-model="recordForm.presentHistory" type="textarea" :rows="4" placeholder="现病史" />
        </el-form-item>
        <el-form-item label="既往史" prop="pastHistory">
          <el-input v-model="recordForm.pastHistory" type="textarea" :rows="3" placeholder="既往史" />
        </el-form-item>
        <el-form-item label="体格检查" prop="physicalExam">
          <el-input v-model="recordForm.physicalExam" type="textarea" :rows="3" placeholder="体格检查" />
        </el-form-item>
        <el-form-item label="初步诊断" prop="initialDiagnosis">
          <el-input v-model="recordForm.initialDiagnosis" type="textarea" :rows="3" placeholder="初步诊断" />
        </el-form-item>
        <el-form-item label="已选胸片" prop="imageId">
          <div v-if="recordForm.imagePath" class="af-xray-preview">
            <el-image :src="thumbUrl(recordForm.imagePath)" style="width:120px;height:120px" fit="cover" :preview-src-list="[thumbUrl(recordForm.imagePath)]" />
            <span class="af-muted af-path">{{ recordForm.imagePath }}</span>
          </div>
          <span v-else class="af-muted">暂无</span>
        </el-form-item>
        <el-form-item label="AI 结果路径" prop="aiResultPath">
          <el-input v-model="recordForm.aiResultPath" placeholder="可选" />
        </el-form-item>
      </el-form>
      <div class="af-footer-btns">
        <el-button @click="goStep(1)">上一步</el-button>
        <el-button type="primary" :loading="recordSaving" @click="saveRecordAndNext">保存并进入下一步</el-button>
      </div>
    </el-card>

    <el-card v-show="stepIndex === 2" shadow="never" class="af-panel">
      <div class="af-panel-head">
        <h2>第 3 / 3 步 — 检验指标</h2>
        <p class="af-muted">{{ labForm.id ? '已加载历史检验，修改后保存将更新记录。' : '录入检验相关数据。' }}</p>
      </div>
      <el-form ref="labFormRef" :model="labForm" :rules="labRules" label-width="120px" size="small" class="af-form">
        <el-form-item label="患者">
          <el-input :value="patientLabel" disabled style="max-width: 360px" />
        </el-form-item>
        <el-form-item label="检验 TXT">
          <el-button type="info" plain icon="el-icon-upload2" size="small" @click="$refs.afLabTxt.click()" v-hasPermi="['medical:lab:import']">选择 TXT 解析导入</el-button>
          <input ref="afLabTxt" type="file" accept=".txt,text/plain" style="display:none" @change="handleLabTxtImport">
          <span class="af-muted" style="margin-left:10px">解析结果将合并到当前表单；若已有检验记录 ID，保存时为更新。</span>
        </el-form-item>
        <el-form-item label="检验时间" prop="testDate">
          <el-date-picker v-model="labForm.testDate" type="datetime" value-format="yyyy-MM-dd HH:mm:ss" placeholder="检验时间" style="width: 100%; max-width: 360px" />
        </el-form-item>
        <el-form-item label="检验医师" prop="testDoctor">
          <el-input v-model="labForm.testDoctor" placeholder="姓名" style="max-width: 360px" />
        </el-form-item>
        <el-form-item label="检验科室" prop="testDepartment">
          <el-input v-model="labForm.testDepartment" placeholder="科室" style="max-width: 360px" />
        </el-form-item>
        <el-collapse v-model="labCollapse" class="af-collapse">
          <el-collapse-item title="生命体征" name="vitals">
            <el-row :gutter="12">
              <el-col :span="8"><el-form-item label="体温(℃)" prop="temperature"><el-input-number v-model="labForm.temperature" :precision="1" :step="0.1" controls-position="right" style="width: 100%" /></el-form-item></el-col>
              <el-col :span="8"><el-form-item label="心率"><el-input-number v-model="labForm.heartRate" :min="0" controls-position="right" style="width: 100%" /></el-form-item></el-col>
              <el-col :span="8"><el-form-item label="呼吸频率"><el-input-number v-model="labForm.respiratoryRate" :min="0" controls-position="right" style="width: 100%" /></el-form-item></el-col>
              <el-col :span="8"><el-form-item label="收缩压"><el-input-number v-model="labForm.systolicBp" :min="0" controls-position="right" style="width: 100%" /></el-form-item></el-col>
              <el-col :span="8"><el-form-item label="舒张压"><el-input-number v-model="labForm.diastolicBp" :min="0" controls-position="right" style="width: 100%" /></el-form-item></el-col>
              <el-col :span="8"><el-form-item label="SpO2(%)"><el-input-number v-model="labForm.spo2" :precision="1" :step="0.1" controls-position="right" style="width: 100%" /></el-form-item></el-col>
            </el-row>
          </el-collapse-item>
          <el-collapse-item title="血常规" name="cbc">
            <el-row :gutter="12">
              <el-col :span="8"><el-form-item label="WBC"><el-input-number v-model="labForm.wbc" :precision="2" controls-position="right" style="width: 100%" /></el-form-item></el-col>
              <el-col :span="8"><el-form-item label="中性粒%"><el-input-number v-model="labForm.neutrophilRatio" :precision="1" controls-position="right" style="width: 100%" /></el-form-item></el-col>
              <el-col :span="8"><el-form-item label="淋巴%"><el-input-number v-model="labForm.lymphocyteRatio" :precision="1" controls-position="right" style="width: 100%" /></el-form-item></el-col>
              <el-col :span="8"><el-form-item label="单核%"><el-input-number v-model="labForm.monocyteRatio" :precision="1" controls-position="right" style="width: 100%" /></el-form-item></el-col>
              <el-col :span="8"><el-form-item label="血小板"><el-input-number v-model="labForm.platelet" :min="0" controls-position="right" style="width: 100%" /></el-form-item></el-col>
            </el-row>
          </el-collapse-item>
          <el-collapse-item title="炎症 / 血气" name="rest">
            <el-row :gutter="12">
              <el-col :span="8"><el-form-item label="CRP"><el-input-number v-model="labForm.crp" :precision="1" controls-position="right" style="width: 100%" /></el-form-item></el-col>
              <el-col :span="8"><el-form-item label="PCT"><el-input-number v-model="labForm.pct" :precision="2" controls-position="right" style="width: 100%" /></el-form-item></el-col>
              <el-col :span="8"><el-form-item label="ESR"><el-input-number v-model="labForm.esr" :min="0" controls-position="right" style="width: 100%" /></el-form-item></el-col>
              <el-col :span="8"><el-form-item label="pH"><el-input-number v-model="labForm.ph" :precision="2" :step="0.01" controls-position="right" style="width: 100%" /></el-form-item></el-col>
              <el-col :span="8"><el-form-item label="PO2"><el-input-number v-model="labForm.po2" :precision="1" controls-position="right" style="width: 100%" /></el-form-item></el-col>
              <el-col :span="8"><el-form-item label="PCO2"><el-input-number v-model="labForm.pco2" :precision="1" controls-position="right" style="width: 100%" /></el-form-item></el-col>
              <el-col :span="8"><el-form-item label="HCO3-"><el-input-number v-model="labForm.hco3" :precision="1" controls-position="right" style="width: 100%" /></el-form-item></el-col>
            </el-row>
          </el-collapse-item>
        </el-collapse>
        <el-form-item label="备注" prop="remark">
          <el-input v-model="labForm.remark" type="textarea" :rows="2" placeholder="备注" />
        </el-form-item>
      </el-form>
      <div class="af-footer-btns">
        <el-button @click="goStep(2)">上一步</el-button>
        <el-button type="primary" :loading="labSaving" @click="saveLabAndFinish">保存并打开阅片</el-button>
      </div>
    </el-card>

    <el-dialog title="选择胸片" :visible.sync="xrayDialogOpen" width="760px" append-to-body @open="loadImagePicker">
      <el-table :data="imageRows" size="small" max-height="420" @row-dblclick="selectXrayRow">
        <el-table-column label="预览" width="88" align="center">
          <template slot-scope="scope">
            <el-image :src="thumbUrl(scope.row.imagePath)" style="width:52px;height:52px" fit="cover" />
          </template>
        </el-table-column>
        <el-table-column label="文件名" prop="imageName" min-width="160" show-overflow-tooltip />
        <el-table-column label="操作" width="88" align="center">
          <template slot-scope="scope">
            <el-button type="text" size="small" @click="selectXrayRow(scope.row)">选用</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-dialog>
  </div>
</template>

<script>
import request from '@/utils/request'
import { imageUrl } from '@/api/medical/imaging/xray'
import { listAiImage } from '@/api/medical/aiImage'
import { getPatient } from '@/api/medical/patient'
import { listMedicalRecord, getMedicalRecord, addMedicalRecord, updateMedicalRecord } from '@/api/medical/record'
import { listLab, getLab, addLab, updateLab, parseLabTxt } from '@/api/medical/labResult'

export default {
  name: 'AssistedDiagnosisFlow',
  data() {
    return {
      patientId: undefined,
      patientName: '',
      imageRows: [],
      fileList: [],
      uploading: false,
      uploadProgress: 0,
      uploadResults: [],
      uploadForm: { patientId: undefined },
      uploadRules: {
        patientId: [{ required: true, message: '请选择患者', trigger: 'change' }]
      },
      recordForm: {
        recordId: undefined,
        patientId: undefined,
        chiefComplaint: undefined,
        presentHistory: undefined,
        pastHistory: undefined,
        physicalExam: undefined,
        initialDiagnosis: undefined,
        imageId: undefined,
        imagePath: undefined,
        aiResultPath: undefined
      },
      recordRules: {
        chiefComplaint: [{ required: true, message: '请填写主诉', trigger: 'blur' }],
        presentHistory: [{ required: true, message: '请填写现病史', trigger: 'blur' }],
        pastHistory: [{ required: true, message: '请填写既往史', trigger: 'blur' }],
        physicalExam: [{ required: true, message: '请填写体格检查', trigger: 'blur' }],
        initialDiagnosis: [{ required: true, message: '请填写初步诊断', trigger: 'blur' }],
        imageId: [{ required: true, message: '请选择胸片', trigger: 'change' }]
      },
      recordSaving: false,
      labForm: {
        id: undefined,
        patientId: undefined,
        testDate: undefined,
        testDoctor: undefined,
        testDepartment: undefined,
        temperature: undefined,
        heartRate: undefined,
        respiratoryRate: undefined,
        systolicBp: undefined,
        diastolicBp: undefined,
        spo2: undefined,
        wbc: undefined,
        neutrophilRatio: undefined,
        lymphocyteRatio: undefined,
        monocyteRatio: undefined,
        platelet: undefined,
        crp: undefined,
        pct: undefined,
        esr: undefined,
        ph: undefined,
        po2: undefined,
        pco2: undefined,
        hco3: undefined,
        remark: undefined
      },
      labRules: {
        testDate: [{ required: true, message: '请选择检验时间', trigger: 'change' }]
      },
      labSaving: false,
      labCollapse: ['vitals', 'cbc', 'rest'],
      xrayDialogOpen: false
    }
  },
  computed: {
    stepIndex() {
      const s = Number(this.$route.query.step || 1)
      if (s >= 1 && s <= 3) return s - 1
      return 0
    },
    patientLabel() {
      if (this.patientName) return `${this.patientName}（ID ${this.patientId}）`
      return this.patientId ? `ID ${this.patientId}` : '—'
    },
    canLeaveStep1() {
      return (this.imageRows && this.imageRows.length > 0) || (this.uploadResults && this.uploadResults.length > 0)
    }
  },
  watch: {
    '$route.query.patientId': {
      immediate: true,
      handler() {
        this.initRoute()
      }
    },
    '$route.query.step'() {
      this.scrollTop()
    }
  },
  created() {
    this.initRoute()
  },
  methods: {
    scrollTop() {
      this.$nextTick(() => {
        try {
          const el = document.querySelector('.main-container')
          if (el) el.scrollTop = 0
        } catch (e) { /* noop */ }
      })
    },
    thumbUrl(path) {
      return imageUrl(path)
    },
    formatNow() {
      const d = new Date()
      const p = n => (n < 10 ? '0' : '') + n
      return `${d.getFullYear()}-${p(d.getMonth() + 1)}-${p(d.getDate())} ${p(d.getHours())}:${p(d.getMinutes())}:${p(d.getSeconds())}`
    },
    defaultRecordForm() {
      return {
        recordId: undefined,
        patientId: undefined,
        chiefComplaint: undefined,
        presentHistory: undefined,
        pastHistory: undefined,
        physicalExam: undefined,
        initialDiagnosis: undefined,
        imageId: undefined,
        imagePath: undefined,
        aiResultPath: undefined
      }
    },
    emptyLabForm() {
      return {
        id: undefined,
        patientId: undefined,
        testDate: undefined,
        testDoctor: undefined,
        testDepartment: undefined,
        temperature: undefined,
        heartRate: undefined,
        respiratoryRate: undefined,
        systolicBp: undefined,
        diastolicBp: undefined,
        spo2: undefined,
        wbc: undefined,
        neutrophilRatio: undefined,
        lymphocyteRatio: undefined,
        monocyteRatio: undefined,
        platelet: undefined,
        crp: undefined,
        pct: undefined,
        esr: undefined,
        ph: undefined,
        po2: undefined,
        pco2: undefined,
        hco3: undefined,
        remark: undefined
      }
    },
    initRoute() {
      const pid = this.$route.query.patientId
      if (!pid) {
        this.$modal.msgError('缺少患者参数')
        this.$router.replace('/patient/list').catch(() => {})
        return
      }
      this.patientId = Number(pid) || pid
      this.uploadForm.patientId = this.patientId
      this.loadPatientAndData()
    },
    loadPatientAndData() {
      const pid = this.patientId
      // 切换患者：清空上传队列与表单，再按患者拉取影像与预填数据
      this.fileList = []
      this.uploadResults = []
      this.uploading = false
      this.uploadProgress = 0
      if (this.$refs.uploader) {
        try { this.$refs.uploader.clearFiles() } catch (e) { /* noop */ }
      }
      this.recordForm = { ...this.defaultRecordForm(), patientId: pid }
      this.labForm = { ...this.emptyLabForm(), patientId: pid, testDate: this.formatNow() }

      getPatient(pid).then(res => {
        const d = res.data || {}
        this.patientName = d.patientName || ''
      }).catch(() => {})
      this.refreshImages()
        .then(() => this.loadPrefills())
        .then(() => {
          this.maybeAutoBindXray()
          this.$nextTick(() => {
            if (this.$refs.recordFormRef) this.$refs.recordFormRef.clearValidate()
            if (this.$refs.labFormRef) this.$refs.labFormRef.clearValidate()
          })
        })
    },
    refreshImages() {
      return listAiImage({ patientId: this.patientId, pageNum: 1, pageSize: 200 }).then(res => {
        this.imageRows = res.rows || []
      })
    },
    maybeAutoBindXray() {
      if (this.recordForm && this.recordForm.imageId) return
      if (this.imageRows.length === 1) {
        const r = this.imageRows[0]
        this.$set(this.recordForm, 'imageId', r.id)
        this.$set(this.recordForm, 'imagePath', r.imagePath)
      }
    },
    /** 有病历时取最新一条详情填充；无则空表。检验同理（依赖 refreshImages 已更新 imageRows）。 */
    loadPrefills() {
      const pid = this.patientId
      const emptyRecord = () => ({ ...this.defaultRecordForm(), patientId: pid })
      const emptyLab = () => ({ ...this.emptyLabForm(), patientId: pid, testDate: this.formatNow() })

      const recP = listMedicalRecord({ patientId: pid, pageNum: 1, pageSize: 80 })
        .then(res => {
          const rows = res.rows || []
          if (!rows.length) {
            this.recordForm = emptyRecord()
            return
          }
          const latest = [...rows].sort((a, b) => new Date(b.createTime || 0) - new Date(a.createTime || 0))[0]
          const rid = latest.recordId != null ? latest.recordId : latest.id
          if (rid == null) {
            this.recordForm = emptyRecord()
            return
          }
          return getMedicalRecord(rid).then(r2 => {
            const data = r2.data || {}
            this.recordForm = { ...emptyRecord(), ...data, patientId: pid }
          })
        })
        .catch(() => {
          this.recordForm = emptyRecord()
        })

      const labP = listLab({ patientId: pid, pageNum: 1, pageSize: 80 })
        .then(res => {
          const rows = res.rows || []
          if (!rows.length) {
            this.labForm = emptyLab()
            return
          }
          const latest = [...rows].sort((a, b) => new Date(b.testDate || 0) - new Date(a.testDate || 0))[0]
          const lid = latest.id
          if (lid == null) {
            this.labForm = emptyLab()
            return
          }
          return getLab(lid).then(r2 => {
            const data = r2.data || {}
            this.labForm = { ...emptyLab(), ...data, patientId: pid }
          })
        })
        .catch(() => {
          this.labForm = emptyLab()
        })

      return Promise.all([recP, labP])
    },
    goStep(step) {
      if (step < 1 || step > 3) return
      this.$router.replace({
        path: '/assisted-diagnosis/flow',
        query: { patientId: String(this.patientId), step: String(step) }
      }).catch(() => {})
    },
    backToPatients() {
      this.$router.push('/patient/list').catch(() => {})
    },
    onFileChange(file, list) {
      this.fileList = list
    },
    onFileRemove(file, list) {
      this.fileList = list
    },
    submitUpload() {
      if (!this.fileList.length) return
      this.$refs.uploadFormRef.validate(valid => {
        if (!valid) return
        this.uploading = true
        this.uploadProgress = 0
        const formData = new FormData()
        formData.append('patientId', this.uploadForm.patientId)
        this.fileList.forEach(f => formData.append('files', f.raw))
        request({
          url: '/imaging/xray/upload',
          method: 'post',
          data: formData,
          headers: { repeatSubmit: false },
          timeout: 600000,
          onUploadProgress: e => {
            if (e.total > 0) this.uploadProgress = Math.round((e.loaded / e.total) * 100)
          }
        }).then(res => {
          this.uploading = false
          if (res.code === 200) {
            this.$modal.msgSuccess(res.msg || '上传成功')
            this.uploadResults = res.data || []
            this.fileList = []
            this.$refs.uploader.clearFiles()
            this.refreshImages().then(() => this.maybeAutoBindXray())
          } else {
            this.$modal.msgError(res.msg || '上传失败')
          }
        }).catch(() => { this.uploading = false })
      })
    },
    openXrayPicker() {
      this.xrayDialogOpen = true
    },
    loadImagePicker() {
      this.refreshImages()
    },
    selectXrayRow(row) {
      this.recordForm.imageId = row.id
      this.recordForm.imagePath = row.imagePath
      this.xrayDialogOpen = false
    },
    handleTxtImport(event) {
      const file = event.target.files[0]
      if (!file) return
      const reader = new FileReader()
      reader.onload = e => {
        this.applyTxtContent(e.target.result || '')
        event.target.value = ''
      }
      reader.readAsText(file, 'UTF-8')
    },
    applyTxtContent(text) {
      const fields = [
        { key: 'chiefComplaint', label: '主诉' },
        { key: 'presentHistory', label: '现病史' },
        { key: 'pastHistory', label: '既往史' },
        { key: 'physicalExam', label: '体格检查' },
        { key: 'initialDiagnosis', label: '初步诊断' }
      ]
      fields.forEach((field, index) => {
        const next = fields[index + 1]
        const sep = '[：:]\\s*'
        const pattern = next
          ? new RegExp(field.label + sep + '([\\s\\S]*?)(?=' + next.label + sep + ')')
          : new RegExp(field.label + sep + '([\\s\\S]*)')
        const match = text.match(pattern)
        if (match) this.$set(this.recordForm, field.key, match[1].trim())
      })
    },
    saveRecordAndNext() {
      this.$refs.recordFormRef.validate(valid => {
        if (!valid) return
        this.recordSaving = true
        const req = this.recordForm.recordId ? updateMedicalRecord(this.recordForm) : addMedicalRecord(this.recordForm)
        req.then(() => {
          this.$modal.msgSuccess('保存成功')
          this.goStep(3)
        }).finally(() => { this.recordSaving = false })
      })
    },
    handleLabTxtImport(e) {
      const files = e.target.files
      if (!files || !files.length) return
      const file = files[0]
      parseLabTxt(file).then(res => {
        const parsed = res.data || {}
        const skip = ['id', 'patientId', 'patientName', 'patientAttendingDoctorIdScope', 'isDeleted', 'params', 'searchValue']
        const merged = { ...this.emptyLabForm(), ...this.labForm, patientId: this.patientId }
        if (!merged.testDate) merged.testDate = this.formatNow()
        Object.keys(parsed).forEach(key => {
          if (skip.includes(key)) return
          const v = parsed[key]
          if (v !== undefined && v !== null) merged[key] = v
        })
        this.labForm = merged
        this.$nextTick(() => this.$refs.labFormRef && this.$refs.labFormRef.clearValidate())
        this.$modal.msgSuccess('已从 TXT 合并检验数据，可继续编辑后保存。')
      }).finally(() => { e.target.value = '' })
    },
    saveLabAndFinish() {
      this.$refs.labFormRef.validate(valid => {
        if (!valid) return
        this.labSaving = true
        const api = this.labForm.id ? updateLab : addLab
        api(this.labForm).then(() => {
          this.$modal.msgSuccess('保存成功')
          return this.refreshImages()
        }).then(() => {
          const firstImg = (this.imageRows && this.imageRows[0]) || null
          const q = { patientId: String(this.patientId) }
          if (firstImg && firstImg.id) q.imageId = String(firstImg.id)
          this.$router.push({ path: '/viewer/pacs', query: q }).catch(() => {})
        }).finally(() => { this.labSaving = false })
      })
    }
  }
}
</script>

<style scoped>
.af-root {
  max-width: 960px;
  margin: 0 auto;
}
.af-toolbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 16px;
}
.af-back {
  font-size: 14px;
}
.af-toolbar-center {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 6px;
}
.af-title {
  font-size: 18px;
  font-weight: 700;
  color: #303133;
}
.af-steps-card {
  margin-bottom: 16px;
  border-radius: 10px;
}
.af-panel {
  border-radius: 10px;
  border: 1px solid #e4e7ed;
}
.af-panel-head h2 {
  margin: 0 0 8px;
  font-size: 17px;
  color: #004085;
}
.af-muted {
  color: #909399;
  font-size: 13px;
}
.af-mb {
  margin-bottom: 12px;
}
.af-mt {
  margin-top: 14px;
}
.af-form {
  margin-top: 8px;
}
.af-upload {
  width: 100%;
}
.af-actions {
  margin-top: 12px;
}
.af-footer-btns {
  margin-top: 24px;
  padding-top: 16px;
  border-top: 1px solid #ebeef5;
  display: flex;
  gap: 12px;
  justify-content: flex-end;
}
.af-xray-preview {
  display: flex;
  align-items: center;
  gap: 12px;
}
.af-path {
  word-break: break-all;
  max-width: 480px;
}
.af-collapse {
  margin-top: 8px;
  border: none;
}
</style>
