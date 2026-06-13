<script setup>
import { ref, reactive, onMounted, computed } from "vue";
import { useRouter } from 'vue-router'
import useAuthStore from '@/stores/useAuthStore'
import userApi from '@/api/user'
import api from '@/api/community'
import courseApi from '@/api/course'
import QuillEditor from '@/components/base/QuillEditor.vue'

const authStore = useAuthStore()

const backend = import.meta.env.VITE_IMG_BASE_URL

//라우터 정보 객체
const router = useRouter()

//폼 정보 객체
const postForm = ref()

//에러 객체 정보 객체
const postInputError = reactive({
  title: {
    errorMessage: null,
    isValid: false
  },
  courseIdx: {
    errorMessage: null,
    isValid: true // 선택사항이므로 기본값 true
  },
  lectureIdx: {
    errorMessage: null,
    isValid: true // 선택사항이므로 기본값 true
  },
  content: {
    errorMessage: null,
    isValid: false
  }
})

// 카테고리와 PostType 매핑
const postTypeMap = {
  'question': 'QUESTION',
  'free': 'FREE',
  'career': 'CAREER',
  'notice': 'NOTICE'
}

// 카테고리 선택 (기본값: question)
const selectedCategory = ref('question')

//게시글 정보 객체
const postInput = reactive({
  postType: 'QUESTION', // 기본값
  title: '',
  text: '',
  content: '',
  courseIdx: 0,
  lectureIdx: 0
})

// 에디터 v-model용 객체
const editorContent = reactive({
  text: '',
  content: ''
})

// 에디터 에러 상태
const editorError = ref(false)

// 태그 관련
const tagInput = ref('')
const tags = ref([])

