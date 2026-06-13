<script setup>
import { onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import api from '@/api/user'

// 라우트 정보 객체
const route = useRoute()

//라우터 정보 객체
const router = useRouter()

//처리 상태: processing(처리 중) | error(실패 — 리다이렉트 직전 잠깐)
const status = ref('processing')

const verifyInfo = {
    email: route.query.email || '',
    uuid: route.query.uuid || ''
}

const emailVerify = async () => {
    //API: 이메일 인증
    const data = await api.emailVerify(verifyInfo)

    if (data.success) {
        //인증 성공 → 로그인 페이지로 이동
        router.push({ name: 'login' })
        return
    }

    //실패 → 로그인 페이지로 안내
    status.value = 'error'
    router.push({ name: 'login', query: { error: 'verify' } })
}

onMounted(() => {
    emailVerify()
})
</script>

<template>
  <main class="verify-callback">
    <div class="verify-callback__box">
      <div class="verify-callback__spinner" aria-hidden="true"></div>
      <p class="verify-callback__title">
        {{ status === 'error' ? '이메일 인증에 실패했습니다' : '이메일 인증 중입니다' }}
      </p>
      <p class="verify-callback__desc">
        {{ status === 'error' ? '잠시 후 로그인 화면으로 이동합니다.' : '이메일 인증을 처리 중입니다. 잠시만 기다려 주세요.' }}
      </p>
    </div>
  </main>
</template>

<style scoped>
.verify-callback {
  /* 헤더/푸터 사이 본문이 비어 보이지 않도록 충분한 높이로 중앙 정렬 */
  min-height: 60vh;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 4rem 1.5rem;
}

.verify-callback__box {
  display: flex;
  flex-direction: column;
  align-items: center;
  text-align: center;
  gap: 1rem;
}

.verify-callback__spinner {
  width: 48px;
  height: 48px;
  border: 4px solid rgba(20, 188, 237, 0.2);
  border-top-color: var(--color-brand, #14bced);
  border-radius: 50%;
  animation: verify-callback-spin 0.8s linear infinite;
}

.verify-callback__title {
  margin: 0;
  font-size: 1.125rem;
  font-weight: 700;
  color: #384158;
}

.verify-callback__desc {
  margin: 0;
  font-size: 0.9rem;
  color: #8a93a6;
}

@keyframes verify-callback-spin {
  to {
    transform: rotate(360deg);
  }
}

/* 모션 최소화 선호 사용자: 회전 애니메이션 완화(느린 페이드 펄스) */
@media (prefers-reduced-motion: reduce) {
  .verify-callback__spinner {
    animation: verify-callback-pulse 1.4s ease-in-out infinite;
    border-top-color: rgba(20, 188, 237, 0.2);
  }

  @keyframes verify-callback-pulse {
    0%,
    100% {
      opacity: 1;
    }
    50% {
      opacity: 0.4;
    }
  }
}
</style>
