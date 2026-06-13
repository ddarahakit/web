/**
 * 이력 정보 저장소 객체
 *
 * 이력 정보를 저장소에 저장한다.
 *
 * action
 *  1. init: 상세 페이지 외 다른 페이지에서 진입
 *  2. list: 상세 페이지에서 목록 버튼 클릭
 */
import { defineStore } from 'pinia'
import { reactive } from 'vue'
import { useRoute, useRouter } from 'vue-router'

//이력 정보 객체
const useHistoryStore = defineStore('history', () => {
    //이력 정보 객체
    const state = reactive({})

    //라우트 정보 객체
    const route = useRoute()

    //라우터 정보 객체
    const router = useRouter()

    /**
     * 이력 정보 생성
     *
     * 라우트 명을 키셋으로 하는 이력 정보 객체를 추가한다.
     *
     * @returns 키셋을 반환한다.
     */
    const createHistoryStore = (name) => {
        //키셋 설정
        let keySet = name

        if (!keySet) {
            //===> 라우트 beforeEnter 통한 생성이 아닌 경우
            //현재 라우트 정보에 requiresHistory 메타 값 필터
            const record = router
                .getRoutes()
                .filter((record) => record.name === route.name && record.meta?.requiresHistory)

            if (record.length) {
                //상세 페이지에서 생성 요청 시 해당하는 parent 명을 키셋으로 설정
                keySet = record[0].meta.requiresHistory[0]
            } else {
                //목록 페이지에서 생성 요청 시 접속 라우트 명을 키셋으로 설정
                keySet = route.name
            }
        }

        //생성된 업무 이력 정보가 없으면 신규 생성
        if (router.hasRoute(keySet) && !state[keySet]) {
            state[keySet] = {
                word: null, //검색어
                page: 1, //현재 페이지
                more: false, //더보기 버튼 보여주기
                results: false, //검색 결과 여부
                list: [], //검색 목록 객체
                total: 0, //전체 목록 수
                action: null, //액션
                options: {
                    category: [], //[서비스/검색]업무별 카테고리
                    selectedCategory: null, //선택된 업무별 카테고리 인덱스
                    analysis: [], //[서비스]분석 목적
                    selectedAnalysis: [], //선택된 분석 목적
                    data: [], //[서비스]데이터 기준
                    selectedData: [], //선택된 데이터 기준
                    customer: [], //[서비스]고객사 기준
                    selectedCustomer: [], //선택된 고객사 기준
                    trendis: false //[활용장]Trendreport만 모아보기
                },
                //타겟마케팅 옵션
                tarmktOption: {
                    tabs: null, //누구에게 보내시나요?
                    targetTendency: [], //이용성향
                    targetFamily: [], //가구형태
                    targetSex: [], //성별 (자유롭게 선택하기)
                    targetAge: [], //연령대 (자유롭게 선택하기)
                    targetAmount: [], //월 이용금액 (자유롭게 선택하기)
                    //거주지 (자유롭게 선택하기)
                    targetArea: {
                        si: null, //광역시도
                        do: null, //시군구
                        selected: [] //선택한 거주지
                    },
                    target: null, //타겟 (많이 이용하는 타겟)
                    targetSexI: [], //성별 (많이 이용하는 타겟)
                    targetAgeI: [], //연령대 (많이 이용하는 타겟)
                    targetAmountI: [], //월 이용금액 (많이 이용하는 타겟)
                    //거주지 (많이 이용하는 타겟)
                    targetAreaI: {
                        si: null, //광역시도
                        do: null, //시군구
                        selected: [] //선택한 거주지
                    },
                    sending: {} //발송매체
                }
            }
        }

        return keySet
    }

    /**
     * 이력 저장소 초기화
     *
     * 키에 해당하는 이력 정보를 초기화한다.
     */
    const initHistoryStore = (name) => {
        //키셋 설정
        const keySet = name || route.name

        state[keySet]['page'] = 1 //현재 페이지
        state[keySet]['more'] = false //더보기 버튼 보여주기
        state[keySet]['results'] = false //검색 결과 여부
        state[keySet]['list'].splice(0) //검색 목록 객체
        state[keySet]['total'] = 0 //전체 목록 수
        state[keySet]['action'] = null //액션
    }

    /**
     * 이력 저장소 옵션 초기화
     *
     * 옵션 속성 키에 해당하는 이력 정보를 초기화한다.
     */
    const initHistoryStoreOptions = () => {
        //키셋 설정
        const keySet = route.name
        //옵션 속성
        const options = state[keySet]['options']

        options['selectedCategory'] = null //선택된 업무별 카테고리 인덱스
        options['selectedAnalysis'] = [] //선택된 분석 목적
        options['selectedData'] = [] //선택된 데이터 기준
        options['selectedCustomer'] = [] //선택된 고객사 기준
        options['trendis'] = false //[활용장]Trendreport만 모아보기
    }

    /**
     * 이력 저장소 타겟마케팅 옵션 초기화
     *
     * 옵션 속성 키에 해당하는 이력 정보를 초기화한다.
     */
    const initHistoryStoreTarmktOptions = () => {
        //키셋 설정
        const keySet = route.name
        //타겟마케팅옵션 속성
        const tarmktOptions = state[keySet]['tarmktOption']

        tarmktOptions['tabs'] = '0' //누구에게 보내시나요?
        tarmktOptions['targetTendency'] = [] //이용성향
        tarmktOptions['targetFamily'] = [] //가구형태
        tarmktOptions['targetSex'] = [] //성별 (자유롭게 선택하기)
        tarmktOptions['targetAge'] = [] //연령대 (자유롭게 선택하기)
        tarmktOptions['targetAmount'] = [] //월 이용금액 (자유롭게 선택하기)
        tarmktOptions['targetArea']['si'] = null //거주지 (자유롭게 선택하기) 광역시도
        tarmktOptions['targetArea']['do'] = null //거주지 (자유롭게 선택하기) 시군구
        tarmktOptions['targetArea']['selected'] = [] //거주지 (자유롭게 선택하기) 선택한 거주지
        tarmktOptions['target'] = null //타겟 (많이 이용하는 타겟)
        tarmktOptions['targetSexI'] = [] //성별 (많이 이용하는 타겟)
        tarmktOptions['targetAgeI'] = [] //연령대 (많이 이용하는 타겟)
        tarmktOptions['targetAmountI'] = [] //월 이용금액 (많이 이용하는 타겟)
        tarmktOptions['targetAreaI']['si'] = null //거주지 (많이 이용하는 타겟) 광역시도
        tarmktOptions['targetAreaI']['do'] = null //거주지 (많이 이용하는 타겟) 시군구
        tarmktOptions['targetAreaI']['selected'] = [] //거주지 (많이 이용하는 타겟) 선택한 거주지
        tarmktOptions['sending'] = {} //발송매체
    }

    /**
     * 데이터 존재 여부
     *
     * 페이지 객체를 기준으로 데이터 존재 여부를 반환한다.
     *
     * @param {object<any>} page
     * @returns 데이터 존재 여부를 반환한다.
     */
    const isMoreData = (page) => {
        //결과
        let result = true

        //더보기 버튼 설정
        if (page) {
            const totalCount = Number(page.totalCount)
            const pageIndex = Number(page.pageIndex)
            const rowsPerPage = Number(page.rowsPerPage)

            //전체 건수와 현재 보여진 건수 비교
            if (totalCount <= pageIndex * rowsPerPage) {
                result = false
            }
        }

        return result
    }

    /**
     * 선택된 검색 값들
     *
     * 다중 선택된 조건의 인덱스를 기준으로 조건에 활용할 실제 값들을 반환한다.
     * @returns 다중 선택된 조건의 실제 값들을 반환한다.
     */
    const selectedSearchValues = () => {
        //키셋 설정
        const keySet = route.name
        //반환 값
        const result = {}
        //속성 값
        let property = null

        //옵션 속성에 속한 키와 값
        for (const [key, value] of Object.entries(state[keySet]['options'])) {
            switch (key) {
                //카테고리
                case 'selectedCategory':
                    property = 'category'
                    break
                //분석목적
                case 'selectedAnalysis':
                    property = 'analysis'
                    break
                //데이터기준
                case 'selectedData':
                    property = 'data'
                    break
                //고객사기준
                case 'selectedCustomer':
                    property = 'customer'
                    break
                default:
                    property = null
            }

            //선택된 값에 해당하는 코드 값을 추가
            if (property === 'category') {
                //반환될 실제 값
                result[key] = null

                //카테고리, 0이 아닌 경우
                if (value) {
                    result[key] = state[keySet]['options'][property][value].productTypeCode
                }
            } else if (property) {
                //대상 속성 값이 존재하면
                //반환될 실제 값
                result[key] = []

                //분석목적/데이터기준/고객사기준
                state[keySet]['options'][property].forEach((item) => {
                    if (item.checked) {
                        result[key].push(item.code)
                    }
                })
            }
        }

        return result
    }

    return {
        state,
        createHistoryStore,
        initHistoryStore,
        initHistoryStoreOptions,
        initHistoryStoreTarmktOptions,
        isMoreData,
        selectedSearchValues
    }
})

export default useHistoryStore
