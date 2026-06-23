import { test, expect } from '@playwright/test'

test.describe('비로그인 액션 가드', () => {
    test('비로그인 상태에서 "수강 신청하기" → 로그인 페이지로 이동', async ({ page }) => {
        // 비로그인으로 비구매 코스 상세 진입
        await page.goto('/course/1')
        await expect(page.getByText('Database').first()).toBeVisible()

        // 수강 신청하기(addCart) 클릭 → 인증 필요 → 로그인 페이지로 리다이렉트
        await page.getByRole('button', { name: /수강 신청/ }).first().click()
        await expect(page).toHaveURL(/\/user\/login/, { timeout: 10000 })
        // 로그인 후 복귀를 위한 redirect 쿼리 포함
        await expect(page).toHaveURL(/redirect=/)
    })
})
