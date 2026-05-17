import request from '@/utils/request'
import { getToken } from '@/utils/auth'

export function listAiImage(query) {
  return request({
    url: '/emr/aiImage/list',
    method: 'get',
    params: query
  })
}

export function analyzeAiImage(imageId) {
  return request({
    url: '/emr/aiImage/' + imageId + '/analyze',
    method: 'post',
    timeout: 180000,
    headers: { repeatSubmit: false }
  })
}

/** 将用户合成的标注图写入 MinIO，覆盖 result/ 下与 AI 相同路径 */
export function uploadAiImageUserOverlay(imageId, fileBlob, filename) {
  const formData = new FormData()
  formData.append('file', fileBlob, filename || 'overlay.jpg')
  return request({
    url: '/emr/aiImage/' + imageId + '/userOverlay',
    method: 'post',
    data: formData,
    timeout: 120000,
    headers: { repeatSubmit: false }
  })
}

export function getAiImageRecord(imageId) {
  return request({
    url: '/emr/aiImage/' + imageId + '/record',
    method: 'get'
  })
}

export function exportFusionReport(imageId, body) {
  return request({
    url: '/emr/aiImage/' + imageId + '/exportFusionReport',
    method: 'post',
    data: body,
    responseType: 'blob',
    timeout: 180000,
    headers: { repeatSubmit: false }
  })
}

export function fusionAnalyze(imageId, body) {
  return request({
    url: '/emr/aiImage/' + imageId + '/fusionAnalyze',
    method: 'post',
    data: body,
    timeout: 120000,
    headers: { repeatSubmit: false }
  })
}

export function aiImageUrl(path) {
  if (!path) return ''
  const normalized = normalizeMinioPath(path)
  const encoded = normalized.split('/').map(item => encodeURIComponent(item)).join('/')
  const base = process.env.VUE_APP_BASE_API + '/emr/aiImage/image/' + encoded
  const token = getToken()
  return token ? base + '?token=' + encodeURIComponent(token) : base
}

export function normalizeMinioPath(path) {
  let value = String(path || '').replace(/\\/g, '/')
  value = value.replace(/^https?:\/\/[^/]+\//, '')
  value = value.replace(/^\/+/, '')
  value = value.replace(/^medical-imaging\//, '')
  return value
}

export function aiResultPathFromImage(imagePath) {
  const normalized = normalizeMinioPath(imagePath)
  const filename = normalized.substring(normalized.lastIndexOf('/') + 1)
  return filename ? 'result/' + filename : ''
}
