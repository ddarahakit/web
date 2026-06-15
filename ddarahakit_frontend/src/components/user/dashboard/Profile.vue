<script setup>
import { ref, reactive, onMounted } from 'vue'
import messageUtil from '@/utils/messageUtil'
import { useRouter } from 'vue-router'
import BasePupAlert from '@/components/base/BasePupAlert.vue'
import useAuthStore from '@/stores/useAuthStore'
import api from '@/api/user'
import { userImageUrl } from '@/utils/image'


const isEditProfile = ref(false)
const isEditPassword = ref(false)

const profileImageInput = ref(null)


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

// 사용자 프로필 정보 객체
const userProfile = ref({
  idx: 0,
  name: '',
  introduction: '',
  email: '',
  profileImageUrl: '',
  provider: ''
})

// 사용자 비밀번호호 정보 객체
const userPassword = ref({
  originPassword: '',
  newPassword1: '',
  newPassword2: '',
})

const originalProfile = ref({}) // 백업용



const updateProfile = async () => {
  if (JSON.stringify(userProfile.value) === JSON.stringify(originalProfile.value)) {
    isEditProfile.value = false
    return
  }

  const data = await api.updateProfile(userProfile.value)
  if (data.success) {
    isEditProfile.value = false
    originalProfile.value = { ...userProfile.value }
  } else {
    BasePupAlertInfo.toggle = true
    BasePupAlertInfo.text = messageUtil.getMessage('MYP00001')
  }
}

const cancelEdit = () => {
  userProfile.value = { ...originalProfile.value }
  userPassword.value = {
    originPassword: '',
    newPassword1: '',
    newPassword2: '',
  }
  isEditProfile.value = false
  isEditPassword.value = false
}

/**
 * 사용자 프로필 상세 조회
 */
const getUserProfile = async () => {
  //API: 사용자 프로필 상세 조회
  const data = await api.userProfile()
  if (data.success) {
    if (data.results) {
      //조회 결과
      userProfile.value = data.results
      originalProfile.value = { ...data.results } // 여기서 백업

      //헤더가 옛/깨진 값을 들고 있어도 최신 프로필 이미지로 동기화
      authStore.setUserProfileImage(data.results.profileImageUrl)
    }
  }
}

/**
 * 직접 로그아웃
 *
 * 저장소에서 정보 삭제 후 로그인 페이지로 이동한다.
 */
const logout = async () => {
  if (BasePupAlertInfo.header != '비밀번호 변경 완료') {
    return
  }
  //API: 사용자 로그아웃
  const result = await api.userLogout()

  if (result) {
    //스토리지 로그아웃
    authStore.logout()
    //메인 페이지로 이동
    router.push({ name: 'login' })
  }
}

/**
 * 비밀번호 변경
 */
const updatePassword = async () => {
  const data = await api.updatePassword(userPassword.value)
  if (data.success) {
    if (data.results) {
      // 팝업
      BasePupAlertInfo.toggle = true
      BasePupAlertInfo.header = '비밀번호 변경 완료'
      BasePupAlertInfo.text = '비밀번호 변경이 완료되었습니다.\n다시 로그인해 주세요.'

      cancelEdit()
    }
  } else {
    if (data.code === 20009) {
      // 팝업
      BasePupAlertInfo.toggle = true
      BasePupAlertInfo.header = '비밀번호 변경 실패'
      BasePupAlertInfo.text = data.message
    }
  }
}

/**
 * 비밀번호 찾기 요청
 */
const resetPassword = async () => {
  const data = await api.resetPasswordReq(userProfile.value)
  if (data.success) {
    if (data.results) {
      // 팝업
      BasePupAlertInfo.toggle = true
      BasePupAlertInfo.header = '비밀번호 변경 메일 발송'
      BasePupAlertInfo.text = '가입하신 이메일로 비밀번호 설정 메일이 발송되었습니다.\n메일이 확인되지 않을 경우, 스팸함을 확인해 주세요.'
    }
  }
}




const profileImageOpen = () => {
  profileImageInput.value.click()
}

const updateProfileImage = async (event) => {
  const file = event.target.files[0]
  if (!file) return

  const formData = new FormData()
  formData.append('profileImage', file)

  //API: 사용자 프로필 상세 조회
  const data = await api.updateProfileImage(formData)
  if (data.success) {
    if (data.results) {
      userProfile.value.profileImageUrl = data.results.profileImageUrl
      originalProfile.value.profileImageUrl = data.results.profileImageUrl
      // 헤더 등에서 참조하는 스토리지/반응형 값도 갱신 → 헤더 프로필 즉시 반영
      authStore.setUserProfileImage(data.results.profileImageUrl)
    }
  }
}

/**
 * 컴포넌트 마운트
 *
 */
