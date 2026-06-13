import routeUtil from '@/utils/routeUtil'

/**
 * 주문문 라우트
 */
const ordersRoutes = {
    path: '/orders',
    component: () => import('@/layouts/DefaultLayout.vue'),
    children: [
        {
            name: 'ordersCart',
            path: 'cart',
            component: () => import('@/views/orders/Cart.vue'),
            meta: {
                title: '수강바구니 | 따라학잇',
                requiresAuth: true,
                category: [{ name: '수강바구니', path: '/user/dashboard' }]
            },
        }
    ]
}

export default ordersRoutes
