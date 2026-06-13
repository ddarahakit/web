<script setup>
import { ref, computed, onMounted, onUnmounted } from 'vue';
import { useRoute, useRouter } from 'vue-router'
import useAuthStore from '@/stores/useAuthStore'
import api from '@/api/lecture'
import QuillEditor from '@/components/base/QuillEditor.vue';

// Route / Router
const route = useRoute();
const router = useRouter();
const authStore = useAuthStore()

// Route Params
const courseIdx = route.params.courseIdx
const lectureIdx = route.params.lectureIdx

// Refs
const mainContent = ref(null)

// Lecture data
const lecture = ref({
    idx: 0,
    name: '',
    playTime: 0,
    videoUrl: '',
    content: '',
    complete: false,
    course: {
        idx: 0,
        name: '',
        image: '',
        description: '',
        sections: []
    }
});

// Sidebar state (closed by default on mobile)
// 강의 이동은 window.location 으로 전체 새로고침되므로, 열림/닫힘 상태를
// localStorage 에 저장·복원해 다음/이전/완료로 이동해도 같은 상태를 유지한다.
const SIDEBAR_STORAGE_KEY = 'lectureSidebarClosed'

const loadSidebarClosed = () => {
    const saved = localStorage.getItem(SIDEBAR_STORAGE_KEY)
    // 저장값이 없으면 기본 닫힘(true)
    return saved === null ? true : saved === 'true'
}

const sidebarClosed = ref(loadSidebarClosed());

const toggleSidebar = () => {
    sidebarClosed.value = !sidebarClosed.value;
    localStorage.setItem(SIDEBAR_STORAGE_KEY, String(sidebarClosed.value))
};

// Notes section state
const isNotesHidden = ref(false);

const closeNotes = () => {
    isNotesHidden.value = true;
    if (window.innerWidth >= 1024) {
        videoFlex.value = 1;
    }
};

const openNotes = () => {
    isNotesHidden.value = false;
    if (window.innerWidth >= 1024) {
        videoFlex.value = 1.5;
        notesFlex.value = 1;
    }
};

// Resizer state (flex-based)
const videoFlex = ref(1.5);
const notesFlex = ref(1);
const isResizing = ref(false);

const startResize = () => {
    isResizing.value = true;
    document.body.style.cursor = 'col-resize';
    document.body.classList.add('select-none');
    document.addEventListener('mousemove', onResize);
    document.addEventListener('mouseup', stopResize);
};

const onResize = (e) => {
    if (!isResizing.value || window.innerWidth < 1024) return;
    const mainRect = mainContent.value.getBoundingClientRect();
    let leftPercentage = ((e.clientX - mainRect.left) / mainRect.width) * 100;
    if (leftPercentage < 25) leftPercentage = 25;
    if (leftPercentage > 75) leftPercentage = 75;
    videoFlex.value = leftPercentage;
    notesFlex.value = 100 - leftPercentage;
};

const stopResize = () => {
    isResizing.value = false;
    document.body.style.cursor = '';
    document.body.classList.remove('select-none');
    document.removeEventListener('mousemove', onResize);
    document.removeEventListener('mouseup', stopResize);
};

// Computed properties
const progressPercent = computed(() => {
    const sections = lecture.value.course.sections;
    if (!sections || sections.length === 0) return 0;
    const allLectures = sections.flatMap(s => s.lectures);
    const total = allLectures.length;
    if (total === 0) return 0;
    const completed = allLectures.filter(l => l.complete).length;
    return Math.round((completed / total) * 100);
});

const currentSectionName = computed(() => {
    const sections = lecture.value.course.sections;
    if (!sections) return '';
    for (let i = 0; i < sections.length; i++) {
        if (sections[i].lectures.some(l => l.idx == lectureIdx)) {
            return `섹션 ${i + 1}. ${sections[i].name}`;
        }
    }
    return '';
});

const currentLessonNumber = computed(() => {
    const sections = lecture.value.course.sections;
    if (!sections || sections.length === 0) return 0;
    const allLectures = sections.flatMap(s => s.lectures);
    const index = allLectures.findIndex(l => l.idx == lectureIdx);
    return index >= 0 ? index + 1 : 0;
});

/**
 * 로그아웃
 *
 * 실제 로그아웃 처리는 전용 페이지(/logout)에서 수행한다.
 */
const logout = () => {
    router.push({ name: 'logout' })
}

/**
 * 코스 상세 조회
 */
