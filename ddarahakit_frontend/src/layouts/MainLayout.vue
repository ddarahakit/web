<script setup>
import { RouterView } from "vue-router";
import { ref, watchEffect } from 'vue'
import Header from "@/components/base/Header.vue";
import Footer from "@/components/base/Footer.vue";

// 스크롤 여부
const isScrolled = ref(false)

// 스크롤 이벤트 리스너
const handleScroll = () => {
  isScrolled.value = window.scrollY > 100;
}

// 컴포넌트가 마운트되었을 때 스크롤 이벤트 리스너 등록
watchEffect((onInvalidate) => {
  window.addEventListener('scroll', handleScroll)

  // 컴포넌트가 언마운트될 때 스크롤 이벤트 리스너 제거
  onInvalidate(() => {
    window.removeEventListener('scroll', handleScroll)
  })
})
</script>

<template>
  <Header :isScrolled="isScrolled" />
  <router-view v-slot="{ Component, route }">
    <transition name="page" mode="out-in">
      <!-- 단일 래퍼 엘리먼트로 감싸 트랜지션 대상이 항상 단일 element 가 되게 한다.
           (뷰 컴포넌트가 주석/다중 루트(fragment) 인 경우 out-in 트랜지션이 깨져 본문이
            안 보이는 문제 방지) -->
      <div :key="route.meta.transitionKey ?? route.path" class="page-transition-root">
        <component :is="Component" />
      </div>
    </transition>
  </router-view>
  <Footer />
</template>

<style scoped></style>
