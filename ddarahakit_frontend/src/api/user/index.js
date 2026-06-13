import $axios from '@/plugins/axiosInterceptor'

/** 
 * 이메일 중복 체크
 *
 * 이메일 중복 체크를 한다.
 */
const emailDuplicateCheck = async (email) => {
    //결과
    let data = {}

    //API 호출
    await $axios
        .get(`/user/email/duplicate?email=${email}`)
        .then((res) => {
            //성공
            data = res.data
        })
        .catch((error) => {
            //실패
            data = error.data
        })

    return data
}

/**
 * 회원가입
 */
const userSignup = async (req) => {
    //요청 정보
    const userInfo = {
        email: req.email,
        password: req.password,
        name: req.name,
    }

    //결과
    let data = {}

    //API 호출
    await $axios
        .post('/user/signup', userInfo)
        .then((res) => {
            //성공
            data = res.data
        })
        .catch((error) => {
            //실패
            data = error.data
        })

    return data
}

/**
 * 사용자 로그인
 *
 * 로그인 정보를 기반으로 사용자를 로그인 처리한다.
 */
const userLogin = async (req) => {
    //요청 정보
    const userInfo = {
        email: req.email,
        password: req.password
    }

    //결과
    let data = {}

    //API 호출
    await $axios
        .post('/user/login', userInfo)
        .then((res) => {
            //성공
            data = res.data
        })
        .catch((error) => {
            //실패
            data = error.data
        })

    return data
}

/**
 * 사용자 로그아웃
 *
 * 로그인 정보를 기반으로 사용자를 로그아웃 처리한다.
 */
const userLogout = async () => {
    //결과
    let data = {}

    //API 호출
    await $axios
        .get('/user/logout')
        .then((res) => {
            //성공
            data = res.data
        })
        .catch((error) => {
            //실패
            data = error.data
        })

    return data
}

/**
 * 사용자 토큰 갱신
 *
 * 로그인 연장 개념으로 새로 토큰을 발급 받는다.
 */
const userTokenRefresh = async () => {
    //결과
    let data = {}

    //API 호출
    await $axios
        .get('/user/token/refresh')
        .then((res) => {
            //성공
            data = res.data
        })
        .catch((error) => {
            //실패
            data = error.data
        })

    return data
}

/**
 * 사용자 토큰 체크
 *
 * 토큰 유효성을 체크한다.
 */
const userTokenCheck = async () => {
    //결과
    let data = {}

    //API 호출
    await $axios
        .get('/user/check')
        .then((res) => {
            //성공
            data = res.data
        })
        .catch((error) => {
            //실패
            data = error.data
        })

    return data
}



/**
 * 내 프로필 조회
 *
 * 사용자 번호, 이메일, 프로필 이미지 주소를 조회한다.
 */
const userProfile = async () => {
    //결과
    let data = {}

    //API 호출
    await $axios
        .get('/user/profile')
        .then((res) => {
            //성공
            data = res.data
        })
        .catch((error) => {
            //실패
            data = error.data
        })

    return data
}



/**
 * 내 프로필 수정
 *
 */
const updateProfile = async (req) => {
    //요청 정보
    const userInfo = {
        name: req.name,
        introduction: req.introduction,
        profileImageUrl: req.profileImageUrl
    }
    //결과
    let data = {}

    //API 호출
    await $axios
        .put('/user/profile', userInfo)
        .then((res) => {
            //성공
            data = res.data
        })
        .catch((error) => {
            //실패
            data = error.data
        })

    return data
}


/**
 * 수강 중 강의 조회
 *
 * 결제가 완료된 코스 목록을 조회한다.
 */
const getPaidCourseList = async () => {
    //결과
    let data = {}

    //API 호출
    await $axios
        .get('/user/ordered')
        .then((res) => {
            //성공
            data = res.data
        })
        .catch((error) => {
            //실패
            data = error.data
        })

    return data
}



/**
 * 작성한 질문 목록 조회
 */
