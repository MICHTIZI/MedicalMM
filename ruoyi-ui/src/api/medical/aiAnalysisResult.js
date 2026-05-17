import request from '@/utils/request'

export function listAiAnalysisResult(query) {
  return request({
    url: '/emr/aiAnalysisResult/list',
    method: 'get',
    params: query
  })
}

export function getAiAnalysisResult(analysisId) {
  return request({
    url: '/emr/aiAnalysisResult/' + analysisId,
    method: 'get'
  })
}

export function delAiAnalysisResult(analysisIds) {
  return request({
    url: '/emr/aiAnalysisResult/' + analysisIds,
    method: 'delete'
  })
}
