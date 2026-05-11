<template>
  <div class="app-container patient-workbench">
    <el-form :model="queryParams" ref="queryForm" size="small" :inline="true" v-show="showSearch" label-width="88px">
      <el-form-item label="患者姓名" prop="patientName">
        <el-input v-model="queryParams.patientName" placeholder="姓名模糊查询" clearable @keyup.enter.native="handleQuery" style="width: 220px" />
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="el-icon-search" size="mini" @click="handleQuery">搜索</el-button>
        <el-button icon="el-icon-refresh" size="mini" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5">
        <el-button type="primary" plain icon="el-icon-plus" size="mini" @click="handleAdd" v-hasPermi="['medical:patient:add']">新增</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="warning" plain icon="el-icon-download" size="mini" @click="handleExport" v-hasPermi="['medical:patient:export']">导出</el-button>
      </el-col>
      <right-toolbar :showSearch.sync="showSearch" @queryTable="getList" />
    </el-row>

    <div class="patient-card-grid" v-loading="loading">
      <div
        v-for="row in cardList"
        :key="row.patientId"
        class="patient-card"
        @click="openDrawer(row)"
      >
        <div class="pc-header">
          <span class="pc-name">{{ row.patientName }}</span>
          <span class="pc-meta">{{ genderLabel(row.gender) }} {{ row.age != null ? row.age + '岁' : '' }}</span>
        </div>
        <div class="pc-doctor">主治医生：{{ row.attendingDoctor || '—' }}</div>

        <div class="pc-mods">
          <span :class="['mod-item', modClass(row.hasImage)]">
            <i :class="modIcon(row.hasImage)" /> {{ modLine(row.hasImage, '胸片') }}
          </span>
          <span :class="['mod-item', modClass(row.hasMedicalRecord)]">
            <i :class="modIcon(row.hasMedicalRecord)" /> {{ modLine(row.hasMedicalRecord, '病历') }}
          </span>
          <span :class="['mod-item', modClass(row.hasLabResult)]">
            <i :class="modIcon(row.hasLabResult)" /> {{ modLine(row.hasLabResult, '检验') }}
          </span>
        </div>

        <div class="pc-status-row">
          <span class="muted">诊断状态</span>
          <el-tag
            :type="Number(row.diagnosisStatus || 0) === 4 ? undefined : diagTagType(row.diagnosisStatus)"
            effect="dark"
            size="small"
            :color="Number(row.diagnosisStatus || 0) === 4 ? '#722ED1' : undefined"
          >{{ diagLabel(row.diagnosisStatus) }}</el-tag>
        </div>

        <div class="pc-last muted">最后诊断：{{ row.lastDiagnosisTime ? parseTime(row.lastDiagnosisTime) : '—' }}</div>

        <div class="pc-actions" @click.stop="">
          <el-button v-for="a in computeCardActions(row)" :key="a.key" size="mini" :type="a.btnType || 'default'" plain @click="handleCardAction(a.key, row)">{{ a.label }}</el-button>
        </div>
      </div>

      <el-empty v-if="!loading && (!cardList || !cardList.length)" description="暂无患者数据" />
    </div>

    <pagination v-show="total > 0" :total="total" :page.sync="queryParams.pageNum" :limit.sync="queryParams.pageSize" @pagination="getList" />

    <!-- 抽屉：诊断详情（append-to-body 下标题栏样式见文件底部非 scoped 块） -->
    <el-drawer
      custom-class="patient-detail-drawer"
      :visible.sync="drawerOpen"
      direction="rtl"
      size="50%"
      append-to-body
      destroy-on-close
      :wrapper-closable="true"
      @close="onDrawerClose"
    >
      <template slot="title">
        <div class="pdd-header-title">
          <span class="pdd-header-title-main">患者详情</span>
          <span v-if="drawerPatientId" class="pdd-header-title-sub">#{{ drawerPatientId }}</span>
        </div>
      </template>
      <div class="pdd-body" v-loading="drawerLoading">
        <template v-if="detail && detail.patientSnapshot">
          <!-- 基本信息 -->
          <section class="pdd-card">
            <h3 class="pdd-card-title">基本信息</h3>
            <div class="pdd-basic-name">{{ detail.patientSnapshot.patientName }}</div>
            <div class="pdd-basic-grid">
              <div class="pdd-info-cell">
                <i class="el-icon-user pdd-info-icon" />
                <div>
                  <div class="pdd-info-label">性别</div>
                  <div class="pdd-info-value">{{ genderLabel(detail.patientSnapshot.gender) }}</div>
                </div>
              </div>
              <div class="pdd-info-cell">
                <i class="el-icon-date pdd-info-icon" />
                <div>
                  <div class="pdd-info-label">年龄</div>
                  <div class="pdd-info-value">{{ detail.patientSnapshot.age != null ? detail.patientSnapshot.age + ' 岁' : '—' }}</div>
                </div>
              </div>
              <div class="pdd-info-cell pdd-span-2">
                <i class="el-icon-phone-outline pdd-info-icon" />
                <div>
                  <div class="pdd-info-label">联系电话</div>
                  <div class="pdd-info-value">{{ detail.patientSnapshot.phone || '—' }}</div>
                </div>
              </div>
              <div class="pdd-info-cell pdd-span-2">
                <i class="el-icon-location-outline pdd-info-icon" />
                <div>
                  <div class="pdd-info-label">地址</div>
                  <div class="pdd-info-value pdd-text-wrap">{{ detail.patientSnapshot.address || '—' }}</div>
                </div>
              </div>
            </div>
            <div class="pdd-basic-meta">
              <span><i class="el-icon-user" /> 主治医生：{{ detail.patientSnapshot.attendingDoctor || '—' }}</span>
              <span><i class="el-icon-time" /> 建档时间：{{ parseTime(detail.patientSnapshot.createTime) }}</span>
            </div>
          </section>

          <!-- 数据绑定 -->
          <section class="pdd-card">
            <h3 class="pdd-card-title">数据绑定状态</h3>
            <div class="pdd-bind-row">
              <div
                class="pdd-bind-chip"
                :class="{ 'is-done': nzDiag('hasImage'), 'is-pending': !nzDiag('hasImage') }"
                role="button"
                tabindex="0"
                @click="onBindChipClick('image')"
                @keyup.enter="onBindChipClick('image')"
              >
                <i :class="nzDiag('hasImage') ? 'el-icon-success' : 'el-icon-circle-close'" />
                <span class="pdd-bind-label">胸片</span>
                <el-tag :type="nzDiag('hasImage') ? 'success' : 'info'" size="small" effect="plain">{{ nzDiag('hasImage') ? '已上传' : '未完成' }}</el-tag>
                <span v-if="nzDiag('hasImage') && diagField('firstImageUploadTime')" class="pdd-bind-time">{{ parseTime(diagField('firstImageUploadTime')) }}</span>
              </div>
              <div
                class="pdd-bind-chip"
                :class="{ 'is-done': nzDiag('hasMedicalRecord'), 'is-pending': !nzDiag('hasMedicalRecord') }"
                role="button"
                tabindex="0"
                @click="onBindChipClick('record')"
                @keyup.enter="onBindChipClick('record')"
              >
                <i :class="nzDiag('hasMedicalRecord') ? 'el-icon-success' : 'el-icon-circle-close'" />
                <span class="pdd-bind-label">电子病历</span>
                <el-tag :type="nzDiag('hasMedicalRecord') ? 'success' : 'info'" size="small" effect="plain">{{ nzDiag('hasMedicalRecord') ? '已录入' : '未完成' }}</el-tag>
                <span v-if="nzDiag('hasMedicalRecord') && timelineTime('病历')" class="pdd-bind-time">{{ parseTime(timelineTime('病历')) }}</span>
              </div>
              <div
                class="pdd-bind-chip"
                :class="{ 'is-done': nzDiag('hasLabResult'), 'is-pending': !nzDiag('hasLabResult') }"
                role="button"
                tabindex="0"
                @click="onBindChipClick('lab')"
                @keyup.enter="onBindChipClick('lab')"
              >
                <i :class="nzDiag('hasLabResult') ? 'el-icon-success' : 'el-icon-circle-close'" />
                <span class="pdd-bind-label">检验数据</span>
                <el-tag :type="nzDiag('hasLabResult') ? 'success' : 'info'" size="small" effect="plain">{{ nzDiag('hasLabResult') ? '已导入' : '未完成' }}</el-tag>
                <span v-if="nzDiag('hasLabResult') && timelineTime('检验')" class="pdd-bind-time">{{ parseTime(timelineTime('检验')) }}</span>
              </div>
            </div>
          </section>

          <!-- 诊断流程时间线 -->
          <section class="pdd-card">
            <h3 class="pdd-card-title">诊断流程进度</h3>
            <div v-if="detail.timeline && detail.timeline.length" class="pdd-timeline">
              <div
                v-for="(step, idx) in detail.timeline"
                :key="step.step"
                class="pdd-timeline-item"
                :class="{
                  'is-done': step.done,
                  'is-current': drawerCurrentStepIndex >= 0 && drawerCurrentStepIndex === idx
                }"
              >
                <div class="pdd-timeline-track">
                  <span class="pdd-timeline-node" />
                  <span v-if="idx < detail.timeline.length - 1" class="pdd-timeline-line" />
                </div>
                <div class="pdd-timeline-content">
                  <div class="pdd-timeline-title">{{ step.title }}</div>
                  <div class="pdd-timeline-meta">
                    <template v-if="step.done">
                      <span class="pdd-timeline-done">已完成</span>
                      <span v-if="step.doneTime" class="pdd-timeline-time">{{ parseTime(step.doneTime) }}</span>
                    </template>
                    <span v-else class="pdd-timeline-pending">未完成</span>
                  </div>
                </div>
              </div>
            </div>
            <div v-else class="pdd-empty-inline">暂无流程数据</div>
          </section>

          <!-- 最新诊断结果 -->
          <section class="pdd-card pdd-card--last">
            <h3 class="pdd-card-title">最新诊断结果</h3>
            <div v-if="!(detail.latestAi && detail.latestAi.diagnosisText)" class="pdd-ai-placeholder">
              <i class="el-icon-document" />
              <p>暂无 AI 诊断文本</p>
              <span class="pdd-ai-placeholder-hint">完成胸片上传并运行 AI 分析后将在此展示结论</span>
            </div>
            <div v-else class="pdd-ai-card">
              <div class="pdd-ai-card-head">
                <span class="pdd-ai-card-label">诊断结论</span>
                <el-tag v-if="aiSeverityMeta" :type="aiSeverityMeta.tagType" size="small" effect="dark">{{ aiSeverityMeta.label }}</el-tag>
              </div>
              <p class="pdd-ai-conclusion">{{ detail.latestAi.diagnosisText }}</p>
              <div v-if="detail.latestAi.infectionHint || detail.latestAi.lesionCount != null" class="pdd-ai-suggest-block">
                <div class="pdd-ai-suggest-title">临床提示与建议</div>
                <p v-if="detail.latestAi.infectionHint" class="pdd-ai-suggest-text">{{ detail.latestAi.infectionHint }}</p>
                <p v-if="detail.latestAi.lesionCount != null" class="pdd-ai-suggest-text">病灶数量：<strong>{{ detail.latestAi.lesionCount }}</strong>，请结合影像表现与实验室指标综合评估。</p>
              </div>
            </div>
          </section>

          <div class="pdd-footer">
            <el-button class="pdd-btn pdd-btn--primary" size="small" icon="el-icon-s-order" type="primary" plain @click="goAssistedFlow(drawerPatientId)">开始辅助诊断</el-button>
            <el-button class="pdd-btn pdd-btn--neutral" size="small" icon="el-icon-edit" @click="openEditFromDrawer" v-hasPermi="['medical:patient:edit']">编辑患者</el-button>
          </div>
        </template>
      </div>
    </el-drawer>

    <!-- 新增/修改弹窗 -->
    <el-dialog :title="title" :visible.sync="open" width="620px" append-to-body>
      <el-form ref="form" :model="form" :rules="rules" label-width="110px">
        <el-form-item label="患者姓名" prop="patientName">
          <el-input v-model="form.patientName" placeholder="请输入患者姓名" />
        </el-form-item>
        <el-form-item label="性别" prop="gender">
          <el-radio-group v-model="form.gender">
            <el-radio v-for="dict in genderOptions" :key="dict.value" :label="dict.value">{{ dict.label }}</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="年龄" prop="age">
          <el-input-number v-model="form.age" :min="0" :max="150" controls-position="right" style="width: 180px" />
        </el-form-item>
        <el-form-item label="电话" prop="phone">
          <el-input v-model="form.phone" placeholder="请输入电话" />
        </el-form-item>
        <el-form-item label="地址" prop="address">
          <el-input v-model="form.address" type="textarea" :rows="2" placeholder="请输入地址" />
        </el-form-item>
        <el-form-item v-if="canAssignDoctor" label="主治医生" prop="attendingDoctorId">
          <el-select v-model="form.attendingDoctorId" filterable placeholder="请选择主治医生" style="width: 100%">
            <el-option v-for="item in doctorOptions" :key="item.userId" :label="item.nickName" :value="item.userId" />
          </el-select>
        </el-form-item>
        <el-form-item v-else label="主治医生">
          <el-input v-model="form.attendingDoctor" disabled placeholder="系统将自动设置为当前登录医生" />
        </el-form-item>
        <el-form-item label="备注" prop="remark">
          <el-input v-model="form.remark" type="textarea" :rows="3" placeholder="请输入备注" />
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button type="primary" @click="submitForm">确 定</el-button>
        <el-button @click="cancel">取 消</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import {
  listPatientCards,
  getPatientDiagnosisDetail,
  getPatient,
  addPatient,
  updatePatient,
  listDoctorOptions
} from '@/api/medical/patient'

