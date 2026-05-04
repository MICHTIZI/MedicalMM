<template>
  <div class="login medical-login">
    <div class="medical-login__decor medical-login__decor--left" aria-hidden="true"></div>
    <div class="medical-login__decor medical-login__decor--right" aria-hidden="true"></div>

    <div class="medical-login__panel">
      <div class="medical-login__brand">
        <div class="medical-login__icon" aria-hidden="true">
          <svg viewBox="0 0 64 64" role="img">
            <path d="M31.9 8.2c-3.9 2.7-6.1 7.1-6.1 12.2v5.9c0 2.9-1.6 5.6-4.1 7.1l-5.1 3c-5.8 3.4-8.8 10-7.4 16.6.4 2 2.5 3.2 4.4 2.6 8.1-2.4 13.3-9 13.3-17.4V21.7c0-3.4 1.4-6.6 3.9-8.9l1.1-1 1.1 1c2.5 2.3 3.9 5.5 3.9 8.9v16.5c0 8.4 5.2 15 13.3 17.4 1.9.6 4-.6 4.4-2.6 1.4-6.6-1.6-13.2-7.4-16.6l-5.1-3c-2.5-1.5-4.1-4.2-4.1-7.1v-5.9c0-5.1-2.2-9.5-6.1-12.2L32 8l-.1.2z" fill="currentColor" opacity=".18"/>
            <path d="M30 18h4v10h10v4H34v10h-4V32H20v-4h10V18z" fill="currentColor"/>
          </svg>
        </div>
        <div class="medical-login__brand-text">
          <span class="medical-login__brand-title">肺炎多模态辅助诊断系统</span>
          <span class="medical-login__brand-sub">{{ brandSub }}</span>
        </div>
      </div>

      <el-tabs v-model="activeTab" class="medical-login__tabs" @tab-click="onTabClick">
        <el-tab-pane label="登录" name="login">
          <el-form ref="loginForm" :model="loginForm" :rules="loginRulesComputed" class="login-form">
            <el-form-item prop="username">
              <el-input
                v-model="loginForm.username"
                type="text"
                auto-complete="off"
                placeholder="账号"
              >
                <svg-icon slot="prefix" icon-class="user" class="el-input__icon input-icon" />
              </el-input>
            </el-form-item>
            <el-form-item prop="password">
              <el-input
                v-model="loginForm.password"
                type="password"
                auto-complete="off"
                placeholder="密码"
                @keyup.enter.native="handleLogin"
              >
                <svg-icon slot="prefix" icon-class="password" class="el-input__icon input-icon" />
              </el-input>
            </el-form-item>
            <el-form-item v-if="captchaEnabled" prop="code" class="medical-login__captcha">
              <el-input
                v-model="loginForm.code"
                auto-complete="off"
                placeholder="验证码"
                @keyup.enter.native="handleLogin"
              >
                <svg-icon slot="prefix" icon-class="validCode" class="el-input__icon input-icon" />
              </el-input>
              <div class="login-code">
                <img :src="codeUrl" class="login-code-img" @click="getCode">
              </div>
            </el-form-item>
            <div class="medical-login__options">
              <el-checkbox v-model="loginForm.rememberMe" class="medical-login__remember">记住密码</el-checkbox>
            </div>
            <el-form-item class="medical-login__submit-wrap">
              <el-button
                :loading="loading"
                size="medium"
                type="primary"
                class="medical-login__submit"
                @click.native.prevent="handleLogin"
              >
                <span v-if="!loading">登 录</span>
                <span v-else>登 录 中...</span>
              </el-button>
            </el-form-item>
          </el-form>
        </el-tab-pane>

        <el-tab-pane label="注册" name="register">
          <el-form ref="registerForm" :model="registerForm" :rules="registerRulesComputed" class="login-form">
            <el-form-item prop="username">
              <el-input v-model="registerForm.username" type="text" auto-complete="off" placeholder="账号（注册后默认医师角色）">
                <svg-icon slot="prefix" icon-class="user" class="el-input__icon input-icon" />
              </el-input>
            </el-form-item>
            <el-form-item prop="password">
              <el-input
                v-model="registerForm.password"
                type="password"
                auto-complete="off"
                placeholder="密码"
                @keyup.enter.native="handleRegister"
              >
                <svg-icon slot="prefix" icon-class="password" class="el-input__icon input-icon" />
              </el-input>
            </el-form-item>
            <el-form-item prop="confirmPassword">
              <el-input
                v-model="registerForm.confirmPassword"
                type="password"
                auto-complete="off"
                placeholder="确认密码"
                @keyup.enter.native="handleRegister"
              >
                <svg-icon slot="prefix" icon-class="password" class="el-input__icon input-icon" />
              </el-input>
            </el-form-item>
            <el-form-item v-if="captchaEnabled" prop="code" class="medical-login__captcha">
              <el-input
                v-model="registerForm.code"
                auto-complete="off"
                placeholder="验证码"
                @keyup.enter.native="handleRegister"
              >
                <svg-icon slot="prefix" icon-class="validCode" class="el-input__icon input-icon" />
              </el-input>
              <div class="login-code">
                <img :src="codeUrl" class="login-code-img" @click="getCode">
              </div>
            </el-form-item>
            <el-form-item class="medical-login__submit-wrap">
              <el-button
                :loading="registerLoading"
                size="medium"
                type="primary"
                class="medical-login__submit"
                @click.native.prevent="handleRegister"
              >
                <span v-if="!registerLoading">注 册</span>
                <span v-else>提交中...</span>
              </el-button>
            </el-form-item>
          </el-form>
        </el-tab-pane>
      </el-tabs>
    </div>
    <div class="el-login-footer">
      <span>{{ footerContent }}</span>
    </div>
  </div>
