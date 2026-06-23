/**
 * 메인 라우트
 */
const mainRoutes = {
    path: '/',
    component: () => import('@/layouts/MainLayout.vue'),
    children: [
        {
            name: 'main',
            path: 'main',
            component: () => import('@/views/main/Main.vue'),            meta: {
                title: '따라하면서 배우는 IT',
                requiresAuth: false
            }
        }
    ]
}

export default mainRoutes