const getLectureDetail = async () => {
    const data = await api.lectureDetail(courseIdx, lectureIdx)
    if (data.success) {
        if (data.results) {
            lecture.value = data.results
            document.title = `${lecture.value.name} | 따라학잇`;
        }
    } else {
        if (data.code === 40005) {
            router.push({ name: 'ordersCart', params: { courseIdx: courseIdx } })
        } else if (data.code === 20007) {
            router.push({ name: 'courseDetail', params: { courseIdx: courseIdx } })
        }
    }
}

/**
 * 영상이 준비되면 자동 재생.
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

/**
 * 강의 수강 완료
 */
const lectureComplete = async () => {
    const data = await api.lectureComplete(courseIdx, lectureIdx)
    if (data.success) {
        goNextLecture()
    }
}

/**
 * 이전 강의 이동
 */
const goPreviousLecture = () => {
    const currentIdx = Number(lectureIdx);
    const allLectures = lecture.value.course.sections.flatMap(section => section.lectures);
    const currentLectureIndex = allLectures.findIndex(lect => lect.idx === currentIdx);
    if (currentLectureIndex > 0) {
        const prevLecture = allLectures[currentLectureIndex - 1];
        window.location.href = `/lecture/${courseIdx}/${prevLecture.idx}`;
    }
}

/**
 * 다음 강의 이동
 */
const goNextLecture = () => {
    const currentIdx = Number(lectureIdx);
    const allLectures = lecture.value.course.sections.flatMap(section => section.lectures);
    const currentLectureIndex = allLectures.findIndex(lect => lect.idx === currentIdx);
    if (currentLectureIndex < allLectures.length - 1) {
        const nextLecture = allLectures[currentLectureIndex + 1];
        window.location.href = `/lecture/${courseIdx}/${nextLecture.idx}`;
    }
}

/**
 * 컴포넌트 마운트
 */
onMounted(() => {
    getLectureDetail()
    document.title = '강의 상세 | 따라학IT'
    authStore.initSettings()
})

onUnmounted(() => {
    document.removeEventListener('mousemove', onResize);
    document.removeEventListener('mouseup', stopResize);
})
</script>

