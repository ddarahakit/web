<script setup>
import { computed, onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import useAuthStore from '@/stores/useAuthStore'
import api from '@/api/user'

// 라우트 정보 객체
const route = useRoute()

//라우터 정보 객체
const router = useRouter()

//저장소 정보 객체
const authStore = useAuthStore()

//처리 상태: processing(처리 중) | error(실패 — 리다이렉트 직전 잠깐)
const status = ref('processing')

//소셜 제공자 라벨 (google/kakao)
const providerLabel = computed(() => {
  const provider = String(route.params.provider || '').toLowerCase()
  if (provider === 'kakao') return '카카오'
  if (provider === 'google') return 'Google'
  return '소셜'
})

const loginCallback = async () => {
  //API: 소셜 로그인 콜백 (쿠키 기반 세션으로 사용자 정보 수령)
  const data = await api.loginCallback()

  if (data.success) {
    //로그인
    authStore.login(data.results)

    //코스 목록 페이지 이동
    const redirectPath = route.query.redirect || '/course/list'

    router.push({ path: redirectPath })
    return
  }

  //실패 → 로그인 페이지로 안내
  status.value = 'error'
  router.push({ name: 'login', query: { error: 'oauth2' } })
}

onMounted(() => {
  loginCallback()
})
</script>

<template>
  <main class="social-callback">
    <div class="social-callback__box">
      <div class="social-callback__spinner" aria-hidden="true"></div>
      <p class="social-callback__title">
        {{ status === 'error' ? '로그인에 실패했습니다' : `${providerLabel} 계정으로 로그인 중입니다` }}
      </p>
      <p class="social-callback__desc">
        {{ status === 'error' ? '잠시 후 로그인 화면으로 이동합니다.' : '소셜 로그인 처리 중입니다. 잠시만 기다려 주세요.' }}
      </p>
    </div>
  </main>
</template>

<style scoped>
.social-callback {
  /* 헤더/푸터 사이 본문이 비어 보이지 않도록 충분한 높이로 중앙 정렬 */
  min-height: 60vh;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 4rem 1.5rem;
}

.social-callback__box {
  display: flex;
  flex-direction: column;
  align-items: center;
  text-align: center;
  gap: 1rem;
}

.social-callback__spinner {
  width: 48px;
  height: 48px;
  border: 4px solid rgba(20, 188, 237, 0.2);
  border-top-color: var(--color-brand, #14bced);
  border-radius: 50%;
  animation: social-callback-spin 0.8s linear infinite;
}

.social-callback__title {
  margin: 0;
  font-size: 1.125rem;
  font-weight: 700;
  color: #384158;
}

.social-callback__desc {
  margin: 0;
  font-size: 0.9rem;
  color: #8a93a6;
}

@keyframes social-callback-spin {
  to {
    transform: rotate(360deg);
  }
}

/* 모션 최소화 선호 사용자: 회전 애니메이션 완화(느린 페이드 펄스) */
@media (prefers-reduced-motion: reduce) {
  .social-callback__spinner {
    animation: social-callback-pulse 1.4s ease-in-out infinite;
    border-top-color: rgba(20, 188, 237, 0.2);
  }

  @keyframes social-callback-pulse {
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
