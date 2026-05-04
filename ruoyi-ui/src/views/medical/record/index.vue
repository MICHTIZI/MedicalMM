<template>
  <div class="app-container">
    <el-form :model="queryParams" ref="queryForm" size="small" :inline="true" v-show="showSearch" label-width="90px">
      <el-form-item label="患者姓名" prop="patientName">
        <el-input v-model="queryParams.patientName" placeholder="请输入患者姓名" clearable @keyup.enter.native="handleQuery" />
      </el-form-item>
      <el-form-item label="操作医生" prop="operateDoctor">
        <el-input v-model="queryParams.operateDoctor" placeholder="请输入医生姓名" clearable @keyup.enter.native="handleQuery" />
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="el-icon-search" size="mini" @click="handleQuery">查询</el-button>
        <el-button icon="el-icon-refresh" size="mini" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5">
        <el-button type="primary" plain icon="el-icon-plus" size="mini" @click="handleAdd" v-hasPermi="['medical:record:add']">新增</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="success" plain icon="el-icon-edit" size="mini" :disabled="single" @click="handleUpdate" v-hasPermi="['medical:record:edit']">修改</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="danger" plain icon="el-icon-delete" size="mini" :disabled="multiple" @click="handleDelete" v-hasPermi="['medical:record:remove']">删除</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="warning" plain icon="el-icon-download" size="mini" @click="handleExport" v-hasPermi="['medical:record:export']">导出</el-button>
      </el-col>
      <right-toolbar :showSearch.sync="showSearch" @queryTable="getList" />
    </el-row>

    <el-table v-loading="loading" :data="recordList" @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="55" align="center" />
      <el-table-column label="ID" align="center" prop="recordId" width="80" />
      <el-table-column label="患者姓名" align="center" prop="patientName" min-width="140" show-overflow-tooltip />
      <el-table-column label="操作医生" align="center" prop="operateDoctor" width="140" show-overflow-tooltip />
      <el-table-column label="初步诊断" align="center" prop="initialDiagnosis" min-width="180" show-overflow-tooltip />
      <el-table-column label="胸片" align="center" prop="imagePath" width="90">
        <template slot-scope="scope">
          <el-image v-if="scope.row.imagePath" :src="getImageUrl(scope.row.imagePath)" style="width:48px;height:48px" fit="cover" :preview-src-list="[getImageUrl(scope.row.imagePath)]" />
          <span v-else>-</span>
        </template>
      </el-table-column>
      <el-table-column label="创建时间" align="center" prop="createTime" width="160">
        <template slot-scope="scope">
          <span>{{ parseTime(scope.row.createTime) }}</span>
        </template>
      </el-table-column>
      <el-table-column label="操作" align="center" class-name="small-padding fixed-width" width="160">
        <template slot-scope="scope">
          <el-button size="mini" type="text" icon="el-icon-edit" @click="handleUpdate(scope.row)" v-hasPermi="['medical:record:edit']">修改</el-button>
          <el-button size="mini" type="text" icon="el-icon-delete" @click="handleDelete(scope.row)" v-hasPermi="['medical:record:remove']">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <pagination v-show="total > 0" :total="total" :page.sync="queryParams.pageNum" :limit.sync="queryParams.pageSize" @pagination="getList" />

    <el-dialog :title="title" :visible.sync="open" width="760px" append-to-body>
      <el-form ref="form" :model="form" :rules="rules" label-width="110px">
        <el-form-item label="患者" prop="patientId">
          <el-select v-model="form.patientId" filterable remote reserve-keyword :remote-method="getPatientOptions" placeholder="请选择患者" style="width: 100%">
            <el-option v-for="item in patientOptions" :key="item.patientId" :label="item.patientName" :value="item.patientId" />
          </el-select>
        </el-form-item>
        <el-row :gutter="10" class="mb8">
          <el-col :span="1.5">
            <el-button type="info" plain icon="el-icon-upload2" size="mini" @click="$refs.txtInput.click()">导入TXT病历</el-button>
            <input ref="txtInput" type="file" accept=".txt,text/plain" style="display:none" @change="handleTxtImport" />
          </el-col>
          <el-col :span="1.5">
            <el-button type="primary" plain icon="el-icon-picture-outline" size="mini" @click="openXrayDialog">绑定胸片</el-button>
          </el-col>
        </el-row>
        <el-form-item label="主诉" prop="chiefComplaint">
          <el-input v-model="form.chiefComplaint" type="textarea" :rows="3" placeholder="请输入主诉" />
        </el-form-item>
        <el-form-item label="现病史" prop="presentHistory">
          <el-input v-model="form.presentHistory" type="textarea" :rows="4" placeholder="请输入现病史" />
        </el-form-item>
        <el-form-item label="既往史" prop="pastHistory">
          <el-input v-model="form.pastHistory" type="textarea" :rows="3" placeholder="请输入既往史" />
        </el-form-item>
        <el-form-item label="体格检查" prop="physicalExam">
          <el-input v-model="form.physicalExam" type="textarea" :rows="3" placeholder="请输入体格检查" />
        </el-form-item>
        <el-form-item label="初步诊断" prop="initialDiagnosis">
          <el-input v-model="form.initialDiagnosis" type="textarea" :rows="3" placeholder="请输入初步诊断" />
        </el-form-item>
        <el-form-item label="已绑定胸片">
          <div v-if="form.imagePath">
            <el-image :src="getImageUrl(form.imagePath)" style="width: 120px; height: 120px" fit="cover" :preview-src-list="[getImageUrl(form.imagePath)]" />
            <div>{{ form.imagePath }}</div>
          </div>
          <span v-else>暂未绑定胸片</span>
        </el-form-item>
        <el-form-item label="AI结果路径" prop="aiResultPath">
          <el-input v-model="form.aiResultPath" placeholder="请输入AI结果路径（可选）" />
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button type="primary" @click="submitForm">确 定</el-button>
        <el-button @click="cancel">取 消</el-button>
      </div>
    </el-dialog>

    <el-dialog title="选择胸片" :visible.sync="xrayOpen" width="780px" append-to-body>
      <el-form size="small" :inline="true">
        <el-form-item label="关键词">
          <el-input v-model="xrayQuery.keyword" placeholder="文件名/路径" clearable @keyup.enter.native="getXrayOptions" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" icon="el-icon-search" size="mini" @click="getXrayOptions">查询</el-button>
        </el-form-item>
      </el-form>
      <el-table :data="xrayOptions" @row-dblclick="selectXray">
        <el-table-column label="预览" width="90" align="center">
          <template slot-scope="scope">
            <el-image :src="getImageUrl(scope.row.imagePath)" style="width:50px;height:50px" fit="cover" :preview-src-list="[getImageUrl(scope.row.imagePath)]" />
          </template>
        </el-table-column>
        <el-table-column label="文件名" prop="imageName" min-width="180" show-overflow-tooltip />
        <el-table-column label="路径" prop="imagePath" min-width="220" show-overflow-tooltip />
        <el-table-column label="操作" width="90" align="center">
          <template slot-scope="scope">
            <el-button size="mini" type="text" @click="selectXray(scope.row)">选择</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-dialog>
  </div>
