<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import api from '@/api/roadmap'
import userApi from '@/api/user'
import useAuthStore from '@/stores/useAuthStore'

const route = useRoute()
const authStore = useAuthStore()

const isLoading = ref(true)

const roadmap = ref({
    idx: 0,
    name: '',
    image: null,
    description: '',
    courses: []
})

// 로그인 사용자의 수강 중 강의 목록 (courseIdx를 key로 하는 Map)
const paidCourseMap = ref(new Map())

/**
 * 로드맵 상세 조회
 */
const getRoadmapDetail = async () => {
    isLoading.value = true
    const roadmapIdx = route.params.roadmapId
    const data = await api.roadmapDetail(roadmapIdx)
    if (data && data.success && data.results) {
        roadmap.value = data.results
        document.title = `${data.results.name} | 따라학잇`
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
        const map = new Map()
        data.results.forEach(course => {
            map.set(course.idx, course)
        })
        paidCourseMap.value = map
    }
}

/**
 * 해당 코스를 구매했는지 확인
 */
const isPurchased = (courseIdx) => paidCourseMap.value.has(courseIdx)

/**
 * 강의 진행률 계산
 */
const getProgress = (courseIdx) => {
    const course = paidCourseMap.value.get(courseIdx)
    if (!course || !course.sections) return 0
    const totalLectures = course.sections.reduce((sum, section) => sum + section.lectures.length, 0)
    if (totalLectures === 0) return 0
    const completedLectures = course.sections.flatMap(s => s.lectures).filter(l => l.complete).length
    return Math.round(completedLectures / totalLectures * 100)
}

/**
 * 다음 강의 Idx 가져오기
 */
const getNextLectureIdx = (courseIdx) => {
    const course = paidCourseMap.value.get(courseIdx)
    if (!course) return null
    return course.nextLectureIdx || null
}

/**
 * 전체 달성도 계산 (구매한 강의 수 / 전체 강의 수)
 */
const progressPercent = computed(() => {
    const courses = roadmap.value.courses
    if (!courses || courses.length === 0) return 0
    const purchased = courses.filter(c => isPurchased(c.courseIdx)).length
    if (purchased === 0) return 0
    // 구매한 강의들의 평균 진행률
    const totalProgress = courses.reduce((sum, c) => {
        if (isPurchased(c.courseIdx)) {
            return sum + getProgress(c.courseIdx)
        }
        return sum
    }, 0)
    return Math.round(totalProgress / courses.length)
})

const isLoggedIn = computed(() => authStore.checkLogin())

/**
 * 짝수/홀수에 따른 좌우 배치
 */
const getRowDirection = (index) => {
    return index % 2 === 0 ? 'md:flex-row-reverse' : 'md:flex-row'
}

onMounted(async () => {
    await Promise.all([getRoadmapDetail(), getPaidCourses()])
})
</script>

