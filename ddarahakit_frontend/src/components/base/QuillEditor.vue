<script setup>
import { ref, nextTick, onMounted, watch, onBeforeUnmount } from 'vue'
import Quill from 'quill'
import QuillImageDropAndPaste, { ImageData } from 'quill-image-drop-and-paste'
import hljs from 'highlight.js'
import 'highlight.js/styles/atom-one-dark.css'
import 'quill/dist/quill.snow.css'
import api from '@/api/community'
import { qnaImageUrl } from '@/utils/image'

const Delta = Quill.import('delta')
Quill.register('modules/imageDropAndPaste', QuillImageDropAndPaste)

const props = defineProps({
    // v-model 바인딩용
    modelValue: {
        type: Object,
        default: () => ({ text: '', content: '' })
    },
    // 읽기 전용 모드
    readOnly: {
        type: Boolean,
        default: false
    },
    // placeholder 텍스트
    placeholder: {
        type: String,
        default: '내용을 작성해주세요.'
    },
    // 툴바 설정 (null이면 툴바 없음)
    toolbar: {
        type: Array,
        default: () => [
            { 'size': ['small', 'medium', 'large', 'huge'] },
            'code-block',
            'bold',
            'link',
            'image'
        ]
    },
    // 이미지 업로드 활성화
    enableImageUpload: {
        type: Boolean,
        default: false
    },
    // 이미지 base URL
    imageBaseUrl: {
        type: String,
        default: ''
    },
    // 초기 델타 컨텐츠 (JSON 문자열 또는 객체)
    initialContent: {
        type: [String, Object],
        default: null
    },
    // 에디터 최소 높이
    minHeight: {
        type: String,
        default: '200px'
    },
    // 에러 상태
    error: {
        type: Boolean,
        default: false
    }
})

const emit = defineEmits(['update:modelValue', 'blur', 'focus', 'text-change'])

const editorRef = ref(null)
let quill = null

/**
 * URL 체크 함수
 */
const isUrl = (str) => {
    try {
        return Boolean(new URL(str))
    } catch (e) {
        return false
    }
}

/**
 * 텍스트 붙여넣기 핸들러
 */
const textPasteHandler = (text) => {
    return isUrl(text)
        ? new Delta().insert(text, { link: text })
        : new Delta().insert(text)
}

/**
 * 이미지 업로드 API 호출
 */
const uploadImage = async (file) => {
    const data = await api.uploadImage(file)
    if (data.success) {
        return data.results
    }
    return null
}

/**
 * 이미지 핸들러 (드래그/드롭, 붙여넣기, 파일선택)
 */
const imageHandler = (dataUrl, type, imageData) => {
    if (!props.enableImageUpload || !quill) return

    imageData
        .minify({ quality: 1.0 })
        .then(async (miniImageData) => {
            const file = miniImageData.toFile(imageData.name)
            const uploadData = await uploadImage(file)

            if (!uploadData) return

            const imageUrl = qnaImageUrl(uploadData.uploadPath, props.imageBaseUrl)
            const range = quill.getSelection(true)
            quill.insertEmbed(range.index, 'image', imageUrl)
            quill.setSelection(range.index + 1)
        })
}

/**
 * 파일 선택으로 이미지 업로드 핸들러 설정
 */
const setupImageInputHandler = () => {
    if (!props.enableImageUpload || !quill) return

    const toolbar = quill.getModule('toolbar')
    if (!toolbar) return

    toolbar.addHandler('image', function (clicked) {
        if (!clicked) return

        let fileInput = this.container.querySelector('input.ql-image[type=file]')
        if (!fileInput) {
            fileInput = document.createElement('input')
            fileInput.setAttribute('type', 'file')
            fileInput.setAttribute('accept', 'image/png, image/gif, image/jpeg, image/bmp, image/x-icon')
            fileInput.classList.add('ql-image')

            fileInput.addEventListener('change', (e) => {
                const files = e.target.files
                if (files.length > 0) {
                    const file = files[0]
                    const type = file.type
                    const reader = new FileReader()
                    reader.onload = (e) => {
                        const dataUrl = e.target.result
                        imageHandler(dataUrl, type, new ImageData(dataUrl, type, file.name))
                        fileInput.value = ''
                    }
                    reader.readAsDataURL(file)
                }
            })
        }
        fileInput.click()
    })
}

/**
 * Quill 에디터 초기화
 */
