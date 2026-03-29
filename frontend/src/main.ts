import { createApp } from 'vue'
import { createPinia } from 'pinia'
import piniaPersistedstate from 'pinia-plugin-persistedstate'
import ElementPlus from 'element-plus'
import * as ElementPlusIconsVue from '@element-plus/icons-vue'
import 'vant/lib/index.css'
import 'element-plus/dist/index.css'

// 项目全局样式（顺序很重要：变量 → reset → 主题覆盖 → 公共类）
import '@/assets/styles/variable.scss'
import '@/assets/styles/reset.scss'
import '@/assets/styles/vant.scss'
import '@/assets/styles/element.scss'
import '@/assets/styles/common.scss'

import router from '@/router'
import App from './App.vue'

const app = createApp(App)

// Pinia
const pinia = createPinia()
pinia.use(piniaPersistedstate)
app.use(pinia)

// Vue Router
app.use(router)

// Element Plus（全局注册，自动导入插件会按需覆盖）
app.use(ElementPlus)

// 注册 Element Plus 图标
for (const [key, component] of Object.entries(ElementPlusIconsVue)) {
  app.component(key, component)
}

app.mount('#app')
