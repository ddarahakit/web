<script setup>
import { useRouter } from 'vue-router'

const router = useRouter()

// 강의 목록 이동 (실제 수업)
const goCourseList = () => router.push({ name: 'courseList' })

// 수업 진행 방식 이동
const goProjects = () => router.push({ name: 'projectManagement' })
</script>

<template>
    <!-- 포트폴리오 소개 -->
    <section class="pt-36 pb-24 px-6 hero-bg min-h-[70vh]">
        <div class="max-w-7xl mx-auto">
            <div class="text-center mb-16">
                <span
                    class="px-4 py-1.5 rounded-full bg-blue-50 border border-blue-100 text-brand text-sm font-semibold mb-4 inline-block">
                    PORTFOLIO
                </span>
                <h2 class="text-3xl md:text-4xl font-bold mb-4 text-gray-900">직접 만든 교육 포트폴리오</h2>
                <p class="text-gray-500 max-w-2xl mx-auto leading-relaxed">
                    제가 진행한 수업과 그 진행 방식을, 실제로 동작하는 웹 서비스로 직접 구현했습니다.
                </p>
            </div>

            <div class="grid md:grid-cols-2 gap-6">
                <!-- 실제 수업 -->
                <div class="bg-white rounded-3xl border border-gray-100 p-8 shadow-sm card-hover">
                    <div class="w-14 h-14 rounded-2xl bg-blue-50 flex items-center justify-center mb-6">
                        <i class="fa-solid fa-circle-play text-brand text-2xl"></i>
                    </div>
                    <h3 class="text-xl font-bold text-gray-900 mb-3">실제 수업</h3>
                    <p class="text-gray-500 leading-relaxed mb-6">
                        이 영상들은 온라인 강의가 아니라 현장(오프라인)에서 진행한 실제 수업을, 디스코드 화면 공유 방송을 학생이 직접 녹화한 것입니다.
                        과목별 섹션·강의 커리큘럼으로 정리되어 있고, 강의를 선택하면 녹화 영상이 바로 재생됩니다.
                        별도 마이크 없이 진행되어 음성·화면 품질이 다소 고르지 않을 수 있는 점 양해 부탁드립니다.
                    </p>
                    <button @click="goCourseList" class="text-brand font-semibold hover:underline">
                        실제 수업 보러가기 <i class="fa-solid fa-chevron-right ml-1 text-xs"></i>
                    </button>
                </div>

                <!-- 수업 진행 방식 (준비 중) -->
                <div class="bg-white rounded-3xl border border-gray-100 p-8 shadow-sm card-hover">
                    <div class="w-14 h-14 rounded-2xl bg-blue-50 flex items-center justify-center mb-6">
                        <i class="fa-solid fa-chalkboard-user text-brand text-2xl"></i>
                    </div>
                    <h3 class="text-xl font-bold text-gray-900 mb-3">수업 진행 방식</h3>
                    <p class="text-gray-500 leading-relaxed mb-6">
                        수업 자료 완비부터 학생 산출물 관리, 직접 배포·운영해보는 실무 경험까지 —
                        제가 수업을 어떻게 이끌어가는지 소개합니다.
                    </p>
                    <button @click="goProjects" class="text-brand font-semibold hover:underline">
                        수업 진행 방식 보러가기 <i class="fa-solid fa-chevron-right ml-1 text-xs"></i>
                    </button>
                </div>
            </div>
        </div>
    </section>
</template>

<style scoped>
.hero-bg {
    background: radial-gradient(circle at 50% 50%, rgba(20, 188, 237, 0.08) 0%, rgba(255, 255, 255, 0) 70%);
}
</style>
