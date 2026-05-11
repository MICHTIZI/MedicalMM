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
