<script setup>
import { ref, computed, onMounted } from 'vue'
import api from '@/api/roadmap'
import userApi from '@/api/user'
import useAuthStore from '@/stores/useAuthStore'

const authStore = useAuthStore()

const isLoading = ref(true)
const roadmapList = ref([])

// 로그인 사용자의 구매 강의 courseIdx Set
const paidCourseIdxSet = ref(new Set())

// 로드맵별 코스 목록 (roadmapIdx → courseIdx[])
const roadmapCoursesMap = ref(new Map())

const isLoggedIn = computed(() => authStore.checkLogin())

// 카드별 스타일 (순환 적용)
const cardStyles = [
    { iconBg: 'bg-blue-50', iconColor: 'text-brand', icon: 'fa-solid fa-layer-group' },
    { iconBg: 'bg-emerald-50', iconColor: 'text-emerald-500', icon: 'fa-solid fa-server' },
    { iconBg: 'bg-purple-50', iconColor: 'text-purple-500', icon: 'fa-solid fa-pen-nib' },
    { iconBg: 'bg-amber-50', iconColor: 'text-amber-500', icon: 'fa-solid fa-database' },
    { iconBg: 'bg-rose-50', iconColor: 'text-rose-500', icon: 'fa-solid fa-mobile-screen' },
    { iconBg: 'bg-cyan-50', iconColor: 'text-cyan-500', icon: 'fa-solid fa-cloud' },
]

const getCardStyle = (index) => cardStyles[index % cardStyles.length]

/**
 * 로드맵 목록 조회
 */
const getRoadmapList = async () => {
    isLoading.value = true
    const data = await api.roadmapList()
    if (data && data.success && data.results) {
        roadmapList.value = data.results
    }
    isLoading.value = false
}

/**
 * 수강 중 강의 목록 조회 (로그인 시에만)
 */
const getPaidCourses = async () => {
    if (!authStore.checkLogin()) return
    const data = await userApi.getPaidCourseList()
    if (data && data.success && data.results) {
        paidCourseIdxSet.value = new Set(data.results.map(c => c.idx))
    }
}

/**
 * 각 로드맵의 코스 목록 조회 (로그인 시에만)
 */
const loadRoadmapCourses = async () => {
    if (!authStore.checkLogin() || roadmapList.value.length === 0) return

    const promises = roadmapList.value.map(async (roadmap) => {
        const data = await api.roadmapDetail(roadmap.idx)
        if (data && data.success && data.results && data.results.courses) {
            roadmapCoursesMap.value.set(roadmap.idx, data.results.courses.map(c => c.courseIdx))
        }
    })
    await Promise.all(promises)
}

/**
 * 해당 로드맵이 진행 중인지 확인
 */
const isRoadmapInProgress = (roadmapIdx) => {
    if (!isLoggedIn.value) return false
    const courseIdxList = roadmapCoursesMap.value.get(roadmapIdx) || []
    return courseIdxList.some(idx => paidCourseIdxSet.value.has(idx))
}

onMounted(async () => {
    await Promise.all([getRoadmapList(), getPaidCourses()])
    await loadRoadmapCourses()
    document.title = '학습 로드맵 | 따라학잇'
})
</script>