const initQuill = () => {
    if (!editorRef.value) return

    const modules = {
        syntax: {
            hljs,
            // 코드블록에서 인식·하이라이트할 언어 목록 (미등록 언어는 plain으로 강등됨)
            languages: [
                { key: 'plain', label: 'Plain' },
                { key: 'yaml', label: 'YAML' },
                { key: 'json', label: 'JSON' },
                { key: 'bash', label: 'Bash' },
                { key: 'dockerfile', label: 'Dockerfile' },
                { key: 'properties', label: 'Properties' },
                { key: 'java', label: 'Java' },
                { key: 'javascript', label: 'JavaScript' },
                { key: 'sql', label: 'SQL' },
                { key: 'xml', label: 'HTML/XML' },
                { key: 'css', label: 'CSS' }
            ]
        },
        clipboard: {
            matchers: [
                [Node.TEXT_NODE, (node) => textPasteHandler(node.data)]
            ]
        }
    }

    // 툴바 설정
    if (!props.readOnly && props.toolbar) {
        modules.toolbar = props.toolbar
    } else {
        modules.toolbar = false
    }

    // 이미지 드래그/드롭 설정
    if (props.enableImageUpload) {
        modules.imageDropAndPaste = {
            handler: imageHandler
        }
    }

    quill = new Quill(editorRef.value, {
        modules,
        placeholder: props.readOnly ? '' : props.placeholder,
        readOnly: props.readOnly,
        theme: 'snow'
    })

    // 이미지 파일 선택 핸들러 설정
    if (props.enableImageUpload) {
        setupImageInputHandler()
    }

    // 텍스트 변경 이벤트
    if (!props.readOnly) {
        quill.on('text-change', () => {
            const { ops } = quill.getContents()
            const text = quill.getText()
            const content = JSON.stringify(ops)

            emit('update:modelValue', { text, content })
            emit('text-change', { text, content })
        })
    }

    // 포커스 이벤트
    quill.root.addEventListener('blur', () => emit('blur'))
    quill.root.addEventListener('focus', () => emit('focus'))

    // 초기 컨텐츠 설정
    if (props.initialContent) {
        setContent(props.initialContent)
    }
}

/**
 * Quill Delta 형태인지 판별
 * Delta는 { ops: [...] } 또는 [...] 형태의 객체이다.
 */
const isDelta = (value) => {
    if (Array.isArray(value)) return true
    return Boolean(value) && typeof value === 'object' && Array.isArray(value.ops)
}

/**
 * 컨텐츠 설정 (외부에서 호출 가능)
 *
 * DB에 Delta(JSON 문자열)·HTML·일반 텍스트가 섞여 저장될 수 있으므로
 * 형식을 추론하여 안전하게 렌더링한다.
 */
const setContent = (content) => {
    if (!quill) return

    // 빈 값 처리
    if (content == null || content === '') {
        quill.setContents([])
        return
    }

    // 이미 객체(Delta)인 경우
    if (typeof content === 'object') {
        if (isDelta(content)) {
            quill.setContents(content)
        }
        return
    }

    // 문자열인 경우: Delta JSON → HTML/Plain 순으로 방어적으로 처리
    if (typeof content === 'string') {
        let parsed = null
        try {
            parsed = JSON.parse(content)
        } catch (e) {
            parsed = null
        }

        if (isDelta(parsed)) {
            // 유효한 Delta JSON
            quill.setContents(parsed)
        } else if (/<[a-z][\s\S]*>/i.test(content)) {
            // HTML로 추정
            quill.clipboard.dangerouslyPasteHTML(content)
        } else {
            // 일반 텍스트
            quill.setText(content)
        }
    }
}

/**
 * 컨텐츠 가져오기
 */
const getContent = () => {
    if (!quill) return null
    return quill.getContents()
}

/**
 * 텍스트 가져오기
 */
const getText = () => {
    if (!quill) return ''
    return quill.getText()
}

/**
 * HTML 붙여넣기
 */
const pasteHtml = (html) => {
    if (!quill) return
    quill.clipboard.dangerouslyPasteHTML(html)
}

/**
 * 에디터 클리어
 */
const clear = () => {
    if (!quill) return
    quill.setContents([])
}

/**
 * Plain 코드 블록 라인 스타일 처리
 * 숫자로 시작하는 라인 → heading, 그 외 → text
 */
let plainBlockObserver = null

const stylePlainCodeBlocks = () => {
    if (!editorRef.value) return
    const blocks = editorRef.value.querySelectorAll('.ql-code-block[data-language="plain"]')
    blocks.forEach(block => {
        const text = block.textContent.trim()
        block.setAttribute('data-plain-type', /^\d+\./.test(text) ? 'heading' : 'text')
    })
}

