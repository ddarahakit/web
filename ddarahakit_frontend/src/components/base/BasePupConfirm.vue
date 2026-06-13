<template>
    <v-dialog v-model="dialogInfo.toggle" persistent width="auto">
        <div class="popup-alert-wrap">
            <div class="alert-cont">
                <p class="text">{{ dialogInfo.text }}</p>
            </div>
            <div class="alert-foot confirm">
                <v-btn class="cancel" variant="text" @click="cancel">취소</v-btn>
                <v-btn class="default" variant="text" @click="confirm">확인</v-btn>
            </div>
        </div>
    </v-dialog>
</template>

<script setup>
//defineModel 정의
const dialogInfo = defineModel('dialogInfo', { type: Object })

//defineEmits 정의
const emits = defineEmits(['confirmEvent', 'cancelEvent'])

//확인 버튼 이벤트
const confirm = () => {
    //emit 호출
    if (dialogInfo.value.code) {
        emits('confirmEvent', dialogInfo.value.code)
    } else {
        emits('confirmEvent')
    }
    //팝업 닫기
    dialogInfo.value.toggle = false
}

//취소 버튼 이벤트
const cancel = () => {
    //emit 호출
    emits('cancelEvent')
    //팝업 닫기
    dialogInfo.value.toggle = false
}
</script>

<style scoped>
.v-btn--size-default {
    font-size: 1.4rem;
}
</style>
