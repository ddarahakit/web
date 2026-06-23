import { test, expect } from '@playwright/test'
import { login } from './helpers.js'

test.describe('로그아웃', () => {
    test('로그아웃 후 보호 페이지 접근 시 로그인으로 리다이렉트', async ({ page }) => {
        await login(page)

        // /logout(LogoutView)이 authStore.logout() 후 메인으로 replace
        await page.goto('/logout')
        await expect(page).not.toHaveURL(/\/logout/, { timeout: 15000 })

        // 로그아웃 확인: 보호 페이지 → 로그인 리다이렉트
        await page.goto('/user/dashboard')
        await expect(page).toHaveURL(/login/)
    })
})