const setupPlainBlockObserver = () => {
    if (!editorRef.value) return
    plainBlockObserver = new MutationObserver(stylePlainCodeBlocks)
    plainBlockObserver.observe(editorRef.value, {
        childList: true,
        subtree: true,
        characterData: true,
        attributes: true,
        attributeFilter: ['data-language']
    })
}

// initialContent가 변경되면 컨텐츠 업데이트
watch(() => props.initialContent, (newContent) => {
    if (newContent && quill) {
        setContent(newContent)
        nextTick(stylePlainCodeBlocks)
    }
})

onMounted(() => {
    initQuill()
    nextTick(() => {
        stylePlainCodeBlocks()
        setupPlainBlockObserver()
    })
})

onBeforeUnmount(() => {
    if (plainBlockObserver) {
        plainBlockObserver.disconnect()
        plainBlockObserver = null
    }
    if (quill) {
        quill = null
    }
})

// 외부에서 사용할 수 있도록 메서드 노출
defineExpose({
    setContent,
    getContent,
    getText,
    pasteHtml,
    clear,
    getQuill: () => quill
})
</script>

<template>
    <div class="quill-editor-wrapper" :class="{ 'quill-error': error, 'quill-readonly': readOnly }">
        <div ref="editorRef" :style="{ minHeight: readOnly ? 'auto' : minHeight }"></div>
    </div>
</template>

<style scoped>


.quill-editor-wrapper {
    width: 100%;
}

.quill-editor-wrapper :deep(.ql-container) {
    font-size: 16px;
    font-family: inherit;
}

.quill-editor-wrapper :deep(.ql-editor) {
    line-height: 1.75;
}

.quill-editor-wrapper.quill-readonly :deep(.ql-container.ql-snow) {
    border: none;
}

.quill-editor-wrapper.quill-readonly :deep(.ql-editor) {
    padding: 0;
}

.quill-editor-wrapper.quill-error :deep(.ql-container.ql-snow) {
    border-color: rgb(255, 54, 54);
}

.quill-editor-wrapper :deep(.ql-toolbar.ql-snow) {
    border-radius: 8px 8px 0 0;
    border-color: #d9d9d9;
}

.quill-editor-wrapper :deep(.ql-container.ql-snow) {
    border-radius: 0 0 8px 8px;
    border-color: #d9d9d9;
}

.quill-editor-wrapper :deep(.ql-editor:focus) {
    outline: none;
}

.quill-editor-wrapper :deep(.ql-editor.ql-blank::before) {
    color: #9ca3af;
    font-style: normal;
}

/* 코드 블록 스타일 */
.quill-editor-wrapper :deep(.ql-code-block-container) {
    border-radius: 0.8rem !important;
    padding: 1rem !important;
}

.quill-editor-wrapper :deep(.ql-code-block-container .ql-ui) {
    right: 1rem !important;
    top: 1rem !important;
}

.quill-editor-wrapper :deep(.ql-code-block-container .ql-ui) {
    color: rgb(156, 163, 175);
}

.quill-editor-wrapper :deep(.ql-code-block-container select.ql-ui option) {
    background-color: white;
    color: rgb(55, 65, 81);
}

/* Plain 언어 코드 블록 — 카드 스타일 */
.quill-editor-wrapper :deep(.ql-code-block-container:has(.ql-code-block[data-language="plain"])) {
    background-color: rgb(248, 250, 252) !important;
    border: 1px solid rgb(241, 245, 249) !important;
    color: rgb(30, 41, 59) !important;
}

.quill-editor-wrapper :deep(.ql-code-block-container:has(.ql-code-block[data-language="plain"]) .ql-ui) {
    color: rgb(148, 163, 184);
}

/* Plain 코드 블록 — 숫자로 시작하는 라인 (heading) */
.quill-editor-wrapper :deep(.ql-code-block[data-language="plain"][data-plain-type="heading"]) {
    font-size: 0.875rem !important;
    font-weight: 700 !important;
    color: rgb(30, 41, 59) !important;
    padding-top: 0.75rem !important;
}

/* Plain 코드 블록 — 일반 텍스트 라인 */
.quill-editor-wrapper :deep(.ql-code-block[data-language="plain"][data-plain-type="text"]) {
    font-size: 0.875rem !important;
    font-weight: 400 !important;
    color: rgb(71, 85, 105) !important;
    line-height: 1.625 !important;
}

.quill-editor-wrapper.quill-readonly :deep(.ql-code-block-container select) {
    pointer-events: none !important;
    appearance: none !important;
    padding: 0 10px !important;
}
</style>
