<script setup>
import { computed, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import validateUtil from '@/utils/validateUtil'
import messageUtil from '@/utils/messageUtil'
import api from '@/api/user'

//라우터 정보 객체
const router = useRouter()

//소셜 로그인 시작 (게이트웨이 OAuth2 authorization 으로 리다이렉트)
const backend = import.meta.env.VITE_API_BASE_URL
const loginWithSocial = (provider) => {
  window.location.href = `${backend}/oauth2/authorization/${provider}`
}


//폼 정보 객체
const signupForm = ref()

//에러 객체 정보 객체
const signupInputError = reactive({
  email: {
    errorMessage: null,
    isValid: false
  },
  name: {
    errorMessage: null,
    isValid: false
  },
  password: {
    errorMessage: null,
    isValid: false
  }
})


const signupResError = reactive({
  toggle: false,
  message: ''
})

//회원가입(개인) 본인확인 정보 객체
const signupInput = reactive({
  email: '', //이메일(아이디)
  name: '', //이름
  password: '', //비밀번호1
})


/**
 * 폼 컨트롤 객체
 * 버튼 활성화 및 텍스트를 제어한다.
 */
const formCntrObj = reactive({
  noStyl: { text: 'text-tip error' },
  submitBtn: { disabled: true }
})


const emailDuplicateCheck = async () => {
  const data = await api.emailDuplicateCheck(signupInput.email)
  if (!data.success) {
    if (data.code === 20006) {
      return true
    }
  }

  return false
}

/**
 * 이메일(아이디) 유효성 룰
 *
 * 이메일(아이디) 유효성 룰을 정의한다.
 */
const emailRules = [
  async (event) => {
    if (event.target.value) {
      if (/.+@.+\..+/.test(event.target.value)) {
        const isEmailDuplicate = await emailDuplicateCheck()
        //이메일 중복 체크  
        if (isEmailDuplicate) {
          signupInputError['email'].errorMessage = '이미 사용중인 이메일입니다.'
          signupInputError.email.isValid = false
          return false
        } else {
          signupInputError['email'].errorMessage = null
          signupInputError.email.isValid = true
          return true
        }
      } else {
        signupInputError['email'].errorMessage = '이메일 형식으로 입력해주세요.'
        signupInputError.email.isValid = false
        return false
      }
    } else {
      signupInputError['email'].errorMessage = '이메일주소는 필수 입력입니다.'
      signupInputError.email.isValid = false
      return false
    }
  }
]

/**
 * 비밀번호 유효성 룰
 *
 * 비밀번호 유효성 룰을 정의한다.
 * - 8자 이상
 * - 대문자 포함
 * - 소문자 포함
 * - 숫자 포함
 * - 특수문자 포함
 */
const passwordRules = [
  (event) => {
    const value = event.target.value
    if (!value) {
      signupInputError['password'].errorMessage = '비밀번호를 입력해주세요.'
      signupInputError.password.isValid = false
      return false
    }

    if (value.length < 8) {
      signupInputError['password'].errorMessage = '비밀번호는 8자 이상이어야 합니다.'
      signupInputError.password.isValid = false
      return false
    }

    if (!/[A-Z]/.test(value)) {
      signupInputError['password'].errorMessage = '대문자를 포함해야 합니다.'
      signupInputError.password.isValid = false
      return false
    }

    if (!/[a-z]/.test(value)) {
      signupInputError['password'].errorMessage = '소문자를 포함해야 합니다.'
      signupInputError.password.isValid = false
      return false
    }

    if (!/[0-9]/.test(value)) {
      signupInputError['password'].errorMessage = '숫자를 포함해야 합니다.'
      signupInputError.password.isValid = false
      return false
    }

    if (!/[!@#$%^&*()_+\-=\[\]{};':"\\|,.<>\/?]/.test(value)) {
      signupInputError['password'].errorMessage = '특수문자를 포함해야 합니다.'
      signupInputError.password.isValid = false
      return false
    }

    signupInputError['password'].errorMessage = null
    signupInputError.password.isValid = true
    return true
  }
]




/**
 * 이름 유효성 룰
 *
 * 이름 유효성 룰을 정의한다.
 */
const nameRules = [
  (event) => {
    if (event.target.value) {
      signupInputError['name'].errorMessage = null
      signupInputError.name.isValid = true
      return true
    } else {
      signupInputError['name'].errorMessage = '이름은 필수입력입니다.'
      signupInputError.name.isValid = false
      return false
    }
  }
]


/**
 * 이메일(아이디) 및 도메인명 입력 이벤트
 *
 * 이메일(아이디) 및 도메인명 입력 이벤트 리스너
 *
 * @params type이 em이면 이메일(아이디), dns이면 도메인명
 */
const lowerCase = (type, value) => {
  if (type === 'email') {
    signupInput.email = value.toLowerCase()
    if (signupInput.email.length > 30) {
      signupInput.email = signupInput.email.slice(0, 30)
    }
    return false
  } else {
    signupInput.dns = value.toLowerCase()
    if (signupInput.dns.length > 30) {
      signupInput.dns = signupInput.dns.slice(0, 30)
    }
    return false
  }
}




/**
 * 비밀번호 및 비밀번호 확인 입력 이벤트
 *
 * 비밀번호 및 비밀번호 확인 입력 이벤트 리스너
 * 다음 버튼 활성화를 제어한다.
 */
const passwordCheckRules = async () => {
  if (signupInput.password.length >= 8) {
    //회원 가입 버튼 활성화 
    formCntrObj['submitBtn'].disabled = false
    signupInputError.password.isValid = true
  } else {
    //회원 가입 버튼 비활성화
    formCntrObj['submitBtn'].disabled = true
    signupInputError.password.errorMessage = messageUtil.getMessage('COM00003')
    signupInputError.password.isValid = false
  }
}





/**
 * 이름 입력 이벤트
 *
 * 이름 입력 이벤트 리스너
 */
const onlyString = (value) => {
  //문자열 값 유효성 검증
  if (!validateUtil.nameChk(value) || signupInput.name.length > 20) {
    signupInput.name = value.replace(/[0-9~!@#$%^*()+\-=?;:`",.<>{|}[\]\\/_'&\s]/g, '')
    if (signupInput.name.length > 20) {
      signupInput.name = signupInput.name.slice(0, 20)
    }
    return false
  }
}

/**
 * 폼 유효성 검사
 */
//약관 동의 상태
const agreeTerms = ref(false)      // [필수] 이용약관 및 개인정보 수집·이용
const agreeMarketing = ref(false)  // [선택] 마케팅 정보 수신

//핵심 입력 필드 유효성 (약관 제외)
const coreFieldsValid = computed(() =>
  signupInputError.email.isValid &&
  signupInputError.name.isValid &&
  signupInputError.password.isValid
);

//폼 전체 유효성 = 핵심 필드 + 필수 약관 동의
const isFormValid = computed(() => coreFieldsValid.value && agreeTerms.value);

//필수 약관 미동의 안내 노출 (핵심 필드는 통과했는데 약관만 미동의일 때)
const showTermsError = computed(() => coreFieldsValid.value && !agreeTerms.value);
/**
 * 폼 서브밋
 *
 * 저장소 객체에 존재하는 키에 대응하는 값을 본인확인 객체 값으로 변경한다.
 * 서비스이용동의 화면으로 이동한다.
 */
const submitForm = async () => {
  //필수 약관 미동의 시 차단 (엔터 제출 등 우회 방지)
  if (!agreeTerms.value) {
    return false
  }

  if (!validateUtil.password(signupInput.password)) {
    signupInputError['password'].errorMessage = messageUtil.getMessage('COM00003')
    return false
  }

  //API: 회원가입(개인사용자)
  const data = await api.userSignup(signupInput)

  if (data.success) {
    router.push({ name: 'login' })

    return
  } else if (data.code === '41001') {

    //에러 처리
    BasePupAlertInfo.text = data.errorMessage
    BasePupAlertInfo.toggle = true
    return false
  }
  //제외 코드
  else if (
    data.code === '42901' ||
    data.code === '42902' ||
    data.code === '42910' ||
    data.code === '41002' ||
    data.code === '42911' ||
    data.code === '42913'
  ) {
    return false
  } else {
    //에러 처리
    BasePupAlertInfo.text = data.errorMessage
    BasePupAlertInfo.toggle = true
    return false
  }

  return false
}

</script>

<template>
  <main class="pt-24 max-w-7xl mx-auto px-6 py-12 flex flex-col md:flex-row items-center justify-center gap-8">
    <div class="max-w-lg w-full bg-white rounded-3xl shadow-xl shadow-blue-100/50 border border-gray-100 p-8 md:p-10">
      <div class="text-center mb-8">
        <h1 class="text-3xl font-bold text-gray-900 mb-3">계정 만들기 🚀</h1>
        <p class="text-gray-500">따라학IT과 함께 성장을 시작해볼까요?</p>
      </div>

      <!-- 소셜 가입 버튼 -->
      <div class="grid grid-cols-1 sm:grid-cols-2 gap-3 mb-8">
        <button type="button" class="social-btn btn-kakao py-3" @click="loginWithSocial('kakao')">
          <i class="fa-solid fa-comment text-lg"></i> 카카오로 시작하기
        </button>
        <button type="button" class="social-btn btn-google py-3" @click="loginWithSocial('google')">
          <i class="fa-brands fa-google text-xl"></i> Google로 시작하기
        </button>
      </div>

      <div class="relative my-8 text-center">
        <div class="absolute inset-0 flex items-center">
          <div class="w-full border-t border-gray-100"></div>
        </div>
        <span class="relative px-4 bg-white text-gray-400 text-sm">또는 이메일로 가입</span>
      </div>

      <!-- 이메일 회원가입 폼 -->
      <div class="space-y-5">
        <v-form ref="signupForm" autocomplete="off" class="space-y-5 comment_form" fast-fail validate-on="blur lazy"
          @keypress.enter.prevent @submit.prevent="submitForm">
          <p v-if="signupResError.toggle" class="error-message" style="display: flex;">
            <i :class="formCntrObj['noStyl'].text" class="fa-solid fa-circle-exclamation"></i>
            {{ signupResError.message }}
          </p>

          <div>
            <label for="name" class="block text-sm font-semibold text-gray-700 mb-2">이름</label>
            <input type="text" id="name" placeholder="실명을 입력해주세요" aria-label="이름" maxlength="20" name="name" required
              class="w-full px-4 py-3.5 rounded-xl border border-gray-200 focus-ring transition-all placeholder:text-gray-300"
              v-model="signupInput.name" @blur="nameRules"
              @text-change="nameRules"
              :class="{ 'input-error': signupInputError.name.errorMessage }" @input="onlyString(signupInput.name)" />
            <p :class="formCntrObj['noStyl'].text" class="error-message">
              <i class="fa-solid fa-circle-exclamation"></i>{{ signupInputError['name']?.errorMessage }}
            </p>
          </div>
          <div>
            <label for="email" class="block text-sm font-semibold text-gray-700 mb-2">이메일</label>
            <input type="email" id="email" placeholder="example@따라학IT.com"
              class="w-full px-4 py-3.5 rounded-xl border border-gray-200 focus-ring transition-all placeholder:text-gray-300"
              aria-label="이메일(아이디)" maxlength="30" required name="email" v-model="signupInput.email"
              :class="{ 'input-error': signupInputError.email.errorMessage }" @blur="emailRules" @text-change="emailRules"
              @input="[lowerCase('email', signupInput.email)]" />
            <p :class="formCntrObj['noStyl'].text" class="error-message">
              <i class="fa-solid fa-circle-exclamation"></i>{{ signupInputError['email']?.errorMessage }}
            </p>
          </div>
          <div>
            <label for="password" class="block text-sm font-semibold text-gray-700 mb-2">비밀번호</label>
            <input type="password" id="password" placeholder="8자 이상, 대/소문자, 숫자, 특수문자 포함" aria-label="비밀번호 확인" maxlength="20"
              required
              class="w-full px-4 py-3.5 rounded-xl border border-gray-200 focus-ring transition-all placeholder:text-gray-300"
              v-model="signupInput.password" :class="{ 'input-error': signupInputError.password.errorMessage }"
              @blur="passwordRules" @text-change="passwordRules" @input="passwordRules" @keypress.enter="submitForm" />

            <p :class="formCntrObj['noStyl'].text" class="error-message">
              <i class="fa-solid fa-circle-exclamation"></i>{{ signupInputError['password']?.errorMessage }}
            </p>
          </div>
          

          <!-- 약관 동의 -->
          <div class="pt-2 space-y-3">
            <label class="flex items-start gap-3 cursor-pointer group">
              <input type="checkbox" v-model="agreeTerms" class="mt-1 w-4 h-4 rounded border-gray-300 text-brand" />
              <span class="text-sm text-gray-600">
                <span class="font-bold text-brand">[필수]</span> 이용약관 및 개인정보 수집 이용에
                동의합니다.
              </span>
            </label>
            <label class="flex items-start gap-3 cursor-pointer group">
              <input type="checkbox" v-model="agreeMarketing" class="mt-1 w-4 h-4 rounded border-gray-300 text-brand" />
              <span class="text-sm text-gray-600">
                <span class="text-gray-400">[선택]</span> 마케팅 정보 수신에 동의합니다.
              </span>
            </label>
            <p v-if="showTermsError" id="termsError" class="error-message" style="display: flex;">
              <i class="fa-solid fa-circle-exclamation"></i> 필수 약관에 동의하셔야 합니다.
            </p>
          </div>

          <button :disabled="!isFormValid" id="submitBtn" @keypress.enter="submitForm" type="submit"
            class="submit-btn w-full py-4 bg-brand text-white rounded-xl font-bold text-lg shadow-lg shadow-blue-200 mt-4">
            가입하기
          </button>
        </v-form>
      </div>


      <!-- 로그인 유도 -->
      <div class="mt-10 text-center border-t border-gray-50 pt-8">
        <p class="text-gray-500 text-sm">
          이미 계정이 있으신가요?
          <RouterLink :to="{ name: 'login' }" class="link-btn text-brand font-bold ml-2">로그인</RouterLink>
        </p>
      </div>
    </div>
  </main>

</template>

<style scoped>
/* ===== 제출 버튼 (회원가입) ===== */
.submit-btn {
  cursor: pointer;
  transition: transform 0.12s ease, box-shadow 0.15s ease,
    background-color 0.15s ease, opacity 0.15s ease;
}

.submit-btn:hover:not(:disabled) {
  transform: translateY(-1px);
  background-color: var(--color-brand-dark, #0ea5e9);
  box-shadow: 0 10px 18px -6px rgba(20, 188, 237, 0.55);
}

.submit-btn:active:not(:disabled) {
  transform: translateY(0);
  box-shadow: 0 3px 8px -4px rgba(20, 188, 237, 0.5);
}

.submit-btn:focus-visible {
  outline: 3px solid rgba(20, 188, 237, 0.5);
  outline-offset: 2px;
}

/* ===== 소셜 버튼 ===== */
.social-btn {
  cursor: pointer;
  transition: transform 0.12s ease, box-shadow 0.15s ease,
    background-color 0.15s ease, border-color 0.15s ease, opacity 0.15s ease;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 12px;
  width: 100%;
  padding: 12px;
  border-radius: 12px;
  font-weight: 600;
  font-size: 0.95rem;
  border: 1px solid #e5e7eb;
  margin-bottom: 10px;
}

.social-btn:hover:not(:disabled) {
  transform: translateY(-1px);
  box-shadow: 0 6px 14px -4px rgba(0, 0, 0, 0.18);
}

.social-btn:active:not(:disabled) {
  transform: translateY(0);
  box-shadow: 0 2px 6px -2px rgba(0, 0, 0, 0.18);
}

.social-btn:focus-visible {
  outline: 3px solid rgba(20, 188, 237, 0.5);
  outline-offset: 2px;
}

.btn-kakao {
  background-color: #fee500;
  color: #3c1e1e;
  border: none;
}

.btn-kakao:hover:not(:disabled) {
  background-color: #f7d800;
}

.btn-google {
  background-color: #ffffff;
  color: #1f2937;
}

.btn-google:hover:not(:disabled) {
  background-color: #f9fafb;
  border-color: #d1d5db;
}

/* ===== 링크형 보조 버튼 (로그인 이동) ===== */
.link-btn {
  display: inline-block;
  cursor: pointer;
  border-radius: 6px;
  padding: 2px 4px;
  text-decoration: none;
  transition: background-color 0.15s ease, color 0.15s ease;
}

.link-btn:hover {
  background-color: rgba(20, 188, 237, 0.1);
  text-decoration: none;
}

.link-btn:focus-visible {
  outline: 3px solid rgba(20, 188, 237, 0.5);
  outline-offset: 2px;
}

/* 모션 최소화 선호 사용자: transform 비활성 */
@media (prefers-reduced-motion: reduce) {
  .submit-btn,
  .social-btn {
    transition: background-color 0.15s ease, opacity 0.15s ease;
  }
  .submit-btn:hover:not(:disabled),
  .submit-btn:active:not(:disabled),
  .social-btn:hover:not(:disabled),
  .social-btn:active:not(:disabled) {
    transform: none;
  }
}

/* 입력 검증 스타일 */
.input-group {
  position: relative;
}

/* 에러 상태 */
.input-error {
  border-color: #ef4444 !important;
  background-color: #fef2f2;
}

.error-message {
  color: #ef4444;
  font-size: 0.75rem;
  margin-top: 0.35rem;
  display: none;
  align-items: center;
  gap: 4px;
}

.input-error+.error-message {
  display: flex;
}

/* 성공 상태 */
.input-success {
  border-color: #10b981 !important;
}

input[type='checkbox'] {
  accent-color: #14bced;
}

.glass-nav {
  background: rgba(255, 255, 255, 0.8);
  backdrop-filter: blur(10px);
  border-bottom: 1px solid rgba(0, 0, 0, 0.05);
}

/* 버튼 비활성화 스타일 */
#submitBtn:disabled {
  opacity: 0.4;
  cursor: not-allowed;
  pointer-events: none;
  box-shadow: none;
}
</style>
