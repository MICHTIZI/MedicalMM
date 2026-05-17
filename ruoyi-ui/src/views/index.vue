<template>
  <div class="app-container welcome-home">
    <div class="welcome-home__inner">
      <p class="welcome-home__eyebrow">医疗辅助诊断管理平台</p>
      <h1 class="welcome-home__title">{{ greeting }}，{{ displayName }}</h1>
      <p class="welcome-home__desc">
        从这里进入患者管理，查看与处理您名下的患者信息。
      </p>
      <el-button type="primary" size="medium" class="welcome-home__cta" @click="goPatientList">
        开始使用
      </el-button>
    </div>
  </div>
</template>

<script>
import { mapGetters } from 'vuex'

export default {
  name: 'Index',
  computed: {
    ...mapGetters(['nickName', 'name']),
    displayName() {
      const n = (this.nickName || '').trim()
      if (n) return n
      return (this.name || '').trim() || '用户'
    },
    greeting() {
      const h = new Date().getHours()
      if (h < 12) return '上午好'
      if (h < 18) return '下午好'
      return '晚上好'
    }
  },
  methods: {
    goPatientList() {
      this.$router.push({ path: '/patient/list' }).catch(() => {})
    }
  }
}
</script>

<style lang="scss" scoped>
.welcome-home {
  min-height: calc(100vh - 120px);
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 32px 16px;
  background: linear-gradient(165deg, #f0f4fb 0%, #f5f7fa 45%, #eef2f8 100%);
}

.welcome-home__inner {
  width: 100%;
  max-width: 520px;
  text-align: center;
  padding: 48px 40px;
  background: #ffffff;
  border: 1px solid #e4e9f2;
  border-radius: 10px;
  box-shadow: 0 8px 28px rgba(15, 35, 95, 0.06);
}

.welcome-home__eyebrow {
  margin: 0 0 12px;
  font-size: 13px;
  letter-spacing: 0.12em;
  text-transform: uppercase;
  color: #909399;
}

.welcome-home__title {
  margin: 0 0 16px;
  font-size: 24px;
  font-weight: 600;
  color: #303133;
  line-height: 1.45;
}

.welcome-home__desc {
  margin: 0 0 28px;
  font-size: 14px;
  line-height: 1.75;
  color: #606266;
}

.welcome-home__cta {
  min-width: 132px;
  padding-left: 28px;
  padding-right: 28px;
}
</style>
