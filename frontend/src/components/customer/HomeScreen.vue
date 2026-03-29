<template>
  <div class="home-screen no-scrollbar">
    <!-- 顶部 Header -->
    <header class="home-header">
      <div class="header-top">
        <div class="user-greeting">
          <div class="user-avatar">
            <el-icon size="18" color="#fff"><User /></el-icon>
          </div>
          <span class="greeting-text">您好，张先生</span>
        </div>
        <div class="header-icons">
          <el-icon size="20" color="#fff"><Camera /></el-icon>
          <el-badge is-dot>
            <el-icon size="20" color="#fff"><Bell /></el-icon>
          </el-badge>
        </div>
      </div>

      <!-- 搜索栏 -->
      <div class="search-box">
        <el-icon size="16" color="rgba(255,255,255,0.6)"><Search /></el-icon>
        <input class="search-input" placeholder="搜索保险产品、理赔进度..." />
      </div>
    </header>

    <!-- 快捷服务宫格 -->
    <div class="quick-services-wrap">
      <div class="quick-services-card">
        <div
          v-for="(item, i) in quickServices"
          :key="i"
          class="service-item"
        >
          <div class="service-icon" :style="{ color: item.color }">
            <el-icon size="24"><component :is="item.icon" /></el-icon>
          </div>
          <span class="service-label">{{ item.label }}</span>
        </div>
      </div>
    </div>

    <!-- Banner 广告 -->
    <div class="banner-wrap">
      <div class="banner">
        <div class="banner-content">
          <h3 class="banner-title">宏掌门·尊享人生</h3>
          <p class="banner-desc">全方位守护您的家庭健康</p>
          <button class="banner-btn">立即了解</button>
        </div>
        <el-icon class="banner-bg-icon" size="128" color="rgba(255,255,255,0.08)"><StarFilled /></el-icon>
      </div>
    </div>

    <!-- 我的保单 -->
    <div class="policy-section">
      <div class="section-header">
        <h3 class="section-title">我的保单</h3>
        <button class="section-more">
          查看全部 <el-icon size="14"><ArrowRight /></el-icon>
        </button>
      </div>
      <div class="policy-list">
        <div
          v-for="(policy, i) in policies"
          :key="i"
          class="policy-card"
        >
          <div class="policy-left">
            <div class="policy-icon">
              <el-icon size="20" color="#00a758"><CircleCheckFilled /></el-icon>
            </div>
            <div class="policy-info">
              <p class="policy-name">{{ policy.name }}</p>
              <p class="policy-no">保单号: {{ policy.id }}</p>
            </div>
          </div>
          <span class="policy-status" :class="policy.statusClass">{{ policy.status }}</span>
        </div>
      </div>
    </div>

    <!-- 悬浮 AI 按钮 -->
    <button class="fab-bot" @click="$emit('open-chat')">
      <svg width="32" height="32" viewBox="0 0 1024 1024" fill="white" xmlns="http://www.w3.org/2000/svg">
        <path d="M22.8 496.7H76v217.2H22.8V496.7z m922 0H998v217.2h-53.2V496.7z"/>
        <path d="M310.3 589.3c0 27.6 22.4 50 50 50s50-22.4 50-50c0-17.9-9.5-34.4-25-43.3-15.5-8.9-34.6-8.9-50 0-15.5 8.9-25 25.4-25 43.3z m0 0M604.1 589.3c0 17.9 9.5 34.4 25 43.3 15.5 8.9 34.6 8.9 50 0 15.5-8.9 25-25.5 25-43.3 0-17.9-9.5-34.4-25-43.3-15.5-8.9-34.6-8.9-50 0-15.4 8.9-25 25.4-25 43.3z m0 0"/>
        <path d="M854.1 299.7H533.8V133.4c14.1-8.9 23.4-24.5 23.4-42.4 0-27.7-22.4-50-50-50-27.7 0-50 22.4-50 50 0 17.9 9.4 33.4 23.4 42.4v166.3H166.7c-24.7 0-44.7 20-44.7 44.7V913c0 24.7 20 44.7 44.7 44.7h687.2c24.7 0 44.7-20 44.7-44.7V344.4c0.2-24.7-20-44.7-44.5-44.7z m-8.5 604.8H175.2V352.9h670.2v551.5h0.2z"/>
      </svg>
      <div v-if="unreadCount > 0" class="fab-badge">{{ unreadCount }}</div>
      <div class="fab-label">宏小二在线</div>
    </button>
  </div>
</template>

<script setup lang="ts">
import {
  User,
  Camera,
  Bell,
  Search,
  CircleCheckFilled,
  CreditCard,
  Grid,
  ArrowRight,
  StarFilled,
  Memo,
} from '@element-plus/icons-vue'

defineProps<{ unreadCount: number }>()
defineEmits<{ 'open-chat': [] }>()

const quickServices = [
  { icon: CircleCheckFilled, label: '理赔服务', color: '#3b82f6' },
  { icon: Memo, label: '保单管理', color: '#00a758' },
  { icon: CreditCard, label: '续期缴费', color: '#f49600' },
  { icon: Grid, label: '全部服务', color: '#8e90a2' },
]

const policies = [
  { name: '宏掌门终身寿险', id: 'POL-88291', status: '保障中', statusClass: 'status-active' },
  { name: '百万医疗险2026', id: 'POL-11023', status: '待缴费', statusClass: 'status-pending' },
]
</script>

