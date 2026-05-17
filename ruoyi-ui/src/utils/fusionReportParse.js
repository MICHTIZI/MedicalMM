/**
 * Parse fusion analyze API envelope / stored fusion_response_json (v2 nested data).
 */

const DASH = '\u2014'

export function parseJsonMaybe(raw) {
  if (raw == null || raw === '') return null
  if (typeof raw === 'object') return raw
  try {
    return JSON.parse(raw)
  } catch (e) {
    return null
  }
}

function hasFusionFields(obj) {
  if (!obj || typeof obj !== 'object') return false
  return !!(obj.modal_score || obj.modalScore || obj.diagnosis_result || obj.diagnosisResult
    || obj.modality_scores || obj.modalityScores || obj.diagnosis_output || obj.diagnosisOutput)
}

/** @returns inner payload: modal_score, diagnosis_result, ... */
export function unwrapFusionPayload(envelope) {
  const root = parseJsonMaybe(envelope)
  if (!root || typeof root !== 'object') return null
  if (hasFusionFields(root)) return root
  if (root.data != null && typeof root.data === 'object') {
    const outer = root.data
    if (hasFusionFields(outer)) return outer
    if (outer.data != null && typeof outer.data === 'object' && hasFusionFields(outer.data)) {
      return outer.data
    }
    if (hasFusionFields(outer)) return outer
    return outer.data != null && typeof outer.data === 'object' ? outer.data : outer
  }
  return null
}

export function pick(obj, snakeKey, camelKey) {
  if (!obj) return ''
  const v = obj[snakeKey] != null ? obj[snakeKey] : obj[camelKey]
  return v != null && v !== '' ? v : ''
}

export function pickDash(obj, snakeKey, camelKey) {
  const v = pick(obj, snakeKey, camelKey)
  return v !== '' ? v : DASH
}

export function flattenAdviceBlock(block) {
  if (block == null) return []
  if (typeof block === 'string') {
    return [{ label: '', text: block }]
  }
  if (Array.isArray(block)) {
    return block.map(t => ({ label: '', text: String(t) }))
  }
  if (typeof block === 'object') {
    return Object.keys(block).map(k => ({
      label: k,
      text: typeof block[k] === 'string' ? block[k] : JSON.stringify(block[k], null, 2)
    }))
  }
  return [{ label: '', text: String(block) }]
}

export function buildFusionAdviceSections(payload) {
  const adv = payload && (payload.fusion_standard_advice || payload.fusionStandardAdvice)
  if (!adv || typeof adv !== 'object') return []
  return Object.keys(adv).map(title => ({
    title,
    items: flattenAdviceBlock(adv[title])
  }))
}

export function legacyModalityScores(payload) {
  if (!payload) return null
  return payload.modality_scores || payload.modalityScores || null
}

export function legacyDiagnosisOutput(payload) {
  if (!payload) return null
  return payload.diagnosis_output || payload.diagnosisOutput || null
}

export function legacyStructuredSuggestions(payload) {
  if (!payload) return []
  const s = payload.structured_suggestions || payload.structuredSuggestions
  return Array.isArray(s) ? s : []
}

export function legacyConsistencyCheck(payload) {
  if (!payload) return null
  return payload.consistency_check || payload.consistencyCheck || null
}

export function extractFusionListSummary(fusionResponseJson) {
  const payload = unwrapFusionPayload(fusionResponseJson)
  if (!payload) {
    return {
      confirmGrade: DASH,
      severityGrade: DASH,
      pathogenInference: DASH,
      fusionTotalScore: DASH
    }
  }
  const dr = payload.diagnosis_result || payload.diagnosisResult || {}
  const ms = payload.modal_score || payload.modalScore || {}
  let fusionTotalScore = pickDash(ms, 'fusion_total_score', 'fusionTotalScore')
  if (fusionTotalScore === DASH && payload.fusion_calculation) {
    const fc = payload.fusion_calculation || payload.fusionCalculation
    const v = fc && (fc.final_score != null ? fc.final_score : fc.finalScore)
    if (v != null && v !== '') fusionTotalScore = String(v)
  }
  let confirmGrade = pickDash(dr, 'confirm_grade', 'confirmGrade')
  let severityGrade = pickDash(dr, 'severity_grade', 'severityGrade')
  const legacy = legacyDiagnosisOutput(payload)
  if (confirmGrade === DASH && legacy) {
    confirmGrade = legacy.grade_cn || legacy.gradeCn || legacy.grade || DASH
  }
  if (severityGrade === DASH && legacy) {
    severityGrade = legacy.severity_cn || legacy.severityCn || legacy.severity || DASH
  }
  return {
    confirmGrade,
    severityGrade,
    pathogenInference: pickDash(dr, 'pathogen_inference', 'pathogenInference'),
    fusionTotalScore
  }
}

export function warningLevelTag(level) {
  const t = String(level || '')
  if (t.indexOf('\u9ad8') >= 0) return 'danger'
  if (t.indexOf('\u4e2d') >= 0) return 'warning'
  return 'info'
}

export function conflictWarningsFromPayload(payload) {
  if (!payload) return []
  const list = payload.conflict_warning_list || payload.conflictWarningList
  return Array.isArray(list) ? list : []
}

export function imageResultSummary(imageResultJson) {
  const ir = parseJsonMaybe(imageResultJson)
  if (!ir) return null
  return {
    lesionCount: ir.lesion_count != null ? ir.lesion_count : ir.lesionCount,
    infectionRate: ir.infection_rate != null ? ir.infection_rate : ir.infectionRate,
    diagnosis: ir.diagnosis || '',
    hasReport: !!(ir.diagnosis_report || ir.diagnosisReport)
  }
}
