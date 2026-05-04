import request from '@/utils/request'

export function listMedicalRecord(query) {
  return request({
    url: '/emr/medicalRecord/list',
    method: 'get',
    params: query
  })
}

export function getMedicalRecord(recordId) {
  return request({
    url: '/emr/medicalRecord/' + recordId,
    method: 'get'
  })
}

export function addMedicalRecord(data) {
  return request({
    url: '/emr/medicalRecord',
    method: 'post',
    data: data
  })
}

export function updateMedicalRecord(data) {
  return request({
    url: '/emr/medicalRecord',
    method: 'put',
    data: data
  })
}

export function delMedicalRecord(recordId) {
  return request({
    url: '/emr/medicalRecord/' + recordId,
    method: 'delete'
  })
}

export function listRecordPatientOptions(query) {
  return request({
    url: '/emr/medicalRecord/patientOptions',
    method: 'get',
    params: query
  })
}

export function listRecordXrayOptions(query) {
  return request({
    url: '/emr/medicalRecord/xrayOptions',
    method: 'get',
    params: query
  })
}
