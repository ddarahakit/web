<script setup>
import { onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import api from '@/api/user'
import Loading from '@/components/base/Loading.vue'
import useLoadingStore from '@/stores/useLoadingStore'


//로딩 저장소 정보 객체
const loadingStore = useLoadingStore();


// 라우트 정보 객체
const route = useRoute()

//라우터 정보 객체
const router = useRouter()


const verifyInfo = {
    email: route.query.email || '',
    uuid: route.query.uuid || ''
}

 const emailVerify = async () => {
  
    //API: 회원가입(개인사용자)
    const data = await api.emailVerify(verifyInfo)

    if (data.success) {
        router.push({ name: 'login' });

    } else if (data.code === 20004) {

        //에러 처리
        loginResError.message = data.message
        loginResError.toggle = true
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

onMounted( () => {
  loadingStore.startLoading();
  emailVerify()
  loadingStore.stopLoading();
})
</script>
<template>
  <div>
    <Loading />
  </div>
</template>