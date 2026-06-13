import $axios from '@/plugins/axiosInterceptor'


/**
 * 장바구니 목록 조회
 */
const cartList = async () => {
    let data = {}

    await $axios
        .get('/cart')
        .then((res) => {
            data = res.data
        })
        .catch((error) => {
            data = error.data
        })

    return data
}

/**
 * 장바구니 항목 추가
 */
const cartAdd = async (courseIdx) => {
    let data = {}

    await $axios
        .post('/cart', { courseIdx })
        .then((res) => {
            data = res.data
        })
        .catch((error) => {
            data = error.data
        })

    return data
}

/**
 * 장바구니 항목 개별 삭제
 */
const cartRemove = async (cartItemIdx) => {
    let data = {}

    await $axios
        .delete(`/cart/${cartItemIdx}`)
        .then((res) => {
            data = res.data
        })
        .catch((error) => {
            data = error.data
        })

    return data
}

/**
 * 장바구니 전체 비우기
 */
const cartClear = async () => {
    let data = {}

    await $axios
        .delete('/cart')
        .then((res) => {
            data = res.data
        })
        .catch((error) => {
            data = error.data
        })

    return data
}


/**
 * 장바구니 항목 수 조회
 */
const cartCount = async () => {
    let data = {}

    await $axios
        .get('/cart/count')
        .then((res) => {
            data = res.data
        })
        .catch((error) => {
            data = error.data
        })

    return data
}


export default { cartList, cartAdd, cartRemove, cartClear, cartCount }
