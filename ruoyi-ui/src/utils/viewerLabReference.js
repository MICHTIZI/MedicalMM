/**
 * Viewer lab fields vs adult reference ranges (demo defaults, not personalized clinical advice).
 * abnormal: 'none' | 'mild' | 'severe'
 */
export const LAB_FIELD_DEFS = [
  { key: 'temperature', label: '体温', unit: '℃', low: 36.0, high: 37.3 },
  { key: 'heartRate', label: '心率', unit: '次/分', low: 60, high: 100 },
  { key: 'respiratoryRate', label: '呼吸频率', unit: '次/分', low: 12, high: 20 },
  { key: 'systolicBp', label: '收缩压', unit: 'mmHg', low: 90, high: 139 },
  { key: 'diastolicBp', label: '舒张压', unit: 'mmHg', low: 60, high: 89 },
  { key: 'spo2', label: '血氧 SpO2', unit: '%', low: 95, high: 100 },
  { key: 'wbc', label: '白细胞 WBC', unit: 'x10^9/L', low: 3.5, high: 9.5 },
  { key: 'neutrophilRatio', label: '中性粒细胞比', unit: '%', low: 40, high: 75 },
  { key: 'lymphocyteRatio', label: '淋巴细胞比', unit: '%', low: 20, high: 40 },
  { key: 'monocyteRatio', label: '单核细胞比', unit: '%', low: 3, high: 8 },
  { key: 'platelet', label: '血小板', unit: 'x10^9/L', low: 100, high: 300 },
  { key: 'crp', label: 'CRP', unit: 'mg/L', low: 0, high: 10 },
  { key: 'pct', label: '降钙素原 PCT', unit: 'ng/mL', low: 0, high: 0.5 },
  { key: 'esr', label: '血沉 ESR', unit: 'mm/h', low: 0, high: 20 },
  { key: 'ph', label: '血气 pH', unit: '', low: 7.35, high: 7.45 },
  { key: 'po2', label: 'PaO2', unit: 'mmHg', low: 80, high: 100 },
  { key: 'pco2', label: 'PaCO2', unit: 'mmHg', low: 35, high: 45 },
  { key: 'hco3', label: 'HCO3-', unit: 'mmol/L', low: 22, high: 26 }
]

export function classifyLabValue(num, low, high) {
  if (num == null || Number.isNaN(num)) return 'none'
  const n = Number(num)
  if (n >= low && n <= high) return 'none'
  const span = high - low || 1
  const mildLow = low - span * 0.15
  const mildHigh = high + span * 0.15
  if (n >= mildLow && n <= mildHigh) return 'mild'
  return 'severe'
}

export function buildLabCompareRows(labRow) {
  if (!labRow) return []
  return LAB_FIELD_DEFS.map(def => {
    const raw = labRow[def.key]
    if (raw === null || raw === undefined || raw === '') return null
    const num = Number(raw)
    if (Number.isNaN(num)) return null
    const level = classifyLabValue(num, def.low, def.high)
    return {
      ...def,
      value: num,
      refText: `${def.low} ~ ${def.high} ${def.unit}`,
      level
    }
  }).filter(Boolean)
}

const FUSION_VITAL_KEYS = ['temperature', 'heartRate', 'respiratoryRate', 'systolicBp', 'diastolicBp', 'spo2']
const FUSION_VITAL_LABELS = {
  temperature: '体温',
  heartRate: '心率',
  respiratoryRate: '呼吸频率',
  systolicBp: '收缩压',
  diastolicBp: '舒张压',
  spo2: '血氧饱和度'
}
const FUSION_CBC_KEYS = ['wbc', 'neutrophilRatio', 'lymphocyteRatio', 'monocyteRatio', 'platelet']
const FUSION_CBC_LABELS = {
  wbc: '白细胞计数',
  neutrophilRatio: '中性粒细胞比例',
  lymphocyteRatio: '淋巴细胞比例',
  monocyteRatio: '单核细胞比例',
  platelet: '血小板计数'
}
const FUSION_INFLAM_KEYS = ['crp', 'pct', 'esr']
const FUSION_INFLAM_LABELS = {
  crp: 'C反应蛋白(CRP)',
  pct: '降钙素原(PCT)',
  esr: '红细胞沉降率(ESR)'
}
const FUSION_BLOODGAS_KEYS = ['ph', 'po2', 'pco2', 'hco3']
const FUSION_BLOODGAS_LABELS = {
  ph: '血液酸碱度(pH)',
  po2: '动脉血氧分压(PO2)',
  pco2: '动脉血二氧化碳分压(PCO2)',
  hco3: '碳酸氢根(HCO3-)'
}

function fusionLineFromRow(r) {
  const u = r.unit || ''
  return `${r.value}${u} (正常: ${r.low}-${r.high}${u})`
}

export function buildFusionLabPayload(labRow) {
  const rows = buildLabCompareRows(labRow)
  if (!rows.length) return {}
  const byKey = {}
  rows.forEach(rr => { byKey[rr.key] = rr })
  const out = {}
  const fill = (title, keys, labels) => {
    const obj = {}
    keys.forEach(k => {
      const r = byKey[k]
      if (!r) return
      obj[labels[k] || k] = fusionLineFromRow(r)
    })
    if (Object.keys(obj).length) out[title] = obj
  }
  fill('生命体征', FUSION_VITAL_KEYS, FUSION_VITAL_LABELS)
  fill('血常规', FUSION_CBC_KEYS, FUSION_CBC_LABELS)
  fill('炎症标志物', FUSION_INFLAM_KEYS, FUSION_INFLAM_LABELS)
  fill('血气分析', FUSION_BLOODGAS_KEYS, FUSION_BLOODGAS_LABELS)
  return out
}
