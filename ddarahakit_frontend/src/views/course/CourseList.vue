<script setup>
import { computed, onMounted, reactive, ref, watch } from 'vue'
import api from '@/api/course'
import { useRoute, useRouter } from 'vue-router'
import useCategoryStore from '@/stores/useCategoryStore'
import { formatPrice } from '@/utils/price'

//라우트 정보 객체
const route = useRoute();
const router = useRouter();

//카테고리 슬러그
const getCategorySlug = () => route.params.categorySlug;

//카테고리 저장소 정보 객체
const categoryStore = useCategoryStore();

//카테고리 목록 정보 객체
const categories = ref([])

//코스 목록 리스트 정보 객체
let courseList = reactive([])

// 로딩 상태
const isLoading = ref(true)
// 페이지 크기. URL 쿼리 ?size= 로 덮어쓸 수 있다(기본 9).
const DEFAULT_PAGE_SIZE = 9
const pageSize = ref(DEFAULT_PAGE_SIZE)
const currentPage = ref(1)

// 정렬/필터 상태
const sortBy = ref('popular') // popular(인기순) | newest(최신순) | rating(평점순)
const filters = reactive({ free: false, discount: false, reviewed: false })
const selectedLevels = ref([]) // ['초급','중급','고급'] 중 체크된 난이도

/**
 * 코스 목록 조회
 */
const getCourseList = async () => {
    isLoading.value = true
    courseList.splice(0)
    //API: 키워드 검색이면 검색, 아니면 (카테고리)목록 조회
    const keyword = route.query.keyword
    const data = keyword
        ? await api.courseSearch(keyword)
        : await api.courseList(getCategorySlug())

    if (data && data.success) {
        //코스 목록 추가
        if (data.results) {
            //조회 결과
            const list = data.results.courses

            if (data.results.category) {
                categoryStore.category = data.results.category
                document.title = `${categoryStore.category.at(-1).name} | 따라학잇`
            }

            if (list?.length) {
                courseList.push(...list)
            }
        }
    } else {
        //코스 목록 초기화
        courseList.splice(0)
    }
    if (currentPage.value > totalPages.value) {
        currentPage.value = totalPages.value
    }
    isLoading.value = false
}

// 필터 + 정렬이 적용된 강의 목록
const filteredSortedCourses = computed(() => {
    const filtered = courseList.filter(c => {
        if (filters.free && c.salePrice !== 0) return false
        if (filters.discount && !(c.originalPrice > c.salePrice)) return false
        if (filters.reviewed && !(c.totalReviewsCount > 0)) return false
        if (selectedLevels.value.length && !selectedLevels.value.includes(c.level)) return false
        return true
    })
    const sorted = [...filtered]
    if (sortBy.value === 'newest') {
        // 등록일 필드가 없어 idx(자동증가)를 최신 기준으로 사용
        sorted.sort((a, b) => b.idx - a.idx)
    } else if (sortBy.value === 'rating') {
        sorted.sort((a, b) => (Number(getAverageRating(b)) - Number(getAverageRating(a))) || (b.totalReviewsCount - a.totalReviewsCount))
    } else {
        // 인기순(기본): 주문수 → idx
        sorted.sort((a, b) => (b.totalOrderedCount - a.totalOrderedCount) || (b.idx - a.idx))
    }
    return sorted
})

const totalCount = computed(() => filteredSortedCourses.value.length)

const totalPages = computed(() => {
    const pages = Math.ceil(totalCount.value / pageSize.value)
    return pages > 0 ? pages : 1
})

const pagedCourseList = computed(() => {
    const start = (currentPage.value - 1) * pageSize.value
    return filteredSortedCourses.value.slice(start, start + pageSize.value)
})

const pageNumbers = computed(() => {
    const maxVisible = 5
    const total = totalPages.value
    if (total <= maxVisible) {
        return Array.from({ length: total }, (_, i) => i + 1)
    }

    let start = Math.max(1, currentPage.value - 2)
    let end = Math.min(total, start + maxVisible - 1)
    if (end - start < maxVisible - 1) {
        start = Math.max(1, end - maxVisible + 1)
    }

    return Array.from({ length: end - start + 1 }, (_, i) => start + i)
})

