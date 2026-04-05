<template>
  <!-- 电子病历 - 病历模板 UTF-8 -->
  <div class="app-container">
    <el-form :model="queryParams" ref="queryForm" size="small" :inline="true" v-show="showSearch" label-width="100px">
      <el-form-item label="模板名称" prop="templateName">
        <el-input v-model="queryParams.templateName" placeholder="请输入" clearable @keyup.enter.native="handleQuery" />
      </el-form-item>
      <el-form-item label="模板类型" prop="templateType">
        <el-select v-model="queryParams.templateType" clearable placeholder="全部" style="width:140px">
          <el-option v-for="o in typeOptions" :key="o.value" :label="o.label" :value="o.value" />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="el-icon-search" size="mini" @click="handleQuery">查询</el-button>
        <el-button icon="el-icon-refresh" size="mini" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>
    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5">
        <el-button type="primary" plain icon="el-icon-plus" size="mini" @click="handleAdd" v-hasPermi="['emr:template:add']">新增</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="success" plain icon="el-icon-edit" size="mini" :disabled="single" @click="handleUpdate" v-hasPermi="['emr:template:edit']">修改</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="danger" plain icon="el-icon-delete" size="mini" :disabled="multiple" @click="handleDelete" v-hasPermi="['emr:template:remove']">删除</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="warning" plain icon="el-icon-download" size="mini" @click="handleExport" v-hasPermi="['emr:template:export']">导出</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="info" plain icon="el-icon-upload2" size="mini" @click="$refs.f.click()" v-hasPermi="['emr:template:import']">导入</el-button>
        <input ref="f" type="file" accept=".xlsx,.xls" style="display:none" @change="onImport" />
      </el-col>
      <right-toolbar :showSearch.sync="showSearch" @queryTable="getList" />
    </el-row>
    <el-table v-loading="loading" :data="list" @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="55" align="center" />
      <el-table-column label="ID" prop="id" width="70" align="center" />
      <el-table-column label="名称" prop="templateName" min-width="140" show-overflow-tooltip />
      <el-table-column label="类型" prop="templateType" width="120" :formatter="fmtTemplateType" />
      <el-table-column label="备注" prop="remark" min-width="120" show-overflow-tooltip />
      <el-table-column label="创建时间" prop="createTime" width="160" align="center">
        <template slot-scope="s">{{ parseTime(s.row.createTime) }}</template>
      </el-table-column>
      <el-table-column label="操作" align="center" width="120">
        <template slot-scope="scope">
          <el-button size="mini" type="text" icon="el-icon-edit" @click="handleUpdateRow(scope.row)" v-hasPermi="['emr:template:edit']">修改</el-button>
          <el-button size="mini" type="text" icon="el-icon-delete" @click="handleDeleteRow(scope.row)" v-hasPermi="['emr:template:remove']">删除</el-button>
        </template>
      </el-table-column>
    </el-table>
    <pagination v-show="total>0" :total="total" :page.sync="queryParams.pageNum" :limit.sync="queryParams.pageSize" @pagination="getList" />

    <el-dialog :title="title" :visible.sync="open" width="800px" append-to-body>
      <el-form ref="form" :model="form" :rules="rules" label-width="120px">
        <el-form-item label="模板名称" prop="templateName"><el-input v-model="form.templateName" /></el-form-item>
        <el-form-item label="模板类型" prop="templateType">
          <el-select v-model="form.templateType" style="width:100%">
            <el-option v-for="o in typeOptions" :key="o.value" :label="o.label" :value="o.value" />
          </el-select>
        </el-form-item>
        <el-form-item label="备注"><el-input v-model="form.remark" /></el-form-item>
        <el-form-item label="模板内容" prop="templateContent">
          <editor v-model="form.templateContent" :min-height="240" />
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button type="primary" @click="submitForm">确定</el-button>
        <el-button @click="open=false">取消</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import request from '@/utils/request'
import Editor from '@/components/Editor'
import { listTemplate, getTemplate, addTemplate, updateTemplate, delTemplate } from '@/api/medical/emr/template'

export default {
  name: 'EmrTemplate',
  components: { Editor },
  data() {
    return {
      loading: true,
      showSearch: true,
      list: [],
      total: 0,
      ids: [],
      single: true,
      multiple: true,
      title: '',
      open: false,
      queryParams: { pageNum: 1, pageSize: 10, templateName: undefined, templateType: undefined },
      form: {},
      typeOptions: [
        { value: 'admission', label: '入院记录' },
        { value: 'discharge', label: '出院记录' },
        { value: 'surgery', label: '手术记录' },
        { value: 'course', label: '病程记录' }
      ],
      rules: {
        templateName: [{ required: true, message: '请输入模板名称', trigger: 'blur' }],
        templateType: [{ required: true, message: '请选择模板类型', trigger: 'change' }],
        templateContent: [{ required: true, message: '请填写模板内容', trigger: 'blur' }]
      }
    }
  },
  created() { this.getList() },
  methods: {
    fmtTemplateType(row) {
      const m = { admission: '入院记录', discharge: '出院记录', surgery: '手术记录', course: '病程记录' }
      return m[row.templateType] || row.templateType || '-'
    },
    getList() {
      this.loading = true
      listTemplate(this.queryParams).then(res => {
        this.list = res.rows
        this.total = res.total
        this.loading = false
      })
    },
    handleQuery() { this.queryParams.pageNum = 1; this.getList() },
    resetQuery() { this.resetForm('queryForm'); this.handleQuery() },
    handleSelectionChange(sel) {
      this.ids = sel.map(i => i.id)
      this.single = sel.length !== 1
      this.multiple = !sel.length
    },
    reset() {
      this.form = { id: undefined, templateName: '', templateType: 'admission', templateContent: '', remark: '' }
      this.resetForm('form')
    },
    handleAdd() { this.reset(); this.open = true; this.title = '新增模板' },
    handleUpdate() {
      this.reset()
      getTemplate(this.ids[0]).then(res => {
        this.form = res.data
        this.open = true
        this.title = '编辑模板'
      })
    },
    handleUpdateRow(row) {
      this.reset()
      getTemplate(row.id).then(res => {
        this.form = res.data
        this.open = true
        this.title = '编辑模板'
      })
    },
    submitForm() {
      this.$refs.form.validate(valid => {
        if (!valid) return
        const api = this.form.id ? updateTemplate(this.form) : addTemplate(this.form)
        api.then(() => { this.$modal.msgSuccess('操作成功'); this.open = false; this.getList() })
      })
    },
    handleDelete() {
      this.$modal.confirm('是否删除选中的模板？').then(() => delTemplate(this.ids.join(','))).then(() => {
        this.getList(); this.$modal.msgSuccess('删除成功')
      }).catch(() => {})
    },
    handleDeleteRow(row) {
      this.$modal.confirm('是否删除该模板？').then(() => delTemplate(row.id)).then(() => {
        this.getList(); this.$modal.msgSuccess('删除成功')
      }).catch(() => {})
    },
    handleExport() {
      this.download('emr/template/export', { ...this.queryParams }, `病历模板_${Date.now()}.xlsx`)
    },
    onImport(e) {
      const file = e.target.files[0]
      if (!file) return
      const form = new FormData()
      form.append('file', file)
      request({ url: '/emr/template/importData', method: 'post', data: form }).then(data => {
        if (data && data.code === 200) { this.$modal.msgSuccess(data.msg || '导入成功'); this.getList() }
        else this.$modal.msgError((data && data.msg) || '导入失败')
      }).catch(() => {}).finally(() => { e.target.value = '' })
    }
  }
}
</script>
