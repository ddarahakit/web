<script setup>
import { ref, reactive, onMounted } from 'vue'
import QuillEditor from '@/components/base/QuillEditor.vue'
import api from '@/api/course'
import lectureApi from '@/api/lecture'
import { useRouter } from 'vue-router'

const backend = import.meta.env.VITE_IMG_BASE_URL

const router = useRouter()

//코스 목록 리스트 정보 객체
let courseList = reactive([])
let selectedSectionList = reactive([]);

// 에디터 컴포넌트 ref (pasteHtml 메서드 접근용)
const editorRef = ref(null)

// 에디터 v-model용 객체
const editorContent = reactive({
    text: '',
    content: ''
})

const htmlInput = ref('')

//강의 정보 객체
const lectureInput = reactive({
    name: '', //강의 이름
    videoUrl: '', //강의 비디오 URL
    playTime: 0, //강의 재생 시간
    free: false, //무료 여부
    text: '', //강의 내용(평문)
    content: '', //강의 내용(에디터)
    courseIdx: 0, //관련 코스 IDX
    sectionIdx: 0, //관련 강의 IDX
})


/**
 * 코스 목록 조회
 */
const getCourseList = async () => {
    //API: 코스 목록 조회
    const data = await api.courseList()

    if (data && data.success) {
        //코스 목록 추가
        if (data.results) {
            //조회 결과
            const list = data.results.courses

            if (data.results.category) {
                categoryStore.category = data.results.category
                document.title = `${categoryStore.category.at(-1).name} | 따라학잇`
            }

            if (list.length) {
                courseList.push(...list)
            }
        }
    } else {
        //코스 목록 초기화
        courseList.splice(0)
    }
}


const updateSelectedSectionList = () => {
    const selectedCourse = courseList.find(course => course.idx === lectureInput.courseIdx);
    if (selectedCourse) {
        selectedSectionList.splice(0, selectedSectionList.length, ...selectedCourse.sections);
    } else {
        selectedSectionList.splice(0);
    }
};

/**
 * 에디터 내용 변경 핸들러
 */
const onEditorChange = (value) => {
    editorContent.text = value.text
    editorContent.content = value.content
    lectureInput.text = value.text
    lectureInput.content = value.content
}

/**
 * 컴포넌트 마운트
 *
 */
onMounted(async () => {
    document.title = "강의 작성 | 따라학IT";
    getCourseList()
});

/**
 * HTML 붙여넣기
 */
const convert = () => {
    if (editorRef.value) {
        editorRef.value.pasteHtml(htmlInput.value)
    }
}

/**
 * 폼 서브밋
 *
 */
const submitForm = async () => {
    //API: 질문 등록
    const data = await lectureApi.lectureCreate(lectureInput)
    if (data.success) {
        router.push({ name: 'lecture', params: { courseIdx: lectureInput.courseIdx, lectureIdx: data.results.idx } })
    }
}


</script>
<template>
    <QuillEditor
        ref="editorRef"
        v-model="editorContent"
        :enable-image-upload="true"
        :image-base-url="backend"
        placeholder="강의 내용을 작성해주세요."
        min-height="20rem"
        @text-change="onEditorChange"
    />


    <div class="bottom-fixed">
        <div class="select-container mb-3">
            <textarea class="form-control" v-model="htmlInput"></textarea>
            <button class="save-btn" @click="convert">붙여넣기기</button>

        </div>
        <div class="select-container mb-3">
            <label for="name">강의 이름</label>
            <input type="text" v-model="lectureInput.name" class="form-control" placeholder="강의 이름을 입력하세요">
            <label for="playTime">강의 재생 시간</label>
            <input type="text" v-model="lectureInput.playTime" class="form-control" placeholder="강의 재생 시간을 입력하세요">
        </div>
        <div class="select-container">
            <input type="text" v-model="lectureInput.videoUrl" class="form-control" placeholder="비디오 URL을 입력하세요">
            <select v-model="lectureInput.free" class="form-select">
                <option value=0 selected disabled>무료 여부</option>
                <option value=true>무료</option>
                <option value=false>유료</option>
            </select>
            <select v-model="lectureInput.courseIdx" class="form-select" @change="updateSelectedSectionList">
                <option value=0 selected disabled>코스를 선택하세요</option>
                <option v-for="course in courseList" :key="course.idx" :value="course.idx">{{ course.name }}</option>
            </select>
            <select v-model="lectureInput.sectionIdx" class="form-select" :disabled="!selectedSectionList.length">
                <option value=0 selected disabled>섹션을 선택하세요</option>
                <option v-for="section in selectedSectionList" :key="section.idx" :value="section.idx">
                    {{ section.name }}
                </option>
            </select>
            <button class="save-btn" @click="submitForm">저장하기</button>
        </div>
    </div>
</template>
<style scoped>
.video_input {
    width: 50%;
    color: #384158;
}

#content-section .ql-container {
    border: none;
}

.bottom-fixed {
    z-index: 2000;
    bottom: 0;
    right: 0;
    width: 100%;
    background: white;
    padding: 1rem;
    box-shadow: 0 -2px 10px rgba(0, 0, 0, 0.1);
}

.select-container {
    display: flex;
    gap: 1rem;
    justify-content: flex-end;
    align-items: center;
}

.form-select {
    width: 200px;
}

.save-btn {
    background: #14bdee;
    color: white;
    border: none;
    padding: 0.5rem 1.5rem;
    border-radius: 4px;
    cursor: pointer;
}

.save-btn:hover {
    background: #0fa7d1;
}
</style>