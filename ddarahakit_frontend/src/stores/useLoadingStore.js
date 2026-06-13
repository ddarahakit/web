/**
 * 로딩 정보 저장소 객체    
 *
 * 로딩 정보를 저장소에 저장한다.
 */
import { defineStore } from 'pinia';
import { ref } from 'vue';

export const useLoadingStore = defineStore('loading', () => {
  const isLoading = ref(false);

  const startLoading = () => {
    isLoading.value = true;
  };

  const stopLoading = () => {
    setTimeout(() => {
      isLoading.value = false;
    }, 500); // UX를 위해 약간의 딜레이 추가
  };

  return { isLoading, startLoading, stopLoading };
});

export default useLoadingStore;
