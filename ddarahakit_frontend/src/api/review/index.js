import $axios from '@/plugins/axiosInterceptor'
import { request } from '@/api/request'

/** 리뷰 더 보기 */
const reviewList = (courseIdx, page) => request($axios.get(`/review/${courseIdx}?page=${page}`))

/** 리뷰 작성하기 */
const reviewCreate = (courseIdx, req) =>
    request($axios.post(`/review/${courseIdx}`, { comment: req.comment, rating: req.rating }))

/** 리뷰 수정하기 */
const reviewUpdate = (courseIdx, req) =>
    request($axios.put(`/review/${courseIdx}`, { comment: req.comment, rating: req.rating }))

/** 리뷰 삭제하기 */
const reviewDelete = (courseIdx) => request($axios.delete(`/review/${courseIdx}`))

export default { reviewList, reviewCreate, reviewUpdate, reviewDelete }
