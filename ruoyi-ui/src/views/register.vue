<template>
  <div class="register medical-login">
    <div class="medical-login__panel">
      <div class="medical-login__brand">
        <div class="medical-login__mark" aria-hidden="true" />
        <div class="medical-login__brand-text">
          <span class="medical-login__brand-title">{{ title }}</span>
          <span class="medical-login__brand-sub">用户注册</span>
        </div>
      </div>
      <el-form ref="registerForm" :model="registerForm" :rules="registerRules" class="register-form">
        <el-form-item prop="username">
          <el-input v-model="registerForm.username" type="text" auto-complete="off" placeholder="账号">
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
          <div class="register-code">
            <img :src="codeUrl" class="register-code-img" @click="getCode">
          </div>
        </el-form-item>
        <el-form-item class="medical-login__submit-wrap">
          <el-button
            :loading="loading"
            size="medium"
            type="primary"
            class="medical-login__submit"
            @click.native.prevent="handleRegister"
          >
            <span v-if="!loading">注 册</span>
            <span v-else>注 册 中...</span>
          </el-button>
          <div class="medical-login__register-link">
            <router-link class="link-type" :to="'/login'">使用已有账户登录</router-link>
          </div>
        </el-form-item>
      </el-form>
    </div>
    <div class="el-register-footer">
      <span>{{ footerContent }}</span>
    </div>
  </div>
</template>

<script>
import { getCodeImg, register } from "@/api/login"
import defaultSettings from '@/settings'

export default {
  name: "Register",
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
      codeUrl: "",
      registerForm: {
        username: "",
        password: "",
        confirmPassword: "",
        code: "",
        uuid: ""
      },
      registerRules: {
        username: [
          { required: true, trigger: "blur", message: "请输入您的账号" },
          { min: 2, max: 20, message: '用户账号长度必须介于 2 和 20 之间', trigger: 'blur' }
        ],
        password: [
          { required: true, trigger: "blur", message: "请输入您的密码" },
          { min: 5, max: 20, message: "用户密码长度必须介于 5 和 20 之间", trigger: "blur" },
          { pattern: /^[^<>"'|\\]+$/, message: "不能包含非法字符：< > \" ' \\\ |", trigger: "blur" }
        ],
        confirmPassword: [
          { required: true, trigger: "blur", message: "请再次输入您的密码" },
          { required: true, validator: equalToPassword, trigger: "blur" }
        ],
        code: [{ required: true, trigger: "change", message: "请输入验证码" }]
      },
      loading: false,
      captchaEnabled: true
    }
  },
  created() {
    this.getCode()
  },
  methods: {
    getCode() {
      getCodeImg().then(res => {
        this.captchaEnabled = res.captchaEnabled === undefined ? true : res.captchaEnabled
        if (this.captchaEnabled) {
          this.codeUrl = "data:image/gif;base64," + res.img
          this.registerForm.uuid = res.uuid
        }
      })
    },
    handleRegister() {
      this.$refs.registerForm.validate(valid => {
        if (valid) {
          this.loading = true
          register(this.registerForm).then(() => {
            const username = this.registerForm.username
            this.$alert('账号「' + username + '」注册成功，请使用新账号登录。', '提示', {
              type: 'success'
            }).then(() => {
              this.$router.push("/login")
            }).catch(() => {})
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

.register-form {
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

.register-code {
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

.register-code-img {
  height: 40px;
}

.el-register-footer {
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
