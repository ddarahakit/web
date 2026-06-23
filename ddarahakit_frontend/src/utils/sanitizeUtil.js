import DOMPurify from 'dompurify'

/**
 * 사용자 입력 HTML(에디터 콘텐츠·댓글·게시글 등)을 DOM 에 삽입하기 전 정화한다.
 *
 * Quill 의 dangerouslyPasteHTML / v-html 처럼 신뢰할 수 없는 HTML 을 렌더링할 때 사용.
 * - 제거: <script>, on* 이벤트 핸들러, javascript: 등 XSS 벡터
 * - 보존: 서식 태그, 코드 하이라이트(span.class), 이미지(http/https URL — 에디터가 백엔드 업로드 URL 사용)
 *
 * @param {string} html 정화할 HTML 문자열
 * @returns {string} 안전한 HTML 문자열
 */
export function sanitizeHtml(html) {
    if (html == null) return ''
    return DOMPurify.sanitize(String(html), {
        // 링크의 새 탭/보안 속성 허용
        ADD_ATTR: ['target', 'rel'],
    })
}
