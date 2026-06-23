import $axios from '@/plugins/axiosInterceptor'
import { request } from '@/api/request'

/** 장바구니 목록 조회 */
const cartList = () => request($axios.get('/cart'))

/** 장바구니 항목 추가 */
const cartAdd = (courseIdx) => request($axios.post('/cart', { courseIdx }))

/** 장바구니 항목 개별 삭제 */
const cartRemove = (cartItemIdx) => request($axios.delete(`/cart/${cartItemIdx}`))

/** 장바구니 전체 비우기 */
const cartClear = () => request($axios.delete('/cart'))

/** 장바구니 항목 수 조회 */
const cartCount = () => request($axios.get('/cart/count'))

export default { cartList, cartAdd, cartRemove, cartClear, cartCount }
