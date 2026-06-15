import routeUtil from '@/utils/routeUtil'

/**
 * 코스 라우트
 */
const courseRoutes = {
    path: '/community',
    component: () => import('@/layouts/MainLayout.vue'),
    children: [
        {
            name: 'communityList',
            path: 'list',
            component: () => import('@/views/community/CommunityList.vue'),
            beforeEnter: [routeUtil.settingHistoryStore],
            meta: {
                title: '질문 게시판 | 따라학잇',
                requiresAuth: false
            }
        },
        {
            name: 'communityListByCourse',
            path: 'list/:courseIdx',
            component: () => import('@/views/community/CommunityList.vue'),
            beforeEnter: [routeUtil.settingHistoryStore],
            meta: {
                title: '질문 게시판 | 따라학잇',
                requiresAuth: false
            }
        },
        {
            name: 'communityDetail',
            path: ':postIdx(\\d+)',
            component: () => import('@/views/community/CommunityDetail.vue'),
            meta: {
                requiresAuth: false
            }
        }, {
            name: 'communityReg',
            path: 'reg',
            component: () => import('@/views/community/CommunityReg.vue'),
            meta: {
                requiresAuth: true
            }
        }, {
            name: 'communityEdit',
            path: 'edit/:postIdx(\\d+)',
            component: () => import('@/views/community/CommunityReg.vue'),
            meta: {
                requiresAuth: true
            }
        }
    ]
}

export default courseRoutes
