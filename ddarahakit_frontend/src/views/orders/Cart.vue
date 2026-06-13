<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import cartApi from '@/api/cart'
import ordersApi from '@/api/orders'
import { formatPrice } from '@/utils/price'

const router = useRouter()

// 로딩 상태
const isLoading = ref(true)

// 장바구니 항목 목록
const cartItems = ref([])

// 총 원가
const totalOriginalPrice = ref(0)

// 총 판매가
const totalSalePrice = ref(0)

// 선택된 항목 인덱스 목록
const selectedItems = ref([])

// 결제 진행 중 여부
const isPaymentProcessing = ref(false)

// 결제 상태
const paymentStatus = ref({
    status: "",
    message: ""
});

/**
 * 선택된 항목들의 원가 합계
 */
const selectedOriginalPrice = computed(() => {
    return cartItems.value
        .filter(item => selectedItems.value.includes(item.cartItemIdx))
        .reduce((sum, item) => sum + item.originalPrice, 0)
})

/**
 * 선택된 항목들의 판매가 합계
 */
const selectedSalePrice = computed(() => {
    return cartItems.value
        .filter(item => selectedItems.value.includes(item.cartItemIdx))
        .reduce((sum, item) => sum + item.salePrice, 0)
})

/**
 * 선택된 항목들의 할인 금액 합계
 */
const selectedDiscountPrice = computed(() => {
    return selectedOriginalPrice.value - selectedSalePrice.value
})

/**
 * 전체 선택 여부
 */
const isAllSelected = computed(() => {
    return cartItems.value.length > 0 && selectedItems.value.length === cartItems.value.length
})

/**
 * 전체 선택/해제 토글
 */
const toggleSelectAll = () => {
    if (isAllSelected.value) {
        selectedItems.value = []
    } else {
        selectedItems.value = cartItems.value.map(item => item.cartItemIdx)
    }
}

/**
 * 개별 항목 선택/해제 토글
 */
const toggleSelectItem = (cartItemIdx) => {
    const index = selectedItems.value.indexOf(cartItemIdx)
    if (index > -1) {
        selectedItems.value.splice(index, 1)
    } else {
        selectedItems.value.push(cartItemIdx)
    }
}

/**
 * 할인율 계산
 *
 * 원가 대비 할인 비율(%)을 반환한다. 할인이 없거나 무료면 0.
 */
const getDiscountRate = (item) => {
    if (!item.originalPrice || item.originalPrice <= item.salePrice) return 0
    return Math.round((item.originalPrice - item.salePrice) / item.originalPrice * 100)
}

/**
 * 강의 상세 페이지 이동
 *
 * 장바구니 카드 본문 클릭 시 해당 강의 상세로 이동한다.
 */
const goCourseDetail = (courseIdx) => {
    router.push({ name: 'courseDetail', params: { courseIdx } })
}

/**
 * 장바구니 목록 조회
 */
const getCartList = async () => {
    const data = await cartApi.cartList()
    if (data.success && data.results) {
        cartItems.value = data.results.cartItems || []
        totalOriginalPrice.value = data.results.totalOriginalPrice || 0
        totalSalePrice.value = data.results.totalSalePrice || 0
        // 초기 로드 시 전체 선택
        selectedItems.value = cartItems.value.map(item => item.cartItemIdx)
    }
}

/**
 * 장바구니 항목 개별 삭제
 */
const removeCartItem = async (cartItemIdx) => {
    const data = await cartApi.cartRemove(cartItemIdx)
    if (data.success) {
        // 선택 목록에서도 제거
        const index = selectedItems.value.indexOf(cartItemIdx)
        if (index > -1) {
            selectedItems.value.splice(index, 1)
        }
        await getCartList()
    }
}

/**
 * 선택된 항목 삭제
 */
const removeSelectedItems = async () => {
    for (const cartItemIdx of [...selectedItems.value]) {
        await cartApi.cartRemove(cartItemIdx)
    }
    selectedItems.value = []
    await getCartList()
}

/**
 * 결제/주문 완료 성공 공통 처리
 *
 * 무료/유료 완료 시 동일하게 성공 상태 표시 → 선택 항목 장바구니 정리 → 대시보드 이동.
 */
const onPaymentSuccess = async (selected, message) => {
    paymentStatus.value = { status: "SUCCESS", message }

    // 장바구니 정리 (실패해도 이동은 진행)
    try {
        await Promise.all(selected.map(item => cartApi.cartRemove(item.cartItemIdx)))
    } catch (e) {
        console.warn("장바구니 정리 중 오류:", e)
    }

    router.push({ name: 'dashboard' })
}

