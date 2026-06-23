<script setup>
import { ref, onMounted } from "vue";
import Quill from "quill";
import "quill/dist/quill.snow.css"; // 원하는 테마 import
import hljs from "highlight.js";
import "highlight.js/styles/atom-one-dark.css";
import { sanitizeHtml } from "@/utils/sanitizeUtil";


//props 설정
const props = defineProps({
    content: { type: String, default: '' },
})

const commentOptions = {
    readOnly: true, // 읽기 전용 모드
    modules: {
        syntax: { hljs },
        toolbar: null // 툴바 제거
    },
    theme: "snow",
};

const contentEditor = ref(null);
let contentQuill = null;

/**
 * Quill Delta 형태인지 판별
 */
const isDelta = (value) => {
    if (Array.isArray(value)) return true
    return Boolean(value) && typeof value === 'object' && Array.isArray(value.ops)
}

/**
 * 컨텐츠 설정
 *
 * DB에 Delta(JSON 문자열)·HTML·일반 텍스트가 섞여 저장될 수 있으므로
 * 형식을 추론하여 안전하게 렌더링한다.
 */
const setContent = (content) => {
    if (!contentQuill) return

    if (content == null || content === '') {
        contentQuill.setContents([])
        return
    }

    let parsed = null
    try {
        parsed = JSON.parse(content)
    } catch (e) {
        parsed = null
    }

    if (isDelta(parsed)) {
        contentQuill.setContents(parsed)
    } else if (/<[a-z][\s\S]*>/i.test(content)) {
        contentQuill.clipboard.dangerouslyPasteHTML(sanitizeHtml(content))
    } else {
        contentQuill.setText(content)
    }
}

/**
 * 컴포넌트 마운트
 *
 */
onMounted(() => {
    contentQuill = new Quill(contentEditor.value, commentOptions);
    setContent(props.content)
});

</script>
<template>
    <div ref="contentEditor"></div>
</template>
<style scoped>
#content-section .ql-container {
    border: none;
}
</style>