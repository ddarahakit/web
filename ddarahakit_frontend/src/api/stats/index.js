import $axios from '@/plugins/axiosInterceptor'
import { request } from '@/api/request'

/**
 * 서비스 요약 통계 조회 (메인 통계)
 * GET /stats/overview (공개)
 * results: { courseCount, studentCount, satisfactionRate(0~100) }
 */
const overview = () => request($axios.get('/stats/overview'))

export default { overview }
