<script setup>
import { computed, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import BasePupAlert from '@/components/base/BasePupAlert.vue'
import api from '@/api/user'

//라우터 정보 객체
const router = useRouter()


//BasePupAlert 정보 객체
let BasePupAlertInfo = reactive({
  toggle: false,
  header: '',
  text: ''
})

//폼 정보 객체
const emailForm = ref()

//에러 객체 정보 객체
const emailInputError = reactive({
  email: {
    errorMessage: null,
    isValid: false
  }
})


const emailResError = reactive({
  toggle: false,
  message: ''
})

//회원가입(개인) 본인확인 정보 객체
const emailInput = reactive({
  email: '', //이메일(아이디)
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
 * 이메일(아이디) 유효성 룰
 *
 * 이메일(아이디) 유효성 룰을 정의한다.
 */
const emailRules = [
  async (event) => {
    if (event.target.value) {
      if (/.+@.+\..+/.test(event.target.value)) {
        emailInputError['email'].errorMessage = null
        emailInputError.email.isValid = true
        return true
      } else {
        emailInputError['email'].errorMessage = '이메일 형식으로 입력해주세요.'
        emailInputError.email.isValid = false
        return false
      }
    } else {
      emailInputError['email'].errorMessage = '이메일주소는 필수 입력입니다.'
      emailInputError.email.isValid = false
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
    emailInput.email = value.toLowerCase()
    if (emailInput.email.length > 30) {
      emailInput.email = emailInput.email.slice(0, 30)
    }
    return false
  } else {
    emailInput.dns = value.toLowerCase()
    if (emailInput.dns.length > 30) {
      emailInput.dns = emailInput.dns.slice(0, 30)
    }
    return false
  }
}



/**
 * 폼 유효성 검사
 */
const isFormValid = computed(() =>
  emailInputError.email.isValid
);
/**
 * 폼 서브밋
 *
 */
const submitForm = async () => {

  //API: 비밀번호 변경 이메일 요청
  const data = await api.resetPasswordReq(emailInput)

  if (data.success) {
    // 팝업
    BasePupAlertInfo.toggle = true
    BasePupAlertInfo.header = '비밀번호 변경 메일 발송'
    BasePupAlertInfo.text = '입력하신 이메일로 비밀번호 설정 메일이 발송되었습니다.\n메일이 확인되지 않을 경우, 스팸함을 확인해 주세요.'

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


const goMain = () => {
  router.push({ name: 'main' })
}


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
              <div class="signup_info_title">비밀번호 찾기</div>
              <v-form ref="emailForm" autocomplete="off" class="comment_form" fast-fail validate-on="blur lazy"
                @keypress.enter.prevent @submit.prevent="submitForm">
                <div v-if="emailResError.toggle" class="alert alert-danger" role="alert">
                  <ul class="errorlist">
                    <li>{{ emailResError.message }}</li>
                  </ul>
                </div>
                <div>
                  <div class="form_title">이메일</div>
                  <input class="comment_input" v-model="emailInput.email" aria-label="이메일(아이디)" maxlength="30"
                    name="email" placeholder="이메일 주소 입력" required title="이메일(아이디) 숫자 최대 50자리 입력" type="text"
                    :class="{ error: emailInputError.email.errorMessage }" @keyup="emailRules"
                    @input="[lowerCase('email', emailInput.email)]" />

                  <p :class="formCntrObj['noStyl'].text">
                    {{ emailInputError['email']?.errorMessage }}
                  </p>
                </div>

                <div class="btn-wrap">
                  <div>
                    <button :disabled="!isFormValid" type="submit" @keypress.enter="submitForm"
                      class="comment_button trans_200">변경 메일 보내기</button>
                  </div>
                </div>

              </v-form>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
  <BasePupAlert :dialog-info="BasePupAlertInfo" @confirm-event="goMain" />
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

.social_signup_icon_list_wrapper {
  width: 100%;
  display: flex;
  gap: 1.5rem;
  align-items: center;
  flex-direction: column;
}

.social_signup_icon_list {
  display: flex;
  gap: 0.5rem;
  margin: 20px 0px;
}

.social_signup_icon_wrapper {
  position: relative;
  margin: 0px 10px;
}

.social_signup_icon_btn {
  cursor: pointer;
  appearance: none;
  font-size: 1rem;
  text-align: left;
  text-decoration: none;
  box-sizing: border-box;
  position: relative;
  border-radius: 0.5rem;
  padding: 0px;
  line-height: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  height: 3.75rem;
  min-height: 3.75rem;
  width: 3.75rem;
  min-width: 3.75rem;
  border: 0.0625rem solid transparent;
  color: rgb(255, 255, 255);
  box-shadow: rgba(0, 0, 0, 0.2) 0px 1px 2px 0px;
}

.kakao {
  background-color: rgb(255, 212, 59);
}

.google {
  background-color: rgb(255, 255, 255);
}

.social_signup_icon_image {
  font-size: 30px;
  color: rgb(44, 46, 51);
  overflow: visible;
  box-sizing: content-box;
  height: 1em;
  overflow: visible;
  vertical-align: -0.125em;
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
