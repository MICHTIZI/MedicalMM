import request from '@/utils/request'

export function listPatient(query) {
  return request({
    url: '/emr/patient/list',
    method: 'get',
    params: query
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
