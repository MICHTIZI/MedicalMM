<template>
  <div class="app-container medical-home">
    <div class="medical-home__card">
      <h1 class="medical-home__title">工作台</h1>
      <p class="medical-home__desc">
        医疗辅助诊断管理平台 · 数据看板与业务入口将在此区域展示。
      </p>
    </div>
  </div>
</template>

<script>
export default {
  name: "Index"
}
</script>

<style lang="scss" scoped>
.medical-home {
  min-height: calc(100vh - 120px);
  display: flex;
  align-items: flex-start;
  justify-content: center;
  padding-top: 24px;
  background: #f5f7fa;
}

.medical-home__card {
  width: 100%;
  max-width: 960px;
  background: #ffffff;
  border: 1px solid #e8ecf2;
  border-radius: 6px;
  padding: 32px 28px;
}

.medical-home__title {
  margin: 0 0 12px;
  font-size: 20px;
  font-weight: 600;
  color: #333333;
  letter-spacing: 0.02em;
}

.medical-home__desc {
  margin: 0;
  font-size: 14px;
  line-height: 1.7;
  color: #666666;
}
</style>
