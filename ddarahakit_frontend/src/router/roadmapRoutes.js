/**
 * 로드맵 라우트
 */
const roadmapRoutes = {
    path: '/roadmap',
    component: () => import('@/layouts/MainLayout.vue'),
    children: [
        {
            name: 'roadmap',
            path: '',
            component: () => import('@/views/roadmap/RoadmapList.vue'),
            meta: {
                title: '학습 로드맵 | 따라학잇',
                requiresAuth: false
            }
        },
        {
            name: 'roadmapDetail',
            path: ':roadmapId',
            component: () => import('@/views/roadmap/RoadmapDetail.vue'),
            meta: {
                title: '로드맵 상세 | 따라학잇',
                requiresAuth: false
            }
        }
    ]
}

export default roadmapRoutes