const getMyQuestionList = async () => {
    //결과
    let data = {}

    //API 호출
    await $axios
        .get('/user/myquestion')
        .then((res) => {
            //성공
            data = res.data
        })
        .catch((error) => {
            //실패
            data = error.data
        })

    return data
}

/**
 * 작성한 커뮤니티 게시글 목록 조회
 */
const getMyPostList = async () => {
    //결과
    let data = {}

    //API 호출
    await $axios
        .get('/user/mypost')
        .then((res) => {
            //성공
            data = res.data
        })
        .catch((error) => {
            //실패
            data = error.data
        })

    return data
}

/**
 * 작성한 리뷰 목록 조회
 *
 * 구매 후 작성한 리뷰 목록을 조회한다.
 */
const getMyReviewList = async () => {
    //결과
    let data = {}

    //API 호출
    await $axios
        .get('/user/myreview')
        .then((res) => {
            //성공
            data = res.data
        })
        .catch((error) => {
            //실패
            data = error.data
        })

    return data
}



/**
 * 소셜 로그인 콜백 호출
 *
 * 서버로부터 받은 UUID로 콜백 호출.
 */
const loginCallback = async () => {
    //결과
    let data = {}

    //API 호출
    await $axios
        .post('/user/social/login')
        .then((res) => {
            //성공
            data = res.data
        })
        .catch((error) => {
            //실패
            data = error.data
        })

    return data
}


/**
 * 비밀번호 변경 호출
 *
 * 기존 비밀번호와 새 비밀번호로 호출.
 */
const updatePassword = async (req) => {
    //요청 정보
    const passwordInfo = {
        originPassword: req.originPassword,
        newPassword1: req.newPassword1,
        newPassword2: req.newPassword2
    }
    //결과
    let data = {}

    //API 호출
    await $axios
        .put('/user/password/update', req)
        .then((res) => {
            //성공
            data = res.data
        })
        .catch((error) => {
            //실패
            data = error.data
        })

    return data
}

/**
 * 비밀번호 찾기 요청
 *
 */
const resetPasswordReq = async (req) => {
    //요청 정보
    const resetInfo = {
        email: req.email,
        uuid: req.uuid,
        newPassword1: req.password1,
        newPassword2: req.password2
    }

    //결과
    let data = {}

    //API 호출
    await $axios
        .post('/user/password/reset', resetInfo)
        .then((res) => {
            //성공
            data = res.data
        })
        .catch((error) => {
            //실패
            data = error.data
        })

    return data
}

/**
 * 비밀번호 다시 설정 요청
 *
 */
const resetPassword = async (req) => {
    //요청 정보
    const resetInfo = {
        email: req.email,
        uuid: req.uuid,
        newPassword1: req.password1,
        newPassword2: req.password2
    }
    //결과
    let data = {}

    //API 호출
    await $axios
        .put('/user/password/reset', resetInfo)
        .then((res) => {
            //성공
            data = res.data
        })
        .catch((error) => {
            //실패
            data = error.data
        })

    return data
}

/**
 * 비밀번호 다시 설정정 요청
 *
 */
const checkUuidExpired = async (req) => {
    //요청 정보
    const params = {
        email: req.email,
        uuid: req.uuid
    }
    //결과
    let data = {}

    //API 호출
    await $axios
        .get('/user/uuid/check', { params })
        .then((res) => {
            //성공
            data = res.data
        })
        .catch((error) => {
            //실패
            data = error.data
        })

    return data
}


/**
 * 이메일 인증
 *
 */
const emailVerify = async (req) => {
    //요청 정보
    const verifyInfo = {
        email: req.email,
        uuid: req.uuid
    }
    //결과
    let data = {}

    //API 호출
    await $axios
        .post('/user/email/verify', verifyInfo)
        .then((res) => {
            //성공
            data = res.data
        })
        .catch((error) => {
            //실패
            data = error.data
        })

    return data
}




/**
 * 프로필 이미지 업로드
 *
 */
