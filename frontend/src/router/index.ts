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
      workflowCode: 'ops-service-flow',
      stageCode: `L${Number(route.params.level) || 1}`,
    }),
    meta: { label: '坐席工作台' },
  },
  {
    path: '/agent/stage/:workflowCode/:stageCode',
    name: 'agent-stage',
    component: () => import('@/views/AgentWorkstationView.vue'),
    props: (route: { params: { workflowCode: string; stageCode: string } }) => ({
      workflowCode: route.params.workflowCode,
      stageCode: route.params.stageCode,
    }),
    meta: { label: '坐席工作台' },
  },
  {
    path: '/admin',
    name: 'admin',
    component: () => import('@/views/AdminDashboardView.vue'),
    meta: { label: '管理端' },
  },
  {
    path: '/admin/agent-workflows',
    name: 'admin-agent-workflows',
    component: () => import('@/views/AdminWorkflowConfigView.vue'),
    meta: { label: '工作流配置' },
  },
  {
    path: '/admin/workbench-config',
    name: 'admin-workbench-config',
    component: () => import('@/views/AdminWorkbenchConfigView.vue'),
    meta: { label: '工作台配置' },
  },
]

const router = createRouter({
  history: createWebHashHistory(),
  routes,
})

export default router
