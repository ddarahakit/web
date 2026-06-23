import $axios from '@/plugins/axiosInterceptor'
import PortOne from "@portone/browser-sdk/v2"
import commonUtil from '@/utils/commonUtil'
import { request } from '@/api/request'

/** 주문 생성하기 */
const ordersCreate = (req) => request($axios.post('/orders/create', req))

/**
 * 결제하기 (PortOne SDK)
 * 표준 request() 래퍼 미적용: 성공 응답이 res 객체 자체(res.data 아님)이기 때문.
 */
const ordersPayment = async (req) => {
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

/** 결제 검증 */
const ordersVerify = (req) => request($axios.post('/orders/verify', req))

/** 주문 취소 */
const ordersCancel = (ordersIdx) => request($axios.delete(`/orders/${ordersIdx}`))

/**
 * 무료(₩0) 주문 완료
 * 포트원 결제 없이 서버에서 주문을 즉시 완료 처리한다(서버가 salePrice 합계 0 재검증).
 */
const ordersFreeComplete = (ordersIdx) => request($axios.post(`/orders/${ordersIdx}/free-complete`))

/** 주문 확인 */
const ordersCheck = (courseIdx) => request($axios.get(`/orders/check/${courseIdx}`))

/** 영수증 조회 (GET /orders/{ordersIdx}/receipt) */
const ordersReceipt = (ordersIdx) => request($axios.get(`/orders/${ordersIdx}/receipt`))

export default { ordersCreate, ordersPayment, ordersVerify, ordersCancel, ordersFreeComplete, ordersCheck, ordersReceipt }
