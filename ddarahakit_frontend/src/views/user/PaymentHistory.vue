<script setup>
import { computed, onMounted, ref } from 'vue'
import UserDashboardSidebar from '@/components/user/UserDashboardSidebar.vue'
import userApi from '@/api/user'
import ordersApi from '@/api/orders'
import { formatPrice } from '@/utils/price'

const PAGE_SIZE = 10

const isLoading = ref(true)
const isError = ref(false)
const paymentList = ref([])
const skeletonRows = [1, 2, 3]

// 페이징 상태 (page 는 0-base)
const page = ref(0)
const totalPages = ref(0)
const totalElements = ref(0)
const hasNext = ref(false)

// 영수증 모달 상태
const receiptOpen = ref(false)
const receiptLoading = ref(false)
const receipt = ref(null)

const fmtDate = (raw) => {
  if (!raw) return '-'
  const d = new Date(raw)
  if (Number.isNaN(d.getTime())) return String(raw)
  const y = d.getFullYear()
  const m = String(d.getMonth() + 1).padStart(2, '0')
  const day = String(d.getDate()).padStart(2, '0')
  return `${y}.${m}.${day}`
}

const iconByName = (name = '') => {
  const n = (name || '').toLowerCase()
  if (n.includes('react') || n.includes('vue') || n.includes('javascript')) return 'fa-solid fa-code'
  if (n.includes('figma') || n.includes('design')) return 'fa-solid fa-palette'
  if (n.includes('ai') || n.includes('data')) return 'fa-solid fa-brain'
  return 'fa-solid fa-book'
}

// 주문 1건 → 표시용으로 정규화 (PaymentRes)
const normalizePayment = (order) => {
  const items = Array.isArray(order.items) ? order.items : []
  const firstName = items[0]?.courseName || '강의'
  const title = items.length > 1 ? `${firstName} 외 ${items.length - 1}건` : firstName
  return {
    ordersIdx: order.ordersIdx,
    paymentId: order.paymentId,
    title,
    itemCount: items.length,
    date: fmtDate(order.paidAt),
    amount: Number(order.paymentPrice || 0),
    refunded: !!order.refunded,
    icon: iconByName(items[0]?.courseName)
  }
}

// 현재 페이지 합계
const currentPageTotal = computed(() =>
  paymentList.value.reduce((sum, item) => sum + (item.refunded ? 0 : item.amount), 0)
)

// 1-base 표시 페이지 번호
const displayPage = computed(() => page.value + 1)

// 페이지네이션 버튼 목록 (최대 5개, 현재 페이지 중심)
const pageNumbers = computed(() => {
  const total = totalPages.value
  if (total <= 1) return total === 1 ? [1] : []
  const cur = displayPage.value
  let start = Math.max(1, cur - 2)
  let end = Math.min(total, start + 4)
  start = Math.max(1, end - 4)
  const arr = []
  for (let p = start; p <= end; p++) arr.push(p)
  return arr
})

const getPaymentHistory = async () => {
  isLoading.value = true
  isError.value = false
  const data = await userApi.getPayments({ page: page.value, size: PAGE_SIZE })

  if (data?.success && data.results) {
    const r = data.results
    paymentList.value = Array.isArray(r.payments) ? r.payments.map(normalizePayment) : []
    totalPages.value = r.totalPages || 0
    totalElements.value = r.totalElements || 0
    hasNext.value = !!r.hasNext
  } else {
    isError.value = true
    paymentList.value = []
    totalPages.value = 0
    totalElements.value = 0
    hasNext.value = false
  }
  isLoading.value = false
}

// 0-base 페이지 이동
const goToPage = (zeroBased) => {
  if (zeroBased < 0 || (totalPages.value && zeroBased > totalPages.value - 1)) return
  if (zeroBased === page.value) return
  page.value = zeroBased
  getPaymentHistory()
}

