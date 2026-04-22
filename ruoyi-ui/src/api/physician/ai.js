/**
 * 医生端 AI 推理接口
 * 后端 AI 服务默认地址：http://127.0.0.1:5000
 * 开发环境通过 vue.config.js 的 /ai-api 代理访问；生产环境需要在 nginx 处配置同名代理。
 */
import axios from 'axios'
import { Message } from 'element-ui'

const AI_BASE = '/ai-api'

const aiRequest = axios.create({
  baseURL: AI_BASE,
  timeout: 60000,
  headers: { 'Content-Type': 'application/json;charset=utf-8' }
})

aiRequest.interceptors.response.use(
  response => {
    const data = response.data
    if (data && typeof data === 'object' && 'code' in data && data.code !== 200) {
      Message.error(data.msg || 'AI 服务调用失败')
      return Promise.reject(new Error(data.msg || 'AI 服务调用失败'))
    }
    return data
  },
  error => {
    Message.error(error.message || 'AI 服务连接失败')
    return Promise.reject(error)
  }
)

/** 电子病历文本抽取（AI 接口 1） */
export function aiExtractMedicalText(medicalRecord) {
  return aiRequest.post('/ai/text/extract', { medical_record: medicalRecord })
}

/** 影像病灶检测（AI 接口 2） */
export function aiDetectLesion(minioImagePath) {
  return aiRequest.post('/ai/image/detect', { minio_image_path: minioImagePath })
}

/**
 * 影像病灶精准诊断 / 分割（AI 接口 3）
 * 服务端可接收 detect 的结果（含 minio_image_path + detections），也兼容仅传 minio_image_path。
 */
export function aiSegmentLesion(payload) {
  return aiRequest.post('/ai/image/segment', payload)
}

export default {
  aiExtractMedicalText,
  aiDetectLesion,
  aiSegmentLesion
}
