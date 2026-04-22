/**
 * 辅助诊断报告 —— 当前采用浏览器本地存储（localStorage）进行草稿持久化。
 * 若后端提供 /emr/diagnosis-report 接口，可将此处实现替换为真实 request。
 * 数据结构：
 * {
 *   id: string,               // 报告 ID（毫秒时间戳）
 *   patientName: string,      // 患者姓名
 *   patientInfo: string,      // 年龄/性别/科室等
 *   recordId: string|number,  // 关联病历 ID（可选）
 *   recordSummary: string,    // 病历摘要
 *   aiTextResult: object,     // AI 文本抽取结构化结果
 *   imagePath: string,        // 关联 MinIO 影像路径
 *   aiDetection: array,       // 影像检测结果
 *   aiSegment: object,        // 影像精准分析结果
 *   conclusion: string,       // AI 诊断结论
 *   suggestion: string,       // 诊疗建议
 *   doctorOpinion: string,    // 医生个人意见
 *   doctorSignature: string,  // 签名
 *   status: '0'|'1'|'2',      // 草稿 / 已审核 / 已归档
 *   createTime: string,
 *   updateTime: string
 * }
 */

const STORAGE_KEY = 'medical:diagnosis-reports'

export const REPORT_STATUS = {
  DRAFT: '0',
  REVIEWED: '1',
  ARCHIVED: '2'
}

export const REPORT_STATUS_LABEL = {
  '0': '草稿',
  '1': '已审核',
  '2': '已归档'
}

function readAll() {
  try {
    const raw = localStorage.getItem(STORAGE_KEY)
    if (!raw) return []
    const arr = JSON.parse(raw)
    return Array.isArray(arr) ? arr : []
  } catch (e) {
    return []
  }
}

function writeAll(list) {
  localStorage.setItem(STORAGE_KEY, JSON.stringify(list || []))
}

function nowStr() {
  const d = new Date()
  const pad = n => (n < 10 ? '0' + n : '' + n)
  return `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())} ${pad(d.getHours())}:${pad(d.getMinutes())}:${pad(d.getSeconds())}`
}

export function listReport(query = {}) {
  const all = readAll()
  const { patientName, status, startDate, endDate, pageNum = 1, pageSize = 10 } = query
  let rows = all
  if (patientName) {
    rows = rows.filter(r => (r.patientName || '').includes(patientName))
  }
  if (status !== undefined && status !== null && status !== '') {
    rows = rows.filter(r => r.status === status)
  }
  if (startDate) {
    rows = rows.filter(r => (r.createTime || '') >= startDate)
  }
  if (endDate) {
    rows = rows.filter(r => (r.createTime || '') <= endDate + ' 23:59:59')
  }
  rows.sort((a, b) => (b.updateTime || '').localeCompare(a.updateTime || ''))
  const total = rows.length
  const start = (pageNum - 1) * pageSize
  const pageRows = rows.slice(start, start + pageSize)
  return Promise.resolve({ code: 200, msg: 'success', total, rows: pageRows })
}

export function getReport(id) {
  const item = readAll().find(r => r.id === id)
  return Promise.resolve({ code: 200, data: item || null })
}

export function saveReport(report) {
  const all = readAll()
  if (report.id) {
    const idx = all.findIndex(r => r.id === report.id)
    if (idx >= 0) {
      all[idx] = { ...all[idx], ...report, updateTime: nowStr() }
      writeAll(all)
      return Promise.resolve({ code: 200, data: all[idx] })
    }
  }
  const newOne = {
    ...report,
    id: report.id || String(Date.now()),
    status: report.status || REPORT_STATUS.DRAFT,
    createTime: report.createTime || nowStr(),
    updateTime: nowStr()
  }
  all.push(newOne)
  writeAll(all)
  return Promise.resolve({ code: 200, data: newOne })
}

export function delReport(id) {
  const all = readAll().filter(r => r.id !== id)
  writeAll(all)
  return Promise.resolve({ code: 200 })
}

export function updateReportStatus(id, status) {
  const all = readAll()
  const idx = all.findIndex(r => r.id === id)
  if (idx >= 0) {
    all[idx].status = status
    all[idx].updateTime = nowStr()
    writeAll(all)
    return Promise.resolve({ code: 200, data: all[idx] })
  }
  return Promise.reject(new Error('报告不存在'))
}

export function countReportByStatus(status) {
  return readAll().filter(r => r.status === status).length
}

export function recentReports(days = 7, limit = 10) {
  const all = readAll()
  const now = Date.now()
  const ms = days * 24 * 3600 * 1000
  return all
    .filter(r => {
      const t = r.updateTime ? new Date(r.updateTime.replace(/-/g, '/')).getTime() : 0
      return now - t <= ms
    })
    .sort((a, b) => (b.updateTime || '').localeCompare(a.updateTime || ''))
    .slice(0, limit)
}