<template>
    <main class="max-w-6xl mx-auto px-6 pt-28 pb-20">

        <!-- 로드맵 선택 목록 섹션 -->
        <section id="roadmap-list-section" class="mb-20">
            <div class="flex flex-col md:flex-row justify-between items-end mb-10 gap-4">
                <div>
                    <h2 class="text-3xl font-bold text-slate-900 tracking-tight">추천 학습 로드맵</h2>
                    <p class="text-slate-500 mt-2">목표에 맞는 커리큘럼을 선택하여 체계적으로 학습하세요.</p>
                </div>
            </div>

            <!-- 스켈레톤 UI -->
            <div v-if="isLoading" class="grid grid-cols-1 md:grid-cols-3 gap-6">
                <div v-for="n in 3" :key="n"
                    class="bg-white p-6 rounded-[2rem] border border-slate-100 shadow-sm">
                    <div class="w-12 h-12 rounded-2xl bg-slate-100 skeleton mb-6"></div>
                    <div class="w-3/4 h-5 bg-slate-100 rounded skeleton mb-3"></div>
                    <div class="w-full h-3 bg-slate-100 rounded skeleton mb-2"></div>
                    <div class="w-2/3 h-3 bg-slate-100 rounded skeleton mb-6"></div>
                    <div class="w-1/3 h-3 bg-slate-100 rounded skeleton"></div>
                </div>
            </div>

            <!-- 로드맵 목록 -->
            <div v-else class="grid grid-cols-1 md:grid-cols-3 gap-6">
                <!-- 로드맵이 없을 때 -->
                <div v-if="roadmapList.length === 0"
                    class="md:col-span-3 text-center py-16 text-slate-400">
                    <i class="fa-solid fa-map text-4xl mb-4"></i>
                    <p>등록된 로드맵이 없습니다.</p>
                </div>

                <!-- 로드맵 카드 -->
                <RouterLink v-for="(roadmap, index) in roadmapList" :key="roadmap.idx"
                    :to="{ name: 'roadmapDetail', params: { roadmapId: roadmap.idx } }"
                    class="roadmap-card cursor-pointer bg-white p-6 rounded-[2rem] shadow-sm relative overflow-hidden block transition-all"
                    :class="isRoadmapInProgress(roadmap.idx)
                        ? 'border-2 border-brand selected'
                        : 'border border-slate-100'">

                    <!-- 진행 중 배지 -->
                    <div v-if="isRoadmapInProgress(roadmap.idx)" class="absolute top-0 right-0 p-4">
                        <span class="px-2 py-1 rounded-lg bg-blue-100 text-brand text-[10px] font-bold">진행 중</span>
                    </div>

                    <div class="w-12 h-12 rounded-2xl flex items-center justify-center mb-6"
                        :class="[getCardStyle(index).iconBg, getCardStyle(index).iconColor]">
                        <i :class="getCardStyle(index).icon" class="text-xl"></i>
                    </div>
                    <h3 class="text-lg font-bold text-slate-800 mb-2">{{ roadmap.name }}</h3>
                    <p class="text-xs text-slate-400 leading-relaxed mb-6">{{ roadmap.description }}</p>
                    <div class="flex justify-between items-center">
                        <span v-if="roadmap.stepCount" class="text-xs font-bold text-slate-400">{{ roadmap.stepCount }}단계 커리큘럼</span>
                        <span class="text-[10px] font-bold text-brand">자세히 보기 <i class="fa-solid fa-arrow-right ml-1"></i></span>
                    </div>
                </RouterLink>
            </div>

            <div
                class="mt-32 p-10 rounded-[3rem] bg-brand/5 border border-brand/10 flex flex-col md:flex-row items-center gap-8 max-w-4xl mx-auto">
                <div
                    class="w-20 h-20 rounded-3xl bg-brand text-white flex items-center justify-center text-3xl shadow-lg shadow-brand/20">
                    <i class="fa-solid fa-compass"></i>
                </div>
                <div class="flex-grow text-center md:text-left">
                    <h3 class="text-xl font-bold text-slate-900">학습 방향이 고민되시나요?</h3>
                    <p class="text-slate-500 text-sm mt-1">현직 멘토가 제공하는 무료 커리어 컨설팅을 신청해 보세요.</p>
                </div>
                <button
                    class="px-8 py-4 bg-slate-900 text-white rounded-2xl font-bold text-sm hover:scale-105 transition-all">1:1
                    멘토링 신청</button>
            </div>
        </section>

    </main>
</template>

<style scoped>
.roadmap-card {
    transition: all 0.3s ease;
}

.roadmap-card:hover {
    transform: translateY(-8px);
    box-shadow: 0 20px 25px -5px rgb(0 0 0 / 0.1);
}

.roadmap-card.selected {
    border-color: #14BCED;
    background-color: #f0f9ff;
}

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
