/**
 * 코스 평균 평점(별점) 계산. 리뷰 0건이면 "0.0".
 * @param {{ totalReviewsCount:number, rating1:number, rating2:number, rating3:number, rating4:number, rating5:number }} course
 * @returns {string} 소수 1자리 문자열 (예: "4.3")
 */
export function getAverageRating(course) {
    if (course.totalReviewsCount === 0) return "0.0";
    return (
        Math.ceil((course.rating5 * 5 + course.rating4 * 4 + course.rating3 * 3 + course.rating2 * 2 + course.rating1) / course.totalReviewsCount * 10) / 10
    ).toFixed(1);
}

/**
 * 평점 분포 막대 너비(%) 계산. 리뷰 0건이면 "0%".
 * @param {number} rating 해당 별점(예: rating5)의 개수
 * @param {number} totalReviewsCount 전체 리뷰 수
 * @returns {string} 예: "60%"
 */
export function getRatingWidth(rating, totalReviewsCount) {
    if (totalReviewsCount === 0) return "0%";
    return (rating * 100 / totalReviewsCount) + '%';
}
