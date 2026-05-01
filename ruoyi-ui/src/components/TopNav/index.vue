<template>
  <el-menu
    :default-active="activeMenu"
    mode="horizontal"
    @select="handleSelect"
  >
    <template v-for="(item, index) in topMenus">
      <el-menu-item :style="{'--theme': theme}" :index="item.path" :key="index" v-if="index < visibleNumber">
        <svg-icon
        v-if="item.meta && item.meta.icon && item.meta.icon !== '#'"
        :icon-class="item.meta.icon"/>
        {{ item.meta.title }}
      </el-menu-item>
    </template>

    <!-- 顶部菜单超出数量折叠 -->
    <el-submenu :style="{'--theme': theme}" index="more" :key="visibleNumber" v-if="topMenus.length > visibleNumber">
      <template slot="title">更多菜单</template>
      <template v-for="(item, index) in topMenus">
        <el-menu-item
          :index="item.path"
          :key="index"
          v-if="index >= visibleNumber">
          <svg-icon
            v-if="item.meta && item.meta.icon && item.meta.icon !== '#'"
            :icon-class="item.meta.icon"/>
          {{ item.meta.title }}
        </el-menu-item>
      </template>
    </el-submenu>
  </el-menu>
</template>

<script>
import { constantRoutes } from "@/router"
import { isHttp } from "@/utils/validate"

// 隐藏侧边栏路由
const hideList = ['/index', '/user/profile']

export default {
  data() {
    return {
      // 顶部栏初始数
      visibleNumber: 5,
      // 当前激活菜单的 index
      currentIndex: undefined
    }
  },
  computed: {
    theme() {
      return this.$store.state.settings.theme
    },
    // 顶部显示菜单
    topMenus() {
      let topMenus = []
      this.routers.map((menu) => {
        if (menu.hidden !== true) {
          // 兼容顶部栏一级菜单内部跳转
          if (menu.path === '/' && menu.children) {
            topMenus.push(menu.children[0])
          } else {
            topMenus.push(menu)
          }
        }
      })
      return topMenus
    },
    // 所有的路由信息
    routers() {
      return this.$store.state.permission.topbarRouters
    },
    // 设置子路由
    childrenMenus() {
      var childrenMenus = []
      this.routers.map((router) => {
        for (var item in router.children) {
          if (router.children[item].parentPath === undefined) {
            if(router.path === "/") {
              router.children[item].path = "/" + router.children[item].path
            } else {
              if(!isHttp(router.children[item].path)) {
                router.children[item].path = router.path + "/" + router.children[item].path
              }
            }
            router.children[item].parentPath = router.path
          }
          childrenMenus.push(router.children[item])
        }
      })
      return constantRoutes.concat(childrenMenus)
    },
    // 默认激活的菜单
    activeMenu() {
      const path = this.$route.path
      let activePath = path
      if (path !== undefined && path.lastIndexOf("/") > 0 && hideList.indexOf(path) === -1) {
        const tmpPath = path.substring(1, path.length)
        if (!this.$route.meta.link) {
          activePath = "/" + tmpPath.substring(0, tmpPath.indexOf("/"))
          this.$store.dispatch('app/toggleSideBarHide', false)
        }
      } else if(!this.$route.children) {
        activePath = path
        this.$store.dispatch('app/toggleSideBarHide', true)
      }
      this.activeRoutes(activePath)
      return activePath
    },
  },
  beforeMount() {
    window.addEventListener('resize', this.setVisibleNumber)
  },
  beforeDestroy() {
    window.removeEventListener('resize', this.setVisibleNumber)
  },
  mounted() {
    this.setVisibleNumber()
  },
  methods: {
    // 根据宽度计算设置显示栏数
    setVisibleNumber() {
      const width = document.body.getBoundingClientRect().width / 3
      this.visibleNumber = parseInt(width / 85)
    },
    // 菜单选择事件
    handleSelect(key, keyPath) {
      this.currentIndex = key
      const route = this.routers.find(item => item.path === key)
      if (isHttp(key)) {
        // http(s):// 路径新窗口打开
        window.open(key, "_blank")
      } else if (!route || !route.children) {
        // 没有子路由路径内部打开
        const routeMenu = this.childrenMenus.find(item => item.path === key)
        if (routeMenu && routeMenu.query) {
          let query = JSON.parse(routeMenu.query)
          this.$router.push({ path: key, query: query })
        } else {
          this.$router.push({ path: key })
        }
        this.$store.dispatch('app/toggleSideBarHide', true)
      } else {
        // 显示左侧联动菜单
        this.activeRoutes(key)
        this.$store.dispatch('app/toggleSideBarHide', false)
      }
    },
    // 当前激活的路由
    activeRoutes(key) {
      var routes = []
      if (this.childrenMenus && this.childrenMenus.length > 0) {
        this.childrenMenus.map((item) => {
          if (key == item.parentPath || (key == "index" && "" == item.path)) {
            routes.push(item)
          }
        })
      }
      if(routes.length > 0) {
        this.$store.commit("SET_SIDEBAR_ROUTERS", routes)
      } else {
        this.$store.dispatch('app/toggleSideBarHide', true)
      }
    }
  }
}
</script>

<style lang="scss">
.topmenu-container.el-menu--horizontal {
  display: flex;
  align-items: center;
  height: 72px;
  background: transparent !important;
  border-bottom: none !important;
  overflow: hidden;
}

.topmenu-container.el-menu--horizontal > .el-menu-item {
  float: none;
  height: 42px !important;
  line-height: 42px !important;
  color: rgba(255, 255, 255, 0.82) !important;
  padding: 0 16px !important;
  margin: 0 4px !important;
  border-bottom: none !important;
  border-radius: 999px;
  font-size: 15px;
  font-weight: 600;
  transition: background .2s, color .2s, transform .2s;
}

.topmenu-container.el-menu--horizontal > .el-menu-item:hover {
  background: rgba(255, 255, 255, 0.12) !important;
  color: #ffffff !important;
}

.topmenu-container.el-menu--horizontal > .el-menu-item.is-active,
.topmenu-container.el-menu--horizontal > .el-submenu.is-active .el-submenu__title {
  background: #ffffff !important;
  color: #0d4e4f !important;
  border-bottom: none !important;
  box-shadow: 0 10px 24px rgba(0, 0, 0, 0.12);
}

/* submenu item */
.topmenu-container.el-menu--horizontal > .el-submenu .el-submenu__title {
  float: none;
  height: 42px !important;
  line-height: 42px !important;
  color: rgba(255, 255, 255, 0.82) !important;
  padding: 0 16px !important;
  margin: 0 4px !important;
  border-bottom: none !important;
  border-radius: 999px;
  font-size: 15px;
  font-weight: 600;
}

.topmenu-container.el-menu--horizontal > .el-submenu .el-submenu__title:hover {
  background: rgba(255, 255, 255, 0.12) !important;
  color: #ffffff !important;
}

.el-menu--horizontal .el-menu--popup {
  border-radius: 14px;
  padding: 8px;
  box-shadow: 0 14px 34px rgba(30, 58, 95, 0.16);
}

.el-menu--horizontal .el-menu--popup .el-menu-item {
  height: 40px;
  line-height: 40px;
  border-radius: 10px;
  color: #425466;
}

.el-menu--horizontal .el-menu--popup .el-menu-item:hover {
  background: #eef7f7 !important;
  color: #0d4e4f !important;
}
</style>
