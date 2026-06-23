import { test, expect } from '@playwright/test'

test.describe('코스 검색', () => {
    test('헤더 검색창 → 키워드 결과에 매칭 코스 표시', async ({ page }) => {
        await page.goto('/course/list')

        const search = page.locator('input[placeholder="강의 검색"]')
        await search.fill('Vue')
        await search.press('Enter')

        await expect(page).toHaveURL(/keyword=Vue/, { timeout: 15000 })
        await expect(page.getByText('Vue.js').first()).toBeVisible()
    })
})
