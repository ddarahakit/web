<script setup>
import { onMounted, ref, reactive, computed } from 'vue'
import { useRoute } from 'vue-router'
import api from '@/api/user'
import reviewApi from '@/api/review'

//라우트 정보 객체
const route = useRoute();


//리뷰 목록 리스트 정보 객체
let reviewList = reactive([])

// 구매 코스 목록 객체
let paidCourseList = reactive([])

//리뷰 입력 객체
const reviewInput = reactive({
    comment: '', //리뷰 내용
    rating: 0, //리뷰 점수
})
const editingReviewId = ref(null); // 현재 수정 중인 리뷰 ID


//에러 객체 정보 객체
const reviewInputError = reactive({
    comment: {
        errorMessage: null,
        isValid: false
    },
    rating: {
        errorMessage: null,
        isValid: false
    }
})

/**
 * 폼 유효성 검사
 */
const isFormValid = computed(() =>
    reviewInputError.comment.isValid && reviewInputError.rating.isValid
);


/**
 * 폼 컨트롤 객체
 * 버튼 활성화 및 텍스트를 제어한다.
 */
const formCntrObj = reactive({
    noStyl: { text: 'text-tip error' },
    submitBtn: { disabled: true }
})




/**
 * 리뷰 점수 유효성 룰
 */
const reviewRatingRules = () => {
    if (reviewInput.rating !== 0) {
        reviewInputError.rating.errorMessage = null
        reviewInputError.rating.isValid = true
    } else {
        reviewInputError.rating.isValid = false
        reviewInputError.rating.errorMessage = '리뷰 점수를 입력해주세요.'
    }
}
/**
 * 리뷰 내용 유효성 룰
 */
const reviewCommentRules = () => {
    if (reviewInput.comment !== '' && reviewInput.comment !== '\n') {
        reviewInputError.comment.errorMessage = null
        reviewInputError.comment.isValid = true
    } else {
        reviewInputError.comment.errorMessage = '리뷰 내용을 입력해주세요.'
        reviewInputError.comment.isValid = false
    }
}



/**
 * 코스 목록 조회
 */
const getMyReviews = async () => {
    //API: 코스 목록 조회
    const data = await api.getMyReviewList()

    if (data.success) {
        //코스 목록 추가
        if (data.results) {
            //조회 결과
            const list = data.results

            if (list.length) {
                reviewList.push(...list)
            }
        }
    } else {
        //코스 목록 초기화
        reviewList.splice(0)
    }
}



/**
 * 리뷰 삭제하기
 */
const deleteReview = async (courseIdx) => {
    //API: 리뷰 삭제하기
    const data = await reviewApi.reviewDelete(courseIdx)

    if (data.success) {
        //리뷰 삭제
        window.location.href = '/user/dashboard?tab=review'
    }
}


/**
 * 리뷰 수정하기
 */
const editReview = async (review) => {
    reviewInput.comment = review.review.comment
    reviewInputError.comment.errorMessage = null
    reviewInputError.comment.isValid = true
    reviewInput.rating = review.review.rating
    reviewInputError.rating.errorMessage = null
    reviewInputError.rating.isValid = true
    editingReviewId.value = review.courseIdx; // 현재 수정 중인 리뷰 ID 설정
}



/**
 * 리뷰 수정하기
 */

const reviewUpdate = async () => {
    //API: 리뷰 수정하기
    const data = await reviewApi.reviewUpdate(editingReviewId.value, reviewInput)
    if (data.success) {
        window.location.href = '/user/dashboard?tab=review'
    }
}


/**
 * 구매 코스 목록 조회
 */
const getPaidCourseList = async () => {
    //API: 구매 코스 목록 조회
    const data = await api.getPaidCourseList()
    if (data.success) {
        //구매 코스 목록 추가
        if (data.results) {
            //조회 결과
            const list = data.results

            if (list.length) {
                paidCourseList.push(...list)
            }
        }
    } else {
        //코스 목록 초기화
        paidCourseList.splice(0)
    }
}

/**
 *  리뷰 작성하러 이동하기
 */
const goToReview = (courseIdx) => {
    window.location.href = `/course/${courseIdx}?tab=reviews`
}

/**
 * 컴포넌트 마운트
 *
 */
onMounted(() => {
    getMyReviews()

    if (reviewList.length === 0) {
        getPaidCourseList()
    }
    document.title = '내 리뷰 관리 | 따라학잇'
})


