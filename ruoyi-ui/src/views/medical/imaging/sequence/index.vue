<template>
  <!-- 医学影像 - 序列管理 UTF-8 -->
  <div class="app-container">
    <el-form :model="queryParams" ref="queryForm" size="small" :inline="true" v-show="showSearch" label-width="100px">
      <el-form-item label="序列名称" prop="seqName">
        <el-input v-model="queryParams.seqName" placeholder="名称" clearable @keyup.enter.native="handleQuery" />
      </el-form-item>
      <el-form-item label="归档" prop="archiveStatus">
        <el-select v-model="queryParams.archiveStatus" placeholder="全部" clearable style="width:100px">
          <el-option label="正常" value="0" />
          <el-option label="已归档" value="1" />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="el-icon-search" size="mini" @click="handleQuery">查询</el-button>
        <el-button icon="el-icon-refresh" size="mini" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5">
        <el-button type="primary" plain icon="el-icon-plus" size="mini" @click="handleAdd" v-hasPermi="['imaging:sequence:add']">新增</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="danger" plain icon="el-icon-delete" size="mini" :disabled="multiple" @click="handleDelete" v-hasPermi="['imaging:sequence:remove']">删除</el-button>
      </el-col>
      <right-toolbar :showSearch.sync="showSearch" @queryTable="getList" />
    </el-row>

    <el-table v-loading="loading" :data="list" @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="55" align="center" />
      <el-table-column label="ID" prop="id" width="70" align="center" />
      <el-table-column label="序列名称" prop="seqName" min-width="160" show-overflow-tooltip />
      <el-table-column label="患者信息" prop="patientInfo" min-width="120" show-overflow-tooltip />
      <el-table-column label="描述" prop="description" min-width="140" show-overflow-tooltip />
      <el-table-column label="归档" prop="archiveStatus" width="80" align="center">
        <template slot-scope="s">
          <el-tag :type="s.row.archiveStatus === '1' ? 'info' : 'success'" size="mini">{{ s.row.archiveStatus === '1' ? '已归档' : '正常' }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="排序" prop="sortOrder" width="70" align="center" />
      <el-table-column label="创建时间" prop="createTime" width="160" align="center">
        <template slot-scope="s">{{ parseTime(s.row.createTime) }}</template>
      </el-table-column>
      <el-table-column label="操作" width="260" align="center">
        <template slot-scope="scope">
          <el-button size="mini" type="text" icon="el-icon-edit" @click="handleEdit(scope.row)" v-if="scope.row.archiveStatus !== '1'" v-hasPermi="['imaging:sequence:edit']">修改</el-button>
          <el-button size="mini" type="text" icon="el-icon-picture" @click="manageImages(scope.row)" v-hasPermi="['imaging:sequence:edit']">影像</el-button>
          <el-button size="mini" type="text" @click="handleArchive(scope.row)" v-if="scope.row.archiveStatus !== '1'" v-hasPermi="['imaging:sequence:archive']">归档</el-button>
          <el-button size="mini" type="text" @click="handleUnarchive(scope.row)" v-if="scope.row.archiveStatus === '1'" v-hasPermi="['imaging:sequence:unarchive']">取消归档</el-button>
          <el-button size="mini" type="text" icon="el-icon-delete" @click="handleDeleteRow(scope.row)" v-hasPermi="['imaging:sequence:remove']">删除</el-button>
        </template>
      </el-table-column>
    </el-table>
    <pagination v-show="total>0" :total="total" :page.sync="queryParams.pageNum" :limit.sync="queryParams.pageSize" @pagination="getList" />

    <el-dialog :title="dialogTitle" :visible.sync="dialogOpen" width="600px" append-to-body>
      <el-form ref="seqForm" :model="form" :rules="rules" label-width="100px">
        <el-form-item label="序列名称" prop="seqName"><el-input v-model="form.seqName" /></el-form-item>
        <el-form-item label="患者信息"><el-input v-model="form.patientInfo" /></el-form-item>
        <el-form-item label="描述"><el-input v-model="form.description" type="textarea" :rows="2" /></el-form-item>
        <el-form-item label="排序号"><el-input-number v-model="form.sortOrder" :min="0" /></el-form-item>
      </el-form>
      <div slot="footer">
        <el-button type="primary" @click="submitForm">确定</el-button>
        <el-button @click="dialogOpen=false">取消</el-button>
      </div>
    </el-dialog>

    <el-dialog title="序列影像管理" :visible.sync="imgDialogOpen" width="700px" append-to-body>
      <p>当前序列: <b>{{ currentSeq ? currentSeq.seqName : '' }}</b></p>
      <el-alert title="输入影像ID（逗号分隔），保存后将替换序列内影像列表" type="info" :closable="false" class="mb8" />
      <el-input v-model="imageIdsStr" placeholder="如: 1,2,3,5" />
      <div v-if="seqImages.length" style="margin-top:12px">
        <el-tag v-for="img in seqImages" :key="img.id" style="margin:4px">{{ img.id }} - {{ img.imageName }}</el-tag>
      </div>
      <div slot="footer">
        <el-button type="primary" @click="saveSeqImages">保存</el-button>
        <el-button @click="imgDialogOpen=false">取消</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import { listSequence, getSequence, addSequence, updateSequence, delSequence, archiveSequence, unarchiveSequence, getSequenceImages, setSequenceImages } from '@/api/medical/imaging/sequence'

export default {
  name: 'ImagingSequence',
  data() {
    return {
      loading: true,
      showSearch: true,
      list: [],
      total: 0,
      ids: [],
      multiple: true,
      queryParams: { pageNum: 1, pageSize: 10, seqName: undefined, archiveStatus: undefined },
      dialogTitle: '',
      dialogOpen: false,
      form: {},
      rules: {
        seqName: [{ required: true, message: '请输入序列名称', trigger: 'blur' }]
      },
      imgDialogOpen: false,
      currentSeq: null,
      seqImages: [],
      imageIdsStr: ''
    }
  },
  created() { this.getList() },
  methods: {
    getList() {
      this.loading = true
      listSequence(this.queryParams).then(res => {
        this.list = res.rows; this.total = res.total; this.loading = false
      })
    },
    handleQuery() { this.queryParams.pageNum = 1; this.getList() },
    resetQuery() { this.resetForm('queryForm'); this.handleQuery() },
    handleSelectionChange(sel) { this.ids = sel.map(i => i.id); this.multiple = !sel.length },
    resetForm2() {
      this.form = { id: undefined, seqName: '', patientInfo: '', description: '', sortOrder: 0 }
    },
    handleAdd() { this.resetForm2(); this.dialogTitle = '新增序列'; this.dialogOpen = true },
    handleEdit(row) {
      this.resetForm2()
      getSequence(row.id).then(res => {
        this.form = res.data; this.dialogTitle = '编辑序列'; this.dialogOpen = true
      })
    },
    submitForm() {
      this.$refs.seqForm.validate(valid => {
        if (!valid) return
        const api = this.form.id ? updateSequence(this.form) : addSequence(this.form)
        api.then(() => { this.$modal.msgSuccess('操作成功'); this.dialogOpen = false; this.getList() })
      })
    },
    handleDelete() {
      this.$modal.confirm('是否删除选中的序列？').then(() => delSequence(this.ids.join(','))).then(() => {
        this.getList(); this.$modal.msgSuccess('操作成功')
      }).catch(() => {})
    },
    handleDeleteRow(row) {
      this.$modal.confirm('是否删除该序列？').then(() => delSequence(row.id)).then(() => {
        this.getList(); this.$modal.msgSuccess('操作成功')
      }).catch(() => {})
    },
    handleArchive(row) {
      this.$modal.confirm('归档后序列只读，是否继续？').then(() => archiveSequence(row.id)).then(() => {
        this.getList(); this.$modal.msgSuccess('操作成功')
      }).catch(() => {})
    },
    handleUnarchive(row) {
      this.$modal.confirm('确定取消归档？').then(() => unarchiveSequence(row.id)).then(() => {
        this.getList(); this.$modal.msgSuccess('操作成功')
      }).catch(() => {})
    },
    manageImages(row) {
      this.currentSeq = row
      this.seqImages = []
      this.imageIdsStr = ''
      getSequenceImages(row.id).then(res => {
        this.seqImages = res.data || []
        this.imageIdsStr = this.seqImages.map(i => i.id).join(',')
      })
      this.imgDialogOpen = true
    },
    saveSeqImages() {
      const ids = this.imageIdsStr.split(',').map(s => parseInt(s.trim(), 10)).filter(n => !isNaN(n))
      setSequenceImages(this.currentSeq.id, ids).then(() => {
        this.$modal.msgSuccess('影像列表已更新')
        this.manageImages(this.currentSeq)
      })
    }
  }
}
</script>
