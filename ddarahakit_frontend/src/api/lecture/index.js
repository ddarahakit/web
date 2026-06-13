import $axios from '@/plugins/axiosInterceptor'

/**
 * 강의의 상세 조회
 */
const lectureDetail = async (courseIdx, lectureIdx) => {
    //결과
    let data = {}

    //API 호출
    await $axios
        .get(`/course/lecture/${courseIdx}/${lectureIdx}`)
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
 * 강의 수강 완료
 */
const lectureComplete = async (courseIdx, lectureIdx) => {
    //요청 정보
    const lectureCompleteInfo = {
        courseIdx: courseIdx,
        lectureIdx: lectureIdx
    }

    //결과
    let data = {}

    //API 호출
    await $axios
        .post('/course/lecture/complete', lectureCompleteInfo)
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
 * 강의 등록
 */
const lectureCreate = async (lectureInput) => {
    //결과
    let data = {}

    //API 호출
    await $axios.post('/lecture/create', lectureInput)
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

export default { lectureDetail, lectureComplete, lectureCreate }