</script>
<template>
    <template v-if="reviewList.length > 0">
        <div v-for="(review, index) in reviewList" :key="index">
            <template v-if="editingReviewId !== review.courseIdx">
                <div class="review d-flex flex-row align-items-start justify-content-start">
                    <div class="review_image">
                        <a :href="`/course/${review.courseIdx}`">
                            <div><img :src="`${review.image}`" alt=""></div>
                        </a>
                    </div>
                    <div class="profile review_content">
                        <div class="aside_lecture_container">
                            <div class="review_title">
                                <a :href="`/course/${review.courseIdx}`">
                                    {{ review.name }}
                                </a>
                            </div>
                        </div>
                        <div class="comment_content">
                            <div
                                class="comment_title_container d-flex flex-row align-items-center justify-content-start">
                                <div class="comment_rating">
                                    <div :class="`rating_r rating_r_${review.review.rating}`">
                                        <i></i><i></i><i></i><i></i><i></i>
                                    </div>
                                </div>
                                <div class="ms-3">
                                    <button @click="editReview(review)" class="review_btn">
                                        수정
                                    </button>
                                    <button @click="deleteReview(review.courseIdx)" class="review_btn ms-1">
                                        삭제
                                    </button>
                                </div>
                            </div>
                            <div class="comment_text">
                                <p>{{ review.review.comment }}</p>
                            </div>
                        </div>
                    </div>
                </div>
            </template>

            <template v-else>
                <div class="review d-flex flex-row align-items-start justify-content-start">
                    <div class="review_image">
                        <a :href="`/course/${review.courseIdx}`">
                            <div><img :src="`${review.image}`" alt=""></div>
                        </a>
                    </div>
                    <div class="profile review_content">
                        <div class="aside_lecture_container">
                            <div class="review_title">
                                <a :href="`/course/${review.courseIdx}`">
                                    {{ review.name }}
                                </a>
                            </div>
                        </div>
                        <div class="comment_content">
                            <div
                                class="comment_title_container d-flex flex-row align-items-center justify-content-start">
                                <div class="comment_rating">
                                    <div class="rating">
                                        <input type="radio" name="stars" v-model="reviewInput.rating" :value="5" id="5"
                                            @change="reviewRatingRules">
                                        <label for="5">☆</label>
                                        <input type="radio" name="stars" v-model="reviewInput.rating" :value="4" id="4"
                                            @change="reviewRatingRules">
                                        <label for="4">☆</label>
                                        <input type="radio" name="stars" v-model="reviewInput.rating" :value="3" id="3"
                                            @change="reviewRatingRules">
                                        <label for="3">☆</label>
                                        <input type="radio" name="stars" v-model="reviewInput.rating" :value="2" id="2"
                                            @change="reviewRatingRules">
                                        <label for="2">☆</label>
                                        <input type="radio" name="stars" v-model="reviewInput.rating" :value="1" id="1"
                                            @change="reviewRatingRules">
                                        <label for="1">☆</label>
                                    </div>
                                </div>
                                <div class="ms-3">
                                    <button @click="reviewUpdate" class="review_btn" :disabled="!isFormValid">
                                        수정하기
                                    </button>
                                </div>
                            </div>
                            <div class="comment_text">
                                <textarea v-model="reviewInput.comment" name="comments" @blur="reviewCommentRules"
                                    @focusin="reviewRatingRules" @input="reviewCommentRules"
                                    :class="{ error: reviewInputError.comment.errorMessage }"
                                    class="comment_input comment_textarea" required="required"></textarea>

                            </div>
                            <p :class="formCntrObj['noStyl'].text">
                                {{ reviewInputError.comment.errorMessage }}
                            </p>
                        </div>
                    </div>
                </div>
            </template>
            <hr class="hr_devider">
        </div>
    </template>
    <template v-else>
        <div class="profile latest_content">
            <div class="aside_lecture_container">
                <div class="latest_title">
                    <h4 class="mb-2">
                        아직 작성한 리뷰가 없습니다.

                    </h4>
                    <p class="mb-3">
                        강의를 수강하고 리뷰를 작성해보세요.
                    </p>
                </div>
            </div>
        </div>
        <div class="row">
            <div v-for="(course, index) in paidCourseList" :key="index" class="col-md-6">
                <div class="review d-flex flex-row align-items-start justify-content-start">
                    <div class="review_image">
                        <a :href="`/course/${course.idx}?tab=reviews`">
                            <div><img :src="`${course.image}`" alt=""></div>
                        </a>
                    </div>
                    <div class="profile review_content">
                        <div class="aside_lecture_container">
                            <div class="review_title">
                                <a :href="`/course/${course.idx}?tab=reviews`">
                                    {{ course.name }}
                                </a>
                            </div>
                        </div>
                        <div class="comment_content">
                            <div
                                class="comment_title_container d-flex flex-row align-items-center justify-content-start">
                                <div>
                                    <button @click="goToReview(course.idx)" class="review_btn">
                                        리뷰 작성하기
                                    </button>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>
                <hr class="hr_devider">
            </div>
        </div>

    </template>
</template>


<style scoped>
.comment_input {
    width: 100%;
    height: 46px;
    border: solid 1px #d9d9d9;
    border-radius: 3px;
    padding-left: 19px;
    font-size: 14px;
    font-weight: 400;
    color: #2c3145;
}

.comment_textarea {
    width: 100%;
    height: 100px;
    padding-top: 15px;
}

.comment_textarea.error {
    border: 1px solid rgb(255, 54, 54);
}


.rating {
    display: flex;
    flex-direction: row-reverse;
    justify-content: left;
}


.rating>input {
    display: none;
}

.rating>label {
    position: relative;
    width: 1.1em;
    font-size: 20px;
    color: #FFD700;
    cursor: pointer;
}

