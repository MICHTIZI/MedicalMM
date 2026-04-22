<template>
  <div class="app-container emr-ai">
    <el-row :gutter="16">
      <!-- 左侧：病历输入 -->
      <el-col :xs="24" :md="10">
        <el-card shadow="never" class="emr-ai__card">
          <div slot="header" class="emr-ai__card-head">
            <span>病历数据</span>
            <el-radio-group v-model="inputMode" size="mini">
              <el-radio-button label="paste">粘贴</el-radio-button>
              <el-radio-button label="select">从病历选择</el-radio-button>
            </el-radio-group>
          </div>

          <div v-show="inputMode === 'paste'">
            <el-alert
              class="mb10"
              type="info"
              :closable="false"
              show-icon
              title="支持纯文本或 JSON 病历数据"
              description='例如: {"patient":"男,55","content":"..."} 或直接粘贴病历正文。'
            />
            <el-input
              v-model="rawInput"
              type="textarea"
              :rows="14"
              placeholder="请粘贴电子病历内容（纯文本或 JSON）"
            />
          </div>

          <div v-show="inputMode === 'select'">
            <el-form :inline="true" size="mini" @submit.native.prevent>
              <el-form-item label="关键词">
                <el-input v-model="recordQuery.keyword" placeholder="标题/内容/疾病" clearable @keyup.enter.native="queryRecord" style="width:200px" />
              </el-form-item>
              <el-form-item>
                <el-button type="primary" icon="el-icon-search" @click="queryRecord">查询</el-button>
              </el-form-item>
            </el-form>
            <el-table
              :data="recordList"
              v-loading="recordLoading"
              size="mini"
              height="320"
              highlight-current-row
              @current-change="onRecordSelect"
              empty-text="请输入关键词查询病历"
            >
              <el-table-column label="ID" prop="id" width="70" />
              <el-table-column label="标题" prop="title" show-overflow-tooltip />
              <el-table-column label="疾病" prop="disease" width="100" show-overflow-tooltip />
              <el-table-column label="更新时间" prop="updateTime" width="140" />
            </el-table>
            <div class="emr-ai__selected" v-if="selectedRecord">
              已选择：<el-tag size="small">#{{ selectedRecord.id }} {{ selectedRecord.title }}</el-tag>
            </div>
          </div>

          <div class="emr-ai__actions">
            <el-button type="primary" icon="el-icon-cpu" :loading="aiLoading" @click="handleExtract">AI 一键诊断</el-button>
            <el-button icon="el-icon-refresh-left" @click="resetForm">重置</el-button>
          </div>
        </el-card>
      </el-col>

      <!-- 右侧：AI 结果展示 -->
      <el-col :xs="24" :md="14">
        <el-card shadow="never" class="emr-ai__card">
          <div slot="header" class="emr-ai__card-head">
            <span>结构化诊断结果</span>
            <div v-if="aiResult">
              <el-button size="mini" icon="el-icon-document-copy" @click="copyAll">复制全部</el-button>
              <el-button size="mini" type="primary" icon="el-icon-s-promotion" @click="toReport">用于生成报告</el-button>
            </div>
          </div>

          <div v-if="!aiResult" class="emr-ai__empty">
            <i class="el-icon-cpu" />
            <p>尚未生成结果。请在左侧输入或选择病历后点击「AI 一键诊断」。</p>
          </div>

          <div v-else class="emr-ai__result">
            <div class="emr-ai__section">
              <div class="emr-ai__section-title"><i class="el-icon-collection-tag" /> 病历摘要</div>
              <el-input type="textarea" :rows="3" v-model="aiResult.summary" />
            </div>

            <div class="emr-ai__section">
              <div class="emr-ai__section-title"><i class="el-icon-first-aid-kit" /> 症状信息</div>
              <el-input type="textarea" :rows="2" v-model="aiResult.symptoms" placeholder="患者主诉 / 现病史症状" />
            </div>

            <div class="emr-ai__section">
              <div class="emr-ai__section-title"><i class="el-icon-s-flag" /> 初步诊断 / 临床印象</div>
              <el-input type="textarea" :rows="3" v-model="aiResult.clinical_impression" />
              <div class="emr-ai__subtitle">疑似诊断（可编辑）</div>
              <el-input
                v-for="(it, idx) in aiResult.differential"
                :key="'diff' + idx"
                v-model="aiResult.differential[idx]"
                size="small"
                class="emr-ai__list-item"
              >
                <el-button slot="append" icon="el-icon-close" @click="aiResult.differential.splice(idx, 1)" />
              </el-input>
              <el-button size="mini" icon="el-icon-plus" @click="aiResult.differential.push('')">新增</el-button>
            </div>

            <div class="emr-ai__section">
              <div class="emr-ai__section-title"><i class="el-icon-s-goods" /> 用药信息</div>
              <el-input type="textarea" :rows="2" v-model="aiResult.medications" placeholder="当前用药 / 既往用药 / 过敏史药物" />
            </div>

            <div class="emr-ai__section">
              <div class="emr-ai__section-title"><i class="el-icon-notebook-2" /> 既往病史</div>
              <el-input type="textarea" :rows="2" v-model="aiResult.history" placeholder="既往疾病 / 手术 / 外伤" />
            </div>

            <div class="emr-ai__section">
              <div class="emr-ai__section-title"><i class="el-icon-s-order" /> 诊疗建议</div>
              <el-input
                v-for="(it, idx) in aiResult.suggested_actions"
                :key="'sug' + idx"
                v-model="aiResult.suggested_actions[idx]"
                size="small"
                class="emr-ai__list-item"
              >
                <el-button slot="append" icon="el-icon-close" @click="aiResult.suggested_actions.splice(idx, 1)" />
              </el-input>
              <el-button size="mini" icon="el-icon-plus" @click="aiResult.suggested_actions.push('')">新增</el-button>
            </div>

            <div class="emr-ai__disclaimer" v-if="aiResult.disclaimer">
              <i class="el-icon-warning-outline" /> {{ aiResult.disclaimer }}
            </div>

            <div class="emr-ai__footer">
              <el-button type="success" icon="el-icon-check" @click="saveToContext">保存到诊断上下文</el-button>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script>
