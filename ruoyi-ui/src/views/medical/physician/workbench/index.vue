<template>
  <div class="app-container physician-workbench">
    <div class="physician-workbench__hero">
      <h1 class="physician-workbench__title">医生工作台</h1>
      <p class="physician-workbench__lead">
        欢迎，{{ nickName || '医生' }}。此处为医师端首页，后续可接入待诊队列、患者概览、快捷入口等业务模块。
      </p>
    </div>
    <el-row :gutter="16" class="physician-workbench__cards">
      <el-col :xs="24" :sm="12" :md="8">
        <el-card shadow="never" class="physician-workbench__card">
          <div slot="header" class="physician-workbench__card-head">工作台</div>
          <p class="physician-workbench__card-text">模块占位：今日排班、待处理任务等。</p>
        </el-card>
      </el-col>
      <el-col :xs="24" :sm="12" :md="8">
        <el-card shadow="never" class="physician-workbench__card">
          <div slot="header" class="physician-workbench__card-head">患者服务</div>
          <p class="physician-workbench__card-text">模块占位：最近接诊、随访提醒等。</p>
        </el-card>
      </el-col>
      <el-col :xs="24" :sm="12" :md="8">
        <el-card shadow="never" class="physician-workbench__card">
          <div slot="header" class="physician-workbench__card-head">诊疗辅助</div>
          <p class="physician-workbench__card-text">模块占位：影像调阅、病历入口等。</p>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script>
import { mapGetters } from 'vuex'

export default {
  name: 'PhysicianWorkbench',
  computed: {
    ...mapGetters(['nickName'])
  }
}
</script>

<style lang="scss" scoped>
.physician-workbench {
  background: #f5f7fa;
  min-height: calc(100vh - 120px);
}

.physician-workbench__hero {
  background: #ffffff;
  border: 1px solid #e8ecf2;
  border-radius: 6px;
  padding: 24px 22px;
  margin-bottom: 16px;
}

.physician-workbench__title {
  margin: 0 0 10px;
  font-size: 20px;
  font-weight: 600;
  color: #333333;
}

.physician-workbench__lead {
  margin: 0;
  font-size: 14px;
  line-height: 1.7;
  color: #666666;
}

.physician-workbench__card {
  border-radius: 6px;
  border: 1px solid #e8ecf2;
  margin-bottom: 16px;
}

.physician-workbench__card-head {
  font-weight: 600;
  color: #165dff;
  font-size: 14px;
}

.physician-workbench__card-text {
  margin: 0;
  font-size: 13px;
  color: #666666;
  line-height: 1.6;
}
</style>