const updateProfileImage = async (formData) => {
    //결과
    let data = {}

    //API 호출
    await $axios
        .post('/user/profile', formData, {
            headers: {
                'Content-Type': 'multipart/form-data',
            },
        })
        .then((res) => {
            //성공
            data = res.data
        })
        .catch((error) => {
            //실패
            data = error.data
        })

    return data
}


/**
 * 결제 내역 조회 (페이징)
 *
 * GET /user/payments?page=&size=  (Pageable, page 는 0-base)
 * results: PaymentPageRes { payments[], page, size, totalElements, totalPages, hasNext }
 */
const getPayments = async (req = {}) => {
    //요청 정보 (page 는 0-base)
    const params = {
        page: req.page ?? 0,
        size: req.size ?? 10
    }
    //결과
    let data = {}

    //API 호출
    await $axios
        .get('/user/payments', { params })
        .then((res) => {
            //성공
            data = res.data
        })
        .catch((error) => {
            //실패
            data = error.data
        })

    return data
}


/**
 * 보안 설정 조회
 *
 * GET /user/security/settings → { loginAlertEnabled, twoFactorEnabled }
 */
const getSecuritySettings = async () => {
    //결과
    let data = {}

    //API 호출
    await $axios
        .get('/user/security/settings')
        .then((res) => {
            //성공
            data = res.data
        })
        .catch((error) => {
            //실패
            data = error.data
        })

    return data
}


/**
 * 보안 설정 변경
 *
 * PUT /user/security/settings { loginAlertEnabled?, twoFactorEnabled? } → 갱신된 설정
 */
const updateSecuritySettings = async (req) => {
    //요청 정보
    const settingsInfo = {
        loginAlertEnabled: req.loginAlertEnabled,
        twoFactorEnabled: req.twoFactorEnabled
    }
    //결과
    let data = {}

    //API 호출
    await $axios
        .put('/user/security/settings', settingsInfo)
        .then((res) => {
            //성공
            data = res.data
        })
        .catch((error) => {
            //실패
            data = error.data
        })

    return data
}


/**
 * 활성 세션 목록 조회
 *
 * GET /user/sessions → [{ sessionIdx, ipAddress, userAgent, createdAt, current }]
 */
const getSessions = async () => {
    //결과
    let data = {}

    //API 호출
    await $axios
        .get('/user/sessions')
        .then((res) => {
            //성공
            data = res.data
        })
        .catch((error) => {
            //실패
            data = error.data
        })

    return data
}


/**
 * 세션 종료 (특정 기기 원격 로그아웃)
 *
 * DELETE /user/sessions/{sessionIdx}
 */
const revokeSession = async (sessionIdx) => {
    //결과
    let data = {}

    //API 호출
    await $axios
        .delete(`/user/sessions/${sessionIdx}`)
        .then((res) => {
            //성공
            data = res.data
        })
        .catch((error) => {
            //실패
            data = error.data
        })

    return data
}


/**
 * 주간 학습활동 조회
 *
 * GET /user/study/weekly (인증)
 * results: WeeklyRes {
 *   days: [{ date, count }] (7개, 오래된→최신),
 *   streakDays, weeklyCompleted, goalRate(0~100), weeklyGoal
 * }
 */
const getWeeklyStudy = async () => {
    //결과
    let data = {}

    //API 호출
    await $axios
        .get('/user/study/weekly')
        .then((res) => {
            //성공
            data = res.data
        })
        .catch((error) => {
            //실패
            data = error.data
        })

    return data
}

export default {
    userSignup, userLogin, userLogout, userTokenRefresh, userTokenCheck, userProfile, getPaidCourseList, getMyQuestionList, getMyPostList, getMyReviewList,
    emailDuplicateCheck, loginCallback, updateProfile, updatePassword, resetPasswordReq, resetPassword, checkUuidExpired, emailVerify, updateProfileImage,
    getPayments, getSecuritySettings, updateSecuritySettings, getSessions, revokeSession, getWeeklyStudy
}
