<template>
  <div class="app-shell">
    <!-- 顶部导航栏 -->
    <header class="app-header">
      <div class="header-brand">
        <div class="brand-icon">
          <el-icon size="24" color="#fff"><Monitor /></el-icon>
        </div>
        <div class="brand-text">
          <h1 class="brand-title">宏掌门 <span class="brand-highlight">智能服务平台</span></h1>
          <p class="brand-sub">Integrated Service Platform V1.0</p>
        </div>
      </div>

      <div class="header-right">
        <!-- 视图切换 -->
        <nav class="view-switcher">
          <button
            class="nav-btn"
            :class="{ active: currentPath === '/customer' }"
            @click="router.push('/customer')"
          >
            <el-icon><Iphone /></el-icon>
            用户端
          </button>
          <button
            class="nav-btn"
            :class="{ active: currentPath.startsWith('/agent/1') }"
            @click="router.push('/agent/1')"
          >
            <el-icon><User /></el-icon>
            一线坐席
          </button>
          <button
            class="nav-btn"
            :class="{ active: currentPath.startsWith('/agent/2') }"
            @click="router.push('/agent/2')"
          >
            <el-icon><User /></el-icon>
            二线坐席
          </button>
          <button
            class="nav-btn"
            :class="{ active: currentPath === '/admin' }"
            @click="router.push('/admin')"
          >
            <el-icon><DataBoard /></el-icon>
            管理端
          </button>
        </nav>

        <div class="header-divider" />

        <!-- 用户信息 -->
        <div class="header-user">
          <el-badge is-dot>
            <el-icon size="20" class="icon-bell"><Bell /></el-icon>
          </el-badge>
          <div class="user-info">
            <p class="user-name">管理员</p>
            <p class="user-id">ID: 882910</p>
          </div>
          <el-avatar :size="36" src="https://picsum.photos/seed/admin/100/100" />
        </div>
      </div>
    </header>

    <!-- 页面内容 -->
    <main class="app-main">
      <router-view v-slot="{ Component }">
        <Transition name="page-fade" mode="out-in">
          <component :is="Component" />
        </Transition>
      </router-view>
    </main>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useRouter, useRoute } from 'vue-router'

const router = useRouter()
const route = useRoute()

const currentPath = computed(() => route.path)
</script>

<style lang="scss" scoped>
.app-shell {
  height: 100vh;
  display: flex;
  flex-direction: column;
  overflow: hidden;
  background-color: var(--bg-color);
}

.app-header {
  flex-shrink: 0;
  height: 64px;
  background-color: #fff;
  border-bottom: 1px solid var(--blod-border-color);
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 24px;
  z-index: 100;
}

.header-brand {
  display: flex;
  align-items: center;
  gap: 12px;
}

.brand-icon {
  width: 40px;
  height: 40px;
  background-color: var(--primary-color);
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 4px 12px var(--shallow-primary-color);
}

.brand-text {
  display: flex;
  flex-direction: column;
}

.brand-title {
  font-size: 18px;
  font-weight: 700;
  color: var(--text-color);
  line-height: 1.2;
}

.brand-highlight {
  color: var(--primary-color);
}

.brand-sub {
  font-size: 10px;
  color: var(--desc-color);
  text-transform: uppercase;
  letter-spacing: 0.1em;
  font-weight: 600;
}

.header-right {
  display: flex;
  align-items: center;
  gap: 24px;
}

.view-switcher {
  display: flex;
  background-color: var(--gray-color);
  padding: 4px;
  border-radius: 8px;
  gap: 2px;
}

.nav-btn {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 6px 16px;
  border-radius: 6px;
  font-size: 14px;
  font-weight: 500;
  color: var(--label-desc-color);
  transition: all 0.2s;
  cursor: pointer;

  &:hover {
    color: var(--title-color);
  }

  &.active {
    background-color: #fff;
    color: var(--primary-color);
    box-shadow: 0 1px 4px rgba(0, 0, 0, 0.08);
  }
}

.header-divider {
  width: 1px;
  height: 32px;
  background-color: var(--blod-border-color);
}

.header-user {
  display: flex;
  align-items: center;
  gap: 12px;
}

.icon-bell {
  color: var(--label-desc-color);
  cursor: pointer;
  transition: color 0.2s;

  &:hover {
    color: var(--primary-color);
  }
}

.user-info {
  text-align: right;
}

.user-name {
  font-size: 14px;
  font-weight: 600;
  color: var(--text-color);
  line-height: 1.2;
}

.user-id {
  font-size: 10px;
  color: var(--desc-color);
  margin-top: 2px;
}

.app-main {
  flex: 1;
  overflow: hidden;
  position: relative;
}

// 页面切换动画
.page-fade-enter-active,
.page-fade-leave-active {
  transition: opacity 0.2s ease, transform 0.2s ease;
}

.page-fade-enter-from {
  opacity: 0;
  transform: translateX(12px);
}

.page-fade-leave-to {
  opacity: 0;
  transform: translateX(-12px);
}
</style>
