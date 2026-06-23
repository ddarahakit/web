import $axios from '@/plugins/axiosInterceptor'
import { request } from '@/api/request'

/** 게시글/댓글 본문 이미지 업로드 */
const uploadImage = (req) => {
    const formData = new FormData()
    formData.append('image', req)
    return request($axios.post('/community/upload', formData, {
        headers: { 'Content-Type': 'multipart/form-data' }
    }))
}

// 게시글 등록/수정 공통 바디
const toPostInfo = (req) => ({
    postType: req.postType,
    title: req.title,
    text: req.text,
    content: req.content,
    courseIdx: req.courseIdx || null,
    lectureIdx: req.lectureIdx || null,
    tags: req.tags || []
})

/** 게시글 작성하기 */
const postCreate = (req) => request($axios.post('/community/post', toPostInfo(req)))

/** 게시글 수정하기 */
const postUpdate = (postIdx, req) => request($axios.put(`/community/post/${postIdx}`, toPostInfo(req)))

/** 게시글 삭제하기 */
const postDelete = (postIdx) => request($axios.delete(`/community/post/${postIdx}`))

/** 댓글 작성하기 */
const commentCreate = (req) => request($axios.post('/community/comment', {
    text: req.text,
    content: req.content,
    postIdx: req.postIdx
}))

/** 댓글 수정하기 */
const commentUpdate = (commentIdx, req) => request($axios.put(`/community/comment/${commentIdx}`, {
    text: req.text,
    content: req.content
}))

/** 댓글 삭제하기 */
const commentDelete = (commentIdx) => request($axios.delete(`/community/comment/${commentIdx}`))

/** 베스트 답변 채택 (토글) */
const commentAccept = (commentIdx) => request($axios.post(`/community/comment/${commentIdx}/accept`))

/** 게시글 목록 조회 (page 는 1-base 입력 → 0-base 로 변환) */
const postList = (req) => {
    const page = req.page || 1
    const size = req.size || 10
    const params = {
        page: page - 1,
        size,
        postType: req.postType,
        courseIdx: req.courseIdx,
        keyword: req.keyword
    }
    return request($axios.get('/community/list', { params }))
}

/** 게시글 상세 조회 */
const getPostDetail = (postIdx) => request($axios.get(`/community/${postIdx}`))

/** 스크랩 토글 */
const scrapToggle = (postIdx) => request($axios.post(`/community/scrap/${postIdx}/toggle`))

/** 스크랩 목록 조회 */
const scrapList = () => request($axios.get('/community/scrap'))

/** 인기 게시글 랭킹 조회 (인기도=스크랩+댓글+조회 상위) */
const getRanking = (limit = 10) => request($axios.get('/community/ranking', { params: { limit } }))

/** 관련 게시글 조회 (같은 코스, 없으면 같은 타입 기준 최신) */
const getRelated = (postIdx, limit = 5) =>
    request($axios.get(`/community/${postIdx}/related`, { params: { limit } }))

export default {
    uploadImage,
    postCreate,
    postUpdate,
    postDelete,
    postList,
    getPostDetail,
    commentCreate,
    commentUpdate,
    commentDelete,
    commentAccept,
    scrapToggle,
    scrapList,
    getRanking,
    getRelated
}
