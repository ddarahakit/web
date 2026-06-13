<template>
    <v-dialog v-model="dialogInfo.toggle" persistent width="auto">
        <div class="popup-alert-wrap">
            <div class="alert-header">
                <p class="text">
                    {{ dialogInfo.header }}
                </p>
            </div>
            <div class="alert-content">
                <p class="text">
                    {{ dialogInfo.text }}
                </p>
            </div>
            <div class="alert-footer">
                <v-btn class="default" variant="text" @click="confirm">확인</v-btn>
            </div>
        </div>
    </v-dialog>
</template>

<script setup>
//defineModel 정의
const dialogInfo = defineModel('dialogInfo', { type: Object })

//defineEmits 정의
const emits = defineEmits(['confirmEvent'])

//확인 버튼 이벤트
const confirm = () => {
    //emit 호출
    if (dialogInfo.value.url) {
        emits('confirmEvent', dialogInfo.value.url)
    } else {
        emits('confirmEvent')
    }
    //팝업 닫기
    dialogInfo.value.toggle = false
}
</script>

<style scoped>

.v-overlay__content {
  outline: none;
  position: absolute;
  pointer-events: auto;
  contain: layout;
}

.v-overlay__scrim {
  pointer-events: auto;
  background: rgb(var(--v-theme-on-surface));
  border-radius: inherit;
  bottom: 0;
  left: 0;
  opacity: var(--v-overlay-opacity, 0.32);
  position: fixed;
  right: 0;
  top: 0;
}

.popup-alert-wrap {
  position: relative;
  width: 33rem;
  max-width: calc(100% - 9.6rem);
  max-height: calc(100% - 4.8rem);
  margin: 2.4rem 4.8rem;
  background-color: #fff;
  border-radius: 1.6rem;
}

.popup-alert-wrap .alert-header {
  position: relative;
  padding: 1rem 2.4rem 0 2.4rem;
  text-align: center;
}

.popup-alert-wrap .alert-header .text {
  font-size: 1.2rem;
  word-break: keep-all;
  white-space: pre-line;
  color: #384158;
  font-weight: 600;
}

.popup-alert-wrap .alert-content {
  position: relative;
  padding: 1rem 2.4rem;
  text-align: center;
}

.popup-alert-wrap .alert-content .text {
  font-size: 1rem;
  word-break: keep-all;
  white-space: pre-line;
}

.popup-alert-wrap .alert-footer {
  position: relative;
  display: flex;
  justify-content: center;
  align-items: center;
  width: 100%;
  height: 3.5rem;
  border-top: 0.1rem solid #e5e5e5;
}

.popup-alert-wrap .alert-footer.confirm .cancel {
  width: 50%;
  height: 100%;
  font-size: 1.6rem;
  color: #666;
}

.popup-alert-wrap .alert-footer.confirm .default {
  width: 50%;
}

.popup-alert-wrap .alert-footer.confirm .default:before {
  content: "";
  display: block;
  position: absolute;
  left: 0;
  top: 0;
  width: 0.1rem;
  height: 100%;
  background-color: #e5e5e5;
}

.popup-alert-wrap .alert-footer .default {
  width: 100%;
  height: 100%;
  font-size: 1rem;
  color: #14bdee;
}

::v-deep(.default:hover .v-btn__content) {
    z-index: 1;
    position: relative;
}

::v-deep(.default:hover .v-btn__overlay) {
    background-color: transparent !important;
}
</style>
