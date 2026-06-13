// 이미지 URL 정리 유틸
// R2 전환 후 업로드 이미지는 전체 URL(예: https://cdn.ddarahakit.com/...)을 반환한다.
// 기존(PVC 시절) 상대경로 값은 레거시 방식으로 폴백해 하위호환을 유지한다.

/** 값이 http(s) 절대 URL 인지 */
export const isAbsoluteUrl = (v) => /^https?:\/\//i.test(v || '');

/**
 * 사용자 이미지(프로필/리뷰/댓글 등) URL 해석.
 * - 절대 URL(R2/외부) → 그대로 사용
 * - 상대경로(로컬 업로드) → 백엔드 display 엔드포인트로 서빙({base}/community/display?fileName={value})
 *   (nginx에 /images 라우팅이 없어 SPA fallback으로 깨지던 문제 해결. 공백·한글 파일명은 인코딩.)
 */
export function userImageUrl(value, base = import.meta.env.VITE_IMG_BASE_URL) {
  if (!value) return '';
  return isAbsoluteUrl(value)
    ? value
    : `${base}/community/display?fileName=${encodeURIComponent(value)}`;
}

/**
 * QnA/에디터 업로드 이미지 URL 해석.
 * - 절대 URL(R2) → 그대로
 * - 상대경로(레거시) → {base}/qna/display?fileName={value}
 */
export function qnaImageUrl(value, base = import.meta.env.VITE_IMG_BASE_URL) {
  if (!value) return '';
  return isAbsoluteUrl(value)
    ? value
    : `${base}/qna/display?fileName=${encodeURIComponent(value)}`;
}
