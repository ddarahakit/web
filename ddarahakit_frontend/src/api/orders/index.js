import $axios from '@/plugins/axiosInterceptor'
import PortOne from "@portone/browser-sdk/v2"
import commonUtil from '@/utils/commonUtil'


/**
 * 주문 생성하기
 */
const ordersCreate = async (req) => {
    //결과
    let data = {}

    //API 호출
    await $axios
        .post('/orders/create', req)
        .then((res) => {
            //성공
            data = res.data
        })
        .catch((error) => {
            //실패
            data = error.data
        })

    return data
}

/**
 * 결제하기
 */
const ordersPayment = async (req) => {
    //결과
    let data = {}

    // 결제 ID
    const paymentId = commonUtil.randomId()

    await PortOne
        .requestPayment({
            // 포트원 V2 상점/채널 식별자 (토스페이먼츠 채널) — 클라이언트 공개 식별자, env 주입
            storeId: import.meta.env.VITE_PORTONE_STORE_ID,
            channelKey: import.meta.env.VITE_PORTONE_CHANNEL_KEY,
            paymentId,
            orderName: req.orderName,
            totalAmount: req.totalAmount,
            currency: 'KRW',
            payMethod: "CARD",
            customData: req.customData
        }).then((res) => {
            //성공
            data = res
        })
        .catch((error) => {
            //실패
            data = error.data
        })

    return data
}



const ordersVerify = async (req) => {
    //결과
    let data = {}

    //API 호출
    await $axios
        .post('/orders/verify', req)
        .then((res) => {
            //성공
            data = res.data
        })
        .catch((error) => {
            //실패
            data = error.data
        })
    return data
}


const ordersCancel = async (ordersIdx) => {
    //결과
    let data = {}

    //API 호출
    await $axios
        .delete(`/orders/${ordersIdx}`)
        .then((res) => {
            //성공
            data = res.data
        })
        .catch((error) => {
            //실패
            data = error.data
        })
    return data
}





/**
 * 무료(₩0) 주문 완료
 *
 * 포트원 결제 없이 서버에서 주문을 즉시 완료 처리한다.
 * 서버가 해당 주문의 salePrice 합계가 실제 0인지 재검증한다.
 */
const ordersFreeComplete = async (ordersIdx) => {
    //결과
    let data = {}

    //API 호출
    await $axios
        .post(`/orders/${ordersIdx}/free-complete`)
        .then((res) => {
            //성공
            data = res.data
        })
        .catch((error) => {
            //실패
            data = error.data
        })
    return data
}


const ordersCheck = async (courseIdx) => {
    //결과
    let data = {}

    //API 호출
    await $axios
        .get(`/orders/check/${courseIdx}`)
        .then((res) => {
            //성공
            data = res.data
        })
        .catch((error) => {
            //실패
            data = error.data
        })
    return data
}


/**
 * 영수증 조회
 *
 * GET /orders/{ordersIdx}/receipt
 * results: ReceiptRes { ordersIdx, paymentId, paymentPrice, refunded, paidAt, items[] }
 */
const ordersReceipt = async (ordersIdx) => {
    //결과
    let data = {}

    //API 호출
    await $axios
        .get(`/orders/${ordersIdx}/receipt`)
        .then((res) => {
            //성공
            data = res.data
        })
        .catch((error) => {
            //실패
            data = error.data
        })
    return data
}




export default { ordersCreate, ordersPayment, ordersVerify, ordersCancel, ordersFreeComplete, ordersCheck, ordersReceipt }