export default {
  name: 'MedicalPatient',
  data() {
    return {
      loading: true,
      showSearch: true,
      cardList: [],
      total: 0,
      queryParams: {
        pageNum: 1,
        pageSize: 12,
        patientName: undefined
      },
      doctorOptions: [],
      title: '',
      open: false,
      form: {},
      drawerOpen: false,
      drawerLoading: false,
      drawerPatientId: undefined,
      detail: null,
      genderOptions: [
        { value: '0', label: '男' },
        { value: '1', label: '女' },
        { value: '2', label: '未知' }
      ],
      rules: {
        patientName: [
          { required: true, message: '请输入患者姓名', trigger: 'blur' }
        ],
        attendingDoctorId: [
          { required: true, message: '请选择主治医生', trigger: 'change' }
        ]
      }
    }
  },
  computed: {
    canAssignDoctor() {
      const roles = this.$store.getters.roles || []
      return roles.map(role => String(role).toLowerCase()).includes('admin')
    },
    /** 第一个未完成步骤下标；若全部完成则为 -1（不高亮待办） */
    drawerCurrentStepIndex() {
      const t = this.detail && this.detail.timeline
      if (!t || !t.length) return -1
      const i = t.findIndex(s => !s.done)
      return i === -1 ? -1 : i
    },
    /** 基于接口已有字段做轻量规则标签，不调用新接口 */
    aiSeverityMeta() {
      const ai = this.detail && this.detail.latestAi
      if (!ai || !(ai.diagnosisText || '').trim()) return null
      const text = (ai.diagnosisText || '') + (ai.infectionHint || '')
      const lc = ai.lesionCount
      if (/重度|严重|高危|大量|广泛/i.test(text) || (lc != null && lc >= 5)) {
        return { label: '高危提示', tagType: 'danger' }
      }
      if (/中度|中等/i.test(text) || (lc != null && lc >= 2)) {
        return { label: '中度关注', tagType: 'warning' }
      }
      if (/轻度|轻微|少许|小/i.test(text) || lc != null) {
        return { label: '轻度 / 观察', tagType: 'success' }
      }
      return { label: '待评估', tagType: 'info' }
    }
  },
  created() {
    this.getList()
    if (this.canAssignDoctor) this.getDoctorOptions()
  },
  methods: {
    genderLabel(v) {
      const o = this.genderOptions.find(i => String(i.value) === String(v))
      return o ? o.label : '—'
    },
    nz(v) {
      return v === 1 || v === true
    },
    diagLabel(status) {
      const s = Number(status || 0)
      const map = {
        0: '未开始',
        1: 'AI 诊断中',
        2: '诊断完成',
        3: '待审核',
        4: '已生成报告'
      }
      return map[s] || '未知'
    },
    diagTagType(status) {
      const s = Number(status || 0)
      if (s === 0) return 'info'
      if (s === 1) return 'primary'
      if (s === 2) return 'success'
      if (s === 3) return 'warning'
      if (s === 4) return ''
      return 'info'
    },
    modClass(flag) {
      return this.nz(flag) ? 'ok' : 'pending'
    },
    modIcon(flag) {
      return this.nz(flag) ? 'el-icon-success' : 'el-icon-circle-plus-outline'
    },
    modLine(flag, noun) {
      return this.nz(flag) ? `${noun} · 已就绪` : `${noun} · 未完成`
    },
    computeCardActions(row) {
      const ready = this.nz(row.hasImage) && this.nz(row.hasMedicalRecord) && this.nz(row.hasLabResult)
      const st = Number(row.diagnosisStatus || 0)
      const acts = [{ key: 'detail', label: '查看详情' }]
      if (!ready) acts.push({ key: 'fill', label: '补全数据', btnType: 'warning' }) // 进入流水式录入（影像→病历→检验）
      else if (st < 2) acts.push({ key: 'ai', label: '开始 AI 诊断', btnType: 'primary' })
      else if (st >= 2 && st < 4) acts.push({ key: 'report', label: '生成报告' })
      else acts.push({ key: 'viewer', label: '阅片器' })
      return acts
    },
    handleCardAction(key, row) {
      const pid = row.patientId
      if (key === 'detail') this.openDrawer(row)
      else if (key === 'fill') this.goAssistedFlow(pid)
      else if (key === 'ai') this.goAiBench(pid)
      else if (key === 'report') this.goAiBench(pid)
      else if (key === 'viewer') this.goAiBench(pid)
    },
    openDrawer(row) {
      const pid = row.patientId
      this.drawerPatientId = pid
      this.drawerOpen = true
      this.drawerLoading = true
      getPatientDiagnosisDetail(pid).then(res => {
        this.detail = res.data || {}
        this.drawerLoading = false
      }).catch(() => { this.drawerLoading = false })
    },
    nzDiag(field) {
      const d = this.detail && this.detail.diagnosisSnapshot
      return d ? this.nz(d[field]) : false
    },
    diagField(field) {
      const d = this.detail && this.detail.diagnosisSnapshot
      return d ? d[field] : undefined
    },
    timelineTime(keyword) {
      const t = this.detail && this.detail.timeline
      if (!t || !keyword) return undefined
      const s = t.find(x => String(x.title).includes(keyword) && x.done)
      return s ? s.doneTime : undefined
    },
    onDrawerClose() {
      this.detail = null
    },
    bindLine(ok, text, when) {
      const mark = ok ? '✔' : '○'
      const t = ok && when ? `（${this.parseTime(when)}）` : ''
      return `${mark} ${text}${t}`
    },
    goAssistedFlow(patientId, step) {
      if (!patientId) return
      const q = { patientId: String(patientId), step: String(step || 1) }
      this.$router.push({ path: '/assisted-diagnosis/flow', query: q }).catch(() => {})
    },
    onBindChipClick(kind) {
      const pid = this.drawerPatientId
      if (!pid) return
      const stepMap = { image: 1, record: 2, lab: 3 }
      const s = stepMap[kind] || 1
      this.drawerOpen = false
      this.goAssistedFlow(pid, s)
    },
    goAiBench(patientId) {
      if (!patientId) return
      this.$router.push({ path: '/viewer/pacs', query: { patientId } }).catch(() => {})
    },
    openEditFromDrawer() {
      if (!this.drawerPatientId) return
      this.handleUpdatePatient(this.drawerPatientId)
      this.drawerOpen = false
    },
    getList() {
      this.loading = true
      listPatientCards(this.queryParams).then(response => {
        this.cardList = response.rows || []
        this.total = response.total
        this.loading = false
      }).catch(() => { this.loading = false })
    },
    getDoctorOptions() {
      listDoctorOptions().then(response => {
        this.doctorOptions = response.data || []
      })
    },
    cancel() {
      this.open = false
      this.reset()
    },
    reset() {
      this.form = {
        patientId: undefined,
        patientName: undefined,
        gender: '2',
        age: undefined,
        phone: undefined,
        address: undefined,
        attendingDoctorId: undefined,
        attendingDoctor: undefined,
        remark: undefined
      }
      this.resetForm('form')
    },
    handleQuery() {
      this.queryParams.pageNum = 1
      this.getList()
    },
    resetQuery() {
      this.resetForm('queryForm')
      this.handleQuery()
    },
    handleAdd() {
      this.reset()
      this.open = true
      this.title = '新增患者'
      if (this.canAssignDoctor) this.getDoctorOptions()
    },
    handleUpdatePatient(patientId) {
      this.reset()
      getPatient(patientId).then(response => {
        this.form = response.data
        this.open = true
        this.title = '修改患者'
        if (this.canAssignDoctor) this.getDoctorOptions()
      })
    },
    submitForm() {
      this.$refs.form.validate(valid => {
        if (!valid) return
        const req = this.form.patientId !== undefined ? updatePatient(this.form) : addPatient(this.form)
        req.then(() => {
          this.$modal.msgSuccess('保存成功')
          this.open = false
          this.getList()
        })
      })
    },
    handleExport() {
      this.download('emr/patient/export', { ...this.queryParams }, `patient_${new Date().getTime()}.xlsx`)
    }
  }
}
</script>

