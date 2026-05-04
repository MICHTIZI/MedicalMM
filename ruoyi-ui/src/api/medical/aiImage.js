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
    method: 'post'
  })
}

export function getAiImageRecord(imageId) {
  return request({
    url: '/emr/aiImage/' + imageId + '/record',
    method: 'get'
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
