import { test, expect } from '@playwright/test'
import { login } from './helpers.js'

const API = 'http://localhost:8080'

test.describe('장바구니 CRUD', () => {
    test('코스 담기 → 장바구니 표시 → 개별 삭제', async ({ page }) => {
        await login(page)

        // 깨끗한 시작: 장바구니 비우기(로그인 쿠키로)
        await page.evaluate(async (api) => {
            await fetch(`${api}/cart`, { method: 'DELETE', credentials: 'include' })
        }, API)

        // 비구매 코스(Database, idx 1) 상세 → "수강 신청하기"(담기)
        await page.goto('/course/1')
        await expect(page.getByText('Database').first()).toBeVisible()
        await page.getByRole('button', { name: /수강 신청/ }).first().click()

        // 장바구니로 이동 + 담긴 코스 표시
        await expect(page).toHaveURL(/\/orders\/cart/)
        await expect(page.getByRole('heading', { name: 'Database' }).first()).toBeVisible()

        // 항목 개별 삭제(x 버튼)
        await page.locator('button:has(i.fa-xmark)').first().click()

        // 비워짐 확인
        await expect(page.getByText('장바구니가 비어있습니다')).toBeVisible({ timeout: 10000 })
    })
})
