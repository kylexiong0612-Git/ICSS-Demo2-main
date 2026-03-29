import { createRouter, createWebHashHistory } from 'vue-router'

const routes = [
  {
    path: '/',
    redirect: '/customer',
  },
  {
    path: '/customer',
    name: 'customer',
    component: () => import('@/views/CustomerView.vue'),
    meta: { label: '用户端' },
  },
  {
    path: '/agent/:level',
    name: 'agent',
    component: () => import('@/views/AgentWorkstationView.vue'),
    props: (route: { params: { level: string } }) => ({
      level: Number(route.params.level) as 1 | 2,
    }),
    meta: { label: '坐席工作台' },
  },
  {
    path: '/admin',
    name: 'admin',
    component: () => import('@/views/AdminDashboardView.vue'),
    meta: { label: '管理端' },
  },
]

const router = createRouter({
  history: createWebHashHistory(),
  routes,
})

export default router
