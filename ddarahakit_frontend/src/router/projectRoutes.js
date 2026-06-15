/**
 * 수업 진행 방식 라우트 (포트폴리오: 수업 진행 방식·지도 노하우)
 */
const projectRoutes = {
    path: '/projects',
    component: () => import('@/layouts/MainLayout.vue'),
    children: [
        {
            name: 'projectManagement',
            path: '',
            component: () => import('@/views/project/ProjectManagement.vue'),
            meta: {
                title: '포트폴리오 | 따라학잇',
                requiresAuth: false
            }
        }
    ]
}

export default projectRoutes
