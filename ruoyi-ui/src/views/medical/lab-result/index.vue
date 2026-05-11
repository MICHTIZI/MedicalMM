<template>
  <div class="app-container">
    <el-form :model="queryParams" ref="queryForm" size="small" :inline="true" v-show="showSearch" label-width="84px">
      <el-form-item label="患者姓名" prop="patientName">
        <el-input v-model="queryParams.patientName" placeholder="按姓名模糊查询列表" clearable @keyup.enter.native="handleQuery" style="width: 200px" />
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="el-icon-search" size="mini" @click="handleQuery">搜索</el-button>
        <el-button icon="el-icon-refresh" size="mini" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5">
        <el-button type="primary" plain icon="el-icon-plus" size="mini" @click="handleAdd" v-hasPermi="['medical:lab:add']">新增</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="success" plain icon="el-icon-edit" size="mini" :disabled="single" @click="handleUpdate" v-hasPermi="['medical:lab:edit']">修改</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="danger" plain icon="el-icon-delete" size="mini" :disabled="multiple" @click="handleDelete" v-hasPermi="['medical:lab:remove']">删除</el-button>
      </el-col>
      <right-toolbar :showSearch.sync="showSearch" @queryTable="getList" />
    </el-row>

    <el-alert title="请先点击「新增」，在弹出窗口中选择患者后可手工录入或通过 TXT 导入检验数据（修改记录时可在窗口内更正指标）。" type="info" :closable="false" class="mb8" />

    <el-table v-loading="loading" :data="labList" @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="55" align="center" />
      <el-table-column label="ID" prop="id" width="70" align="center" />
      <el-table-column label="患者姓名" prop="patientName" min-width="110" show-overflow-tooltip />
      <el-table-column label="检验时间" prop="testDate" width="160" align="center">
        <template slot-scope="scope">{{ parseTime(scope.row.testDate) }}</template>
      </el-table-column>
      <el-table-column label="检验科室" prop="testDepartment" width="120" show-overflow-tooltip />
      <el-table-column label="体温℃" prop="temperature" width="72" align="center" />
      <el-table-column label="心率" prop="heartRate" width="72" align="center" />
      <el-table-column label="CRP" prop="crp" width="80" align="center" />
      <el-table-column label="白细胞" prop="wbc" width="88" align="center" />
      <el-table-column label="备注" prop="remark" min-width="120" show-overflow-tooltip />
      <el-table-column label="操作" align="center" width="140" class-name="small-padding fixed-width">
        <template slot-scope="scope">
          <el-button size="mini" type="text" icon="el-icon-edit" @click="handleUpdate(scope.row)" v-hasPermi="['medical:lab:edit']">修改</el-button>
          <el-button size="mini" type="text" icon="el-icon-delete" @click="handleDelete(scope.row)" v-hasPermi="['medical:lab:remove']">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <pagination v-show="total > 0" :total="total" :page.sync="queryParams.pageNum" :limit.sync="queryParams.pageSize" @pagination="getList" />

    <el-dialog :title="title" :visible.sync="open" width="780px" append-to-body destroy-on-close>
      <el-form ref="form" :model="form" :rules="rules" label-width="120px">
        <el-form-item label="所属患者" prop="patientId">
          <el-select v-model="form.patientId" filterable remote reserve-keyword :remote-method="remotePatient" :loading="patientLoading" placeholder="输入关键字搜索并选择患者" style="width: 100%" :disabled="!!form.id">
            <el-option v-for="p in patientHits" :key="p.patientId" :label="p.patientName + ' (ID:' + p.patientId + ')'" :value="p.patientId" />
          </el-select>
        </el-form-item>
        <el-form-item v-if="!form.id" label="检验TXT">
          <el-button type="info" plain icon="el-icon-upload2" size="small" :disabled="!form.patientId" @click="$refs.dialogLabTxt.click()" v-hasPermi="['medical:lab:import']">选择TXT解析并填入表单</el-button>
          <input ref="dialogLabTxt" type="file" accept=".txt,text/plain" style="display:none" @change="handleDialogTxtImport" />
          <span style="margin-left:10px;color:#909399;font-size:12px">需先选定患者；解析后填充下方字段，核对无误后点「确定」写入数据库。</span>
        </el-form-item>
        <el-form-item label="检验时间" prop="testDate">
          <el-date-picker v-model="form.testDate" type="datetime" value-format="yyyy-MM-dd HH:mm:ss" placeholder="请选择检验时间" style="width: 100%" />
        </el-form-item>
        <el-form-item label="检验医师" prop="testDoctor">
          <el-input v-model="form.testDoctor" placeholder="检验医师姓名" />
        </el-form-item>
        <el-form-item label="检验科室" prop="testDepartment">
          <el-input v-model="form.testDepartment" placeholder="检验科室名称" />
        </el-form-item>

        <el-collapse v-model="activeCollapse">
          <el-collapse-item title="生命体征" name="vitals">
            <el-row :gutter="12">
              <el-col :span="8"><el-form-item label="体温(℃)" prop="temperature"><el-input-number v-model="form.temperature" :precision="1" :step="0.1" controls-position="right" style="width: 100%" /></el-form-item></el-col>
              <el-col :span="8"><el-form-item label="心率"><el-input-number v-model="form.heartRate" :min="0" :precision="0" controls-position="right" style="width: 100%" /></el-form-item></el-col>
              <el-col :span="8"><el-form-item label="呼吸频率"><el-input-number v-model="form.respiratoryRate" :min="0" controls-position="right" style="width: 100%" /></el-form-item></el-col>
              <el-col :span="8"><el-form-item label="收缩压"><el-input-number v-model="form.systolicBp" :min="0" controls-position="right" style="width: 100%" /></el-form-item></el-col>
              <el-col :span="8"><el-form-item label="舒张压"><el-input-number v-model="form.diastolicBp" :min="0" controls-position="right" style="width: 100%" /></el-form-item></el-col>
              <el-col :span="8"><el-form-item label="血氧SpO2(%)"><el-input-number v-model="form.spo2" :precision="1" :step="0.1" controls-position="right" style="width: 100%" /></el-form-item></el-col>
            </el-row>
          </el-collapse-item>
          <el-collapse-item title="血常规" name="cbc">
            <el-row :gutter="12">
              <el-col :span="8"><el-form-item label="WBC"><el-input-number v-model="form.wbc" :precision="2" controls-position="right" style="width: 100%" /></el-form-item></el-col>
              <el-col :span="8"><el-form-item label="中性粒细胞比%"><el-input-number v-model="form.neutrophilRatio" :precision="1" controls-position="right" style="width: 100%" /></el-form-item></el-col>
              <el-col :span="8"><el-form-item label="淋巴细胞比%"><el-input-number v-model="form.lymphocyteRatio" :precision="1" controls-position="right" style="width: 100%" /></el-form-item></el-col>
              <el-col :span="8"><el-form-item label="单核细胞比%"><el-input-number v-model="form.monocyteRatio" :precision="1" controls-position="right" style="width: 100%" /></el-form-item></el-col>
              <el-col :span="8"><el-form-item label="血小板"><el-input-number v-model="form.platelet" :min="0" controls-position="right" style="width: 100%" /></el-form-item></el-col>
            </el-row>
          </el-collapse-item>
          <el-collapse-item title="炎症指标" name="inflam">
            <el-row :gutter="12">
              <el-col :span="8"><el-form-item label="CRP"><el-input-number v-model="form.crp" :precision="1" controls-position="right" style="width: 100%" /></el-form-item></el-col>
              <el-col :span="8"><el-form-item label="PCT"><el-input-number v-model="form.pct" :precision="2" controls-position="right" style="width: 100%" /></el-form-item></el-col>
              <el-col :span="8"><el-form-item label="ESR"><el-input-number v-model="form.esr" :min="0" controls-position="right" style="width: 100%" /></el-form-item></el-col>
            </el-row>
          </el-collapse-item>
          <el-collapse-item title="血气分析" name="gas">
            <el-row :gutter="12">
              <el-col :span="8"><el-form-item label="pH"><el-input-number v-model="form.ph" :precision="2" :step="0.01" controls-position="right" style="width: 100%" /></el-form-item></el-col>
              <el-col :span="8"><el-form-item label="PO2"><el-input-number v-model="form.po2" :precision="1" controls-position="right" style="width: 100%" /></el-form-item></el-col>
              <el-col :span="8"><el-form-item label="PCO2"><el-input-number v-model="form.pco2" :precision="1" controls-position="right" style="width: 100%" /></el-form-item></el-col>
              <el-col :span="8"><el-form-item label="HCO3"><el-input-number v-model="form.hco3" :precision="1" controls-position="right" style="width: 100%" /></el-form-item></el-col>
            </el-row>
          </el-collapse-item>
        </el-collapse>
        <el-form-item label="备注" prop="remark">
          <el-input v-model="form.remark" type="textarea" :rows="2" placeholder="备注说明" />
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
import { listLab, getLab, addLab, updateLab, delLab, parseLabTxt, listLabPatientOptions } from '@/api/medical/labResult'

