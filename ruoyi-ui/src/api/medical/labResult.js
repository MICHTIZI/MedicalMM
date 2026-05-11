import request from '@/utils/request'

export function listLab(query) {
  return request({
    url: '/emr/lab/list',
    method: 'get',
    params: query
  })
}

export function getLab(id) {
  return request({
    url: '/emr/lab/' + id,
    method: 'get'
  })
}

export function addLab(data) {
  return request({
    url: '/emr/lab',
    method: 'post',
    data: data
  })
}

export function updateLab(data) {
  return request({
    url: '/emr/lab',
    method: 'put',
    data: data
  })
}

export function delLab(ids) {
  return request({
    url: '/emr/lab/' + ids,
    method: 'delete'
  })
}

export function listLabPatientOptions(query) {
  return request({
    url: '/emr/lab/patientOptions',
    method: 'get',
    params: query
  })
}

export function parseLabTxt(file) {
  const formData = new FormData()
  formData.append('file', file)
  return request({
    url: '/emr/lab/parseTxt',
    method: 'post',
    data: formData
  })
}

export function importLabTxt(patientId, file) {
  const formData = new FormData()
  formData.append('patientId', patientId)
  formData.append('file', file)
  return request({
    url: '/emr/lab/importTxt',
    method: 'post',
    data: formData
  })
}