// 영수증 보기
const openReceipt = async (ordersIdx) => {
  receiptOpen.value = true
  receiptLoading.value = true
  receipt.value = null
  const data = await ordersApi.ordersReceipt(ordersIdx)
  if (data?.success && data.results) {
    receipt.value = data.results
  }
  receiptLoading.value = false
}

const closeReceipt = () => {
  receiptOpen.value = false
  receipt.value = null
}

onMounted(() => {
  document.title = '결제 내역 | 따라학잇'
  getPaymentHistory()
})
</script>

<template>
  <main class="max-w-7xl mx-auto px-6 pt-28 pb-20">
    <div class="flex flex-col lg:flex-row gap-12">
      <UserDashboardSidebar heading="Account" />

      <div class="flex-grow space-y-8">
        <header class="flex flex-col md:flex-row md:items-end justify-between gap-4">
          <div>
            <h1 class="text-3xl font-bold text-slate-900 tracking-tight">결제 내역</h1>
            <p class="text-slate-500 text-sm mt-1">구매하신 강의와 결제 정보를 확인하실 수 있습니다.</p>
          </div>
        </header>

        <div class="grid grid-cols-1 md:grid-cols-2 gap-4">
          <div class="bg-white p-6 rounded-3xl border border-slate-100 card-shadow">
            <p class="text-[11px] font-bold text-slate-400 uppercase tracking-wider mb-2">총 결제 건수</p>
            <h3 class="text-2xl font-bold text-slate-800">{{ totalElements }} 건</h3>
            <p class="text-[11px] text-brand font-bold mt-1">전체 결제 내역 기준</p>
          </div>
          <div class="bg-white p-6 rounded-3xl border border-slate-100 card-shadow">
            <p class="text-[11px] font-bold text-slate-400 uppercase tracking-wider mb-2">이 페이지 결제 금액</p>
            <div class="flex items-center gap-2 mt-1">
              <i class="fa-solid fa-receipt text-xl text-slate-400"></i>
              <span class="text-lg font-bold text-slate-700">{{ formatPrice(currentPageTotal) }}</span>
            </div>
          </div>
        </div>

        <div class="bg-white rounded-[2rem] border border-slate-100 shadow-sm overflow-hidden">
          <div class="p-6 border-b border-slate-50 flex items-center justify-between">
            <h3 class="font-bold text-slate-800">결제 상세</h3>
          </div>

          <div class="overflow-x-auto">
            <table class="w-full text-left border-collapse">
              <thead>
                <tr class="bg-slate-50/50">
                  <th class="px-6 py-4 text-[11px] font-bold text-slate-400 uppercase tracking-wider">주문 정보</th>
                  <th class="px-6 py-4 text-[11px] font-bold text-slate-400 uppercase tracking-wider">결제 금액</th>
                  <th class="px-6 py-4 text-[11px] font-bold text-slate-400 uppercase tracking-wider">상태</th>
                  <th class="px-6 py-4 text-[11px] font-bold text-slate-400 uppercase tracking-wider">관리</th>
                </tr>
              </thead>
              <tbody v-if="isLoading" class="divide-y divide-slate-50">
                <tr v-for="row in skeletonRows" :key="`skeleton-${row}`">
                  <td class="px-6 py-5">
                    <div class="flex items-center gap-4">
                      <div class="w-12 h-12 rounded-lg skeleton"></div>
                      <div class="space-y-2">
                        <div class="h-4 w-56 rounded skeleton"></div>
                        <div class="h-3 w-40 rounded skeleton"></div>
                      </div>
                    </div>
                  </td>
                  <td class="px-6 py-5">
                    <div class="space-y-2">
                      <div class="h-4 w-20 rounded skeleton"></div>
                      <div class="h-3 w-24 rounded skeleton"></div>
                    </div>
                  </td>
                  <td class="px-6 py-5">
                    <div class="h-6 w-16 rounded-full skeleton"></div>
                  </td>
                  <td class="px-6 py-5">
                    <div class="h-4 w-16 rounded skeleton"></div>
                  </td>
                </tr>
              </tbody>
              <tbody v-else-if="isError">
                <tr>
                  <td colspan="4" class="px-6 py-14 text-center text-slate-400 text-sm">
                    결제 내역을 불러오지 못했습니다. 잠시 후 다시 시도해주세요.
                  </td>
                </tr>
              </tbody>
              <tbody v-else-if="paymentList.length > 0" class="divide-y divide-slate-50">
                <tr v-for="item in paymentList" :key="item.ordersIdx" class="hover:bg-slate-50/30 transition-colors">
                  <td class="px-6 py-5">
                    <div class="flex items-center gap-4">
                      <div class="w-12 h-12 rounded-lg bg-slate-100 flex-shrink-0 flex items-center justify-center overflow-hidden">
                        <i :class="[item.icon, 'text-slate-400']"></i>
                      </div>
                      <div>
                        <p class="text-sm font-bold text-slate-800">{{ item.title }}</p>
                        <p class="text-[11px] text-slate-400">{{ item.date }} · 주문번호: {{ item.ordersIdx }}</p>
                      </div>
                    </div>
                  </td>
                  <td class="px-6 py-5">
                    <p class="text-sm font-bold text-slate-800">{{ formatPrice(item.amount) }}</p>
                  </td>
                  <td class="px-6 py-5">
                    <span v-if="item.refunded" class="px-2.5 py-1 rounded-full text-[10px] font-bold status-badge-refunded">환불완료</span>
                    <span v-else class="px-2.5 py-1 rounded-full text-[10px] font-bold status-badge-complete">결제완료</span>
                  </td>
                  <td class="px-6 py-5">
                    <button type="button" @click="openReceipt(item.ordersIdx)" class="text-[11px] font-bold text-slate-500 hover:text-brand transition-colors">영수증 보기</button>
                  </td>
                </tr>
              </tbody>
              <tbody v-else>
                <tr>
                  <td colspan="4" class="px-6 py-14 text-center text-slate-400 text-sm">
                    표시할 결제 내역이 없습니다.
                  </td>
                </tr>
              </tbody>
            </table>
          </div>

          <div v-if="!isLoading && !isError && totalPages > 1" class="p-6 bg-slate-50/30 flex justify-center border-t border-slate-50">
            <nav class="flex gap-1">
              <button type="button" :disabled="page === 0" @click="goToPage(page - 1)"
                class="w-8 h-8 flex items-center justify-center rounded-lg bg-white border border-slate-200 text-slate-400 hover:text-brand transition-all disabled:opacity-40 disabled:cursor-not-allowed">
                <i class="fa-solid fa-chevron-left text-[10px]"></i>
              </button>
              <button type="button" v-for="p in pageNumbers" :key="p" @click="goToPage(p - 1)"
                class="w-8 h-8 flex items-center justify-center rounded-lg font-bold text-xs transition-all"
                :class="p === displayPage
                  ? 'bg-brand text-white shadow-sm shadow-brand/20'
                  : 'bg-white border border-slate-200 text-slate-500 hover:border-brand/50'">
                {{ p }}
              </button>
              <button type="button" :disabled="!hasNext" @click="goToPage(page + 1)"
                class="w-8 h-8 flex items-center justify-center rounded-lg bg-white border border-slate-200 text-slate-400 hover:text-brand transition-all disabled:opacity-40 disabled:cursor-not-allowed">
                <i class="fa-solid fa-chevron-right text-[10px]"></i>
              </button>
            </nav>
          </div>
        </div>

        <div class="p-6 rounded-2xl bg-blue-50/50 border border-blue-100 flex items-start gap-4">
          <i class="fa-solid fa-circle-info text-blue-400 mt-1"></i>
          <div>
            <h4 class="text-sm font-bold text-slate-800">환불 규정 안내</h4>
            <p class="text-xs text-slate-500 leading-relaxed mt-1">
              구매 후 7일 이내, 진도율 5% 미만인 경우 100% 환불이 가능합니다. 패키지 강의의 경우 구성 항목에 따라 별도의 규정이 적용될 수 있습니다.
            </p>
          </div>
        </div>
      </div>
    </div>

    <!-- 영수증 모달 -->
    <div v-if="receiptOpen" class="fixed inset-0 z-50 flex items-center justify-center p-4 bg-slate-900/40" @click.self="closeReceipt">
      <div class="bg-white w-full max-w-md rounded-3xl shadow-2xl overflow-hidden">
        <div class="px-6 py-5 border-b border-slate-100 flex items-center justify-between">
          <h3 class="font-bold text-slate-800 flex items-center gap-2">
            <i class="fa-solid fa-receipt text-brand"></i> 영수증
          </h3>
          <button type="button" @click="closeReceipt" class="w-8 h-8 rounded-lg flex items-center justify-center text-slate-400 hover:bg-slate-100 transition-all">
            <i class="fa-solid fa-xmark"></i>
          </button>
        </div>

        <!-- 로딩 -->
        <div v-if="receiptLoading" class="p-6 space-y-3">
          <div class="h-4 w-1/2 rounded skeleton"></div>
          <div class="h-12 rounded skeleton"></div>
          <div class="h-12 rounded skeleton"></div>
        </div>

        <!-- 실패 -->
        <div v-else-if="!receipt" class="p-10 text-center text-slate-400 text-sm">
          영수증을 불러오지 못했습니다.
        </div>

        <!-- 내용 -->
        <div v-else class="p-6">
          <div class="flex justify-between text-xs text-slate-400 mb-1">
            <span>주문번호</span><span class="font-bold text-slate-600">{{ receipt.ordersIdx }}</span>
          </div>
          <div class="flex justify-between text-xs text-slate-400 mb-1">
            <span>결제일시</span><span class="font-bold text-slate-600">{{ fmtDate(receipt.paidAt) }}</span>
          </div>
          <div class="flex justify-between text-xs text-slate-400 mb-1">
            <span>결제 ID</span><span class="font-bold text-slate-600 truncate ml-4">{{ receipt.paymentId || '-' }}</span>
          </div>
          <div class="flex justify-between text-xs text-slate-400 mb-4">
            <span>상태</span>
            <span class="font-bold" :class="receipt.refunded ? 'text-red-500' : 'text-emerald-600'">
              {{ receipt.refunded ? '환불완료' : '결제완료' }}
            </span>
          </div>

          <div class="border-t border-slate-100 pt-4 space-y-3">
            <div v-for="(it, i) in (receipt.items || [])" :key="i" class="flex items-center gap-3">
              <div class="w-10 h-10 rounded-lg bg-slate-100 flex-shrink-0 overflow-hidden flex items-center justify-center">
                <img v-if="it.courseImage" :src="`${it.courseImage}`" :alt="it.courseName" class="w-full h-full object-cover" />
                <i v-else class="fa-solid fa-book text-slate-400"></i>
              </div>
              <p class="flex-grow text-sm text-slate-700 line-clamp-1">{{ it.courseName }}</p>
              <span class="text-sm font-bold text-slate-800">{{ formatPrice(it.salePrice) }}</span>
            </div>
          </div>

          <div class="border-t border-slate-100 mt-4 pt-4 flex justify-between items-center">
            <span class="text-sm font-bold text-slate-800">총 결제 금액</span>
            <span class="text-lg font-extrabold text-brand">{{ formatPrice(receipt.paymentPrice) }}</span>
          </div>
        </div>
      </div>
    </div>
  </main>
</template>

<style scoped>
.card-shadow {
  box-shadow: 0 10px 25px -5px rgba(0, 0, 0, 0.02), 0 8px 10px -6px rgba(0, 0, 0, 0.02);
}

.status-badge-complete {
  background: #f0fdf4;
  color: #16a34a;
}

.status-badge-refunded {
  background: #fef2f2;
  color: #dc2626;
}

.skeleton {
  background: linear-gradient(90deg, #f1f5f9 25%, #e2e8f0 50%, #f1f5f9 75%);
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