export default {
  name: 'MedicalLabResult',
  data() {
    return {
      loading: true,
      showSearch: true,
      ids: [],
      single: true,
      multiple: true,
      total: 0,
      labList: [],
      title: '',
      open: false,
      patientHits: [],
      patientLoading: false,
      activeCollapse: ['vitals', 'cbc', 'inflam', 'gas'],
      queryParams: {
        pageNum: 1,
        pageSize: 10,
        patientName: undefined
      },
      form: {},
      rules: {
        patientId: [{ required: true, message: '请选择患者', trigger: 'change' }],
        testDate: [{ required: true, message: '请选择检验时间', trigger: 'change' }]
      }
    }
  },
  created() {
    this.getList()
  },
  methods: {
    remotePatient(query) {
      const q = (query || '').trim()
      this.patientLoading = true
      listLabPatientOptions({ patientName: q, pageNum: 1, pageSize: 30 })
        .then(res => {
          this.patientHits = res.data || []
        })
        .finally(() => { this.patientLoading = false })
    },
    getList() {
      this.loading = true
      listLab(this.queryParams).then(res => {
        this.labList = res.rows
        this.total = res.total
        this.loading = false
      }).catch(() => { this.loading = false })
    },
    handleQuery() {
      this.queryParams.pageNum = 1
      this.getList()
    },
    resetQuery() {
      this.resetForm('queryForm')
      this.queryParams.patientName = undefined
      this.handleQuery()
    },
    emptyForm() {
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
    handleAdd() {
      this.resetForm('form')
      this.form = this.emptyForm()
      this.remotePatient('')
      this.open = true
      this.title = '新增检验指标'
    },
    handleUpdate(row) {
      const id = row.id || this.ids[0]
      getLab(id).then(res => {
        this.form = Object.assign({}, res.data)
        this.open = true
        this.title = '修改检验指标'
        const pid = this.form.patientId
        listLabPatientOptions({ patientName: '', pageNum: 1, pageSize: 30 }).then(r => {
          const list = r.data || []
          if (pid != null && !list.some(p => p.patientId === pid)) {
            list.unshift({ patientId: pid, patientName: this.form.patientName || ('ID:' + pid) })
          }
          this.patientHits = list
        })
      })
    },
    submitForm() {
      this.$refs.form.validate(valid => {
        if (!valid) return
        const api = this.form.id ? updateLab : addLab
        api(this.form).then(() => {
          this.$modal.msgSuccess('保存成功')
          this.open = false
          this.getList()
        })
      })
    },
    cancel() {
      this.open = false
    },
    handleSelectionChange(sel) {
      this.ids = sel.map(i => i.id)
      this.single = sel.length !== 1
      this.multiple = !sel.length
    },
    handleDelete(row) {
      const ids = row.id ? [row.id] : this.ids
      this.$modal.confirm('是否确认删除选中的检验指标数据？').then(() => delLab(ids.join(','))).then(() => {
        this.$modal.msgSuccess('删除成功')
        this.getList()
      }).catch(() => {})
    },
    handleDialogTxtImport(e) {
      const files = e.target.files
      if (!files || !files.length) return
      const pid = this.form.patientId
      if (!pid) {
        this.$modal.msgWarning('请先在下拉框中选择患者')
        e.target.value = ''
        return
      }
      const file = files[0]
      parseLabTxt(file).then(res => {
        const parsed = res.data || {}
        const base = this.emptyForm()
        base.patientId = pid
        const skip = ['id', 'patientId', 'patientName', 'patientAttendingDoctorIdScope', 'isDeleted', 'params', 'searchValue']
        Object.keys(parsed).forEach(key => {
          if (skip.includes(key)) return
          const v = parsed[key]
          if (v !== undefined && v !== null) {
            base[key] = v
          }
        })
        this.form = base
        this.$nextTick(() => {
          if (this.$refs.form) this.$refs.form.clearValidate()
        })
        this.$modal.msgSuccess('已从 TXT 填充表单，请核对后点击「确定」保存')
      }).finally(() => { e.target.value = '' })
    }
  }
}
</script>

