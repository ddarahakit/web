<script setup>
import { useLoadingStore } from '@/stores/useLoadingStore';

const loadingStore = useLoadingStore();
</script>

<template>
  <div>
    <div v-if="loadingStore.isLoading" class="loading-overlay">
      <p>로딩 중...</p>
    </div>
    <router-view />
  </div>
</template>

<style scoped>
.loading-overlay {
  z-index: 2000;
  position: fixed;
  top: 3rem;
  left: 0;
  width: 100%;
  height: 100%;
  background: rgba(240, 240, 240, 1);
  display: flex;
  justify-content: center;
  align-items: center;
  color: white;
  font-size: 1.5rem;
}
</style>
