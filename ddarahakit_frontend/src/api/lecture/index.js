import $axios from '@/plugins/axiosInterceptor'
import { request } from '@/api/request'

/** 강의 상세 조회 */
const lectureDetail = (courseIdx, lectureIdx) =>
    request($axios.get(`/course/lecture/${courseIdx}/${lectureIdx}`))

/** 강의 수강 완료 */
const lectureComplete = (courseIdx, lectureIdx) =>
    request($axios.post('/course/lecture/complete', { courseIdx, lectureIdx }))

/** 강의 등록 */
const lectureCreate = (lectureInput) => request($axios.post('/lecture/create', lectureInput))

export default { lectureDetail, lectureComplete, lectureCreate }
