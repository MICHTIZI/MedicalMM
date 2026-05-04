<template>
  <div class="navbar medical-navbar" :class="'nav' + navType">
    <hamburger v-if="false" id="hamburger-container" :is-active="sidebar.opened" class="hamburger-container" @toggleClick="toggleSideBar" />
    <breadcrumb v-if="false" id="breadcrumb-container" class="breadcrumb-container" />
    <top-bar v-if="false" id="topbar-container" class="topbar-container" />
    <div class="medical-navbar__brand">
      <logo v-show="showLogo" :collapse="false"></logo>
      <div class="medical-navbar__meta">
        <span class="medical-navbar__eyebrow">Multimodal Pneumonia Diagnosis</span>
        <span class="medical-navbar__title">肺炎多模态辅助诊断系统</span>
      </div>
    </div>

    <top-nav id="topmenu-container" class="topmenu-container medical-primary-menu" />

    <div class="right-menu">
      <template v-if="device!=='mobile'">
        <search id="header-search" class="right-menu-item" />

        <screenfull id="screenfull" class="right-menu-item hover-effect" />

        <el-tooltip content="布局大小" effect="light" placement="bottom">
          <size-select id="size-select" class="right-menu-item hover-effect" />
        </el-tooltip>

        <el-tooltip content="消息通知" effect="light" placement="bottom">
          <header-notice id="header-notice" class="right-menu-item hover-effect" />
        </el-tooltip>

      </template>

      <el-dropdown class="avatar-container right-menu-item hover-effect" trigger="hover">
        <div class="avatar-wrapper">
          <img :src="avatar" class="user-avatar">
          <span class="user-nickname"> {{ nickName }} </span>
        </div>
        <el-dropdown-menu slot="dropdown">
          <router-link to="/user/profile">
            <el-dropdown-item>个人中心</el-dropdown-item>
          </router-link>
          <el-dropdown-item @click.native="setLayout" v-if="setting">
            <span>布局设置</span>
          </el-dropdown-item>
          <el-dropdown-item @click.native="lockScreen">
            <span>锁定屏幕</span>
          </el-dropdown-item>
          <el-dropdown-item divided @click.native="logout">
            <span>退出登录</span>
          </el-dropdown-item>
        </el-dropdown-menu>
      </el-dropdown>
    </div>
  </div>
</template>

<script>
import { mapGetters } from 'vuex'
import Breadcrumb from '@/components/Breadcrumb'
import TopNav from '@/components/TopNav'
import TopBar from './TopBar'
import Logo from './Sidebar/Logo'
import Hamburger from '@/components/Hamburger'
import Screenfull from '@/components/Screenfull'
import SizeSelect from '@/components/SizeSelect'
import Search from '@/components/HeaderSearch'
import HeaderNotice from './HeaderNotice'

export default {
  components: {
    Breadcrumb,
    Logo,
    TopNav,
    TopBar,
    Hamburger,
    Screenfull,
    SizeSelect,
    Search,
    HeaderNotice
  },
  computed: {
    ...mapGetters([
      'sidebar',
      'avatar',
      'device',
      'nickName'
    ]),
    setting: {
      get() {
        return this.$store.state.settings.showSettings
      }
    },
    navType: {
      get() {
        return this.$store.state.settings.navType
      }
    },
    showLogo: {
      get() {
        return this.$store.state.settings.sidebarLogo
      }
    }
  },
  methods: {
    toggleSideBar() {
      this.$store.dispatch('app/toggleSideBar')
    },
    setLayout(event) {
      this.$emit('setLayout')
    },
    lockScreen() {
      const currentPath = this.$route.fullPath
      this.$store.dispatch('lock/lockScreen', currentPath).then(() => {
        this.$router.push('/lock')
      })
    },
    logout() {
      this.$confirm('确定注销并退出系统吗？', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        this.$store.dispatch('LogOut').then(() => {
          location.href = '/index'
        })
      }).catch(() => {})
    }
  }
}
</script>