<template>
    <main class="max-w-6xl mx-auto px-6 pt-28 pb-20">

        <section class="mb-20">
            <!-- 스켈레톤 UI -->
            <template v-if="isLoading">
                <div class="text-center mb-16">
                    <div class="w-40 h-6 bg-slate-100 rounded-full skeleton mx-auto mb-4"></div>
                    <div class="w-80 h-8 bg-slate-100 rounded skeleton mx-auto mb-4"></div>
                    <div class="w-96 h-4 bg-slate-100 rounded skeleton mx-auto"></div>
                    <div class="mt-10 max-w-md mx-auto">
                        <div class="w-full h-3 bg-slate-100 rounded-full skeleton"></div>
                    </div>
                </div>
                <div class="relative py-10 max-w-4xl mx-auto space-y-24">
                    <div v-for="n in 3" :key="n" class="flex items-center gap-8"
                        :class="n % 2 === 1 ? 'md:flex-row-reverse' : 'md:flex-row'">
                        <div class="hidden md:block w-1/2"></div>
                        <div class="w-12 h-12 rounded-2xl bg-slate-100 skeleton shrink-0"></div>
                        <div class="w-full md:w-1/2 bg-white p-6 rounded-[2rem] border border-slate-100">
                            <div class="w-16 h-3 bg-slate-100 rounded skeleton mb-3"></div>
                            <div class="h-36 bg-slate-100 rounded-xl skeleton mb-4"></div>
                            <div class="w-3/4 h-5 bg-slate-100 rounded skeleton mb-3"></div>
                            <div class="w-full h-10 bg-slate-100 rounded-xl skeleton"></div>
                        </div>
                    </div>
                </div>
            </template>

            <!-- 실제 데이터 -->
            <template v-else>
                <!-- 헤더 -->
                <div class="text-center mb-16">
                    <span
                        class="inline-block px-4 py-1.5 rounded-full bg-blue-50 text-brand text-[11px] font-bold uppercase tracking-widest mb-4">
                        Current Roadmap
                    </span>
                    <h1 class="text-4xl font-bold text-slate-900 tracking-tight">{{ roadmap.name }}</h1>
                    <p class="text-slate-500 mt-4 max-w-lg mx-auto leading-relaxed">{{ roadmap.description }}</p>

                    <!-- Overall Progress Bar (로그인 시에만) -->
                    <div v-if="isLoggedIn && roadmap.courses.length > 0" class="mt-10 max-w-md mx-auto">
                        <div class="flex justify-between items-end mb-2">
                            <span class="text-xs font-bold text-slate-600">전체 달성도</span>
                            <span class="text-sm font-bold text-brand">{{ progressPercent }}%</span>
                        </div>
                        <div class="w-full h-3 bg-slate-200 rounded-full overflow-hidden">
                            <div class="h-full bg-brand rounded-full shadow-[0_0_12px_rgba(20,188,237,0.4)] transition-all duration-500"
                                :style="`width: ${progressPercent}%`"></div>
                        </div>
                    </div>
                </div>

                <!-- 코스가 없을 때 -->
                <div v-if="roadmap.courses.length === 0"
                    class="text-center py-16 text-slate-400">
                    <i class="fa-solid fa-list-check text-4xl mb-4"></i>
                    <p>아직 등록된 강의가 없습니다.</p>
                </div>

                <!-- 코스 타임라인 -->
                <div v-else class="relative py-10 max-w-4xl mx-auto">
                    <!-- Vertical Line -->
                    <div class="roadmap-line"></div>
                    <div v-if="isLoggedIn" class="roadmap-progress" :style="`height: ${progressPercent}%`"></div>

                    <div class="space-y-24">
                        <div v-for="(course, index) in roadmap.courses" :key="course.courseIdx"
                            class="step-node flex items-center gap-8"
                            :class="[
                                getRowDirection(index),
                                isLoggedIn && !isPurchased(course.courseIdx) ? 'opacity-60' : '',
                                isLoggedIn && isPurchased(course.courseIdx) ? 'active' : ''
                            ]">
                            <div class="hidden md:block w-1/2"></div>

                            <!-- 노드 아이콘 -->
                            <div class="node-circle shrink-0"
                                :class="!isLoggedIn ? 'text-brand'
                                    : isPurchased(course.courseIdx) ? 'text-brand'
                                    : 'text-slate-400'">
                                <i v-if="isLoggedIn && isPurchased(course.courseIdx) && getProgress(course.courseIdx) === 100" class="fa-solid fa-check"></i>
                                <i v-else-if="isLoggedIn && !isPurchased(course.courseIdx)" class="fa-solid fa-lock text-sm"></i>
                                <template v-else>{{ String(course.sortOrder || index + 1).padStart(2, '0') }}</template>
                            </div>

                            <!-- 비로그인: 일반 카드 -->
                            <RouterLink v-if="!isLoggedIn" :to="`/course/${course.courseIdx}`"
                                class="w-full md:w-1/2 bg-white p-6 rounded-[2rem] shadow-sm border border-slate-100 hover:border-brand hover:shadow-[0_20px_40px_rgba(0,0,0,0.06)] transition-all block group">
                                <div class="flex justify-between items-start mb-4">
                                    <span class="text-[10px] font-bold text-brand uppercase tracking-widest">
                                        Step {{ String(course.sortOrder || index + 1).padStart(2, '0') }}
                                    </span>
                                </div>

                                <div v-if="course.courseImage"
                                    class="h-36 rounded-xl overflow-hidden mb-4 bg-slate-50">
                                    <img :src="course.courseImage" :alt="course.courseName"
                                        class="w-full h-full object-cover group-hover:scale-105 transition-transform duration-500">
                                </div>

                                <h3 class="text-lg font-bold text-slate-800 mb-4 group-hover:text-brand transition-colors">
                                    {{ course.courseName }}
                                </h3>

                                <div class="flex items-center justify-between">
                                    <span class="text-xs font-bold text-brand group-hover:underline">
                                        강의 보러가기 <i class="fa-solid fa-arrow-right ml-1"></i>
                                    </span>
                                </div>
                            </RouterLink>

                            <!-- 로그인 + 구매한 강의: 진행 중 카드 -->
                            <div v-else-if="isPurchased(course.courseIdx)"
                                class="w-full md:w-1/2 bg-white p-6 rounded-[2rem] shadow-[0_20px_40px_rgba(0,0,0,0.04)] border-2 border-brand relative">
                                <div class="absolute -top-3 left-6 px-3 py-1 bg-brand text-white text-[10px] font-bold rounded-full">
                                    진행 중
                                </div>
                                <div class="flex justify-between items-start mb-2">
                                    <span class="text-[10px] font-bold text-brand uppercase tracking-widest">
                                        Step {{ String(course.sortOrder || index + 1).padStart(2, '0') }}
                                    </span>
                                    <span class="text-[10px] font-bold text-slate-400">{{ getProgress(course.courseIdx) }}% 진행</span>
                                </div>

                                <div v-if="course.courseImage"
                                    class="h-36 rounded-xl overflow-hidden mb-4 bg-slate-50">
                                    <img :src="course.courseImage" :alt="course.courseName"
                                        class="w-full h-full object-cover">
                                </div>

                                <h3 class="text-lg font-bold text-slate-800 mb-4">{{ course.courseName }}</h3>

                                <div class="w-full h-1.5 bg-slate-100 rounded-full mb-4">
                                    <div class="h-full bg-brand rounded-full transition-all duration-500"
                                        :style="`width: ${getProgress(course.courseIdx)}%`"></div>
                                </div>

                                <RouterLink
                                    :to="getNextLectureIdx(course.courseIdx)
                                        ? `/lecture/${course.courseIdx}/${getNextLectureIdx(course.courseIdx)}`
                                        : `/course/${course.courseIdx}`"
                                    class="block w-full py-3 bg-brand text-white rounded-xl font-bold text-sm hover:shadow-lg hover:shadow-brand/20 transition-all text-center">
                                    학습 계속하기
                                </RouterLink>
                            </div>

                            <!-- 로그인 + 미구매 강의: 잠금 카드 -->
                            <RouterLink v-else :to="`/course/${course.courseIdx}`"
                                class="w-full md:w-1/2 bg-slate-50 p-6 rounded-[2rem] border border-slate-200 border-dashed block hover:bg-slate-100 transition-colors">
                                <div class="flex justify-between items-start mb-2">
                                    <span class="text-[10px] font-bold text-slate-400 uppercase tracking-widest">
                                        Step {{ String(course.sortOrder || index + 1).padStart(2, '0') }}
                                    </span>
                                </div>

                                <div v-if="course.courseImage"
                                    class="h-36 rounded-xl overflow-hidden mb-4 bg-slate-100 opacity-60">
                                    <img :src="course.courseImage" :alt="course.courseName"
                                        class="w-full h-full object-cover">
                                </div>

                                <h3 class="text-lg font-bold text-slate-400 mb-2">{{ course.courseName }}</h3>
                                <span class="text-xs font-bold text-slate-300">수강권 구매 필요</span>
                            </RouterLink>
                        </div>

                        <!-- 마지막 목표 노드 -->
                        <div class="step-node flex items-center gap-8"
                            :class="roadmap.courses.length % 2 === 0 ? 'md:flex-row-reverse' : 'md:flex-row'">
                            <div class="hidden md:block w-1/2"></div>
                            <div class="node-circle shrink-0 !border-slate-200">
                                <i class="fa-solid fa-trophy text-lg text-yellow-400"></i>
                            </div>
                            <div
                                class="w-full md:w-1/2 p-6 rounded-[2rem] bg-gradient-to-br from-slate-800 to-slate-900 text-white shadow-xl">
                                <h3 class="text-lg font-bold mb-2">로드맵 완주!</h3>
                                <p class="text-sm text-slate-400 leading-relaxed mb-4">
                                    모든 강의를 수강하면 {{ roadmap.name }} 로드맵을 완주하게 됩니다.
                                </p>
                                <div class="flex items-center gap-2 py-2 px-3 bg-white/10 rounded-lg">
                                    <i class="fa-solid fa-certificate text-brand"></i>
                                    <span class="text-[11px] font-bold">총 {{ roadmap.courses.length }}개 강의 커리큘럼</span>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>
            </template>

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
.roadmap-line {
    position: absolute;
    left: 50%;
    top: 0;
    bottom: 0;
    width: 4px;
    background: #e2e8f0;
    transform: translateX(-50%);
    z-index: 0;
}

.roadmap-progress {
    position: absolute;
    left: 50%;
    top: 0;
    width: 4px;
    background: linear-gradient(to bottom, #14BCED, #60a5fa);
    transform: translateX(-50%);
    z-index: 1;
    transition: height 0.5s ease;
}

.step-node {
    position: relative;
    z-index: 10;
    transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}

.step-node:hover {
    transform: translateY(-5px);
}

.step-node.active .node-circle {
    background: white;
    border-color: #14BCED;
    box-shadow: 0 0 20px rgba(20, 188, 237, 0.3);
}

.node-circle {
    width: 48px;
    height: 48px;
    border-radius: 16px;
    border: 4px solid #e2e8f0;
    background: white;
    display: flex;
    align-items: center;
    justify-content: center;
    font-weight: bold;
    transition: all 0.3s ease;
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
