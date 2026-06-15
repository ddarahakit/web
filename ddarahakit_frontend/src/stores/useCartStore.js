/**
 * 장바구니 상태 저장소
 *
 * 헤더 배지와 장바구니 화면이 같은 카운트를 공유하도록 한다.
 * 장바구니를 변경(추가/삭제/결제)하는 쪽에서 setCount/fetchCount 를 호출하면
 * 헤더의 숫자가 즉시 반영된다.
 */
import { ref } from 'vue'
import { defineStore } from 'pinia'
import cartApi from '@/api/cart'

const useCartStore = defineStore('cart', () => {
    // 장바구니에 담긴 코스 수
    const count = ref(0)

    /**
     * 카운트 직접 설정
     *
     * 장바구니 목록을 이미 알고 있을 때(목록 길이 등) 추가 API 호출 없이 갱신한다.
     */
    const setCount = (n) => {
        count.value = Number(n) || 0
    }

    /**
     * 서버에서 장바구니 수 조회
     */
    const fetchCount = async () => {
        const data = await cartApi.cartCount()
        if (data.success && data.results) {
            count.value = data.results.count
        } else {
            count.value = 0
        }
    }

    /**
     * 초기화 (로그아웃 등)
     */
    const reset = () => {
        count.value = 0
    }

    return { count, setCount, fetchCount, reset }
})

export default useCartStore
