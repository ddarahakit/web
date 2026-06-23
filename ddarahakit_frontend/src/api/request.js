/**
 * API 호출 공통 래퍼.
 *
 * axios 응답 인터셉터(axiosInterceptor.js)가 성공/실패를 모두 동일한 data 객체
 * (success/errorMessage 포함)로 정규화하므로, 각 API 함수에 반복되던
 * `then(res => data = res.data).catch(e => data = e.data)` 보일러플레이트를 이 헬퍼로 일원화한다.
 *
 * @param {Promise} promise $axios 호출 프라미스
 * @returns {Promise<object>} 성공 시 res.data, 실패(reject) 시 error.data
 */
export async function request(promise) {
    try {
        return (await promise).data
    } catch (error) {
        return error.data
    }
}