/**
 * 선택된 항목 결제
 */
const onPayment = async () => {
    const selected = cartItems.value.filter(item => selectedItems.value.includes(item.cartItemIdx))
    if (selected.length === 0) return
    if (isPaymentProcessing.value) return

    isPaymentProcessing.value = true
    paymentStatus.value = { status: "", message: "" }

    let ordersIdx = null

    try {
        const firstItem = selected[0]
        // 결제 금액은 정수 Number 로 합산한다(salePrice 가 문자열/undefined 여도 NaN 방어).
        // 0 은 전부 무료 → 무료 완료 경로로, 음수/NaN 만 비정상으로 차단한다.
        const totalAmount = selected.reduce((sum, item) => sum + (Number(item.salePrice) || 0), 0)

        // 비정상 금액(음수/NaN)만 차단. 0 은 아래에서 무료 완료 경로로 처리한다.
        if (!Number.isFinite(totalAmount) || totalAmount < 0) {
            paymentStatus.value = { status: "FAILED", message: '결제 금액을 확인할 수 없습니다. 장바구니를 새로고침 후 다시 시도해주세요.' }
            isPaymentProcessing.value = false
            return
        }

        const courseIdxList = selected.map(item => item.courseIdx)
        const orderName = selected.length === 1
            ? firstItem.courseName
            : `${firstItem.courseName} 외 ${selected.length - 1}건`

        // 1. 주문 생성
        const createResponse = await ordersApi.ordersCreate({
            paymentPrice: totalAmount,
            courseIdx: firstItem.courseIdx,
            courseIdxList
        })

        if (!createResponse?.success || !createResponse?.results?.ordersIdx) {
            paymentStatus.value = { status: "FAILED", message: '주문 생성에 실패하였습니다. 잠시 후 다시 시도해주세요.' }
            isPaymentProcessing.value = false
            return
        }

        ordersIdx = createResponse.results.ordersIdx

        if (totalAmount === 0) {
            // ===> 전부 무료(₩0): 포트원/verify 건너뛰고 서버에서 즉시 완료 처리
            paymentStatus.value = { status: "IDLE", message: '주문을 완료하는 중...' }

            const freeResponse = await ordersApi.ordersFreeComplete(ordersIdx)

            if (freeResponse?.success && freeResponse?.results?.orders?.paid === true) {
                await onPaymentSuccess(selected, "무료 강의 등록이 완료되었습니다.")
            } else {
                paymentStatus.value = { status: "FAILED", message: freeResponse?.errorMessage || freeResponse?.message || '무료 주문 완료에 실패하였습니다. 잠시 후 다시 시도해주세요.' }
                await ordersApi.ordersCancel(ordersIdx)
            }

            isPaymentProcessing.value = false
            return
        }

        // ===> 유료(totalAmount > 0): 기존 포트원 결제 흐름
        paymentStatus.value = { status: "IDLE", message: '결제 진행 중...' }

        // 2. PortOne 결제 요청
        const payment = await ordersApi.ordersPayment({
            orderName,
            totalAmount,
            customData: { ordersIdx, courseIdx: firstItem.courseIdx, courseIdxList },
        })

        // 사용자가 결제창을 닫거나 PG 에러 발생
        if (!payment || payment.code) {
            const msg = payment?.message || payment?.pgMessage || '결제가 취소되었습니다.'
            paymentStatus.value = { status: "FAILED", message: msg }
            await ordersApi.ordersCancel(ordersIdx)
            isPaymentProcessing.value = false
            return
        }

        // 3. 결제 검증
        const verifyResponse = await ordersApi.ordersVerify({ paymentId: payment.paymentId })

        if (verifyResponse?.success && verifyResponse?.results) {
            await onPaymentSuccess(selected, "결제가 정상적으로 완료되었습니다.")
        } else {
            paymentStatus.value = { status: "FAILED", message: '결제 검증에 실패하였습니다. 취소 처리 중입니다.' }
            await ordersApi.ordersCancel(ordersIdx)
        }
    } catch (error) {
        console.error("결제 중 오류 발생:", error)
        paymentStatus.value = { status: "FAILED", message: '결제 처리 중 오류가 발생했습니다. 다시 시도해주세요.' }
        // 주문이 생성된 상태에서 에러 발생 시 주문 취소
        if (ordersIdx) {
            try {
                await ordersApi.ordersCancel(ordersIdx)
            } catch (e) {
                console.error("주문 취소 중 오류:", e)
            }
        }
    } finally {
        isPaymentProcessing.value = false
    }
}

