<script setup>
import { useRoute, useRouter } from 'vue-router'

// 라우트 정보 객체
const route = useRoute()

//라우터 정보 객체
const router = useRouter()

//가입에 사용한 이메일 (Signup 에서 query 로 전달)
const email = route.query.email || ''

const goLogin = () => router.push({ name: 'login' })
</script>

<template>
  <main class="pt-24 max-w-7xl mx-auto px-6 py-12 flex items-center justify-center">
    <div class="max-w-lg w-full bg-white rounded-3xl shadow-xl shadow-blue-100/50 border border-gray-100 p-8 md:p-10 text-center">
      <div class="w-16 h-16 rounded-2xl bg-emerald-50 text-emerald-500 flex items-center justify-center mx-auto mb-6">
        <i class="fa-solid fa-envelope-circle-check text-3xl"></i>
      </div>

      <h1 class="text-2xl font-bold text-gray-900 mb-3">회원가입이 완료되었습니다 🎉</h1>

      <p class="text-gray-500 leading-relaxed mb-2">
        <span v-if="email" class="font-bold text-gray-700">{{ email }}</span>
        <span v-else>입력하신 이메일</span> 주소로 인증 메일을 보냈습니다.
      </p>
      <p class="text-gray-500 leading-relaxed mb-8">
        메일에 포함된 인증 링크를 클릭하면 가입이 최종 완료됩니다.<br>
        메일이 보이지 않으면 스팸함도 확인해 주세요.
      </p>

      <button type="button" @click="goLogin"
        class="w-full py-3.5 bg-brand text-white rounded-xl font-bold text-base shadow-lg shadow-blue-200 hover:opacity-90 transition-all">
        로그인 화면으로
      </button>
    </div>
  </main>
</template>
