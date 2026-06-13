/**
 * 가격 표시 포맷.
 * 0원(무료) 강의는 "무료" 로, 그 외엔 ₩ + 천단위 콤마로 표시.
 *
 * @param {number} price
 * @returns {string} 예) 0 → "무료", 38500 → "₩38,500"
 */
export function formatPrice(price) {
  const n = Number(price) || 0;
  return n <= 0 ? "무료" : `₩${n.toLocaleString()}`;
}