</template>

<script>
import { listMedicalRecord, getMedicalRecord, addMedicalRecord, updateMedicalRecord, delMedicalRecord, listRecordPatientOptions, listRecordXrayOptions } from '@/api/medical/record'
import { imageUrl } from '@/api/medical/imaging/xray'

export default {
  name: 'MedicalRecord',
  data() {
    return {
      loading: true,
      showSearch: true,
      ids: [],
      single: true,
      multiple: true,
      total: 0,
      recordList: [],
      patientOptions: [],
      xrayOptions: [],
      title: '',
      open: false,
      xrayOpen: false,
      queryParams: {
        pageNum: 1,
        pageSize: 10,
        patientName: undefined,
        operateDoctor: undefined
      },
      xrayQuery: {
        keyword: undefined
      },
      form: {},
      rules: {
        patientId: [{ required: true, message: '请选择患者', trigger: 'change' }],
        chiefComplaint: [{ required: true, message: '请输入主诉', trigger: 'blur' }],
        presentHistory: [{ required: true, message: '请输入现病史', trigger: 'blur' }],
        pastHistory: [{ required: true, message: '请输入既往史', trigger: 'blur' }],
        physicalExam: [{ required: true, message: '请输入体格检查', trigger: 'blur' }],
        initialDiagnosis: [{ required: true, message: '请输入初步诊断', trigger: 'blur' }],
        imageId: [{ required: true, message: '请选择胸片', trigger: 'change' }]
      }
    }
  },
  created() {
    this.getList()
    this.getPatientOptions()
  },
  methods: {
    getImageUrl(path) {
      return imageUrl(path)
    },
    getList() {
      this.loading = true
      listMedicalRecord(this.queryParams).then(response => {
        this.recordList = response.rows
        this.total = response.total
        this.loading = false
      })
    },
    getPatientOptions(keyword) {
      listRecordPatientOptions({ patientName: keyword }).then(response => {
        this.patientOptions = response.data || []
      })
    },
    getXrayOptions() {
      listRecordXrayOptions(this.xrayQuery).then(response => {
        this.xrayOptions = response.data || []
      })
    },
    cancel() {
      this.open = false
      this.reset()
    },
    reset() {
      this.form = {
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
    handleSelectionChange(selection) {
      this.ids = selection.map(item => item.recordId)
      this.single = selection.length !== 1
      this.multiple = !selection.length
    },
    handleAdd() {
      this.reset()
      this.open = true
      this.title = '新增结构化病历'
      this.getPatientOptions()
    },
    handleUpdate(row) {
      this.reset()
      const recordId = row.recordId || this.ids[0]
      getMedicalRecord(recordId).then(response => {
        this.form = response.data
        this.open = true
        this.title = '修改结构化病历'
        this.getPatientOptions()
      })
    },
    submitForm() {
      this.$refs.form.validate(valid => {
        if (!valid) return
        const request = this.form.recordId !== undefined ? updateMedicalRecord(this.form) : addMedicalRecord(this.form)
        request.then(() => {
          this.$modal.msgSuccess('操作成功')
          this.open = false
          this.getList()
        })
      })
    },
    handleDelete(row) {
      const recordIds = row.recordId || this.ids
      this.$modal.confirm('是否确认删除选中病历？').then(() => {
        return delMedicalRecord(recordIds)
      }).then(() => {
        this.getList()
        this.$modal.msgSuccess('删除成功')
      }).catch(() => {})
    },
    handleExport() {
      this.download('emr/medicalRecord/export', {
        ...this.queryParams
      }, `medical_record_${new Date().getTime()}.xlsx`)
    },
    openXrayDialog() {
      this.xrayOpen = true
      this.getXrayOptions()
    },
    selectXray(row) {
      this.form.imageId = row.id
      this.form.imagePath = row.imagePath
      this.xrayOpen = false
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
        const pattern = next
          ? new RegExp(field.label + '[:：]([\\s\\S]*?)(?=' + next.label + '[:：])')
          : new RegExp(field.label + '[:：]([\\s\\S]*)')
        const match = text.match(pattern)
        if (match) {
          this.form[field.key] = match[1].trim()
        }
      })
    }
  }
}
</script>

