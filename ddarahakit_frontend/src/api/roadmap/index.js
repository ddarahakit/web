import $axios from '@/plugins/axiosInterceptor'

/**
 * 로드맵 목록 조회
 */
const roadmapList = async () => {
    let data = {}

    await $axios
        .get('/roadmap/list')
        .then((res) => {
            data = res.data
        })
        .catch((error) => {
            data = error.data
        })

    return data
}

/**
 * 로드맵 상세 조회
 */
const roadmapDetail = async (roadmapIdx) => {
    let data = {}

    await $axios
        .get(`/roadmap/${roadmapIdx}`)
        .then((res) => {
            data = res.data
        })
        .catch((error) => {
            data = error.data
        })

    return data
}

/**
 * 로드맵 생성
 */
const roadmapCreate = async (req) => {
    let data = {}

    await $axios
        .post('/roadmap', req)
        .then((res) => {
            data = res.data
        })
        .catch((error) => {
            data = error.data
        })

    return data
}

export default { roadmapList, roadmapDetail, roadmapCreate }
