<template>
  <div class="app-container physician-workbench">
    <!-- 顶部欢迎 + 个人信息 -->
    <el-card shadow="never" class="pw-card pw-hero">
      <div class="pw-hero__left">
        <div class="pw-hero__greeting">
          你好，{{ nickName || userName || '医生' }}
        </div>
        <div class="pw-hero__meta">
          <span><i class="el-icon-user" /> {{ userName || '-' }}</span>
          <span><i class="el-icon-office-building" /> 科室：{{ deptName || '-' }}</span>
          <span><i class="el-icon-postcard" /> 工号：{{ userId || '-' }}</span>
          <span class="pw-hero__online">
            <i class="el-icon-success" /> 在线
          </span>
        </div>
      </div>
      <div class="pw-hero__right">
        <span class="pw-hero__date">{{ today }}</span>
      </div>
    </el-card>

    <!-- 统计看板 -->
    <el-row :gutter="16" class="pw-stats">
      <el-col :xs="24" :sm="12" :md="8" v-for="s in stats" :key="s.key">
        <el-card shadow="never" class="pw-card pw-stat" :class="'pw-stat--' + s.key">
          <div class="pw-stat__row">
            <div class="pw-stat__title">{{ s.title }}</div>
            <i :class="['pw-stat__icon', s.icon]" />
          </div>
          <div class="pw-stat__value" v-loading="s.loading">{{ s.value }}</div>
          <div class="pw-stat__foot">
            <el-link :underline="false" type="primary" @click="$router.push(s.link)">
              查看详情 <i class="el-icon-arrow-right" />
            </el-link>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 快捷入口 -->
    <el-card shadow="never" class="pw-card">
      <div slot="header" class="pw-card__header">
        <span>快捷操作</span>
      </div>
      <el-row :gutter="16">
        <el-col :xs="12" :sm="8" :md="6" v-for="q in quickEntries" :key="q.path">
          <div class="pw-quick" @click="$router.push(q.path)">
            <div class="pw-quick__icon" :style="{background: q.bg}">
              <i :class="q.icon" />
            </div>
            <div class="pw-quick__text">
              <div class="pw-quick__title">{{ q.title }}</div>
              <div class="pw-quick__desc">{{ q.desc }}</div>
            </div>
          </div>
        </el-col>
      </el-row>
    </el-card>

    <!-- 最近任务 -->
    <el-card shadow="never" class="pw-card">
      <div slot="header" class="pw-card__header">
        <span>近 7 天处理记录</span>
        <el-radio-group v-model="taskTab" size="mini">
          <el-radio-button label="all">全部</el-radio-button>
          <el-radio-button label="emr">病历</el-radio-button>
          <el-radio-button label="image">影像</el-radio-button>
          <el-radio-button label="report">报告</el-radio-button>
        </el-radio-group>
      </div>
      <el-table :data="filteredTasks" v-loading="taskLoading" size="mini" :show-header="true" empty-text="暂无数据">
        <el-table-column label="类型" width="90">
          <template slot-scope="s">
            <el-tag :type="typeTag(s.row.type)" size="mini">{{ typeLabel(s.row.type) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="标题 / 摘要" prop="title" show-overflow-tooltip />
        <el-table-column label="状态" width="110" align="center">
          <template slot-scope="s">
            <span>{{ s.row.statusLabel || '-' }}</span>
          </template>
        </el-table-column>
        <el-table-column label="时间" prop="time" width="170" align="center" />
        <el-table-column label="操作" width="90" align="center">
          <template slot-scope="s">
            <el-button type="text" size="mini" @click="jumpTo(s.row)">打开</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>
  </div>
</template>

<script>
import { mapGetters } from 'vuex'
import { listRecord } from '@/api/medical/emr/record'
import { listXray } from '@/api/medical/imaging/xray'
import { getUserProfile } from '@/api/system/user'
import { countReportByStatus, recentReports, REPORT_STATUS, REPORT_STATUS_LABEL } from '@/api/physician/report'

export default {
  name: 'PhysicianWorkbench',
  data() {
    return {
      stats: [
        { key: 'emr', title: '待处理电子病历', value: '-', icon: 'el-icon-document', loading: true, link: '/emr/record' },
        { key: 'image', title: '待分析影像', value: '-', icon: 'el-icon-picture-outline', loading: true, link: '/imaging/xray' },
        { key: 'report', title: '未审核诊断报告', value: '-', icon: 'el-icon-s-order', loading: true, link: '/physician/report' }
      ],
      quickEntries: [
        { title: '病历 AI 诊断', desc: '智能抽取病历关键信息', icon: 'el-icon-edit-outline', bg: '#e8f0ff', path: '/physician/emr-ai' },
        { title: '影像病灶检测', desc: '一键检测可疑病灶区域', icon: 'el-icon-view', bg: '#eaf7ef', path: '/physician/image-detect' },
        { title: '病灶精准诊断', desc: '对病灶进行精细分析', icon: 'el-icon-aim', bg: '#fff2e6', path: '/physician/image-segment' },
        { title: '辅助诊断报告', desc: '汇总生成标准化报告', icon: 'el-icon-document-copy', bg: '#f0eafd', path: '/physician/report' }
      ],
      taskTab: 'all',
      taskLoading: false,
      tasks: [],
      profile: null
    }
  },
  computed: {
    ...mapGetters(['nickName', 'name', 'id']),
    userName() { return this.name },
    userId() { return this.id },
    deptName() {
      return (this.profile && this.profile.dept && this.profile.dept.deptName) || '-'
    },
    today() {
      const d = new Date()
      const pad = n => (n < 10 ? '0' + n : '' + n)
      const week = ['星期日','星期一','星期二','星期三','星期四','星期五','星期六']
      return `${d.getFullYear()}-${pad(d.getMonth()+1)}-${pad(d.getDate())} ${week[d.getDay()]}`
    },
    filteredTasks() {
      if (this.taskTab === 'all') return this.tasks
      return this.tasks.filter(t => t.type === this.taskTab)
    }
  },
  created() {
    this.loadStats()
    this.loadRecentTasks()
    this.loadProfile()
  },
  methods: {
    async loadProfile() {
      try {
        const res = await getUserProfile()
        this.profile = res.data || null
      } catch (e) { this.profile = null }
    },
    async loadStats() {
      try {
        const res = await listRecord({ pageNum: 1, pageSize: 1, archiveStatus: '0' })
        this.stats[0].value = res.total != null ? res.total : (res.rows ? res.rows.length : 0)
      } catch (e) { this.stats[0].value = 0 }
      finally { this.$set(this.stats[0], 'loading', false) }
      try {
        const res = await listXray({ pageNum: 1, pageSize: 1 })
        this.stats[1].value = res.total != null ? res.total : (res.rows ? res.rows.length : 0)
      } catch (e) { this.stats[1].value = 0 }
      finally { this.$set(this.stats[1], 'loading', false) }
      try {
        this.stats[2].value = countReportByStatus(REPORT_STATUS.DRAFT)
      } catch (e) { this.stats[2].value = 0 }
      finally { this.$set(this.stats[2], 'loading', false) }
    },
    async loadRecentTasks() {
      this.taskLoading = true
      const since = new Date(Date.now() - 7 * 24 * 3600 * 1000)
      const iso = this.fmtDate(since)
      const tasks = []
      try {
        const res = await listRecord({ pageNum: 1, pageSize: 10, beginTime: iso })
        const rows = res.rows || []
        rows.forEach(r => tasks.push({
          type: 'emr',
          title: r.title || `病历 #${r.id}`,
          statusLabel: r.archiveStatus === '1' ? '已归档' : '未归档',
          time: r.updateTime || r.createTime,
          _raw: r
        }))
      } catch (e) {}
      try {
        const res = await listXray({ pageNum: 1, pageSize: 10 })
        const rows = res.rows || []
        rows.forEach(r => tasks.push({
          type: 'image',
          title: r.disease ? `${r.disease} - ${r.imagePath || ''}` : (r.imagePath || `影像 #${r.id}`),
          statusLabel: r.annotatedAt ? '已标注' : '待分析',
          time: r.updateTime || r.createTime,
          _raw: r
        }))
      } catch (e) {}
      const reports = recentReports(7, 10)
      reports.forEach(r => tasks.push({
        type: 'report',
        title: `${r.patientName || '未命名'} - ${r.conclusion ? r.conclusion.substring(0, 40) : '辅助诊断报告'}`,
        statusLabel: REPORT_STATUS_LABEL[r.status] || '-',
        time: r.updateTime,
        _raw: r
      }))
      tasks.sort((a, b) => (b.time || '').localeCompare(a.time || ''))
      this.tasks = tasks.slice(0, 15)
      this.taskLoading = false
    },
    typeLabel(t) { return { emr: '病历', image: '影像', report: '报告' }[t] || t },
    typeTag(t) { return { emr: 'primary', image: 'success', report: 'warning' }[t] || 'info' },
    jumpTo(row) {
      if (row.type === 'emr') this.$router.push('/emr/record')
      else if (row.type === 'image') this.$router.push('/imaging/xray')
      else if (row.type === 'report') this.$router.push('/physician/report/edit/' + row._raw.id)
    },
    fmtDate(d) {
      const pad = n => (n < 10 ? '0' + n : '' + n)
      return `${d.getFullYear()}-${pad(d.getMonth()+1)}-${pad(d.getDate())} 00:00:00`
    }
  }
}
</script>

<style lang="scss" scoped>
.physician-workbench { background: #f5f7fa; min-height: calc(100vh - 120px); }

.pw-card {
  border: 1px solid #e8ecf2;
  border-radius: 6px;
  margin-bottom: 16px;
}

.pw-card__header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  font-weight: 600;
  color: #333;
}

.pw-hero {
  display: flex;
  align-items: center;
  justify-content: space-between;

  ::v-deep .el-card__body {
    padding: 20px 24px;
    display: flex;
    align-items: center;
    justify-content: space-between;
    width: 100%;
  }

  &__left { display: flex; flex-direction: column; gap: 8px; }
  &__greeting { font-size: 18px; font-weight: 600; color: #333; }
  &__meta {
    display: flex;
    gap: 20px;
    color: #666;
    font-size: 13px;
    flex-wrap: wrap;
    i { color: #165dff; margin-right: 4px; }
  }
  &__online { color: #18a058; }
  &__online i { color: #18a058; }
  &__right { color: #666; font-size: 13px; }
}

.pw-stats { margin-bottom: 0; }

.pw-stat {
  margin-bottom: 16px;

  ::v-deep .el-card__body { padding: 20px 22px; }

  &__row { display: flex; align-items: center; justify-content: space-between; }
  &__title { color: #666; font-size: 14px; }
  &__icon { font-size: 26px; color: #165dff; opacity: 0.85; }
  &__value { margin: 10px 0; font-size: 28px; font-weight: 600; color: #333; line-height: 1.2; min-height: 34px; }
  &__foot { font-size: 13px; }

  &--image .pw-stat__icon { color: #18a058; }
  &--report .pw-stat__icon { color: #f39c12; }
}

.pw-quick {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 14px 16px;
  border: 1px solid #eef1f6;
  border-radius: 6px;
  cursor: pointer;
  background: #fff;
  margin-bottom: 12px;
  transition: all 0.2s;

  &:hover {
    border-color: #165dff;
    box-shadow: 0 3px 12px rgba(22, 93, 255, 0.08);
    transform: translateY(-1px);
  }

  &__icon {
    width: 42px;
    height: 42px;
    border-radius: 6px;
    display: flex;
    align-items: center;
    justify-content: center;
    font-size: 20px;
    color: #165dff;
    flex-shrink: 0;
  }
  &__title { font-weight: 600; color: #333; font-size: 14px; }
  &__desc { color: #999; font-size: 12px; margin-top: 4px; }
}
</style>
