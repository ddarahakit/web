<script setup>
import { reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import useAuthStore from '@/stores/useAuthStore'
import api from '@/api/user'
import UserDashboardSidebar from '@/components/user/UserDashboardSidebar.vue'

const router = useRouter()
const authStore = useAuthStore()

const securityForm = reactive({
    originPassword: '',
    newPassword1: '',
    newPassword2: ''
})

const alert = reactive({
    show: false,
    type: 'success',
    message: ''
})

const showAlert = (type, message) => {
    alert.show = true
    alert.type = type
    alert.message = message
    setTimeout(() => { alert.show = false }, 3000)
}

const updatePassword = async () => {
    if (!securityForm.originPassword || !securityForm.newPassword1 || !securityForm.newPassword2) {
        showAlert('error', '모든 비밀번호 필드를 입력해주세요.')
        return
    }

    if (securityForm.newPassword1 !== securityForm.newPassword2) {
        showAlert('error', '새 비밀번호가 일치하지 않습니다.')
        return
    }

    const data = await api.updatePassword(securityForm)
    if (data.success) {
        showAlert('success', '비밀번호가 변경되었습니다. 다시 로그인해 주세요.')
        setTimeout(async () => {
            await api.userLogout()
            authStore.logout()
            router.push({ name: 'login' })
        }, 1500)
    } else {
        showAlert('error', data.message || '비밀번호 변경에 실패했습니다.')
    }
}

onMounted(() => {
    document.title = '비밀번호 변경 | 따라학잇'
})
</script>

<template>
    <main class="max-w-7xl mx-auto px-6 pt-28 pb-20">
        <transition name="slide-down">
            <div
                v-if="alert.show"
                class="fixed left-1/2 top-24 z-50 -translate-x-1/2 px-5 py-3 rounded-xl text-sm font-bold shadow-lg border"
                :class="alert.type === 'success'
                    ? 'bg-emerald-50 text-emerald-700 border-emerald-100'
                    : 'bg-red-50 text-red-600 border-red-100'">
                {{ alert.message }}
            </div>
        </transition>

        <div class="flex flex-col lg:flex-row gap-12">
            <UserDashboardSidebar heading="Account" />

            <div class="flex-grow space-y-8">
                <header>
                    <h1 class="text-3xl font-bold text-slate-900 tracking-tight">비밀번호 변경</h1>
                    <p class="text-slate-500 text-sm mt-1">주기적인 비밀번호 변경으로 계정을 안전하게 보호하세요.</p>
                </header>

                <div class="grid grid-cols-1 gap-6">
                    <div class="bg-white rounded-[2rem] border border-slate-100 shadow-sm p-8 md:p-10">
                        <form @submit.prevent="updatePassword" class="space-y-6 max-w-md">
                            <div class="space-y-2">
                                <label class="text-[13px] font-bold text-slate-700 ml-1">현재 비밀번호</label>
                                <input
                                    v-model="securityForm.originPassword"
                                    type="password"
                                    class="w-full px-5 py-3.5 rounded-2xl border border-slate-200 profile-input text-sm"
                                    placeholder="현재 비밀번호 입력">
                            </div>
                            <div class="space-y-2">
                                <label class="text-[13px] font-bold text-slate-700 ml-1">새 비밀번호</label>
                                <input
                                    v-model="securityForm.newPassword1"
                                    type="password"
                                    class="w-full px-5 py-3.5 rounded-2xl border border-slate-200 profile-input text-sm"
                                    placeholder="영문, 숫자, 특수문자 조합 8자 이상">
                            </div>
                            <div class="space-y-2">
                                <label class="text-[13px] font-bold text-slate-700 ml-1">새 비밀번호 확인</label>
                                <input
                                    v-model="securityForm.newPassword2"
                                    type="password"
                                    class="w-full px-5 py-3.5 rounded-2xl border border-slate-200 profile-input text-sm"
                                    placeholder="새 비밀번호 다시 입력">
                            </div>
                            <button type="submit" class="px-8 py-3 bg-slate-900 text-white rounded-xl font-bold text-sm hover:bg-slate-800 transition-all">
                                비밀번호 업데이트
                            </button>
                        </form>
                    </div>
                </div>
            </div>
        </div>
    </main>
</template>

<style scoped>
.slide-down-enter-active,
.slide-down-leave-active {
    transition: all 0.3s ease;
}

.slide-down-enter-from,
.slide-down-leave-to {
    opacity: 0;
    transform: translate(-50%, -20px);
}
</style>
