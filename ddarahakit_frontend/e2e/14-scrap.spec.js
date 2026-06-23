import { test, expect } from '@playwright/test'
import { login } from './helpers.js'

test.describe('커뮤니티 스크랩', () => {
    test('스크랩 토글 (켜기 → 끄기)', async ({ page }) => {
        await login(page)
        await page.goto('/community/102')

        // 현재 스크랩 상태 파악
        const isScrapped = await page.getByRole('button', { name: '스크랩됨' })
            .isVisible({ timeout: 8000 }).catch(() => false)
        const initial = isScrapped ? '스크랩됨' : '스크랩'
        const toggled = isScrapped ? '스크랩' : '스크랩됨'

        // 토글
        await page.getByRole('button', { name: initial }).click()
        await expect(page.getByRole('button', { name: toggled })).toBeVisible({ timeout: 10000 })

        // 원복
        await page.getByRole('button', { name: toggled }).click()
        await expect(page.getByRole('button', { name: initial })).toBeVisible({ timeout: 10000 })
    })
})