.rating>label::before {
    content: "\2605";
    position: absolute;
    opacity: 0;
}

.rating>label:hover:before,
.rating>label:hover~label:before {
    opacity: 1 !important;
}

.rating>input:checked~label:before {
    opacity: 1;
}

.rating:hover>input:checked~label:before {
    opacity: 0.4;
}


.review_btn {
    cursor: pointer;
    padding: 3px 4px;
    font-size: 9px;
    border-radius: .2rem;
    color: #6c757d;
    border: 1px solid transparent;
    border-color: #6c757d;
    transition: none;
    user-select: none;
    background-color: transparent;
    text-align: center;
    text-decoration: none;
    vertical-align: middle;
    display: inline-block;
    font-weight: 400;
    line-height: 1.5;
}

.review_btn:disabled {
    pointer-events: none;
    opacity: 0.26;
}

.review_btn:hover {
    color: white;
    background-color: #6c757d;
}

.profile.review_content {
    width: 100%
}

.review_content {
    padding-left: 21px;
    margin-top: -4px;
}

.row .review {
    align-items: center !important;
}

.row .review_content {
    padding-left: 21px;
    margin-top: -20px;
}

.review_image div {
    width: 10rem;
    height: 7rem;
    border-radius: 3px;
    overflow: hidden;
}


.review_image div img {
    max-width: 100%;
}

.review_title a {
    font-family: 'FontAwesome';
    font-size: 16px;
    font-weight: 700;
    color: #383749;
    line-height: 1.625;
    -webkit-transition: all 200ms ease;
    -moz-transition: all 200ms ease;
    -ms-transition: all 200ms ease;
    -o-transition: all 200ms ease;
    transition: all 200ms ease;
}

.review_title a:hover {
    color: #14bdee;
}


.rating_r {
    margin-top: 4px;
}

.rating_r i::before {
    font-family: 'FontAwesome';
    content: "\f006";
    font-style: normal;
    font-size: 17px;
    margin-right: 4px;
    color: #14bdee;
}

.rating_r_1 i:first-child::before {
    content: "\f005";
}

.rating_r_2 i:first-child::before {
    content: "\f005";
}

.rating_r_2 i:nth-child(2)::before {
    content: "\f005";
}

.rating_r_3 i:first-child::before {
    content: "\f005";
}

.rating_r_3 i:nth-child(2)::before {
    content: "\f005";
}

.rating_r_3 i:nth-child(3)::before {
    content: "\f005";
}

.rating_r_4 i:first-child::before {
    content: "\f005";
}

.rating_r_4 i:nth-child(2)::before {
    content: "\f005";
}

.rating_r_4 i:nth-child(3)::before {
    content: "\f005";
}

.rating_r_4 i:nth-child(4)::before {
    content: "\f005";
}

.rating_r_5 i:first-child::before {
    content: "\f005";
}

.rating_r_5 i:nth-child(2)::before {
    content: "\f005";
}

.rating_r_5 i:nth-child(3)::before {
    content: "\f005";
}

.rating_r_5 i:nth-child(4)::before {
    content: "\f005";
}

.rating_r_5 i:nth-child(5)::before {
    content: "\f005";
}


.comments_container {
    margin-top: 80px;
}

.comment_item {
    border-bottom: solid 1px #e5e5e5;
    padding-top: 31px;
    padding-bottom: 16px;
}

.comment_image div {
    width: 60px;
    height: 60px;
    border-radius: 50%;
    border: 1px solid #efefef;
    overflow: hidden;
}

.comment_image div img {
    max-width: 100%;
}

.comment_image div:before {
    display: inline-block;
    height: 100%;
    content: "";
    vertical-align: middle;
}


.comment_content {
    width: 100%;
}

.comment_author::after {
    display: inline-block;
    content: '-';
    margin-left: 6px;
}

.comment_author a {
    font-family: 'FontAwesome';
    font-size: 18px;
    font-weight: 700;
    color: #384158;
    -webkit-transition: all 200ms ease;
    -moz-transition: all 200ms ease;
    -ms-transition: all 200ms ease;
    -o-transition: all 200ms ease;
    transition: all 200ms ease;
}

.comment_author a:hover {
    color: #14bdee;
}

.comments_container .rating_r {
    margin-top: 3px;
    margin-left: 5px;
}

.rating_r i::before {
    color: #ffc80a;
}

.comment_text {
    margin-top: 12px;
    white-space: pre-line;
}

.comment_edit_btn {
    padding: 0px 5px;
    margin-left: 10px;
    width: 100%;
    height: 20px;
    background: #14bdee;
    font-size: 14px;
    font-weight: 500;
    color: #FFFFFF;
    cursor: pointer;
    border: none;
    outline: none;
    border-radius: 3px;
}

.comment_remove_btn {
    padding: 0px 5px;
    margin-left: 20px;
    width: 100%;
    height: 20px;
    background: #14bdee;
    font-size: 14px;
    font-weight: 500;
    color: #FFFFFF;
    cursor: pointer;
    border: none;
    outline: none;
    border-radius: 3px;
}


.hr_devider {
    margin: 0 0 1.5rem 0;
}
</style>