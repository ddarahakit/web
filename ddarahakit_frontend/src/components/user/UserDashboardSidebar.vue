<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { useRoute } from 'vue-router'
import api from '@/api/user'

const props = defineProps({
    heading: {
        type: String,
        default: 'Dashboard'
    },
    widgetType: {
        type: String,
        default: 'study' // 'study' | 'security' | 'payment'
    }
})

const route = useRoute()

// ── 포인트 (payment 위젯) ─────────────────────────────
const pointBalance = ref(0)
const pointsLoading = ref(true)

// 충전 모달
const chargeOpen = ref(false)
const chargeSaving = ref(false)
const chargeForm = reactive({ amount: 10000 })
const chargeAmounts = [10000, 30000, 50000, 100000]
const chargeError = ref('')

/**
 * 포인트 조회
 */
const getPoints = async () => {
    pointsLoading.value = true
    const data = await api.getPoints()
    if (data?.success && data.results) {
        pointBalance.value = Number(data.results.balance || 0)
    }
    pointsLoading.value = false
}

const openCharge = () => {
    chargeError.value = ''
    chargeForm.amount = 10000
    chargeOpen.value = true
}
const closeCharge = () => { chargeOpen.value = false }

/**
 * 포인트 충전
 */
const submitCharge = async () => {
    const amount = Number(chargeForm.amount)
    if (!Number.isFinite(amount) || amount < 1) {
        chargeError.value = '충전 금액은 1 이상이어야 합니다.'
        return
    }
    chargeSaving.value = true
    chargeError.value = ''
    const data = await api.chargePoints({ amount, memo: '포인트 충전' })
    if (data?.success && data.results) {
        pointBalance.value = Number(data.results.balance || pointBalance.value)
        chargeOpen.value = false
    } else {
        chargeError.value = data?.message || '포인트 충전에 실패했습니다.'
    }
    chargeSaving.value = false
}

onMounted(() => {
    if (props.widgetType === 'payment') {
        getPoints()
    }
})

const menuItems = computed(() => [
    {
        key: 'dashboard',
        label: '대시보드',
        icon: 'fa-solid fa-house-chimney text-sm',
        to: { name: 'dashboard' }
    },
    {
        key: 'profileEdit',
        label: '프로필 설정',
        icon: 'fa-solid fa-user-gear text-sm',
        to: { name: 'profileEdit' }
    },
    {
        key: 'accountSecurity',
        label: '계정 보안',
        icon: 'fa-solid fa-lock text-sm',
        to: { name: 'accountSecurity' }
    },
    {
        key: 'paymentHistory',
        label: '결제 내역',
        icon: 'fa-solid fa-credit-card text-sm',
        to: { name: 'paymentHistory' }
    }
])

const isActiveMenu = (key) => {
    return route.name === key
}
</script>

