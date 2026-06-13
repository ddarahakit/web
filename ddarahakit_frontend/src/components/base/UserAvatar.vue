<script setup>
import { ref, computed, watch } from 'vue'
import { userImageUrl } from '@/utils/image'

// 프로필/사용자 아바타 공용 컴포넌트.
// - 데이터 fetch 전·이미지 로드 중에는 스켈레톤 UI 표시
// - 로드 완료 시 이미지 노출 / 이미지 없으면 중립 기본 아이콘(fa-user)
// 부모 컨테이너의 크기·라운딩·overflow를 그대로 채운다(w-full h-full).
const props = defineProps({
  // 원본 저장값(상대경로 또는 절대 URL). userImageUrl로 해석된다.
  src: { type: String, default: '' },
  alt: { type: String, default: 'Profile' },
  iconClass: { type: String, default: 'text-2xl' },
  // 부모가 overflow-hidden + 라운딩을 갖지 않을 때 내부 요소에 적용할 라운딩 클래스
  rounded: { type: String, default: 'rounded-none' }
})

const loading = ref(true)
const resolved = computed(() => userImageUrl(props.src))

// 표시할 이미지가 없으면 스켈레톤 종료(기본 아이콘 노출). src가 새로 생기면 다시 로딩.
watch(resolved, (v) => { loading.value = !!v }, { immediate: true })
</script>

<template>
  <div class="w-full h-full relative">
    <!-- 로딩 중: 스켈레톤 UI -->
    <div v-if="loading" class="absolute inset-0 animate-pulse bg-slate-200" :class="rounded"></div>

    <!-- 프로필 이미지 -->
    <img
      v-if="resolved"
      v-show="!loading"
      :src="resolved"
      :alt="alt"
      @load="loading = false"
      @error="loading = false"
      class="w-full h-full object-cover"
      :class="rounded">

    <!-- 이미지 없음: 중립 기본 아이콘 -->
    <div
      v-else
      class="w-full h-full flex items-center justify-center bg-slate-100 text-slate-300"
      :class="rounded">
      <i class="fa-solid fa-user" :class="iconClass"></i>
    </div>
  </div>
</template>
