<script setup>
import { useRoute } from 'vue-router'

const route = useRoute()
</script>

<template>
    <div class="bg-[#F8FAFC] min-h-screen flex flex-col items-center justify-center p-6">
        <!-- 에러 콘텐츠 컨테이너 -->
        <div class="max-w-2xl w-full text-center space-y-8">

            <!-- 일러스트 영역 -->
            <div class="relative flex justify-center items-center mb-12 error-animation">
                <svg width="320" height="240" viewBox="0 0 400 300" fill="none" xmlns="http://www.w3.org/2000/svg"
                    class="max-w-full h-auto">
                    <circle cx="200" cy="150" r="100" fill="#E0F7FE" />
                    <path
                        d="M150 140C150 134.477 154.477 130 160 130H240C245.523 130 250 134.477 250 140V220C250 225.523 245.523 230 240 230H160C154.477 230 150 225.523 150 220V140Z"
                        fill="white" stroke="#14BCED" stroke-width="4" />
                    <path
                        d="M185 170C185 167.239 187.239 165 190 165H195C197.761 165 200 167.239 200 170V175C200 177.761 197.761 180 195 180H190C187.239 180 185 177.761 185 175V170Z"
                        fill="#14BCED" />
                    <path
                        d="M205 170C205 167.239 207.239 165 210 165H215C217.761 165 220 167.239 220 170V175C220 177.761 217.761 180 215 180H210C207.239 180 205 177.761 205 175V170Z"
                        fill="#14BCED" />
                    <path d="M180 200C180 200 190 190 200 190C210 190 220 200 220 200" stroke="#14BCED" stroke-width="4"
                        stroke-linecap="round" />
                    <circle cx="280" cy="80" r="20" fill="#14BCED" opacity="0.2" />
                    <circle cx="100" cy="220" r="15" fill="#14BCED" opacity="0.1" />
                </svg>
                <div class="absolute top-1/2 left-1/2 transform -translate-x-1/2 -translate-y-1/2 mt-12">
                    <span class="text-8xl font-black text-brand opacity-10 select-none">ERROR</span>
                </div>
            </div>

            <!-- 텍스트 설명 -->
            <div class="space-y-4">
                <h1 class="text-4xl md:text-5xl font-bold text-gray-900 leading-tight">
                    길을 잃으신 것 같아요!
                </h1>
                <p class="text-lg text-gray-500 max-w-md mx-auto leading-relaxed">
                    찾으시는 페이지가 삭제되었거나 주소가 변경되었을 수 있습니다.<br>
                    아래 버튼을 통해 정상적인 경로로 돌아가실 수 있어요.
                </p>
            </div>

            <!-- 액션 버튼 -->
            <div class="flex flex-col sm:flex-row items-center justify-center gap-4 mt-12">
                <button onclick="history.back()"
                    class="w-full sm:w-auto px-8 py-4 bg-white text-gray-700 font-bold rounded-2xl border border-gray-200 hover:bg-gray-50 transition-all flex items-center justify-center gap-2">
                    <i class="fa-solid fa-arrow-left"></i>
                    이전 페이지로
                </button>
                <a href="/"
                    class="w-full sm:w-auto px-8 py-4 bg-brand text-white font-bold rounded-2xl shadow-lg shadow-blue-200 hover:opacity-90 transition-all flex items-center justify-center gap-2">
                    <i class="fa-solid fa-house"></i>
                    메인으로 이동
                </a>
            </div>

            <!-- 고객지원 링크 -->
            <div class="pt-12">
                <p class="text-sm text-gray-400">
                    문제가 지속된다면 <a href="#" class="text-brand font-semibold underline underline-offset-4">고객 센터</a>로 문의해
                    주세요.
                </p>
            </div>
        </div>

        <!-- 푸터 로고 -->
        <footer class="absolute bottom-8 text-center w-full">
            <p class="text-sm font-bold text-gray-300">
                따라하면서 배우는 <span class="text-brand">IT</span>
            </p>
        </footer>
    </div>
</template>
<style scoped></style>