/**
 * 컴포넌트 마운트
 */
onMounted(async () => {
    isLoading.value = true
    await getCartList()
    isLoading.value = false
})
</script>

<template>
    <main class="pt-24 max-w-7xl mx-auto px-6 py-12 flex-col">
        <h2 class="text-2xl font-bold text-gray-900 mb-8 px-1">수강 바구니</h2>

        <!-- 스켈레톤 UI -->
        <div v-if="isLoading" class="grid grid-cols-1 lg:grid-cols-3 gap-8">
            <div class="lg:col-span-2 space-y-4">
                <!-- 전체 선택 스켈레톤 -->
                <div class="bg-white p-4 rounded-xl border border-gray-100 shadow-sm">
                    <div class="flex items-center gap-3">
                        <div class="w-5 h-5 bg-gray-100 rounded skeleton"></div>
                        <div class="w-32 h-4 bg-gray-100 rounded skeleton"></div>
                    </div>
                </div>
                <!-- 강의 아이템 스켈레톤 -->
                <div v-for="n in 3" :key="n"
                    class="bg-white p-4 md:p-6 rounded-2xl border border-gray-100 shadow-sm flex flex-col md:flex-row gap-6">
                    <div class="w-full md:w-48 h-28 bg-gray-100 rounded-xl skeleton flex-shrink-0"></div>
                    <div class="flex-grow flex flex-col justify-between">
                        <div>
                            <div class="w-3/4 h-6 bg-gray-100 rounded skeleton mb-2"></div>
                            <div class="w-1/2 h-6 bg-gray-100 rounded skeleton"></div>
                        </div>
                        <div class="mt-4 flex items-center justify-between">
                            <div class="w-24 h-5 bg-gray-100 rounded skeleton"></div>
                            <div class="w-28 h-6 bg-gray-100 rounded skeleton"></div>
                        </div>
                    </div>
                </div>
            </div>
            <!-- 결제 정보 스켈레톤 -->
            <div class="space-y-6">
                <div class="bg-white p-6 rounded-2xl border border-gray-100 shadow-sm">
                    <div class="w-24 h-6 bg-gray-100 rounded skeleton mb-6"></div>
                    <div class="space-y-4 mb-6">
                        <div class="flex justify-between">
                            <div class="w-24 h-4 bg-gray-100 rounded skeleton"></div>
                            <div class="w-20 h-4 bg-gray-100 rounded skeleton"></div>
                        </div>
                        <div class="flex justify-between">
                            <div class="w-24 h-4 bg-gray-100 rounded skeleton"></div>
                            <div class="w-20 h-4 bg-gray-100 rounded skeleton"></div>
                        </div>
                        <div class="pt-4 border-t border-gray-100 flex justify-between">
                            <div class="w-28 h-5 bg-gray-100 rounded skeleton"></div>
                            <div class="w-24 h-7 bg-gray-100 rounded skeleton"></div>
                        </div>
                    </div>
                    <div class="w-full h-14 bg-gray-100 rounded-xl skeleton"></div>
                </div>
            </div>
        </div>

        <!-- 장바구니가 비어있을 때 -->
        <div v-else-if="cartItems.length === 0" class="text-center py-20">
            <i class="fa-solid fa-cart-shopping text-6xl text-gray-200 mb-6"></i>
            <p class="text-gray-400 text-lg">장바구니가 비어있습니다.</p>
            <RouterLink :to="{ name: 'courseList' }"
                class="inline-block mt-6 px-6 py-3 bg-brand text-white rounded-xl font-bold hover:opacity-90 transition-all">
                강의 둘러보기
            </RouterLink>
        </div>

        <div v-else class="grid grid-cols-1 lg:grid-cols-3 gap-8">
            <!-- 장바구니 리스트 영역 -->
            <div class="lg:col-span-2 space-y-4">
                <!-- 전체 선택 및 삭제 제어 -->
                <div class="flex items-center justify-between bg-white p-4 rounded-xl border border-gray-100 shadow-sm">
                    <label class="flex items-center gap-3 cursor-pointer group">
                        <input type="checkbox" :checked="isAllSelected" @change="toggleSelectAll" class="custom-checkbox">
                        <span class="text-sm font-semibold text-gray-700 group-hover:text-brand transition-colors">
                            전체 선택 ({{ selectedItems.length }}/{{ cartItems.length }})
                        </span>
                    </label>
                    <button @click="removeSelectedItems" class="text-sm text-gray-400 hover:text-red-500 transition-colors">
                        선택 삭제
                    </button>
                </div>

                <!-- 강의 아이템 (본문 클릭 시 강의 상세로 이동) -->
                <div v-for="item in cartItems" :key="item.cartItemIdx"
                    @click="goCourseDetail(item.courseIdx)"
                    class="bg-white p-4 md:p-6 rounded-2xl border border-gray-100 shadow-sm flex flex-col md:flex-row gap-6 relative group cursor-pointer hover:border-brand/40 hover:shadow-md transition-all">
                    <div class="absolute top-4 left-4 z-10">
                        <input type="checkbox" :checked="selectedItems.includes(item.cartItemIdx)"
                            @click.stop @change="toggleSelectItem(item.cartItemIdx)" class="custom-checkbox">
                    </div>
                    <div class="w-full md:w-48 h-28 bg-gray-100 rounded-xl overflow-hidden flex-shrink-0 relative">
                        <img :src="`${item.courseImage}`" :alt="item.courseName"
                            class="w-full h-full object-cover">
                        <div class="absolute inset-0 bg-black/5 group-hover:bg-transparent transition-colors"></div>
                    </div>
                    <div class="flex-grow flex flex-col justify-between">
                        <div>
                            <!-- 카테고리 + 난이도 메타 라인 -->
                            <div v-if="item.categoryName || item.levelDescription"
                                class="flex items-center gap-2 mb-1.5">
                                <span v-if="item.categoryName"
                                    class="text-[10px] font-bold text-brand uppercase tracking-wider">
                                    {{ item.categoryName }}
                                </span>
                                <span v-if="item.levelDescription"
                                    class="text-[10px] font-bold text-gray-500 bg-gray-100 px-2 py-0.5 rounded">
                                    {{ item.levelDescription }}
                                </span>
                            </div>
                            <div class="flex justify-between items-start">
                                <h3 class="font-bold text-lg text-gray-900 line-clamp-2 pr-8 group-hover:text-brand transition-colors">
                                    {{ item.courseName }}
                                </h3>
                                <button @click.stop="removeCartItem(item.cartItemIdx)"
                                    class="text-gray-300 hover:text-red-400 transition-colors">
                                    <i class="fa-solid fa-xmark text-xl"></i>
                                </button>
                            </div>
                            <!-- 요약 -->
                            <p v-if="item.summary" class="mt-1 text-sm text-gray-500 line-clamp-1 pr-8">
                                {{ item.summary }}
                            </p>
                            <!-- 평점 + 수강생 수 -->
                            <div class="mt-2 flex flex-wrap items-center gap-x-3 gap-y-1 text-xs">
                                <span v-if="item.averageRating > 0 && item.totalReviewsCount > 0"
                                    class="flex items-center gap-1 text-yellow-400">
                                    <i class="fa-solid fa-star"></i>
                                    <span class="text-gray-700 font-semibold">{{ item.averageRating.toFixed(1) }}</span>
                                    <span class="text-gray-400">({{ item.totalReviewsCount }})</span>
                                </span>
                                <span v-if="item.totalOrderedCount > 0" class="text-gray-400">
                                    수강생 {{ item.totalOrderedCount.toLocaleString() }}명
                                </span>
                            </div>
                            <!-- 가격/할인 정보 -->
                            <div class="mt-2 flex flex-wrap items-center gap-x-2 gap-y-1">
                                <span v-if="getDiscountRate(item) > 0"
                                    class="text-[12px] font-bold text-red-500">
                                    {{ getDiscountRate(item) }}% 할인
                                </span>
                                <span v-if="item.originalPrice !== item.salePrice"
                                    class="text-sm text-gray-400 line-through">
                                    ₩{{ item.originalPrice.toLocaleString() }}
                                </span>
                                <span class="text-base font-bold text-gray-900">
                                    {{ formatPrice(item.salePrice) }}
                                </span>
                            </div>
                        </div>
                        <div class="mt-4 flex items-center justify-end">
                            <span class="text-xs text-gray-400 flex items-center gap-1 group-hover:text-brand transition-colors">
                                강의 보기 <i class="fa-solid fa-chevron-right text-[10px]"></i>
                            </span>
                        </div>
                    </div>
                </div>
            </div>

            <!-- 결제 요약 영역 -->
            <div class="space-y-6">
                <div class="bg-white p-6 rounded-2xl border border-gray-100 shadow-sm top-24">
                    <h3 class="font-bold text-lg mb-6 flex items-center gap-2">
                        <i class="fa-solid fa-receipt text-gray-400"></i>
                        결제 정보
                    </h3>

                    <div class="space-y-4 mb-6">
                        <div class="flex justify-between text-gray-600 text-sm">
                            <span>선택 상품 금액</span>
                            <span class="font-medium">₩{{ selectedOriginalPrice.toLocaleString() }}</span>
                        </div>
                        <div class="flex justify-between text-red-500 text-sm">
                            <span>상품 할인 금액</span>
                            <span class="font-medium">-₩{{ selectedDiscountPrice.toLocaleString() }}</span>
                        </div>
                        <div class="pt-4 border-t border-gray-100 flex justify-between items-end">
                            <span class="font-bold text-gray-900">최종 결제 금액</span>
                            <span class="text-2xl font-black text-brand">₩{{ selectedSalePrice.toLocaleString() }}</span>
                        </div>
                    </div>

                    <button @click="onPayment" :disabled="selectedItems.length === 0 || isPaymentProcessing"
                        class="w-full bg-brand text-white py-4 rounded-xl font-bold text-lg shadow-lg shadow-blue-100 hover:opacity-95 active:scale-[0.98] transition-all flex items-center justify-center gap-2 disabled:opacity-50 disabled:cursor-not-allowed">
                        <template v-if="isPaymentProcessing">
                            <i class="fa-solid fa-spinner fa-spin mr-1"></i> 결제 처리 중...
                        </template>
                        <template v-else>
                            <span>₩{{ selectedSalePrice.toLocaleString() }}</span>
                            <span>결제하기</span>
                        </template>
                    </button>

                    <!-- 결제 상태 메시지 -->
                    <p v-if="paymentStatus.message" class="text-sm mt-4 text-center"
                        :class="paymentStatus.status === 'SUCCESS' ? 'text-green-500' : paymentStatus.status === 'FAILED' ? 'text-red-500' : 'text-gray-500'">
                        {{ paymentStatus.message }}
                    </p>

                    <p class="text-[11px] text-gray-400 mt-5 text-center leading-relaxed">
                        따라학IT는 안전한 결제 환경을 제공합니다.<br>
                        취소 및 환불은 관련 규정에 따릅니다.
                    </p>
                </div>

                <div class="bg-blue-50/50 p-4 rounded-xl border border-blue-100 flex gap-3">
                    <i class="fa-solid fa-circle-info text-brand mt-0.5"></i>
                    <p class="text-xs text-gray-600 leading-normal">
                        <strong class="text-gray-800">알려드립니다!</strong><br>
                        장바구니에는 최대 100개의 강의를 담을 수 있으며, 30일 동안 보관됩니다.
                    </p>
                </div>
            </div>
        </div>
    </main>
