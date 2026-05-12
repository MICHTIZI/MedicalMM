import request from '@/utils/request'

export function listPatient(query) {
  return request({
    url: '/emr/patient/list',
    method: 'get',
    params: query
  })
}

/** 工作台卡片分页（姓名筛选 + 诊断状态聚合） */
export function listPatientCards(query) {
  return request({
    url: '/emr/patient/cardList',
    method: 'get',
    params: query
  })
}

export function getPatientDiagnosisDetail(patientId) {
  return request({
    url: '/emr/patient/diagnosisDetail/' + patientId,
    method: 'get'
  })
}

export function getPatient(patientId) {
  return request({
    url: '/emr/patient/' + patientId,
    method: 'get'
  })
}

export function addPatient(data) {
  return request({
    url: '/emr/patient',
    method: 'post',
    data: data
  })
}

export function updatePatient(data) {
  return request({
    url: '/emr/patient',
    method: 'put',
    data: data
  })
}

export function delPatient(patientId) {
  return request({
    url: '/emr/patient/' + patientId,
    method: 'delete'
  })
}

export function listDoctorOptions() {
  return request({
    url: '/emr/patient/doctorOptions',
    method: 'get'
  })
}

export function archivePatient(patientId, data) {
  return request({
    url: '/emr/patient/archive/' + patientId,
    method: 'post',
    data: data || {}
  })
}

export function unarchivePatient(patientId) {
  return request({
    url: '/emr/patient/unarchive/' + patientId,
    method: 'post'
  })
}