<template>
    <aside class="w-full lg:w-60 shrink-0">
        <div class="sticky top-28 space-y-1">
            <p class="px-4 text-[11px] font-bold text-slate-400 uppercase tracking-widest mb-4">
                {{ props.heading }}
            </p>

            <RouterLink
                v-for="item in menuItems"
                :key="item.key"
                :to="item.to"
                class="flex items-center gap-3 px-4 py-3 transition-all font-medium"
                :class="isActiveMenu(item.key)
                    ? 'sidebar-item-active font-bold'
                    : 'text-slate-500 hover:text-slate-800'">
                <i :class="item.icon"></i> {{ item.label }}
            </RouterLink>

            <div v-if="props.widgetType === 'study'" class="mt-12 p-5 rounded-2xl bg-white border border-slate-100 shadow-sm">
                <div class="flex items-center gap-2 mb-3">
                    <div class="w-2 h-2 rounded-full bg-orange-400"></div>
                    <p class="text-xs font-bold text-slate-700">연속 학습 5일째!</p>
                </div>
                <p class="text-[11px] text-slate-500 leading-relaxed mb-4">
                    오늘만 더 공부하면 이번 주 목표 달성입니다.
                </p>
                <button
                    class="w-full py-2 bg-slate-900 text-white rounded-lg text-[11px] font-bold hover:bg-slate-800 transition-colors">
                    학습 목표 설정
                </button>
            </div>

            <div v-else-if="props.widgetType === 'security'" class="mt-12 p-5 rounded-2xl bg-white border border-slate-100 shadow-sm">
                <div class="flex items-center gap-2 mb-3">
                    <div class="w-2 h-2 rounded-full bg-emerald-400"></div>
                    <p class="text-xs font-bold text-slate-700">보안 등급: 높음</p>
                </div>
                <p class="text-[10px] text-slate-400 leading-relaxed mb-4">
                    현재 계정은<br>안전하게 보호되고 있습니다.
                </p>
                <div class="flex -space-x-2">
                    <div class="w-6 h-6 rounded-full bg-blue-50 border-2 border-white flex items-center justify-center">
                        <i class="fa-solid fa-key text-[8px] text-brand"></i>
                    </div>
                    <div class="w-6 h-6 rounded-full bg-emerald-50 border-2 border-white flex items-center justify-center">
                        <i class="fa-solid fa-mobile-screen text-[8px] text-emerald-500"></i>
                    </div>
                </div>
            </div>

            <div v-else class="mt-12 p-5 rounded-2xl bg-gradient-to-br from-slate-800 to-slate-900 text-white shadow-lg rounded-2xl">
                <p class="text-[10px] font-bold text-slate-400 uppercase tracking-wider mb-1">My Points</p>
                <h4 class="text-xl font-bold mb-4">{{ pointsLoading ? '–' : pointBalance.toLocaleString() }} P</h4>
                <button type="button" @click="openCharge"
                    class="w-full py-2 bg-white/10 hover:bg-white/20 rounded-lg text-xs font-bold transition-all">
                    포인트 충전
                </button>
            </div>
        </div>

        <!-- 포인트 충전 모달 -->
        <Teleport to="body">
            <div v-if="chargeOpen" class="fixed inset-0 z-50 flex items-center justify-center p-4 bg-slate-900/40" @click.self="closeCharge">
                <div class="bg-white w-full max-w-sm rounded-3xl shadow-2xl overflow-hidden">
                    <div class="px-6 py-5 border-b border-slate-100 flex items-center justify-between">
                        <h3 class="font-bold text-slate-800 flex items-center gap-2">
                            <i class="fa-solid fa-coins text-amber-400"></i> 포인트 충전
                        </h3>
                        <button type="button" @click="closeCharge" class="w-8 h-8 rounded-lg flex items-center justify-center text-slate-400 hover:bg-slate-100 transition-all">
                            <i class="fa-solid fa-xmark"></i>
                        </button>
                    </div>

                    <div class="p-6 space-y-5">
                        <div>
                            <p class="text-[11px] font-bold text-slate-400 uppercase tracking-wider mb-1">현재 잔액</p>
                            <p class="text-lg font-bold text-slate-800">{{ pointBalance.toLocaleString() }} P</p>
                        </div>

                        <div>
                            <p class="text-[13px] font-bold text-slate-700 mb-2">충전 금액</p>
                            <div class="grid grid-cols-2 gap-2 mb-3">
                                <button type="button" v-for="amt in chargeAmounts" :key="amt" @click="chargeForm.amount = amt"
                                    class="py-2 rounded-xl text-sm font-bold border transition-all"
                                    :class="Number(chargeForm.amount) === amt ? 'bg-brand text-white border-brand' : 'border-slate-200 text-slate-600 hover:border-brand'">
                                    {{ amt.toLocaleString() }} P
                                </button>
                            </div>
                            <input v-model.number="chargeForm.amount" type="number" min="1"
                                class="w-full px-4 py-3 rounded-xl border border-slate-200 text-sm focus:outline-none focus:ring-2 focus:ring-brand/20"
                                placeholder="직접 입력" />
                            <p v-if="chargeError" class="text-xs text-red-500 mt-2">{{ chargeError }}</p>
                        </div>
                    </div>

                    <div class="px-6 py-5 bg-slate-50/40 border-t border-slate-100 flex gap-2">
                        <button type="button" @click="closeCharge"
                            class="flex-1 py-3 bg-white border border-slate-200 text-slate-600 rounded-xl font-bold text-sm hover:bg-slate-50 transition-all">
                            취소
                        </button>
                        <button type="button" @click="submitCharge" :disabled="chargeSaving"
                            class="flex-1 py-3 bg-brand text-white rounded-xl font-bold text-sm hover:opacity-90 transition-all disabled:opacity-50 disabled:cursor-not-allowed">
                            <i v-if="chargeSaving" class="fa-solid fa-spinner fa-spin mr-1"></i> 충전하기
                        </button>
                    </div>
                </div>
            </div>
        </Teleport>
    </aside>
</template>

<style scoped>
.sidebar-item-active {
    color: #14BCED;
    background: rgba(20, 188, 237, 0.06);
    border-radius: 0.75rem;
}
</style>