<template>
    <div class="flex flex-col min-h-screen">
        <!-- Header (h-16) -->
        <header
            class="h-16 border-b border-slate-200 flex items-center justify-between px-4 sm:px-6 bg-white z-50 relative">
            <div class="flex items-center gap-2 sm:gap-4">
                <button @click="toggleSidebar"
                    class="w-10 h-10 flex items-center justify-center hover:bg-slate-100 rounded-lg transition-colors text-slate-600">
                    <i class="fa-solid fa-bars text-xl"></i>
                </button>
                <a href="/" class="font-bold text-lg sm:text-xl shrink-0">
                    따라학<span class="text-brand">IT</span>
                </a>
                <span class="hidden md:inline text-slate-400 text-sm border-l border-slate-200 pl-4 font-medium truncate max-w-xs lg:max-w-md">
                    {{ lecture.course.name }}
                </span>
            </div>
            <div class="flex items-center gap-2 sm:gap-4">
                <button v-show="isNotesHidden" @click="openNotes"
                    class="px-3 lg:px-4 py-2 bg-slate-100 hover:bg-slate-200 text-slate-700 text-xs sm:text-sm font-bold rounded-lg transition-colors flex items-center gap-1.5">
                    <i class="fa-solid fa-file-lines"></i>
                    <span class="hidden lg:inline">자료 보기</span>
                </button>
                <a :href="`/qna/list/${courseIdx}`"
                    class="px-3 py-2 bg-white border border-slate-200 hover:border-[#14BCED] text-slate-700 text-xs sm:text-sm font-bold rounded-lg transition-colors">질문하기</a>
                <button @click="logout"
                    class="w-8 h-8 sm:w-10 sm:h-10 flex items-center justify-center bg-slate-100 hover:bg-slate-200 rounded-full text-slate-600">
                    <i class="fa-regular fa-user"></i>
                </button>
            </div>
        </header>

        <!-- Player Container -->
        <div class="player-container">

            <!-- Sidebar Overlay (mobile) -->
            <div v-if="!sidebarClosed" class="sidebar-overlay lg:hidden" @click="toggleSidebar"></div>

            <!-- Sidebar (320px) -->
            <aside id="sidebar" :class="{ closed: sidebarClosed }">
                <!-- NOW PLAYING Card -->
                <div class="p-4 bg-white">
                    <div class="now-playing-card rounded-2xl p-5">
                        <span class="text-[10px] font-black text-slate-400 uppercase tracking-widest mb-2 block">NOW
                            PLAYING</span>
                        <h3 class="font-bold text-slate-900 text-lg leading-tight mb-1">{{ lecture.name }}</h3>
                        <p class="text-xs text-slate-400 font-medium tracking-tight">{{ currentSectionName }}</p>
                    </div>
                </div>

                <!-- Progress Bar -->
                <div class="px-5 pb-5 bg-white border-b border-slate-100">
                    <div class="flex justify-between items-end mb-2">
                        <span class="text-[11px] font-bold text-slate-800">전체 학습 진도</span>
                        <span class="text-[11px] font-black text-slate-900">{{ progressPercent }}%</span>
                    </div>
                    <div class="w-full h-2 bg-slate-100 rounded-full overflow-hidden">
                        <div class="bg-[#14BCED] h-full rounded-full transition-all"
                            :style="{ width: progressPercent + '%' }"></div>
                    </div>
                </div>

                <!-- Lecture List -->
                <div class="flex-grow overflow-y-auto custom-scrollbar">
                    <div v-for="section in lecture.course.sections" :key="section.name" class="py-2">
                        <div
                            class="px-5 py-3 text-[10px] font-bold text-slate-400 uppercase tracking-widest bg-slate-50/50">
                            {{ section.name }}</div>
                        <div class="mt-1">
                            <a v-for="lect in section.lectures" :key="lect.idx"
                                :href="`/lecture/${courseIdx}/${lect.idx}`"
                                class="w-full px-5 py-4 flex items-center gap-3 text-sm text-left transition-colors border-b border-slate-50 block"
                                :class="lect.idx == lectureIdx ? 'active-lesson' : 'hover:bg-slate-50'">
                                <i v-if="lect.complete" class="fa-solid fa-square-check text-green-500"></i>
                                <i v-else-if="lect.idx == lectureIdx"
                                    class="fa-solid fa-square-play text-[#14BCED]"></i>
                                <i v-else class="fa-regular fa-square text-slate-300"></i>
                                <span
                                    :class="lect.idx == lectureIdx ? 'text-slate-900 font-bold' : 'text-slate-500 font-medium'">{{
                                        lect.name }}</span>
                            </a>
                        </div>
                    </div>
                </div>

                <!-- Next Lecture Button -->
                <div class="p-4 bg-white border-t border-slate-200">
                    <button @click="goNextLecture"
                        class="w-full py-3.5 bg-[#14BCED] text-white rounded-xl font-bold text-sm shadow-lg shadow-blue-100 hover:opacity-90 transition-all">
                        다음 강의 수강하기
                    </button>
                </div>
            </aside>

            <!-- Main Content Area -->
            <main class="main-content custom-scrollbar" ref="mainContent">

                <!-- Video Section -->
                <section id="video-section" class="custom-scrollbar" :style="{ flex: videoFlex + ' 1 0%' }">
                    <!-- Video Player -->
                    <video v-if="lecture.videoUrl" :key="lecture.videoUrl"
                        :src="encodeURI(lecture.videoUrl)" controls autoplay playsinline preload="auto"
                        @loadeddata="autoPlayVideo"
                        class="bg-black w-full aspect-video shrink-0">
                        브라우저가 video 태그를 지원하지 않습니다.
                    </video>

                    <!-- Placeholder (영상 로딩 전/없음) -->
                    <div v-else
                        class="bg-black w-full aspect-video relative flex items-center justify-center overflow-hidden shrink-0">
                        <div class="absolute inset-0 bg-slate-800 flex items-center justify-center">
                            <div class="text-center">
                                <div
                                    class="w-16 h-16 sm:w-20 sm:h-20 bg-[#14BCED]/20 rounded-full flex items-center justify-center mb-4 mx-auto animate-pulse">
                                    <i class="fa-solid fa-play text-2xl sm:text-3xl text-[#14BCED]"></i>
                                </div>
                                <p class="text-slate-400 text-xs sm:text-sm">동영상을 불러오는 중...</p>
                            </div>
                        </div>
                    </div>

                    <!-- 영상 안내 -->
                    <p class="shrink-0 bg-slate-50 border-b border-slate-100 px-4 py-2 text-[11px] sm:text-xs text-slate-500 flex items-center gap-1.5">
                        <i class="fa-solid fa-circle-info text-slate-400 shrink-0"></i>
                        <span>현장 수업을 디스코드 방송으로 학생이 녹화한 영상으로, 별도 마이크 없이 진행되어 음성·화면이 고르지 않을 수 있습니다.</span>
                    </p>

                    <!-- Video Navigation -->
                    <div class="p-5 sm:p-8 bg-white">
                        <div class="max-w-4xl mx-auto">
                            <div class="flex flex-wrap items-center gap-3 mb-3">
                                <span
                                    class="px-2 py-0.5 bg-blue-50 text-[#14BCED] text-[9px] font-black rounded uppercase border border-blue-100">
                                    Lesson {{ String(currentLessonNumber).padStart(2, '0') }}
                                </span>
                                <span class="text-slate-400 text-xs font-medium">{{ currentSectionName }}</span>
                            </div>
                            <h2 class="text-xl sm:text-3xl font-black text-slate-900 mb-6 leading-tight">
                                {{ lecture.name }}
                            </h2>

                            <div class="flex items-center justify-between py-5 border-y border-slate-50">
                                <div class="flex items-center gap-3">
                                    <button @click="goPreviousLecture"
                                        class="flex items-center gap-1.5 px-4 py-2 rounded-lg bg-slate-50 hover:bg-slate-100 text-slate-600 text-sm font-bold transition-colors">
                                        <i class="fa-solid fa-arrow-left"></i>
                                        <span>이전 수업</span>
                                    </button>
                                    <button v-if="lecture.complete"
                                        class="flex items-center gap-1.5 px-4 py-2 rounded-lg bg-green-50 text-green-600 text-sm font-bold cursor-default">
                                        <i class="fa-solid fa-check"></i>
                                        <span>수강 완료</span>
                                    </button>
                                    <button v-else @click="lectureComplete"
                                        class="flex items-center gap-1.5 px-4 py-2 rounded-lg bg-slate-50 hover:bg-green-50 text-slate-600 hover:text-green-600 text-sm font-bold transition-colors">
                                        <i class="fa-solid fa-check"></i>
                                        <span>수강 완료</span>
                                    </button>
                                    <button @click="goNextLecture"
                                        class="flex items-center gap-1.5 px-4 py-2 rounded-lg bg-slate-50 hover:bg-slate-100 text-slate-600 text-sm font-bold transition-colors">
                                        <span>다음 수업</span>
                                        <i class="fa-solid fa-arrow-right"></i>
                                    </button>
                                </div>
                            </div>

                            <!-- 모바일 전용 자료 보기 버튼 -->
                            <div class="mt-8 mb-6 lg:hidden">
                                <button @click="openNotes"
                                    class="w-full py-4 bg-[#14BCED] text-white rounded-xl font-bold shadow-lg shadow-blue-100 flex items-center justify-center gap-2 active:scale-[0.98] transition-transform">
                                    <i class="fa-solid fa-file-lines"></i>
                                    <span>학습 자료 보기</span>
                                </button>
                            </div>

                            <!-- 강의 소개 -->
                            <div v-if="lecture.course.description" class="py-4">
                                <h4 class="font-bold text-slate-800 mb-2">강의 소개</h4>
                                <p class="text-sm text-slate-600 leading-relaxed">{{ lecture.course.text }}</p>
                            </div>
                        </div>
                    </div>
                </section>

                <!-- Resizer -->
                <div id="resizer" :class="{ 'hidden-resizer': isNotesHidden, resizing: isResizing }"
                    @mousedown="startResize"></div>

                <!-- Notes Section -->
                <section id="notes-section" :class="{ 'hidden-section': isNotesHidden }"
                    :style="{ flex: notesFlex + ' 1 0%' }">
                    <div
                        class="p-4 border-b border-slate-100 flex justify-between items-center bg-white sticky top-0 z-10">
                        <h3 class="font-bold text-slate-800 flex items-center gap-2 text-sm">
                            <i class="fa-solid fa-square-pen text-[#14BCED]"></i>
                            수업 자료 및 노트
                        </h3>
                        <button @click="closeNotes"
                            class="w-7 h-7 flex items-center justify-center hover:bg-slate-100 rounded-full text-slate-400 transition-colors"
                            title="자료 닫기">
                            <i class="fa-solid fa-xmark"></i>
                        </button>
                    </div>
                    <div class="p-6 overflow-y-auto custom-scrollbar flex-grow bg-white">
                        <QuillEditor v-if="lecture.content" :read-only="true" :initial-content="lecture.content" />
                    </div>
                </section>
            </main>
        </div>
    </div>