import { listRecord, getRecord } from '@/api/medical/emr/record'
import { aiExtractMedicalText } from '@/api/physician/ai'
import { patchContext } from '@/api/physician/diagnosisContext'

export default {
  name: 'PhysicianEmrAi',
  data() {
    return {
      inputMode: 'paste',
      rawInput: '',
      recordQuery: { keyword: '', pageNum: 1, pageSize: 20 },
      recordList: [],
      recordLoading: false,
      selectedRecord: null,
      aiLoading: false,
      aiResult: null
    }
  },
  methods: {
    queryRecord() {
      this.recordLoading = true
      listRecord(this.recordQuery).then(res => {
        this.recordList = res.rows || []
      }).finally(() => { this.recordLoading = false })
    },
    onRecordSelect(row) {
      if (!row) return
      this.selectedRecord = row
      getRecord(row.id).then(res => {
        const r = res.data || row
        const txt = r.content || r.rawText || r.title || ''
        this.rawInput = typeof txt === 'string' ? txt : JSON.stringify(txt)
      })
    },
    extractText() {
      const v = (this.rawInput || '').trim()
      if (!v) return ''
      try {
        const obj = JSON.parse(v)
        if (obj && typeof obj === 'object') {
          return obj.medical_record || obj.content || obj.text || obj.rawText || v
        }
      } catch (e) {}
      return v
    },
    normalizeResult(data) {
      const r = data || {}
      return {
        summary: r.summary || '',
        clinical_impression: r.clinical_impression || '',
        differential: Array.isArray(r.differential) ? [...r.differential] : [],
        suggested_actions: Array.isArray(r.suggested_actions) ? [...r.suggested_actions] : [],
        symptoms: r.symptoms || '',
        medications: r.medications || '',
        history: r.history || '',
        disclaimer: r.disclaimer || ''
      }
    },
    handleExtract() {
      const text = this.extractText()
      if (!text) {
        this.$message.warning('请先输入或选择病历内容')
        return
      }
      this.aiLoading = true
      aiExtractMedicalText(text).then(res => {
        this.aiResult = this.normalizeResult(res.data)
        this.$message.success(res.msg || 'AI 分析完成')
      }).catch(() => {}).finally(() => { this.aiLoading = false })
    },
    resetForm() {
      this.rawInput = ''
      this.aiResult = null
      this.selectedRecord = null
    },
    copyAll() {
      if (!this.aiResult) return
      const lines = []
      const r = this.aiResult
      lines.push('【病历摘要】' + r.summary)
      if (r.symptoms) lines.push('【症状】' + r.symptoms)
      lines.push('【临床印象】' + r.clinical_impression)
      if (r.differential && r.differential.length) lines.push('【疑似诊断】\n  - ' + r.differential.join('\n  - '))
      if (r.medications) lines.push('【用药】' + r.medications)
      if (r.history) lines.push('【既往史】' + r.history)
      if (r.suggested_actions && r.suggested_actions.length) lines.push('【建议】\n  - ' + r.suggested_actions.join('\n  - '))
      const text = lines.join('\n')
      if (navigator.clipboard && navigator.clipboard.writeText) {
        navigator.clipboard.writeText(text).then(() => this.$message.success('已复制到剪贴板'))
      } else {
        const ta = document.createElement('textarea')
        ta.value = text; document.body.appendChild(ta); ta.select();
        try { document.execCommand('copy'); this.$message.success('已复制') } catch (e) {}
        document.body.removeChild(ta)
      }
    },
    saveToContext() {
      if (!this.aiResult) return
      patchContext({
        emr: {
          recordId: this.selectedRecord ? this.selectedRecord.id : null,
          title: this.selectedRecord ? this.selectedRecord.title : '手动输入病历',
          rawText: this.rawInput,
          aiResult: { ...this.aiResult }
        }
      })
      this.$message.success('已保存，后续可在报告中复用')
    },
    toReport() {
      this.saveToContext()
      this.$router.push('/physician/report/edit')
    }
  }
}
</script>