<style lang="scss" scoped>
.home-screen {
  flex: 1;
  overflow-y: auto;
  position: relative;
  background-color: var(--bg-color);
}

.home-header {
  background-color: var(--primary-color);
  padding-top: 48px;
  padding-bottom: 24px;
  padding-left: 24px;
  padding-right: 24px;
}

.header-top {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}

.user-greeting {
  display: flex;
  align-items: center;
  gap: 8px;
}

.user-avatar {
  width: 32px;
  height: 32px;
  border-radius: 50%;
  background-color: rgba(255, 255, 255, 0.2);
  display: flex;
  align-items: center;
  justify-content: center;
}

.greeting-text {
  font-size: 14px;
  font-weight: 700;
  color: #fff;
}

.header-icons {
  display: flex;
  align-items: center;
  gap: 16px;
}

.search-box {
  display: flex;
  align-items: center;
  gap: 10px;
  background-color: rgba(255, 255, 255, 0.1);
  border-radius: 16px;
  padding: 12px 16px;
  border: 1px solid rgba(255, 255, 255, 0.1);
}

.search-input {
  flex: 1;
  background: transparent;
  border: none;
  font-size: 14px;
  color: #fff;

  &::placeholder {
    color: rgba(255, 255, 255, 0.6);
  }
}

// 快捷服务
.quick-services-wrap {
  padding: 0 24px;
  margin-top: -16px;
  position: relative;
  z-index: 10;
}

.quick-services-card {
  background-color: #fff;
  border-radius: 24px;
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.08);
  padding: 20px 16px;
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 20px 8px;
}

.service-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
  cursor: pointer;
}

.service-icon {
  width: 48px;
  height: 48px;
  border-radius: 16px;
  background-color: var(--gray-color);
  display: flex;
  align-items: center;
  justify-content: center;
}

.service-label {
  font-size: 11px;
  font-weight: 700;
  color: var(--title-color);
}

// Banner
.banner-wrap {
  padding: 20px 24px 0;
}

.banner {
  width: 100%;
  height: 120px;
  background: linear-gradient(to right, #00a758, #00875a);
  border-radius: 24px;
  padding: 20px;
  color: #fff;
  position: relative;
  overflow: hidden;
  display: flex;
  align-items: flex-start;
}

.banner-content {
  position: relative;
  z-index: 1;
}

.banner-title {
  font-size: 18px;
  font-weight: 700;
  margin-bottom: 4px;
}

.banner-desc {
  font-size: 12px;
  opacity: 0.8;
  margin-bottom: 12px;
}

.banner-btn {
  padding: 6px 16px;
  background-color: #ffd700;
  color: #282b3e;
  font-size: 11px;
  font-weight: 700;
  border-radius: 0;
}

.banner-bg-icon {
  position: absolute;
  right: -16px;
  bottom: -16px;
  pointer-events: none;
}

// 保单列表
.policy-section {
  padding: 24px 24px 80px;
}

.section-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 12px;
}

.section-title {
  font-size: 15px;
  font-weight: 700;
  color: var(--text-color);
}

.section-more {
  display: flex;
  align-items: center;
  gap: 2px;
  font-size: 12px;
  font-weight: 700;
  color: var(--primary-color);
  cursor: pointer;
}

.policy-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.policy-card {
  background-color: #fff;
  padding: 14px;
  border-radius: 16px;
  border: 1px solid var(--border-color);
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.04);
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.policy-left {
  display: flex;
  align-items: center;
  gap: 12px;
}

.policy-icon {
  width: 40px;
  height: 40px;
  border-radius: 12px;
  background-color: var(--gray-color);
  display: flex;
  align-items: center;
  justify-content: center;
}

.policy-name {
  font-size: 14px;
  font-weight: 700;
  color: var(--text-color);
  margin-bottom: 2px;
}

.policy-no {
  font-size: 11px;
  color: var(--desc-color);
}

.policy-status {
  font-size: 11px;
  font-weight: 700;
  padding: 4px 8px;
  border-radius: 8px;

  &.status-active {
    background-color: rgba(0, 167, 88, 0.1);
    color: var(--primary-color);
  }

  &.status-pending {
    background-color: rgba(244, 150, 0, 0.1);
    color: var(--orange);
  }
}

// 悬浮 AI 按钮
.fab-bot {
  position: absolute;
  bottom: 24px;
  right: 24px;
  width: 64px;
  height: 64px;
  background-color: var(--primary-color);
  border-radius: 50%;
  box-shadow: 0 8px 24px rgba(0, 167, 88, 0.4);
  display: flex;
  align-items: center;
  justify-content: center;
  border: 4px solid #fff;
  z-index: 50;
  cursor: pointer;
  transition: transform 0.2s;

  &:hover {
    transform: scale(1.05);
  }

  &:active {
    transform: scale(0.95);
  }
}

.fab-badge {
  position: absolute;
  top: -4px;
  right: -4px;
  width: 20px;
  height: 20px;
  background-color: var(--danger-color);
  border-radius: 50%;
  border: 2px solid #fff;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 9px;
  font-weight: 700;
  color: #fff;
}

.fab-label {
  position: absolute;
  bottom: -32px;
  right: 0;
  background-color: #fff;
  padding: 4px 10px;
  border-radius: 20px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
  border: 1px solid var(--blod-border-color);
  font-size: 11px;
  font-weight: 700;
  color: var(--primary-color);
  white-space: nowrap;
}
</style>
