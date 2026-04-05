/**
 * 医学影像 - 序列管理接口（UTF-8）
 */
import request from '@/utils/request'

export function listSequence(query) {
  return request({ url: '/imaging/sequence/list', method: 'get', params: query })
}

export function getSequence(id) {
  return request({ url: '/imaging/sequence/' + id, method: 'get' })
}

export function addSequence(data) {
  return request({ url: '/imaging/sequence', method: 'post', data })
}

export function updateSequence(data) {
  return request({ url: '/imaging/sequence', method: 'put', data })
}

export function delSequence(ids) {
  return request({ url: '/imaging/sequence/' + ids, method: 'delete' })
}

export function archiveSequence(id) {
  return request({ url: '/imaging/sequence/archive/' + id, method: 'put' })
}

export function unarchiveSequence(id) {
  return request({ url: '/imaging/sequence/unarchive/' + id, method: 'put' })
}

export function getSequenceImages(id) {
  return request({ url: '/imaging/sequence/' + id + '/images', method: 'get' })
}

export function setSequenceImages(id, imageIds) {
  return request({ url: '/imaging/sequence/' + id + '/images', method: 'put', data: imageIds })
}
