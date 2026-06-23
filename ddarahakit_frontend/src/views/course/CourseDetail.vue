<script setup>
import { onMounted, ref, reactive, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import useAuthStore from '@/stores/useAuthStore'
import api from '@/api/course'
import cartApi from '@/api/cart'
import reviewApi from '@/api/review'
import communityApi from '@/api/community'
import commonUtil from '@/utils/commonUtil'
import UserAvatar from '@/components/base/UserAvatar.vue'
import { formatPrice } from '@/utils/price'
import { getAverageRating, getRatingWidth } from '@/utils/ratingUtil'
import useCategoryStore from '@/stores/useCategoryStore'
import QuillEditor from '@/components/base/QuillEditor.vue'

//라우트 정보 객체
const route = useRoute();
const router = useRouter();

//권한 저장소 정보 객체
const authStore = useAuthStore();

//카테고리 저장소 정보 객체
const categoryStore = useCategoryStore();


//코스 목록 리스트 정보 객체
const course = ref({
    idx: 0,
    name: '',
    image: "",
    description: "",
    originalPrice: 0,
    salePrice: 0,
    totalOrderedCount: 0,
    starsAvg: "",
    sections: [],
    totalReviewsCount: 0,
    rating1: 0,
    rating2: 0,
    rating3: 0,
    rating4: 0,
    rating5: 0,
    reviews: {},
    isReviewed: false,
    isOrdered: false,
    nextLectureIdx: 0
})

// 로딩 상태
const isLoading = ref(true)

//코스 Idx
const courseIdx = route.params.courseIdx




// 현재 활성화된 탭
const activeTab = ref('intro')

// 아코디언 열림 상태 (섹션 인덱스 배열)
const openSections = ref([])

/**
 * 아코디언 토글
 */
const toggleAccordion = (index) => {
    const idx = openSections.value.indexOf(index)
    if (idx > -1) {
        openSections.value.splice(idx, 1)
    } else {
        openSections.value.push(index)
    }
}

/**
 * 탭 클릭 시 해당 섹션으로 스크롤 이동
 */
const scrollToSection = (tabName) => {
    activeTab.value = tabName
    const sectionId = {
        'intro': 'section-intro',
        'curriculum': 'section-curriculum',
        'reviews': 'section-reviews',
        'qna': 'section-qna'
    }
    const element = document.getElementById(sectionId[tabName])
    if (element) {
        const offset = 120 // sticky 헤더 높이 보정
        const top = element.getBoundingClientRect().top + window.scrollY - offset
        window.scrollTo({ top, behavior: 'smooth' })
    }
}


/**
 * 폼 컨트롤 객체
 * 버튼 활성화 및 텍스트를 제어한다.
 */
const formCntrObj = reactive({
    noStyl: { text: 'text-tip error' },
    submitBtn: { disabled: true }
})

//리뷰 입력 객체
const reviewInput = reactive({
    comment: '', //리뷰 내용
    rating: 0, //리뷰 점수
})
const isEditing = ref(false); // 수정 모드 활성화 여부


//에러 객체 정보 객체
const reviewInputError = reactive({
    comment: {
        errorMessage: null,
        isValid: false
    },
    rating: {
        errorMessage: null,
        isValid: false
    }
})


/**
 * 코스 상세 조회
 */
const getCourseDetail = async () => {
    isLoading.value = true
    //API: 코스 상세 조회
    const data = await api.courseDetail(courseIdx)
    if (data.success) {
        if (data.results) {
            //조회 결과
            course.value = data.results
            openSections.value = (data.results.sections || []).map((_, index) => index)
            categoryStore.category = data.results.category
            document.title = `${course.value.name} | 따라학잇`;
        }
    }
    isLoading.value = false
}


/**
 * 리뷰 더 보기
 */
const getMoreReviews = async (courseIdx, page) => {
    //API: 리뷰 더 보기
    const data = await reviewApi.reviewList(courseIdx, page)
    if (data.success) {
        if (data.results) {
            //조회 결과
            course.value.reviews.list.push(...data.results.list)
            course.value.reviews.hasNext = data.results.hasNext
        }
    }
}

/**
 * 리뷰 점수 유효성 룰
 */
const reviewRatingRules = () => {
    if (reviewInput.rating !== 0) {
        reviewInputError.rating.errorMessage = null
        reviewInputError.rating.isValid = true
    } else {
        reviewInputError.rating.isValid = false
        reviewInputError.rating.errorMessage = '리뷰 점수를 입력해주세요.'
    }
}
/**
 * 리뷰 내용 유효성 룰
 */
const reviewCommentRules = () => {
    if (reviewInput.comment !== '' && reviewInput.comment !== '\n') {
        reviewInputError.comment.errorMessage = null
        reviewInputError.comment.isValid = true
    } else {
        reviewInputError.comment.errorMessage = '리뷰 내용을 입력해주세요.'
        reviewInputError.comment.isValid = false
    }
}

/**
 * 폼 유효성 검사
 */
const isFormValid = computed(() =>
    reviewInputError.comment.isValid && reviewInputError.rating.isValid
);

/**
 * 리뷰 작성하기
 */
// 리뷰 작성/수정/삭제 후 폼 초기화 (전체 새로고침 대신 입력 상태만 리셋)
const resetReviewForm = () => {
    reviewInput.comment = ''
    reviewInput.rating = 0
    reviewInputError.comment.errorMessage = null
    reviewInputError.comment.isValid = false
    reviewInputError.rating.errorMessage = null
    reviewInputError.rating.isValid = false
    isEditing.value = false
}

const reviewCreate = async () => {

    //API: 리뷰 작성하기
    const data = await reviewApi.reviewCreate(courseIdx, reviewInput)
    if (data.success) {
        if (data.results) {
            resetReviewForm()
            await getCourseDetail()
        }
    }
}

const reviewUpdate = async () => {
    //API: 리뷰 수정하기
    const data = await reviewApi.reviewUpdate(courseIdx, reviewInput)
    if (data.success) {
        resetReviewForm()
        await getCourseDetail()
    }
}


/**
 * 리뷰 수정하기
 */
const editReview = async (review) => {
    reviewInput.comment = review.comment
    reviewInput.rating = review.rating
    reviewInputError.comment.errorMessage = null
    reviewInputError.comment.isValid = true
    reviewInputError.rating.errorMessage = null
    reviewInputError.rating.isValid = true
    isEditing.value = true;
}


/**
 * 리뷰 삭제하기
 */
const deleteReview = async (courseIdx) => {
    //API: 리뷰 삭제하기
    const data = await reviewApi.reviewDelete(courseIdx)

    if (data.success) {
        //리뷰 삭제 후 폼 초기화 + 상세 재조회
        resetReviewForm()
        await getCourseDetail()
    }
}


const isPreviewModalOpen = ref(false)
const lecturePreviewObject = ref({
    complete: false,
    content: [],
    free: false,
    idx: 0,
    name: '',
    playTime: 0,
    videoUrl: ''
})

const previewLectureList = computed(() => {
    return (course.value.sections || [])
        .flatMap(section => section.lectures || [])
        .filter(lecture => lecture.free)
})

/**
 * 미리보기 모달 오픈
 */
const lecturePreview = (lectureIdx) => {
    const selectedLecture = previewLectureList.value.find(lecture => lecture.idx === lectureIdx)
    if (!selectedLecture) return
    lecturePreviewObject.value = selectedLecture
    isPreviewModalOpen.value = true
}

const selectPreviewLecture = (lecture) => {
    lecturePreviewObject.value = lecture
}

const previewVideo = ref(null)

/**
 * 미리보기 영상이 준비되면 자동 재생.
 * 소리 포함 자동재생이 브라우저 정책으로 막히면 음소거로라도 재생한다(사용자가 컨트롤로 해제 가능).
 */
const autoPlayVideo = (event) => {
    const video = event.target
    const playPromise = video.play()
    if (playPromise && typeof playPromise.catch === 'function') {
        playPromise.catch(() => {
            video.muted = true
            video.play().catch(() => { })
        })
    }
}

const closeLecturePreview = () => {
    // 모달 닫을 때 재생 중지(소리 잔존 방지). v-if 제거로도 멈추지만 안전망으로 명시 정지.
    if (previewVideo.value) {
        previewVideo.value.pause()
    }
    isPreviewModalOpen.value = false
}

//질문 목록
const questionList = ref([])

/**
 * 질문 목록 조회
 */
const getQuestionList = async () => {
    const data = await communityApi.postList({
        courseIdx: courseIdx,
        postType: 'QUESTION',
        size: 3
    })
    if (data && data.success && data.results) {
        questionList.value = data.results.posts || []
    }
}

/**
 * 질문하기 버튼 클릭
 */
const goToQuestionWrite = () => {
    if (!authStore.checkLogin()) {
        alert('로그인 후 이용할 수 있습니다.')
        router.push({ name: 'login', query: { redirect: route.fullPath } })
        return
    }
    router.push({ name: 'communityReg', query: { courseIdx: courseIdx } })
}

/**
 * 컴포넌트 마운트
 *
 */
onMounted(() => {
    getCourseDetail()
    getQuestionList()
    document.title = '강의 상세 | 따라학잇'

    if (route.query.tab) {
        const tab = route.query.tab;
        if (tab === 'reviews') {
            setTimeout(() => {
                scrollToSection('reviews')
            }, 500)
        }
    }
})

// 이미 장바구니에 추가된 코스 응답 코드
const CART_ALREADY_ADDED_CODE = 40006

const addCart = async () => {
    // 비로그인 시: 장바구니 추가는 인증이 필요하다. 인터셉터는 "세션이 있었을 때만" 로그인으로
    // 리다이렉트하므로(로그아웃 상태 반복 알림 방지), 비로그인 사용자는 여기서 직접 로그인 페이지로 보낸다.
    if (!authStore.isLogin) {
        router.push({ name: 'login', query: { redirect: route.fullPath } })
        return
    }

    // cartApi.cartAdd 는 then/catch 응답을 모두 동일 data 객체로 반환하며 code 가 보존된다.
    // (HTTP 200 success:false 든 4xx 든 data.code 로 분기 가능)
    const data = await cartApi.cartAdd(courseIdx)

    if (data.success) {
        // 정상 추가: 장바구니로 이동 (기존 동작 유지)
        router.push({ name: 'ordersCart' })
    } else if (data.code === CART_ALREADY_ADDED_CODE) {
        // 이미 담겨 있는 코스: 에러 알럿 없이 장바구니로 자연스럽게 이동
        router.push({ name: 'ordersCart' })
    }
    // 그 외 에러 코드는 기존과 동일하게 별도 처리하지 않는다.
}

</script>

<template>
    <!-- 스켈레톤 UI - 헤더 섹션 -->
    <section v-if="isLoading" class="pt-32 pb-12 bg-gray-900 text-white px-6">
        <div class="max-w-7xl mx-auto grid lg:grid-cols-3 gap-12 items-center">
            <div class="lg:col-span-2">
                <div class="flex gap-2 mb-6">
                    <div class="w-16 h-4 bg-gray-700 rounded skeleton-dark"></div>
                    <div class="w-20 h-4 bg-gray-700 rounded skeleton-dark"></div>
                </div>
                <div class="w-full h-10 bg-gray-700 rounded skeleton-dark mb-3"></div>
                <div class="w-3/4 h-10 bg-gray-700 rounded skeleton-dark mb-6"></div>
                <div class="flex flex-wrap items-center gap-6 mb-8">
                    <div class="w-32 h-5 bg-gray-700 rounded skeleton-dark"></div>
                    <div class="w-24 h-5 bg-gray-700 rounded skeleton-dark"></div>
                    <div class="w-28 h-5 bg-gray-700 rounded skeleton-dark"></div>
                </div>
                <div class="flex items-center gap-3">
                    <div class="w-10 h-10 rounded-full bg-gray-700 skeleton-dark"></div>
                    <div>
                        <div class="w-16 h-3 bg-gray-700 rounded skeleton-dark mb-2"></div>
                        <div class="w-20 h-4 bg-gray-700 rounded skeleton-dark"></div>
                    </div>
                </div>
            </div>
            <div class="hidden lg:block">
                <div class="aspect-video bg-gray-800 rounded-2xl skeleton-dark"></div>
            </div>
        </div>
    </section>

    <!-- 실제 헤더 섹션 -->
    <section v-else class="pt-32 pb-12 bg-gray-900 text-white px-6">
        <div class="max-w-7xl mx-auto grid lg:grid-cols-3 gap-12 items-center">
            <div class="lg:col-span-2">
                <nav class="flex gap-2 text-sm text-gray-400 mb-6">

                    <template v-for="(category, index) in course.category">
                        <a href="#" class="hover:text-white">{{ category.name }}</a>
                        <span v-if="index !== course.category.length - 1">/</span>
                    </template>

                </nav>
                <h1 class="text-3xl md:text-4xl font-extrabold mb-6 leading-tight">
                    <!-- 변수?.변수 처럼 앞의 변수에 값이 있을 때만 뒤에 변수를 사용하게 설정 -->
                    {{ course.name }}
                </h1>
                <div class="flex flex-wrap items-center gap-6 text-sm mb-8">
                    <div class="flex items-center gap-2">
                        <div :class="`rating_r rating_r_${Math.round(getAverageRating(course))}`">
                            <i class="fa-solid fa-star"></i><i class="fa-solid fa-star"></i><i
                                class="fa-solid fa-star"></i><i class="fa-solid fa-star"></i><i
                                class="fa-solid fa-star"></i>
                        </div>
                        <span class="font-bold">{{ getAverageRating(course) }}</span>
                        <span class="text-gray-400">({{ course.totalReviewsCount }}개의 수강평)</span>
                    </div>
                    <div class="flex items-center gap-2">
                        <i class="fa-solid fa-users text-gray-400"></i>
                        <span class="font-bold">{{ course.totalOrderedCount }} 명</span> 수강 중
                    </div>
                    <div class="flex items-center gap-2">
                        <i class="fa-solid fa-circle-check text-green-400"></i>
                        <span>최근 업데이트: {{ course.updatedAt  }}</span>
                    </div>
                </div>
                <div class="flex items-center gap-3">
                    <div class="w-10 h-10 rounded-full bg-brand flex items-center justify-center font-bold">
                        JS
                    </div>
                    <div>
                        <p class="text-xs text-gray-400 tracking-wide uppercase">지식공유자</p>
                        <p class="font-bold">따라학IT</p>
                    </div>
                </div>
            </div>
            <div class="hidden lg:block relative group">
                <div
                    class="aspect-video bg-gray-800 rounded-2xl overflow-hidden border border-gray-700 flex items-center justify-center relative">
                    <i class="fa-solid fa-play text-5xl text-white/50 group-hover:scale-110 transition-transform"></i>
                    <div class="absolute inset-0 bg-black/20 group-hover:bg-black/10 transition-colors"></div>
                    <p
                        class="absolute bottom-4 left-4 text-xs font-medium text-white bg-black/40 px-3 py-1 rounded-full">
                        미리보기 강의 제공
                    </p>
                </div>
            </div>
        </div>
    </section>
    <!-- 스켈레톤 UI - 메인 콘텐츠 -->
    <main v-if="isLoading" class="pt-6 max-w-7xl mx-auto px-6 py-12 flex flex-col lg:flex-row gap-8">
        <div class="flex-grow lg:max-w-[calc(100%-400px)]">
            <!-- 탭 스켈레톤 -->
            <div class="flex items-center gap-8 border-b border-gray-100 mb-10">
                <div class="w-20 h-8 bg-gray-100 rounded skeleton"></div>
                <div class="w-20 h-8 bg-gray-100 rounded skeleton"></div>
                <div class="w-16 h-8 bg-gray-100 rounded skeleton"></div>
                <div class="w-20 h-8 bg-gray-100 rounded skeleton"></div>
            </div>
            <!-- 본문 스켈레톤 -->
            <div class="space-y-4 mb-12">
                <div class="w-full h-6 bg-gray-100 rounded skeleton"></div>
                <div class="w-full h-6 bg-gray-100 rounded skeleton"></div>
                <div class="w-3/4 h-6 bg-gray-100 rounded skeleton"></div>
                <div class="w-full h-6 bg-gray-100 rounded skeleton"></div>
                <div class="w-5/6 h-6 bg-gray-100 rounded skeleton"></div>
            </div>
            <!-- 커리큘럼 스켈레톤 -->
            <div class="mt-12">
                <div class="w-40 h-8 bg-gray-100 rounded skeleton mb-6"></div>
                <div class="border border-gray-100 rounded-2xl overflow-hidden divide-y divide-gray-100">
                    <div v-for="n in 3" :key="n" class="p-6">
                        <div class="w-48 h-5 bg-gray-100 rounded skeleton mb-4"></div>
                        <div class="space-y-3">
                            <div class="w-full h-4 bg-gray-50 rounded skeleton"></div>
                            <div class="w-full h-4 bg-gray-50 rounded skeleton"></div>
                            <div class="w-3/4 h-4 bg-gray-50 rounded skeleton"></div>
                        </div>
                    </div>
                </div>
            </div>
        </div>
        <!-- 사이드바 스켈레톤 -->
        <aside class="w-full lg:w-[360px] flex-shrink-0">
            <div
                class="sticky sticky-sidebar bg-white border border-gray-100 rounded-3xl shadow-2xl shadow-blue-100/50 overflow-hidden">
                <div class="aspect-video bg-gray-100 skeleton"></div>
                <div class="p-8">
                    <div class="flex items-baseline gap-2 mb-6">
                        <div class="w-28 h-8 bg-gray-100 rounded skeleton"></div>
                        <div class="w-16 h-4 bg-gray-100 rounded skeleton"></div>
                    </div>
                    <div class="space-y-3 mb-8">
                        <div class="w-full h-14 bg-gray-100 rounded-xl skeleton"></div>
                        <div class="w-full h-14 bg-gray-100 rounded-xl skeleton"></div>
                    </div>
                    <div class="space-y-4 border-t border-gray-50 pt-6">
                        <div class="flex justify-between">
                            <div class="w-20 h-4 bg-gray-100 rounded skeleton"></div>
                            <div class="w-16 h-4 bg-gray-100 rounded skeleton"></div>
                        </div>
                        <div class="flex justify-between">
                            <div class="w-20 h-4 bg-gray-100 rounded skeleton"></div>
                            <div class="w-12 h-4 bg-gray-100 rounded skeleton"></div>
                        </div>
                        <div class="flex justify-between">
                            <div class="w-20 h-4 bg-gray-100 rounded skeleton"></div>
                            <div class="w-16 h-4 bg-gray-100 rounded skeleton"></div>
                        </div>
                    </div>
                </div>
            </div>
        </aside>
    </main>

    <main v-else class="pt-6 max-w-7xl mx-auto px-6 py-12 flex flex-col lg:flex-row gap-8">
        <!-- 왼쪽 상세 설명 -->
        <div class="flex-grow lg:max-w-[calc(100%-400px)]">
            <!-- 탭 메뉴 -->
            <div class="flex items-center gap-4 md:gap-8 border-b border-gray-100 mb-10 top-[4.5rem] bg-white z-10 overflow-x-auto">
                <button @click="scrollToSection('intro')" class="pt-4 pb-4 text-md font-bold tab-btn shrink-0 whitespace-nowrap"
                    :class="activeTab === 'intro' ? 'active' : 'text-gray-400 hover:text-gray-600'">
                    강의 소개
                </button>
                <button @click="scrollToSection('curriculum')" class="pt-4 pb-4 text-md font-bold tab-btn shrink-0 whitespace-nowrap"
                    :class="activeTab === 'curriculum' ? 'active' : 'text-gray-400 hover:text-gray-600'">
                    커리큘럼
                </button>
                <button @click="scrollToSection('reviews')" class="pt-4 pb-4 text-md font-bold tab-btn shrink-0 whitespace-nowrap"
                    :class="activeTab === 'reviews' ? 'active' : 'text-gray-400 hover:text-gray-600'">
                    수강평
                </button>
                <button @click="scrollToSection('qna')" class="pt-4 pb-4 text-md font-bold tab-btn shrink-0 whitespace-nowrap"
                    :class="activeTab === 'qna' ? 'active' : 'text-gray-400 hover:text-gray-600'">
                    질문답변
                </button>
            </div>

            <!-- 강의 소개 섹션 -->
            <section id="section-intro" class="prose max-w-none text-gray-600 leading-loose space-y-8">
                <div>
                    <QuillEditor v-if="course.description" :read-only="true" :initial-content="course.description" />
                </div>
            </section>

            <!-- 커리큘럼 섹션 (아코디언 적용) -->
            <section id="section-curriculum" class="mt-12">
                <h2 class="text-2xl font-bold text-gray-900 mb-6">커리큘럼 📋</h2>
                <div class="border border-gray-100 rounded-2xl overflow-hidden shadow-sm divide-y divide-gray-100">
                    <!-- 섹션 아코디언 -->
                    <div v-for="(section, index) in course.sections" :key="index" class="accordion-item"
                        :class="{ active: openSections.includes(index) }">
                        <button @click="toggleAccordion(index)"
                            class="w-full bg-gray-50 px-6 py-5 font-bold text-gray-900 flex justify-between items-center text-left hover:bg-gray-100 transition-colors">
                            <div class="flex flex-col sm:flex-row sm:items-center gap-2">
                                <span>섹션 {{ index + 1 }}. {{ section.name }}</span>
                                <span class="text-[11px] text-gray-400 font-medium">
                                    {{ section.lectures.length }}강 ∙
                                    {{commonUtil.formattedPlayTime(section.lectures.reduce((sum, lecture) => sum +
                                    lecture.playTime, 0)) }}
                                </span>
                            </div>
                            <i class="fa-solid fa-chevron-down accordion-icon text-gray-400"></i>
                        </button>
                        <div class="accordion-content">
                            <div class="divide-y divide-gray-50 bg-white">
                                <RouterLink v-for="(lecture, lectureIndex) in section.lectures" :key="lectureIndex"
                                    :to="`/lecture/${course.idx}/${lecture.idx}`"
                                    class="px-6 py-4 flex items-center justify-between curriculum-item cursor-pointer">
                                    <div class="flex items-center gap-4">
                                        <i class="fa-regular fa-circle-play text-brand"></i>
                                        <span class="text-sm">{{ lecture.name }}</span>
                                    </div>

                                    <div class="flex items-center gap-2">
                                        <span class="text-xs text-gray-400 font-medium">
                                            {{ commonUtil.formattedPlayTime(lecture.playTime) }}
                                        </span>
                                        <span v-if="lecture.free"
                                            class="text-xs text-brand font-bold bg-blue-50 px-2 py-1 rounded font-mono"
                                            @click.prevent="lecturePreview(lecture.idx)">미리보기</span>
                                    </div>
                                </RouterLink>
                            </div>
                        </div>
                    </div>
                </div>
            </section>

            <!-- 수강생 후기 -->
            <section id="section-reviews" class="mt-12">
                <h2 class="text-2xl font-bold text-gray-900 mb-6">수강생 리얼 후기 ⭐</h2>

                <!-- 리뷰 작성/수정 폼 (수강한 사용자만, 미작성이거나 수정 중일 때) -->
                <div v-if="course.ordered && (!course.reviewed || isEditing)"
                    class="p-6 bg-white border border-gray-100 rounded-2xl shadow-sm mb-6">
                    <h3 class="font-bold text-gray-900 mb-4">{{ isEditing ? '내 후기 수정' : '수강 후기 작성' }}</h3>
                    <!-- 별점 선택 -->
                    <div class="flex items-center gap-1">
                        <button v-for="n in 5" :key="n" type="button"
                            @click="reviewInput.rating = n; reviewRatingRules()"
                            class="text-2xl transition-colors"
                            :class="n <= reviewInput.rating ? 'text-yellow-400' : 'text-gray-300 hover:text-yellow-200'"
                            :aria-label="`별점 ${n}점`">
                            <i class="fa-solid fa-star"></i>
                        </button>
                        <span class="ml-2 text-sm text-gray-400">{{ reviewInput.rating > 0 ? `${reviewInput.rating}점` : '' }}</span>
                    </div>
                    <p v-if="reviewInputError.rating.errorMessage" class="text-red-500 text-xs mt-1">{{ reviewInputError.rating.errorMessage }}</p>
                    <!-- 내용 -->
                    <textarea v-model="reviewInput.comment" @input="reviewCommentRules" @blur="reviewCommentRules"
                        rows="4" maxlength="500" placeholder="수강 후기를 남겨주세요."
                        class="w-full mt-3 px-4 py-3 rounded-xl border border-gray-200 text-sm resize-none focus:outline-none focus:border-brand"></textarea>
                    <p v-if="reviewInputError.comment.errorMessage" class="text-red-500 text-xs mt-1">{{ reviewInputError.comment.errorMessage }}</p>
                    <!-- 버튼 -->
                    <div class="flex justify-end gap-2 mt-4">
                        <button v-if="isEditing" type="button" @click="resetReviewForm"
                            class="px-5 py-2.5 text-sm text-gray-500 rounded-xl hover:bg-gray-100 transition-all">취소</button>
                        <button type="button" :disabled="!isFormValid"
                            @click="isEditing ? reviewUpdate() : reviewCreate()"
                            class="px-6 py-2.5 bg-brand text-white rounded-xl text-sm font-bold shadow-lg shadow-blue-100 disabled:opacity-40 disabled:cursor-not-allowed hover:opacity-90 transition-all">
                            {{ isEditing ? '수정' : '작성' }}
                        </button>
                    </div>
                </div>

                <!-- 이미 작성한 후기: 수정/삭제 -->
                <div v-else-if="course.ordered && course.reviewed"
                    class="flex items-center justify-between p-4 bg-blue-50/50 border border-blue-100 rounded-2xl mb-6">
                    <p class="text-sm text-gray-600"><i class="fa-solid fa-circle-check text-brand mr-2"></i>이미 후기를 작성하셨습니다.</p>
                    <div class="flex gap-2">
                        <button type="button" @click="editReview(course.reviews.list[0])"
                            class="px-4 py-2 text-sm font-bold text-brand border border-brand/30 rounded-xl hover:bg-brand/5 transition-all">수정</button>
                        <button type="button" @click="deleteReview(courseIdx)"
                            class="px-4 py-2 text-sm font-bold text-red-400 border border-red-100 rounded-xl hover:bg-red-50 transition-all">삭제</button>
                    </div>
                </div>

                <div class="grid gap-6">
                    <div v-for="(review, index) in course.reviews.list" :key="index"
                        class="p-6 bg-white border border-gray-100 rounded-2xl shadow-sm">
                        <div class="flex justify-between items-center mb-4">
                            <div class="flex items-center gap-3">
                                <div class="w-10 h-10 rounded-full overflow-hidden">
                                    <UserAvatar :src="review.userProfileImageUrl" alt="" icon-class="text-sm" />
                                </div>
                                <div>
                                    <p class="text-sm font-bold text-gray-900">{{ review.userName }}</p>

                                    <div :class="`flex text-yellow-400 text-[10px] rating_r rating_r_${review.rating}`">
                                        <i class="fa-solid fa-star"></i><i class="fa-solid fa-star"></i><i
                                            class="fa-solid fa-star"></i><i class="fa-solid fa-star"></i><i
                                            class="fa-solid fa-star"></i>
                                    </div>
                                </div>
                            </div>
                            <span class="text-xs text-gray-400">{{ review.createdAt }}</span>
                        </div>
                        <p class="text-sm text-gray-600 leading-relaxed">
                            {{ review.comment }}
                        </p>
                    </div>

                    <!-- 리뷰가 없을 때 -->
                    <div v-if="!course.reviews.list || course.reviews.list.length === 0"
                        class="text-center py-12 text-gray-400">
                        <i class="fa-regular fa-comment-dots text-4xl mb-4"></i>
                        <p>아직 수강평이 없습니다.</p>
                    </div>
                </div>
            </section>

            <!-- 질문답변 섹션 -->
            <section id="section-qna" class="mt-12">
                <h2 class="text-2xl font-bold text-gray-900 mb-6">질문답변 💬</h2>
                <div class="space-y-4">
                    <!-- 질문 입력 영역 -->
                    <div class="p-6 bg-white border border-gray-100 rounded-2xl shadow-sm">
                        <div class="flex items-center gap-3 mb-4">
                            <div
                                class="w-10 h-10 rounded-full bg-brand flex items-center justify-center text-white font-bold text-sm">
                                Q
                            </div>
                            <span class="font-bold text-gray-900">궁금한 점이 있으신가요?</span>
                        </div>
                        <p class="text-sm text-gray-500 mb-4">
                            강의 내용에 대한 질문을 남겨주세요. 지식공유자가 답변을 드립니다.
                        </p>
                        <button @click="goToQuestionWrite"
                            class="px-6 py-3 bg-brand text-white rounded-xl font-bold text-sm hover:opacity-90 transition-all">
                            <i class="fa-solid fa-pen-to-square mr-2"></i>
                            질문하기
                        </button>
                    </div>

                    <!-- 질문 목록 -->
                    <RouterLink v-for="question in questionList" :key="question.idx"
                        :to="`/community/${question.idx}`"
                        class="block p-6 bg-white border border-gray-100 rounded-2xl shadow-sm hover:border-brand transition-colors">
                        <div class="flex items-start gap-4">
                            <div
                                class="w-10 h-10 rounded-full flex items-center justify-center font-bold text-sm shrink-0"
                                :class="question.commentCount > 0 ? 'bg-blue-50 text-brand' : 'bg-gray-100 text-gray-500'">
                                Q
                            </div>
                            <div class="flex-grow min-w-0">
                                <div class="flex justify-between items-start mb-2 gap-2">
                                    <h4 class="font-bold text-gray-900 truncate">{{ question.title }}</h4>
                                    <span v-if="question.commentCount > 0"
                                        class="text-[10px] text-brand font-bold bg-blue-50 px-2 py-1 rounded whitespace-nowrap">답변
                                        완료</span>
                                    <span v-else
                                        class="text-[10px] text-gray-400 font-bold bg-gray-100 px-2 py-1 rounded whitespace-nowrap">답변
                                        대기</span>
                                </div>
                                <p v-if="question.text" class="text-sm text-gray-600 mb-3 line-clamp-2">
                                    {{ question.text }}
                                </p>
                                <div class="flex items-center gap-4 text-[11px] text-gray-400">
                                    <span>{{ question.userName }}</span>
                                    <span>•</span>
                                    <span>{{ question.createdAt }}</span>
                                    <span>•</span>
                                    <span class="flex items-center gap-1">
                                        <i class="fa-regular fa-comment"></i> {{ question.commentCount }}
                                    </span>
                                </div>
                            </div>
                        </div>
                    </RouterLink>

                    <!-- 질문이 없을 때 -->
                    <div v-if="questionList.length === 0"
                        class="text-center py-12 text-gray-400">
                        <i class="fa-regular fa-comment-dots text-4xl mb-4"></i>
                        <p>아직 질문이 없습니다.</p>
                    </div>

                    <!-- 더보기 버튼 -->
                    <div v-if="questionList.length > 0" class="text-center pt-4">
                        <RouterLink :to="`/community?courseIdx=${courseIdx}`"
                            class="inline-block px-6 py-3 border border-gray-200 rounded-xl text-sm font-medium text-gray-600 hover:bg-gray-50 transition-colors">
                            질문 더보기
                        </RouterLink>
                    </div>
                </div>
            </section>
        </div>

        <!-- 오른쪽 구매 사이드바 (모바일에서는 상단으로 이동, sticky 해제) -->
        <aside class="w-full lg:w-[360px] flex-shrink-0 order-first lg:order-none">
            <div
                class="lg:sticky sticky-sidebar bg-white border border-gray-100 rounded-3xl shadow-2xl shadow-blue-100/50 overflow-hidden">
                <div class="aspect-video bg-blue-50 flex items-center justify-center border-b border-gray-100">
                    <img class="w-full h-full object-cover" :src="`${course.image}`">
                </div>
                <div class="p-8">
                    <div class="flex items-baseline gap-2 mb-6">
                        <span class="text-3xl font-extrabold text-gray-900">{{ formatPrice(course.salePrice) }}</span>
                        <template v-if="course.originalPrice > course.salePrice">
                            <span class="text-gray-400 line-through text-sm">₩{{ course.originalPrice.toLocaleString()
                            }}</span>
                            <span class="text-red-500 font-bold text-sm ml-auto">{{ Math.ceil((course.originalPrice -
                                course.salePrice) / course.originalPrice * 100) }}% 할인</span>
                        </template>
                    </div>
                    <div class="space-y-3 mb-8">

                        <button
                            v-if="course.ordered && course.sections.flatMap(section => section.lectures).filter(lecture => lecture.complete).length === 0"
                            @click="router.push({ name: 'lecture', params: { courseIdx: courseIdx, lectureIdx: course.nextLectureIdx } })"
                            class="w-full py-4 bg-brand text-white rounded-xl font-bold text-lg hover:opacity-90 transition-all shadow-lg shadow-blue-200">
                            강의 입장하기
                        </button>

                        <button v-else-if="course.ordered"
                            @click="router.push({ name: 'lecture', params: { courseIdx: courseIdx, lectureIdx: course.nextLectureIdx } })"
                            class="w-full py-4 bg-brand text-white rounded-xl font-bold text-lg hover:opacity-90 transition-all shadow-lg shadow-blue-200">
                            이어서 수강하기
                        </button>

                        <button v-else @click="addCart"
                            class="w-full py-4 bg-brand text-white rounded-xl font-bold text-lg hover:opacity-90 transition-all shadow-lg shadow-blue-200">
                            수강 신청하기
                        </button>
                    </div>
                    <div class="space-y-4 border-t border-gray-50 pt-6">
                        <div class="flex items-center justify-between text-sm">
                            <span class="text-gray-500">강의 수준</span>
                            <span class="text-gray-900 font-medium">{{ course.levelDescription || '미정' }}</span>
                        </div>
                        <div class="flex items-center justify-between text-sm">
                            <span class="text-gray-500">총 강의 수</span>
                            <span class="text-gray-900 font-medium">{{course.sections.reduce((sum,
                                section) => sum + section.lectures.length, 0)}}강</span>
                        </div>
                        <div class="flex items-center justify-between text-sm">
                            <span class="text-gray-500">총 강의 시간</span>
                            <span
                                class="text-gray-900 font-medium">{{commonUtil.formattedPlayTime(course.sections.reduce((sum,
                                    section) =>
                                    sum + section.lectures.reduce((sum, lecture) => sum + lecture.playTime,
                                        0), 0))}}</span>
                        </div>
                        <div class="flex items-center justify-between text-sm text-brand font-bold">
                            <span class="text-gray-500 font-normal">수강 기간</span>
                            <span>무제한 소장</span>
                        </div>

                    </div>
                </div>
            </div>
        </aside>
    </main>

    <div id="previewModal" :class="{ active: isPreviewModalOpen }"
        class="fixed inset-0 z-[100] items-center justify-center p-4 sm:p-6" @click.self="closeLecturePreview">
        <div class="absolute inset-0 bg-gray-900/60 backdrop-blur-md" @click="closeLecturePreview"></div>

        <div class="modal-container relative w-full max-w-5xl bg-white rounded-3xl overflow-hidden shadow-[0_20px_50px_rgba(0,0,0,0.15)] flex flex-col lg:flex-row">
            <div class="flex-grow bg-gray-100 relative aspect-video lg:aspect-auto flex items-center justify-center">
                <video v-if="isPreviewModalOpen && lecturePreviewObject.videoUrl" ref="previewVideo"
                    :src="lecturePreviewObject.videoUrl" @loadeddata="autoPlayVideo"
                    class="absolute inset-0 w-full h-full object-contain bg-black" controls autoplay playsinline />

                <div v-else class="absolute inset-0 bg-gray-200 flex items-center justify-center">
                    <div class="flex flex-col items-center gap-4 text-gray-400">
                        <div class="w-20 h-20 bg-white rounded-full flex items-center justify-center shadow-lg">
                            <i class="fa-solid fa-play text-brand text-3xl ml-1"></i>
                        </div>
                        <p class="font-medium">강의 영상을 준비 중입니다.</p>
                    </div>
                </div>

                <button @click="closeLecturePreview"
                    class="absolute top-4 left-4 lg:hidden w-10 h-10 bg-white/80 backdrop-blur rounded-full flex items-center justify-center text-gray-600 shadow-md">
                    <i class="fa-solid fa-xmark"></i>
                </button>
            </div>

            <div class="w-full lg:w-80 bg-white flex flex-col h-[400px] lg:h-[540px] border-l border-gray-100">
                <div class="p-6 border-b border-gray-50 flex justify-between items-center">
                    <div class="flex items-center gap-2">
                        <i class="fa-solid fa-list-ul text-brand text-sm"></i>
                        <h3 class="text-gray-900 font-bold">무료 미리보기</h3>
                    </div>
                    <button @click="closeLecturePreview"
                        class="hidden lg:flex w-8 h-8 items-center justify-center bg-gray-50 hover:bg-gray-100 text-gray-400 rounded-full transition-colors">
                        <i class="fa-solid fa-xmark text-sm"></i>
                    </button>
                </div>

                <div class="flex-grow overflow-y-auto p-3 custom-scrollbar">
                    <div v-for="lecture in previewLectureList" :key="lecture.idx"
                        @click="selectPreviewLecture(lecture)"
                        class="p-4 rounded-2xl mb-3 flex gap-3 cursor-pointer group transition-all border"
                        :class="lecture.idx === lecturePreviewObject.idx
                            ? 'bg-blue-50/50 border-blue-100'
                            : 'bg-white border-transparent hover:border-gray-100 hover:bg-gray-50'">
                        <div
                            class="relative w-20 h-12 rounded-lg flex items-center justify-center overflow-hidden flex-shrink-0"
                            :class="lecture.idx === lecturePreviewObject.idx ? 'bg-blue-100/50' : 'bg-gray-100'">
                            <i class="fa-solid fa-play text-xs"
                                :class="lecture.idx === lecturePreviewObject.idx ? 'text-brand' : 'text-gray-400'"></i>
                        </div>
                        <div class="flex flex-col justify-center min-w-0">
                            <p class="text-[13px] font-bold truncate"
                                :class="lecture.idx === lecturePreviewObject.idx ? 'text-gray-900' : 'text-gray-600'">
                                {{ lecture.name }}
                            </p>
                            <div class="flex items-center gap-2 mt-1">
                                <span v-if="lecture.idx === lecturePreviewObject.idx"
                                    class="text-[10px] bg-brand text-white px-1.5 py-0.5 rounded-sm font-bold uppercase">Playing</span>
                                <span class="text-[11px] text-gray-400 font-mono">
                                    {{ commonUtil.formattedPlayTime(lecture.playTime) }}
                                </span>
                            </div>
                        </div>
                    </div>
                </div>

                <div class="p-5 bg-gray-50/50 border-t border-gray-50">
                    <button @click="addCart"
                        class="w-full py-4 bg-brand text-white rounded-xl font-bold text-sm shadow-lg shadow-blue-100 hover:scale-[1.02] active:scale-100 transition-all">
                        지금 바로 수강 신청
                    </button>
                </div>
            </div>
        </div>
    </div>

</template>

<style scoped>
.ql-container.ql-snow {
    border: none;
}

.sticky-sidebar {
    top: 100px;
}

.curriculum-item:hover {
    background-color: #f9fafb;
}

.tab-btn.active {
    color: #14bced;
    border-bottom: 3px solid #14bced;
}

/* 아코디언 애니메이션 스타일 */
.accordion-content {
    max-height: 0;
    overflow: hidden;
    transition: max-height 0.3s ease-out;
}

.accordion-item.active .accordion-content {
    max-height: 2000px;
}

.accordion-icon {
    transition: transform 0.3s ease;
}

.accordion-item.active .accordion-icon {
    transform: rotate(180deg);
}

.line-clamp-2 {
    display: -webkit-box;
    -webkit-line-clamp: 2;
    -webkit-box-orient: vertical;
    overflow: hidden;
}

#previewModal {
    display: none;
    opacity: 0;
    transition: opacity 0.3s ease;
}

#previewModal.active {
    display: flex;
    opacity: 1;
}

.modal-container {
    transform: scale(0.95);
    transition: transform 0.3s ease;
}

#previewModal.active .modal-container {
    transform: scale(1);
}

.custom-scrollbar::-webkit-scrollbar {
    width: 4px;
}

.custom-scrollbar::-webkit-scrollbar-thumb {
    background: #e2e8f0;
    border-radius: 10px;
}

/* 스켈레톤 애니메이션 */
.skeleton {
    background: linear-gradient(90deg, #f0f0f0 25%, #e0e0e0 50%, #f0f0f0 75%);
    background-size: 200% 100%;
    animation: skeleton-loading 1.5s infinite;
}

.skeleton-dark {
    background: linear-gradient(90deg, #374151 25%, #4b5563 50%, #374151 75%);
    background-size: 200% 100%;
    animation: skeleton-loading 1.5s infinite;
}

@keyframes skeleton-loading {
    0% {
        background-position: 200% 0;
    }

    100% {
        background-position: -200% 0;
    }
}
</style>
