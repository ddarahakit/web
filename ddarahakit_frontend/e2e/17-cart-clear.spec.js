import { test, expect } from '@playwright/test'
import { login } from './helpers.js'

test.describe('장바구니 전체 삭제', () => {
    test('여러 코스 담기 → 선택 삭제(전체) → 빈 장바구니', async ({ page }) => {
        await login(page)

        // 깨끗한 시작
        await page.evaluate(async () => {
            await fetch('http://localhost:8080/cart', { method: 'DELETE', credentials: 'include' })
        })

        // 비구매 코스 2개 담기 (Database=1, Docker=3)
        for (const id of [1, 3]) {
            await page.goto(`/course/${id}`)
            await page.getByRole('button', { name: /수강 신청/ }).first().click()
            await expect(page).toHaveURL(/\/orders\/cart/)
        }

        await page.goto('/orders/cart')
        // 기본 전체 선택 상태 → "선택 삭제"로 전체 제거
        await page.getByRole('button', { name: '선택 삭제' }).click()
        await expect(page.getByText('장바구니가 비어있습니다')).toBeVisible({ timeout: 10000 })
    })
})