const goToPage = (page) => {
    const target = Math.min(Math.max(Number(page) || 1, 1), totalPages.value)
    if (target === currentPage.value) return
    currentPage.value = target

    // 선택한 페이지 번호와 페이지 크기를 프론트 URL 쿼리에 반영한다
    // (공유/북마크/새로고침 시 동일 페이지 복원 가능).
    router.push({
        query: { ...route.query, page: String(target), size: String(pageSize.value) },
    })
}

const syncPageFromQuery = () => {
    // URL 쿼리(page, size)에서 현재 페이지와 페이지 크기를 복원한다.
    const page = Number(route.query.page)
    currentPage.value = Number.isFinite(page) && page > 0 ? page : 1

    const size = Number(route.query.size)
    pageSize.value = Number.isFinite(size) && size > 0 ? size : DEFAULT_PAGE_SIZE
}

// 난이도(level) 필터를 URL 쿼리(?level=초급,중급)에 반영/복원한다 (페이지네이션과 동일 패턴).
const LEVELS = ['초급', '중급', '고급']

// 체크된 난이도를 항상 LEVELS 순서로 정규화한 CSV. URL 값과 1:1 비교해 동기화 루프를 막는다.
const levelsCsv = computed(() => LEVELS.filter(l => selectedLevels.value.includes(l)).join(','))

const syncLevelsFromQuery = () => {
    // URL 쿼리(level)에서 체크된 난이도를 복원한다.
    const raw = route.query.level
    const str = Array.isArray(raw) ? raw.join(',') : (raw || '')
    const picked = str.split(',').map(s => s.trim()).filter(v => LEVELS.includes(v))
    // 유효값만, LEVELS 순서로 정규화해 저장
    selectedLevels.value = LEVELS.filter(l => picked.includes(l))
}

const getAverageRating = (course) => {
    if (course.totalReviewsCount === 0) return "0.0";
    return (
        Math.ceil((course.rating5 * 5 + course.rating4 * 4 + course.rating3 * 3 + course.rating2 * 2 + course.rating1) / course.totalReviewsCount * 10) / 10
    ).toFixed(1);
}

// 카테고리 강의 수: 자기 자신 + 하위 카테고리 합산 (부모 카테고리가 0 으로 보이는 문제 해결)
const categoryTotal = (cat) =>
    (cat.courseCount || 0) + (cat.children?.reduce((s, c) => s + (c.courseCount || 0), 0) || 0)

// 코스 카드 라벨: 가장 구체적인(leaf) 카테고리명
const courseCategoryName = (course) =>
    course.category?.[course.category.length - 1]?.name || ''

/**
 * 카테고리 목록 조회
 */
const getCategoryList = async () => {
    const data = await api.categoryList()
    if (data && data.success && data.results) {
        categories.value = data.results
    }
}

//현재 선택된 카테고리 슬러그
const activeSlug = computed(() => getCategorySlug() || null)

//전체 강의 수 (부모 + 자식 카테고리 courseCount 합산)
const totalCourseCount = computed(() => {
    let total = 0
    for (const cat of categories.value) {
        total += cat.courseCount || 0
        if (cat.children) {
            for (const child of cat.children) {
                total += child.courseCount || 0
            }
        }
    }
    return total
})

//현재 카테고리명
const activeCategoryName = computed(() => {
    if (route.query.keyword) return `'${route.query.keyword}' 검색 결과`
    if (!activeSlug.value) return '전체 강의'
    for (const cat of categories.value) {
        if (cat.slug === activeSlug.value) return cat.name
        if (cat.children) {
            const child = cat.children.find(c => c.slug === activeSlug.value)
            if (child) return child.name
        }
    }
    return '전체 강의'
})

onMounted(() => {
    //카테고리 목록 조회
    getCategoryList()
    //코스 목록 조회
    syncPageFromQuery()
    syncLevelsFromQuery()
    getCourseList()

})

watch(
    () => [route.query.page, route.query.size],
    () => {
        syncPageFromQuery()
    }
)

// URL 쿼리 level 변경(뒤로가기/공유 링크/카테고리 이동 등) → 체크박스 상태 복원
watch(
    () => route.query.level,
    () => {
        syncLevelsFromQuery()
    }
)

