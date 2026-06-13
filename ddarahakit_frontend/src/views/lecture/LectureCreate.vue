<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import QuillEditor from '@/components/base/QuillEditor.vue'
import api from '@/api/course'
import lectureApi from '@/api/lecture'

const backend = import.meta.env.VITE_IMG_BASE_URL
const router = useRouter()

// 코스 목록
let courseList = reactive([])
let selectedSectionList = reactive([])

// 에디터
const editorRef = ref(null)
const editorContent = reactive({ text: '', content: '' })
const htmlInput = ref('')
const showHtmlInput = ref(false)

// 강의 정보
const lectureInput = reactive({
    name: '',
    videoUrl: '',
    playTime: 0,
    free: false,
    text: '',
    content: '',
    courseIdx: 0,
    sectionIdx: 0,
})

// 에디터 툴바 (풍부한 기능)
const editorToolbar = [
    [{ 'header': [1, 2, 3, 4, false] }],
    [{ 'size': ['small', false, 'large', 'huge'] }],
    ['bold', 'italic', 'underline', 'strike'],
    [{ 'color': [] }, { 'background': [] }],
    [{ 'list': 'ordered' }, { 'list': 'bullet' }],
    [{ 'indent': '-1' }, { 'indent': '+1' }],
    [{ 'align': [] }],
    ['blockquote', 'code-block'],
    ['link', 'image'],
    ['clean'],
]

/**
 * 코스 목록 조회
 */
const getCourseList = async () => {
    const data = await api.courseList()
    if (data && data.success) {
        if (data.results) {
            const list = data.results.courses
            if (list.length) {
                courseList.push(...list)
            }
        }
    } else {
        courseList.splice(0)
    }
}

const updateSelectedSectionList = () => {
    const selectedCourse = courseList.find(course => course.idx === lectureInput.courseIdx)
    if (selectedCourse) {
        selectedSectionList.splice(0, selectedSectionList.length, ...selectedCourse.sections)
    } else {
        selectedSectionList.splice(0)
    }
}

const onEditorChange = (value) => {
    editorContent.text = value.text
    editorContent.content = value.content
    lectureInput.text = value.text
    lectureInput.content = value.content
}

const convert = () => {
    if (editorRef.value) {
        editorRef.value.pasteHtml(htmlInput.value)
        htmlInput.value = ''
        showHtmlInput.value = false
    }
}

const submitForm = async () => {
    const data = await lectureApi.lectureCreate(lectureInput)
    if (data.success) {
        router.push({ name: 'lecture', params: { courseIdx: lectureInput.courseIdx, lectureIdx: data.results.idx } })
    }
}

onMounted(() => {
    document.title = '강의 작성 | 따라학IT'
    getCourseList()
})
</script>

