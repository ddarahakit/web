import { test, expect } from '@playwright/test'
import { login } from './helpers.js'

test.describe('프로필 수정', () => {
    test('한 줄 소개 수정 → 저장 성공 알림', async ({ page }) => {
        const intro = 'E2E소개-' + Date.now()

        await login(page)
        await page.goto('/user/profile')

        const introInput = page.locator('input[placeholder*="자신을 한 줄로"]')
        await expect(introInput).toBeVisible({ timeout: 15000 })

        await introInput.fill(intro)
        await page.getByRole('button', { name: '변경사항 저장' }).click()

        // 저장 성공 알림 ("변경된 내용이 없습니다"가 아니라 실제 저장)
        await expect(page.getByText('프로필이 저장되었습니다')).toBeVisible({ timeout: 10000 })
    })
})
