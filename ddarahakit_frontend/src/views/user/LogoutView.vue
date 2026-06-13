<script setup>
import { onMounted } from 'vue'
import { useRouter } from 'vue-router'
import useAuthStore from '@/stores/useAuthStore'
import api from '@/api/user'

//라우터 정보 객체
const router = useRouter()

//저장소 정보 객체
const authStore = useAuthStore()

/**
 * 로그아웃 처리
 *
 * API 로그아웃 호출 → 스토리지 상태 정리 → 메인으로 이동.
 * 실패해도 상태를 정리하고 안전하게 리다이렉트한다.
 */
const processLogout = async () => {
  try {
    //API: 사용자 로그아웃
    await api.userLogout()
  } finally {
    //스토리지 로그아웃 (실패해도 안전하게 정리)
    authStore.logout()
    //메인 페이지로 이동
    router.replace({ name: 'main' })
  }
}

onMounted(() => {
  processLogout()
})
</script>

<template>
  <main class="logout">
    <div class="logout__box">
      <div class="logout__spinner" aria-hidden="true"></div>
      <p class="logout__title">로그아웃 중입니다…</p>
      <p class="logout__desc">잠시만 기다려 주세요. 곧 메인 화면으로 이동합니다.</p>
    </div>
  </main>
</template>

<style scoped>
.logout {
  /* 헤더/푸터 사이 본문이 비어 보이지 않도록 충분한 높이로 중앙 정렬 */
  min-height: 60vh;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 4rem 1.5rem;
}

.logout__box {
  display: flex;
  flex-direction: column;
  align-items: center;
  text-align: center;
  gap: 1rem;
}

.logout__spinner {
  width: 48px;
  height: 48px;
  border: 4px solid rgba(20, 188, 237, 0.2);
  border-top-color: var(--color-brand, #14bced);
  border-radius: 50%;
  animation: logout-spin 0.8s linear infinite;
}

.logout__title {
  margin: 0;
  font-size: 1.125rem;
  font-weight: 700;
  color: #384158;
}

.logout__desc {
  margin: 0;
  font-size: 0.9rem;
  color: #8a93a6;
}

@keyframes logout-spin {
  to {
    transform: rotate(360deg);
  }
}

/* 모션 최소화 선호 사용자: 회전 애니메이션 완화(느린 페이드 펄스) */
@media (prefers-reduced-motion: reduce) {
  .logout__spinner {
    animation: logout-pulse 1.4s ease-in-out infinite;
    border-top-color: rgba(20, 188, 237, 0.2);
  }

  @keyframes logout-pulse {
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