<template>
    <div class="flex flex-col min-h-screen bg-slate-50">
        <!-- Header -->
        <header
            class="h-16 border-b border-slate-200 flex items-center justify-between px-4 sm:px-6 bg-white z-50 relative shrink-0">
            <div class="flex items-center gap-2 sm:gap-4">
                <a href="/" class="font-bold text-lg sm:text-xl shrink-0">
                    따라학<span class="text-brand">IT</span>
                </a>
                <span class="text-slate-300 hidden sm:inline">|</span>
                <span class="text-slate-700 text-sm font-bold hidden sm:inline">강의 등록</span>
            </div>
            <div class="flex items-center gap-2">
                <button @click="showHtmlInput = !showHtmlInput"
                    class="px-3 py-2 bg-slate-100 hover:bg-slate-200 text-slate-600 text-xs sm:text-sm font-bold rounded-lg transition-colors flex items-center gap-1.5">
                    <i class="fa-solid fa-code"></i>
                    <span class="hidden sm:inline">HTML 붙여넣기</span>
                </button>
            </div>
        </header>

        <!-- Main -->
        <div class="flex flex-1 overflow-hidden">
            <!-- Editor Area -->
            <main class="flex-1 flex flex-col overflow-hidden">
                <!-- HTML Input (toggle) -->
                <div v-if="showHtmlInput" class="p-4 bg-slate-800 border-b border-slate-700">
                    <div class="flex items-center gap-2 mb-2">
                        <i class="fa-solid fa-code text-slate-400 text-xs"></i>
                        <span class="text-xs font-bold text-slate-300">HTML 붙여넣기</span>
                    </div>
                    <div class="flex gap-2">
                        <textarea v-model="htmlInput" rows="3"
                            class="flex-1 bg-slate-900 text-slate-200 text-sm rounded-lg border border-slate-600 p-3 focus:outline-none focus:border-[#14BCED] font-mono resize-none"
                            placeholder="HTML 코드를 붙여넣으세요..."></textarea>
                        <div class="flex flex-col gap-2">
                            <button @click="convert"
                                class="px-4 py-2 bg-[#14BCED] text-white text-xs font-bold rounded-lg hover:opacity-90 transition-colors">
                                변환
                            </button>
                            <button @click="showHtmlInput = false"
                                class="px-4 py-2 bg-slate-700 text-slate-300 text-xs font-bold rounded-lg hover:bg-slate-600 transition-colors">
                                닫기
                            </button>
                        </div>
                    </div>
                </div>

                <!-- Quill Editor -->
                <div class="flex-1 overflow-y-auto bg-white custom-scrollbar">
                    <QuillEditor ref="editorRef" v-model="editorContent" :toolbar="editorToolbar"
                        :enable-image-upload="true" :image-base-url="backend" placeholder="강의 내용을 작성해주세요."
                        min-height="calc(100vh - 200px)" @text-change="onEditorChange" />
                </div>
            </main>

            <!-- Right Panel (강의 정보 입력) -->
            <aside class="w-80 bg-white border-l border-slate-200 flex flex-col shrink-0 hidden lg:flex">
                <div class="p-5 border-b border-slate-100">
                    <h3 class="font-bold text-slate-900 flex items-center gap-2 text-sm">
                        <i class="fa-solid fa-square-pen text-[#14BCED]"></i>
                        강의 정보
                    </h3>
                </div>

                <div class="flex-1 overflow-y-auto p-5 custom-scrollbar">
                    <div class="space-y-5">
                        <!-- 강의 이름 -->
                        <div>
                            <label class="block text-[11px] font-bold text-slate-500 uppercase tracking-wider mb-1.5">강의
                                이름</label>
                            <input type="text" v-model="lectureInput.name"
                                class="w-full px-3 py-2.5 text-sm border border-slate-200 rounded-lg focus:outline-none focus:border-[#14BCED] focus:ring-1 focus:ring-[#14BCED]/20 transition-colors"
                                placeholder="강의 이름을 입력하세요">
                        </div>

                        <!-- 비디오 URL -->
                        <div>
                            <label class="block text-[11px] font-bold text-slate-500 uppercase tracking-wider mb-1.5">비디오
                                URL</label>
                            <input type="text" v-model="lectureInput.videoUrl"
                                class="w-full px-3 py-2.5 text-sm border border-slate-200 rounded-lg focus:outline-none focus:border-[#14BCED] focus:ring-1 focus:ring-[#14BCED]/20 transition-colors"
                                placeholder="비디오 URL을 입력하세요">
                        </div>

                        <!-- 재생 시간 -->
                        <div>
                            <label
                                class="block text-[11px] font-bold text-slate-500 uppercase tracking-wider mb-1.5">재생
                                시간</label>
                            <input type="number" v-model="lectureInput.playTime"
                                class="w-full px-3 py-2.5 text-sm border border-slate-200 rounded-lg focus:outline-none focus:border-[#14BCED] focus:ring-1 focus:ring-[#14BCED]/20 transition-colors"
                                placeholder="재생 시간 (초)">
                        </div>

                        <!-- 무료 여부 -->
                        <div>
                            <label class="block text-[11px] font-bold text-slate-500 uppercase tracking-wider mb-1.5">무료
                                여부</label>
                            <select v-model="lectureInput.free"
                                class="w-full px-3 py-2.5 text-sm border border-slate-200 rounded-lg focus:outline-none focus:border-[#14BCED] bg-white transition-colors">
                                <option :value="false">유료</option>
                                <option :value="true">무료</option>
                            </select>
                        </div>

                        <hr class="border-slate-100">

                        <!-- 코스 선택 -->
                        <div>
                            <label class="block text-[11px] font-bold text-slate-500 uppercase tracking-wider mb-1.5">코스
                                선택</label>
                            <select v-model="lectureInput.courseIdx" @change="updateSelectedSectionList"
                                class="w-full px-3 py-2.5 text-sm border border-slate-200 rounded-lg focus:outline-none focus:border-[#14BCED] bg-white transition-colors">
                                <option :value="0" disabled>코스를 선택하세요</option>
                                <option v-for="course in courseList" :key="course.idx" :value="course.idx">
                                    {{ course.name }}
                                </option>
                            </select>
                        </div>

                        <!-- 섹션 선택 -->
                        <div>
                            <label
                                class="block text-[11px] font-bold text-slate-500 uppercase tracking-wider mb-1.5">섹션
                                선택</label>
                            <select v-model="lectureInput.sectionIdx" :disabled="!selectedSectionList.length"
                                class="w-full px-3 py-2.5 text-sm border border-slate-200 rounded-lg focus:outline-none focus:border-[#14BCED] bg-white transition-colors disabled:bg-slate-50 disabled:text-slate-400">
                                <option :value="0" disabled>섹션을 선택하세요</option>
                                <option v-for="section in selectedSectionList" :key="section.idx" :value="section.idx">
                                    {{ section.name }}
                                </option>
                            </select>
                        </div>
                    </div>
                </div>

                <!-- 저장 버튼 -->
                <div class="p-4 border-t border-slate-200">
                    <button @click="submitForm"
                        class="w-full py-3.5 bg-[#14BCED] text-white rounded-xl font-bold text-sm shadow-lg shadow-blue-100 hover:opacity-90 transition-all">
                        강의 등록하기
                    </button>
                </div>
            </aside>
        </div>

        <!-- Mobile Bottom Bar (lg 미만) -->
        <div class="lg:hidden border-t border-slate-200 bg-white p-4 shrink-0">
            <div class="grid grid-cols-2 gap-3 mb-3">
                <input type="text" v-model="lectureInput.name"
                    class="px-3 py-2.5 text-sm border border-slate-200 rounded-lg focus:outline-none focus:border-[#14BCED]"
                    placeholder="강의 이름">
                <input type="text" v-model="lectureInput.videoUrl"
                    class="px-3 py-2.5 text-sm border border-slate-200 rounded-lg focus:outline-none focus:border-[#14BCED]"
                    placeholder="비디오 URL">
            </div>
            <div class="grid grid-cols-3 gap-3 mb-3">
                <input type="number" v-model="lectureInput.playTime"
                    class="px-3 py-2.5 text-sm border border-slate-200 rounded-lg focus:outline-none focus:border-[#14BCED]"
                    placeholder="재생 시간">
                <select v-model="lectureInput.courseIdx" @change="updateSelectedSectionList"
                    class="px-3 py-2.5 text-sm border border-slate-200 rounded-lg focus:outline-none focus:border-[#14BCED] bg-white">
                    <option :value="0" disabled>코스 선택</option>
                    <option v-for="course in courseList" :key="course.idx" :value="course.idx">{{ course.name }}
                    </option>
                </select>
                <select v-model="lectureInput.sectionIdx" :disabled="!selectedSectionList.length"
                    class="px-3 py-2.5 text-sm border border-slate-200 rounded-lg focus:outline-none focus:border-[#14BCED] bg-white disabled:bg-slate-50">
                    <option :value="0" disabled>섹션 선택</option>
                    <option v-for="section in selectedSectionList" :key="section.idx" :value="section.idx">
                        {{ section.name }}
                    </option>
                </select>
            </div>
            <button @click="submitForm"
                class="w-full py-3 bg-[#14BCED] text-white rounded-xl font-bold text-sm hover:opacity-90 transition-all">
                강의 등록하기
            </button>
        </div>
    </div>
</template>

<style scoped>
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

i {
    font-style: normal;
}
</style>
