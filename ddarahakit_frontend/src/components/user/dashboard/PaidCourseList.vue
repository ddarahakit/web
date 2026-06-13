<script setup>
import { reactive, onMounted } from 'vue'
import api from '@/api/user'
import commonUtil from '@/utils/commonUtil'


// 구매 코스 목록 객체
let paidCourseList = reactive([])

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
 * 컴포넌트 마운트
 *
 */
onMounted(() => {
    getPaidCourseList()

    document.title = '수강 중 강의 | 따라학잇'
})
</script>

<template>
    <!-- <div class="col-md-6"> -->
    <div v-if="paidCourseList.length > 0">
        <div v-for="(course, index) in paidCourseList" :key="index">
            <div class="d-flex flex-row align-items-start justify-content-start">
                <div class="paid_image">
                    <a :href="`/lecture/${course.idx}/${course.nextLectureIdx}`">
                        <div><img :src="`${course.image}`" alt=""></div>
                    </a>
                </div>
                <div class="dabhaord paid_content">
                    <div class="aside_lecture_container">
                        <div class="paid_title">
                            <a :href="`/lecture/${course.idx}/${course.nextLectureIdx}`">
                                {{ course.name }}
                            </a>
                        </div>
                        <div class="aside_lecture_info">
                            <div>
                                <p class="aside_lecture_complete">
                                    시간:
                                    {{commonUtil.formattedPlayTime(course.sections
                                        .flatMap(section => section.lectures)
                                        .filter(lecture => lecture.complete)
                                        .reduce((sum, lecture) => sum + lecture.playTime, 0))}}
                                    / {{commonUtil.formattedPlayTime(course.sections.reduce((sum, section) =>
                                        sum + section.lectures.reduce((sum, lecture) => sum + lecture.playTime, 0), 0))}}
                                </p>
                            </div>
                        </div>

                        <div class="progress_container">
                            <div class="progress_bar_container">
                                <div class="progress_bar progress_bar_base"></div>
                                <div class="progress_bar progress_bar_ing"
                                    :style="`width: ${(course.sections.flatMap(section => section.lectures)
                                        .filter(lecture => lecture.complete).length /
                                        course.sections.reduce((sum, section) => sum + section.lectures.length, 0) * 100).toFixed(0)}%`"></div>
                                <div class="progress_bar progress_bar_devider"></div>
                                <div class="progress_currentpoint"
                                    :style="`left: ${(course.sections.flatMap(section => section.lectures)
                                        .filter(lecture => lecture.complete).length /
                                        course.sections.reduce((sum, section) => sum + section.lectures.length, 0) * 100).toFixed(0)}%`"></div>
                            </div>
                            <div class="progress_text_container">
                                <p class="progress_startpoint">{{
                                    (course.sections.flatMap(section => section.lectures)
                                        .filter(lecture => lecture.complete).length /
                                        course.sections.reduce((sum, section) => sum + section.lectures.length, 0) * 100
                                    ).toFixed(0)}}% 완료</p>
                                <p class="progress_endpoint">
                                    {{course.sections.flatMap(section => section.lectures).filter(lecture =>
                                        lecture.complete).length}}강 /
                                    {{course.sections.reduce((sum, section) => sum + section.lectures.length, 0)}} 강</p>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
            <hr class="hr_devider">
        </div>
    </div>

    <div v-else class="latest d-flex flex-row align-items-start justify-content-start">
        <div class="profile latest_content">
            <div class="aside_lecture_container">
                <div class="latest_title">
                    <h4>
                        아직 수강 중인 강의가 없습니다
                    </h4>
                </div>
            </div>
        </div>
    </div>


</template>

<style scoped>
.dabhaord.paid_content {
    width: 100%
}

.paid_content {
    padding-left: 21px;
    margin-top: -4px;
}

.paid_image div {
    width: 13rem;
    height: 9rem;
    border-radius: 3px;
    overflow: hidden;
}

.paid_image div img {
    max-width: 100%;
}


.paid_title a {
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

.paid_title a:hover {
    color: #14bdee;
}


.progress_container {
    width: 100%;
    margin: 0 auto;
    font-size: 1rem;
    color: #868686;
}

.progress_text_container {
    position: relative;
    overflow: hidden;
    margin-top: 0.7rem;
}

.progress_startpoint {
    float: left;
}

.progress_endpoint,
.progress_startpoint {
    display: block;
    margin: 0;
}

.progress_endpoint {
    float: right;
}

.progress_bar_container {
    position: relative;
    height: 0.6rem;
    margin-top: 0.5rem;
}

.progress_bar_container>* {
    position: absolute;
}

.progress_bar {
    top: 0;
    left: 0;
    height: 0.6rem;
    border-radius: 1rem;
}

.progress_bar_base {
    width: 100%;
    opacity: 0.6;
    background-color: #d5d9e5;
}

.progress_bar_ing {
    background-color: #14bdee;
}

.progress_bar_devider {
    width: 100%;
    background-image: repeating-linear-gradient(90deg,
            transparent,
            transparent calc((100% / 5) - 0.12rem),
            #fff calc(100% / 5 - 0.12rem),
            #fff calc(100% / 5));
}

.progress_currentpoint {
    top: 50%;
    width: 1.3rem;
    height: 1.3rem;
    border: 0.2rem solid #fff;
    border-radius: 1.3rem;
    background-color: #14bdee;
}

.progress_currentpoint,
.progress_message {
    -webkit-transform: translate(-50%, -50%);
    transform: translate(-50%, -50%);
}

.progress_message {
    top: 2.65rem;
    max-height: 3rem;
    margin-left: -0.1rem;
}

.hr_devider {
    margin: 0 0 1.5rem 0;
}
</style>