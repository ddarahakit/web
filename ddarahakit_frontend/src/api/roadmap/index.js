import $axios from '@/plugins/axiosInterceptor'
import { request } from '@/api/request'

/** 로드맵 목록 조회 */
const roadmapList = () => request($axios.get('/roadmap/list'))

/** 로드맵 상세 조회 */
const roadmapDetail = (roadmapIdx) => request($axios.get(`/roadmap/${roadmapIdx}`))

/** 로드맵 생성 */
const roadmapCreate = (req) => request($axios.post('/roadmap', req))

export default { roadmapList, roadmapDetail, roadmapCreate }
