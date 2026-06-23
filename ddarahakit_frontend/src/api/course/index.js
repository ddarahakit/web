import $axios from '@/plugins/axiosInterceptor'
import { request } from '@/api/request'

/** 코스 목록 조회 (카테고리 슬러그 있으면 카테고리별) */
const courseList = (categorySlug) =>
    request($axios.get(categorySlug ? `/course/list/${categorySlug}` : '/course/list'))

/** 코스 상세 조회 */
const courseDetail = (courseIdx) => request($axios.get(`/course/${courseIdx}`))

/** 카테고리 목록 조회 */
const categoryList = () => request($axios.get('/course/category'))

/** 코스 검색 (키워드) */
const courseSearch = (keyword) => request($axios.get('/course/search', { params: { keyword } }))

export default { courseList, courseDetail, categoryList, courseSearch }
