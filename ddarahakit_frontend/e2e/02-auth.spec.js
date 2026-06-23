import { test, expect } from '@playwright/test'
import { login } from './helpers.js'

test.describe('인증', () => {
    test('이메일/비밀번호 로그인 성공 → 코스 목록 이동', async ({ page }) => {
        await login(page)
        await expect(page).toHaveURL(/\/course\/list/)
    })

    test('로그인 후 보호 페이지(대시보드) 접근', async ({ page }) => {
        await login(page)
        await page.goto('/user/dashboard')
        await expect(page).toHaveURL(/dashboard/)
        // 로그인 사용자 이름 표시
        await expect(page.getByText(/e2etester/).first()).toBeVisible()
    })

    test('비로그인 시 보호 페이지 → 로그인으로 리다이렉트', async ({ page }) => {
        await page.goto('/user/dashboard')
        await expect(page).toHaveURL(/login/)
    })
})
