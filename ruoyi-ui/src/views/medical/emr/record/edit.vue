<template>
  <!-- 电子病历 - 病历新增编辑 UTF-8 -->
  <div class="app-container">
    <el-card>
      <div slot="header" class="clearfix">
        <span>{{ recordId ? '编辑病历' : '新增病历' }}</span>
        <el-button style="float:right;padding:3px 0" type="text" @click="goBack">返回</el-button>
      </div>
      <el-form ref="form" :model="form" :rules="rules" label-width="120px">
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="标题" prop="title">
              <el-input v-model="form.title" placeholder="请输入病历标题" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="病历类型" prop="recordType">
              <el-select v-model="form.recordType" placeholder="请选择" style="width:100%">
                <el-option v-for="o in recordTypeOptions" :key="o.value" :label="o.label" :value="o.value" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="疾病关键词" prop="disease">
              <el-input v-model="form.disease" placeholder="用于检索、归档筛选" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="病历模板">
              <el-select v-model="templatePick" placeholder="选择模板" clearable filterable style="width:70%" @change="applyTemplate">
                <el-option v-for="t in templateList" :key="t.id" :label="t.templateName + '(' + fmtTemplateType(t.templateType) + ')'" :value="t.id" />
              </el-select>
              <el-button size="mini" type="primary" @click="loadTemplates">刷新列表</el-button>
            </el-form-item>
          </el-col>
        </el-row>
        <el-divider>结构化字段</el-divider>
        <el-row :gutter="20">
          <el-col :span="12"><el-form-item label="主诉"><el-input v-model="structured.chiefComplaint" type="textarea" :rows="2" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="现病史"><el-input v-model="structured.presentIllness" type="textarea" :rows="2" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="既往史"><el-input v-model="structured.pastHistory" type="textarea" :rows="2" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="体格检查"><el-input v-model="structured.physicalExam" type="textarea" :rows="2" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="检验检查"><el-input v-model="structured.labs" type="textarea" :rows="2" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="诊断计划"><el-input v-model="structured.plan" type="textarea" :rows="2" /></el-form-item></el-col>
        </el-row>
        <el-divider>病历内容</el-divider>
        <el-form-item label="病历正文" prop="content">
          <editor v-model="form.content" :min-height="280" />
        </el-form-item>
        <el-form-item>
          <el-button type="success" size="mini" @click="runExtract">自动提取实体</el-button>
          <span class="tip">实体位置会从 HTML 转为文本偏移计算</span>
        </el-form-item>
        <el-divider>实体列表</el-divider>
        <el-table :data="entities" border size="small">
          <el-table-column label="类型" width="140">
            <template slot-scope="s">
              <el-select v-model="s.row.labelType" size="mini" style="width:120px">
                <el-option v-for="o in labelTypeOptions" :key="o.value" :label="o.label" :value="o.value" />
              </el-select>
            </template>
          </el-table-column>
          <el-table-column label="文本" min-width="160"><template slot-scope="s"><el-input v-model="s.row.entityText" size="mini" /></template></el-table-column>
          <el-table-column label="起始" width="90"><template slot-scope="s"><el-input-number v-model="s.row.startPos" :min="0" size="mini" controls-position="right" /></template></el-table-column>
          <el-table-column label="结束" width="90"><template slot-scope="s"><el-input-number v-model="s.row.endPos" :min="0" size="mini" controls-position="right" /></template></el-table-column>
          <el-table-column label="操作" width="70" align="center">
            <template slot-scope="s">
              <el-button type="text" size="mini" @click="entities.splice(s.$index,1)">删除</el-button>
            </template>
          </el-table-column>
        </el-table>
        <el-button size="mini" type="primary" plain @click="addEntityRow" style="margin-top:8px">新增一行</el-button>
        <el-form-item style="margin-top:20px">
          <el-button type="primary" @click="submitForm" v-hasPermi="['emr:record:add','emr:record:edit']">提交</el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script>
import Editor from '@/components/Editor'
import { getRecord, addRecord, updateRecord, extractEntities } from '@/api/medical/emr/record'
import { listTemplate, getTemplate } from '@/api/medical/emr/template'

