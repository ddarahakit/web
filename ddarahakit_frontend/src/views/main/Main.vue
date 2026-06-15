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
            <div class="text-center mb-3">
                <span
                    class="px-4 py-1.5 rounded-full bg-blue-50 border border-blue-100 text-brand text-sm font-semibold mb-4 inline-block">
                    PORTFOLIO
                </span>
            </div>

            <!-- 상단 안내 (수업 녹화 · 테스트 결제) -->
            <div class="max-w-3xl mx-auto mb-16 space-y-3">
                <!-- 수업 녹화 안내 -->
                <div class="bg-blue-50/60 border border-blue-100 rounded-2xl p-4 md:p-5 flex items-start gap-3 text-left">
                    <i class="fa-solid fa-circle-info text-brand mt-0.5 shrink-0"></i>
                    <p class="text-sm text-slate-600 leading-relaxed">
                        <span class="font-semibold text-slate-800">안내</span> ·
                        수업 영상들은 온라인 강의가 아니라 <b class="font-semibold text-slate-700">현장(오프라인)에서 진행한 실제 수업</b>을 녹화한 것입니다.
                        뒷자리에서 화면이 잘 보이지 않는 학생들을 위해 <b class="font-semibold text-slate-700">디스코드로 화면을 공유</b>하며 수업했고,
                        그 방송을 <b class="font-semibold text-slate-700">학생이 직접 녹화</b>했습니다.
                        <b class="font-semibold text-slate-700">별도 마이크 없이</b> 진행되어 음성·화면 품질이 다소 고르지 않을 수 있는 점 양해 부탁드립니다.
                    </p>
                </div>
                <!-- 테스트 결제 안내 -->
                <div class="bg-blue-50/60 border border-blue-100 rounded-2xl p-4 md:p-5 flex items-start gap-3 text-left">
                    <i class="fa-solid fa-circle-info text-brand mt-0.5 shrink-0"></i>
                    <p class="text-sm text-slate-600 leading-relaxed">
                        <span class="font-semibold text-slate-800">안내</span> ·
                        코스에 표시된 금액으로 결제할 수 있지만, 현재 결제는 <b class="font-semibold text-slate-700">포트원(PortOne)의 토스페이</b>로 연동한 <b class="font-semibold text-slate-700">테스트 결제</b>입니다.
                        실제로 <b class="font-semibold text-slate-700">금액이 청구되거나 돈이 빠져나가지 않으니</b> 안심하고 결제 과정을 체험해 보세요.
                    </p>
                </div>
            </div>

            <div class="grid md:grid-cols-2 gap-6">
                <!-- 실제 수업 -->
                <div class="bg-white rounded-3xl border border-gray-100 p-8 shadow-sm card-hover">
                    <h3 class="text-xl font-bold text-gray-900 mb-3">실제 수업</h3>
                    <p class="text-gray-500 leading-relaxed mb-6">
                        지금 보고 계신 이 사이트가 곧 수업 결과물입니다.
                        회원가입·이메일 인증, 로그인, 강의 시청, 장바구니·결제, 커뮤니티 게시판, 로드맵까지 —
                        실제로 동작하는 기능들을 수업에서 학생들과 함께 하나씩 만들어 갑니다.
                        강의 영상은 그 과정을 현장에서 그대로 녹화한 것입니다.
                    </p>
                    <button @click="goCourseList" class="text-brand font-semibold hover:underline">
                        실제 수업 보러가기 <i class="fa-solid fa-chevron-right ml-1 text-xs"></i>
                    </button>
                </div>

                <!-- 수업 진행 방식 (준비 중) -->
                <div class="bg-white rounded-3xl border border-gray-100 p-8 shadow-sm card-hover">
                    <h3 class="text-xl font-bold text-gray-900 mb-3">포트폴리오</h3>
                    <p class="text-gray-500 leading-relaxed mb-6">
                        강의 자료 완비, 산출물 관리, 직접 배포·운영까지 —
                        강사로서 수업을 설계하고 운영하며 쌓아온 결과물과 노하우를 모았습니다.
                    </p>
                    <button @click="goProjects" class="text-brand font-semibold hover:underline">
                        포트폴리오 보러가기 <i class="fa-solid fa-chevron-right ml-1 text-xs"></i>
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
