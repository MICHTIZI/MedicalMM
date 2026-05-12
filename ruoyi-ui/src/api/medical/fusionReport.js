import request from '@/utils/request'

export function listFusionReport(query) {
  return request({
    url: '/emr/fusionReport/list',
    method: 'get',
    params: query
  })
}

export function getFusionReport(reportId) {
  return request({
    url: '/emr/fusionReport/' + reportId,
    method: 'get'
  })
}

export function getFusionReportByImage(imageId) {
  return request({
    url: '/emr/fusionReport/byImage/' + imageId,
    method: 'get'
  })
}

export function saveFusionAnalyze(imageId, data) {
  return request({
    url: '/emr/fusionReport/saveAnalyze/' + imageId,
    method: 'post',
    data,
    timeout: 120000,
    headers: { repeatSubmit: false }
  })
}

export function updateFusionDoctor(data) {
  return request({
    url: '/emr/fusionReport/doctor',
    method: 'put',
    data
  })
}

export function updateFusionReport(data) {
  return request({
    url: '/emr/fusionReport',
    method: 'put',
    data
  })
}

export function delFusionReport(reportIds) {
  return request({
    url: '/emr/fusionReport/' + reportIds,
    method: 'delete'
  })
}
