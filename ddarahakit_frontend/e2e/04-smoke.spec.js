import { test, expect } from '@playwright/test'
import { login } from './helpers.js'

// 로그인 후 주요 페이지 전반을 돌며 "JS 런타임 에러 없이 렌더되는지" 폭넓게 확인.
// (영상/이미지 404 같은 네트워크 잡음은 무시하고, pageerror=실제 미처리 예외만 검사)
const ROUTES = [
    { path: '/course/list', name: '코스 목록' },
    { path: '/course/11', name: '코스 상세' },
    { path: '/community/list', name: '커뮤니티 목록' },
    { path: '/community/102', name: '커뮤니티 상세' },
    { path: '/roadmap', name: '로드맵 목록' },
    { path: '/user/dashboard', name: '대시보드' },
    { path: '/user/profile', name: '프로필 수정' },
    { path: '/user/payments', name: '결제 내역' },
    { path: '/user/security', name: '계정 보안' },
    { path: '/orders/cart', name: '장바구니' },
]

test('로그인 후 주요 페이지 스모크 — 미처리 JS 예외 없이 렌더', async ({ page }) => {
    const pageErrors = []
    page.on('pageerror', (e) => pageErrors.push(e.message))

    await login(page)

    for (const r of ROUTES) {
        pageErrors.length = 0
        await page.goto(r.path)
        await expect(page.locator('body'), `${r.name} body 렌더`).toBeVisible()
        await page.waitForLoadState('networkidle').catch(() => {})
        await page.waitForTimeout(600)
        expect(
            pageErrors,
            `${r.name}(${r.path}) 에서 미처리 JS 예외: ${pageErrors.join(' | ')}`
        ).toEqual([])
    }
})
