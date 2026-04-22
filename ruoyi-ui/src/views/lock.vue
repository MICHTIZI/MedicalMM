<template>
  <div class="lock-container">
    <div class="lock-time">{{ currentTime }}</div>
    <div class="lock-date">{{ currentDate }}</div>

    <div class="lock-card">
      <div class="avatar-wrap">
        <img :src="avatar" class="lock-avatar" @error="onAvatarError">
        <div class="lock-icon" aria-hidden="true">
          <i class="el-icon-lock" />
        </div>
      </div>
      <div class="lock-username">{{ nickName }}</div>
      <div class="lock-hint">系统已锁定，请输入密码解锁</div>

      <div class="input-wrap" :class="{ shake: isShaking }">
        <input
          ref="passwordInput"
          v-model="password"
          type="password"
          placeholder="请输入登录密码"
          class="lock-input"
          autocomplete="off"
          @keydown.enter="handleUnlock"
        >
        <button class="unlock-btn" type="button" :disabled="loading" @click="handleUnlock">
          <span v-if="!loading">解锁</span>
          <span v-else class="loading-dot">···</span>
        </button>
      </div>

      <div v-if="errorMsg" class="error-msg">{{ errorMsg }}</div>

      <div class="lock-footer">
        <a href="/login" @click.prevent="goLogin">退出重新登录</a>
      </div>
    </div>
  </div>
</template>

<script>
import { mapGetters } from 'vuex'
import { unlockScreen } from '@/api/login'
import defAva from '@/assets/images/profile.jpg'

export default {
  name: 'LockScreen',
  data() {
    return {
      password: '',
      loading: false,
      errorMsg: '',
      isShaking: false,
      currentTime: '',
      currentDate: '',
      timer: null
    }
  },
  computed: {
    ...mapGetters(['avatar', 'nickName'])
  },
  mounted() {
    this.startClock()
    this.$nextTick(() => {
      this.$refs.passwordInput && this.$refs.passwordInput.focus()
    })
  },
  beforeDestroy() {
    clearInterval(this.timer)
  },
  methods: {
    onAvatarError(e) {
      e.target.src = defAva
    },
    startClock() {
      const update = () => {
        const now = new Date()
        const h = String(now.getHours()).padStart(2, '0')
        const m = String(now.getMinutes()).padStart(2, '0')
        const s = String(now.getSeconds()).padStart(2, '0')
        this.currentTime = `${h}:${m}:${s}`
        const days = ['星期日', '星期一', '星期二', '星期三', '星期四', '星期五', '星期六']
        this.currentDate = `${now.getFullYear()}年${now.getMonth() + 1}月${now.getDate()}日 ${days[now.getDay()]}`
      }
      update()
      this.timer = setInterval(update, 1000)
    },
    async handleUnlock() {
      if (!this.password) {
        this.showError('请输入密码')
        return
      }
      this.loading = true
      this.errorMsg = ''
      try {
        await unlockScreen(this.password)
        const lockPath = this.$store.getters.lockPath
        await this.$store.dispatch('lock/unlockScreen')
        this.$router.replace(lockPath)
      } catch (err) {
        const msg = err.message || err.toString()
        this.showError(msg)
        this.password = ''
        this.$refs.passwordInput && this.$refs.passwordInput.focus()
      } finally {
        this.loading = false
      }
    },
    showError(msg) {
      this.errorMsg = msg
      this.isShaking = true
      setTimeout(() => { this.isShaking = false }, 600)
    },
    goLogin() {
      this.$store.dispatch('lock/unlockScreen')
      this.$store.dispatch('LogOut').then(() => {
        this.$router.push('/login')
      })
    }
  }
}
</script>

<style scoped>
.lock-container {
  position: fixed;
  inset: 0;
  background: #f5f7fa;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  z-index: 9999;
  font-family: 'PingFang SC', 'Microsoft YaHei', sans-serif;
  overflow: auto;
  padding: 24px 16px;
}

.lock-time {
  font-size: 48px;
  font-weight: 600;
  color: #333333;
  letter-spacing: 2px;
  margin-bottom: 8px;
  font-variant-numeric: tabular-nums;
}

.lock-date {
  font-size: 14px;
  color: #666666;
  margin-bottom: 32px;
  letter-spacing: 0.02em;
}

.lock-card {
  background: #ffffff;
  border: 1px solid #e8ecf2;
  border-radius: 8px;
  padding: 32px 36px 28px;
  width: 100%;
  max-width: 380px;
  display: flex;
  flex-direction: column;
  align-items: center;
  box-sizing: border-box;
}

.avatar-wrap {
  position: relative;
  margin-bottom: 16px;
}

.lock-avatar {
  width: 72px;
  height: 72px;
  border-radius: 50%;
  border: 2px solid #e8ecf2;
  object-fit: cover;
  display: block;
}

.lock-icon {
  position: absolute;
  bottom: -2px;
  right: -2px;
  background: #ecf3ff;
  border: 1px solid #d6e4ff;
  border-radius: 50%;
  width: 28px;
  height: 28px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 14px;
  color: #165dff;
}

.lock-username {
  color: #333333;
  font-size: 17px;
  font-weight: 600;
  margin-bottom: 6px;
}

.lock-hint {
  color: #666666;
  font-size: 13px;
  margin-bottom: 22px;
}

.input-wrap {
  width: 100%;
  display: flex;
  align-items: center;
  background: #f5f7fa;
  border: 1px solid #e8ecf2;
  border-radius: 6px;
  padding: 4px 4px 4px 14px;
  transition: border-color 0.2s, box-shadow 0.2s;
}

.input-wrap:focus-within {
  border-color: #165dff;
  box-shadow: 0 0 0 2px rgba(22, 93, 255, 0.12);
}

.input-wrap.shake {
  animation: shake 0.5s ease;
}

@keyframes shake {
  0%, 100% { transform: translateX(0); }
  20% { transform: translateX(-6px); }
  40% { transform: translateX(6px); }
  60% { transform: translateX(-4px); }
  80% { transform: translateX(4px); }
}

.lock-input {
  flex: 1;
  background: transparent;
  border: none;
  outline: none;
  color: #333333;
  font-size: 14px;
  padding: 10px 0;
}

.lock-input::placeholder {
  color: #999999;
}

.unlock-btn {
  min-width: 72px;
  height: 36px;
  padding: 0 14px;
  border-radius: 5px;
  background: #165dff;
  border: none;
  color: #ffffff;
  font-size: 14px;
  cursor: pointer;
  transition: background 0.2s, opacity 0.2s;
  flex-shrink: 0;
}

.unlock-btn:hover:not(:disabled) {
  background: #0f4cd9;
}

.unlock-btn:disabled {
  opacity: 0.65;
  cursor: not-allowed;
}

.loading-dot {
  font-size: 13px;
  letter-spacing: 1px;
}

.error-msg {
  margin-top: 12px;
  color: #e5484d;
  font-size: 13px;
  text-align: center;
}

.lock-footer {
  margin-top: 20px;
}

.lock-footer a {
  color: #165dff;
  font-size: 13px;
  text-decoration: none;
}

.lock-footer a:hover {
  text-decoration: underline;
}
</style>