</template>

<style scoped>
.ql-container.ql-snow {
    border: none;
}


.player-container {
    display: flex;
    flex-direction: column;
    height: calc(100vh - 64px);
    width: 100%;
    position: relative;
    overflow: hidden;
}

@media (min-width: 1024px) {
    .player-container {
        flex-direction: row;
    }
}

/* Sidebar overlay (mobile) */
.sidebar-overlay {
    position: fixed;
    inset: 0;
    background-color: rgba(0, 0, 0, 0.4);
    backdrop-filter: blur(2px);
    z-index: 39;
}

/* Sidebar */
#sidebar {
    width: 320px;
    min-width: 320px;
    max-width: 85vw;
    background-color: #f8fafc;
    border-right: 1px solid #e2e8f0;
    transition: transform 0.3s cubic-bezier(0.4, 0, 0.2, 1), margin-left 0.3s cubic-bezier(0.4, 0, 0.2, 1);
    display: flex;
    flex-direction: column;
    z-index: 40;
    position: fixed;
    top: 64px;
    bottom: 0;
    left: 0;
    transform: translateX(-100%);
}

#sidebar:not(.closed) {
    transform: translateX(0);
}

@media (min-width: 1024px) {
    #sidebar {
        position: relative;
        top: auto;
        bottom: auto;
        height: auto;
        transform: none;
        margin-left: 0;
    }

    #sidebar.closed {
        margin-left: -320px;
    }
}

