<template>
  <div class="login medical-login">
    <div class="medical-login__panel">
      <div class="medical-login__brand">
        <div class="medical-login__mark" aria-hidden="true" />
        <div class="medical-login__brand-text">
          <span class="medical-login__brand-title">{{ title }}</span>
          <span class="medical-login__brand-sub">管理员登录</span>
        </div>
      </div>
      <el-form ref="loginForm" :model="loginForm" :rules="loginRules" class="login-form">
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
        <el-form-item prop="code" v-if="captchaEnabled">
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
          <div v-if="register" class="medical-login__register-link">
            <router-link class="link-type" :to="'/register'">立即注册</router-link>
          </div>
        </el-form-item>
      </el-form>
    </div>
    <div class="el-login-footer">
      <span>{{ footerContent }}</span>
    </div>
  </div>
</template>

<script>
import { getCodeImg } from "@/api/login"
import Cookies from "js-cookie"
import { encrypt, decrypt } from '@/utils/jsencrypt'
import defaultSettings from '@/settings'

export default {
  name: "Login",
  data() {
    return {
      title: process.env.VUE_APP_TITLE,
      footerContent: defaultSettings.footerContent,
      codeUrl: "",
      loginForm: {
        username: "admin",
        password: "admin123",
        rememberMe: false,
        code: "",
        uuid: ""
      },
      loginRules: {
        username: [
          { required: true, trigger: "blur", message: "请输入您的账号" }
        ],
        password: [
          { required: true, trigger: "blur", message: "请输入您的密码" }
        ],
        code: [{ required: true, trigger: "change", message: "请输入验证码" }]
      },
      loading: false,
      captchaEnabled: true,
      register: false,
      redirect: undefined
    }
  },
  watch: {
    $route: {
      handler: function(route) {
        this.redirect = route.query && route.query.redirect
      },
      immediate: true
    }
  },
  created() {
    this.getCode()
    this.getCookie()
  },
  methods: {
    getCode() {
      getCodeImg().then(res => {
        this.captchaEnabled = res.captchaEnabled === undefined ? true : res.captchaEnabled
        if (this.captchaEnabled) {
          this.codeUrl = "data:image/gif;base64," + res.img
          this.loginForm.uuid = res.uuid
        }
      })
    },
    getCookie() {
      const username = Cookies.get("username")
      const password = Cookies.get("password")
      const rememberMe = Cookies.get('rememberMe')
      this.loginForm = {
        username: username === undefined ? this.loginForm.username : username,
        password: password === undefined ? this.loginForm.password : decrypt(password),
        rememberMe: rememberMe === undefined ? false : Boolean(rememberMe)
      }
    },
    handleLogin() {
      this.$refs.loginForm.validate(valid => {
        if (valid) {
          this.loading = true
          if (this.loginForm.rememberMe) {
            Cookies.set("username", this.loginForm.username, { expires: 30 })
            Cookies.set("password", encrypt(this.loginForm.password), { expires: 30 })
            Cookies.set('rememberMe', this.loginForm.rememberMe, { expires: 30 })
          } else {
            Cookies.remove("username")
            Cookies.remove("password")
            Cookies.remove('rememberMe')
          }
          this.$store.dispatch("Login", this.loginForm).then(() => {
            this.$router.push({ path: this.redirect || "/" }).catch(()=>{})
          }).catch(() => {
            this.loading = false
            if (this.captchaEnabled) {
              this.getCode()
            }
          })
        }
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
  max-width: 420px;
  padding: 40px 36px 28px;
  margin: 24px 16px 56px;
  background: #ffffff;
  border: 1px solid #e8ecf2;
  border-radius: 8px;
  box-sizing: border-box;
}

.medical-login__brand {
  display: flex;
  align-items: center;
  margin-bottom: 28px;
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

.medical-login__register-link {
  margin-top: 12px;
  text-align: right;
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