</template>

<script>
import { getCodeImg, register } from "@/api/login"
import Cookies from "js-cookie"
import { encrypt, decrypt } from '@/utils/jsencrypt'
import defaultSettings from '@/settings'

export default {
  name: "Login",
  data() {
    const equalToPassword = (rule, value, callback) => {
      if (this.registerForm.password !== value) {
        callback(new Error("两次输入的密码不一致"))
      } else {
        callback()
      }
    }
    return {
      title: process.env.VUE_APP_TITLE,
      footerContent: defaultSettings.footerContent,
      activeTab: "login",
      codeUrl: "",
      loginForm: {
        username: "admin",
        password: "admin123",
        rememberMe: false,
        code: "",
        uuid: ""
      },
      registerForm: {
        username: "",
        password: "",
        confirmPassword: "",
        code: "",
        uuid: ""
      },
      loginRulesBase: {
        username: [{ required: true, trigger: "blur", message: "请输入您的账号" }],
        password: [{ required: true, trigger: "blur", message: "请输入您的密码" }]
      },
      registerRulesBase: {
        username: [
          { required: true, trigger: "blur", message: "请输入您的账号" },
          { min: 2, max: 20, message: "账号长度在 2 到 20 个字符", trigger: "blur" }
        ],
        password: [
          { required: true, trigger: "blur", message: "请输入您的密码" },
          { min: 5, max: 20, message: "密码长度在 5 到 20 个字符", trigger: "blur" },
          { pattern: /^[^<>"'|\\]+$/, message: "不能包含非法字符：< > \" ' \\\ |", trigger: "blur" }
        ],
        confirmPassword: [
          { required: true, trigger: "blur", message: "请再次输入密码" },
          { required: true, validator: equalToPassword, trigger: "blur" }
        ]
      },
      loading: false,
      registerLoading: false,
      captchaEnabled: true,
      redirect: undefined
    }
  },
  computed: {
    brandSub() {
      return this.activeTab === "register" ? "自助注册（默认医师角色）" : "账号登录"
    },
    loginRulesComputed() {
      const rules = { ...this.loginRulesBase }
      if (this.captchaEnabled) {
        rules.code = [{ required: true, trigger: "change", message: "请输入验证码" }]
      }
      return rules
    },
    registerRulesComputed() {
      const rules = { ...this.registerRulesBase }
      if (this.captchaEnabled) {
        rules.code = [{ required: true, trigger: "change", message: "请输入验证码" }]
      }
      return rules
    }
  },
  watch: {
    $route: {
      handler(route) {
        this.redirect = route.query && route.query.redirect
        this.applyTabFromRoute(route)
      },
      immediate: true
    }
  },
  created() {
    this.getCode()
    this.getCookie()
  },
  methods: {
    applyTabFromRoute(route) {
      if (route && route.query && route.query.tab === "register") {
        this.activeTab = "register"
      }
    },
    onTabClick() {
      const q = { ...this.$route.query }
      if (this.activeTab === "register") {
        q.tab = "register"
      } else {
        delete q.tab
      }
      this.$router.replace({ path: "/login", query: q }).catch(() => {})
      this.getCode()
    },
    getCode() {
      getCodeImg().then(res => {
        this.captchaEnabled = res.captchaEnabled === undefined ? true : res.captchaEnabled
        if (this.captchaEnabled) {
          this.codeUrl = "data:image/gif;base64," + res.img
          const uuid = res.uuid
          this.loginForm.uuid = uuid
          this.registerForm.uuid = uuid
        }
      })
    },
    getCookie() {
      const username = Cookies.get("username")
      const password = Cookies.get("password")
      const rememberMe = Cookies.get("rememberMe")
      this.loginForm = {
        ...this.loginForm,
        username: username === undefined ? this.loginForm.username : username,
        password: username === undefined ? this.loginForm.password : (password === undefined ? this.loginForm.password : decrypt(password)),
        rememberMe: rememberMe === undefined ? false : Boolean(rememberMe)
      }
    },
    handleLogin() {
      this.$refs.loginForm.validate(valid => {
        if (!valid) return
        this.loading = true
        if (this.loginForm.rememberMe) {
          Cookies.set("username", this.loginForm.username, { expires: 30 })
          Cookies.set("password", encrypt(this.loginForm.password), { expires: 30 })
          Cookies.set("rememberMe", this.loginForm.rememberMe, { expires: 30 })
        } else {
          Cookies.remove("username")
          Cookies.remove("password")
          Cookies.remove("rememberMe")
        }
        this.$store.dispatch("Login", this.loginForm).then(() => {
          this.$router.push({ path: this.redirect || "/" }).catch(() => {})
        }).catch(() => {
          this.loading = false
          if (this.captchaEnabled) {
            this.getCode()
          }
        })
      })
    },
    handleRegister() {
      this.$refs.registerForm.validate(valid => {
        if (!valid) return
        this.registerLoading = true
        register({
          username: this.registerForm.username,
          password: this.registerForm.password,
          code: this.registerForm.code,
          uuid: this.registerForm.uuid
        }).then(() => {
          this.$message.success("注册成功，请使用新账号登录")
          this.loginForm.username = this.registerForm.username
          this.loginForm.password = ""
          this.registerForm.password = ""
          this.registerForm.confirmPassword = ""
          this.registerForm.code = ""
          this.activeTab = "login"
          this.$router.replace({ path: "/login", query: {} }).catch(() => {})
          this.$nextTick(() => {
            this.$refs.loginForm && this.$refs.loginForm.clearValidate()
            this.$refs.registerForm && this.$refs.registerForm.clearValidate()
          })
          this.getCode()
        }).catch(() => {
          if (this.captchaEnabled) {
            this.getCode()
          }
        }).finally(() => {
          this.registerLoading = false
        })
      })
    }
  }
}
</script>

<style rel="stylesheet/scss" lang="scss" scoped>
.medical-login {
  display: flex;
  justify-content: center;
  align-items: center;
  height: 100%;
  min-height: 100vh;
  position: relative;
  overflow: hidden;
  padding: 40px 18px 72px;
  background:
    radial-gradient(circle at 18% 18%, rgba(67, 182, 165, 0.2), transparent 26%),
    radial-gradient(circle at 82% 24%, rgba(30, 58, 95, 0.12), transparent 28%),
    linear-gradient(135deg, #e8f7f5 0%, #f8fcfc 48%, #ffffff 100%);
}

.medical-login::before {
  content: "";
  position: absolute;
  inset: 0;
  background-image:
    linear-gradient(rgba(13, 78, 79, 0.04) 1px, transparent 1px),
    linear-gradient(90deg, rgba(13, 78, 79, 0.04) 1px, transparent 1px);
  background-size: 44px 44px;
  mask-image: linear-gradient(to bottom, rgba(0, 0, 0, 0.55), transparent 72%);
  pointer-events: none;
}

.medical-login__decor {
  position: absolute;
  width: 260px;
  height: 260px;
  border-radius: 50%;
  border: 42px solid rgba(13, 78, 79, 0.06);
  pointer-events: none;
}

.medical-login__decor--left {
  left: -96px;
  bottom: 70px;
}

.medical-login__decor--right {
  top: 72px;
  right: -88px;
  border-color: rgba(67, 182, 165, 0.09);
}

.medical-login__panel {
  width: 100%;
  max-width: 460px;
  padding: 40px 38px 34px;
  margin: 0;
  position: relative;
  z-index: 1;
  background: rgba(255, 255, 255, 0.92);
  border: 1px solid rgba(30, 58, 95, 0.08);
  border-radius: 24px;
  box-shadow: 0 28px 70px rgba(30, 58, 95, 0.14);
  backdrop-filter: blur(16px);
  box-sizing: border-box;
}

.medical-login__brand {
  display: flex;
  flex-direction: column;
  align-items: center;
  text-align: center;
  margin-bottom: 24px;
}

.medical-login__icon {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 68px;
  height: 68px;
  margin-bottom: 16px;
  color: #0d4e4f;
  border-radius: 22px;
  background: linear-gradient(135deg, rgba(13, 78, 79, 0.12), rgba(67, 182, 165, 0.18));
  box-shadow: 0 14px 30px rgba(13, 78, 79, 0.14);
}

.medical-login__icon svg {
  width: 42px;
  height: 42px;
}

.medical-login__brand-text {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
}

.medical-login__brand-title {
  font-size: 28px;
  font-weight: 800;
  color: #1e3a5f;
  letter-spacing: 0.02em;
}

.medical-login__brand-sub {
  font-size: 14px;
  color: #66788a;
}

.medical-login__tabs {
  ::v-deep .el-tabs__header {
    margin-bottom: 22px;
  }

  ::v-deep .el-tabs__nav-wrap::after {
    height: 1px;
    background-color: rgba(30, 58, 95, 0.08);
  }

  ::v-deep .el-tabs__item {
    height: 42px;
    line-height: 42px;
    font-size: 16px;
    color: #66788a;
  }

  ::v-deep .el-tabs__item.is-active {
    color: #0d4e4f;
    font-weight: 700;
  }

  ::v-deep .el-tabs__active-bar {
    height: 3px;
    border-radius: 999px;
    background-color: #0d4e4f;
  }
}

.login-form {
  width: 100%;
  padding: 0;

  ::v-deep .el-form-item {
    margin-bottom: 20px;
  }

  .el-input {
    height: 50px;

    input {
      height: 50px;
      padding-left: 42px;
      border-radius: 15px;
      border-color: rgba(30, 58, 95, 0.14);
      background: #fbfefe;
      color: #1f2f3f;
      font-size: 15px;
      transition: border-color .2s, box-shadow .2s, background .2s;

      &:focus {
        border-color: #0d4e4f;
        background: #ffffff;
        box-shadow: 0 0 0 4px rgba(13, 78, 79, 0.08);
      }
    }
  }

  .input-icon {
    height: 50px;
    width: 16px;
    margin-left: 8px;
    color: #7890a4;
  }
}

.medical-login__captcha {
  ::v-deep .el-form-item__content {
    display: flex;
    align-items: center;
    gap: 12px;
  }

  .el-input {
    flex: 1;
  }
}

.medical-login__options {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin: -4px 0 22px;
}

.medical-login__remember {
  color: #66788a;

  ::v-deep .el-checkbox__input.is-checked .el-checkbox__inner {
    background-color: #0d4e4f;
    border-color: #0d4e4f;
  }

  ::v-deep .el-checkbox__input.is-checked + .el-checkbox__label {
    color: #0d4e4f;
  }
}

.medical-login__submit-wrap {
  width: 100%;
  margin-bottom: 0;
}

.medical-login__submit {
  width: 100%;
  height: 50px;
  border-radius: 15px;
  border-color: #1e3a5f;
  background: linear-gradient(135deg, #1e3a5f 0%, #0d4e4f 100%);
  font-size: 16px;
  font-weight: 700;
  letter-spacing: 0.08em;
  box-shadow: 0 14px 30px rgba(30, 58, 95, 0.22);

  &:hover,
  &:focus {
    border-color: #0d4e4f;
    background: linear-gradient(135deg, #244b78 0%, #126466 100%);
    box-shadow: 0 16px 34px rgba(30, 58, 95, 0.26);
  }
}

.login-code {
  width: 120px;
  height: 50px;
  flex-shrink: 0;

  img {
    cursor: pointer;
    vertical-align: middle;
    border-radius: 14px;
    border: 1px solid rgba(30, 58, 95, 0.12);
    background: #ffffff;
  }
}

.login-code-img {
  width: 100%;
  height: 50px;
  object-fit: cover;
}

.el-login-footer {
  height: 40px;
  line-height: 40px;
  position: fixed;
  bottom: 0;
  width: 100%;
  text-align: center;
  color: #7890a4;
  font-size: 12px;
  letter-spacing: 0.02em;
  background: transparent;
}

@media screen and (max-width: 520px) {
  .medical-login {
    padding: 28px 14px 62px;
  }

  .medical-login__panel {
    padding: 32px 22px 28px;
    border-radius: 20px;
  }

  .medical-login__brand-title {
    font-size: 24px;
  }

  .medical-login__captcha {
    ::v-deep .el-form-item__content {
      gap: 8px;
    }
  }

  .login-code {
    width: 104px;
  }
}
</style>
