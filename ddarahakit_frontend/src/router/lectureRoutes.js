import routeUtil from '@/utils/routeUtil'

/**
 * 코스 라우트
 */
const lectureRoutes = {
    path: '/lecture',
    component: () => import('@/layouts/LectureLayout.vue'),
    children: [
        {
            name: 'lecture',
            path: ':courseIdx/:lectureIdx',
            component: () => import('@/views/lecture/Lecture.vue'),
            beforeEnter: [routeUtil.settingHistoryStore],
            meta: {
                title: '강의 수강 | 따라학잇',
                requiresAuth: true
            }
        },
        {
            name: 'lectureCreate',
            path: 'create',
            component: () => import('@/views/lecture/LectureCreate.vue'),
            beforeEnter: [routeUtil.settingHistoryStore],
            meta: {
                title: '강의 생성 | 따라학잇',
                requiresAuth: false
            }
        }

    ]
}

export default lectureRoutes
