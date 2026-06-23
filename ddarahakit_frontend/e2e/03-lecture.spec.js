import { test, expect } from '@playwright/test'
import { login, PAID_COURSE_IDX, FIRST_LECTURE_IDX } from './helpers.js'

const SECOND_LECTURE_IDX = 403  // course 11 의 두 번째 강의

// 강의 SPA 네비게이션: window.location.reload/href 제거 → router 전환 검증.
// 전체 새로고침이 발생하면 page.evaluate 로 심은 window 마커가 사라진다(SPA면 유지).
test.describe('강의 SPA 네비게이션', () => {
    test.beforeEach(async ({ page }) => {
        // 사이드바를 열린 상태로 시작(기본 닫힘이라 링크가 뷰포트 밖)
        await page.addInitScript(() => localStorage.setItem('lectureSidebarClosed', 'false'))
        await login(page)
        await page.goto(`/lecture/${PAID_COURSE_IDX}/${FIRST_LECTURE_IDX}`)
        await expect(page).toHaveURL(new RegExp(`/lecture/${PAID_COURSE_IDX}/${FIRST_LECTURE_IDX}`))
        // 첫 강의 사이드바 링크가 보일 때까지 대기(데이터 로드 확인)
        await expect(page.locator(`a[href="/lecture/${PAID_COURSE_IDX}/${FIRST_LECTURE_IDX}"]`).first())
            .toBeVisible({ timeout: 15000 })
    })

    test('"다음 수업" 버튼 → 전체 새로고침 없이 URL·콘텐츠 갱신', async ({ page }) => {
        await page.evaluate(() => { window.__spaMarker = 'kept' })

        await page.getByRole('button', { name: '다음 수업' }).first().click()

        // 다른 강의로 URL 변경
        await expect(page).toHaveURL(
            new RegExp(`/lecture/${PAID_COURSE_IDX}/(?!${FIRST_LECTURE_IDX}\\b)\\d+`),
            { timeout: 15000 }
        )
        // 전체 새로고침이면 마커 소멸 → SPA 라우팅이면 유지
        const kept = await page.evaluate(() => window.__spaMarker === 'kept')
        expect(kept, '전체 새로고침이 일어나면 안 됨 (SPA 라우팅)').toBe(true)
    })

    test('사이드바 강의 클릭 → SPA 이동(새로고침 없음)', async ({ page }) => {
        await page.evaluate(() => { window.__spaMarker2 = 'kept' })

        // 두 번째 강의 사이드바 링크(router-link → <a href>)
        await page.locator(`a[href="/lecture/${PAID_COURSE_IDX}/${SECOND_LECTURE_IDX}"]`).first().click()

        await expect(page).toHaveURL(new RegExp(`/lecture/${PAID_COURSE_IDX}/${SECOND_LECTURE_IDX}`), { timeout: 15000 })
        const kept = await page.evaluate(() => window.__spaMarker2 === 'kept')
        expect(kept, '사이드바 이동도 전체 새로고침 없이 SPA').toBe(true)
    })

    test('"이전 수업" 버튼 → 두 번째 강의에서 첫 강의로 SPA 복귀', async ({ page }) => {
        // 두 번째 강의로 이동
        await page.locator(`a[href="/lecture/${PAID_COURSE_IDX}/${SECOND_LECTURE_IDX}"]`).first().click()
        await expect(page).toHaveURL(new RegExp(`/lecture/${PAID_COURSE_IDX}/${SECOND_LECTURE_IDX}`))

        await page.evaluate(() => { window.__spaMarker3 = 'kept' })
        await page.getByRole('button', { name: '이전 수업' }).first().click()

        await expect(page).toHaveURL(new RegExp(`/lecture/${PAID_COURSE_IDX}/${FIRST_LECTURE_IDX}`), { timeout: 15000 })
        const kept = await page.evaluate(() => window.__spaMarker3 === 'kept')
        expect(kept).toBe(true)
    })
})
