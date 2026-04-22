<template>
  <div class="login medical-login">
    <div class="medical-login__panel">
      <div class="medical-login__brand">
        <div class="medical-login__mark" aria-hidden="true" />
        <div class="medical-login__brand-text">
          <span class="medical-login__brand-title">{{ title }}</span>
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
            <el-form-item v-if="captchaEnabled" prop="code">
              <el-input
                v-model="loginForm.code"
                auto-complete="off"
                placeholder="验证码"
                style="width: 63%"
                @keyup.enter.native="handleLogin"
              >
                <svg-icon slot="prefix" icon-class="validCode" class="el-input__icon input-icon" />
              </el-input>
              <div class="login-code">
                <img :src="codeUrl" class="login-code-img" @click="getCode">
              </div>
            </el-form-item>
            <el-checkbox v-model="loginForm.rememberMe" class="medical-login__remember">记住密码</el-checkbox>
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
            <el-form-item v-if="captchaEnabled" prop="code">
              <el-input
                v-model="registerForm.code"
                auto-complete="off"
                placeholder="验证码"
                style="width: 63%"
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
  background: #ffffff;
}

.medical-login__panel {
  width: 100%;
  max-width: 440px;
  padding: 36px 32px 28px;
  margin: 24px 16px 56px;
  background: #ffffff;
  border: 1px solid #e8ecf2;
  border-radius: 8px;
  box-sizing: border-box;
}

.medical-login__brand {
  display: flex;
  align-items: center;
  margin-bottom: 20px;
}

.medical-login__mark {
  width: 4px;
  height: 40px;
  border-radius: 2px;
  background: #165dff;
  margin-right: 14px;
  flex-shrink: 0;
}

.medical-login__brand-text {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.medical-login__brand-title {
  font-size: 20px;
  font-weight: 600;
  color: #333333;
  letter-spacing: 0.02em;
}

.medical-login__brand-sub {
  font-size: 13px;
  color: #666666;
}

.medical-login__tabs {
  ::v-deep .el-tabs__header {
    margin-bottom: 18px;
  }
  ::v-deep .el-tabs__item {
    font-size: 15px;
    color: #666666;
  }
  ::v-deep .el-tabs__item.is-active {
    color: #165dff;
    font-weight: 600;
  }
  ::v-deep .el-tabs__active-bar {
    background-color: #165dff;
  }
}

.login-form {
  width: 100%;
  padding: 0;
  .el-input {
    height: 40px;
    input {
      height: 40px;
      border-radius: 5px;
    }
  }
  .input-icon {
    height: 40px;
    width: 14px;
    margin-left: 2px;
  }
}

.medical-login__remember {
  margin: 0 0 22px;
  color: #666666;
}

.medical-login__submit-wrap {
  width: 100%;
  margin-bottom: 0;
}

.medical-login__submit {
  width: 100%;
  border-radius: 5px;
}

.login-code {
  width: 33%;
  height: 40px;
  float: right;
  img {
    cursor: pointer;
    vertical-align: middle;
    border-radius: 4px;
    border: 1px solid #e8ecf2;
  }
}

.login-code-img {
  height: 40px;
}

.el-login-footer {
  height: 40px;
  line-height: 40px;
  position: fixed;
  bottom: 0;
  width: 100%;
  text-align: center;
  color: #666666;
  font-size: 12px;
  letter-spacing: 0.02em;
  background: #ffffff;
  border-top: 1px solid #f0f2f5;
}
</style>
