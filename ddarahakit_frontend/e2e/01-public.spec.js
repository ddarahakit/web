import { test, expect } from '@playwright/test'

test.describe('공개 페이지 (비로그인)', () => {
    test('메인 페이지 로드', async ({ page }) => {
        await page.goto('/')
        await expect(page).toHaveTitle(/IT|따라/)
    })

    test('코스 목록 - dev DB 코스 표시(API 연동 확인)', async ({ page }) => {
        await page.goto('/course/list')
        // dev DB 의 코스(예: Vue.js)가 보이면 프론트→백엔드 API 연동 정상
        await expect(page.getByText('Vue.js').first()).toBeVisible()
    })

    test('코스 상세 로드 (Vue.js, idx 11)', async ({ page }) => {
        await page.goto('/course/11')
        await expect(page.getByText('Vue.js').first()).toBeVisible()
    })

    test('커뮤니티 목록 로드', async ({ page }) => {
        await page.goto('/community/list')
        await expect(page.locator('body')).toBeVisible()
        // 질문 게시판 헤더/목록 영역
        await expect(page.getByText(/질문|게시판|커뮤니티|글쓰기/).first()).toBeVisible()
    })

    test('로드맵 목록 로드', async ({ page }) => {
        await page.goto('/roadmap')
        // dev DB 로드맵 0건이라 카드 텍스트 대신 페이지 제목으로 로드 확인
        await expect(page).toHaveTitle(/로드맵/)
    })

    test('로그인 페이지 폼 표시', async ({ page }) => {
        await page.goto('/user/login')
        await expect(page.locator('#email')).toBeVisible()
        await expect(page.locator('#password')).toBeVisible()
        await expect(page.locator('#submitBtn')).toBeVisible()
    })
})
