/**
 * 유저 라우트
 */
const userRoutes = {
    path: '/user',
    component: () => import('@/layouts/DefaultLayout.vue'),
    children: [
        {
            name: 'login',
            path: 'login',
            component: () => import('@/views/user/Login.vue'),
            meta: {
                title: '로그인 | 따라학잇',
                category: [{ name: '로그인', path: '/user/login' }],
                requiresAuth: false
            }
        },
        {
            name: 'signup',
            path: 'signup',
            component: () => import('@/views/user/Signup.vue'),
            meta: {
                title: '회원 가입 | 따라학잇',
                category: [{ name: '회원 가입', path: '/user/signup' }],
                requiresAuth: false,
            }
        },
        {
            name: 'signupComplete',
            path: 'signup/complete',
            component: () => import('@/views/user/SignupComplete.vue'),
            meta: {
                title: '회원가입 완료 | 따라학잇',
                category: [{ name: '회원 가입', path: '/user/signup' }, { name: '가입 완료', path: '' }],
                requiresAuth: false,
            }
        },
        {
            name: 'social',
            path: 'social/:provider',
            component: () => import('@/components/user/social/SocialLoginCallback.vue'),
            meta: {
                title: '카카오 로그인 | 따라학잇',
                category: [{ name: '카카오 로그인', path: '/user/login' }],
                requiresAuth: false
            }
        },
        {
            // 전용 로그아웃 페이지 (절대 경로 → /logout, DefaultLayout 헤더/푸터 유지)
            name: 'logout',
            path: '/logout',
            component: () => import('@/views/user/LogoutView.vue'),
            meta: {
                title: '로그아웃 | 따라학잇',
                requiresAuth: false
            }
        },
        {
            name: 'dashboard',
            path: 'dashboard',
            component: () => import('@/views/user/Dashboard.vue'),
            meta: {
                title: '내 강의실 | 따라학잇',
                category: [{ name: '내 강의실', path: '/user/dashboard' }],
                requiresAuth: true,
            }
        },
        {
            name: 'profileEdit',
            path: 'profile',
            component: () => import('@/views/user/ProfileEdit.vue'),
            meta: {
                title: '프로필 수정 | 따라학IT',
                category: [{ name: '내 강의실', path: '/user/dashboard' }, { name: '프로필 수정', path: '/user/profile' }],
                requiresAuth: true,
            }
        },
        {
            name: 'accountSecurity',
            path: 'security',
            component: () => import('@/views/user/AccountSecurity.vue'),
            meta: {
                title: '계정 보안 | 따라학잇',
                category: [{ name: '내 강의실', path: '/user/dashboard' }, { name: '계정 보안', path: '/user/security' }],
                requiresAuth: true,
            }
        },
        {
            name: 'paymentHistory',
            path: 'payments',
            component: () => import('@/views/user/PaymentHistory.vue'),
            meta: {
                title: '결제 내역 | 따라학잇',
                category: [{ name: '내 강의실', path: '/user/dashboard' }, { name: '결제 내역', path: '/user/payments' }],
                requiresAuth: true,
            }
        },
        {
            name: 'resetPassowrd',
            path: 'password/reset',
            component: () => import('@/views/user/PasswordReset.vue'),
            meta: {
                title: '비밀번호 변경 | 따라학잇',
                category: [{ name: '내 강의실', path: '/user/dashboard' }, { name: '비밀번호 변경', path: '/user/password/reset' }],
                requiresAuth: false,
            }
        },
        {
            name: 'findPassowrd',
            path: 'password/find',
            component: () => import('@/views/user/PasswordFind.vue'),
            meta: {
                title: '비밀번호 찾기 | 따라학잇',
                category: [{ name: '내 강의실', path: '/user/dashboard' }, { name: '비밀번호 찾기', path: '/user/password/reset' }],
                requiresAuth: false,
            }
        },
        {
            name: 'emailVerify',
            path: 'email/verify',
            component: () => import('@/views/user/SignupVerify.vue'),
            meta: {
                title: '이메일 인증 | 따라학잇',
                category: [{ name: '내 강의실', path: '/user/dashboard' }, { name: '이메일 인증', path: '' }],
                requiresAuth: false,
            }
        }

    ]
}

export default userRoutes