/* Main learning area */
.main-content {
    flex: 1 1 0%;
    display: flex;
    flex-direction: column;
    position: relative;
    overflow-y: auto;
    background-color: #f1f5f9;
    min-width: 0;
}

@media (min-width: 1024px) {
    .main-content {
        flex-direction: row;
        overflow: hidden;
    }
}

/* Video section */
#video-section {
    background-color: #f1f5f9;
    display: flex;
    flex-direction: column;
    width: 100%;
    flex-shrink: 0;
}

@media (min-width: 1024px) {
    #video-section {
        width: auto;
        flex-shrink: 1;
        overflow-y: auto;
    }
}

/* Notes section */
#notes-section {
    background-color: #ffffff;
    color: #1e293b;
    width: 100%;
    display: flex;
    flex-direction: column;
    border-top: 1px solid #e2e8f0;
    transition: transform 0.3s ease, width 0.3s ease, opacity 0.3s ease;
}

@media (min-width: 1024px) {
    #notes-section {
        width: auto;
        border-top: none;
        border-left: 1px solid #e2e8f0;
    }
}

#notes-section.hidden-section {
    display: none;
}

/* Resizer */
#resizer {
    display: none;
    width: 8px;
    cursor: col-resize;
    background-color: #e2e8f0;
    align-items: center;
    justify-content: center;
    transition: all 0.2s;
    z-index: 20;
}

@media (min-width: 1024px) {
    #resizer {
        display: flex;
    }
}

#resizer.hidden-resizer {
    display: none !important;
}

#resizer:hover,
#resizer.resizing {
    background-color: #14BCED;
}

#resizer::after {
    content: "\22EE";
    color: #94a3b8;
    font-size: 14px;
}

/* Custom scrollbar */
.custom-scrollbar::-webkit-scrollbar {
    width: 6px;
}

.custom-scrollbar::-webkit-scrollbar-track {
    background: transparent;
}

.custom-scrollbar::-webkit-scrollbar-thumb {
    background: #cbd5e1;
    border-radius: 10px;
}

.custom-scrollbar::-webkit-scrollbar-thumb:hover {
    background: #94a3b8;
}

/* Active lesson highlight */
.active-lesson {
    background-color: #effaff;
    border-left: 4px solid #14BCED;
}

/* NOW PLAYING card */
.now-playing-card {
    background: #ffffff;
    border: 2px solid #e0e7ff;
    box-shadow: 0 4px 20px rgba(99, 102, 241, 0.1);
    position: relative;
}

.now-playing-card::after {
    content: '';
    position: absolute;
    inset: -2px;
    border-radius: 1rem;
    padding: 2px;
    background: linear-gradient(135deg, #c7d2fe, #fbcfe8);
    -webkit-mask: linear-gradient(#fff 0 0) content-box, linear-gradient(#fff 0 0);
    mask: linear-gradient(#fff 0 0) content-box, linear-gradient(#fff 0 0);
    -webkit-mask-composite: xor;
    mask-composite: exclude;
    pointer-events: none;
}

/* Font style reset */
i {
    font-style: normal;
}
</style>
