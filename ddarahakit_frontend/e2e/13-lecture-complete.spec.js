import { test, expect } from '@playwright/test'
import { login, PAID_COURSE_IDX, FIRST_LECTURE_IDX } from './helpers.js'

// 주의: 첫 강의(400)가 미완료 상태여야 한다. 재실행 시 e2e 사용자의 lecture_complete 를 비우면 된다.
test.describe('강의 수강완료', () => {
    test('"수강 완료" 클릭 → 완료 처리 + 다음 강의로 SPA 이동', async ({ page }) => {
        await page.addInitScript(() => localStorage.setItem('lectureSidebarClosed', 'false'))
        await login(page)
        await page.goto(`/lecture/${PAID_COURSE_IDX}/${FIRST_LECTURE_IDX}`)
        await expect(page.locator(`a[href="/lecture/${PAID_COURSE_IDX}/${FIRST_LECTURE_IDX}"]`).first())
            .toBeVisible({ timeout: 15000 })

        // 수강 완료(미완료 상태의 클릭 가능한 버튼)
        await page.getByRole('button', { name: '수강 완료' }).first().click()

        // lectureComplete → goNextLecture: 다른 강의로 이동
        await expect(page).toHaveURL(
            new RegExp(`/lecture/${PAID_COURSE_IDX}/(?!${FIRST_LECTURE_IDX}\\b)\\d+`),
            { timeout: 15000 }
        )
    })
})