</template>

<style scoped>
.custom-checkbox {
    appearance: none;
    -webkit-appearance: none;
    width: 1.25rem;
    height: 1.25rem;
    border: 2px solid #E2E8F0;
    border-radius: 0.375rem;
    background-color: white;
    display: inline-grid;
    place-content: center;
    cursor: pointer;
    transition: all 0.2s ease;
}

.custom-checkbox:checked {
    background-color: #14BCED;
    border-color: #14BCED;
}

.custom-checkbox::before {
    content: "\f00c";
    font-family: "Font Awesome 6 Free";
    font-weight: 900;
    font-size: 0.75rem;
    color: white;
    transform: scale(0);
    transition: transform 0.1s ease-in-out;
}

.custom-checkbox:checked::before {
    transform: scale(1);
}

.custom-checkbox:hover {
    border-color: #14BCED;
}

.line-clamp-2 {
    display: -webkit-box;
    -webkit-line-clamp: 2;
    -webkit-box-orient: vertical;
    overflow: hidden;
}

/* 스켈레톤 애니메이션 */
.skeleton {
    background: linear-gradient(90deg, #f0f0f0 25%, #e0e0e0 50%, #f0f0f0 75%);
    background-size: 200% 100%;
    animation: skeleton-loading 1.5s infinite;
}

@keyframes skeleton-loading {
    0% {
        background-position: 200% 0;
    }
    100% {
        background-position: -200% 0;
    }
}
</style>
