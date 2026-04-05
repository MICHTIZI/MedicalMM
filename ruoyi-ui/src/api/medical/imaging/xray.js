/**
 * 医学影像 - 胸部X光接口（UTF-8）
 */
import request from '@/utils/request'
import { getToken } from '@/utils/auth'

export function listXray(query) {
  return request({ url: '/imaging/xray/list', method: 'get', params: query })
}

export function getXray(id) {
  return request({ url: '/imaging/xray/' + id, method: 'get' })
}

export function addXray(data) {
  return request({ url: '/imaging/xray', method: 'post', data })
}

export function updateXray(data) {
  return request({ url: '/imaging/xray', method: 'put', data })
}

export function delXray(ids) {
  return request({ url: '/imaging/xray/' + ids, method: 'delete' })
}

export function annotateXray(id, data) {
  return request({ url: '/imaging/xray/annotate/' + id, method: 'put', data })
}

export function getDiseaseLabels() {
  return request({ url: '/imaging/xray/diseaseLabels', method: 'get' })
}

/**
 * 从 imagePath 提取纯文件名，构建图片代理 URL。
 * 后端代理会自动在 MinIO 的 chest_224_hd/ 目录下查找该文件。
 * 兼容各种旧格式：完整 URL、Windows/Linux 路径、纯文件名。
 */
export function imageUrl(imagePath) {
  if (!imagePath) return ''
  let filename = imagePath
  // 去掉路径前缀，只保留文件名
  const slashIdx = Math.max(filename.lastIndexOf('/'), filename.lastIndexOf('\\'))
  if (slashIdx >= 0) {
    filename = filename.substring(slashIdx + 1)
  }
  const base = process.env.VUE_APP_BASE_API + '/imaging/xray/image/' + encodeURIComponent(filename)
  const token = getToken()
  return token ? base + '?token=' + encodeURIComponent(token) : base
}
