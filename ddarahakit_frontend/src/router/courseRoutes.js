/**
 * 코스 라우트
 */
const courseRoutes = {
    path: '/course',
    component: () => import('@/layouts/DefaultLayout.vue'),
    children: [
        {
            name: 'courseList',
            path: 'list',
            component: () => import('@/views/course/CourseList.vue'),            meta: {
                title: '전체 강의 | 따라학잇',
                category: [{ name: '전체 강의', path: '/course/list' }],
                requiresAuth: false,
                // 카테고리 전환 시 페이지 전체가 아닌 강의 목록만 갱신되도록
                // 강의목록/카테고리 라우트가 동일 transitionKey 를 공유 → 컴포넌트 유지(remount/transition 방지)
                transitionKey: 'course-list'
            }
        },
        {
            name: 'courseListByCategory',
            path: 'list/:categorySlug',
            component: () => import('@/views/course/CourseList.vue'),
            meta: {
                requiresAuth: false,
                transitionKey: 'course-list'
            }
        },
        {
            name: 'courseDetail',
            path: ':courseIdx(\\d+)', // 숫자만 허용하는 정규식 추가
            component: () => import('@/views/course/CourseDetail.vue'),
            meta: {
                requiresAuth: false
            }
        }
    ]
}

export default courseRoutes