export default {
  name: 'EmrRecordEdit',
  components: { Editor },
  data() {
    return {
      recordId: undefined,
      templatePick: undefined,
      templateList: [],
      form: {
        id: undefined,
        title: '',
        content: '',
        disease: '',
        recordType: 'admission',
        archiveStatus: '0',
        structuredJson: ''
      },
      structured: {
        chiefComplaint: '',
        presentIllness: '',
        pastHistory: '',
        physicalExam: '',
        labs: '',
        plan: ''
      },
      entities: [],
      recordTypeOptions: [
        { value: 'admission', label: '入院记录' },
        { value: 'discharge', label: '出院记录' },
        { value: 'surgery', label: '手术记录' },
        { value: 'course', label: '病程记录' }
      ],
      labelTypeOptions: [
        { value: 'DISEASE', label: '疾病' },
        { value: 'DRUG', label: '药物' },
        { value: 'SURGERY', label: '手术' },
        { value: 'ANATOMY', label: '解剖部位' },
        { value: 'IMAGING', label: '影像' },
        { value: 'LAB', label: '检验' }
      ],
      rules: {
        title: [{ required: true, message: '请输入病历标题', trigger: 'blur' }]
      }
    }
  },
  created() {
    this.recordId = this.$route.query.id ? parseInt(this.$route.query.id, 10) : undefined
    this.loadTemplates()
    if (this.recordId) {
      this.loadDetail()
    }
  },
  methods: {
    fmtTemplateType(code) {
      const m = { admission: '入院', discharge: '出院', surgery: '手术', course: '病程' }
      return m[code] || code || '-'
    },
    goBack() {
      this.$router.push('/emr/record')
    },
    loadTemplates() {
      listTemplate({ pageNum: 1, pageSize: 200 }).then(res => {
        this.templateList = res.rows || []
      })
    },
    applyTemplate(id) {
      if (!id) return
      getTemplate(id).then(response => {
        const t = response.data
        if (t && t.templateContent) {
          this.form.content = t.templateContent
          this.form.recordType = t.templateType || this.form.recordType
          this.form.templateId = t.id
        }
      })
    },
    loadDetail() {
      getRecord(this.recordId).then(response => {
        const d = response.data
        if (!d || !d.record) return
        const r = d.record
        this.form = { ...this.form, ...r }
        this.entities = (d.entities || []).map(e => ({
          labelType: e.labelType,
          entityText: e.entityText,
          startPos: e.startPos,
          endPos: e.endPos
        }))
        this.parseStructured()
      })
    },
    parseStructured() {
      try {
        if (this.form.structuredJson) {
          const o = JSON.parse(this.form.structuredJson)
          this.structured = { ...this.structured, ...o }
        }
      } catch (e) {
        /* 无效JSON忽略 */
      }
    },
    runExtract() {
      extractEntities(this.form.content || '').then(response => {
        const list = response.data || []
        this.entities = list.map(e => ({
          labelType: e.labelType,
          entityText: e.entityText,
          startPos: e.startPos,
          endPos: e.endPos
        }))
        this.$modal.msgSuccess('已提取 ' + this.entities.length + ' 个实体')
      })
    },
    addEntityRow() {
      this.entities.push({ labelType: 'DISEASE', entityText: '', startPos: 0, endPos: 0 })
    },
    submitForm() {
      this.$refs.form.validate(valid => {
        if (!valid) return
        this.form.structuredJson = JSON.stringify(this.structured)
        const payload = {
          record: { ...this.form },
          entities: this.entities.filter(e => e.labelType && e.entityText)
        }
        const req = this.recordId ? updateRecord(payload) : addRecord(payload)
        req.then(response => {
          this.$modal.msgSuccess('操作成功')
          if (!this.recordId && response.data != null) {
            this.recordId = response.data
            this.form.id = response.data
            this.$router.replace({ query: { id: String(response.data) } })
          } else {
            this.goBack()
          }
        })
      })
    }
  }
}
</script>

<style scoped>
.tip { margin-left: 12px; color: #909399; font-size: 12px; }
</style>
