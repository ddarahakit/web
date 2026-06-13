// 이미지 URL 정리 유틸
// R2 전환 후 업로드 이미지는 전체 URL(예: https://cdn.ddarahakit.com/...)을 반환한다.
// 기존(PVC 시절) 상대경로 값은 레거시 방식으로 폴백해 하위호환을 유지한다.

/** 값이 http(s) 절대 URL 인지 */
export const isAbsoluteUrl = (v) => /^https?:\/\//i.test(v || '');

/**
 * 사용자 이미지(프로필/리뷰/댓글 등) URL 해석.
 * - 절대 URL(R2/외부) → 그대로 사용
 * - 상대경로(레거시) → /images 프리픽스(기존 방식)
 */
export function userImageUrl(value) {
  if (!value) return '';
  return isAbsoluteUrl(value) ? value : `/images${value}`;
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