<style lang="scss" scoped>
.emr-ai {
  .emr-ai__card { border: 1px solid #e8ecf2; border-radius: 6px; }
  .emr-ai__card-head { display: flex; align-items: center; justify-content: space-between; font-weight: 600; color: #333; }
  .emr-ai__actions { margin-top: 14px; display: flex; gap: 8px; }
  .emr-ai__selected { margin-top: 8px; font-size: 13px; color: #666; }
  .emr-ai__empty {
    color: #999; text-align: center; padding: 60px 0;
    i { font-size: 40px; color: #c0c4cc; display: block; margin-bottom: 12px; }
  }
  .emr-ai__section { padding: 10px 0; border-bottom: 1px dashed #e8ecf2; }
  .emr-ai__section:last-child { border-bottom: none; }
  .emr-ai__section-title { font-weight: 600; color: #165dff; font-size: 14px; margin-bottom: 8px; i { margin-right: 4px; } }
  .emr-ai__subtitle { color: #666; font-size: 12px; margin: 8px 0 4px; }
  .emr-ai__list-item { margin-bottom: 6px; }
  .emr-ai__disclaimer {
    margin-top: 12px; padding: 8px 12px; background: #fff7e6; border-left: 3px solid #f39c12;
    color: #8a6513; font-size: 12px; border-radius: 4px;
  }
  .emr-ai__footer { margin-top: 14px; text-align: right; }
  .mb10 { margin-bottom: 10px; }
}
</style>