onMounted(() => {
  getUserProfile()

  document.title = '내 프로필 | 따라학잇'
})
</script>
<template>
  <div class="profile p-4">
    <div v-if="userProfile.provider == 'email'" class="mb-3 d-flex justify-content-between flex-wrap">
      <div class="profile_edit">
        <h6>이메일</h6>
        <p class="text-muted mt-2">{{ userProfile.email }}</p>
      </div>
      <div class="d-flex align-items-center mb-3 comment_image">
        <div v-if="isEditProfile">
          <button class="btn btn-edit mt-2 me-2" @click="profileImageOpen">변경</button>
          <input type="file" ref="profileImageInput" @change="updateProfileImage" accept="image/*"
            style="display: none" />
        </div>
        <div class="profile_img">
          <template v-if="userProfile.profileImageUrl">
            <img :src="userImageUrl(userProfile.profileImageUrl)" class="rounded" alt="프로필 이미지">
          </template>
        </div>
      </div>

    </div>

    <div class="mb-3 d-flex justify-content-between flex-wrap">
      <div class="profile_edit">
        <h6>닉네임</h6>
        <div v-if="isEditProfile">
          <input v-model="userProfile.name" class="profile_input mt-2" />
        </div>
        <p v-else class="text-muted mt-2">{{ userProfile.name }}</p>
      </div>
      <div v-if="userProfile.provider != 'email'" class="d-flex align-items-center mb-3 comment_image">
        <div v-if="isEditProfile">
          <button class="btn btn-edit mt-2 me-2" @click="profileImageOpen">변경</button>
          <input type="file" ref="profileImageInput" @change="updateProfileImage" accept="image/*"
            style="display: none" />
        </div>
        <div class="profile_img">
          <template v-if="userProfile.profileImageUrl">
            <img :src="userImageUrl(userProfile.profileImageUrl)" class="rounded" alt="프로필 이미지">
          </template>
        </div>
      </div>
    </div>

    <div class="mb-3 d-flex justify-content-between flex-wrap">
      <div class="profile_edit">
        <h6>자기소개</h6>
        <div v-if="isEditProfile">
          <textarea v-model="userProfile.introduction" class="profile_textarea mt-2"
            placeholder="자기소개를 입력해주세요"></textarea>
        </div>
        <p v-else-if="userProfile.introduction == null" class="text-muted mt-2">간단한 소개글을 작성해보세요.</p>
        <p v-else class="text-muted mt-2">{{ userProfile.introduction }}</p>
      </div>
      <div>
        <button v-if="!isEditProfile" class="btn btn-edit mt-2" @click="isEditProfile = true">설정</button>
        <template v-else>
          <button class="btn btn-edit mt-2 me-2" @click="updateProfile">저장</button>
          <button class="btn btn-secondary mt-2" @click="cancelEdit">취소</button>
        </template>
      </div>
    </div>
    <hr v-if="userProfile.provider == 'email'">
    <div v-if="userProfile.provider == 'email'" class="d-flex justify-content-between align-items-center flex-wrap">
      <div class="profile_edit">
        <h6>비밀번호</h6>
        <div v-if="isEditPassword">
          <input v-model="userPassword.originPassword" class="profile_input mt-2" placeholder="현재 비밀번호" />
          <input v-model="userPassword.newPassword1" class="profile_input mt-2" placeholder="새 비밀번호" />
          <input v-model="userPassword.newPassword2" class="profile_input mt-2" placeholder="새 비밀번호 확인" />
        </div>
        <p v-else class="text-muted mt-2">비밀번호를 변경하려면 변경버튼을 눌러주세요.</p>
      </div>
      <div>
        <template v-if="!isEditPassword">
          <button class="btn btn-edit mt-2 me-2" @click="isEditPassword = true">변경</button>
          <button class="btn btn-edit mt-2" @click="resetPassword">찾기</button>
        </template>

        <template v-else>
          <button class="btn btn-edit mt-2 me-2" @click="updatePassword">변경</button>
          <button class="btn btn-secondary mt-2" @click="cancelEdit">취소</button>
        </template>
      </div>
    </div>
  </div>
  <BasePupAlert :dialog-info="BasePupAlertInfo" @confirm-event="logout" />

</template>

<style scoped>
.profile_edit {
  width: 60% !important;
  white-space: pre-line;
}

.profile_input {
  width: 100%;
  height: 46px;
  border: solid 1px #d9d9d9;
  border-radius: 3px;
  padding-left: 1rem;
  font-size: 16px;
  font-weight: 400;
  color: #2c3145;
}

.profile_input:focus {
  border: solid 1px #14bdee;
  outline: none;
}

.profile_textarea {
  width: 100%;
  height: 146px;
  border: solid 1px #d9d9d9;
  border-radius: 3px;
  padding: 0.5rem 1rem;
  font-size: 16px;
  font-weight: 400;
  color: #2c3145;
}


.profile_textarea:focus {
  border: solid 1px #14bdee;
  outline: none;
}

.profile {
  position: relative;
  max-width: 700px;
  margin: auto;
}

.btn-edit {
  color: #fff;
  background-color: #14bdee;
  border-color: #14bdee;
}

.comment_image {
  position: absolute;
  right: 0;
  top: 50%;
  transform: translateY(-50%);
}

.comment_image .profile_img {
  width: 60px;
  height: 60px;
  border-radius: 50%;
  border: 1px solid #efefef;
  overflow: hidden;
}

.comment_image .profile_img img {
  max-width: 100%;
  width: 60px;
  height: 60px;
}
</style>