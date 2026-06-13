<script setup>
import { computed, reactive, ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import validateUtil from '@/utils/validateUtil'
import messageUtil from '@/utils/messageUtil'
import BasePupAlert from '@/components/base/BasePupAlert.vue'
import useAuthStore from '@/stores/useAuthStore'
import api from '@/api/user'

//라우트 정보 객체
const route = useRoute()
//라우터 정보 객체
const router = useRouter()

//저장소 정보 객체
const authStore = useAuthStore()

//BasePupAlert 정보 객체
let BasePupAlertInfo = reactive({
  toggle: false,
  header: '',
  text: ''
})

//폼 정보 객체
const passwordForm = ref()

//에러 객체 정보 객체
const passwordInputError = reactive({
  password1: {
    errorMessage: null,
    isValid: false
  },
  password2: {
    errorMessage: null,
    isValid: false
  },
})


const passwordResError = reactive({
  toggle: false,
  message: ''
})

//비밀번호 정보 객체
const passwordInput = reactive({
  email: route.query.email || '',
  uuid: route.query.uuid || '',
  password1: '', //비밀번호1
  password2: '', //비밀번호2
})


/**
 * 폼 컨트롤 객체
 * 버튼 활성화 및 텍스트를 제어한다.
 */
const formCntrObj = reactive({
  noStyl: { text: 'text-tip error' },
  submitBtn: { disabled: true }
})


/**
 * 비밀번호 유효성 룰
 *
 * 비밀번호 유효성 룰을 정의한다.
 */
const password1Rules = [
  (event) => {
    if (event.target.value) {
      passwordInputError['password1'].errorMessage = null
      passwordInputError.password1.isValid = true
      return true
    } else {
      passwordInputError['password1'].errorMessage = '비밀번호를 입력해주세요.'
      passwordInputError.password1.isValid = false
      return false
    }
  }
]

/**
 * 비밀번호 확인 유효성 룰
 *
 * 비밀번호 확인 유효성 룰을 정의한다.
 */
const password2Rules = [
  (event) => {
    if (event.target.value) {
      passwordInputError['password2'].errorMessage = null
      passwordInputError.password2.isValid = true
      return true
    } else {
      passwordInputError['password2'].errorMessage = '비밀번호를 입력해주세요.'
      passwordInputError.password2.isValid = false
      return false
    }
  }
]





/**
 * 비밀번호 및 비밀번호 확인 입력 이벤트
 *
 * 비밀번호 및 비밀번호 확인 입력 이벤트 리스너
 * 다음 버튼 활성화를 제어한다.
 */
const passwordCheckRules = async () => {
  if (passwordInput.password1.length >= 8 &&
    passwordInput.password2.length >= 8 &&
    passwordInput.password1 === passwordInput.password2) {
    //회원 가입 버튼 활성화 
    formCntrObj['submitBtn'].disabled = false
    passwordInputError.password2.isValid = true
  } else {
    //회원 가입 버튼 비활성화
    formCntrObj['submitBtn'].disabled = true
    passwordInputError.password2.isValid = false
  }
}




/**
 * 폼 유효성 검사
 */
const isFormValid = computed(() =>
  passwordInputError.password1.isValid &&
  passwordInputError.password2.isValid
);
/**
 * 폼 서브밋
 *
 * 저장소 객체에 존재하는 키에 대응하는 값을 본인확인 객체 값으로 변경한다.
 * 서비스이용동의 화면으로 이동한다.
 */
const submitForm = async () => {
  if (!validateUtil.password(passwordInput.password1)) {
    passwordInputError['password1'].errorMessage = messageUtil.getMessage('COM00003')
    return false
  }
  if (passwordInput.password1 != passwordInput.password2) {
    passwordInputError['password2'].errorMessage =
      '비밀번호가 일치하지 않습니다. 입력하신 비밀번호와 동일하게 입력해 주세요.'
    return false
  }

  //API: 회원가입(개인사용자)
  const data = await api.resetPassword(passwordInput)

  if (data.success) {
    // 팝업
    BasePupAlertInfo.toggle = true
    BasePupAlertInfo.header = '비밀번호 변경 성공'
    BasePupAlertInfo.text = '비밀번호 변경이 완료되었습니다.\n다시 로그인해 주세요.'

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

const goEvent = () => {
  //스토리지 로그아웃
  authStore.logout()
  
  router.push({ name: 'login' })
}

const checkUuidExpired = async () => {
  //API: 사용자 프로필 상세 조회
  const data = await api.checkUuidExpired(passwordInput)
  if (data.success) {
    if (data.results) {
      //조회 결과
      userProfile.value = data.results
      originalProfile.value = { ...data.results } // 여기서 백업

    }
  } else if (data.code === 20011) {
    BasePupAlertInfo.toggle = true
    BasePupAlertInfo.header = '비밀번호 변경 실패'
    BasePupAlertInfo.text = data.message
  }
}

/**
 * 컴포넌트 마운트
 *
 */
onMounted(() => {
  checkUuidExpired()

  document.title = '내 프로필 | 따라학잇'
})
</script>

<template>
  <div class="contact">
    <!-- Contact Info -->

    <div class="signup_info_container">
      <div class="container">
        <div class="row">
          <!-- Contact Form -->
          <div class="col-lg-6 mx-auto member">
            <div class="signup_form">
              <div class="signup_info_title">비밀번호 변경</div>
              <v-form ref="passwordForm" autocomplete="off" class="comment_form" fast-fail validate-on="blur lazy"
                @keypress.enter.prevent @submit.prevent="submitForm">
                <div v-if="passwordResError.toggle" class="alert alert-danger" role="alert">
                  <ul class="errorlist">
                    <li>{{ passwordResError.message }}</li>
                  </ul>
                </div>


                <div>
                  <div class="form_title">새로운 비밀번호</div>
                  <input class="comment_input" v-model="passwordInput.password1" aria-label="비밀번호" maxlength="20"
                    name="password1" placeholder="비밀번호 입력" required title="비밀번호 입력" type="password"
                    :class="{ error: passwordInputError.password1.errorMessage }" @blur="password1Rules" />

                  <p class="text-tip error">{{ passwordInputError['password1']?.errorMessage }}</p>
                </div>

                <div>
                  <div class="form_title">새로운 비밀번호 확인</div>
                  <input class="comment_input" v-model="passwordInput.password2" aria-label="비밀번호 확인" maxlength="20"
                    name="password2" placeholder="비밀번호 재입력" required title="비밀번호 재입력" type="password"
                    :class="{ error: passwordInputError.password2.errorMessage }" @blur="password2Rules"
                    @input="passwordCheckRules" @keypress.enter="submitForm" />

                  <p class="text-tip message">
                    비밀번호는 숫자,영문,특수문자( !@#$%^&*() )를 조합해 8~20자로 생성해주세요.
                  </p>

                  <p class="text-tip error">{{ passwordInputError['password2']?.errorMessage }}</p>
                </div>


                <div class="btn-wrap">
                  <div>
                    <button :disabled="!isFormValid" type="submit" @keypress.enter="submitForm"
                      class="comment_button trans_200">변경하기</button>
                  </div>
                </div>

              </v-form>

            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
  <BasePupAlert :dialog-info="BasePupAlertInfo" @confirm-event="goEvent" />
</template>

<style scoped>
/*********************************
7. Contact
*********************************/

.contact {
  width: 100%;
  padding-bottom: 100px;
}

.signup_map {
  width: 100%;
}

.map {
  width: 100%;
}

.google_map {
  width: 100%;
  height: 500px;
}

.map_container {
  width: 100%;
  height: 100%;
  overflow: hidden;
}

#map {
  width: 100%;
  height: calc(100% + 30px);
}

#content {
  width: 180px;
  padding-left: 20px;
  padding-top: 20px;
  padding-bottom: 20px;
  font-family: "Roboto", sans-serif;
  font-size: 14px;
  color: #76777a;
  line-height: 1.71;
}

.signup_info_container {
  margin-top: 100px;
}

.signup_info_title {
  font-family: "FontAwesome";
  font-size: 36px;
  font-weight: 700;
  color: #384158;
  text-align: center;
  margin-bottom: 30px;
}

.signup_form {
  padding-right: 10px;
}

.comment_form {
  margin-top: 35px;
}

.comment_input {
  width: 100%;
  height: 46px;
  border: solid 1px #d9d9d9;
  border-radius: 3px;
  padding-left: 19px;
  font-size: 16px;
  font-weight: 400;
  color: #2c3145;
}

.comment_input.error {
  border-color: rgb(255, 54, 54);
}

.comment_form>div:not(:last-child) {
  margin-bottom: 25px;
}

.comment_input:focus {
  border: solid 1px #14bdee;
  outline: none;
}

.comment_textarea {
  width: 100%;
  height: 150px;
  padding-top: 15px;
}

.form_title {
  font-size: 16px;
  font-weight: 400;
  color: #384158;
  margin-bottom: 12px;
}

.comment_button {
  width: 100%;
  height: 46px;
  background: #14bdee;
  text-transform: uppercase;
  font-size: 14px;
  font-weight: 500;
  color: #ffffff;
  cursor: pointer;
  border: none;
  outline: none;
  border-radius: 3px;
  margin-top: 15px;
}


.comment_button:disabled {
  pointer-events: none;
  opacity: 0.26;
}

.comment_button:hover {
  box-shadow: 0px 5px 40px rgba(0, 0, 0, 0.25);
}

.signup_info {
  padding-left: 45px;
}

.signup_info_text {
  margin-top: 27px;
}

.signup_info_location {
  margin-top: 28px;
}

.signup_info_location_title {
  font-family: "FontAwesome";
  font-size: 18px;
  font-weight: 700;
  color: #384158;
}

.location_list {
  margin-top: 14px;
}

.location_list li {
  font-size: 14px;
  color: #5e6271;
}

.location_list li:not(:last-child) {
  margin-bottom: 9px;
}

.signup_hr {
  width: 100%;
  display: flex;
  gap: 0.375rem;
  align-items: center;
  justify-content: center;
  flex-direction: row;
  margin-top: 30px;
}

.signup_hr:before {
  flex: 1 1 100%;
  content: "";
  display: block;
  height: 1px;
  background-color: rgb(233, 236, 239);
}

.signup_hr:after {
  flex: 1 1 100%;
  content: "";
  display: block;
  height: 1px;
  background-color: rgb(233, 236, 239);
}

.signup_hr_text {
  flex: 0 0 80px;
  color: rgb(173, 181, 189);
  font-size: 0.75rem;
  text-decoration: none;
  line-height: 1.5;
  text-underline-position: under;
}


.login-more-action {
  padding-top: 20px;
  display: flex;
  justify-content: center;
  align-items: center;
  margin-bottom: 24px;
}

.login-more-action-text {
  line-height: 1.38;
  letter-spacing: -0.3px;
  font-size: 13px;
  font-weight: 400;
  color: #616568;
  border-bottom: 1px solid #858a8d;
  cursor: pointer;
}

.login-more-action-divider {
  margin: 0 8px;
  width: 1px;
  height: 10px;
  background-color: #858a8d;
  pointer-events: none;
}
</style>
