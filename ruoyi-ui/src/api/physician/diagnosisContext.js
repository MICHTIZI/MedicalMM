/**
 * 医生端 AI 诊断联动数据（内存 + sessionStorage）
 * 用于在「电子病历 AI 诊断」「影像病灶检测」「精准诊断」「报告」等页面间传递上下文。
 */

const KEY = 'medical:diagnosis-context'

function emptyCtx() {
  return {
    emr: {
      recordId: null,
      title: '',
      rawText: '',
      aiResult: null // { summary, clinical_impression, differential, suggested_actions, symptoms, medications, history }
    },
    image: {
      path: '',
      detection: null, // 病灶检测返回数组
      segment: null // 精准分析返回
    },
    patient: {
      name: '',
      info: ''
    }
  }
}

export function getContext() {
  try {
    const raw = sessionStorage.getItem(KEY)
    if (!raw) return emptyCtx()
    const parsed = JSON.parse(raw)
    return { ...emptyCtx(), ...parsed, emr: { ...emptyCtx().emr, ...(parsed.emr || {}) }, image: { ...emptyCtx().image, ...(parsed.image || {}) }, patient: { ...emptyCtx().patient, ...(parsed.patient || {}) } }
  } catch (e) {
    return emptyCtx()
  }
}

export function saveContext(ctx) {
  sessionStorage.setItem(KEY, JSON.stringify(ctx || emptyCtx()))
}

export function patchContext(patch) {
  const cur = getContext()
  const next = {
    ...cur,
    ...patch,
    emr: { ...cur.emr, ...(patch.emr || {}) },
    image: { ...cur.image, ...(patch.image || {}) },
    patient: { ...cur.patient, ...(patch.patient || {}) }
  }
  saveContext(next)
  return next
}

export function clearContext() {
  sessionStorage.removeItem(KEY)
}
