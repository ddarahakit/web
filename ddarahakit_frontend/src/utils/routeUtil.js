import useHistoryStore from '@/stores/useHistoryStore'

/**
 * 이력 저장소 액션 세팅
 *
 * 1. 상세 페이지 외 페이지에서 진입 시
 *  - inbound: 검색 페이지에서 사용하는 액션
 *  - init   : 상세 페이지에서 목록 페이지 진입이 아닐 경우
 * 2. 상세 페이지에서 진입 시
 *  - init   : 목록에서 상세 이동 없이 직접 상세 접근 후 목록 이동 시 검색 목록 객체 값 미존재
 *
 * @param {RouteLocationNormalized} to - 도착 경로 위치
 * @param {RouteLocationNormalized} from - 출발 경로 위치
 */
const settingHistoryStore = (to, from) => {
    //상단 메뉴 직접 호출 변수
    const init = to.query.store

    //저장소 정보 객체
    const historyStore = useHistoryStore()

    //이력 정보 생성 (라우트 명)
    const keySet = historyStore.createHistoryStore(to.name)

    //페이지 이동 전후를 기준으로 액션 설정
    if (!from.meta.requiresHistory?.includes(keySet) || init == 'init') {
        //===> 상세 페이지 외 페이지에서 진입 시
        //이력 저장소 초기화
        historyStore.initHistoryStore(to.name)

        //액션 설정
        if (from.name === 'main' && to.name === 'search') {
            //검색 페이지에서 사용하는 액션
            historyStore.state[keySet]['action'] = 'inbound'
        } else {
            //상세 페이지에서 목록 페이지 진입이 아닐 경우
            historyStore.state[keySet]['action'] = 'init'
        }

        //상단 메뉴 직접 호출 시 액션 설정
        if (init) {
            //쿼리 제거
            to.fullPath = to.path
            //쿼리 초기화
            to.query = {}
        }
    }
    //===> 상세 페이지에서 진입 시
    //목록에서 상세 이동 없이 직접 상세 접근 후 목록 이동 시 검색 목록 객체 값 미존재
    else if (!historyStore.state[keySet]['list'].length) {
        historyStore.state[keySet]['action'] = 'init'
    }
}

export default {
    settingHistoryStore
}