<style scoped>
.patient-workbench {
  --mv-primary: #004085;
  --mv-primary-soft: rgba(0, 64, 133, 0.08);
  --mv-text: #303133;
  --mv-muted: #909399;
}
.patient-card-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 16px;
  min-height: 120px;
}
@media screen and (max-width: 1600px) {
  .patient-card-grid { grid-template-columns: repeat(3, 1fr); }
}
@media screen and (max-width: 1200px) {
  .patient-card-grid { grid-template-columns: repeat(2, 1fr); }
}
@media screen and (max-width: 768px) {
  .patient-card-grid { grid-template-columns: 1fr; }
}
.patient-card {
  border: 1px solid #ebeef5;
  border-radius: 10px;
  padding: 12px 14px;
  height: 220px;
  box-sizing: border-box;
  cursor: pointer;
  display: flex;
  flex-direction: column;
  background: #fff;
  transition: box-shadow .2s;
}
.patient-card:hover {
  box-shadow: 0 8px 20px rgba(0, 0, 0, 0.08);
}
.pc-header {
  display: flex;
  align-items: baseline;
  justify-content: space-between;
}
.pc-name {
  font-size: 18px;
  font-weight: 700;
  color: var(--mv-primary);
}
.pc-meta {
  font-size: 13px;
  color: #606266;
}
.pc-doctor {
  font-size: 13px;
  color: #909399;
  margin-top: 6px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}