<style lang="scss" scoped>
.navbar {
  height: 72px;
  overflow: hidden;
  position: relative;
  background: linear-gradient(135deg, #1e3a5f 0%, #0d4e4f 100%);
  border-bottom: 1px solid rgba(255, 255, 255, 0.12);
  box-shadow: 0 10px 30px rgba(13, 78, 79, 0.18);
  display: flex;
  align-items: center;
  padding: 0 24px;
  box-sizing: border-box;

  .medical-navbar__brand {
    display: flex;
    align-items: center;
    width: 310px;
    flex-shrink: 0;
    min-width: 0;
  }

  .medical-navbar__meta {
    display: flex;
    flex-direction: column;
    justify-content: center;
    margin-left: 14px;
    line-height: 1.25;
    color: #ffffff;
    min-width: 0;
  }

  .medical-navbar__eyebrow {
    font-size: 12px;
    letter-spacing: 0.08em;
    text-transform: uppercase;
    color: rgba(255, 255, 255, 0.62);
  }

  .medical-navbar__title {
    margin-top: 3px;
    font-size: 19px;
    font-weight: 700;
    white-space: nowrap;
  }

  .topmenu-container {
    flex: 1;
    min-width: 0;
    margin-left: 8px;
  }

  .right-menu {
    height: 100%;
    line-height: 72px;
    display: flex;
    align-items: center;
    gap: 8px;
    margin-left: 20px;
    flex-shrink: 0;

    &:focus {
      outline: none;
    }

    .right-menu-item {
      display: inline-flex;
      align-items: center;
      justify-content: center;
      padding: 0 10px;
      height: 40px;
      min-width: 40px;
      font-size: 18px;
      color: rgba(255, 255, 255, 0.86);
      border-radius: 14px;
      vertical-align: text-bottom;

      &.hover-effect {
        cursor: pointer;
        transition: background .2s, color .2s, transform .2s;

        &:hover {
          background: rgba(255, 255, 255, 0.14);
          color: #ffffff;
          transform: translateY(-1px);
        }
      }
    }

    .avatar-container {
      padding: 0 4px 0 10px;

      .avatar-wrapper {
        display: flex;
        align-items: center;
        gap: 10px;
        height: 40px;

        .user-avatar {
          cursor: pointer;
          width: 34px;
          height: 34px;
          border-radius: 50%;
          border: 2px solid rgba(255, 255, 255, 0.72);
          box-shadow: 0 4px 12px rgba(0, 0, 0, 0.16);
        }

        .user-nickname{
          max-width: 96px;
          overflow: hidden;
          text-overflow: ellipsis;
          white-space: nowrap;
          font-size: 15px;
          font-weight: 600;
          color: #ffffff;
        }

        .el-icon-caret-bottom {
          cursor: pointer;
          font-size: 12px;
        }
      }
    }
  }
}

::v-deep .sidebar-logo-container {
  height: 48px;
  line-height: 48px;
  background: transparent !important;
  border-bottom: none;
  text-align: left;

  .sidebar-logo-link {
    display: flex;
    align-items: center;
    width: auto;
  }

  .sidebar-logo {
    width: 42px !important;
    height: 42px !important;
    margin-right: 0 !important;
    padding: 6px;
    background: rgba(255, 255, 255, 0.16);
    border-radius: 14px;
  }

  .sidebar-title {
    display: none !important;
  }
}

@media screen and (max-width: 991px) {
  .navbar {
    height: auto;
    min-height: 72px;
    flex-wrap: wrap;
    padding: 12px 14px;

    .medical-navbar__brand {
      width: auto;
      flex: 1;
    }

    .topmenu-container {
      order: 3;
      flex-basis: 100%;
      margin: 10px 0 0;
    }

    .right-menu {
      margin-left: 8px;
    }
  }
}
</style>
