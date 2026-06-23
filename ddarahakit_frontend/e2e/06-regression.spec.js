import { test, expect } from '@playwright/test'

// 버그 수정 회귀: 잘못된 로드맵 id 로 상세 페이지가 크래시하던 문제(라우트 숫자 제약으로 방어)
test.describe('회귀: 로드맵 잘못된 id 방어', () => {
    test('/roadmap/list (비숫자) → 미처리 JS 예외 없음', async ({ page }) => {
        const errors = []
        page.on('pageerror', (e) => errors.push(e.message))
        await page.goto('/roadmap/list')
        await page.waitForTimeout(1200)
        expect(errors, `예외 발생: ${errors.join(' | ')}`).toEqual([])
    })

    test('/roadmap/999999 (없는 로드맵, 숫자) → 크래시 없이 처리', async ({ page }) => {
        const errors = []
        page.on('pageerror', (e) => errors.push(e.message))
        await page.goto('/roadmap/999999')
        await page.waitForTimeout(1500)
        expect(errors, `예외 발생: ${errors.join(' | ')}`).toEqual([])
    })
})
