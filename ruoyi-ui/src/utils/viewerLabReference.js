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
  temperature: '\u4f53\u6e29',
  heartRate: '\u5fc3\u7387',
  respiratoryRate: '\u547c\u5438\u9891\u7387',
  systolicBp: '\u6536\u7f29\u538b',
  diastolicBp: '\u8212\u5f20\u538b',
  spo2: '\u8840\u6c27\u9971\u548c\u5ea6'
}
const FUSION_CBC_KEYS = ['wbc', 'neutrophilRatio', 'lymphocyteRatio', 'monocyteRatio', 'platelet']
const FUSION_CBC_LABELS = {
  wbc: '\u767d\u7ec6\u80de\u8ba1\u6570',
  neutrophilRatio: '\u4e2d\u6027\u7c92\u7ec6\u80de\u6bd4\u4f8b',
  lymphocyteRatio: '\u6dcb\u5df4\u7ec6\u80de\u6bd4\u4f8b',
  monocyteRatio: '\u5355\u6838\u7ec6\u80de\u6bd4\u4f8b',
  platelet: '\u8840\u5c0f\u677f\u8ba1\u6570'
}
const FUSION_INFLAM_KEYS = ['crp', 'pct', 'esr']
const FUSION_INFLAM_LABELS = {
  crp: 'C\u53cd\u5e94\u86cb\u767d(CRP)',
  pct: '\u964d\u9499\u7d20\u539f(PCT)',
  esr: '\u7ea2\u7ec6\u80de\u6c89\u964d\u7387(ESR)'
}
const FUSION_BLOODGAS_KEYS = ['ph', 'po2', 'pco2', 'hco3']
const FUSION_BLOODGAS_LABELS = {
  ph: '\u8840\u6db2\u9178\u78b1\u5ea6(pH)',
  po2: '\u52a8\u8109\u8840\u6c27\u5206\u538b(PO2)',
  pco2: '\u52a8\u8109\u8840\u4e8c\u6c27\u5316\u78b3\u5206\u538b(PCO2)',
  hco3: '\u78b3\u9178\u6c22\u6839(HCO3-)'
}

function fusionLineFromRow(r) {
  const u = r.unit || ''
  return `${r.value}${u} (\u6b63\u5e38: ${r.low}-${r.high}${u})`
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
  fill('\u751f\u547d\u4f53\u5f81', FUSION_VITAL_KEYS, FUSION_VITAL_LABELS)
  fill('\u8840\u5e38\u89c4', FUSION_CBC_KEYS, FUSION_CBC_LABELS)
  fill('\u708e\u75c7\u6807\u5fd7\u7269', FUSION_INFLAM_KEYS, FUSION_INFLAM_LABELS)
  fill('\u8840\u6c14\u5206\u6790', FUSION_BLOODGAS_KEYS, FUSION_BLOODGAS_LABELS)
  return out
}
