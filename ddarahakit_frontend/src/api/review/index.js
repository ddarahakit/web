import $axios from '@/plugins/axiosInterceptor'

/**
 * 리뷰 더 보기
 */
const reviewList = async (courseIdx, page) => {
    //결과
    let data = {}

    //API 호출
    await $axios
        .get(`/review/${courseIdx}?page=${page}`)
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
 * 리뷰 작성하기
 */
const reviewCreate = async (courseIdx, req) => {
    //요청 정보
    const reviewInfo = {
        comment: req.comment,
        rating: req.rating
    }
    //결과
    let data = {}

    //API 호출
    await $axios
        .post(`/review/${courseIdx}`, reviewInfo)
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

const reviewUpdate = async (courseIdx, req) => {
    //요청 정보
    const reviewInfo = {
        comment: req.comment,
        rating: req.rating
    }
    //결과
    let data = {}

    //API 호출
    await $axios
        .put(`/review/${courseIdx}`, reviewInfo)
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
 * 리뷰 삭제하기
 */
const reviewDelete = async (courseIdx) => {
    
    //결과
    let data = {}

    //API 호출
    await $axios
        .delete(`/review/${courseIdx}`)
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

export default { reviewList, reviewCreate, reviewUpdate, reviewDelete }
