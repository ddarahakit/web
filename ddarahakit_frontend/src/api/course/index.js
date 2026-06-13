import $axios from '@/plugins/axiosInterceptor'

/**
 * 코스 목록 조회
 */
const courseList = async (categorySlug) => {
    //결과
    let data = {}
    let url = '/course/list'

    if (categorySlug) {
        //카테고리 슬러그가 있는 경우
        url = `/course/list/${categorySlug}`
    } 

    //API 호출
    await $axios
        .get(url)
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
 * 코스 상세 조회
 */
const courseDetail = async (courseIdx) => {
    //결과
    let data = {}

    //API 호출
    await $axios
        .get(`/course/${courseIdx}`)
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
 * 카테고리 목록 조회
 */
const categoryList = async () => {
    //결과
    let data = {}

    //API 호출
    await $axios
        .get('/course/category')
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
 * 코스 검색 (키워드)
 */
const courseSearch = async (keyword) => {
    //결과
    let data = {}

    //API 호출
    await $axios
        .get('/course/search', { params: { keyword } })
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


export default { courseList, courseDetail, categoryList, courseSearch }
