/**
 * 辅助诊断报告 —— 简易 PDF 导出
 * 利用浏览器打印能力（window.print），无需额外依赖：
 * 1. 打开新窗口写入 HTML
 * 2. 调用 window.print()，用户选择「另存为 PDF」即可
 * 可后续替换为 html2pdf.js / jsPDF 等方案。
 */
import { REPORT_STATUS_LABEL } from '@/api/physician/report'

function escapeHtml(str) {
  if (str == null) return ''
  return String(str)
    .replace(/&/g, '&amp;')
    .replace(/</g, '&lt;')
    .replace(/>/g, '&gt;')
    .replace(/"/g, '&quot;')
    .replace(/'/g, '&#39;')
}

function listToHtml(arr) {
  if (!arr || !arr.length) return '<p class="muted">—</p>'
  return '<ul>' + arr.map(x => '<li>' + escapeHtml(x) + '</li>').join('') + '</ul>'
}

export function exportReportPdf(report) {
  if (!report) return
  const ai = report.aiTextResult || {}
  const seg = (report.aiSegment && (report.aiSegment.detections || report.aiSegment.data)) || []
  const det = Array.isArray(report.aiDetection) ? report.aiDetection : []

  const html = `<!DOCTYPE html>
<html lang="zh-CN">
<head>
<meta charset="utf-8">
<title>辅助诊断报告 ${escapeHtml(report.patientName || '')}</title>
<style>
  * { box-sizing: border-box; }
  body { font-family: 'Microsoft YaHei', 'PingFang SC', sans-serif; color: #333; padding: 32px 40px; }
  h1 { text-align: center; font-size: 22px; margin: 0 0 6px; }
  .sub { text-align: center; color: #666; font-size: 12px; margin-bottom: 24px; }
  h2 { font-size: 15px; border-left: 3px solid #165dff; padding-left: 8px; margin: 18px 0 8px; color: #165dff; }
  .kv { display: grid; grid-template-columns: 120px 1fr; gap: 6px 12px; font-size: 13px; margin-bottom: 6px; }
  .kv .k { color: #666; }
  p { margin: 4px 0; font-size: 13px; line-height: 1.7; }
  ul { margin: 4px 0 0 20px; font-size: 13px; line-height: 1.7; }
  .muted { color: #999; }
  table { width: 100%; border-collapse: collapse; font-size: 12px; margin-top: 6px; }
  th, td { border: 1px solid #dcdfe6; padding: 4px 6px; text-align: left; }
  th { background: #f5f7fa; color: #333; }
  .footer { margin-top: 28px; display: flex; justify-content: space-between; font-size: 13px; color: #666; }
  .sign { border-top: 1px solid #dcdfe6; padding-top: 8px; min-width: 200px; }
  @media print { body { padding: 16px 20px; } }
</style>
</head>
<body>
  <h1>医疗辅助诊断报告</h1>
  <div class="sub">报告编号：${escapeHtml(report.id || '')} · 状态：${escapeHtml(REPORT_STATUS_LABEL[report.status] || '-')}</div>

  <h2>患者基本信息</h2>
  <div class="kv">
    <div class="k">姓名</div><div>${escapeHtml(report.patientName || '-')}</div>
    <div class="k">信息</div><div>${escapeHtml(report.patientInfo || '-')}</div>
    <div class="k">关联病历</div><div>${escapeHtml(report.recordId || '-')}</div>
    <div class="k">创建时间</div><div>${escapeHtml(report.createTime || '-')}</div>
  </div>

  <h2>病历关键信息摘要</h2>
  <p>${escapeHtml(report.recordSummary || ai.summary || '-')}</p>

  ${ai.clinical_impression ? `<h2>病历 AI 临床印象</h2><p>${escapeHtml(ai.clinical_impression)}</p>` : ''}
  ${ai.differential && ai.differential.length ? `<h2>疑似诊断</h2>${listToHtml(ai.differential)}` : ''}
  ${ai.symptoms ? `<h2>症状信息</h2><p>${escapeHtml(ai.symptoms)}</p>` : ''}
  ${ai.medications ? `<h2>用药信息</h2><p>${escapeHtml(ai.medications)}</p>` : ''}
  ${ai.history ? `<h2>既往病史</h2><p>${escapeHtml(ai.history)}</p>` : ''}
  ${ai.suggested_actions && ai.suggested_actions.length ? `<h2>诊疗建议</h2>${listToHtml(ai.suggested_actions)}` : ''}

  ${report.imagePath ? `<h2>影像信息</h2>
  <div class="kv"><div class="k">MinIO 路径</div><div>${escapeHtml(report.imagePath)}</div></div>
  ${det.length ? `<table><thead><tr><th>#</th><th>病灶</th><th>置信度</th><th>x1,y1</th><th>x2,y2</th></tr></thead><tbody>
    ${det.map((d, i) => `<tr><td>${i + 1}</td><td>${escapeHtml(d.label)}</td><td>${((d.confidence || 0) * 100).toFixed(0)}%</td><td>${d.x1},${d.y1}</td><td>${d.x2},${d.y2}</td></tr>`).join('')}
  </tbody></table>` : ''}
  ${seg.length ? `<h2>精准分析结果</h2><table><thead><tr><th>#</th><th>病灶</th><th>置信度</th><th>位置</th><th>尺寸</th></tr></thead><tbody>
    ${seg.map((d, i) => `<tr><td>${i + 1}</td><td>${escapeHtml(d.label)}</td><td>${((d.confidence || 0) * 100).toFixed(0)}%</td><td>${d.x1},${d.y1} ~ ${d.x2},${d.y2}</td><td>${Math.max(0, d.x2 - d.x1)} × ${Math.max(0, d.y2 - d.y1)}</td></tr>`).join('')}
  </tbody></table>` : ''}
  ` : ''}

  <h2>AI 诊断结论</h2>
  <p>${escapeHtml(report.conclusion || '-')}</p>
  <h2>诊疗建议</h2>
  <p>${escapeHtml(report.suggestion || '-')}</p>

  ${report.doctorOpinion ? `<h2>医生补充意见</h2><p>${escapeHtml(report.doctorOpinion)}</p>` : ''}

  <div class="footer">
    <div class="sign">医生签名：${escapeHtml(report.doctorSignature || '')}</div>
    <div class="sign">报告日期：${escapeHtml(report.updateTime || report.createTime || '')}</div>
  </div>

  <div style="margin-top:24px;font-size:11px;color:#999;">本报告由 AI 辅助生成，仅供临床参考，不能替代执业医师的独立判断与诊疗决策。</div>
</body>
</html>`

  const win = window.open('', '_blank', 'width=900,height=700')
  if (!win) return
  win.document.open()
  win.document.write(html)
  win.document.close()
  win.focus()
  setTimeout(() => {
    try { win.print() } catch (e) {}
  }, 400)
}
