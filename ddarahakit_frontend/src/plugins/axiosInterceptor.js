import axios from 'axios'
import userApi from "@/api/user";
import useAuthStore from '@/stores/useAuthStore'

// Credentials 기본 설정
axios.defaults.withCredentials = true

// 토큰 갱신 상태 관리
let isRefreshing = false
let failedQueue = []

/**
 * 대기 중인 요청 처리
 * 토큰 갱신 완료 후 대기열의 요청들을 재시도하거나 거부한다.
 */
const processQueue = (error) => {
    failedQueue.forEach(prom => {
        if (error) {
            prom.reject(error)
        } else {
            prom.resolve()
        }
    })
    failedQueue = []
}

/**
 * axios 인스턴스 생성
 */
const $axios = axios.create({
    baseURL: import.meta.env.VITE_LOCAL_API_BASE_URL
        ? import.meta.env.VITE_LOCAL_API_BASE_URL
        : import.meta.env.VITE_API_BASE_URL,
    timeout: import.meta.env.VITE_API_TIMEOUT
})

/**
 * 요청 인터셉터
 */
$axios.interceptors.request.use(
    (config) => {

        return config
    },
    (error) => {
        //요청 오류
        return Promise.reject(error)
    }
)

/**
 * 응답 인터셉터
 */
$axios.interceptors.response.use(
    (response) => {
        //응답 전달 전

        //STR: 업무상 실제 성공 여부 추가
        const stream = response.headers['content-type'] === 'application/octet-stream'
        let success = false
        let errorMessage = ''

        if (response.data.code === 20000) {
            //===> 일반 응답
            //상세 결과
            if (
                typeof response.data.results === 'undefined' ||
                response.data.results === null ||
                typeof response.data.results.result === 'undefined' ||
                response.data.results.result
            ) {
                //성공: 상세 결과 미정의
                //성공: 상세 결과 없음
                //성공: 상세 결과-결과 없음
                //성공: 상세 결과-결과 참
                success = true
            }
        } else if (stream) {
            //===> 스트림 응답
            if (response.status === 200) {
                success = true
            }
        }

        if (!success) {
            //실패일 경우 에러 메시지
            if (!response?.data?.results?.result) {
                errorMessage = response?.data?.results?.resultMessage
            } else if (response?.data) {
                errorMessage = response?.data?.message
            } else {
                errorMessage = '오류가 발생했습니다.'
            }
        }

        if (stream) {
            //===> 스트림 응답
            response.success = success
            response.errorMessage = errorMessage
        } else {
            //===> 일반 응답
            response.data.success = success
            response.data.errorMessage = errorMessage
        }
        //END: 업무상 실제 성공 여부 추가

        return response
    },
    async (error) => {
        let data = {}

        if (error.response && error.response.status === 401) {
            const errorCode = error.response.data?.code

            if (errorCode === 20001) {
                const authStore = useAuthStore()

                // 토큰 갱신 요청 자체가 401인 경우 재시도하지 않음 (무한 재귀 방지)
                if (error.config.url?.includes('/user/token/refresh')) {
                    // 리프레시 토큰도 만료됨 → 에러 처리로 진행
                } else if (isRefreshing) {
                    // 이미 갱신 중이면 대기열에 추가 후 갱신 완료 시 재시도
                    return new Promise((resolve, reject) => {
                        failedQueue.push({ resolve, reject })
                    }).then(() => {
                        return $axios(error.config)
                    })
                } else if (!error.config._retry) {
                    error.config._retry = true
                    isRefreshing = true

                    try {
                        if (authStore.checkLogin()) {
                            const refreshData = await userApi.userTokenRefresh()
                            if (refreshData && refreshData.success) {
                                authStore.loginTmXts(refreshData.results)
                                isRefreshing = false
                                processQueue(null)
                                return $axios(error.config)
                            }
                        }
                    } catch (refreshError) {
                        // 리프레시 요청 자체가 실패
                    }

                    isRefreshing = false
                    processQueue(error)

                    // 리프레시 실패 시 로그아웃 처리
                    // 정리 전에 실제로 로그인 세션이 있었는지 확인한다.
                    const hadSession = authStore.checkLogin()

                    // 스토리지의 로그인 기록 삭제
                    authStore.logout()

                    // 세션이 있었을 때만 알림 + 로그인 리다이렉트.
                    // 이미 로그아웃 상태에서 들어온 401 은 조용히 종료해
                    // "세션이 만료되었습니다" 알림이 반복되는 루프를 막는다.
                    if (hadSession) {
                        alert("세션이 만료되었습니다. 다시 로그인해주세요.")
                        window.location.href = '/user/login'
                    }
                    return Promise.reject(error)
                }
            }
        }

        if (error.response && error.response.data) {
            //요청 전송 성공, 응답 수신 성공(2xx 외 상태 코드)
            data = error.response.data
            data.errorMessage = error.response.data.message
        } else {
            //요청 전송 성공, 응답 수신 실패('error.request'는 브라우저에서 XMLHttpRequest 인스턴스)
            //오류가 발생한 요청을 설정하는 동안 문제 발생
            data.code = error.code
            data.errorMessage = error.message
        }

        //에러 데이터
        error.data = data
        //업무상 실제 성공 여부
        error.data.success = false

        //ECONNREFUSED, ECONNABORTED, ERR_NETWORK
        return Promise.reject(error)
    }
)

export default $axios
