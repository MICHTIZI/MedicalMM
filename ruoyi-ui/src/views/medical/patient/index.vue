<template>
  <div class="app-container">
    <el-form :model="queryParams" ref="queryForm" size="small" :inline="true" v-show="showSearch" label-width="90px">
      <el-form-item label="患者姓名" prop="patientName">
        <el-input v-model="queryParams.patientName" placeholder="请输入患者姓名" clearable @keyup.enter.native="handleQuery" />
      </el-form-item>
      <el-form-item label="性别" prop="gender">
        <el-select v-model="queryParams.gender" placeholder="全部" clearable style="width: 120px">
          <el-option v-for="dict in genderOptions" :key="dict.value" :label="dict.label" :value="dict.value" />
        </el-select>
      </el-form-item>
      <el-form-item label="电话" prop="phone">
        <el-input v-model="queryParams.phone" placeholder="请输入电话" clearable @keyup.enter.native="handleQuery" />
      </el-form-item>
      <el-form-item label="主治医生" prop="attendingDoctor">
        <el-input v-model="queryParams.attendingDoctor" placeholder="请输入医生姓名" clearable @keyup.enter.native="handleQuery" />
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="el-icon-search" size="mini" @click="handleQuery">查询</el-button>
        <el-button icon="el-icon-refresh" size="mini" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5">
        <el-button type="primary" plain icon="el-icon-plus" size="mini" @click="handleAdd" v-hasPermi="['medical:patient:add']">新增</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="success" plain icon="el-icon-edit" size="mini" :disabled="single" @click="handleUpdate" v-hasPermi="['medical:patient:edit']">修改</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="danger" plain icon="el-icon-delete" size="mini" :disabled="multiple" @click="handleDelete" v-hasPermi="['medical:patient:remove']">删除</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="warning" plain icon="el-icon-download" size="mini" @click="handleExport" v-hasPermi="['medical:patient:export']">导出</el-button>
      </el-col>
      <right-toolbar :showSearch.sync="showSearch" @queryTable="getList" />
    </el-row>

    <el-table v-loading="loading" :data="patientList" @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="55" align="center" />
      <el-table-column label="ID" align="center" prop="patientId" width="80" />
      <el-table-column label="患者姓名" align="center" prop="patientName" min-width="120" show-overflow-tooltip />
      <el-table-column label="性别" align="center" prop="gender" width="80" :formatter="genderFormat" />
      <el-table-column label="年龄" align="center" prop="age" width="80" />
      <el-table-column label="电话" align="center" prop="phone" width="130" show-overflow-tooltip />
      <el-table-column label="地址" align="center" prop="address" min-width="160" show-overflow-tooltip />
      <el-table-column label="主治医生" align="center" prop="attendingDoctor" width="140" show-overflow-tooltip />
      <el-table-column label="创建时间" align="center" prop="createTime" width="160">
        <template slot-scope="scope">
          <span>{{ parseTime(scope.row.createTime) }}</span>
        </template>
      </el-table-column>
      <el-table-column label="操作" align="center" class-name="small-padding fixed-width" width="160">
        <template slot-scope="scope">
          <el-button size="mini" type="text" icon="el-icon-edit" @click="handleUpdate(scope.row)" v-hasPermi="['medical:patient:edit']">修改</el-button>
          <el-button size="mini" type="text" icon="el-icon-delete" @click="handleDelete(scope.row)" v-hasPermi="['medical:patient:remove']">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <pagination v-show="total > 0" :total="total" :page.sync="queryParams.pageNum" :limit.sync="queryParams.pageSize" @pagination="getList" />

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
import { listPatient, getPatient, addPatient, updatePatient, delPatient, listDoctorOptions } from '@/api/medical/patient'

export default {
  name: 'MedicalPatient',
  data() {
    return {
      loading: true,
      showSearch: true,
      ids: [],
      single: true,
      multiple: true,
      total: 0,
      patientList: [],
      doctorOptions: [],
      title: '',
      open: false,
      queryParams: {
        pageNum: 1,
        pageSize: 10,
        patientName: undefined,
        gender: undefined,
        phone: undefined,
        attendingDoctor: undefined
      },
      form: {},
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
    }
  },
  created() {
    this.getList()
    if (this.canAssignDoctor) {
      this.getDoctorOptions()
    }
  },
  methods: {
    getList() {
      this.loading = true
      listPatient(this.queryParams).then(response => {
        this.patientList = response.rows
        this.total = response.total
        this.loading = false
      })
    },
    getDoctorOptions() {
      listDoctorOptions().then(response => {
        this.doctorOptions = response.data || []
      })
    },
    genderFormat(row) {
      const item = this.genderOptions.find(option => option.value === row.gender)
      return item ? item.label : '-'
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
    handleSelectionChange(selection) {
      this.ids = selection.map(item => item.patientId)
      this.single = selection.length !== 1
      this.multiple = !selection.length
    },
    handleAdd() {
      this.reset()
      this.open = true
      this.title = '新增患者'
      if (this.canAssignDoctor) {
        this.getDoctorOptions()
      }
    },
    handleUpdate(row) {
      this.reset()
      const patientId = row.patientId || this.ids[0]
      getPatient(patientId).then(response => {
        this.form = response.data
        this.open = true
        this.title = '修改患者'
        if (this.canAssignDoctor) {
          this.getDoctorOptions()
        }
      })
    },
    submitForm() {
      this.$refs.form.validate(valid => {
        if (!valid) return
        const request = this.form.patientId !== undefined ? updatePatient(this.form) : addPatient(this.form)
        request.then(() => {
          this.$modal.msgSuccess('操作成功')
          this.open = false
          this.getList()
        })
      })
    },
    handleDelete(row) {
      const patientIds = row.patientId || this.ids
      this.$modal.confirm('是否确认删除选中患者？').then(() => {
        return delPatient(patientIds)
      }).then(() => {
        this.getList()
        this.$modal.msgSuccess('删除成功')
      }).catch(() => {})
    },
    handleExport() {
      this.download('emr/patient/export', {
        ...this.queryParams
      }, `patient_${new Date().getTime()}.xlsx`)
    }
  }
}
</script>