const addTag = () => {
  const tag = tagInput.value.trim().replace(/^#/, '')
  if (tag && !tags.value.includes(tag) && tags.value.length < 5) {
    tags.value.push(tag)
    tagInput.value = ''
  }
}

const removeTag = (index) => {
  tags.value.splice(index, 1)
}

/**
 * 폼 컨트롤 객체
 * 버튼 활성화 및 텍스트를 제어한다.
 */
const formCntrObj = reactive({
  noStyl: { text: 'text-tip error' },
  submitBtn: { disabled: true }
})


/**
 * 카테고리 변경 시 postType 업데이트
 */
const onCategoryChange = () => {
  postInput.postType = postTypeMap[selectedCategory.value]
}

/**
 * 제목 유효성 룰
 */
const titleRules = [
  (event) => {
    if (event.target.value) {
      postInputError.title.errorMessage = null
      postInputError.title.isValid = true
      return true
    } else {
      postInputError.title.errorMessage = '제목을 입력해주세요.'
      postInputError.title.isValid = false
      return false
    }
  }
]


/**
 * 코스, 강의 유효성 룰 (선택사항이므로 에러 표시 안함)
 */
const selectRules = () => {
  postInputError.courseIdx.isValid = true
  postInputError.lectureIdx.isValid = true
  return true
}

/**
 * 에디터 유효성 검사
 */
const editorRules = () => {
  if (editorContent.text !== '' && editorContent.text !== '\n') {
    editorError.value = false
    postInputError.content.errorMessage = null
    postInputError.content.isValid = true
  } else {
    editorError.value = true
    postInputError.content.errorMessage = '내용을 입력해주세요.'
    postInputError.content.isValid = false
  }
}

/**
 * 에디터 내용 변경 핸들러
 */
const onEditorChange = (value) => {
  editorContent.text = value.text
  editorContent.content = value.content
  postInput.text = value.text
  postInput.content = value.content
  // 입력 시 바로 유효성 검사
  editorRules()
}

/**
 * 폼 유효성 검사
 * 제목과 내용만 필수, 코스/강의는 선택사항
 */
const isFormValid = computed(() => {
  return postInputError.title.isValid && postInputError.content.isValid
});


/**
 * 폼 서브밋
 */
const submitForm = async () => {
  // postType 최종 업데이트
  postInput.postType = postTypeMap[selectedCategory.value]

  // API: 게시글 등록
  const data = await api.postCreate(postInput)
  if (data.success) {
    router.push({ name: 'communityDetail', params: { postIdx: data.results.idx } })
  }
}


// 코스 목록 객체
let paidCourseList = reactive([]) // 구매한 코스
let allCourseList = reactive([]) // 전체 코스
let selectedLectureList = reactive([])

// 구매한 코스 IDX 목록 (빠른 조회용)
const paidCourseIdxSet = ref(new Set())

// 전체 코스 중 구매하지 않은 코스 목록
const otherCourseList = computed(() => {
  return allCourseList.filter(course => !paidCourseIdxSet.value.has(course.idx))
})

/**
 * 구매 코스 목록 조회
 */
const getPaidCourseList = async () => {
  const data = await userApi.getPaidCourseList()
  if (data.success && data.results) {
    const list = data.results
    if (list.length) {
      paidCourseList.push(...list)
      list.forEach(course => paidCourseIdxSet.value.add(course.idx))
    }
  }
}

/**
 * 전체 코스 목록 조회
 */
const getAllCourseList = async () => {
  const data = await courseApi.courseList()
  if (data.success && data.results) {
    allCourseList.push(...data.results.courses)
  }
}

const updateSelectedLectureList = async () => {
  let selectedCourse = paidCourseList.find(course => course.idx === postInput.courseIdx)

  if (selectedCourse && selectedCourse.sections) {
    selectedLectureList.splice(0, selectedLectureList.length, ...selectedCourse.sections.flatMap(section => section.lectures))
  } else if (postInput.courseIdx > 0) {
    const data = await courseApi.courseDetail(postInput.courseIdx)
    if (data.success && data.results && data.results.sections) {
      selectedLectureList.splice(0, selectedLectureList.length, ...data.results.sections.flatMap(section => section.lectures))
    } else {
      selectedLectureList.splice(0)
    }
  } else {
    selectedLectureList.splice(0)
  }
}

/**
 * 컴포넌트 마운트
 */
onMounted(() => {
  document.title = "새 게시글 작성 | 따라학IT"
  getPaidCourseList()
  getAllCourseList()
});
</script>


<template>
  <main class="pt-24 max-w-4xl mx-auto px-6 py-12">
    <!-- 상단 타이틀 -->
    <div class="mb-8 flex items-center justify-between">
      <div>
        <h1 class="text-2xl font-bold text-gray-900">새 게시글 작성</h1>
        <p class="text-sm text-gray-500 mt-1">따라학IT 커뮤니티에 새로운 이야기를 들려주세요.</p>
      </div>
      <a href="javascript:history.back()" class="text-gray-400 hover:text-gray-600 transition-all text-sm">
        <i class="fa-solid fa-xmark text-xl"></i>
      </a>
    </div>

    <v-form ref="postForm" autocomplete="off" fast-fail validate-on="blur lazy" @submit.prevent="submitForm"
      class="space-y-6">
      <!-- 메인 작성 영역 -->
      <div class="bg-white rounded-3xl border border-gray-100 shadow-sm overflow-hidden">

        <!-- 1. 카테고리 선택 -->
        <div class="px-8 py-6 bg-gray-50/50 border-b border-gray-50">
          <p class="text-xs font-bold text-gray-400 mb-4 uppercase tracking-wider">게시판 카테고리</p>
          <div class="flex flex-wrap gap-2">
            <div class="relative">
              <input type="radio" name="category" id="cat-question" value="question" class="hidden category-radio"
                v-model="selectedCategory" @change="onCategoryChange">
              <label for="cat-question"
                class="category-label px-5 py-2.5 rounded-xl border border-gray-200 text-sm font-medium cursor-pointer hover:bg-white transition-all block bg-white">
                <i class="fa-solid fa-circle-question mr-1.5 opacity-70"></i>질문/답변
              </label>
            </div>
            <div class="relative">
              <input type="radio" name="category" id="cat-free" value="free" class="hidden category-radio"
                v-model="selectedCategory" @change="onCategoryChange">
              <label for="cat-free"
                class="category-label px-5 py-2.5 rounded-xl border border-gray-200 text-sm font-medium cursor-pointer hover:bg-white transition-all block bg-white">
                <i class="fa-solid fa-comments mr-1.5 opacity-70"></i>자유주제
              </label>
            </div>
            <div class="relative">
              <input type="radio" name="category" id="cat-career" value="career" class="hidden category-radio"
                v-model="selectedCategory" @change="onCategoryChange">
              <label for="cat-career"
                class="category-label px-5 py-2.5 rounded-xl border border-gray-200 text-sm font-medium cursor-pointer hover:bg-white transition-all block bg-white">
                <i class="fa-solid fa-briefcase mr-1.5 opacity-70"></i>커리어/이직
              </label>
            </div>
            <div v-if="authStore.getUserRole() === 'ROLE_ADMIN'" class="relative">
              <input type="radio" name="category" id="cat-notice" value="notice" class="hidden category-radio"
                v-model="selectedCategory" @change="onCategoryChange">
              <label for="cat-notice"
                class="category-label px-5 py-2.5 rounded-xl border border-gray-200 text-sm font-medium cursor-pointer hover:bg-white transition-all block bg-white">
                <i class="fa-solid fa-bullhorn mr-1.5 opacity-70"></i>공지사항
              </label>
            </div>
          </div>
        </div>

        <!-- 2. 관련 강의 선택 (질문/답변 전용, 선택사항) -->
        <div v-if="selectedCategory === 'question'" class="px-8 py-6 bg-blue-50/30 border-b border-gray-50">
          <p class="text-xs font-bold text-brand mb-3 uppercase tracking-wider flex items-center gap-1.5">
            <i class="fa-solid fa-graduation-cap"></i> 관련 강의 연결 <span class="text-gray-400 font-normal">(선택)</span>
          </p>
          <div class="flex flex-wrap gap-3">
            <select v-model="postInput.courseIdx" @change="updateSelectedLectureList"
              class="flex-1 min-w-[200px] px-4 py-3 bg-white border border-gray-200 rounded-xl text-sm focus:border-brand focus:outline-none transition-all">
              <option :value="0" selected>코스를 선택하세요 (선택사항)</option>
              <!-- 수강 중인 코스 (상단) -->
              <optgroup v-if="paidCourseList.length" label="수강 중인 코스">
                <option v-for="course in paidCourseList" :key="'paid-' + course.idx" :value="course.idx">
                  {{ course.name }}
                </option>
              </optgroup>
              <!-- 전체 코스 (하단, 수강 중인 코스 제외) -->
              <optgroup v-if="otherCourseList.length" label="전체 코스">
                <option v-for="course in otherCourseList" :key="'all-' + course.idx" :value="course.idx">
                  {{ course.name }}
                </option>
              </optgroup>
            </select>

            <select v-model="postInput.lectureIdx" :disabled="!selectedLectureList.length"
              class="flex-1 min-w-[200px] px-4 py-3 bg-white border border-gray-200 rounded-xl text-sm focus:border-brand focus:outline-none transition-all disabled:bg-gray-100 disabled:cursor-not-allowed">
              <option :value="0" selected>강의를 선택하세요 (선택사항)</option>
              <option v-for="lecture in selectedLectureList" :key="lecture.idx" :value="lecture.idx">
                {{ lecture.name }}
              </option>
            </select>
          </div>
        </div>

        <!-- 3. 제목 입력 -->
        <div class="px-8 pt-8">
          <input type="text" v-model="postInput.title" @blur="titleRules" placeholder="제목을 입력하세요" maxlength="50"
            class="w-full text-2xl md:text-3xl font-bold placeholder:text-gray-200 pb-6 border-b border-gray-50 focus:outline-none"
            :class="{ 'border-red-400': postInputError.title.errorMessage }">
          <p v-if="postInputError.title.errorMessage" class="text-red-500 text-xs mt-2">
            <i class="fa-solid fa-circle-exclamation mr-1"></i>{{ postInputError.title.errorMessage }}
          </p>
        </div>

        <!-- 4. 본문 입력 (QuillEditor) -->
        <div class="editor-wrapper">
          <QuillEditor v-model="editorContent" :enable-image-upload="true" :image-base-url="backend"
            placeholder="동료들과 나누고 싶은 이야기를 적어보세요.&#10;질문 게시판인 경우 어떤 에러가 발생했는지 자세히 적어주시면 더 좋은 답변을 얻을 수 있습니다."
            min-height="15rem" :error="editorError" @focus="selectRules" @blur="editorRules"
            @text-change="onEditorChange" />
          <p v-if="postInputError.content.errorMessage" class="text-red-500 text-xs mt-2 px-8">
            <i class="fa-solid fa-circle-exclamation mr-1"></i>{{ postInputError.content.errorMessage }}
          </p>
        </div>
      </div>

      <!-- 태그 입력 영역 -->
      <div class="bg-white p-6 rounded-2xl border border-gray-100 shadow-sm">
        <div class="flex items-center gap-3 mb-4">
          <i class="fa-solid fa-hashtag text-brand"></i>
          <label class="text-sm font-bold text-gray-700">태그 설정</label>
        </div>
        <div
          class="flex items-center gap-2 px-3 py-1 border border-gray-100 rounded-xl bg-gray-50 focus-within:bg-white focus-within:border-brand transition-all">
          <input type="text" v-model="tagInput" @keypress.enter.prevent="addTag" placeholder="관련 태그를 입력하고 엔터를 누르세요"
            class="w-full text-sm py-2 bg-transparent focus:outline-none">
        </div>
        <div class="mt-4 flex flex-wrap gap-2">
          <span v-for="(tag, index) in tags" :key="index"
            class="inline-flex items-center gap-1.5 px-3 py-1.5 bg-blue-50 text-brand text-xs font-bold rounded-lg border border-blue-100">
            #{{ tag }}
            <button type="button" @click="removeTag(index)" class="hover:text-red-400 transition-colors ml-1">
              <i class="fa-solid fa-xmark"></i>
            </button>
          </span>
        </div>
      </div>

      <!-- 하단 액션 버튼 -->
      <div class="flex items-center justify-between pt-4">
        <div class="flex items-center gap-4 text-xs text-gray-400">
          <span class="flex items-center gap-1">
            <i class="fa-solid fa-circle-info"></i> 작성 중인 내용은 자동 저장되지 않습니다
          </span>
        </div>
        <div class="flex items-center gap-3">
          <button type="button" @click="$router.back()"
            class="px-6 py-3 rounded-xl text-gray-500 font-bold hover:bg-gray-100 transition-all">
            취소
          </button>
          <button type="submit" :disabled="!isFormValid"
            class="px-10 py-3 bg-brand text-white rounded-xl font-bold shadow-lg shadow-blue-100 hover:translate-y-[-2px] transition-all disabled:opacity-40 disabled:cursor-not-allowed disabled:transform-none disabled:shadow-none">
            게시하기
          </button>
        </div>
      </div>
    </v-form>
  </main>
</template>

<style scoped>
/* 카테고리 라디오 버튼 스타일 */
.category-radio:checked+.category-label {
  background-color: rgb(20, 189, 238);
  color: white;
  border-color: rgb(20, 189, 238);
}

.category-radio:checked+.category-label i {
  opacity: 1;
}

/* select 커스텀 화살표 */
select {
  appearance: none;
  background-image: url("data:image/svg+xml;charset=UTF-8,%3csvg xmlns='http://www.w3.org/2000/svg' viewBox='0 0 24 24' fill='none' stroke='currentColor' stroke-width='2' stroke-linecap='round' stroke-linejoin='round'%3e%3cpolyline points='6 9 12 15 18 9'%3e%3c/polyline%3e%3c/svg%3e");
  background-repeat: no-repeat;
  background-position: right 1rem center;
  background-size: 1rem;
  padding-right: 2.5rem;
}

/* optgroup 스타일 */
select optgroup {
  font-weight: 600;
  color: rgb(107, 114, 128);
  background-color: rgb(249, 250, 251);
  padding: 0.5rem 0;
}

select optgroup option {
  font-weight: 400;
  color: rgb(55, 65, 81);
  background-color: white;
  padding: 0.5rem 1rem;
}

/* 에디터 래퍼 스타일 */
.editor-wrapper {
  padding-bottom: 1.5rem;
}

/* QuillEditor 툴바 스타일 커스터마이징 */
.editor-wrapper :deep(.ql-toolbar.ql-snow) {
  border: none;
  border-bottom: 1px solid rgb(229, 231, 235);
  border-radius: 0;
  padding: 0.75rem 2rem;
  background-color: white;
}

.editor-wrapper :deep(.ql-toolbar .ql-formats) {
  margin-right: 1rem;
}

.editor-wrapper :deep(.ql-toolbar button) {
  color: rgb(156, 163, 175);
  transition: all 0.2s;
}

.editor-wrapper :deep(.ql-toolbar button:hover) {
  color: rgb(20, 189, 238);
}

.editor-wrapper :deep(.ql-toolbar button.ql-active) {
  color: rgb(20, 189, 238);
}

.editor-wrapper :deep(.ql-toolbar .ql-stroke) {
  stroke: rgb(156, 163, 175);
}

.editor-wrapper :deep(.ql-toolbar .ql-fill) {
  fill: rgb(156, 163, 175);
}

.editor-wrapper :deep(.ql-toolbar button:hover .ql-stroke),
.editor-wrapper :deep(.ql-toolbar button.ql-active .ql-stroke) {
  stroke: rgb(20, 189, 238);
}

.editor-wrapper :deep(.ql-toolbar button:hover .ql-fill),
.editor-wrapper :deep(.ql-toolbar button.ql-active .ql-fill) {
  fill: rgb(20, 189, 238);
}

.editor-wrapper :deep(.ql-toolbar .ql-picker) {
  color: rgb(156, 163, 175);
}

.editor-wrapper :deep(.ql-toolbar .ql-picker-label) {
  color: rgb(156, 163, 175);
}

.editor-wrapper :deep(.ql-toolbar .ql-picker-label:hover),
.editor-wrapper :deep(.ql-toolbar .ql-picker-label.ql-active) {
  color: rgb(20, 189, 238);
}

.editor-wrapper :deep(.ql-toolbar .ql-picker-label .ql-stroke) {
  stroke: rgb(156, 163, 175);
}

.editor-wrapper :deep(.ql-toolbar .ql-picker-label:hover .ql-stroke) {
  stroke: rgb(20, 189, 238);
}

/* QuillEditor 컨테이너 스타일 */
.editor-wrapper :deep(.ql-container.ql-snow) {
  border: none;
  border-radius: 0;
}

.editor-wrapper :deep(.ql-editor) {
  padding: 1.5rem 2rem;
  font-size: 1.125rem;
  line-height: 1.75;
  color: rgb(55, 65, 81);
  min-height: 15rem;
}

.editor-wrapper :deep(.ql-editor.ql-blank::before) {
  color: rgb(209, 213, 219);
  font-style: normal;
  left: 2rem;
  right: 2rem;
}

/* 코드 블록 언어 선택 드롭다운 스타일 */
.editor-wrapper :deep(.ql-code-block-container .ql-ui) {
  color: rgb(156, 163, 175);
}

.editor-wrapper :deep(.ql-code-block-container select.ql-ui option) {
  background-color: white;
  color: rgb(55, 65, 81);
}
</style>
