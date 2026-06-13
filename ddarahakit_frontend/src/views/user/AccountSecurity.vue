<script setup>
import { reactive, ref, onMounted } from 'vue'
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

// 보안 설정 (서버 연동)
const otpEnabled = ref(false)
const loginAlertEnabled = ref(false)
const settingsLoading = ref(true)
const settingsSaving = ref(false)

// 접속 세션 목록
const sessions = ref([])
const sessionsLoading = ref(true)

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

/**
 * 보안 설정 조회
 */
const getSecuritySettings = async () => {
    settingsLoading.value = true
    const data = await api.getSecuritySettings()
    if (data?.success && data.results) {
        loginAlertEnabled.value = !!data.results.loginAlertEnabled
        otpEnabled.value = !!data.results.twoFactorEnabled
    }
    settingsLoading.value = false
}

/**
 * 보안 설정 변경 (토글 변경 시 PUT, 실패 시 롤백)
 *
 * @param {'otp'|'loginAlert'} field 변경된 토글
 */
const updateSetting = async (field) => {
    if (settingsSaving.value) return
    settingsSaving.value = true

    const data = await api.updateSecuritySettings({
        loginAlertEnabled: loginAlertEnabled.value,
        twoFactorEnabled: otpEnabled.value
    })

    if (data?.success && data.results) {
        // 서버 응답값으로 동기화
        loginAlertEnabled.value = !!data.results.loginAlertEnabled
        otpEnabled.value = !!data.results.twoFactorEnabled
        showAlert('success', '보안 설정이 변경되었습니다.')
    } else {
        // 실패 → 롤백
        if (field === 'otp') otpEnabled.value = !otpEnabled.value
        else if (field === 'loginAlert') loginAlertEnabled.value = !loginAlertEnabled.value
        showAlert('error', data?.message || '보안 설정 변경에 실패했습니다.')
    }
    settingsSaving.value = false
}

/**
 * 접속 세션 목록 조회
 */
const getSessions = async () => {
    sessionsLoading.value = true
    const data = await api.getSessions()
    if (data?.success && Array.isArray(data.results)) {
        sessions.value = data.results
    } else {
        sessions.value = []
    }
    sessionsLoading.value = false
}

/**
 * 세션 종료 (원격 로그아웃)
 */
const revokeSession = async (sessionIdx) => {
    const data = await api.revokeSession(sessionIdx)
    if (data?.success) {
        sessions.value = sessions.value.filter((s) => s.sessionIdx !== sessionIdx)
        showAlert('success', '해당 기기를 로그아웃했습니다.')
    } else {
        showAlert('error', data?.message || '세션 종료에 실패했습니다.')
    }
}

/**
 * userAgent → 사람이 읽기 쉬운 기기/브라우저 라벨
 */
const deviceLabel = (ua = '') => {
    if (!ua) return '알 수 없는 기기'
    let os = '기타 기기'
    if (/Windows/i.test(ua)) os = 'Windows PC'
    else if (/iPhone/i.test(ua)) os = 'iPhone'
    else if (/iPad/i.test(ua)) os = 'iPad'
    else if (/Android/i.test(ua)) os = 'Android'
    else if (/Macintosh|Mac OS/i.test(ua)) os = 'Mac'
    else if (/Linux/i.test(ua)) os = 'Linux'

    let browser = ''
    if (/Edg/i.test(ua)) browser = 'Edge'
    else if (/Chrome/i.test(ua)) browser = 'Chrome'
    else if (/Firefox/i.test(ua)) browser = 'Firefox'
    else if (/Safari/i.test(ua)) browser = 'Safari'

    return browser ? `${os} / ${browser}` : os
}

const isMobileUA = (ua = '') => /iPhone|iPad|Android/i.test(ua)

