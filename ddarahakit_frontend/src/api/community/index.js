import $axios from '@/plugins/axiosInterceptor'


/**
 * 질문 작성하기
 */
const uploadImage = async (req) => {
    //결과
    let data = {}

    // 요청 데이터 생성
    const formData = new FormData();
    formData.append('image', req);

    //API 호출
    await $axios
        .post('/community/upload', formData, {
            headers: {
                'Content-Type': 'multipart/form-data'
            }
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
 * 게시글 작성하기
 */
const postCreate = async (req) => {

    const postInfo = {
        postType: req.postType,
        title: req.title,
        text: req.text,
        content: req.content,
        courseIdx: req.courseIdx || null,
        lectureIdx: req.lectureIdx || null,
        tags: req.tags || []
    }

    //결과
    let data = {}

    //API 호출
    await $axios
        .post('/community/post', postInfo)
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
 * 게시글 수정하기
 */
const postUpdate = async (postIdx, req) => {

    const postInfo = {
        postType: req.postType,
        title: req.title,
        text: req.text,
        content: req.content,
        courseIdx: req.courseIdx || null,
        lectureIdx: req.lectureIdx || null,
        tags: req.tags || []
    }

    //결과
    let data = {}

    //API 호출
    await $axios
        .put(`/community/post/${postIdx}`, postInfo)
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
 * 게시글 삭제하기
 */
const postDelete = async (postIdx) => {
    //결과
    let data = {}

    //API 호출
    await $axios
        .delete(`/community/post/${postIdx}`)
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
 * 댓글 작성하기
 */
const commentCreate = async (req) => {

    const commentInfo = {
        text: req.text,
        content: req.content,
        postIdx: req.postIdx,
    }

    //결과
    let data = {}

    //API 호출
    await $axios
        .post('/community/comment', commentInfo)
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
 * 댓글 수정하기
 */
const commentUpdate = async (commentIdx, req) => {

    const commentInfo = {
        text: req.text,
        content: req.content,
    }

    //결과
    let data = {}

    //API 호출
    await $axios
        .put(`/community/comment/${commentIdx}`, commentInfo)
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
 * 댓글 삭제하기
 */
const commentDelete = async (commentIdx) => {
    //결과
    let data = {}

    //API 호출
    await $axios
        .delete(`/community/comment/${commentIdx}`)
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
 * 베스트 답변 채택 (토글)
 */
const commentAccept = async (commentIdx) => {
    //결과
    let data = {}

    //API 호출
    await $axios
        .post(`/community/comment/${commentIdx}/accept`)
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
 * 게시글 목록 조회
 */
const postList = async (req) => {
    //결과
    let data = {}

    //페이지 초기화
    if (!req.page) req.page = 1

    //사이즈 초기화
    if (!req.size) req.size = 10

    //조건
    const params = {
        page: req.page - 1,
        size: req.size,
        postType: req.postType,
        courseIdx: req.courseIdx,
        keyword: req.keyword
    }

    //API 호출
    await $axios
        .get('/community/list', { params })
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
 * 게시글 상세 정보를 가져오는 API
 */
const getPostDetail = async (postIdx) => {
    //결과
    let data = {}

    //API 호출
    await $axios
        .get(`/community/${postIdx}`)
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
 * 스크랩 토글
 */
const scrapToggle = async (postIdx) => {
    let data = {}

    await $axios
        .post(`/community/scrap/${postIdx}/toggle`)
        .then((res) => {
            data = res.data
        })
        .catch((error) => {
            data = error.data
        })

    return data
}

/**
 * 스크랩 목록 조회
 */
const scrapList = async () => {
    let data = {}

    await $axios
        .get('/community/scrap')
        .then((res) => {
            data = res.data
        })
        .catch((error) => {
            data = error.data
        })

    return data
}

/**
 * 인기 게시글 랭킹 조회 (명예의 전당 / 실시간 인기 질문)
 *
 * 인기도(스크랩+댓글+조회) 상위 게시글 목록.
 */
const getRanking = async (limit = 10) => {
    let data = {}

    await $axios
        .get('/community/ranking', { params: { limit } })
        .then((res) => {
            data = res.data
        })
        .catch((error) => {
            data = error.data
        })

    return data
}

/**
 * 관련 게시글 조회
 *
 * 같은 코스(없으면 같은 타입) 기준 최신 게시글.
 */
const getRelated = async (postIdx, limit = 5) => {
    let data = {}

    await $axios
        .get(`/community/${postIdx}/related`, { params: { limit } })
        .then((res) => {
            data = res.data
        })
        .catch((error) => {
            data = error.data
        })

    return data
}


export default {
    uploadImage,
    // 신규 (post/comment)
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