.pc-mods {
  margin-top: 10px;
  display: flex;
  justify-content: space-between;
  font-size: 12px;
  flex-shrink: 0;
}
.mod-item.ok { color: #67c23a; }
.mod-item.pending { color: #909399; }
.pc-status-row {
  margin-top: 10px;
  display: flex;
  align-items: center;
  justify-content: space-between;
}
.pc-last {
  margin-top: 6px;
  font-size: 12px;
  flex-shrink: 0;
}
.pc-actions {
  margin-top: auto;
  padding-top: 10px;
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}
.muted { color: #909399; font-size: 13px; }

/* —— 抽屉正文（slot 内容仍带 scoped，类名以 pdd- 前缀） —— */
.pdd-body {
  padding: 0 20px 24px;
  background: #f5f7fa;
  min-height: 100%;
  box-sizing: border-box;
}
.pdd-header-title {
  display: flex;
  align-items: baseline;
  gap: 10px;
}
.pdd-header-title-main {
  font-size: 17px;
  font-weight: 600;
  letter-spacing: 0.02em;
}
.pdd-header-title-sub {
  font-size: 13px;
  opacity: 0.85;
  font-weight: 400;
}
.pdd-card {
  background: #fff;
  border: 1px solid #e4e7ed;
  border-radius: 10px;
  box-shadow: 0 1px 4px rgba(0, 64, 133, 0.06);
  padding: 16px 18px;
  margin-bottom: 16px;
}
.pdd-card--last {
  margin-bottom: 0;
}
.pdd-card-title {
  margin: 0 0 14px;
  padding-bottom: 10px;
  border-bottom: 1px solid #ebeef5;
  font-size: 16px;
  font-weight: 700;
  color: #004085;
  letter-spacing: 0.02em;
}
.pdd-basic-name {
  font-size: 22px;
  font-weight: 700;
  color: #1a1a1a;
  margin-bottom: 14px;
  line-height: 1.3;
}
.pdd-basic-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 12px 20px;
}
.pdd-span-2 {
  grid-column: 1 / -1;
}
.pdd-info-cell {
  display: flex;
  align-items: flex-start;
  gap: 10px;
}
.pdd-info-icon {
  font-size: 18px;
  color: #004085;
  margin-top: 2px;
  flex-shrink: 0;
}
.pdd-info-label {
  font-size: 12px;
  color: #909399;
  margin-bottom: 2px;
}
.pdd-info-value {
  font-size: 14px;
  color: #303133;
  font-weight: 500;
  line-height: 1.45;
}
.pdd-text-wrap {
  word-break: break-all;
}
.pdd-basic-meta {
  margin-top: 14px;
  padding-top: 12px;
  border-top: 1px solid #ebeef5;
  display: flex;
  flex-wrap: wrap;
  gap: 10px 20px;
  font-size: 12px;
  color: #909399;
}
.pdd-basic-meta i {
  margin-right: 4px;
  color: #c0c4cc;
}
.pdd-bind-row {
  display: flex;
  flex-direction: column;
  gap: 12px;
}
.pdd-bind-chip {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 10px 12px;
  padding: 12px 14px;
  border-radius: 8px;
  border: 1px solid #e4e7ed;
  background: #fafbfc;
  cursor: pointer;
  transition: transform 0.2s ease, box-shadow 0.2s ease, border-color 0.2s ease;
}
.pdd-bind-chip:hover {
  transform: scale(1.01);
  box-shadow: 0 4px 12px rgba(0, 64, 133, 0.1);
  border-color: #c6d4e4;
}
.pdd-bind-chip:focus {
  outline: none;
  box-shadow: 0 0 0 2px rgba(0, 64, 133, 0.2);
}
.pdd-bind-chip i {
  font-size: 20px;
}
.pdd-bind-chip.is-done i {
  color: #67c23a;
}
.pdd-bind-chip.is-pending i {
  color: #c0c4cc;
}
.pdd-bind-label {
  font-size: 14px;
  font-weight: 600;
  color: #303133;
  min-width: 4.5em;
}
.pdd-bind-time {
  width: 100%;
  font-size: 12px;
  color: #c0c4cc;
  margin-left: 0;
  flex-basis: 100%;
  padding-left: 32px;
}
.pdd-timeline {
  padding: 4px 0 4px 4px;
}
.pdd-timeline-item {
  display: flex;
  gap: 14px;
  min-height: 48px;
}
.pdd-timeline-track {
  position: relative;
  width: 18px;
  flex-shrink: 0;
  display: flex;
  flex-direction: column;
  align-items: center;
}
.pdd-timeline-node {
  width: 12px;
  height: 12px;
  border-radius: 50%;
  border: 2px solid #dcdfe6;
  background: #fff;
  z-index: 1;
  flex-shrink: 0;
  transition: border-color 0.2s, background 0.2s;
}
.pdd-timeline-item.is-done .pdd-timeline-node {
  border-color: #004085;
  background: #004085;
}
.pdd-timeline-item.is-current .pdd-timeline-node {
  box-shadow: 0 0 0 4px rgba(0, 64, 133, 0.2);
  border-color: #004085;
  background: #004085;
}
.pdd-timeline-item.is-current:not(.is-done) .pdd-timeline-node {
  background: #fff;
  border-width: 3px;
}
.pdd-timeline-line {
  flex: 1;
  width: 2px;
  min-height: 12px;
  margin: 2px 0;
  background: #e4e7ed;
  border-radius: 1px;
}
.pdd-timeline-content {
  flex: 1;
  padding-bottom: 14px;
}
.pdd-timeline-title {
  font-size: 14px;
  font-weight: 600;
  color: #303133;
  line-height: 1.4;
}
.pdd-timeline-item:not(.is-done) .pdd-timeline-title {
  color: #909399;
  font-weight: 500;
}
.pdd-timeline-item.is-current:not(.is-done) .pdd-timeline-title {
  color: #004085;
  font-weight: 700;
}
.pdd-timeline-meta {
  margin-top: 4px;
  font-size: 12px;
}
.pdd-timeline-done {
  color: #303133;
}
.pdd-timeline-time {
  color: #909399;
  margin-left: 8px;
}
.pdd-timeline-pending {
  color: #c0c4cc;
}
.pdd-empty-inline {
  text-align: center;
  color: #c0c4cc;
  font-size: 13px;
  padding: 12px 0;
}
.pdd-ai-placeholder {
  text-align: center;
  padding: 28px 16px;
  color: #c0c4cc;
  background: #fafbfc;
  border-radius: 8px;
  border: 1px dashed #e4e7ed;
}
.pdd-ai-placeholder i {
  font-size: 36px;
  margin-bottom: 8px;
  display: block;
  opacity: 0.6;
}
.pdd-ai-placeholder p {
  margin: 0 0 6px;
  font-size: 14px;
  color: #909399;
}
.pdd-ai-placeholder-hint {
  font-size: 12px;
  color: #c0c4cc;
  line-height: 1.5;
  display: block;
  max-width: 280px;
  margin: 0 auto;
}
.pdd-ai-card {
  background: linear-gradient(180deg, #f8fafc 0%, #fff 40%);
  border: 1px solid #e4e7ed;
  border-radius: 10px;
  padding: 14px 16px;
  box-shadow: 0 2px 8px rgba(0, 64, 133, 0.06);
}
.pdd-ai-card-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  margin-bottom: 10px;
}
.pdd-ai-card-label {
  font-size: 12px;
  font-weight: 600;
  color: #004085;
  text-transform: uppercase;
  letter-spacing: 0.06em;
}
.pdd-ai-conclusion {
  margin: 0;
  font-size: 14px;
  line-height: 1.65;
  color: #303133;
}
.pdd-ai-suggest-block {
  margin-top: 14px;
  padding-top: 12px;
  border-top: 1px solid #ebeef5;
}
.pdd-ai-suggest-title {
  font-size: 12px;
  font-weight: 600;
  color: #606266;
  margin-bottom: 6px;
}
.pdd-ai-suggest-text {
  margin: 0 0 6px;
  font-size: 13px;
  color: #606266;
  line-height: 1.55;
}
.pdd-footer {
  margin-top: 20px;
  padding-top: 16px;
  border-top: 1px solid #ebeef5;
  display: flex;
  flex-wrap: wrap;
  justify-content: flex-end;
  gap: 10px;
}
.pdd-btn-wrap {
  display: inline-block;
}
.pdd-btn {
  transition: transform 0.18s ease, box-shadow 0.18s ease, opacity 0.18s ease;
}
.pdd-btn:not(:disabled):hover {
  transform: translateY(-1px);
  box-shadow: 0 4px 10px rgba(0, 64, 133, 0.15);
}
.pdd-btn:not(:disabled):active {
  transform: translateY(0);
  box-shadow: none;
}
.pdd-btn--primary {
  background: #004085 !important;
  border-color: #004085 !important;
  color: #fff !important;
}
.pdd-btn--primary:hover:not(:disabled),
.pdd-btn--primary:focus:not(:disabled) {
  background: #00306b !important;
  border-color: #00306b !important;
}
.pdd-btn--secondary {
  background: #fff !important;
  border-color: #004085 !important;
  color: #004085 !important;
}
.pdd-btn--secondary:hover:not(:disabled) {
  background: rgba(0, 64, 133, 0.06) !important;
}
.pdd-btn--ghost {
  background: #fff !important;
  border-color: #dcdfe6 !important;
  color: #606266 !important;
}
.pdd-btn--ghost:hover:not(:disabled) {
  border-color: #004085 !important;
  color: #004085 !important;
}
.pdd-btn--neutral {
  background: #f4f4f5 !important;
  border-color: #e4e7ed !important;
  color: #606266 !important;
}
.pdd-btn--primary.is-disabled,
.pdd-btn--primary.is-disabled:hover {
  background: #e4e7ed !important;
  border-color: #dcdfe6 !important;
  color: #c0c4cc !important;
  transform: none !important;
  box-shadow: none !important;
}
</style>

<style>
/* append-to-body 抽屉外壳无 scoped，须全局命中 custom-class */
.patient-detail-drawer.el-drawer {
  transition: transform 0.28s cubic-bezier(0.23, 1, 0.32, 1) !important;
}
.patient-detail-drawer.el-drawer.rtl {
  box-shadow: -8px 0 32px rgba(0, 0, 0, 0.12);
}
.patient-detail-drawer .el-drawer__header {
  margin-bottom: 0;
  padding: 14px 20px;
  background: #004085;
  color: #fff !important;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.12);
  border-bottom: 1px solid rgba(255, 255, 255, 0.08);
}
.patient-detail-drawer .el-drawer__title {
  flex: 1;
  color: #fff !important;
}
.patient-detail-drawer .el-drawer__close-btn {
  color: #fff !important;
  top: 14px;
  right: 16px;
  transition: transform 0.2s ease, opacity 0.2s ease;
}
.patient-detail-drawer .el-drawer__close-btn:hover {
  color: #fff !important;
  opacity: 0.9;
  transform: scale(1.08);
}
.patient-detail-drawer .el-drawer__body {
  background: #f5f7fa;
  padding: 0;
  overflow: auto;
}
</style>
