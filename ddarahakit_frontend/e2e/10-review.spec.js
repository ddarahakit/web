import { test, expect } from '@playwright/test'
import { login, PAID_COURSE_IDX } from './helpers.js'

test.describe('리뷰 작성/삭제 (구매한 코스)', () => {
    test('후기 작성 → 목록 반영(새로고침 없이) → 삭제', async ({ page }) => {
        const comment = 'E2E후기-' + Date.now()

        await login(page)
        await page.goto(`/course/${PAID_COURSE_IDX}`)
        await page.locator('#section-reviews').scrollIntoViewIfNeeded()

        // 이전 실행 잔여 리뷰가 있으면 먼저 삭제(idempotent)
        const already = page.getByText('이미 후기를 작성하셨습니다')
        if (await already.isVisible({ timeout: 3000 }).catch(() => false)) {
            await page.getByRole('button', { name: '삭제' }).click()
        }
        await expect(page.getByRole('heading', { name: '수강 후기 작성' })).toBeVisible({ timeout: 15000 })

        // 전체 새로고침 감지 마커
        await page.evaluate(() => { window.__noReloadReview = 'kept' })

        // 별점 4점 + 내용
        await page.locator('button[aria-label="별점 4점"]').click()
        await page.locator('textarea[placeholder="수강 후기를 남겨주세요."]').fill(comment)

        // 작성
        const submit = page.getByRole('button', { name: '작성', exact: true })
        await expect(submit).toBeEnabled({ timeout: 8000 })
        await submit.click()

        // 목록에 새 후기 표시 + "이미 작성" 패널로 전환
        await expect(page.getByText(comment).first()).toBeVisible({ timeout: 15000 })
        await expect(page.getByText('이미 후기를 작성하셨습니다')).toBeVisible({ timeout: 10000 })

        // 전체 새로고침 없이 갱신(refetch) 확인
        const kept = await page.evaluate(() => window.__noReloadReview === 'kept')
        expect(kept, '리뷰 작성 후 전체 새로고침이 일어나면 안 됨').toBe(true)

        // 정리: 삭제 → 작성 폼 복귀
        await page.getByRole('button', { name: '삭제' }).click()
        await expect(page.getByRole('heading', { name: '수강 후기 작성' })).toBeVisible({ timeout: 10000 })
    })
})
