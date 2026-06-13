<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import useAuthStore from '@/stores/useAuthStore'
import api from '@/api/user'
import UserDashboardSidebar from '@/components/user/UserDashboardSidebar.vue'
import { userImageUrl } from '@/utils/image'

const router = useRouter()
const authStore = useAuthStore()

// 프로필 정보
const userProfile = ref({
    idx: 0,
    name: '',
    introduction: '',
    email: '',
    profileImageUrl: '',
    provider: ''
})

const originalProfile = ref({})

// 비밀번호 변경
const isEditPassword = ref(false)
const userPassword = ref({
    originPassword: '',
    newPassword1: '',
    newPassword2: '',
})

// 알림 메시지
const alert = reactive({
    show: false,
    type: 'success', // 'success' | 'error'
    message: ''
})

const showAlert = (type, message) => {
    alert.show = true
    alert.type = type
    alert.message = message
    setTimeout(() => { alert.show = false }, 3000)
}

// 프로필 조회
const getUserProfile = async () => {
    const data = await api.userProfile()
    if (data.success && data.results) {
        userProfile.value = data.results
        originalProfile.value = { ...data.results }
    }
}

// 프로필 저장
const updateProfile = async () => {
    if (JSON.stringify(userProfile.value) === JSON.stringify(originalProfile.value)) {
        showAlert('success', '변경된 내용이 없습니다.')
        return
    }

    const data = await api.updateProfile(userProfile.value)
    if (data.success) {
        originalProfile.value = { ...userProfile.value }
        showAlert('success', '프로필이 저장되었습니다.')
    } else {
        showAlert('error', '프로필 저장에 실패했습니다.')
    }
}

// 프로필 수정 취소 → 대시보드로 이동
const cancelEdit = () => {
    router.push({ name: 'dashboard' })
}

// 프로필 이미지 변경
const updateProfileImage = async (event) => {
    const file = event.target.files[0]
    if (!file) return

    const formData = new FormData()
    formData.append('profileImage', file)

    const data = await api.updateProfileImage(formData)
    if (data.success && data.results) {
        userProfile.value.profileImageUrl = data.results.profileImageUrl
        originalProfile.value.profileImageUrl = data.results.profileImageUrl
        // 헤더 등에서 참조하는 스토리지 값도 갱신
        authStore.setUserProfileImage(data.results.profileImageUrl)
        showAlert('success', '프로필 이미지가 변경되었습니다.')
    }
}

// 비밀번호 변경
const updatePassword = async () => {
    if (!userPassword.value.originPassword || !userPassword.value.newPassword1 || !userPassword.value.newPassword2) {
        showAlert('error', '모든 비밀번호 필드를 입력해주세요.')
        return
    }
    if (userPassword.value.newPassword1 !== userPassword.value.newPassword2) {
        showAlert('error', '새 비밀번호가 일치하지 않습니다.')
        return
    }

    const data = await api.updatePassword(userPassword.value)
    if (data.success) {
        showAlert('success', '비밀번호가 변경되었습니다. 다시 로그인해 주세요.')
        setTimeout(async () => {
            const result = await api.userLogout()
            if (result) {
                authStore.logout()
                router.push({ name: 'login' })
            }
        }, 2000)
    } else {
        showAlert('error', data.message || '비밀번호 변경에 실패했습니다.')
    }
}

// 비밀번호 찾기
const resetPassword = async () => {
    const data = await api.resetPasswordReq(userProfile.value)
    if (data.success) {
        showAlert('success', '비밀번호 재설정 메일이 발송되었습니다.')
    }
}

