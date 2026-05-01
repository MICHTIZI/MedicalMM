<template>
    <div :class="['sidebar-theme-wrapper', {'has-logo':showLogo}, settings.sideTheme]" :style="{ backgroundColor: settings.sideTheme === 'theme-dark' ? variables.menuBackground : variables.menuLightBackground }">
        <logo v-if="false && showLogo" :collapse="isCollapse" />
        <div class="medical-side-title">
            <span class="medical-side-title__dot"></span>
            <div>
                <p>当前模块</p>
                <strong>功能导航</strong>
            </div>
        </div>
        <el-scrollbar :class="settings.sideTheme" wrap-class="scrollbar-wrapper">
            <el-menu
                :default-active="activeMenu"
                :collapse="isCollapse"
                :background-color="settings.sideTheme === 'theme-dark' ? variables.menuBackground : variables.menuLightBackground"
                :text-color="settings.sideTheme === 'theme-dark' ? variables.menuColor : variables.menuLightColor"
                :unique-opened="true"
                :active-text-color="settings.theme"
                :collapse-transition="false"
                mode="vertical"
            >
                <sidebar-item
                    v-for="(route, index) in sidebarRouters"
                    :key="route.path  + index"
                    :item="route"
                    :base-path="route.path"
                />
            </el-menu>
        </el-scrollbar>
    </div>
</template>

<script>
import { mapGetters, mapState } from "vuex"
import Logo from "./Logo"
import SidebarItem from "./SidebarItem"
import variables from "@/assets/styles/variables.scss"

export default {
    components: { SidebarItem, Logo },
    computed: {
        ...mapState(["settings"]),
        ...mapGetters(["sidebarRouters", "sidebar"]),
        activeMenu() {
            const route = this.$route
            const { meta, path } = route
            // if set path, the sidebar will highlight the path you set
            if (meta.activeMenu) {
                return meta.activeMenu
            }
            return path
        },
        showLogo() {
            return this.$store.state.settings.sidebarLogo
        },
        variables() {
            return variables
        },
        isCollapse() {
            return !this.sidebar.opened
        }
    }
}
</script>

<style lang="scss" scoped>
.sidebar-theme-wrapper {
    height: 100%;
    padding: 18px 14px;
    background: transparent !important;
}

.medical-side-title {
    display: flex;
    align-items: center;
    gap: 12px;
    min-height: 72px;
    padding: 16px 14px;
    margin-bottom: 12px;
    border-radius: 18px;
    background: linear-gradient(135deg, rgba(30, 58, 95, 0.08), rgba(13, 78, 79, 0.08));
    border: 1px solid rgba(30, 58, 95, 0.08);
    color: #1e3a5f;
}

.medical-side-title__dot {
    width: 12px;
    height: 38px;
    border-radius: 999px;
    background: linear-gradient(180deg, #0d4e4f, #43b6a5);
    box-shadow: 0 8px 18px rgba(13, 78, 79, 0.18);
}

.medical-side-title p {
    margin: 0 0 4px;
    font-size: 12px;
    color: #6b7b8f;
}

.medical-side-title strong {
    font-size: 18px;
    letter-spacing: 0.02em;
}

::v-deep .el-scrollbar {
    height: calc(100% - 84px);
}
</style>
