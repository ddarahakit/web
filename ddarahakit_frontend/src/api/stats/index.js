import $axios from '@/plugins/axiosInterceptor'

/**
 * 서비스 요약 통계 조회 (메인 통계)
 *
 * GET /stats/overview (공개)
 * results: { courseCount, studentCount, satisfactionRate(0~100) }
 */
const overview = async () => {
    //결과
    let data = {}

    //API 호출
    await $axios
        .get('/stats/overview')
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

export default { overview }