onMounted(() => {
    document.title = '프로필 수정 | 따라학IT'
    getUserProfile()
    authStore.initSettings()
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
            <UserDashboardSidebar />

            <!-- 메인 컨텐츠 영역 -->
            <div class="flex-grow space-y-8">
                
                <!-- 상단 헤더 -->
                <header class="flex flex-col md:flex-row md:items-center justify-between gap-6">
                    <div>
                        <h1 class="text-3xl font-bold text-slate-900 tracking-tight">프로필 설정</h1>
                        <p class="text-slate-500 text-sm mt-1">{{ userProfile.name || '회원' }}님의 개성을 에듀스트림 친구들에게 보여주세요.</p>
                    </div>
                    <div class="flex items-center gap-3">
                        <button
                            @click="cancelEdit"
                            class="px-6 py-2.5 rounded-xl border border-slate-200 bg-white text-sm font-bold text-slate-500 hover:bg-slate-50 transition-colors">취소</button>
                        <button
                            @click="updateProfile"
                            class="px-8 py-2.5 bg-brand text-white rounded-xl font-bold text-sm shadow-lg shadow-blue-100 hover:translate-y-[-2px] transition-all">변경사항 저장</button>
                    </div>
                </header>

                <!-- 대시보드 스타일의 통합 카드 -->
                <div class="bg-white rounded-[2rem] border border-slate-100 shadow-sm overflow-hidden">
                    
                    <!-- 프로필 이미지 섹션 (그라데이션 배경 포함) -->
                    <div class="profile-card text-white relative overflow-hidden shadow-xl shadow-blue-100 h-32 md:h-40">
                        <div class="bg-pattern-icons" data-v-c667c4cb=""><i class="fa-solid fa-laptop text-4xl" data-v-c667c4cb=""></i><span data-v-c667c4cb=""></span><span data-v-c667c4cb=""></span><i class="fa-solid fa-keyboard text-4xl mt-10" data-v-c667c4cb=""></i><i class="fa-solid fa-mouse text-3xl ml-10" data-v-c667c4cb=""></i><i class="fa-solid fa-display text-4xl mt-5" data-v-c667c4cb=""></i><span data-v-c667c4cb=""></span><span data-v-c667c4cb=""></span><i class="fa-solid fa-microchip text-3xl" data-v-c667c4cb=""></i><i class="fa-solid fa-code text-4xl mt-12" data-v-c667c4cb=""></i><i class="fa-solid fa-server text-3xl ml-5" data-v-c667c4cb=""></i><i class="fa-solid fa-headphones text-4xl mt-4" data-v-c667c4cb=""></i><i class="fa-solid fa-mobile-screen text-3xl" data-v-c667c4cb=""></i></div>
                    </div>
                    
                    <div class="px-8 md:px-12 pb-12">
                        <!-- 아바타 위치 (배너에 걸치게) -->
                        <div class="relative -mt-16 mb-10 flex flex-col md:flex-row items-end gap-6">
                            <div class="relative group">
                                <div class="w-32 h-32 rounded-[2rem] bg-white p-1 shadow-lg">
                                    <div class="w-full h-full rounded-[1.8rem] overflow-hidden bg-blue-50">
                                        <img
                                            :src="userImageUrl(userProfile.profileImageUrl) || 'https://api.dicebear.com/7.x/avataaars/svg?seed=Felix'"
                                            alt="Profile"
                                            class="w-full h-full object-cover">
                                    </div>
                                </div>
                                <label
                                    for="avatar-upload"
                                    class="absolute bottom-1 right-1 w-10 h-10 bg-white text-slate-600 rounded-xl flex items-center justify-center shadow-lg cursor-pointer hover:bg-brand hover:text-white transition-all border border-slate-100">
                                    <i class="fa-solid fa-camera text-sm"></i>
                                    <input
                                        type="file"
                                        id="avatar-upload"
                                        class="hidden"
                                        accept="image/*"
                                        @change="updateProfileImage">
                                </label>
                            </div>
                            <div class="flex-grow pt-2">
                                <h2 class="text-2xl font-bold text-slate-900">
                                    {{ userProfile.name || '이름 없음' }}
                                    <span class="text-sm font-medium text-slate-400 ml-2">{{ userProfile.email }}</span>
                                </h2>
                                <p class="text-sm text-slate-400">
                                    {{ userProfile.provider ? `${userProfile.provider} 계정` : '일반 계정' }}
                                </p>
                            </div>
                        </div>

                        <form @submit.prevent="updateProfile" class="grid grid-cols-1 md:grid-cols-2 gap-x-8 gap-y-8">
                            <!-- 닉네임 -->
                            <div class="space-y-2.5">
                                <label class="text-[13px] font-bold text-slate-700 flex items-center gap-2 ml-1">
                                    <i class="fa-solid fa-signature text-brand text-[10px]"></i> 닉네임
                                </label>
                                <input
                                    type="text"
                                    v-model="userProfile.name"
                                    class="w-full px-5 py-3.5 rounded-2xl border border-slate-200 profile-input text-sm font-medium"
                                    placeholder="닉네임을 입력하세요">
                            </div>

                            <!-- 이메일 -->
                            <div class="space-y-2.5">
                                <label class="text-[13px] font-bold text-slate-700 flex items-center gap-2 ml-1">
                                    <i class="fa-solid fa-envelope text-brand text-[10px]"></i> 이메일 주소
                                </label>
                                <div class="relative">
                                    <input
                                        type="email"
                                        :value="userProfile.email"
                                        disabled
                                        class="w-full px-5 py-3.5 rounded-2xl border border-slate-100 bg-slate-50 text-slate-400 text-sm font-medium cursor-not-allowed">
                                    <i class="fa-solid fa-lock absolute right-5 top-1/2 -translate-y-1/2 text-[10px] text-slate-300"></i>
                                </div>
                            </div>

                            <!-- 한 줄 소개 -->
                            <div class="md:col-span-2 space-y-2.5">
                                <label class="text-[13px] font-bold text-slate-700 flex items-center gap-2 ml-1">
                                    <i class="fa-solid fa-quote-left text-brand text-[10px]"></i> 한 줄 소개
                                </label>
                                <input
                                    type="text"
                                    v-model="userProfile.introduction"
                                    class="w-full px-5 py-3.5 rounded-2xl border border-slate-200 profile-input text-sm font-medium"
                                    placeholder="자신을 한 줄로 멋지게 표현해 보세요">
                            </div>

                            <!-- 소셜 링크 -->
                            <div class="md:col-span-2 grid grid-cols-1 md:grid-cols-2 gap-6 pt-6">
                                <div class="space-y-2.5">
                                    <label class="text-[13px] font-bold text-slate-700 flex items-center gap-2 ml-1">
                                        <i class="fa-brands fa-github text-slate-900"></i> GitHub
                                    </label>
                                    <input type="text" class="w-full px-5 py-3.5 rounded-2xl border border-slate-200 profile-input text-sm font-medium" placeholder="https://github.com/...">
                                </div>
                                <div class="space-y-2.5">
                                    <label class="text-[13px] font-bold text-slate-700 flex items-center gap-2 ml-1">
                                        <i class="fa-solid fa-link text-green-500"></i> Blog / Website
                                    </label>
                                    <input type="text" class="w-full px-5 py-3.5 rounded-2xl border border-slate-200 profile-input text-sm font-medium" placeholder="https://...">
                                </div>
                            </div>
                        </form>
                    </div>
                </div>

                <!-- 위험 구역 영역 (더 부드러운 디자인) -->
                <div class="bg-white rounded-3xl border border-slate-100 p-8 flex flex-col md:flex-row items-center justify-between gap-6">
                    <div class="flex items-center gap-5">
                        <div class="w-12 h-12 rounded-2xl bg-red-50 text-red-500 flex items-center justify-center flex-shrink-0">
                            <i class="fa-solid fa-user text-xl"></i>
                        </div>
                        <div>
                            <h4 class="text-sm font-bold text-slate-800">회원 탈퇴</h4>
                            <p class="text-[11px] text-slate-400 mt-0.5">계정을 삭제하면 모든 학습 데이터가 영구히 소멸됩니다.</p>
                        </div>
                    </div>
                    <button
                        @click="resetPassword"
                        class="px-6 py-2.5 text-xs font-bold text-red-400 border border-red-100 rounded-xl hover:bg-red-50 transition-all">
                        탈퇴하기
                    </button>
                </div>
            </div>
        </div>
    </main>
</template>

<style scoped>
.profile-card {
    background: linear-gradient(135deg, #14BCED 0%, #0ea5e9 50%, #0284c7 100%);
}

.bg-pattern-icons {
    position: absolute;
    inset: 0;
    display: flex;
    flex-wrap: wrap;
    align-items: center;
    justify-content: space-around;
    opacity: 0.08;
    pointer-events: none;
    padding: 1rem;
}

.slide-down-enter-active,
.slide-down-leave-active {
    transition: all 0.3s ease;
}

.slide-down-enter-from,
.slide-down-leave-to {
    opacity: 0;
    transform: translate(-50%, -20px);
}

i {
    font-style: normal;
}
</style>
