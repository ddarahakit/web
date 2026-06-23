import $axios from '@/plugins/axiosInterceptor'
import { request } from '@/api/request'

/** 이메일 중복 체크 */
const emailDuplicateCheck = (email) => request($axios.get(`/user/email/duplicate?email=${email}`))

/** 회원가입 */
const userSignup = (req) => request($axios.post('/user/signup', {
    email: req.email,
    password: req.password,
    name: req.name
}))

/** 사용자 로그인 */
const userLogin = (req) => request($axios.post('/user/login', {
    email: req.email,
    password: req.password
}))

/** 사용자 로그아웃 */
const userLogout = () => request($axios.get('/user/logout'))

/** 사용자 토큰 갱신 (로그인 연장) */
const userTokenRefresh = () => request($axios.get('/user/token/refresh'))

/** 사용자 토큰 유효성 체크 */
const userTokenCheck = () => request($axios.get('/user/check'))

/** 내 프로필 조회 */
const userProfile = () => request($axios.get('/user/profile'))

/** 내 프로필 수정 */
const updateProfile = (req) => request($axios.put('/user/profile', {
    name: req.name,
    introduction: req.introduction,
    profileImageUrl: req.profileImageUrl
}))

/** 수강 중(결제 완료) 코스 목록 조회 */
const getPaidCourseList = () => request($axios.get('/user/ordered'))

/** 작성한 질문 목록 조회 */
const getMyQuestionList = () => request($axios.get('/user/myquestion'))

/** 작성한 커뮤니티 게시글 목록 조회 */
const getMyPostList = () => request($axios.get('/user/mypost'))

/** 작성한 리뷰 목록 조회 */
const getMyReviewList = () => request($axios.get('/user/myreview'))

/** 소셜 로그인 콜백 호출 */
const loginCallback = () => request($axios.post('/user/social/login'))

/** 비밀번호 변경 (기존/새 비밀번호) */
const updatePassword = (req) => request($axios.put('/user/password/update', req))

// 비밀번호 재설정 공통 바디
const toResetInfo = (req) => ({
    email: req.email,
    uuid: req.uuid,
    newPassword1: req.password1,
    newPassword2: req.password2
})

/** 비밀번호 찾기 요청 */
const resetPasswordReq = (req) => request($axios.post('/user/password/reset', toResetInfo(req)))

/** 비밀번호 다시 설정 */
const resetPassword = (req) => request($axios.put('/user/password/reset', toResetInfo(req)))

/** 비밀번호 재설정 링크(UUID) 만료 확인 */
const checkUuidExpired = (req) =>
    request($axios.get('/user/uuid/check', { params: { email: req.email, uuid: req.uuid } }))

/** 이메일 인증 */
const emailVerify = (req) =>
    request($axios.post('/user/email/verify', { email: req.email, uuid: req.uuid }))

/** 프로필 이미지 업로드 */
const updateProfileImage = (formData) => request($axios.post('/user/profile', formData, {
    headers: { 'Content-Type': 'multipart/form-data' }
}))

/**
 * 결제 내역 조회 (페이징, page 는 0-base)
 * results: PaymentPageRes { payments[], page, size, totalElements, totalPages, hasNext }
 */
const getPayments = (req = {}) =>
    request($axios.get('/user/payments', { params: { page: req.page ?? 0, size: req.size ?? 10 } }))

/** 보안 설정 조회 → { loginAlertEnabled, twoFactorEnabled } */
const getSecuritySettings = () => request($axios.get('/user/security/settings'))

/** 보안 설정 변경 */
const updateSecuritySettings = (req) => request($axios.put('/user/security/settings', {
    loginAlertEnabled: req.loginAlertEnabled,
    twoFactorEnabled: req.twoFactorEnabled
}))

/** 활성 세션 목록 조회 */
const getSessions = () => request($axios.get('/user/sessions'))

/** 세션 종료 (특정 기기 원격 로그아웃) */
const revokeSession = (sessionIdx) => request($axios.delete(`/user/sessions/${sessionIdx}`))

/**
 * 주간 학습활동 조회
 * results: WeeklyRes { days:[{date,count}](7), streakDays, weeklyCompleted, goalRate(0~100), weeklyGoal }
 */
const getWeeklyStudy = () => request($axios.get('/user/study/weekly'))

export default {
    userSignup, userLogin, userLogout, userTokenRefresh, userTokenCheck, userProfile, getPaidCourseList, getMyQuestionList, getMyPostList, getMyReviewList,
    emailDuplicateCheck, loginCallback, updateProfile, updatePassword, resetPasswordReq, resetPassword, checkUuidExpired, emailVerify, updateProfileImage,
    getPayments, getSecuritySettings, updateSecuritySettings, getSessions, revokeSession, getWeeklyStudy
}