// 체크박스로 난이도 변경 → URL 쿼리에 반영(+1페이지로 리셋). URL 을 단일 소스로 두어 공유/북마크/새로고침 시 복원된다.
watch(levelsCsv, (csv) => {
    const current = Array.isArray(route.query.level) ? route.query.level.join(',') : (route.query.level || '')
    if (current === csv) return // URL 과 동일하면 무시(동기화 루프 방지)
    const query = { ...route.query, page: '1' }
    if (csv) query.level = csv
    else delete query.level
    router.push({ query })
})

watch(
    () => route.params.categorySlug,
    () => {
        currentPage.value = 1
        getCourseList()
    }
)

// 검색 키워드 변경 시 재조회
watch(
    () => route.query.keyword,
    () => {
        currentPage.value = 1
        getCourseList()
    }
)

// 정렬/필터 변경 시 1페이지로 리셋 (난이도는 URL 반영 watch 에서 page=1 처리하므로 제외)
watch(
    () => [sortBy.value, filters.free, filters.discount, filters.reviewed],
    () => {
        currentPage.value = 1
    }
)
</script>

<template>

    <!-- 실제 수업 안내 -->
    <div class="pt-24 max-w-7xl mx-auto px-6">
        <div class="bg-blue-50/60 border border-blue-100 rounded-2xl p-4 md:p-5 flex items-start gap-3">
            <i class="fa-solid fa-circle-info text-brand mt-0.5 shrink-0"></i>
            <p class="text-sm text-slate-600 leading-relaxed">
                <span class="font-semibold text-slate-800">안내</span> ·
                이 영상들은 온라인 강의가 아니라 <b class="font-semibold text-slate-700">현장(오프라인)에서 진행한 실제 수업</b>을 녹화한 것입니다.
                뒷자리에서 화면이 잘 보이지 않는 학생들을 위해 <b class="font-semibold text-slate-700">디스코드로 화면을 공유</b>하며 수업했고,
                그 방송을 <b class="font-semibold text-slate-700">학생이 직접 녹화</b>했습니다.
                <b class="font-semibold text-slate-700">별도 마이크 없이</b> 진행되어 음성·화면 품질이 다소 고르지 않을 수 있는 점 양해 부탁드립니다.
            </p>
        </div>
    </div>

    <main class="max-w-7xl mx-auto px-6 pt-6 pb-12 flex flex-col md:flex-row gap-8">
        <!-- 왼쪽 사이드바 (카테고리) -->
        <aside class="w-full md:w-64 flex-shrink-0">
            <div class="sticky top-28 space-y-8">
                <div>
                    <h3 class="font-bold text-lg mb-4 flex items-center gap-2">
                        <i class="fa-solid fa-list-ul text-brand"></i> 카테고리
                    </h3>
                    <ul class="space-y-1">
                        <li>
                            <RouterLink to="/course/list"
                                class="category-item flex items-center justify-between px-4 py-2.5 rounded-xl font-medium"
                                :class="!activeSlug ? 'active' : 'text-gray-600'">
                                전체
                                <span class="text-xs bg-gray-100 px-2 py-0.5 rounded-full text-gray-500">{{ totalCourseCount }}</span>
                            </RouterLink>
                        </li>
                        <template v-for="cat in categories" :key="cat.idx">
                            <li>
                                <RouterLink :to="`/course/list/${cat.slug}`"
                                    class="category-item flex items-center justify-between px-4 py-2.5 rounded-xl font-medium"
                                    :class="activeSlug === cat.slug ? 'active' : 'text-gray-600'">
                                    {{ cat.name }}
                                    <span class="text-xs bg-gray-100 px-2 py-0.5 rounded-full text-gray-500">{{ categoryTotal(cat) }}</span>
                                </RouterLink>
                            </li>
                            <li v-for="child in cat.children" :key="child.idx">
                                <RouterLink :to="`/course/list/${child.slug}`"
                                    class="category-item flex items-center justify-between pl-8 pr-4 py-2 rounded-xl font-medium text-sm"
                                    :class="activeSlug === child.slug ? 'active' : 'text-gray-400'">
                                    {{ child.name }}
                                    <span class="text-xs bg-gray-100 px-2 py-0.5 rounded-full text-gray-500">{{ child.courseCount || 0 }}</span>
                                </RouterLink>
                            </li>
                        </template>
                    </ul>
                </div>

                <div class="pt-6 border-t border-gray-100">
                    <h3 class="font-bold text-lg mb-4">필터</h3>
                    <div class="space-y-4">
                        <label class="flex items-center gap-3 cursor-pointer group">
                            <input type="checkbox" v-model="filters.free"
                                class="w-4 h-4 rounded border-gray-300 text-brand focus:ring-brand" />
                            <span class="text-sm text-gray-600 group-hover:text-gray-900">무료 강의</span>
                        </label>
                        <label class="flex items-center gap-3 cursor-pointer group">
                            <input type="checkbox" v-model="filters.discount"
                                class="w-4 h-4 rounded border-gray-300 text-brand focus:ring-brand" />
                            <span class="text-sm text-gray-600 group-hover:text-gray-900">할인 중</span>
                        </label>
                        <label class="flex items-center gap-3 cursor-pointer group">
                            <input type="checkbox" v-model="filters.reviewed"
                                class="w-4 h-4 rounded border-gray-300 text-brand focus:ring-brand" />
                            <span class="text-sm text-gray-600 group-hover:text-gray-900">수강평 있는 강의</span>
                        </label>
                    </div>

                    <h3 class="font-bold text-lg mb-4 mt-8">난이도</h3>
                    <div class="space-y-4">
                        <label class="flex items-center gap-3 cursor-pointer group">
                            <input type="checkbox" value="초급" v-model="selectedLevels"
                                class="w-4 h-4 rounded border-gray-300 text-brand focus:ring-brand" />
                            <span class="text-sm text-gray-600 group-hover:text-gray-900">초급</span>
                        </label>
                        <label class="flex items-center gap-3 cursor-pointer group">
                            <input type="checkbox" value="중급" v-model="selectedLevels"
                                class="w-4 h-4 rounded border-gray-300 text-brand focus:ring-brand" />
                            <span class="text-sm text-gray-600 group-hover:text-gray-900">중급</span>
                        </label>
                        <label class="flex items-center gap-3 cursor-pointer group">
                            <input type="checkbox" value="고급" v-model="selectedLevels"
                                class="w-4 h-4 rounded border-gray-300 text-brand focus:ring-brand" />
                            <span class="text-sm text-gray-600 group-hover:text-gray-900">고급</span>
                        </label>
                    </div>
                </div>
            </div>
        </aside>

        <!-- 오른쪽 강의 그리드 (9개) -->
        <section class="flex-grow">
            <div class="flex justify-between items-center mb-8">
                <h2 class="text-2xl font-bold">{{ activeCategoryName }} <span class="text-brand ml-1">{{ totalCount }}</span></h2>
                <div class="flex items-center gap-2">
                    <select v-model="sortBy" class="bg-white border border-gray-200 rounded-lg px-3 py-1.5 text-sm focus:outline-none">
                        <option value="popular">인기순</option>
                        <option value="newest">최신순</option>
                        <option value="rating">평점 높은순</option>
                    </select>
                </div>
            </div>

            <!-- 그리드 시작 -->
            <div class="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-6">
                <!-- 스켈레톤 UI -->
                <template v-if="isLoading">
                    <div v-for="n in 6" :key="n" class="bg-white rounded-2xl border border-gray-100 overflow-hidden">
                        <div class="h-40 bg-gray-100 skeleton"></div>
                        <div class="p-5">
                            <div class="w-24 h-3 bg-gray-100 rounded skeleton mb-3"></div>
                            <div class="w-full h-5 bg-gray-100 rounded skeleton mb-2"></div>
                            <div class="w-3/4 h-5 bg-gray-100 rounded skeleton mb-4"></div>
                            <div class="w-20 h-3 bg-gray-100 rounded skeleton mb-4"></div>
                            <div class="flex items-center justify-between pt-4 border-t border-gray-50">
                                <div class="w-24 h-5 bg-gray-100 rounded skeleton"></div>
                                <div class="w-10 h-4 bg-gray-100 rounded skeleton"></div>
                            </div>
                        </div>
                    </div>
                </template>

                <!-- 실제 데이터 -->
                <template v-else>
                    <RouterLink v-for="course in pagedCourseList" :key="course.idx" :to="`/course/${course.idx}`"
                        class="bg-white rounded-2xl border border-gray-100 overflow-hidden course-card cursor-pointer">
                        <div class="h-40 bg-blue-50 flex items-center justify-center">
                            <img class="course-image" :src="`${course.image}`" />
                        </div>
                        <div class="p-5">
                            <span class="text-[10px] font-bold text-brand mb-2 block uppercase tracking-wider">{{ courseCategoryName(course) }}</span>
                            <h3 class="text-lg font-bold mb-2 text-gray-900 line-clamp-2">
                                {{ course.name }}
                            </h3>
                            <div class="flex items-center gap-1 text-yellow-400 text-xs mb-4">
                                <i class="fa-solid fa-star"></i>
                                <span class="text-gray-400 ml-1">{{ getAverageRating(course) }}</span>
                                <span class="text-gray-400 ml-1">({{ course.totalReviewsCount || 0 }})</span>
                            </div>
                            <div class="flex items-center justify-between pt-4 border-t border-gray-50">
                                <span class="font-bold text-gray-900">{{ formatPrice(course.salePrice) }}</span>
                                <span class="text-xs text-gray-400">{{ course.level }}</span>
                            </div>
                        </div>
                    </RouterLink>
                </template>
            </div>

            <!-- 빈 결과 안내 (검색/필터 결과 없음) -->
            <div v-if="!isLoading && totalCount === 0" class="text-center py-24 text-gray-400">
                <i class="fa-solid fa-magnifying-glass text-4xl mb-4"></i>
                <p class="text-lg">{{ route.query.keyword ? '검색 결과가 없습니다.' : '조건에 맞는 강의가 없습니다.' }}</p>
            </div>

            <!-- 페이지네이션 -->
            <div v-if="!isLoading && totalPages > 1" class="mt-16 flex justify-center items-center gap-2">
                <button
                    :disabled="currentPage === 1"
                    @click="goToPage(currentPage - 1)"
                    class="w-10 h-10 rounded-lg border border-gray-200 flex items-center justify-center transition-all"
                    :class="currentPage === 1 ? 'text-gray-300 cursor-not-allowed' : 'hover:border-brand hover:text-brand'">
                    <i class="fa-solid fa-chevron-left"></i>
                </button>
                <button
                    v-for="page in pageNumbers"
                    :key="page"
                    @click="goToPage(page)"
                    class="w-10 h-10 rounded-lg border transition-all font-bold text-sm"
                    :class="page === currentPage
                        ? 'bg-brand text-white border-brand'
                        : 'border-gray-200 text-gray-500 hover:border-brand hover:text-brand'">
                    {{ page }}
                </button>
                <button
                    :disabled="currentPage === totalPages"
                    @click="goToPage(currentPage + 1)"
                    class="w-10 h-10 rounded-lg border border-gray-200 flex items-center justify-center transition-all"
                    :class="currentPage === totalPages ? 'text-gray-300 cursor-not-allowed' : 'hover:border-brand hover:text-brand'">
                    <i class="fa-solid fa-chevron-right"></i>
                </button>
            </div>
        </section>
    </main>


</template>

<style scoped>
.category-item:hover,
.category-item.active {
    color: #14BCED;
    background-color: rgba(20, 188, 237, 0.05);
}

.course-card {
    transition: all 0.3s ease;
}

.course-card:hover {
    transform: translateY(-5px);
    box-shadow: 0 10px 20px rgba(0, 0, 0, 0.05);
    border-color: #14BCED;
}

/* 썸네일이 h-40 박스를 비율 유지하며 꽉 채우도록(넘치는 부분은 크롭) */
.course-image {
    width: 100%;
    height: 100%;
    object-fit: cover;
}

/* 스켈레톤 애니메이션 */
.skeleton {
    background: linear-gradient(90deg, #f0f0f0 25%, #e0e0e0 50%, #f0f0f0 75%);
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