const fmtSessionDate = (raw) => {
    if (!raw) return ''
    const d = new Date(raw)
    if (Number.isNaN(d.getTime())) return ''
    const y = d.getFullYear()
    const m = String(d.getMonth() + 1).padStart(2, '0')
    const day = String(d.getDate()).padStart(2, '0')
    const hh = String(d.getHours()).padStart(2, '0')
    const mm = String(d.getMinutes()).padStart(2, '0')
    return `${y}.${m}.${day} ${hh}:${mm}`
}

onMounted(() => {
    document.title = '계정 보안 | 따라학잇'
    getSecuritySettings()
    getSessions()
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
            <UserDashboardSidebar heading="Account" widget-type="security" />

            <div class="flex-grow space-y-8">
                <header>
                    <h1 class="text-3xl font-bold text-slate-900 tracking-tight">계정 보안</h1>
                    <p class="text-slate-500 text-sm mt-1">소중한 학습 데이터와 개인정보를 안전하게 관리하세요.</p>
                </header>

                <div class="grid grid-cols-1 gap-6">
                    <div class="bg-white rounded-[2rem] border border-slate-100 shadow-sm p-8 md:p-10">
                        <div class="flex items-center gap-4 mb-8">
                            <div class="w-12 h-12 rounded-2xl bg-blue-50 text-brand flex items-center justify-center">
                                <i class="fa-solid fa-lock text-xl"></i>
                            </div>
                            <div>
                                <h3 class="text-lg font-bold text-slate-800">비밀번호 변경</h3>
                                <p class="text-xs text-slate-400">주기적인 비밀번호 변경으로 계정을 보호하세요.</p>
                            </div>
                        </div>

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

                    <div class="grid grid-cols-1 md:grid-cols-2 gap-6">
                        <div class="bg-white rounded-[2rem] border border-slate-100 shadow-sm p-8">
                            <div class="flex justify-between items-start mb-6">
                                <div class="w-10 h-10 rounded-xl bg-emerald-50 text-emerald-500 flex items-center justify-center">
                                    <i class="fa-solid fa-shield-check text-lg"></i>
                                </div>
                                <label class="switch">
                                    <input type="checkbox" v-model="otpEnabled" :disabled="settingsLoading || settingsSaving" @change="updateSetting('otp')">
                                    <span class="slider"></span>
                                </label>
                            </div>
                            <h4 class="text-base font-bold text-slate-800 mb-1">2단계 인증 (OTP)</h4>
                            <p class="text-xs text-slate-400 leading-relaxed mb-2">로그인 시 등록된 기기로 전송된 인증번호를 추가로 입력합니다.</p>
                            <p class="text-[11px] font-bold" :class="otpEnabled ? 'text-emerald-500' : 'text-slate-400'">
                                {{ settingsLoading ? '불러오는 중…' : (otpEnabled ? '사용 중' : '사용 안 함') }}
                            </p>
                        </div>

                        <div class="bg-white rounded-[2rem] border border-slate-100 shadow-sm p-8">
                            <div class="flex justify-between items-start mb-6">
                                <div class="w-10 h-10 rounded-xl bg-orange-50 text-orange-500 flex items-center justify-center">
                                    <i class="fa-solid fa-bell text-lg"></i>
                                </div>
                                <label class="switch">
                                    <input type="checkbox" v-model="loginAlertEnabled" :disabled="settingsLoading || settingsSaving" @change="updateSetting('loginAlert')">
                                    <span class="slider"></span>
                                </label>
                            </div>
                            <h4 class="text-base font-bold text-slate-800 mb-1">새로운 로그인 알림</h4>
                            <p class="text-xs text-slate-400 leading-relaxed mb-2">평소 사용하지 않는 기기나 장소에서 로그인 시 즉시 알림을 받습니다.</p>
                            <p class="text-[11px] font-bold" :class="loginAlertEnabled ? 'text-emerald-500' : 'text-slate-400'">
                                {{ settingsLoading ? '불러오는 중…' : (loginAlertEnabled ? '사용 중' : '사용 안 함') }}
                            </p>
                        </div>
                    </div>

                    <div class="bg-white rounded-[2rem] border border-slate-100 shadow-sm p-8">
                        <h3 class="text-lg font-bold text-slate-800 mb-6 flex items-center gap-2">
                            <i class="fa-solid fa-display text-slate-400 text-sm"></i> 접속 중인 기기 관리
                        </h3>

                        <!-- 로딩 -->
                        <div v-if="sessionsLoading" class="space-y-4">
                            <div v-for="n in 2" :key="n" class="flex items-center gap-4 p-4 rounded-2xl border border-slate-50">
                                <div class="w-10 h-10 rounded-full security-skeleton"></div>
                                <div class="flex-grow space-y-2">
                                    <div class="h-3 w-40 rounded security-skeleton"></div>
                                    <div class="h-3 w-28 rounded security-skeleton"></div>
                                </div>
                            </div>
                        </div>

                        <!-- 빈 상태 -->
                        <p v-else-if="sessions.length === 0" class="text-sm text-slate-400 text-center py-6">
                            활성 세션이 없습니다.
                        </p>

                        <!-- 세션 목록 -->
                        <div v-else class="space-y-4">
                            <div v-for="session in sessions" :key="session.sessionIdx"
                                class="flex items-center justify-between p-4 rounded-2xl border border-slate-50"
                                :class="session.current ? 'bg-slate-50/30' : 'bg-white'">
                                <div class="flex items-center gap-4">
                                    <div class="w-10 h-10 rounded-full bg-white flex items-center justify-center text-slate-400 border border-slate-100 shadow-sm">
                                        <i :class="isMobileUA(session.userAgent) ? 'fa-solid fa-mobile-screen-button' : 'fa-solid fa-laptop'"></i>
                                    </div>
                                    <div>
                                        <div class="flex items-center gap-2">
                                            <span class="text-sm font-bold text-slate-800">{{ deviceLabel(session.userAgent) }}</span>
                                            <span v-if="session.current" class="text-[10px] px-2 py-0.5 rounded-full security-badge-active font-bold">현재 기기</span>
                                        </div>
                                        <p class="text-[11px] text-slate-400">
                                            {{ session.ipAddress || 'IP 미상' }} · {{ fmtSessionDate(session.createdAt) }}
                                        </p>
                                    </div>
                                </div>
                                <button v-if="session.current" type="button" class="text-xs font-bold text-slate-300 cursor-not-allowed">로그아웃 불가</button>
                                <button v-else type="button" @click="revokeSession(session.sessionIdx)" class="text-xs font-bold text-red-400 hover:bg-red-50 px-3 py-1.5 rounded-lg transition-colors">원격 로그아웃</button>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    </main>
</template>

<style scoped>
.security-skeleton {
    background: linear-gradient(90deg, #f1f5f9 25%, #e2e8f0 50%, #f1f5f9 75%);
    background-size: 200% 100%;
    animation: security-skeleton-loading 1.5s infinite;
}

@keyframes security-skeleton-loading {
    0% { background-position: 200% 0; }
    100% { background-position: -200% 0; }
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

.security-badge-active {
    color: #059669;
    background: #ecfdf5;
    border: 1px solid #a7f3d0;
}

.switch {
    position: relative;
    display: inline-block;
    width: 42px;
    height: 24px;
}

.switch input {
    opacity: 0;
    width: 0;
    height: 0;
}

.slider {
    position: absolute;
    cursor: pointer;
    inset: 0;
    background-color: #e2e8f0;
    transition: .2s;
    border-radius: 999px;
}

.slider:before {
    position: absolute;
    content: "";
    height: 18px;
    width: 18px;
    left: 3px;
    top: 3px;
    background-color: white;
    transition: .2s;
    border-radius: 50%;
}

input:checked + .slider {
    background-color: #14bced;
}

input:checked + .slider:before {
    transform: translateX(18px);
}
</style>
