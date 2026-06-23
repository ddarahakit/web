/**
 * 결제 ID 생성
 *
 * @returns paymentId를 생성해 반환한다.
 */
const randomId = () => {
    const id = [...crypto.getRandomValues(new Uint32Array(2))]
        .map((word) => word.toString(16).padStart(8, "0"))
        .join("");

    const now = new Date();
    const year = now.getFullYear();
    const month = String(now.getMonth() + 1).padStart(2, '0');
    const day = String(now.getDate()).padStart(2, '0');
    const hours = String(now.getHours()).padStart(2, '0');
    const minutes = String(now.getMinutes()).padStart(2, '0');
    const seconds = String(now.getSeconds()).padStart(2, '0');

    const formattedDate = `${year}-${month}-${day}_${hours}-${minutes}-${seconds}`;

    return `imp_${id}_${formattedDate}`;
};


// 시간을 "X시간 Y분" 형태로 변환
const formattedPlayTime = ((playTime) => {
    const minutes = playTime;
    const hours = Math.floor(minutes / 60);  // 시간
    const remainingMinutes = minutes % 60;   // 남은 분

    if (hours > 0) {
        return `${hours}시간 ${remainingMinutes > 0 ? remainingMinutes + '분' : ''}`;
    } else {
        return `${remainingMinutes}분`;
    }
});


export default {
    randomId,
    formattedPlayTime
}
